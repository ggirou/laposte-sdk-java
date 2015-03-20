package fr.laposte.api;

import static com.jcabi.matchers.RegexMatchers.matchesPattern;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

}
