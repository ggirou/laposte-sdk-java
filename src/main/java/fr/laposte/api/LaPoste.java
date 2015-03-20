package fr.laposte.api;

import java.net.MalformedURLException;
import java.util.Map;

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

	private final ApiClient apiClient;
	private String accessToken;
	private String refreshToken;
	private Token token = new Token();

	public LaPoste() throws MalformedURLException {
		final Map<String, String> env = System.getenv();
		final String baseUrl = env.get("LAPOSTE_API_BASE_URL");
		this.apiClient = new ApiClient(baseUrl);
	}

	public void auth(String clientId, String clientSecret, String username,
			String password) throws MalformedURLException, UnirestException {
		final Map<String, String> env = System.getenv();
		if (clientId == null) {
			clientId = env.get("LAPOSTE_API_CONSUMER_KEY");
		}
		if (clientSecret == null) {
			clientSecret = env.get("LAPOSTE_API_CONSUMER_SECRET");
		}
		if (username == null) {
			username = env.get("LAPOSTE_API_USERNAME");
		}
		if (password == null) {
			password = env.get("LAPOSTE_API_PASSWORD");
		}
		final HttpResponse<JsonNode> res = apiClient.post("/oauth2/token")
				.header("accept", "application/json")
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

	public String getAccessToken() {
		return accessToken;
	}

	public ApiClient getApiClient() {
		return apiClient;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public Token getToken() {
		return token;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public void setToken(Token token) {
		this.token = token;
	}
}