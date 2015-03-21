package fr.laposte.api;

import static com.jcabi.matchers.RegexMatchers.matchesPattern;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
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
		assertTrue(docs.length() > 0);
		assertTrue(result.getInt("count") >= docs.length());
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
		assertTrue(docs.length() > 0);
		assertTrue(result.getInt("count") >= docs.length());
		final List<String> titleList = new ArrayList<String>();
		for (int i = 0; i < docs.length(); i++) {
			final JSONObject doc = docs.getJSONObject(i);
			titleList.add(doc.getString("title"));
		}
		final String[] titles = new String[titleList.size()];
		titleList.toArray(titles);
		logger.debug("titles : " + Arrays.toString(titles));
		Collections.sort(titleList, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return o1.toLowerCase().compareTo(o2.toLowerCase());
			}
		});
		final String[] expectTitles = new String[titleList.size()];
		titleList.toArray(expectTitles);
		logger.debug("expectTitles : " + Arrays.toString(expectTitles));
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
		assertTrue(docs.length() > 0);
		assertTrue(result.getInt("count") >= docs.length());
		final List<String> titleList = new ArrayList<String>();
		for (int i = 0; i < docs.length(); i++) {
			final JSONObject doc = docs.getJSONObject(i);
			titleList.add(doc.getString("title"));
		}
		final String[] titles = new String[titleList.size()];
		titleList.toArray(titles);
		logger.debug("titles : " + Arrays.toString(titles));
		Collections.sort(titleList, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return o2.toLowerCase().compareTo(o1.toLowerCase());
			}
		});
		final String[] expectTitles = new String[titleList.size()];
		titleList.toArray(expectTitles);
		logger.debug("expectTitles : " + Arrays.toString(expectTitles));
		assertArrayEquals(expectTitles, titles);
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
		assertTrue(docs.length() > 0);
		assertTrue(result.getInt("count") >= docs.length());
		JSONObject doc = docs.getJSONObject(0);
		logger.debug("doc : " + doc);
		docId = doc.getString("id");
		logger.debug("docId : " + docId);
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
		logger.debug("doc : " + doc);
		String[] names = new String[] { "geolocalized", "id", "category",
				"filename", "title", "mimetype", "size", "creation_date",
				"author_name", "document_logo", "location", "read", "shared",
				"digishoot", "certified", "invoice", "eligible2ddoc",
				"favorite", "user_tags", "sender_tags" };
		for (String name : names) {
			assertNotNull(doc.get(name));
		}
	}

}
