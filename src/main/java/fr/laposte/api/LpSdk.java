package fr.laposte.api;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Properties;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequestWithBody;

public class LpSdk {
	public static interface Defaults {
		public static final String LAPOSTE_API_BASE_URL = "https://api.laposte.fr/";
		public static final String DIGIPOSTE_API_BASE_URL = "https://api.laposte.fr/digiposte/1.0";
	}

	public static interface Env {
		public static final String LAPOSTE_API_BASE_URL = "LAPOSTE_API_BASE_URL";
		public static final String LAPOSTE_API_CONSUMER_KEY = "LAPOSTE_API_CONSUMER_KEY";
		public static final String LAPOSTE_API_CONSUMER_SECRET = "LAPOSTE_API_CONSUMER_SECRET";
		public static final String LAPOSTE_API_USERNAME = "LAPOSTE_API_USERNAME";
		public static final String LAPOSTE_API_PASSWORD = "LAPOSTE_API_PASSWORD";
		public static final String LAPOSTE_API_ACCESS_TOKEN = "LAPOSTE_API_ACCESS_TOKEN";
		public static final String LAPOSTE_API_REFRESH_TOKEN = "LAPOSTE_API_REFRESH_TOKEN";
		public static final String DIGIPOSTE_API_BASE_URL = "DIGIPOSTE_API_BASE_URL";
		public static final String DIGIPOSTE_API_ACCESS_TOKEN = "DIGIPOSTE_API_ACCESS_TOKEN";
		public static final String DIGIPOSTE_API_USERNAME = "DIGIPOSTE_API_USERNAME";
		public static final String DIGIPOSTE_API_PASSWORD = "DIGIPOSTE_API_PASSWORD";
	};

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
	};

	private static final Logger logger = LoggerFactory.getLogger(LpSdk.class);;

	static {
		final Field[] interfaceFields = Env.class.getFields();
		for (final Field field : interfaceFields) {
			try {
				final String name = field.get(null).toString();
				final String value = System.getenv(name);
				if (value != null) {
					System.setProperty(name, value);
				}
			} catch (final Exception e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public static class ApiClient {

		public static void init() throws KeyManagementException,
				NoSuchAlgorithmException, KeyStoreException {
			if ("false".equals(System.getenv("LAPOSTE_API_STRICT_SSL"))) {
				Unirest.setHttpClient(makeClient());
			}
			Unirest.setDefaultHeader("User-Agent",
					"laposte-sdk/" + LpSdk.getVersion());
		}

		public static void quit() throws IOException {
			Unirest.shutdown();
		}

		private static CloseableHttpClient makeClient()
				throws NoSuchAlgorithmException, KeyStoreException,
				KeyManagementException {
			final SSLContextBuilder builder = new SSLContextBuilder();
			CloseableHttpClient httpclient = null;
			builder.loadTrustMaterial(null, new TrustStrategy() {
				@Override
				public boolean isTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
					return true;
				}
			});
			final SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
					builder.build());
			httpclient = HttpClients.custom().setSSLSocketFactory(sslsf)
					.build();
			return httpclient;
		}

		private static final Logger logger = LoggerFactory
				.getLogger(ApiClient.class);

		private final URL baseUrl;

		public ApiClient(String baseUrl) throws MalformedURLException {
			this.baseUrl = LpSdk.buildBaseUrl(baseUrl);
			logger.debug("baseUrl : " + this.baseUrl);
		}

		public GetRequest get(String url) throws MalformedURLException {
			final String apiUrl = LpSdk.buildApiUrl(this.baseUrl, url);
			logger.debug("GET " + apiUrl);
			return Unirest.get(apiUrl);
		}

		public HttpRequestWithBody post(String url)
				throws MalformedURLException {
			final String apiUrl = LpSdk.buildApiUrl(this.baseUrl, url);
			logger.debug("POST " + apiUrl);
			return Unirest.post(apiUrl);
		}
	}

	public static URL buildBaseUrl(String baseUrl) throws MalformedURLException {
		return new URL(baseUrl + "/");
	}

	public static String buildApiUrl(URL baseUrl, String url)
			throws MalformedURLException {
		return new URL(baseUrl, "." + url).toString();
	}
}
