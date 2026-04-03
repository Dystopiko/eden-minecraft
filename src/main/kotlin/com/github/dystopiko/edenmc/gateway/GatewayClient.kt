package com.github.dystopiko.edenmc.gateway

import com.github.dystopiko.edenmc.config.GatewayConfig
import com.github.dystopiko.edenmc.exceptions.GatewayException
import com.github.dystopiko.edenmc.gateway.admin.Invitees
import com.github.dystopiko.edenmc.gateway.admin.PatchSettings
import com.github.dystopiko.edenmc.gateway.alerts.AlertAdminCommandUse
import com.github.dystopiko.edenmc.gateway.alerts.CommandExecutor
import com.github.dystopiko.edenmc.gateway.members.FullMember
import com.github.dystopiko.edenmc.gateway.members.LinkChallenge
import com.github.dystopiko.edenmc.gateway.members.LinkMcAccount
import com.github.dystopiko.edenmc.gateway.sessions.RequestSession
import com.github.dystopiko.edenmc.gateway.sessions.SessionGranted
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.Route
import java.io.IOException
import java.time.Duration
import java.util.UUID
import kotlin.jvm.Throws

/**
 * HTTP client for the Eden Gateway API.
 */
class GatewayClient(config: GatewayConfig) {
    private val baseUrl = config.baseUrl.trimEnd('/')
    private val http: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(Duration.ofSeconds(30))
        .followRedirects(true)
        .authenticator(TokenAuthenticator(config.token))
        .build()

    /**
     * Changes guild settings with the following parameters provided in `PatchSettings`
     * (`PATCH /admin/settings`)
     *
     * @throws GatewayException if the gateway returns an error response.
     * @throws java.io.IOException if the gateway is unreachable.
     */
    @Throws(GatewayException::class, IOException::class)
    fun patchSettings(delta: PatchSettings) {
        val request = Request.Builder()
            .url("$baseUrl/admin/settings")
            .patch(json.encodeToString(delta).toRequestBody(JSON_MEDIA_TYPE))
            .build()

        perform(request)
    }

    /**
     * Gets all members invited by a specified member from their Discord ID
     * (`GET /admin/members/{id}/invitees`)
     *
     * @throws GatewayException if the gateway returns an error response.
     * @throws java.io.IOException if the gateway is unreachable.
     */
    @Throws(GatewayException::class, IOException::class)
    fun getInvitees(id: String): Invitees {
        assert(id.toCharArray().all { it.isDigit() })
        return model(Request.Builder().url("$baseUrl/admin/members/$id/invitees").get().build())
    }

    /**
     * Gets the detailed information of a member from a member's Discord ID.
     * (`GET /admin/members/{id}`)
     *
     * @throws GatewayException if the gateway returns an error response.
     * @throws java.io.IOException if the gateway is unreachable.
     */
    @Throws(GatewayException::class, IOException::class)
    fun fetchFullMember(id: String): FullMember {
        assert(id.toCharArray().all { it.isDigit() })
        return model(Request.Builder().url("$baseUrl/admin/members/$id").get().build())
    }

    /**
     * Logs a potential admin command use and sends over to the appropriate
     * admin channels for possible intervention. (`POST /alerts/admin_commands`).
     *
     * @throws GatewayException if the gateway returns an error response.
     * @throws java.io.IOException if the gateway is unreachable.
     */
    @Throws(GatewayException::class, IOException::class)
    fun logCommandAlert(command: String, executor: CommandExecutor) {
        val body = AlertAdminCommandUse(command, executor)
        val request = Request.Builder()
            .url("$baseUrl/alerts/admin_commands")
            .put(json.encodeToString(body).toRequestBody(JSON_MEDIA_TYPE))
            .build()

        perform(request)
    }

    /**
     * Requests to start a linking process to any member's Discord account
     * (`POST /members/link/minecraft`).
     *
     * @throws GatewayException if the gateway returns an error response.
     * @throws java.io.IOException if the gateway is unreachable.
     */
    @Throws(GatewayException::class, IOException::class)
    fun linkAccount(uuid: UUID, username: String, ip: String, java: Boolean): LinkChallenge {
        val request = Request.Builder()
            .url("$baseUrl/members/link/minecraft")
            .json(LinkMcAccount(uuid, username, ip, java))
            .build()

        return model(request)
    }

    /**
     * Requests a new session for a connecting player (`POST /sessions`).
     *
     * @throws GatewayException if the gateway returns an error response.
     * @throws java.io.IOException if the gateway is unreachable.
     */
    @Throws(GatewayException::class, IOException::class)
    fun requestSession(uuid: UUID, ip: String, java: Boolean): SessionGranted {
        val request = Request.Builder()
            .url("$baseUrl/sessions")
            .json(RequestSession(uuid, ip, java))
            .build()

        return model(request)
    }

    private fun perform(request: Request) {
        http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                response.body.use { body -> handleError(response.code, body) }
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private inline fun <reified T> model(request: Request): T {
        http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                response.body.use { body -> handleError(response.code, body) }
            }
            return response.body.byteStream().use { stream ->
                json.decodeFromStream<T>(stream)
            }
        }
    }

    private fun handleError(code: Int, body: ResponseBody): Nothing {
        if (body.contentType() != JSON_MEDIA_TYPE) {
            var content = body.string()
            if (content.isBlank()) {
                content = "Received HTTP status code $code"
            }
            throw GatewayException(GatewayErrorCode.Internal, content)
        }

        val error = try {
            json.decodeFromString<GatewayError>(body.string())
        } catch (e: SerializationException) {
            throw GatewayException(GatewayErrorCode.Internal, "Failed to deserialize error response", e)
        }

        throw GatewayException(error)
    }
}

class TokenAuthenticator(val token: String): Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        synchronized(this) {
            return response.request
                .newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
    }
}

private val JSON_MEDIA_TYPE = "application/json".toMediaType()
private val json = Json { ignoreUnknownKeys = true }

private inline fun <reified T> Request.Builder.json(body: T): Request.Builder =
    this.post(json.encodeToString(body).toRequestBody(JSON_MEDIA_TYPE))
