package cv.mcdonnell

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

data class AppEngineTaskRequestPrincipal(val queueName: String, val taskName: String)

const val APP_ENGINE_TASK_REQUEST_AUTH_NAME = "app-engine-task-request"

fun Application.configureSecurity() {
    install(Authentication) {
        httpHeaders(APP_ENGINE_TASK_REQUEST_AUTH_NAME) {
            authenticate { credentials ->
                val queueName = credentials.headers["X-AppEngine-QueueName"]
                val taskName = credentials.headers["X-AppEngine-TaskName"]
                if (queueName != null && taskName != null) {
                    // Expected headers are present, allow access
                    AppEngineTaskRequestPrincipal(queueName, taskName)
                } else {
                    // Expected headers are missing, reject access
                    null
                }
            }
        }
    }
}
