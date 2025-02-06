package cv.mcdonnell

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        // Create an unauthenticated route
        get("/unauthenticated") {
            call.respondText("Hello World!")
        }

        // The GCP Cloud Task Queue invokes App Engine Tasks with a request that includes
        // a header named "X-AppEngine-QueueName" that contains the name of the queue that
        // the task belongs to, and a header named "X-AppEngine-TaskName" that contains the
        // name of the task. If an App Engine service is invoked from something other than
        // a task queue, these headers will not be present (App Engine will strip them out).
        // Thus, we can verify that a request is coming from a task queue by checking for
        // the presence of these headers.
        authenticate(APP_ENGINE_TASK_REQUEST_AUTH_NAME) {
            get("/authenticated") {
                val principal = call.principal<AppEngineTaskRequestPrincipal>()!!
                call.respondText("Successfully authenticated with queueName=${principal.queueName} and taskName=${principal.taskName}")
            }
        }
    }
}
