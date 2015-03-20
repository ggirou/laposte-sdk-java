package fr.laposte.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class LpSdk {
	public static String getVersion() {
		final String versionPropPath = "/version.properties";
		final InputStream is = LpSdk.class.getResourceAsStream(versionPropPath);
		if (is == null) {
			return "UNKNOWN";
		}
		final Properties props = new Properties();
		try {
			props.load(is);
			is.close();
			return (String) props.get("version");
		} catch (final IOException e) {
			return "UNKNOWN";
		}
	}
}
