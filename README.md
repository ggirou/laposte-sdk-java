[![Build Status](https://travis-ci.org/LaPosteApi/laposte-sdk-java.png?branch=master)](https://travis-ci.org/LaPosteApi/laposte-sdk-java)
[![Coverage Status](https://coveralls.io/repos/LaPosteApi/laposte-sdk-java/badge.svg)](https://coveralls.io/r/LaPosteApi/laposte-sdk-java)

# La Poste Open API SDK Java

<a href="http://laposte.fr/" target="_blank">
<img src="http://upload.wikimedia.org/wikipedia/fr/2/2a/Logo-laposte.png" alt="La Poste" height="200">
</a>
<a href="http://fr.wikipedia.org/wiki/Java_%28langage%29" target="_blank">
<img src="http://answers.ea.com/t5/image/serverpage/image-id/10151i305CAFB28ED1CE16?v=mpbl-1" alt="JavaScript" height="200">
</a>

The official La Poste Open API SDK for the Java language.

More informations about Open API and La Poste (french) : [developer.laposte.fr](http://developer.laposte.fr/)

## Usage

To use an exposed API (by example Digiposte), you first need to authenticate with LaPoste.auth method.

Once you got an access token, you are able to use it with any API provider.

### Get a La Poste token

```java
import java.io.*;
import fr.laposte.api.*;

public class LaPosteSample {
	public static void main(String[] args) {
		try {
			LpSdk.ApiClient.init();
			LaPosteSample sample = new LaPosteSample();
			sample.getToken();
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			try {
				LpSdk.ApiClient.quit();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	void getToken() throws Exception {
		LaPoste lp = new LaPoste();
		String clientId = null; // Replace with your own datas
		String clientSecret = null; // Replace with your own datas
		String username = null; // Replace with your own datas
		String password = null; // Replace with your own datas
		lp.auth(clientId, clientSecret, username, password);
		System.out.println("token : " + lp.getToken());
	}
}
```

### Get a Digiposte token

```java
import java.io.*;
import org.json.*;
import fr.laposte.api.*;
import fr.laposte.api.providers.*;

public class DigiposteSample {
	public static void main(String[] args) {
		try {
			LpSdk.ApiClient.init();
			DigiposteSample sample = new DigiposteSample();
			sample.getDgpToken();
			sample.getDocs();
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			try {
				LpSdk.ApiClient.quit();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	LaPoste lp;
	Digiposte dgp;

	void getDgpToken() throws Exception {
		lp = new LaPoste();
		String clientId = null; // Replace with your own datas
		String clientSecret = null; // Replace with your own datas
		String username = null; // Replace with your own datas
		String password = null; // Replace with your own datas
		lp.auth(clientId, clientSecret, username, password);
		dgp = new Digiposte();
		String dgpUsername = null; // Replace with your own datas
		String dgpPassword = null; // Replace with your own datas
		dgp.auth(lp.getToken().accessToken, dgpUsername, dgpPassword);
		System.out.println("token : " + dgp.getDgpToken());
	}
}
```
### Get Digiposte documents

```java
import java.io.*;
import org.json.*;
import fr.laposte.api.*;
import fr.laposte.api.providers.*;

public class DigiposteSample {
	public static void main(String[] args) {
		try {
			LpSdk.ApiClient.init();
			DigiposteSample sample = new DigiposteSample();
			sample.getDgpToken();
			sample.getDocs();
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			try {
				LpSdk.ApiClient.quit();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	LaPoste lp;
	Digiposte dgp;

	void getDocs() throws Exception {
		String location = null; // Available values : TRASH, INBOX, SAFE (default : all documents)
		Integer index = null; // default : 0
		Integer maxResults = null;// default : 10
		String sort = null; // Field name to sort with
		Boolean ascending = null; // true:ASCENDING, false:DESCENDING
		JSONObject docs = dgp.getDocs(location, index, maxResults, sort, ascending);
		System.out.println("docs : " + docs);
	}
}
```

## License

This SDK is distributed under the [MIT License](https://raw.githubusercontent.com/LaPosteApi/laposte-sdk-js/master/LICENSE).

Enjoy !