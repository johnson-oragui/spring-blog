package com.johnson.utilities.config;

import io.github.cdimascio.dotenv.Dotenv;

public class ConfigUtils {
	private static Dotenv dotenv = Dotenv.load();

	public static final String PORT = dotenv.get("PORT");

	public static final String DB_URL = dotenv.get("DB_URL");
	public static final String DB_USER = dotenv.get("POSTGRES_USER");
	public static final String DB_PASSWORD = dotenv.get("POSTGRES_PASSWORD");

	public static final String CLOUDINARY_CLOUD_NAME = dotenv.get("CLOUDINARY_CLOUD_NAME");
	public static final String CLOUDINARY_API_KEY = dotenv.get("CLOUDINARY_API_KEY");
	public static final String CLOUDINARY_API_SECRET = dotenv.get("CLOUDINARY_API_SECRET");

	public static final String JWT_SECRET = dotenv.get("JWT_SECRET");
	public static final String JWT_EXPIRATION = dotenv.get("JWT_EXPIRATION");

	public static void load() {
		System.setProperty("PORT", PORT);
		System.setProperty("DB_URL", DB_URL);
		System.setProperty("POSTGRES_USER", DB_USER);
		System.setProperty("POSTGRES_PASSWORD", DB_PASSWORD);
		System.setProperty("JWT_SECRET", JWT_SECRET);
		System.setProperty("JWT_EXPIRATION", JWT_EXPIRATION);
	}
}
