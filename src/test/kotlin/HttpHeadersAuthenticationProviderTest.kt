import cv.mcdonnell.httpHeaders
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlin.test.Test


data class HttpHeadersPrincipal(val headers: Headers)

class HttpHeadersAuthenticationProviderTest {
    @Test
    fun testRespectsHeader() = testApplication {
        application { testModule() }

        val response = client.get("/") {
            headers {
                append("X-Custom-Auth", "letmein")
            }
        }

        assert(response.status.isSuccess())
        assert(response.bodyAsText() == "letmein")
    }

    @Test
    fun testRejectIfHeaderIsMissing() = testApplication {
        application { testModule() }

        val response = client.get("/")

        assert(response.status == HttpStatusCode.Unauthorized)
    }

    private fun Application.testModule() {
        install(Authentication) {
            httpHeaders {
                authenticate { credentials ->
                    if (credentials.headers["X-Custom-Auth"] == "letmein") {
                        HttpHeadersPrincipal(credentials.headers)
                    } else {
                        null
                    }
                }
            }
        }

        routing {
            authenticate {
                get("/") {
                    // If authentication is successful, the principal is set to HttpHeadersPrincipal,
                    // and the call responds with the value of the "X-Custom-Auth" header.
                    call.respondText(call.authentication.principal<HttpHeadersPrincipal>()!!.headers["X-Custom-Auth"]!!)
                }
            }
        }
    }
}
