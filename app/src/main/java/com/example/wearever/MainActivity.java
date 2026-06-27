package com.example.wearever;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wearever.adapter.PrevisaoAdapter;
import com.example.wearever.model.WeatherForecast;
import com.example.wearever.repository.WeatherRepository;
import com.example.wearever.util.LocationHelper;
import com.example.wearever.db.WeatherDbHelper;

import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION = 1;

    private WeatherRepository repository;
    private LocationHelper locationHelper;

    private TextView tvCidade, tvTemperaturaAtual, tvDescricaoClima;
    private TextView tvTempMax, tvTempMin, tvUmidade, tvVento, tvSensacaoTermica, tvUltimaAtualizacao;
    private RecyclerView rvPrevisao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repository = new WeatherRepository(this);
        locationHelper = new LocationHelper(this);

        new Thread(() -> WeatherDbHelper.getInstance(this).deleteExpiredForecasts(
                TimeUnit.DAYS.toMillis(7))).start(); // remove cache com mais de 7 dias

        bindViews();

        findViewById(R.id.cardBuscaTemperatura).setOnClickListener(v ->
                startActivity(new Intent(this, BuscaTemperaturaActivity.class)));

        findViewById(R.id.ivAtualizar).setOnClickListener(v -> requestLocationAndLoad());
        requestLocationAndLoad();
    }

    private void bindViews() {
        tvCidade            = findViewById(R.id.tvCidade);
        tvTemperaturaAtual  = findViewById(R.id.tvTemperaturaAtual);
        tvDescricaoClima    = findViewById(R.id.tvDescricaoClima);
        tvTempMax           = findViewById(R.id.tvTempMax);
        tvTempMin           = findViewById(R.id.tvTempMin);
        tvUmidade           = findViewById(R.id.tvUmidade);
        tvVento             = findViewById(R.id.tvVento);
        tvSensacaoTermica   = findViewById(R.id.tvSensacaoTermica);
        tvUltimaAtualizacao = findViewById(R.id.tvUltimaAtualizacao);
        rvPrevisao          = findViewById(R.id.rvPrevisao);
    }

    private void requestLocationAndLoad() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            return;
        }
        loadWeather();
    }

    private void loadWeather() {
        locationHelper.getFreshLocation(new LocationHelper.OnLocationResult() {
            @Override
            public void onLocation(android.location.Location location) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                loadCurrentWeather(lat, lon);
                loadForecast(lat, lon);
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show());
            }
        });
    }

    private void loadCurrentWeather(double lat, double lon) {
        repository.getCurrentWeather(lat, lon, new WeatherRepository.Callback<WeatherForecast>() {
            @Override
            public void onSuccess(WeatherForecast f) {
                runOnUiThread(() -> updateCurrentWeatherUI(f));
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Erro: " + message, Toast.LENGTH_LONG).show());
            }
        });
    }

    private void loadForecast(double lat, double lon) {
        repository.getForecast(lat, lon, new WeatherRepository.Callback<List<WeatherForecast>>() {
            @Override
            public void onSuccess(List<WeatherForecast> forecasts) {
                runOnUiThread(() -> updateForecastUI(forecasts));
            }

            @Override
            public void onError(String message) {}
        });
    }

    private void updateCurrentWeatherUI(WeatherForecast f) {
        tvCidade.setText(f.getCityName() + ", " + f.getCountry());
        tvTemperaturaAtual.setText(String.format(Locale.getDefault(), "%.0f°", f.getTemp()));
        tvDescricaoClima.setText(capitalize(f.getWeatherDescription()));
        tvTempMax.setText(String.format(Locale.getDefault(), "%.0f°", f.getTempMax()));
        tvTempMin.setText(String.format(Locale.getDefault(), "%.0f°", f.getTempMin()));
        tvUmidade.setText(f.getHumidity() + "%");
        tvVento.setText(String.format(Locale.getDefault(), "%.0f km/h", f.getWindSpeed() * 3.6));
        tvSensacaoTermica.setText(String.format(Locale.getDefault(), "%.0f°", f.getFeelsLike()));
        tvUltimaAtualizacao.setText("Atualizado às " +
                new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));

        long ageSeconds = (System.currentTimeMillis() / 1000L) - f.getCachedAt();
        if (ageSeconds > 60) { // mais de 1 minuto = veio do cache, não da API
            tvUltimaAtualizacao.setText("Dados salvos (offline) — " +
                    new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(f.getCachedAt() * 1000)));
        }
    }

    private void updateForecastUI(List<WeatherForecast> forecasts) {
        rvPrevisao.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvPrevisao.setAdapter(new PrevisaoAdapter(this, forecasts));
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadWeather();
        } else {
            Toast.makeText(this, "Permissão de localização necessária.", Toast.LENGTH_SHORT).show();
        }
    }
}