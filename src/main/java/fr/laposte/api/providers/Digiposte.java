package fr.laposte.api.providers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;

import fr.laposte.api.LaPoste.Token;
import fr.laposte.api.LpSdk;
import fr.laposte.api.LpSdk.ApiException;

/**
 *
 * This class provides services of the Digiposte API.
 *
 */
public class Digiposte {

	/**
	 *
	 * Pojo container for Digiposte token informations.
	 *
	 */
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

	public Digiposte() throws MalformedURLException, URISyntaxException {
		final String baseUrl = System.getProperty(
				LpSdk.Env.DIGIPOSTE_API_BASE_URL,
				LpSdk.Defaults.DIGIPOSTE_API_BASE_URL);
		this.apiClient = new LpSdk.ApiClient(baseUrl);
	}

	/**
	 * Authenticate a Digiposte customer, and provide a token for Digiposte API
	 * services.
	 * <p>
	 * The resulting token is stored as "token" instance attribute.
	 *
	 * @param accessToken
	 *            the La Poste access token
	 * @param username
	 *            the Digiposte account username
	 * @param password
	 *            the Digiposte account password
	 * @see Token
	 */
	public void auth(String accessToken, String username, String password)
			throws MalformedURLException, ApiException {
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
		final JsonNode reqBody = new JsonNode("{\"credential\":{\"user\":\""
				+ username + "\", \"password\":\"" + password + "\"}}");
		try {
			final HttpResponse<JsonNode> res = apiClient.post("/login")
					.header("Content-Type", "application/json")
					.header("Accept", "application/json")
					.header("Authorization", "Bearer " + accessToken)
					.body(reqBody).asJson();
			final int code = res.getStatus();
			if (code != 200) {
				throw new ApiException(code);
			}
			this.accessToken = accessToken;
			final JsonNode body = res.getBody();
			final JSONObject result = body.getObject();
			dgpToken.accessToken = result.getString("access_token");
			dgpToken.refreshToken = result.getString("refresh_token");
		} catch (final UnirestException e) {
			throw new ApiException(e);
		}
	}

	public LpSdk.ApiClient getApiClient() {
		return apiClient;
	}

	public DgpToken getDgpToken() {
		return dgpToken;
	}

	/**
	 * Get a document by id.
	 *
	 * @param id
	 *            the document id
	 * @return the resulting JSON object
	 * @throws MalformedURLException
	 * @throws ApiException
	 */
	public JSONObject getDoc(String id) throws MalformedURLException,
			ApiException {
		try {
			final HttpResponse<JsonNode> res = apiClient.get("/document/{id}")
					.routeParam("id", id).header("Accept", "application/json")
					.header("Authorization", "Bearer " + accessToken)
					.header("User-Token", dgpToken.accessToken).asJson();
			final int code = res.getStatus();
			if (code != 200) {
				throw new ApiException(code);
			}
			final JsonNode body = res.getBody();
			final JSONObject result = body.getObject();
			return result;
		} catch (final UnirestException e) {
			throw new ApiException(e);
		}
	}

	/**
	 * Get documents of the safebox.
	 *
	 * @param location
	 *            the location of the documents (NULL, SAFE, INBOX, or TRASH)
	 * @param index
	 *            the index of the pagination
	 * @param maxResults
	 *            the maximum number of results returned
	 * @param sort
	 *            the field on which you want to sort the results
	 * @param ascending
	 *            the direction in which you want to sort the results, for the
	 *            given field : true for ascending, false for descending
	 * @return the resulting JSON object
	 * @throws MalformedURLException
	 * @throws ApiException
	 */
	public JSONObject getDocs(String location, Integer index,
			Integer maxResults, String sort, Boolean ascending)
			throws MalformedURLException, ApiException {
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
		try {
			final HttpResponse<JsonNode> res = req.asJson();
			final int code = res.getStatus();
			if (code != 200) {
				throw new ApiException(code);
			}
			final JsonNode body = res.getBody();
			final JSONObject result = body.getObject();
			return result;
		} catch (final UnirestException e) {
			throw new ApiException(e);
		}
	}

	/**
	 * Get a document thumbnail.
	 *
	 * @param id
	 *            the document id
	 * @return an array of bytes containing the binary data of the downloaded
	 *         thumbnail
	 * @throws ApiException
	 * @throws IOException
	 */
	public byte[] getDocThumbnail(String id) throws ApiException, IOException {
		try {
			final HttpResponse<InputStream> res = apiClient
					.get("/document/{id}/thumbnail").routeParam("id", id)
					.header("Authorization", "Bearer " + accessToken)
					.header("User-Token", dgpToken.accessToken).asBinary();
			final int code = res.getStatus();
			if (code != 200) {
				throw new ApiException(code);
			}
			final InputStream body = res.getRawBody();
			final byte[] buffer = new byte[4096];
			final ByteArrayOutputStream result = new ByteArrayOutputStream();
			int numRead;
			try {
				while ((numRead = body.read(buffer)) > -1) {
					result.write(buffer, 0, numRead);
				}
			} finally {
				body.close();
			}
			result.flush();
			return result.toByteArray();
		} catch (final UnirestException e) {
			throw new ApiException(e);
		}
	}

	public void setToken(DgpToken token) {
		this.dgpToken = token;
	}
}