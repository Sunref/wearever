package com.example.wearever.model;

/**
 * Model que representa uma previsão do tempo.
 */
public class WeatherForecast {

	private long id;
	private String cityId;
	private String cityName;
	private String country;
	private double latitude;
	private double longitude;

	/** Ex: chuva, tempo limpo, neve, nublado etc */
	private String weatherCondition;

	/** Ex: tempestade */
	private String weatherDescription;

	/** Código do ícone */
	private String weatherIcon;

	private double temp;
	private double tempMin;
	private double tempMax;
	private double feelsLike;
	private int humidity;
	private double windSpeed;
	private int visibility;

	/** Timestamp em segundos da previsão */
	private long forecastTimestamp;

	/** Timestamp em segundos de quando foi salvo no cache */
	private long cachedAt;

	// Construtores

	public WeatherForecast() {}

	public WeatherForecast(
		String cityId,
		String cityName,
		String country,
		double latitude,
		double longitude,
		String weatherCondition,
		String weatherDescription,
		String weatherIcon,
		double temp,
		double tempMin,
		double tempMax,
		double feelsLike,
		int humidity,
		double windSpeed,
		int visibility,
		long forecastTimestamp
	) {
		this.cityId = cityId;
		this.cityName = cityName;
		this.country = country;
		this.latitude = latitude;
		this.longitude = longitude;
		this.weatherCondition = weatherCondition;
		this.weatherDescription = weatherDescription;
		this.weatherIcon = weatherIcon;
		this.temp = temp;
		this.tempMin = tempMin;
		this.tempMax = tempMax;
		this.feelsLike = feelsLike;
		this.humidity = humidity;
		this.windSpeed = windSpeed;
		this.visibility = visibility;
		this.forecastTimestamp = forecastTimestamp;
		this.cachedAt = System.currentTimeMillis() / 1000L;
	}

	// Getters & Setters

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getCityId() {
		return cityId;
	}

	public void setCityId(String cityId) {
		this.cityId = cityId;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String getWeatherCondition() {
		return weatherCondition;
	}

	public void setWeatherCondition(String weatherCondition) {
		this.weatherCondition = weatherCondition;
	}

	public String getWeatherDescription() {
		return weatherDescription;
	}

	public void setWeatherDescription(String weatherDescription) {
		this.weatherDescription = weatherDescription;
	}

	public String getWeatherIcon() {
		return weatherIcon;
	}

	public void setWeatherIcon(String weatherIcon) {
		this.weatherIcon = weatherIcon;
	}

	public double getTemp() {
		return temp;
	}

	public void setTemp(double temp) {
		this.temp = temp;
	}

	public double getTempMin() {
		return tempMin;
	}

	public void setTempMin(double tempMin) {
		this.tempMin = tempMin;
	}

	public double getTempMax() {
		return tempMax;
	}

	public void setTempMax(double tempMax) {
		this.tempMax = tempMax;
	}

	public double getFeelsLike() {
		return feelsLike;
	}

	public void setFeelsLike(double feelsLike) {
		this.feelsLike = feelsLike;
	}

	public int getHumidity() {
		return humidity;
	}

	public void setHumidity(int humidity) {
		this.humidity = humidity;
	}

	public double getWindSpeed() {
		return windSpeed;
	}

	public void setWindSpeed(double windSpeed) {
		this.windSpeed = windSpeed;
	}

	public int getVisibility() {
		return visibility;
	}

	public void setVisibility(int visibility) {
		this.visibility = visibility;
	}

	public long getForecastTimestamp() {
		return forecastTimestamp;
	}

	public void setForecastTimestamp(long forecastTimestamp) {
		this.forecastTimestamp = forecastTimestamp;
	}

	public long getCachedAt() {
		return cachedAt;
	}

	public void setCachedAt(long cachedAt) {
		this.cachedAt = cachedAt;
	}

	@Override
	public String toString() {
		return (
			"WeatherForecast{" +
			"cityName='" +
			cityName +
			'\'' +
			", condition='" +
			weatherCondition +
			'\'' +
			", temp=" +
			temp +
			", lat=" +
			latitude +
			", lon=" +
			longitude +
			'}'
		);
	}
}
