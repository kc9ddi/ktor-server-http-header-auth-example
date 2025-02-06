package cv.mcdonnell

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*


data class HttpHeadersCredentials(val headers: Headers)

/**
 * An [AuthenticationProvider] that authenticates using the headers in the request.
 * This provider will pass the HTTP request headers to the [authenticate] function, which should
 * examine the headers and return a [Principal] if they are valid.
 */
class HttpHeaderAuthenticationProvider internal constructor(config: Config) :
    AuthenticationProvider(config) {

    private val getHeaders = config.getHeaders
    private val authenticate = config.authenticate

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val headers = getHeaders(context.call)
        val principal = authenticate(context.call, HttpHeadersCredentials(headers))

        if (principal != null) {
            context.principal(name, principal)
            return
        }

        context.challenge(challengeKey, AuthenticationFailedCause.InvalidCredentials) { challenge, call ->
            call.respond(UnauthorizedResponse())
            challenge.complete()
        }
    }

    public class Config(name: String?) : AuthenticationProvider.Config(name) {
        internal var authenticate: AuthenticationFunction<HttpHeadersCredentials> = {
            throw NotImplementedError(
                "HttpHeaders auth authenticate function is not specified. Use httpHeaders { authenticate { ... } } to fix."
            )
        }

        internal var getHeaders: (ApplicationCall) -> Headers = { call ->
            call.request.headers
        }

        /**
         * Parses the headers and returns a Principal.
         *
         * @return a principal or `null`
         */
        public fun authenticate(authenticate: suspend ApplicationCall.(HttpHeadersCredentials) -> Any?) {
            this.authenticate = authenticate
        }

        internal fun build() = HttpHeaderAuthenticationProvider(this)
    }
}

/**
 * Installs the Http Headers [Authentication] provider.
 * Http Headers auth requires the developer to provide a custom 'authenticate' function to authorize the headers
 * provided in the request, and return the associated principal.
 */
public fun AuthenticationConfig.httpHeaders(
    name: String? = null,
    configure: HttpHeaderAuthenticationProvider.Config.() -> Unit,
) {
    val provider = HttpHeaderAuthenticationProvider.Config(name).apply(configure).build()
    register(provider)
}

private val challengeKey: Any = "HttpHeadersAuth"