package fr.laposte.api;

import static com.jcabi.matchers.RegexMatchers.matchesPattern;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LpSdkTest extends LpSdk {

	private static final Logger logger = LoggerFactory
			.getLogger(LpSdkTest.class);

	@Test
	public void testBuildApiUrl() throws MalformedURLException,
			URISyntaxException {
		String url = LpSdk.buildApiUrl(new URL("https://api.laposte.fr"),
				"/my/resource");
		assertEquals(new URL("https://api.laposte.fr/my/resource").toString(),
				url);
	}

	@Test
	public void testBuildBaseUrl() throws MalformedURLException,
			URISyntaxException {
		URL url = LpSdk.buildBaseUrl("https://api.laposte.fr");
		assertEquals(new URL("https://api.laposte.fr"), url);
	}

	@Test
	public void testGetVersion() {
		String version = LpSdk.getVersion();
		assertThat(version, matchesPattern("^\\d\\.\\d\\.\\d(-SNAPSHOT)?"));
	}

}
