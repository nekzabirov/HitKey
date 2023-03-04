package com.hitkey.common.component

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit

@Component
class HitCrypto {
    private val secret: String = "nek_zabirov, nek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirov"

    private val expiredTime: Long = TimeUnit.DAYS.toMillis(100)

    fun encodePassword(password: String): String = password

    fun matches(rawPassword: String, password: String) = rawPassword == password

    fun generateAuthToken(subjectID: Long): String {
        val creationDate = Date()

        return Jwts.builder()
            .setClaims(hashMapOf<String, String>())
            .setSubject(subjectID.toString())
            .setIssuedAt(creationDate)
            .setExpiration(Date(creationDate.time + expiredTime))
            .signWith(Keys.hmacShaKeyFor(secret.toByteArray()))
            .compact()
    }

    fun generateToken(claims: Map<String, Any>): String {
        val creationDate = Date()

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(creationDate)
            .setExpiration(Date(creationDate.time + expiredTime))
            .signWith(Keys.hmacShaKeyFor(secret.toByteArray()))
            .compact()
    }

    fun readClaims(token: String): Claims = Jwts.parserBuilder()
        .setSigningKey(secret.let { Base64.getEncoder().encodeToString(secret.toByteArray()) })
        .build()
        .parseClaimsJws(token)
        .body

    fun readSubjectID(token: String): String = Jwts.parserBuilder()
        .setSigningKey(secret.let { Base64.getEncoder().encodeToString(secret.toByteArray()) })
        .build()
        .parseClaimsJws(token)
        .body
        .subject

    fun validateToken(token: String): Boolean {
        val claims = try {
            readClaims(token)
        } catch (e: Exception) {
            return false
        }

        return !claims.expiration.before(Date())
    }
}