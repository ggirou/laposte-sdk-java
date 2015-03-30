package fr.laposte.api;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;

import fr.laposte.api.LpSdk.ApiException;

/**
 *
 * This class provides general service about La Poste Open API.
 *
 */
public class LaPoste {

	/**
	 *
	 * Pojo container for La Poste token informations.
	 *
	 */
	public static class Token {
		private String accessToken;
		private String refreshToken;
		private String scope;
		private String type;
		private int expiresIn;
		private long creationTime;

		/**
		 * @return the access token value
		 */
		public String getAccessToken() {
			return accessToken;
		}

		/**
		 * @return the creation time of the token in milliseconds
		 */
		public long getCreationTime() {
			return creationTime;
		}

		/**
		 * @return the expires in value of the token
		 */
		public int getExpiresIn() {
			return expiresIn;
		}

		/**
		 * @return the refresh token value
		 */
		public String getRefreshToken() {
			return refreshToken;
		}

		/**
		 * @return the scope
		 */
		public String getScope() {
			return scope;
		}

		/**
		 * @return the type value of token
		 */
		public String getType() {
			return type;
		}

		/**
		 * @return the token validity state
		 */
		public boolean isValid() {
			return validityTime() > 0;
		}

		private void setTokens(final String accessToken,
				final String refreshToken) {
			this.creationTime = System.currentTimeMillis();
			this.accessToken = accessToken;
			this.refreshToken = refreshToken;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Token [accessToken=" + accessToken + ", refreshToken="
					+ refreshToken + ", scope=" + scope + ", type=" + type
					+ ", expiresIn=" + expiresIn + ", creationTime="
					+ creationTime + "]";
		}

		/**
		 * @return the token validity time left in milliseconds
		 */
		public long validityTime() {
			final long now = System.currentTimeMillis();
			return creationTime + (long) (expiresIn * 1E6) - now;
		}

	}

	private static final Logger logger = LoggerFactory.getLogger(LaPoste.class);

	private final LpSdk.ApiClient apiClient;
	private Token token = new Token();

	public LaPoste() throws MalformedURLException, URISyntaxException {
		this(null);
	}

	public LaPoste(String baseUrl) throws MalformedURLException,
	URISyntaxException {
		if (baseUrl == null) {
			baseUrl = System.getProperty(LpSdk.Env.LAPOSTE_API_BASE_URL,
					LpSdk.Defaults.LAPOSTE_API_BASE_URL);
		}
		this.apiClient = new LpSdk.ApiClient(baseUrl);
	}

	/**
	 * Authenticate a developer, and provide a token for La Poste Open API.
	 * <p>
	 * The resulting token is stored as "token" instance attribute.
	 *
	 * @param consumerKey
	 *            the consumer key
	 * @param consumerSecret
	 *            the consumer secret
	 * @param username
	 *            the developer account username
	 * @param password
	 *            the developer account password
	 * @throws MalformedURLException
	 * @throws ApiException
	 * @see Token
	 */
	public void auth(String consumerKey, String consumerSecret,
			String username, String password) throws MalformedURLException,
			ApiException {
		if (consumerKey == null) {
			consumerKey = System
					.getProperty(LpSdk.Env.LAPOSTE_API_CONSUMER_KEY);
		}
		if (consumerSecret == null) {
			consumerSecret = System
					.getProperty(LpSdk.Env.LAPOSTE_API_CONSUMER_SECRET);
		}
		if (username == null) {
			username = System.getProperty(LpSdk.Env.LAPOSTE_API_USERNAME);
		}
		if (password == null) {
			password = System.getProperty(LpSdk.Env.LAPOSTE_API_PASSWORD);
		}
		try {
			final HttpResponse<JsonNode> res = apiClient.post("/oauth2/token")
					.header("Accept", "application/json")
					.field("client_id", consumerKey)
					.field("client_secret", consumerSecret)
					.field("grant_type", "password")
					.field("username", username).field("password", password)
					.asJson();
			final int code = res.getStatus();
			if (code != 200) {
				throw new ApiException(code);
			}
			// Map<String, List<String>> headers = res.getHeaders();
			// logger.debug("headers : " + headers);
			final JsonNode body = res.getBody();
			final JSONObject result = body.getObject();
			token.setTokens(result.getString("access_token"),
					result.getString("refresh_token"));
			token.scope = result.getString("scope");
			token.type = result.getString("token_type");
			token.expiresIn = result.getInt("expires_in");
		} catch (final UnirestException e) {
			throw new ApiException(e);
		}
	}

	/**
	 * Get the current token infos.
	 *
	 * @return the current token object instance
	 * @see Token
	 */
	public Token getToken() {
		return token;
	}

	/**
	 * Refresh the La Poste Open API access token.
	 * <p>
	 * The resulting token is stored as "token" instance attribute.
	 *
	 * @param consumerKey
	 *            the consumer key
	 * @param consumerSecret
	 *            the consumer secret
	 * @param refreshToken
	 *            the refresh token
	 * @throws MalformedURLException
	 * @throws ApiException
	 * @see Token
	 */
	public void refreshToken(String consumerKey, String consumerSecret,
			String refreshToken) throws MalformedURLException, ApiException {
		if (consumerKey == null) {
			consumerKey = System
					.getProperty(LpSdk.Env.LAPOSTE_API_CONSUMER_KEY);
		}
		if (consumerSecret == null) {
			consumerSecret = System
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
		try {
			final HttpResponse<JsonNode> res = apiClient.post("/oauth2/token")
					.header("Accept", "application/json")
					.field("client_id", consumerKey)
					.field("client_secret", consumerSecret)
					.field("grant_type", "refresh_token")
					.field("refresh_token", refreshToken).asJson();
			final int code = res.getStatus();
			if (code != 200) {
				throw new ApiException(code);
			}
			final JsonNode body = res.getBody();
			final JSONObject result = body.getObject();
			token.setTokens(result.getString("access_token"),
					result.getString("refresh_token"));
			token.scope = result.getString("scope");
			token.type = result.getString("token_type");
			token.expiresIn = result.getInt("expires_in");
		} catch (final UnirestException e) {
			throw new ApiException(e);
		}
	}

	/**
	 * Set the current token infos.
	 *
	 * @param token
	 *            the token object to set
	 * @see Token
	 */
	public void setToken(final Token token) {
		this.token = token;
	}
}