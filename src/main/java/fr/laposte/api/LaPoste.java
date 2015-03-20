package fr.laposte.api;

import java.net.MalformedURLException;

import javax.xml.ws.http.HTTPException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;

public class LaPoste {

	public static class Token {
		public String accessToken;
		public String refreshToken;
		public String scope;
		public String type;
		public int expiresIn;

		@Override
		public String toString() {
			return "Token [accessToken=" + accessToken + ", refreshToken="
					+ refreshToken + ", scope=" + scope + ", type=" + type
					+ ", expiresIn=" + expiresIn + "]";
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(LaPoste.class);

	private final LpSdk.ApiClient apiClient;
	private Token token = new Token();

	public LaPoste() throws MalformedURLException {
		String baseUrl = System.getProperty(LpSdk.Env.LAPOSTE_API_BASE_URL);
		if (baseUrl == null) {
			baseUrl = LpSdk.Defaults.LAPOSTE_API_BASE_URL;
		}
		this.apiClient = new LpSdk.ApiClient(baseUrl);
	}

	public void auth(String clientId, String clientSecret, String username,
			String password) throws MalformedURLException, UnirestException {
		if (clientId == null) {
			clientId = System.getProperty(LpSdk.Env.LAPOSTE_API_CONSUMER_KEY);
		}
		if (clientSecret == null) {
			clientSecret = System
					.getProperty(LpSdk.Env.LAPOSTE_API_CONSUMER_SECRET);
		}
		if (username == null) {
			username = System.getProperty(LpSdk.Env.LAPOSTE_API_USERNAME);
		}
		if (password == null) {
			password = System.getProperty(LpSdk.Env.LAPOSTE_API_PASSWORD);
		}
		final HttpResponse<JsonNode> res = apiClient.post("/oauth2/token")
				.header("Accept", "application/json")
				.field("client_id", clientId)
				.field("client_secret", clientSecret)
				.field("grant_type", "password").field("username", username)
				.field("password", password).asJson();
		logger.debug("res : " + res);
		final int code = res.getStatus();
		logger.debug("code : " + code);
		if (code != 200) {
			throw new HTTPException(code);
		}
		// Map<String, List<String>> headers = res.getHeaders();
		// logger.debug("headers : " + headers);
		final JsonNode body = res.getBody();
		final JSONObject result = body.getObject();
		token.accessToken = result.getString("access_token");
		token.refreshToken = result.getString("refresh_token");
		token.scope = result.getString("scope");
		token.type = result.getString("token_type");
		token.expiresIn = result.getInt("expires_in");
		logger.debug("token : " + token);
	}

	public LpSdk.ApiClient getApiClient() {
		return apiClient;
	}

	public Token getToken() {
		return token;
	}

	public void refreshToken(String clientId, String clientSecret,
			String refreshToken) throws MalformedURLException, UnirestException {
		if (clientId == null) {
			clientId = System.getProperty(LpSdk.Env.LAPOSTE_API_CONSUMER_KEY);
		}
		if (clientSecret == null) {
			clientSecret = System
					.getProperty(LpSdk.Env.LAPOSTE_API_CONSUMER_SECRET);
		}
		if (refreshToken == null) {
			refreshToken = token.refreshToken;
		}
		if (refreshToken == null) {
			refreshToken = System
					.getProperty(LpSdk.Env.LAPOSTE_API_REFRESH_TOKEN);
		}
		logger.debug("refreshToken : " + refreshToken);
		final HttpResponse<JsonNode> res = apiClient.post("/oauth2/token")
				.header("Accept", "application/json")
				.field("client_id", clientId)
				.field("client_secret", clientSecret)
				.field("grant_type", "refresh_token")
				.field("refresh_token", refreshToken).asJson();
		logger.debug("res : " + res);
		final int code = res.getStatus();
		logger.debug("code : " + code);
		if (code != 200) {
			throw new HTTPException(code);
		}
		// Map<String, List<String>> headers = res.getHeaders();
		// logger.debug("headers : " + headers);
		final JsonNode body = res.getBody();
		final JSONObject result = body.getObject();
		token.accessToken = result.getString("access_token");
		token.refreshToken = result.getString("refresh_token");
		token.scope = result.getString("scope");
		token.type = result.getString("token_type");
		token.expiresIn = result.getInt("expires_in");
		logger.debug("token : " + token);
	}

	public void setToken(Token token) {
		this.token = token;
	}
}