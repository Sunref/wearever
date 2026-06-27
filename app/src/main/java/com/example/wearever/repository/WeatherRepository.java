package com.example.wearever.repository;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.example.wearever.api.OpenWeatherService;
import com.example.wearever.db.WeatherDbHelper;
import com.example.wearever.model.SearchedLocation;
import com.example.wearever.model.WeatherForecast;

import java.util.List;

public class WeatherRepository {

    private static final String TAG = "WeatherRepository";

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

    public void getCurrentWeather(double lat, double lon, Callback<WeatherForecast> callback) {
        new Thread(() -> {
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
        }).start();
    }

    public void getForecast(double lat, double lon, Callback<List<WeatherForecast>> callback) {
        new Thread(() -> {
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
        }).start();
    }

    public void searchByTemperature(double targetTemp, double userLat, double userLon,
                                    Callback<WeatherForecast> callback) {
        new Thread(() -> {
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
                float bestDist = Float.MAX_VALUE;
                float[] dist = new float[1];

                for (WeatherForecast f : results) {
                    if (f.getCityId().equals(userCityId)) continue;
                    Location.distanceBetween(f.getLatitude(), f.getLongitude(),
                            userLat, userLon, dist);
                    if (dist[0] < bestDist) {
                        bestDist = dist[0];
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
        }).start();
    }

    private void saveSearchHistory(String condition, WeatherForecast result,
                                   double userLat, double userLon) {
        float[] d = new float[1];
        Location.distanceBetween(userLat, userLon,
                result.getLatitude(), result.getLongitude(), d);
        SearchedLocation sl = new SearchedLocation(
                condition,
                result.getCityId(),
                result.getCityName(),
                result.getCountry(),
                result.getLatitude(),
                result.getLongitude(),
                d[0] / 1000.0,
                userLat,
                userLon
        );
        db.insertSearchedLocation(sl);
    }
}