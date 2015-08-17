# HttpKnife

It's a Android HTTP Request Library based on URLConnection.I have use it in [WeGit](https://github.com/Leaking/WeGit) and [HttpKnife](https://github.com/Leaking/HttpKnife).


# Asynchronous ? 

It's not asynchronous so that you need to handle the asynchronous problem yourself.You can use Thread-Handler or AsyncTask.


# Examples


## Handle the Response

The response is wrapped into an object called `Response`,you can get the statusCode ,headers,body in string format body in json.

```java

response.statusCode();
response.headers();
response.body(); //get response body in string-format
response.json(); //get response body in json-format

```
       

## Get Request without parameter

```java
HttpKnife http = new HttpKnife(context);
Response response = http.get(url).response();
```

## Get Request with parameter

```java
Map<String,String> params = new HashMap<String,String>();
params.put("key1","value1");
params.put("key2","value2");
HttpKnife http = new HttpKnife(context);
Response response = http.get(url,params).response();
```
## Post Request: Form

```java

Map<String,String> params = new HashMap<String,String>();
params.put("key1","value1");
params.put("key2","value2");
Response response = http.post(url).form(params).response();
```
## Post Request: Json

```java
JSON json;
,,,
Response response = http.post(url).json(json).response();
```

## Post Request: Mutipart

```java
File tempFile = createTempFile();
Map<String,String> params = new HashMap<String,String>();
params.put("key1","value1");
params.put("key2","value2");
Response response = http.post(url).mutipart(params, "file-key-name", "file.txt", tempFile).response();
```

## GZIP

```java
Response response = http.get(url).gzip().response();
```

## Basic Authorization

```java
Response response = http.post(url).basicAuthorization(username, password).response();
```

## Custom Headers

```java
Response response = http.post(url)
                .headers(params)
                .response();
```

