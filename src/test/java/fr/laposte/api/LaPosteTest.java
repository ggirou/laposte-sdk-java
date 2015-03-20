package fr.laposte.api;

import static com.jcabi.matchers.RegexMatchers.matchesPattern;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LaPosteTest {
	@AfterClass
	public static void afterClass() throws Exception {
		LpSdk.ApiClient.quit();
	}

	@BeforeClass
	public static void beforeClass() throws Exception {
		LpSdk.ApiClient.init();
		lp = new LaPoste();
	}

	private static LaPoste lp;

	private static final Logger logger = LoggerFactory
			.getLogger(LaPosteTest.class);

	@Test
	public void testAuth() throws Exception {
		lp.auth(null, null, null, null);
		final LaPoste.Token token = lp.getToken();
		assertNotNull(token);
		assertThat(token.accessToken, matchesPattern("\\w+"));
		assertThat(token.refreshToken, matchesPattern("\\w+"));
		assertEquals("default", token.scope);
		assertEquals("Bearer", token.type);
		assertThat(Integer.valueOf(token.expiresIn), greaterThan(0));
	}

	@Test
	public void testRefresh() throws Exception {
		if (lp.getToken().refreshToken == null) {
			lp.auth(null, null, null, null);
		}
		lp.refreshToken(null, null, null);
		final LaPoste.Token token = lp.getToken();
		assertNotNull(token);
		assertThat(token, instanceOf(LaPoste.Token.class));
		assertThat(token.accessToken, matchesPattern("\\w+"));
		assertThat(token.refreshToken, matchesPattern("\\w+"));
		assertEquals("default", token.scope);
		assertEquals("Bearer", token.type);
		assertThat(Integer.valueOf(token.expiresIn), greaterThan(0));
	}

}
