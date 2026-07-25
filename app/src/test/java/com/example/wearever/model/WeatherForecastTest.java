package com.example.wearever.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WeatherForecastTest {

    @Test
    public void construtorCompleto_devePreencherTodosOsCampos() {
        long antes = System.currentTimeMillis() / 1000L;

        WeatherForecast f = new WeatherForecast(
                "123", "São Paulo", "BR",
                -23.55, -46.63,
                "Clouds", "nublado", "02d",
                25.0, 20.0, 28.0, 24.0,
                68, 3.5, 10000,
                1_700_000_000L
        );

        long depois = System.currentTimeMillis() / 1000L;

        assertEquals("123", f.getCityId());
        assertEquals("São Paulo", f.getCityName());
        assertEquals("BR", f.getCountry());
        assertEquals(-23.55, f.getLatitude(), 0.0001);
        assertEquals(-46.63, f.getLongitude(), 0.0001);
        assertEquals("Clouds", f.getWeatherCondition());
        assertEquals("nublado", f.getWeatherDescription());
        assertEquals("02d", f.getWeatherIcon());
        assertEquals(25.0, f.getTemp(), 0.0001);
        assertEquals(20.0, f.getTempMin(), 0.0001);
        assertEquals(28.0, f.getTempMax(), 0.0001);
        assertEquals(24.0, f.getFeelsLike(), 0.0001);
        assertEquals(68, f.getHumidity());
        assertEquals(3.5, f.getWindSpeed(), 0.0001);
        assertEquals(10000, f.getVisibility());
        assertEquals(1_700_000_000L, f.getForecastTimestamp());

        // cachedAt é preenchido automaticamente pelo construtor com o instante atual
        assertTrue("cachedAt deveria estar entre 'antes' e 'depois'",
                f.getCachedAt() >= antes && f.getCachedAt() <= depois);
    }

    @Test
    public void construtorVazio_maisSetters_devemFuncionarIgual() {
        WeatherForecast f = new WeatherForecast();
        f.setId(7L);
        f.setCityName("Campinas");
        f.setTemp(22.5);

        assertEquals(7L, f.getId());
        assertEquals("Campinas", f.getCityName());
        assertEquals(22.5, f.getTemp(), 0.0001);
        // Sem passar pelo construtor completo, cachedAt permanece no valor padrão (0)
        assertEquals(0L, f.getCachedAt());
    }
}
