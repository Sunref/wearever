package com.example.wearever.db;

import android.provider.BaseColumns;

/**
 * Contrato do banco de dados SQLite.
 * Define as tabelas e colunas usadas para armazenar a previsões do tempo e as localidades
 */
public final class WeatherContract {

	private WeatherContract() {}

	// -----------------------------------------------------------------------
	// Armazena as previsões obtidas da API
	// -----------------------------------------------------------------------
	public static final class WeatherEntry implements BaseColumns {

		public static final String TABLE_NAME = "weather_forecasts";

		public static final String COLUMN_CITY_ID = "city_id";

		public static final String COLUMN_CITY_NAME = "city_name";

		public static final String COLUMN_COUNTRY = "country";

		public static final String COLUMN_LATITUDE = "latitude";

		public static final String COLUMN_LONGITUDE = "longitude";

		/**
		 * Condição climática
		 */
		public static final String COLUMN_WEATHER_CONDITION =
			"weather_condition";

		public static final String COLUMN_WEATHER_DESCRIPTION =
			"weather_description";

		public static final String COLUMN_WEATHER_ICON = "weather_icon";

		public static final String COLUMN_TEMP = "temp";

		public static final String COLUMN_TEMP_MIN = "temp_min";

		public static final String COLUMN_TEMP_MAX = "temp_max";

		public static final String COLUMN_FEELS_LIKE = "feels_like";

		public static final String COLUMN_HUMIDITY = "humidity";

		public static final String COLUMN_WIND_SPEED = "wind_speed";

		public static final String COLUMN_VISIBILITY = "visibility";

		/**
		 * Timestamp (segundos) da previsão.
		 */
		public static final String COLUMN_FORECAST_TIMESTAMP =
			"forecast_timestamp";

		/**
		 * Timestamp Unix (segundos) de quando o registro foi salvo
		 */
		public static final String COLUMN_CACHED_AT = "cached_at";

		public static final String SQL_CREATE_TABLE =
			"CREATE TABLE " +
			TABLE_NAME +
			" (" +
			_ID +
			" INTEGER PRIMARY KEY AUTOINCREMENT, " +
			COLUMN_CITY_ID +
			" TEXT NOT NULL, " +
			COLUMN_CITY_NAME +
			" TEXT NOT NULL, " +
			COLUMN_COUNTRY +
			" TEXT, " +
			COLUMN_LATITUDE +
			" REAL NOT NULL, " +
			COLUMN_LONGITUDE +
			" REAL NOT NULL, " +
			COLUMN_WEATHER_CONDITION +
			" TEXT NOT NULL, " +
			COLUMN_WEATHER_DESCRIPTION +
			" TEXT, " +
			COLUMN_WEATHER_ICON +
			" TEXT, " +
			COLUMN_TEMP +
			" REAL, " +
			COLUMN_TEMP_MIN +
			" REAL, " +
			COLUMN_TEMP_MAX +
			" REAL, " +
			COLUMN_FEELS_LIKE +
			" REAL, " +
			COLUMN_HUMIDITY +
			" INTEGER, " +
			COLUMN_WIND_SPEED +
			" REAL, " +
			COLUMN_VISIBILITY +
			" INTEGER, " +
			COLUMN_FORECAST_TIMESTAMP +
			" INTEGER NOT NULL, " +
			COLUMN_CACHED_AT +
			" INTEGER NOT NULL" +
			");";

		/** Índice para buscas rápidas por condição climática */
		public static final String SQL_CREATE_INDEX_CONDITION =
			"CREATE INDEX idx_weather_condition ON " +
			TABLE_NAME +
			" (" +
			COLUMN_WEATHER_CONDITION +
			");";

		/** Índice para buscas por cidade + timestamp */
		public static final String SQL_CREATE_INDEX_CITY_TIME =
			"CREATE INDEX idx_city_time ON " +
			TABLE_NAME +
			" (" +
			COLUMN_CITY_ID +
			", " +
			COLUMN_FORECAST_TIMESTAMP +
			");";

		public static final String SQL_DROP_TABLE =
			"DROP TABLE IF EXISTS " + TABLE_NAME + ";";
	}

	// -----------------------------------------------------------------------
	// Guarda o histórico de buscas
	// -----------------------------------------------------------------------
	public static final class LocationEntry implements BaseColumns {

		public static final String TABLE_NAME = "searched_locations";

		public static final String COLUMN_DESIRED_CONDITION =
			"desired_condition";

		public static final String COLUMN_RESULT_CITY_ID = "result_city_id";

		public static final String COLUMN_RESULT_CITY_NAME = "result_city_name";

		public static final String COLUMN_RESULT_COUNTRY = "result_country";

		public static final String COLUMN_RESULT_LATITUDE = "result_latitude";

		public static final String COLUMN_RESULT_LONGITUDE = "result_longitude";

		/**
		 * Distância em km entre a localização atual e a cidade resultado
		 */
		public static final String COLUMN_DISTANCE_KM = "distance_km";

		/** Latitude no momento da busca */
		public static final String COLUMN_USER_LATITUDE = "user_latitude";

		/** Longitude no momento da busca */
		public static final String COLUMN_USER_LONGITUDE = "user_longitude";

		/** Timestamp (segundos) da busca */
		public static final String COLUMN_SEARCHED_AT = "searched_at";

		public static final String SQL_CREATE_TABLE =
			"CREATE TABLE " +
			TABLE_NAME +
			" (" +
			_ID +
			" INTEGER PRIMARY KEY AUTOINCREMENT, " +
			COLUMN_DESIRED_CONDITION +
			" TEXT NOT NULL, " +
			COLUMN_RESULT_CITY_ID +
			" TEXT NOT NULL, " +
			COLUMN_RESULT_CITY_NAME +
			" TEXT NOT NULL, " +
			COLUMN_RESULT_COUNTRY +
			" TEXT, " +
			COLUMN_RESULT_LATITUDE +
			" REAL NOT NULL, " +
			COLUMN_RESULT_LONGITUDE +
			" REAL NOT NULL, " +
			COLUMN_DISTANCE_KM +
			" REAL, " +
			COLUMN_USER_LATITUDE +
			" REAL NOT NULL, " +
			COLUMN_USER_LONGITUDE +
			" REAL NOT NULL, " +
			COLUMN_SEARCHED_AT +
			" INTEGER NOT NULL" +
			");";

		/** Índice para exibir histórico por data */
		public static final String SQL_CREATE_INDEX_DATE =
			"CREATE INDEX idx_searched_at ON " +
			TABLE_NAME +
			" (" +
			COLUMN_SEARCHED_AT +
			" DESC);";

		public static final String SQL_DROP_TABLE =
			"DROP TABLE IF EXISTS " + TABLE_NAME + ";";
	}
}
