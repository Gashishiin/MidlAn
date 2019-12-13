package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom

class User private constructor(
    private val firstName: String,
    private val lastName: String?,
    email: String? = null,
    rawPhone: String? = null,
    meta: Map<String, Any>? = null
) {
    val userInfo: String

    private val fullName: String
        get() = listOfNotNull(firstName, lastName)
            .joinToString(" ")
            .capitalize()

    private val initials: String
        get() = listOfNotNull(firstName, lastName)
            .map { it.first().toUpperCase() }
            .joinToString(" ")

    private var phone: String? = null
        set(value) {
            field = value?.replace("[^+\\d]".toRegex(), "")?.apply {
                if (!"^\\+\\d{11}".toRegex().matches(this))
                    throw IllegalArgumentException("Enter a valid phone number starting with a + and containing 11 digits")
            }
        }
    private var _login: String? = null
    var login: String
        set(value) {
            _login = value.trim().toLowerCase()
        }
    get() = _login!!

    private var salt = ByteArray(16).also { SecureRandom().nextBytes(it) }.toString()

    private lateinit var passwordHash: String

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    var accessCode: String? = null

    constructor(
        firstName: String,
        lastName: String?,
        email: String,
        password: String
    ) :this (firstName, lastName, email = email, meta = mapOf("auth" to "password")) {
        println("Secondary email constructor")
        passwordHash = encrypt(password)
    }

    constructor(
        firstName: String,
        lastName: String?,
        rawPhone: String
    ) :this (firstName, lastName, rawPhone = rawPhone, meta = mapOf("auth" to "sms")) {
        println("Secondary phone constructor")
        val code = generateAccessCode()
        passwordHash = encrypt(code)
        accessCode = code
        sendAccessCodeToUser(phone, code)
    }

    constructor(
        firstName: String,
        lastName: String?,
        email: String?,
        phone: String?,
        salt: String,
        passwordHash: String
    ): this (firstName, lastName, email, phone, mapOf("src" to "csv")) {
        this.passwordHash = passwordHash
        this.salt = salt
    }

    init {
        println("First init block, primary constructor was called")

        check(!firstName.isBlank()) {"FirstName must be not blank" }
        check(!email.isNullOrBlank() || !rawPhone.isNullOrBlank()) {"Email or phone must be not blank" }

        phone = rawPhone
        login = email?.trim() ?: phone!!

        userInfo = """
            firstName: $firstName
            lastName: $lastName
            login: $login
            fullName: $fullName
            initials: $initials
            email: $email
            phone: $phone
            meta: $meta
        """.trimIndent()
    }

    fun checkPassword(pass: String) = encrypt(pass) == passwordHash

    fun changePassword(oldPass: String, newPass: String) {
        if (checkPassword(oldPass)) passwordHash = encrypt(newPass)
        else throw IllegalArgumentException("The entered password does not match the current password")
    }

    fun requestAccessCode() {
        val oldCode = accessCode
        accessCode = generateAccessCode()
        changePassword(oldCode!!, accessCode!!)
    }



    private fun encrypt(password: String): String {
        return salt.plus(password).md5()
    }

    private fun String.md5(): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(toByteArray())
        val hexString = BigInteger(1, digest).toString(16)
        return hexString.padStart(32, '0')
    }

    private fun generateAccessCode(): String {
        val possibles = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return StringBuilder().apply {
            repeat(6) {
                (possibles.indices).random().also { index ->
                    append(possibles[index])
                }
            }
        }.toString()
    }

    private fun sendAccessCodeToUser(phone: String?, code: String) {
        println("..... sending access code: $code on $phone")
    }

    companion object Factory{
        fun makeUser(
            fullName: String,
            email: String? = null,
            password: String? = null,
            phone: String? = null
        ) : User {
            val (firstName, lastName ) = fullName.fullNameToPair()
            return when {
                !phone.isNullOrBlank() -> User(firstName, lastName, phone)
                !email.isNullOrBlank() && !password.isNullOrBlank() -> User(firstName, lastName, email, password)
                else -> throw IllegalArgumentException("Email or phone must be not null or blank")
            }
        }

        fun importFromCsv(
            fullName: String,
            email: String?,
            salt: String,
            passwordHash: String,
            phone: String?
        ): User {
            val(firstName, lastName) = fullName.fullNameToPair()
            return User(firstName, lastName, email, phone, salt, passwordHash)
        }

        private fun String.fullNameToPair(): Pair<String, String?> {
            return this.split(" ")
                .filter { it.isNotBlank() }
                .run {
                    when(size) {
                        1 -> first() to null
                        2 -> first() to last()
                        else -> throw IllegalArgumentException("Fullname must contain only first name and last name, " +
                                "current split result ${this@fullNameToPair}")
                    }
                }
        }
    }
}
