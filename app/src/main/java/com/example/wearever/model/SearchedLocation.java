package com.example.wearever.model;

/**
 * Model que representa a busca realizada.
 */
public class SearchedLocation {

	private long id;

	/** Condição buscada */
	private String desiredCondition;

	/** Cidade mais próxima encontrada */
	private String resultCityId;
	private String resultCityName;
	private String resultCountry;
	private double resultLatitude;
	private double resultLongitude;

	/** Distância em km entre usuário e cidade resultado */
	private double distanceKm;

	/** Coordenadas do usuário no momento da busca */
	private double userLatitude;
	private double userLongitude;

	/** Timestamp em segundos de quando a busca ocorreu */
	private long searchedAt;

	// Construtores

	public SearchedLocation() {}

	public SearchedLocation(
		String desiredCondition,
		String resultCityId,
		String resultCityName,
		String resultCountry,
		double resultLatitude,
		double resultLongitude,
		double distanceKm,
		double userLatitude,
		double userLongitude
	) {
		this.desiredCondition = desiredCondition;
		this.resultCityId = resultCityId;
		this.resultCityName = resultCityName;
		this.resultCountry = resultCountry;
		this.resultLatitude = resultLatitude;
		this.resultLongitude = resultLongitude;
		this.distanceKm = distanceKm;
		this.userLatitude = userLatitude;
		this.userLongitude = userLongitude;
		this.searchedAt = System.currentTimeMillis() / 1000L;
	}

	// Getters & Setters

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getDesiredCondition() {
		return desiredCondition;
	}

	public void setDesiredCondition(String desiredCondition) {
		this.desiredCondition = desiredCondition;
	}

	public String getResultCityId() {
		return resultCityId;
	}

	public void setResultCityId(String resultCityId) {
		this.resultCityId = resultCityId;
	}

	public String getResultCityName() {
		return resultCityName;
	}

	public void setResultCityName(String resultCityName) {
		this.resultCityName = resultCityName;
	}

	public String getResultCountry() {
		return resultCountry;
	}

	public void setResultCountry(String resultCountry) {
		this.resultCountry = resultCountry;
	}

	public double getResultLatitude() {
		return resultLatitude;
	}

	public void setResultLatitude(double resultLatitude) {
		this.resultLatitude = resultLatitude;
	}

	public double getResultLongitude() {
		return resultLongitude;
	}

	public void setResultLongitude(double resultLongitude) {
		this.resultLongitude = resultLongitude;
	}

	public double getDistanceKm() {
		return distanceKm;
	}

	public void setDistanceKm(double distanceKm) {
		this.distanceKm = distanceKm;
	}

	public double getUserLatitude() {
		return userLatitude;
	}

	public void setUserLatitude(double userLatitude) {
		this.userLatitude = userLatitude;
	}

	public double getUserLongitude() {
		return userLongitude;
	}

	public void setUserLongitude(double userLongitude) {
		this.userLongitude = userLongitude;
	}

	public long getSearchedAt() {
		return searchedAt;
	}

	public void setSearchedAt(long searchedAt) {
		this.searchedAt = searchedAt;
	}

	@Override
	public String toString() {
		return (
			"SearchedLocation{" +
			"desiredCondition='" +
			desiredCondition +
			'\'' +
			", resultCity='" +
			resultCityName +
			"' (" +
			resultCountry +
			")" +
			", distanceKm=" +
			distanceKm +
			'}'
		);
	}
}
