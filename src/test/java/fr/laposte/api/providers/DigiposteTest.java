package fr.laposte.api.providers;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static com.jcabi.matchers.RegexMatchers.matchesPattern;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.laposte.api.LaPoste;
import fr.laposte.api.LpSdk;
import fr.laposte.api.LpSdk.ApiException;
import fr.laposte.api.providers.Digiposte.DgpToken;

public class DigiposteTest {
	@AfterClass
	public static void afterClass() throws Exception {
		LpSdk.ApiClient.quit();
	}

	@BeforeClass
	public static void beforeClass() throws Exception {
		LpSdk.ApiClient.init();
		lp = new LaPoste();
		lp.auth(null, null, null, null);
		System.setProperty(LpSdk.Env.LAPOSTE_API_ACCESS_TOKEN, lp.getToken()
				.getAccessToken());
		System.setProperty(LpSdk.Env.LAPOSTE_API_REFRESH_TOKEN, lp.getToken()
				.getRefreshToken());
		dgp = new Digiposte();
	}

	private static LaPoste lp;

	private static Digiposte dgp;

	private static final Logger logger = LoggerFactory
			.getLogger(DigiposteTest.class);

	private String docId;

	@Before
	public void auth() throws MalformedURLException, ApiException {
		final DgpToken dgpToken = dgp.getDgpToken();
		if (dgpToken.getAccessToken() == null) {
			dgp.auth(null, null, null);
			/*
			 * } else { if (!dgpToken.isValid()) { dgp.refreshToken(null); }
			 */
		}
	}

	@Test
	public void testAuth() {
		final Digiposte.DgpToken dgpToken = dgp.getDgpToken();
		assertNotNull(dgpToken);
		logger.debug("dgpToken : " + dgpToken);
		assertThat(dgpToken.getAccessToken(), matchesPattern("[\\w-]+"));
		assertThat(dgpToken.getRefreshToken(), matchesPattern("[\\w-]+"));
		assertEquals("bearer", dgpToken.getType());
		assertThat(dgpToken.getExpiresIn(), greaterThan(0));
		assertThat(dgpToken.validityTime(), greaterThan(0L));
		assertTrue(dgpToken.isValid());
		assertEquals("DgpToken [accessToken=" + dgpToken.getAccessToken()
				+ ", refreshToken=" + dgpToken.getRefreshToken() + ", type="
				+ dgpToken.getType() + ", expiresIn=" + dgpToken.getExpiresIn()
				+ ", creationTime=" + dgpToken.getCreationTime() + "]",
				dgpToken.toString());
	}

	@Test
	public void testGetDocById() throws JSONException, MalformedURLException,
	ApiException {
		String docId = System.getProperty("DIGIPOSTE_API_DOC_ID", this.docId);
		if (docId == null) {
			final JSONArray docs = (JSONArray) dgp.getDocs("safe", 1, 1, null,
					null).get("documents");
			docId = docs.getJSONObject(0).getString("id");
		}
		final JSONObject doc = dgp.getDoc(docId);
		assertNotNull(doc);
		final String[] names = new String[] { "geolocalized", "id", "category",
				"filename", "title", "mimetype", "size", "creation_date",
				"author_name", "document_logo", "location", "read", "shared",
				"digishoot", "certified", "invoice", "eligible2ddoc",
				"favorite", "user_tags", "sender_tags" };
		for (final String name : names) {
			assertNotNull(doc.get(name));
		}
	}

	@Test
	public void testGetDocs() throws MalformedURLException, ApiException {
		final JSONObject result = dgp.getDocs(null, null, null, null, null);
		assertNotNull(result);
		assertEquals(0, result.getInt("index"));
		assertEquals(10, result.getInt("max_results"));
		assertThat(result.get("documents"), instanceOf(JSONArray.class));
		final JSONArray docs = (JSONArray) result.get("documents");
		assertThat(docs.length(), greaterThan(0));
		assertThat(result.getInt("count"), greaterThanOrEqualTo(docs.length()));
	}

	@Test
	public void testGetDocsSortedByTitle() throws MalformedURLException,
	ApiException {
		final JSONObject result = dgp.getDocs(null, null, null, "TITLE", true);
		assertNotNull(result);
		assertThat(result.get("documents"), instanceOf(JSONArray.class));
		final JSONArray docs = (JSONArray) result.get("documents");
		assertThat(docs.length(), greaterThan(0));
		assertThat(result.getInt("count"), greaterThanOrEqualTo(docs.length()));
		final List<String> titleList = new ArrayList<String>();
		for (int i = 0; i < docs.length(); i++) {
			final JSONObject doc = docs.getJSONObject(i);
			titleList.add(doc.getString("title"));
		}
		final String[] titles = new String[titleList.size()];
		titleList.toArray(titles);
		Collections.sort(titleList, new Comparator<String>() {

			@Override
			public int compare(final String o1, final String o2) {
				return o1.toLowerCase().compareTo(o2.toLowerCase());
			}
		});
		final String[] expectTitles = new String[titleList.size()];
		titleList.toArray(expectTitles);
		assertArrayEquals(expectTitles, titles);
	}

