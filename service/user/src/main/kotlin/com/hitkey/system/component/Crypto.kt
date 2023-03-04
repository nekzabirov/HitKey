package com.hitkey.system.component

import com.hitkey.system.database.entity.user.UserEntity
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.util.Base64
import java.util.Date
import java.util.concurrent.TimeUnit

@Component
class HitCrypto {
    private val secret: String = "nek_zabirov, nek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirovnek_zabirov"

    private val expiredTime: Long = TimeUnit.DAYS.toMillis(100)

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    fun encodePassword(password: String): String = passwordEncoder.encode(password)

    fun matches(rawPassword: String, password: String) = passwordEncoder.matches(rawPassword, password)

    fun generateAuthToken(user: UserEntity): String {
        val creationDate = Date()

        return Jwts.builder()
            .setClaims(hashMapOf("role" to "USER"))
            .setSubject(user.id.toString())
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

    fun readUserID(token: String): String = Jwts.parserBuilder()
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