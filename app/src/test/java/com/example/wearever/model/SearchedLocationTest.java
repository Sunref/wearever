package com.example.wearever.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SearchedLocationTest {

    @Test
    public void construtorCompleto_devePreencherTodosOsCamposEDataDaBusca() {
        long antes = System.currentTimeMillis() / 1000L;

        SearchedLocation s = new SearchedLocation(
                "25.0", "456", "Rio de Janeiro", "BR",
                -22.90, -43.17,
                360.0,
                -23.55, -46.63
        );

        long depois = System.currentTimeMillis() / 1000L;

        assertEquals("25.0", s.getDesiredCondition());
        assertEquals("456", s.getResultCityId());
        assertEquals("Rio de Janeiro", s.getResultCityName());
        assertEquals("BR", s.getResultCountry());
        assertEquals(-22.90, s.getResultLatitude(), 0.0001);
        assertEquals(-43.17, s.getResultLongitude(), 0.0001);
        assertEquals(360.0, s.getDistanceKm(), 0.0001);
        assertEquals(-23.55, s.getUserLatitude(), 0.0001);
        assertEquals(-46.63, s.getUserLongitude(), 0.0001);

        // searchedAt é preenchido automaticamente pelo construtor com o instante atual
        assertTrue("searchedAt deveria estar entre 'antes' e 'depois'",
                s.getSearchedAt() >= antes && s.getSearchedAt() <= depois);
    }
}
