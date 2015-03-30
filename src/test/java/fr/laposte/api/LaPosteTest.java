package fr.laposte.api;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static com.jcabi.matchers.RegexMatchers.matchesPattern;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mashape.unirest.http.exceptions.UnirestException;

import fr.laposte.api.LpSdk.ApiException;

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
		assertThat(token.getAccessToken(), matchesPattern("\\w+"));
		assertThat(token.getRefreshToken(), matchesPattern("\\w+"));
		assertEquals("default", token.getScope());
		assertEquals("Bearer", token.getType());
		assertThat(token.getExpiresIn(), greaterThan(0));
		assertThat(token.validityTime(), greaterThan(0L));
		assertTrue(token.isValid());
		assertEquals("Token [accessToken=" + token.getAccessToken()
				+ ", refreshToken=" + token.getRefreshToken() + ", scope="
				+ token.getScope() + ", type=" + token.getType()
				+ ", expiresIn=" + token.getExpiresIn() + ", creationTime="
				+ token.getCreationTime() + "]", token.toString());
	}

	@Test
	public void testAuthAccountError() throws Exception {
		catchException(lp).auth("badconsumerkey", "badconsumersecert",
				"badusername", "badpassword");
		final Exception caughtException = caughtException();
		assertTrue(caughtException instanceof ApiException);
		final ApiException apiException = (ApiException) caughtException;
		assertEquals(401, apiException.getStatusCode());
	}

	@Test
	public void testAuthBaseUrlError() throws Exception {
		final LaPoste lp = new LaPoste("https://api.laposte.fr/badurl/");
		catchException(lp).auth(null, null, null, null);
		final Exception caughtException = caughtException();
		assertTrue(caughtException instanceof ApiException);
		final ApiException apiException = (ApiException) caughtException;
		assertTrue(apiException.getCause() instanceof UnirestException);
		assertEquals(0, apiException.getStatusCode());
	}

	@Test
	public void testRefresh() throws Exception {
		if (lp.getToken().getRefreshToken() == null) {
			lp.auth(null, null, null, null);
		}
		lp.refreshToken(null, null, null);
		final LaPoste.Token token = lp.getToken();
		assertNotNull(token);
		assertThat(token, instanceOf(LaPoste.Token.class));
		assertThat(token.getAccessToken(), matchesPattern("\\w+"));
		assertThat(token.getRefreshToken(), matchesPattern("\\w+"));
		assertEquals("default", token.getScope());
		assertEquals("Bearer", token.getType());
		assertThat(token.getExpiresIn(), greaterThan(0));
		assertThat(token.validityTime(), greaterThan(0L));
		assertTrue(token.isValid());
		assertEquals("Token [accessToken=" + token.getAccessToken()
				+ ", refreshToken=" + token.getRefreshToken() + ", scope="
				+ token.getScope() + ", type=" + token.getType()
				+ ", expiresIn=" + token.getExpiresIn() + ", creationTime="
				+ token.getCreationTime() + "]", token.toString());
	}

}
