package pikapack.util

import java.nio.file.Files
import java.nio.file.Paths
import java.security.SecureRandom
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class FileEncryptor {
    companion object {
        private const val ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
        private const val KEY_SIZE = 256
        private const val IV_SIZE = 16
    }


    private fun generateKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance(ALGORITHM)
        keyGen.init(KEY_SIZE)
        return keyGen.generateKey()
    }

    private fun generateIv(): IvParameterSpec {
        val iv = ByteArray(IV_SIZE)
        SecureRandom().nextBytes(iv)
        return IvParameterSpec(iv)
    }

    fun encryptFile(
        inputFile: String,
        outputFile: String,
    ) {
        val key = generateKey()
        val iv = generateIv()

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key, iv)

        val inputPath = Paths.get(inputFile)
        val outputPath = Paths.get(outputFile)

        Files.newOutputStream(outputPath).use { outputStream ->
            outputStream.write(key.encoded)
            outputStream.write(iv.iv)

            CipherOutputStream(outputStream, cipher).use { cipherOut ->
                Files.newInputStream(inputPath).use { inputStream ->
                    inputStream.copyTo(cipherOut)
                }
            }
        }
    }

    fun decryptFile(
        inputFile: String,
        outputFile: String
    ) {
        val inputPath = Paths.get(inputFile)
        val outputPath = Paths.get(outputFile)

        Files.newInputStream(inputPath).use { inputStream ->
            val keyBytes = ByteArray(KEY_SIZE / 8)
            inputStream.read(keyBytes)
            val key = SecretKeySpec(keyBytes, ALGORITHM)

            val ivBytes = ByteArray(IV_SIZE)
            inputStream.read(ivBytes)
            val iv = IvParameterSpec(ivBytes)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key, iv)

            CipherInputStream(inputStream, cipher).use { cipherIn ->
                Files.newOutputStream(outputPath).use { outputStream ->
                    cipherIn.copyTo(outputStream)
                }
            }
        }
    }
}