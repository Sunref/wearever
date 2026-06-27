package com.example.wearever.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.example.wearever.model.SearchedLocation;
import com.example.wearever.model.WeatherForecast;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Helper do banco de dados SQLite.
 *  Criar / atualiza o schema do banco, CRUD para WeatherForecast e SearchedLocation e gerencia expiração de cache (padrão: 1 hora)
 */
public class WeatherDbHelper extends SQLiteOpenHelper {

	private static final String TAG = "WeatherDbHelper";

	// Incremente DATABASE_VERSION sempre que alterar o schema
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "wearever.db";

	/** Cache expira após 1 hora por padrão */
	public static final long CACHE_TTL_MS = TimeUnit.HOURS.toMillis(1);

	// Singleton
	private static WeatherDbHelper sInstance;

	public static synchronized WeatherDbHelper getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new WeatherDbHelper(context.getApplicationContext());
		}
		return sInstance;
	}

	private WeatherDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Ciclo de vida
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(WeatherContract.WeatherEntry.SQL_CREATE_TABLE);
		db.execSQL(WeatherContract.WeatherEntry.SQL_CREATE_INDEX_CONDITION);
		db.execSQL(WeatherContract.WeatherEntry.SQL_CREATE_INDEX_CITY_TIME);

		db.execSQL(WeatherContract.LocationEntry.SQL_CREATE_TABLE);
		db.execSQL(WeatherContract.LocationEntry.SQL_CREATE_INDEX_DATE);

		Log.d(
			TAG,
			"Banco de dados criado: " + DATABASE_NAME + " v" + DATABASE_VERSION
		);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(
			TAG,
			"Upgrade " +
				oldVersion +
				" -> " +
				newVersion +
				": recriando tabelas"
		);
		db.execSQL(WeatherContract.WeatherEntry.SQL_DROP_TABLE);
		db.execSQL(WeatherContract.LocationEntry.SQL_DROP_TABLE);
		onCreate(db);
	}

	@Override
	public void onConfigure(SQLiteDatabase db) {
		super.onConfigure(db);
		// Ativa chaves estrangeiras (boas práticas)
		db.setForeignKeyConstraintsEnabled(true);
	}

	// CRUD
	/**
	 * Insere ou substitui uma previsão no banco.
	 */
	public long insertForecast(WeatherForecast forecast) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues cv = forecastToContentValues(forecast);
		long rowId = db.insertWithOnConflict(
			WeatherContract.WeatherEntry.TABLE_NAME,
			null,
			cv,
			SQLiteDatabase.CONFLICT_REPLACE
		);
		Log.d(
			TAG,
			"insertForecast: rowId=" +
				rowId +
				" cidade=" +
				forecast.getCityName()
		);
		return rowId;
	}

	/**
	 * Insere uma lista de previsões em uma única transação.
	 */
	public int insertForecasts(List<WeatherForecast> forecasts) {
		SQLiteDatabase db = getWritableDatabase();
		int count = 0;
		db.beginTransaction();
		try {
			for (WeatherForecast f : forecasts) {
				long id = db.insertWithOnConflict(
					WeatherContract.WeatherEntry.TABLE_NAME,
					null,
					forecastToContentValues(f),
					SQLiteDatabase.CONFLICT_REPLACE
				);
				if (id != -1) count++;
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		Log.d(
			TAG,
			"insertForecasts: " + count + "/" + forecasts.size() + " inseridas"
		);
		return count;
	}

	/**
	 * Busca previsões por condição climática, ordenadas por distância
	 * usando a fórmula aproximada de distância euclidiana
	 */
	public List<WeatherForecast> getForecastsByCondition(
		String condition,
		double userLat,
		double userLon,
		long maxAgeMs
	) {
		long minCachedAt = (System.currentTimeMillis() - maxAgeMs) / 1000L;

		// Distância euclidiana como proxy de ordenação (não requer funções de BD)
		String orderBy =
			"((" +
			WeatherContract.WeatherEntry.COLUMN_LATITUDE +
			" - (" +
			userLat +
			")) * " +
			"(" +
			WeatherContract.WeatherEntry.COLUMN_LATITUDE +
			" - (" +
			userLat +
			")) + " +
			"(" +
			WeatherContract.WeatherEntry.COLUMN_LONGITUDE +
			" - (" +
			userLon +
			")) * " +
			"(" +
			WeatherContract.WeatherEntry.COLUMN_LONGITUDE +
			" - (" +
			userLon +
			"))) ASC";

		String selection =
			WeatherContract.WeatherEntry.COLUMN_WEATHER_CONDITION +
			" = ? AND " +
			WeatherContract.WeatherEntry.COLUMN_CACHED_AT +
			" >= ?";

		String[] selectionArgs = { condition, String.valueOf(minCachedAt) };

		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(
			WeatherContract.WeatherEntry.TABLE_NAME,
			null,
			selection,
			selectionArgs,
			null,
			null,
			orderBy
		);

		return cursorToForecastList(cursor);
	}

	/**
	 * Retorna todas as previsões de uma cidade específica ainda válidas no cache.
	 */
	public List<WeatherForecast> getForecastsByCity(
		String cityId,
		long maxAgeMs
	) {
		long minCachedAt = (System.currentTimeMillis() - maxAgeMs) / 1000L;

		String selection =
			WeatherContract.WeatherEntry.COLUMN_CITY_ID +
			" = ? AND " +
			WeatherContract.WeatherEntry.COLUMN_CACHED_AT +
			" >= ?";

		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(
			WeatherContract.WeatherEntry.TABLE_NAME,
			null,
			selection,
			new String[] { cityId, String.valueOf(minCachedAt) },
			null,
			null,
			WeatherContract.WeatherEntry.COLUMN_FORECAST_TIMESTAMP + " ASC"
		);

		return cursorToForecastList(cursor);
	}

	/**
	 * Remove previsões mais antigas
	 */
	public int deleteExpiredForecasts(long maxAgeMs) {
		long minCachedAt = (System.currentTimeMillis() - maxAgeMs) / 1000L;
		SQLiteDatabase db = getWritableDatabase();
		int deleted = db.delete(
			WeatherContract.WeatherEntry.TABLE_NAME,
			WeatherContract.WeatherEntry.COLUMN_CACHED_AT + " < ?",
			new String[] { String.valueOf(minCachedAt) }
		);
		Log.d(TAG, "deleteExpiredForecasts: " + deleted + " linhas removidas");
		return deleted;
	}

	/**
	 * Salva uma entrada de histórico de busca.
	 */
	public long insertSearchedLocation(SearchedLocation location) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues cv = searchedLocationToContentValues(location);
		long rowId = db.insert(
			WeatherContract.LocationEntry.TABLE_NAME,
			null,
			cv
		);
		Log.d(
			TAG,
			"insertSearchedLocation: rowId=" +
				rowId +
				" busca=" +
				location.getDesiredCondition() +
				" resultado=" +
				location.getResultCityName()
		);
		return rowId;
	}

	/**
	 * Retorna o histórico de buscas, do mais recente ao mais antigo.
	 */
	public List<SearchedLocation> getSearchHistory(int limit) {
		SQLiteDatabase db = getReadableDatabase();
		String limitClause = limit > 0 ? String.valueOf(limit) : null;
		Cursor cursor = db.query(
			WeatherContract.LocationEntry.TABLE_NAME,
			null,
			null,
			null,
			null,
			null,
			WeatherContract.LocationEntry.COLUMN_SEARCHED_AT + " DESC",
			limitClause
		);
		return cursorToSearchedLocationList(cursor);
	}

	/**
	 * Remove todo o histórico de buscas.
	 */
	public void clearSearchHistory() {
		getWritableDatabase().delete(
			WeatherContract.LocationEntry.TABLE_NAME,
			null,
			null
		);
		Log.d(TAG, "clearSearchHistory: histórico apagado");
	}

	// Helpers

	private ContentValues forecastToContentValues(WeatherForecast f) {
		ContentValues cv = new ContentValues();
		cv.put(WeatherContract.WeatherEntry.COLUMN_CITY_ID, f.getCityId());
		cv.put(WeatherContract.WeatherEntry.COLUMN_CITY_NAME, f.getCityName());
		cv.put(WeatherContract.WeatherEntry.COLUMN_COUNTRY, f.getCountry());
		cv.put(WeatherContract.WeatherEntry.COLUMN_LATITUDE, f.getLatitude());
		cv.put(WeatherContract.WeatherEntry.COLUMN_LONGITUDE, f.getLongitude());
		cv.put(
			WeatherContract.WeatherEntry.COLUMN_WEATHER_CONDITION,
			f.getWeatherCondition()
		);
		cv.put(
			WeatherContract.WeatherEntry.COLUMN_WEATHER_DESCRIPTION,
			f.getWeatherDescription()
		);
		cv.put(
			WeatherContract.WeatherEntry.COLUMN_WEATHER_ICON,
			f.getWeatherIcon()
		);
		cv.put(WeatherContract.WeatherEntry.COLUMN_TEMP, f.getTemp());
		cv.put(WeatherContract.WeatherEntry.COLUMN_TEMP_MIN, f.getTempMin());
		cv.put(WeatherContract.WeatherEntry.COLUMN_TEMP_MAX, f.getTempMax());
		cv.put(
			WeatherContract.WeatherEntry.COLUMN_FEELS_LIKE,
			f.getFeelsLike()
		);
		cv.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, f.getHumidity());
		cv.put(
			WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
			f.getWindSpeed()
		);
		cv.put(
			WeatherContract.WeatherEntry.COLUMN_VISIBILITY,
			f.getVisibility()
		);
		cv.put(
			WeatherContract.WeatherEntry.COLUMN_FORECAST_TIMESTAMP,
			f.getForecastTimestamp()
		);
		cv.put(WeatherContract.WeatherEntry.COLUMN_CACHED_AT, f.getCachedAt());
		return cv;
	}

	private List<WeatherForecast> cursorToForecastList(Cursor cursor) {
		List<WeatherForecast> list = new ArrayList<>();
		if (cursor == null) return list;
		try {
			while (cursor.moveToNext()) {
				WeatherForecast f = new WeatherForecast();
				f.setId(
					cursor.getLong(
						cursor.getColumnIndexOrThrow(
							WeatherContract.WeatherEntry._ID
						)
					)
				);
				f.setCityId(
					cursor.getString(
						cursor.getColumnIndexOrThrow(
							WeatherContract.WeatherEntry.COLUMN_CITY_ID
						)
					)
				);
				f.setCityName(
					cursor.getString(
						cursor.getColumnIndexOrThrow(
							WeatherContract.WeatherEntry.COLUMN_CITY_NAME
						)
					)
				);
				f.setCountry(
					cursor.getString(
						cursor.getColumnIndexOrThrow(
							WeatherContract.WeatherEntry.COLUMN_COUNTRY
						)
					)
				);
				f.setLatitude(
					cursor.getDouble(
						cursor.getColumnIndexOrThrow(
							WeatherContract.WeatherEntry.COLUMN_LATITUDE
						)
					)
				);
				f.setLongitude(
					cursor.getDouble(
						cursor.getColumnIndexOrThrow(
							WeatherContract.WeatherEntry.COLUMN_LONGITUDE
						)
					)
				);
				f.setWeatherCondition(
					cursor.getString(
						cursor.getColumnIndexOrThrow(
							WeatherContract.WeatherEntry.COLUMN_WEATHER_CONDITION
						)
					)
				);
				f.setWeatherDescription(
					cursor.getString(
						cursor.getColumnIndexOrThrow(
							WeatherContract.WeatherEntry.COLUMN_WEATHER_DESCRIPTION
						)
					)
				);
				f.setWeatherIcon(
					cursor.getString(
						cursor.getColumnIndexOrThrow(
							WeatherContract.WeatherEntry.COLUMN_WEATHER_ICON
						)
					)
				);
				f.setTemp(
					cursor.getDouble(
						cursor.getColumnIndexOrThrow(
							WeatherContract.WeatherEntry.COLUMN_TEMP
						)
					)
				);
				f.setTempMin(
					cursor.getDouble(
						cursor.getColumnIndexOrThrow(
							WeatherContract.WeatherEntry.COLUMN_TEMP_MIN
						)
					)
				);
				f.setTempMax(
					cursor.getDouble(
						cursor.getColumnIndexOrThrow(
							WeatherContract.WeatherEntry.COLUMN_TEMP_MAX
						)
					)
				);
				f.setFeelsLike(
					cursor.getDouble(
						cursor.getColumnIndexOrThrow(
							WeatherContract.WeatherEntry.COLUMN_FEELS_LIKE
						)
					)
				);
				f.setHumidity(
					cursor.getInt(
						cursor.getColumnIndexOrThrow(
							WeatherContract.WeatherEntry.COLUMN_HUMIDITY
						)
					)
				);
				f.setWindSpeed(
					cursor.getDouble(
						cursor.getColumnIndexOrThrow(
							WeatherContract.WeatherEntry.COLUMN_WIND_SPEED
						)
					)
				);
				f.setVisibility(
					cursor.getInt(
						cursor.getColumnIndexOrThrow(
							WeatherContract.WeatherEntry.COLUMN_VISIBILITY
						)
					)
				);
				f.setForecastTimestamp(
					cursor.getLong(
						cursor.getColumnIndexOrThrow(
							WeatherContract.WeatherEntry.COLUMN_FORECAST_TIMESTAMP
						)
					)
				);
				f.setCachedAt(
					cursor.getLong(
						cursor.getColumnIndexOrThrow(
							WeatherContract.WeatherEntry.COLUMN_CACHED_AT
						)
					)
				);
				list.add(f);
			}
		} finally {
			cursor.close();
		}
		return list;
	}

	private ContentValues searchedLocationToContentValues(SearchedLocation s) {
		ContentValues cv = new ContentValues();
		cv.put(
			WeatherContract.LocationEntry.COLUMN_DESIRED_CONDITION,
			s.getDesiredCondition()
		);
		cv.put(
			WeatherContract.LocationEntry.COLUMN_RESULT_CITY_ID,
			s.getResultCityId()
		);
		cv.put(
			WeatherContract.LocationEntry.COLUMN_RESULT_CITY_NAME,
			s.getResultCityName()
		);
		cv.put(
			WeatherContract.LocationEntry.COLUMN_RESULT_COUNTRY,
			s.getResultCountry()
		);
		cv.put(
			WeatherContract.LocationEntry.COLUMN_RESULT_LATITUDE,
			s.getResultLatitude()
		);
		cv.put(
			WeatherContract.LocationEntry.COLUMN_RESULT_LONGITUDE,
			s.getResultLongitude()
		);
		cv.put(
			WeatherContract.LocationEntry.COLUMN_DISTANCE_KM,
			s.getDistanceKm()
		);
		cv.put(
			WeatherContract.LocationEntry.COLUMN_USER_LATITUDE,
			s.getUserLatitude()
		);
		cv.put(
			WeatherContract.LocationEntry.COLUMN_USER_LONGITUDE,
			s.getUserLongitude()
		);
		cv.put(
			WeatherContract.LocationEntry.COLUMN_SEARCHED_AT,
			s.getSearchedAt()
		);
		return cv;
	}

	private List<SearchedLocation> cursorToSearchedLocationList(Cursor cursor) {
		List<SearchedLocation> list = new ArrayList<>();
		if (cursor == null) return list;
		try {
			while (cursor.moveToNext()) {
				SearchedLocation s = new SearchedLocation();
				s.setId(
					cursor.getLong(
						cursor.getColumnIndexOrThrow(
							WeatherContract.LocationEntry._ID
						)
					)
				);
				s.setDesiredCondition(
					cursor.getString(
						cursor.getColumnIndexOrThrow(
							WeatherContract.LocationEntry.COLUMN_DESIRED_CONDITION
						)
					)
				);
				s.setResultCityId(
					cursor.getString(
						cursor.getColumnIndexOrThrow(
							WeatherContract.LocationEntry.COLUMN_RESULT_CITY_ID
						)
					)
				);
				s.setResultCityName(
					cursor.getString(
						cursor.getColumnIndexOrThrow(
							WeatherContract.LocationEntry.COLUMN_RESULT_CITY_NAME
						)
					)
				);
				s.setResultCountry(
					cursor.getString(
						cursor.getColumnIndexOrThrow(
							WeatherContract.LocationEntry.COLUMN_RESULT_COUNTRY
						)
					)
				);
				s.setResultLatitude(
					cursor.getDouble(
						cursor.getColumnIndexOrThrow(
							WeatherContract.LocationEntry.COLUMN_RESULT_LATITUDE
						)
					)
				);
				s.setResultLongitude(
					cursor.getDouble(
						cursor.getColumnIndexOrThrow(
							WeatherContract.LocationEntry.COLUMN_RESULT_LONGITUDE
						)
					)
				);
				s.setDistanceKm(
					cursor.getDouble(
						cursor.getColumnIndexOrThrow(
							WeatherContract.LocationEntry.COLUMN_DISTANCE_KM
						)
					)
				);
				s.setUserLatitude(
					cursor.getDouble(
						cursor.getColumnIndexOrThrow(
							WeatherContract.LocationEntry.COLUMN_USER_LATITUDE
						)
					)
				);
				s.setUserLongitude(
					cursor.getDouble(
						cursor.getColumnIndexOrThrow(
							WeatherContract.LocationEntry.COLUMN_USER_LONGITUDE
						)
					)
				);
				s.setSearchedAt(
					cursor.getLong(
						cursor.getColumnIndexOrThrow(
							WeatherContract.LocationEntry.COLUMN_SEARCHED_AT
						)
					)
				);
				list.add(s);
			}
		} finally {
			cursor.close();
		}
		return list;
	}
}
