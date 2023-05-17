package com.example.plugins

import com.example.Consts.USER_ID
import com.example.auth.JWTHelper
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

fun Application.configureSecurity(
    jwtHelper: JWTHelper
) {

    install(Authentication) {
        jwt {
            verifier(jwtHelper.verifier)
            validate { credential ->
                credential.payload.getClaim(USER_ID).asInt()?.let { userId ->
                    JWTPrincipal(credential.payload)
                }
            }
            challenge { defaultScheme, realm ->
                call.respond(HttpStatusCode.Unauthorized, "Токен не валидный")
            }
        }
    }
}
