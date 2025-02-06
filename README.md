# ktor-server-http-header-auth-example

This project demonstrates how to create a custom authentication provider for Ktor server. This custom authentication
provider extracts HTTP headers from the HTTP request, and allows the developer to examine them and return a principal to
allow authentication to succeed. The [HttpHeadersAuthenticationProvider](src/main/kotlin/HttpHeadersAuthenticationProvider.kt)
file demonstrates how to create this custom authentication provider. It is tested in the 
[HttpHeadersAuthenticationProviderTest](src/test/kotlin/HttpHeadersAuthenticationProviderTest.kt) file.

## Authenticating requests from Google Cloud Tasks to Google App Engine Task Handlers

This project was created to demonstrate how to authenticate requests from Google Cloud Tasks to Google App Engine Task
Handlers. A Google App Engine service might serve public requests from a web application or mobile application over
the Internet, or it may serve private requests from Google Cloud Tasks. Public requests might be authenticated using
Ktor's out-of-the-box handling for JWT tokens, OAuth2, or other authentication mechanisms. However, Google Cloud Tasks
requests are not authenticated by default, and the developer must implement their own authentication mechanism.

Requests from Google Cloud Tasks to Google App Engine Task Handlers include certain HTTP request headers, including
`X-AppEngine-QueueName` and `X-AppEngine-TaskName`. Further documentation is available [here](https://cloud.google.com/tasks/docs/creating-appengine-handlers#reading-headers).
App Engine will strip these request headers if the request originates from the Internet. Thus, if these headers are
present, the our application can conclude that the request is from Google Cloud Tasks.

In [Application.configureSecurity](src/main/kotlin/Security.kt), we demonstrate how to configure the `HttpHeadersAuthenticationProvider`
to authenticate requests from Google Cloud Tasks. We examine the `X-AppEngine-QueueName` and `X-AppEngine-TaskName` headers
to determine if the request is from Google Cloud Tasks. If so, we return a principal to allow the request to proceed.

In [Application.configureRouting](src/main/kotlin/Routing.kt), we demonstrate how to require that these request headers
be present in order to access a route. If the headers are not present, the request will be rejected with a 401 Unauthorized
response.

In [ApplicationTest](/src/test/kotlin/ApplicationTest.kt), we demonstrate how to test the application with and without the
required headers present. You can also run the test server, and use `curl`, or another HTTP client of your choice, to
test the application.

```console
$ curl -i localhost:8080/unauthenticated
HTTP/1.1 200 OK
Content-Length: 12
Content-Type: text/plain; charset=UTF-8

Hello World!
```

```console
$ curl -i localhost:8080/authenticated
HTTP/1.1 401 Unauthorized
Content-Length: 0
```

```console
$ curl -i -H "X-AppEngine-QueueName: TestQueue" -H "X-AppEngine-TaskName: TestTask" localhost:8080/authenticated
HTTP/1.1 200 OK
Content-Length: 73
Content-Type: text/plain; charset=UTF-8

Successfully authenticated with queueName=TestQueue and taskName=TestTask
```