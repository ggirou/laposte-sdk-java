package fr.laposte.api;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.StringTokenizer;

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

/**
 *
 * This class is a helper tool for using La Poste Open API SDK.
 *
 * @author openhoat
 *
 */
public class LpSdk {

	/**
	 *
	 * REST client helper to use with La Poste Open APIs.
	 *
	 */
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

		/**
		 *
		 * Build an ApiClient instance based on the given API provider base URL
		 *
		 * @param baseUrl
		 *            the base URL of the API provider
		 * @throws MalformedURLException
		 * @throws URISyntaxException
		 */
		public ApiClient(String baseUrl) throws MalformedURLException,
				URISyntaxException {
			this.baseUrl = LpSdk.buildBaseUrl(baseUrl);
			logger.debug("baseUrl : " + this.baseUrl);
		}

		/**
		 *
		 * Build a HTTP DELETE request with the given API URL.
		 *
		 * @param url
		 * @return Unirest request
		 * @throws MalformedURLException
		 * @see Unirest
		 */
		public HttpRequestWithBody delete(String url)
				throws MalformedURLException {
			final String apiUrl = LpSdk.buildApiUrl(this.baseUrl, url);
			logger.debug("POST " + apiUrl);
			return Unirest.delete(apiUrl);
		}

		/**
		 *
		 * Build a HTTP GET request with the given API URL.
		 *
		 * @param url
		 * @return Unirest request
		 * @throws MalformedURLException
		 * @see Unirest
		 */
		public GetRequest get(String url) throws MalformedURLException {
			final String apiUrl = LpSdk.buildApiUrl(this.baseUrl, url);
			logger.debug("GET " + apiUrl);
			return Unirest.get(apiUrl);
		}

		/**
		 *
		 * Build a HTTP POST request with the given API URL.
		 *
		 * @param url
		 * @return Unirest request
		 * @throws MalformedURLException
		 * @see Unirest
		 */
		public HttpRequestWithBody post(String url)
				throws MalformedURLException {
			final String apiUrl = LpSdk.buildApiUrl(this.baseUrl, url);
			logger.debug("POST " + apiUrl);
			return Unirest.post(apiUrl);
		}

		/**
		 *
		 * Build a HTTP PUT request with the given API URL.
		 *
		 * @param url
		 * @return Unirest request
		 * @throws MalformedURLException
		 * @see Unirest
		 */
		public HttpRequestWithBody put(String url) throws MalformedURLException {
			final String apiUrl = LpSdk.buildApiUrl(this.baseUrl, url);
			logger.debug("POST " + apiUrl);
			return Unirest.put(apiUrl);
		}

	}

	/**
	 *
	 * API client exception.
	 *
	 */
	public static class ApiException extends Exception {

		/**
		 *
		 */
		private static final long serialVersionUID = 4935635089514341634L;

		private int statusCode;

		public ApiException(Exception e) {
			super(e);
		}

		public ApiException(int statusCode) {
			super();
			this.statusCode = statusCode;
		}

		public ApiException(int statusCode, String msg) {
			super(msg);
			this.statusCode = statusCode;
		}

		public ApiException(String msg) {
			super(msg);
		}

		public int getStatusCode() {
			return statusCode;
		}
	}

	/**
	 *
	 * La Poste Open API SDK default values
	 *
	 */
	public static interface Defaults {
		public static final String LAPOSTE_API_BASE_URL = "https://api.laposte.fr/";
		public static final String DIGIPOSTE_API_BASE_URL = "https://api.laposte.fr/digiposte/1.0";
	};

	/**
	 *
	 * La Poste Open API SDK supported environment variable names
	 *
	 */
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

	static String buildApiUrl(URL baseUrl, String url)
			throws MalformedURLException {
		return new URL(baseUrl, "." + normalizeUrl(url)).toString();
	}

	static URL buildBaseUrl(String baseUrl) throws MalformedURLException,
			URISyntaxException {
		return new URI(baseUrl).toURL();
	};

	static String getVersion() {
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

	static String normalizeUrl(String url) {
		if ("".equals(url)) {
			return url;
		}
		int slashes = 0;
		while (slashes < url.length() && url.charAt(slashes) == '/') {
			slashes++;
		}
		boolean isDir = (url.charAt(url.length() - 1) == '/');
		final StringTokenizer st = new StringTokenizer(url, "/");
		final LinkedList<String> clean = new LinkedList<String>();
		while (st.hasMoreTokens()) {
			final String token = st.nextToken();
			if ("..".equals(token)) {
				if (!clean.isEmpty() && !"..".equals(clean.getLast())) {
					clean.removeLast();
					if (!st.hasMoreTokens()) {
						isDir = true;
					}
				} else {
					clean.add("..");
				}
			} else if (!".".equals(token) && !"".equals(token)) {
				clean.add(token);
			}
		}
		final StringBuilder sb = new StringBuilder();
		while (slashes-- > 0) {
			sb.append('/');
		}
		for (final Iterator<String> it = clean.iterator(); it.hasNext();) {
			sb.append(it.next());
			if (it.hasNext()) {
				sb.append('/');
			}
		}
		if (isDir && sb.length() > 0 && sb.charAt(sb.length() - 1) != '/') {
			sb.append('/');
		}
		return sb.toString();
	}

	private static final Logger logger = LoggerFactory.getLogger(LpSdk.class);

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
}
