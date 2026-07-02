package com.example.wearever.util;

/**
 * Cálculo de distância entre coordenadas usando a fórmula de Haversine.
 *
 * Extraído como classe pura em Java (sem depender de android.location.Location)
 * para que a lógica de "cidade mais próxima" possa ser coberta por testes
 * unitários JUnit comuns, sem precisar de Robolectric/instrumentação —
 * chamar Location.distanceBetween() fora de um teste instrumentado lança
 * "not mocked".
 */
public final class DistanceUtils {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private DistanceUtils() {}

    /**
     * Retorna a distância em quilômetros entre dois pontos geográficos.
     */
    public static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}
