package fr.laposte.api.providers;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static com.jcabi.matchers.RegexMatchers.matchesPattern;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.laposte.api.LaPoste;
import fr.laposte.api.LpSdk;
import fr.laposte.api.LpSdk.ApiException;
import fr.laposte.api.providers.Digiposte;

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
		System.setProperty(LpSdk.Env.LAPOSTE_API_ACCESS_TOKEN,
				lp.getToken().accessToken);
		System.setProperty(LpSdk.Env.LAPOSTE_API_REFRESH_TOKEN,
				lp.getToken().refreshToken);
		dgp = new Digiposte();
	}

	private static LaPoste lp;

	private static Digiposte dgp;

	private static final Logger logger = LoggerFactory
			.getLogger(DigiposteTest.class);

	private String docId;

	@Test
	public void testAuth() throws Exception {
		dgp.auth(null, null, null);
		final Digiposte.DgpToken dgpToken = dgp.getDgpToken();
		assertNotNull(dgpToken);
		assertThat(dgpToken.accessToken, matchesPattern("[\\w-]+"));
		assertThat(dgpToken.refreshToken, matchesPattern("[\\w-]+"));
	}

	@Test
	public void testGetDocById() throws Exception {
		if (dgp.getDgpToken().accessToken == null) {
			dgp.auth(null, null, null);
		}
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
	public void testGetDocs() throws Exception {
		if (dgp.getDgpToken().accessToken == null) {
			dgp.auth(null, null, null);
		}
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
	public void testGetDocsSortedByTitle() throws Exception {
		if (dgp.getDgpToken().accessToken == null) {
			dgp.auth(null, null, null);
		}
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
			public int compare(String o1, String o2) {
				return o1.toLowerCase().compareTo(o2.toLowerCase());
			}
		});
		final String[] expectTitles = new String[titleList.size()];
		titleList.toArray(expectTitles);
		assertArrayEquals(expectTitles, titles);
	}

	@Test
	public void testGetDocsSortedByTitleDescending() throws Exception {
		if (dgp.getDgpToken().accessToken == null) {
			dgp.auth(null, null, null);
		}
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
			public int compare(String o1, String o2) {
				return o2.toLowerCase().compareTo(o1.toLowerCase());
			}
		});
		final String[] expectTitles = new String[titleList.size()];
		titleList.toArray(expectTitles);
		assertArrayEquals(expectTitles, titles);
	}

	@Test
	public void testGetDocThumnail() throws Exception {
		if (dgp.getDgpToken().accessToken == null) {
			dgp.auth(null, null, null);
		}
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
	public void testGetFirstDocSafebox() throws Exception {
		if (dgp.getDgpToken().accessToken == null) {
			dgp.auth(null, null, null);
		}
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
	public void testGetProfile() throws Exception {
		if (dgp.getDgpToken().accessToken == null) {
			dgp.auth(null, null, null);
		}
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
	public void testGetProfileAvatar() throws Exception {
		if (dgp.getDgpToken().accessToken == null) {
			dgp.auth(null, null, null);
		}
		final byte[] content = dgp.getProfileAvatar();
		assertNotNull(content);
		assertThat(content.length, greaterThan(0));
	}

	@Test
	public void testGetTou() throws Exception {
		if (dgp.getDgpToken().accessToken == null) {
			dgp.auth(null, null, null);
		}
		final JSONObject result = dgp.getTou();
		assertNotNull(result);
		assertNotNull(result.getString("version"));
		assertNotNull(result.getString("href"));
	}

	@Test
	public void testTryToGetDocWithBadId() throws Exception {
		if (dgp.getDgpToken().accessToken == null) {
			dgp.auth(null, null, null);
		}
		final String docId = "badid";
		catchException(dgp).getDoc(docId);
		final Exception caughtException = caughtException();
		assertTrue(caughtException instanceof ApiException);
		final ApiException apiException = (ApiException) caughtException;
		assertEquals(403, apiException.getStatusCode());
	}
}
