package com.veosps.game.util.security

interface PasswordEncryption {

    fun encrypt(plainText: String): String

    fun verify(plainText: String, encrypted: String): Boolean
}
