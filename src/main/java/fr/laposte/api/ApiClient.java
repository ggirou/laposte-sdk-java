package fr.laposte.api;

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
import com.mashape.unirest.request.HttpRequestWithBody;

public class ApiClient {

	public static void init() throws KeyManagementException,
	NoSuchAlgorithmException, KeyStoreException {
		if ("false".equals(System.getenv("LAPOSTE_API_STRICT_SSL"))) {
			Unirest.setHttpClient(makeClient());
		}
		Unirest.setDefaultHeader("User-Agent", LpSdk.getVersion());
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
			public boolean isTrusted(X509Certificate[] chain, String authType)
					throws CertificateException {
				return true;
			}
		});
		final SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
				builder.build());
		httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
		return httpclient;
	}

	private static final Logger logger = LoggerFactory
			.getLogger(ApiClient.class);

	private final URL baseUrl;

	public ApiClient(String baseUrl) throws MalformedURLException {
		this.baseUrl = new URL(baseUrl);
	}

	public HttpRequestWithBody post(String url) throws MalformedURLException {
		return Unirest.post(new URL(this.baseUrl, url).toString());
	}
}