	@Test
	public void testGetDocsSortedByTitleDescending()
			throws MalformedURLException, ApiException {
		final JSONObject result = dgp.getDocs(null, null, null, "TITLE", false);
		assertNotNull(result);
		assertThat(result.get("documents"), instanceOf(JSONArray.class));
		final JSONArray docs = (JSONArray) result.get("documents");
		assertThat(docs.length(), greaterThan(0));
		assertThat(result.getInt("count"), greaterThanOrEqualTo(docs.length()));
		final List<String> titleList = new ArrayList<String>();
		for (int i = 0; i < docs.length(); i++) {
			final JSONObject doc = docs.getJSONObject(i);
			titleList.add(doc.getString("title"));
		}
		final String[] titles = new String[titleList.size()];
		titleList.toArray(titles);
		Collections.sort(titleList, new Comparator<String>() {

			@Override
			public int compare(final String o1, final String o2) {
				return o2.toLowerCase().compareTo(o1.toLowerCase());
			}
		});
		final String[] expectTitles = new String[titleList.size()];
		titleList.toArray(expectTitles);
		assertArrayEquals(expectTitles, titles);
	}

	@Test
	public void testGetDocThumnail() throws JSONException, ApiException,
	IOException {
		String docId = System.getProperty("DIGIPOSTE_API_DOC_ID", this.docId);
		if (docId == null) {
			final JSONArray docs = (JSONArray) dgp.getDocs("safe", 1, 1, null,
					null).get("documents");
			docId = docs.getJSONObject(0).getString("id");
		}
		final byte[] content = dgp.getDocThumbnail(docId);
		assertNotNull(content);
		assertEquals(711, content.length);
	}

	@Test
	public void testGetFirstDocSafebox() throws MalformedURLException,
	ApiException {
		final JSONObject result = dgp.getDocs("safe", 1, 1, null, null);
		assertNotNull(result);
		assertEquals(1, result.getInt("index"));
		assertEquals(1, result.getInt("max_results"));
		assertThat(result.get("documents"), instanceOf(JSONArray.class));
		final JSONArray docs = (JSONArray) result.get("documents");
		assertThat(docs.length(), greaterThan(0));
		assertThat(result.getInt("count"), greaterThanOrEqualTo(docs.length()));
		final JSONObject doc = docs.getJSONObject(0);
		docId = doc.getString("id");
	}

	@Test
	public void testGetProfile() throws MalformedURLException, ApiException {
		final JSONObject profile = dgp.getProfile();
		assertNotNull(profile);
		final String[] names = new String[] { "id", "title", "first_name",
				"last_name", "date_of_birth", "id_xiti", "login", "user_type",
				"status", "space_used", "space_free", "space_max",
				"space_not_computed", "author_name", "support_available",
				"tos_version", "tos_updated_at", "share_space_status",
				"partial_account", "basic_user", "offer_pid",
				"offer_updated_at", "show2ddoc", "idn_valid",
				"last_connexion_date", "completion" };
		for (final String name : names) {
			assertNotNull(profile.get(name));
		}
		assertEquals("MR", profile.getString("title"));
		assertEquals("digiposte", profile.getString("first_name"));
		assertEquals("digiposte", profile.getString("last_name"));
		assertEquals(dgp.getUsername(), profile.getString("login"));
		assertEquals("PERSON", profile.getString("user_type"));
		assertEquals("VALID", profile.getString("status"));
		assertThat(profile.getInt("space_used"), greaterThan(0));
		assertThat(profile.getInt("space_free"), greaterThan(0));
		assertThat(profile.getInt("space_max"), greaterThan(0));
		assertFalse(profile.getBoolean("partial_account"));
		assertTrue(profile.getBoolean("basic_user"));
	}

	@Test
	public void testGetProfileAvatar() throws ApiException, IOException {
		final byte[] content = dgp.getProfileAvatar();
		assertNotNull(content);
		assertThat(content.length, greaterThan(0));
	}

	@Test
	public void testGetTou() throws MalformedURLException, ApiException {
		final JSONObject result = dgp.getTou();
		assertNotNull(result);
		assertNotNull(result.getString("version"));
		assertNotNull(result.getString("href"));
	}

	@Test
	public void testTryToGetDocWithBadId() throws MalformedURLException,
	ApiException {
		final String docId = "badid";
		catchException(dgp).getDoc(docId);
		final Exception caughtException = caughtException();
		assertTrue(caughtException instanceof ApiException);
		final ApiException apiException = (ApiException) caughtException;
		assertEquals(403, apiException.getStatusCode());
	}
}
