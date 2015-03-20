package fr.laposte.api.providers;

import java.net.MalformedURLException;

import javax.xml.ws.http.HTTPException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;

import fr.laposte.api.LpSdk;

public class Digiposte {

	public static class DgpToken {
		public String accessToken;
		public String refreshToken;

		@Override
		public String toString() {
			return "DgpToken [accessToken=" + accessToken + ", refreshToken="
					+ refreshToken + "]";
		}
	}

	private static final Logger logger = LoggerFactory
			.getLogger(Digiposte.class);

	private final LpSdk.ApiClient apiClient;
	private DgpToken dgpToken = new DgpToken();

	private String accessToken;

	public Digiposte() throws MalformedURLException {
		final String baseUrl = System.getProperty(
				LpSdk.Env.DIGIPOSTE_API_BASE_URL,
				LpSdk.Defaults.DIGIPOSTE_API_BASE_URL);
		this.apiClient = new LpSdk.ApiClient(baseUrl);
	}

	public void auth(String accessToken, String username, String password)
			throws MalformedURLException, UnirestException {
		if (accessToken == null) {
			accessToken = System
					.getProperty(LpSdk.Env.LAPOSTE_API_ACCESS_TOKEN);
		}
		if (username == null) {
			username = System.getProperty(LpSdk.Env.DIGIPOSTE_API_USERNAME);
		}
		if (password == null) {
			password = System.getProperty(LpSdk.Env.DIGIPOSTE_API_PASSWORD);
		}
		logger.debug("accessToken : " + accessToken);
		logger.debug("username : " + username);
		logger.debug("password : " + password);
		final JsonNode reqBody = new JsonNode("{\"credential\":{\"user\":\""
				+ username + "\", \"password\":\"" + password + "\"}}");
		logger.debug("reqBody : " + reqBody);
		final HttpResponse<JsonNode> res = apiClient.post("/login")
				.header("Content-Type", "application/json")
				.header("Accept", "application/json")
				.header("Authorization", "Bearer " + accessToken).body(reqBody)
				.asJson();
		logger.debug("res : " + res);
		final int code = res.getStatus();
		logger.debug("code : " + code);
		if (code != 200) {
			logger.debug("status : " + res.getStatusText());
			throw new HTTPException(code);
		}
		this.accessToken = accessToken;
		// Map<String, List<String>> headers = res.getHeaders();
		// logger.debug("headers : " + headers);
		final JsonNode body = res.getBody();
		final JSONObject result = body.getObject();
		dgpToken.accessToken = result.getString("access_token");
		dgpToken.refreshToken = result.getString("refresh_token");
		logger.debug("dgpToken : " + dgpToken);
	}

	public LpSdk.ApiClient getApiClient() {
		return apiClient;
	}

	public DgpToken getDgpToken() {
		return dgpToken;
	}

	public JSONObject getDocs(String location, Integer index,
			Integer maxResults, String sort, Boolean ascending)
					throws MalformedURLException, UnirestException {
		final String url = "/documents"
				+ (location != null ? ("/" + location) : "");
		HttpRequest req = apiClient.get(url)
				.header("Accept", "application/json")
				.header("Authorization", "Bearer " + accessToken)
				.header("User-Token", dgpToken.accessToken);
		if (index != null) {
			req = req.queryString("index", index);
		}
		if (maxResults != null) {
			req = req.queryString("max_results", maxResults);
		}
		if (sort != null) {
			req = req.queryString("sort", sort);
		}
		if (ascending != null) {
			req = req.queryString("direction",
					ascending.booleanValue() ? "ASCENDING" : "DESCENDING");
		}
		final HttpResponse<JsonNode> res = req.asJson();
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
		return result;
	}

	public void setToken(DgpToken token) {
		this.dgpToken = token;
	}
}