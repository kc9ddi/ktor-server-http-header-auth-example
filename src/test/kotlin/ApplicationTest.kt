package cv.mcdonnell

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun testUnauthenticatedEndpoint() = testApplication {
        application {
            module()
        }
        client.get("/unauthenticated").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello World!", bodyAsText())
        }
    }

    @Test
    fun testAuthenticatedEndpointWithoutHeaders() = testApplication {
        application {
            module()
        }
        client.get("/authenticated").apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }

    @Test
    fun testAuthenticatedEndpointWithAppEngineHeaders() = testApplication {
        application {
            module()
        }
        client.get("/authenticated") {
            headers {
                append("X-AppEngine-QueueName", "QueueName")
                append("X-AppEngine-TaskName", "TaskName")
            }
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Successfully authenticated with queueName=QueueName and taskName=TaskName", bodyAsText())
        }
    }

}
