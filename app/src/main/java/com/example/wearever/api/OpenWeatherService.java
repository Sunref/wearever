package com.example.wearever.api;

import com.example.wearever.model.WeatherForecast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class OpenWeatherService {

    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";
    private static final String API_KEY = "5c82605a03467ad753cc5c30b1adea8f";
    private static final String UNITS = "metric";
    private static final String LANG = "pt_br";

    // 40 cidades distribuídas globalmente com climas variados
    private static final double[][] WORLD_CITIES = {
            // {lat, lon}
            // América do Sul
            {-23.55, -46.63},  // São Paulo, BR
            {-22.90, -43.17},  // Rio de Janeiro, BR
            {-30.03, -51.23},  // Porto Alegre, BR
            {-3.10,  -60.02},  // Manaus, BR
            {-34.61, -58.37},  // Buenos Aires, AR
            {-33.45, -70.67},  // Santiago, CL
            {-12.05, -77.05},  // Lima, PE

            // América do Norte
            {40.71,  -74.01},  // New York, US
            {41.85,  -87.65},  // Chicago, US
            {34.05, -118.24},  // Los Angeles, US
            {25.77,  -80.19},  // Miami, US
            {47.61, -122.33},  // Seattle, US
            {64.20, -149.49},  // Fairbanks, US (frio extremo)
            {45.50,  -73.57},  // Montreal, CA
            {19.43,  -99.13},  // Cidade do México, MX

            // Europa
            {51.51,   -0.13},  // London, GB
            {48.85,    2.35},  // Paris, FR
            {52.52,   13.41},  // Berlin, DE
            {41.90,   12.50},  // Rome, IT
            {59.33,   18.07},  // Stockholm, SE
            {60.17,   24.94},  // Helsinki, FI
            {68.97,   33.09},  // Murmansk, RU (neve quase sempre)

            // Ásia
            {35.69,  139.69},  // Tokyo, JP
            {37.57,  126.98},  // Seoul, KR
            {39.91,  116.39},  // Beijing, CN
            {28.61,   77.21},  // New Delhi, IN
            {13.75,  100.52},  // Bangkok, TH
            { 1.29,  103.85},  // Singapore, SG
            {43.10,   76.10},  // Almaty, KZ (frio continental)

            // Oriente Médio
            {25.20,   55.27},  // Dubai, AE
            {24.69,   46.72},  // Riyadh, SA
            {30.06,   31.25},  // Cairo, EG

            // África
            { 6.45,    3.47},  // Lagos, NG
            {-1.29,   36.82},  // Nairobi, KE
            {-33.93,  18.42},  // Cape Town, ZA

            // Oceania
            {-33.87, 151.21},  // Sydney, AU
            {-37.81, 144.96},  // Melbourne, AU
            {-36.87, 174.77},  // Auckland, NZ

            // Climas extremos extras
            {-90.00,    0.00}, // Polo Sul (aproximado)
            {78.22,   15.65},  // Longyearbyen, NO (ártico)
    };

    public WeatherForecast fetchCurrentWeather(double lat, double lon) throws Exception {
        String endpoint = BASE_URL + "weather?lat=" + lat + "&lon=" + lon
                + "&appid=" + API_KEY + "&units=" + UNITS + "&lang=" + LANG;
        JSONObject json = request(endpoint);
        return parseWeather(json);
    }

    public List<WeatherForecast> fetchForecast(double lat, double lon) throws Exception {
        String endpoint = BASE_URL + "forecast?lat=" + lat + "&lon=" + lon
                + "&appid=" + API_KEY + "&units=" + UNITS + "&lang=" + LANG + "&cnt=5";
        JSONObject json = request(endpoint);
        return parseForecast(json);
    }

    public List<WeatherForecast> fetchCitiesByTemperature(double targetTemp) throws Exception {
        List<WeatherForecast> matched = new ArrayList<>();
        double tolerance = 5.0;

        for (double[] city : WORLD_CITIES) {
            try {
                String endpoint = BASE_URL + "weather?lat=" + city[0] + "&lon=" + city[1]
                        + "&appid=" + API_KEY + "&units=" + UNITS + "&lang=" + LANG;
                JSONObject json = request(endpoint);
                double temp = json.getJSONObject("main").getDouble("temp");

                if (Math.abs(temp - targetTemp) <= tolerance) {
                    matched.add(parseWeather(json));
                }
            } catch (Exception e) {
                // Segue para a próxima cidade se uma falhar
            }
        }

        return matched;
    }

    private JSONObject request(String endpoint) throws Exception {
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        int code = conn.getResponseCode();
        if (code != HttpURLConnection.HTTP_OK) {
            throw new Exception("HTTP " + code);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        conn.disconnect();

        return new JSONObject(sb.toString());
    }

    private WeatherForecast parseWeather(JSONObject json) throws Exception {
        WeatherForecast f = new WeatherForecast();
        f.setCityId(String.valueOf(json.getLong("id")));
        f.setCityName(json.getString("name"));
        f.setCountry(json.getJSONObject("sys").getString("country"));

        JSONObject coord = json.getJSONObject("coord");
        f.setLatitude(coord.getDouble("lat"));
        f.setLongitude(coord.getDouble("lon"));

        JSONObject weather = json.getJSONArray("weather").getJSONObject(0);
        f.setWeatherCondition(weather.getString("main"));
        f.setWeatherDescription(weather.getString("description"));
        f.setWeatherIcon(weather.getString("icon"));

        JSONObject main = json.getJSONObject("main");
        f.setTemp(main.getDouble("temp"));
        f.setTempMin(main.getDouble("temp_min"));
        f.setTempMax(main.getDouble("temp_max"));
        f.setFeelsLike(main.getDouble("feels_like"));
        f.setHumidity(main.getInt("humidity"));

        f.setWindSpeed(json.getJSONObject("wind").getDouble("speed"));
        f.setVisibility(json.optInt("visibility", 0));
        f.setForecastTimestamp(json.getLong("dt"));
        f.setCachedAt(System.currentTimeMillis() / 1000L);
        return f;
    }

    private List<WeatherForecast> parseForecast(JSONObject json) throws Exception {
        List<WeatherForecast> list = new ArrayList<>();
        org.json.JSONArray items = json.getJSONArray("list");
        JSONObject cityObj = json.getJSONObject("city");
        String cityId = String.valueOf(cityObj.getLong("id"));
        String cityName = cityObj.getString("name");
        String country = cityObj.getString("country");
        double lat = cityObj.getJSONObject("coord").getDouble("lat");
        double lon = cityObj.getJSONObject("coord").getDouble("lon");

        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            WeatherForecast f = new WeatherForecast();
            f.setCityId(cityId);
            f.setCityName(cityName);
            f.setCountry(country);
            f.setLatitude(lat);
            f.setLongitude(lon);

            JSONObject weather = item.getJSONArray("weather").getJSONObject(0);
            f.setWeatherCondition(weather.getString("main"));
            f.setWeatherDescription(weather.getString("description"));
            f.setWeatherIcon(weather.getString("icon"));

            JSONObject main = item.getJSONObject("main");
            f.setTemp(main.getDouble("temp"));
            f.setTempMin(main.getDouble("temp_min"));
            f.setTempMax(main.getDouble("temp_max"));
            f.setFeelsLike(main.getDouble("feels_like"));
            f.setHumidity(main.getInt("humidity"));

            f.setWindSpeed(item.getJSONObject("wind").getDouble("speed"));
            f.setVisibility(item.optInt("visibility", 0));
            f.setForecastTimestamp(item.getLong("dt"));
            f.setCachedAt(System.currentTimeMillis() / 1000L);
            list.add(f);
        }
        return list;
    }
}