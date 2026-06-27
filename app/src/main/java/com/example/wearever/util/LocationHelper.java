package com.example.wearever.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;

public class LocationHelper {

    public interface OnLocationResult {
        void onLocation(Location location);
        void onError(String message);
    }

    private final Context context;
    private final LocationManager locationManager;

    public LocationHelper(Context context) {
        this.context = context;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    /**
     * Pega localização rapidamente, usando cache se disponível.
     * Ideal para o carregamento inicial do app.
     */
    public void getLocation(OnLocationResult callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            callback.onError("Permissão de localização não concedida.");
            return;
        }

        // Tenta última localização conhecida primeiro (instantâneo)
        Location last = getLastKnown();
        if (last != null) {
            callback.onLocation(last);
            return;
        }

        requestFreshLocation(callback);
    }

    /**
     * Ignora o cache e força uma nova leitura de localização.
     * Use quando o usuário clicar em "atualizar" manualmente.
     */
    public void getFreshLocation(OnLocationResult callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            callback.onError("Permissão de localização não concedida.");
            return;
        }

        requestFreshLocation(callback);
    }

    private void requestFreshLocation(OnLocationResult callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            callback.onError("Permissão de localização não concedida.");
            return;
        }
        String provider = getBestProvider();
        if (provider == null) {
            callback.onError("Nenhum provedor de localização disponível.");
            return;
        }

        locationManager.requestSingleUpdate(provider, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                callback.onLocation(location);
            }

            @Override
            public void onProviderDisabled(String provider) {
                callback.onError("Provedor de localização desativado.");
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}
        }, null);
    }

    private Location getLastKnown() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return null;

        Location best = null;
        for (String provider : locationManager.getProviders(true)) {
            Location l = locationManager.getLastKnownLocation(provider);
            if (l != null && (best == null || l.getAccuracy() < best.getAccuracy())) {
                best = l;
            }
        }
        return best;
    }

    private String getBestProvider() {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            return LocationManager.GPS_PROVIDER;
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            return LocationManager.NETWORK_PROVIDER;
        return null;
    }
}