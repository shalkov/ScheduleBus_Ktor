package com.example.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.example.Consts.USER_ID
import com.example.auth.response.TokenResponse
import com.example.db.models.User
import java.util.*

class JWTHelperImpl : JWTHelper {

    private val secret = "bkFwb2xpdGE2OTk5"
    private val issuer = "bkFwb2xpdGE2OTk5"
    private val validityAccessInMs: Long = 1200000L // 20 минут
    //private val validityAccessInMs: Long = 60000L // 1 минута
    private val refreshValidityInMs: Long = 3600000L * 24L * 30L // 30 дней
    //private val refreshValidityInMs: Long = 120000L // 2 минуты
    private val algorithm = Algorithm.HMAC512(secret)

    override val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .build()

    override fun verifyToken(token: String): Int? {
        return verifier.verify(token).claims[USER_ID]?.asInt()
    }

    override fun getTokenExpiration(token: String): Date {
        return verifier.verify(token).expiresAt
    }

    /**
     * Создаёт пару токенов для выбранного пользователя
     */
    override fun createTokens(user: User) = TokensModel(
        createAccessToken(user, createTokenExpirationDate(validityAccessInMs)),
        createRefreshToken(user, createTokenExpirationDate(refreshValidityInMs))
    )

    override fun verifyTokenType(token: String): String {
        return verifier.verify(token).claims["tokenType"]!!.asString()
    }

    override fun isRefreshToken(token: String): Boolean {
        return verifyTokenType(token) == "refreshToken"
    }

    private fun createAccessToken(user: User, expiration: Date) = JWT.create()
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withClaim(USER_ID, user.id)
        .withClaim("tokenType", "accessToken")
        .withClaim("roles", user.roles.map { it.role })
        .withExpiresAt(expiration)
        .sign(algorithm)

    private fun createRefreshToken(user: User, expiration: Date) = JWT.create()
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withClaim(USER_ID, user.id)
        .withClaim("tokenType", "refreshToken")
        .withClaim("roles", user.roles.map { it.role })
        .withExpiresAt(expiration)
        .sign(algorithm)

    /**
     * Сформировать дату "протухания" токена
     */
    private fun createTokenExpirationDate(validity: Long) = Date(System.currentTimeMillis() + validity)
}

interface JWTHelper {
    val verifier: JWTVerifier
    fun createTokens(user: User): TokensModel
    fun verifyTokenType(token: String): String

    fun isRefreshToken(token: String): Boolean
    fun verifyToken(token: String): Int?
    fun getTokenExpiration(token: String): Date
}