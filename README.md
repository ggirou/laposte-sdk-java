[![Build Status](https://travis-ci.org/LaPosteApi/laposte-sdk-java.png?branch=master)](https://travis-ci.org/LaPosteApi/laposte-sdk-java)
[![Coverage Status](https://coveralls.io/repos/LaPosteApi/laposte-sdk-java/badge.svg)](https://coveralls.io/r/LaPosteApi/laposte-sdk-java)
<!--[![Maven Central](https://maven-badges.herokuapp.com/maven-central/fr.laposte.api/laposte-sdk/badge.svg)](https://maven-badges.herokuapp.com/maven-central/fr.laposte.api/laposte-sdk)-->

## Coming soon :
# La Poste Open API SDK Java

<a href="http://laposte.fr/" target="_blank">
<img src="http://upload.wikimedia.org/wikipedia/fr/2/2a/Logo-laposte.png" alt="La Poste" height="200">
</a>
<a href="http://fr.wikipedia.org/wiki/Java_%28langage%29" target="_blank">
<img src="http://answers.ea.com/t5/image/serverpage/image-id/10151i305CAFB28ED1CE16?v=mpbl-1" alt="JavaScript" height="200">
</a>

The official La Poste Open API SDK for the Java language.

More informations about Open API and La Poste (french) : [developer.laposte.fr](http://developer.laposte.fr/)

## Installation

### Maven 

Add maven dependency to your pom.xml

```xml
<dependency>
	<groupId>fr.laposte</groupId>
	<artifactId>laposte-sdk</artifactId>
	<version>0.0.1</version>
</dependency>
```

## What's provided?

The La Poste Open API developer kit provides some service classes that make API consumption easy.

Available services :

### [LaPoste](https://developer.laposte.fr)

This is the main service, its main goal is to deal with Open API token.

Have a look at [LaPoste api doc](http://laposteapi.github.io/laposte-sdk-js/classes/LaPoste.html) for more information.

### [Digiposte](http://www.laposte.fr/particulier/produits/presentation/digiposte/vos-donnees-securisees-a-vie)

Digiposte is a safebox web application, and an API provider of the Groupe La Poste.

The Digiposte class exposes all the things you need to consume Digiposte APIs.

Have a look at [Digiposte api doc](http://laposteapi.github.io/laposte-sdk-js/classes/Digiposte.html) for more information.

## Usage

To use an exposed API (by example Digiposte), you first need to authenticate with LaPoste.auth method.

Once you got an access token, you are able to use it with any API provider.

### Get a La Poste token

```java
LaPoste lp = new LaPoste();
String clientId = null; // Replace with your own datas
String clientSecret = null; // Replace with your own datas
String username = null; // Replace with your own datas
String password = null; // Replace with your own datas
lp.auth(clientId, clientSecret, username, password);
System.out.println("token : " + lp.getToken());
```

### Get a Digiposte token

```java
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
```

### Get Digiposte documents

```java
String location = null; // Available values : safe, trash, inbox (default : all documents)
Integer index = null; // default : 0
Integer maxResults = null;// default : 10
String sort = null; // Field name to sort with
Boolean ascending = null; // true:ASCENDING, false:DESCENDING
JSONObject docs = dgp.getDocs(location, index, maxResults, sort, ascending);
System.out.println("docs : " + docs);
```

### Get a Digiposte document by id

```java
JSONObject doc = dgp.getDoc(docId);
System.out.println("doc : " + doc);
```

## API doc

Full documentation : [laposteapi.github.io/laposte-sdk-java](http://laposteapi.github.io/laposte-sdk-java)

## License

This SDK is distributed under the [MIT License](https://raw.githubusercontent.com/LaPosteApi/laposte-sdk-java/master/LICENSE).

Enjoy !
