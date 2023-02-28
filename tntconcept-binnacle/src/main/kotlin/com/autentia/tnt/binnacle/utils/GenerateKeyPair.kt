package com.autentia.tnt.binnacle.utils

import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.SecureRandom
import java.util.Base64

fun main() {
    try {
        val keyGen = KeyPairGenerator.getInstance("RSA")

        // Initialize KeyPairGenerator.
        val random: SecureRandom = SecureRandom.getInstance("SHA1PRNG", "SUN")
        keyGen.initialize(1024, random)

        // Generate Key Pairs, a private key and a public key.
        val keyPair = keyGen.generateKeyPair()
        val privateKey = keyPair.private
        val publicKey = keyPair.public
        val encoder: Base64.Encoder = Base64.getEncoder()
        println("privateKey: " + encoder.encodeToString(privateKey.encoded))
        println("publicKey: " + encoder.encodeToString(publicKey.encoded))
    } catch (e: NoSuchAlgorithmException) {
        println("Error generating key$e")
    } catch (e: NoSuchProviderException) {
        println("Error generating key$e")
    }
}
