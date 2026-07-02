package com.example.wearever;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.example.wearever.model.WeatherForecast;
import com.example.wearever.repository.WeatherRepository;
import com.example.wearever.util.DistanceUtils;
import com.example.wearever.util.LocationHelper;

import java.util.Locale;

public class BuscaTemperaturaActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION = 2;

    private EditText etTemperatura;
    private CardView cardResultado;
    private TextView tvCidadeResultado, tvDistancia, tvTemperaturaResultado, tvCondicaoResultado;
    private ProgressBar progressBar;

    private WeatherRepository repository;
    private LocationHelper locationHelper;
    private android.location.Location userLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_busca_temperatura);

        repository = new WeatherRepository(this);
        locationHelper = new LocationHelper(this);

        etTemperatura          = findViewById(R.id.etTemperatura);
        cardResultado          = findViewById(R.id.cardResultado);
        tvCidadeResultado      = findViewById(R.id.tvCidadeResultado);
        tvDistancia            = findViewById(R.id.tvDistancia);
        tvTemperaturaResultado = findViewById(R.id.tvTemperaturaResultado);
        tvCondicaoResultado    = findViewById(R.id.tvCondicaoResultado);
        progressBar            = findViewById(R.id.progressBar);

        findViewById(R.id.ivVoltar).setOnClickListener(v -> finish());
        ((Button) findViewById(R.id.btnBuscar)).setOnClickListener(v -> realizarBusca());

        prefetchLocation();
    }

    private void prefetchLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            return;
        }
        locationHelper.getLocation(new LocationHelper.OnLocationResult() {
            @Override public void onLocation(android.location.Location location) { userLocation = location; }
            @Override public void onError(String message) {}
        });
    }

    private void realizarBusca() {
        String input = etTemperatura.getText().toString().trim();
        if (input.isEmpty()) {
            Toast.makeText(this, "Digite uma temperatura", Toast.LENGTH_SHORT).show();
            return;
        }

        double temp;
        try {
            temp = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Temperatura inválida", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        cardResultado.setVisibility(View.GONE);

        if (userLocation != null) {
            buscar(temp, userLocation);
        } else {
            locationHelper.getLocation(new LocationHelper.OnLocationResult() {
                @Override
                public void onLocation(android.location.Location location) {
                    userLocation = location;
                    buscar(temp, location);
                }
                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(BuscaTemperaturaActivity.this,
                                "Não foi possível obter localização.", Toast.LENGTH_LONG).show();
                    });
                }
            });
        }
    }

    private void buscar(double temp, android.location.Location location) {
        repository.searchByTemperature(temp, location.getLatitude(), location.getLongitude(),
                new WeatherRepository.Callback<WeatherForecast>() {
                    @Override
                    public void onSuccess(WeatherForecast f) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            showResult(f);
                        });
                    }
                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(BuscaTemperaturaActivity.this,
                                    message, Toast.LENGTH_LONG).show();
                        });
                    }
                });
    }

    private void showResult(WeatherForecast f) {
        double distKm = DistanceUtils.distanceKm(
                userLocation.getLatitude(), userLocation.getLongitude(),
                f.getLatitude(), f.getLongitude());

        tvCidadeResultado.setText(f.getCityName() + ", " + f.getCountry());
        tvDistancia.setText(String.format(Locale.getDefault(), "%.0f km", distKm));
        tvTemperaturaResultado.setText(String.format(Locale.getDefault(), "%.0f°C", f.getTemp()));
        tvCondicaoResultado.setText(capitalize(f.getWeatherDescription()));
        cardResultado.setVisibility(View.VISIBLE);
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
            prefetchLocation();
        }
    }
}