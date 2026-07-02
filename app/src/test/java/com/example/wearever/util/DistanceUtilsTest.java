package com.example.wearever.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Testa a lógica de distância usada para achar a "cidade mais próxima" na busca
 * por temperatura (WeatherRepository) e na tela de resultado (BuscaTemperaturaActivity).
 *
 * Antes essa lógica dependia de android.location.Location.distanceBetween, que não
 * pode ser chamado em um teste JUnit comum (lança "Method ... not mocked" fora de um
 * teste instrumentado). Com a distância extraída para DistanceUtils, dá pra testar
 * a regra de negócio sem precisar de emulador/dispositivo.
 */
public class DistanceUtilsTest {

    private static final double DELTA_KM = 1.0; // tolerância de 1km para arredondamentos

    @Test
    public void distanciaEntrePontoIguais_deveSerZero() {
        double d = DistanceUtils.distanceKm(-23.55, -46.63, -23.55, -46.63);
        assertEquals(0.0, d, 0.0001);
    }

    @Test
    public void distanciaSaoPauloRioDeJaneiro_deveSerAproximadamente360km() {
        // São Paulo, BR -> Rio de Janeiro, BR (mesmas coordenadas usadas em OpenWeatherService)
        double d = DistanceUtils.distanceKm(-23.55, -46.63, -22.90, -43.17);
        assertEquals(360.0, d, 15.0);
    }

    @Test
    public void distanciaUmQuartoDoEquador_deveBaterComOCalculoTeorico() {
        // De (0,0) até (0,90) é exatamente 1/4 da circunferência da Terra no equador.
        double esperado = (2 * Math.PI * 6371.0) / 4.0;
        double d = DistanceUtils.distanceKm(0, 0, 0, 90);
        assertEquals(esperado, d, DELTA_KM);
    }

    @Test
    public void distancia_deveSerSimetrica() {
        double d1 = DistanceUtils.distanceKm(-23.55, -46.63, 35.69, 139.69);
        double d2 = DistanceUtils.distanceKm(35.69, 139.69, -23.55, -46.63);
        assertEquals(d1, d2, 0.0001);
    }
}
