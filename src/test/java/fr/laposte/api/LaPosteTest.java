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
		ApiClient.quit();
	}

	@BeforeClass
	public static void beforeClass() throws Exception {
		ApiClient.init();
	}

	private static final Logger logger = LoggerFactory
			.getLogger(LaPosteTest.class);

	@Test
	public void testAuth() throws Exception {
		final LaPoste lp = new LaPoste();
		lp.auth(null, null, null, null);
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
