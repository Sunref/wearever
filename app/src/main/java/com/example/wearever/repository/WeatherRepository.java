package com.example.wearever.repository;

import android.content.Context;
import android.util.Log;

import com.example.wearever.api.OpenWeatherService;
import com.example.wearever.db.WeatherDbHelper;
import com.example.wearever.model.SearchedLocation;
import com.example.wearever.model.WeatherForecast;
import com.example.wearever.util.DistanceUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeatherRepository {

    private static final String TAG = "WeatherRepository";

    /**
     * Pool compartilhado por todas as instâncias do repositório. Antes cada chamada
     * (getCurrentWeather, getForecast, searchByTemperature...) criava uma Thread nova
     * do zero; com várias chamadas seguidas (ex: usuário batendo no refresh), isso
     * gerava threads demais sem necessidade. Reaproveitar um pool fixo é mais leve.
     */
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    private final WeatherDbHelper db;
    private final OpenWeatherService service;

    public WeatherRepository(Context context) {
        this.db = WeatherDbHelper.getInstance(context);
        this.service = new OpenWeatherService();
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    /**
     * Remove do cache as previsões mais antigas que maxAgeMs. Roda em background.
     */
    public void pruneOldCache(long maxAgeMs) {
        executor.execute(() -> db.deleteExpiredForecasts(maxAgeMs));
    }

    public void getCurrentWeather(double lat, double lon, Callback<WeatherForecast> callback) {
        executor.execute(() -> {
            try {
                List<WeatherForecast> cached = db.getForecastsByCity(
                        lat + "," + lon, WeatherDbHelper.CACHE_TTL_MS);
                if (!cached.isEmpty()) {
                    Log.d(TAG, "Cache hit: clima atual");
                    callback.onSuccess(cached.get(0));
                    return;
                }

                WeatherForecast forecast = service.fetchCurrentWeather(lat, lon);
                if (forecast.getCityId() == null) forecast.setCityId(lat + "," + lon);
                db.insertForecast(forecast);
                callback.onSuccess(forecast);
            } catch (Exception e) {
                Log.e(TAG, "Erro ao buscar clima atual: " + e.getMessage());
                // Fallback: tenta cache expirado antes de desistir
                List<WeatherForecast> staleCache = db.getForecastsByCity(
                        lat + "," + lon, Long.MAX_VALUE);
                if (!staleCache.isEmpty()) {
                    callback.onSuccess(staleCache.get(0));
                } else {
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    public void getForecast(double lat, double lon, Callback<List<WeatherForecast>> callback) {
        executor.execute(() -> {
            try {
                List<WeatherForecast> forecasts = service.fetchForecast(lat, lon);
                db.insertForecasts(forecasts);
                callback.onSuccess(forecasts);
            } catch (Exception e) {
                Log.e(TAG, "Erro ao buscar previsão: " + e.getMessage());
                // fallback: tenta cache expirado antes de desistir
                List<WeatherForecast> staleCache = db.getForecastsByCity(
                        lat + "," + lon, Long.MAX_VALUE);
                if (!staleCache.isEmpty()) {
                    callback.onSuccess(staleCache);
                } else {
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    public void searchByTemperature(double targetTemp, double userLat, double userLon,
                                    Callback<WeatherForecast> callback) {
        executor.execute(() -> {
            try {
                List<WeatherForecast> results = service.fetchCitiesByTemperature(targetTemp);

                if (results.isEmpty()) {
                    callback.onError("Nenhuma cidade encontrada com essa temperatura agora.");
                    return;
                }

                // Exclui a própria cidade do usuário se ela aparecer
                String userCityId = null;
                List<WeatherForecast> userCache = db.getForecastsByCity(
                        userLat + "," + userLon, WeatherDbHelper.CACHE_TTL_MS);
                if (!userCache.isEmpty()) userCityId = userCache.get(0).getCityId();

                WeatherForecast best = null;
                double bestDistKm = Double.MAX_VALUE;

                for (WeatherForecast f : results) {
                    if (f.getCityId().equals(userCityId)) continue;
                    double distKm = DistanceUtils.distanceKm(
                            f.getLatitude(), f.getLongitude(), userLat, userLon);
                    if (distKm < bestDistKm) {
                        bestDistKm = distKm;
                        best = f;
                    }
                }

                if (best == null) best = results.get(0);

                db.insertForecasts(results);
                saveSearchHistory(String.valueOf(targetTemp), best, userLat, userLon);
                callback.onSuccess(best);
            } catch (Exception e) {
                Log.e(TAG, "Erro na busca por temperatura: " + e.getMessage());
                callback.onError(e.getMessage());
            }
        });
    }

    private void saveSearchHistory(String condition, WeatherForecast result,
                                   double userLat, double userLon) {
        double distKm = DistanceUtils.distanceKm(userLat, userLon,
                result.getLatitude(), result.getLongitude());
        SearchedLocation sl = new SearchedLocation(
                condition,
                result.getCityId(),
                result.getCityName(),
                result.getCountry(),
                result.getLatitude(),
                result.getLongitude(),
                distKm,
                userLat,
                userLon
        );
        db.insertSearchedLocation(sl);
    }
}