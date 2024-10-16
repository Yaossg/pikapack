package pikapack.core

import pikapack.plan.SyncPlan

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

import java.nio.file.Path
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.util.zip.ZipInputStream
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.xml.stream.events.Characters
import kotlin.io.path.createDirectories
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists
import kotlin.io.path.outputStream
import kotlin.io.path.inputStream

object PackSyncBehavior: SyncBehavior {
    private const val ALGORITHM = "AES"
    private const val KEY_SIZE = 256
    private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
    private const val IV_SIZE = 16

    private fun inZipInput(plan: SyncPlan, zipIn : ZipInputStream, srcDir: Path) {
        var entry : ZipEntry? = zipIn.nextEntry
        while (entry != null) {
            var filePath = srcDir.resolve(entry.name)
            if (!entry.isDirectory) {
                extractFile(zipIn, filePath)
            } else {
                if (!filePath.exists()) {
                    filePath.createDirectories()
                }
            }
            zipIn.closeEntry()
            entry = zipIn.nextEntry
        }
    }

    private fun inZipOutput(plan: SyncPlan, zipOut : ZipOutputStream) {
        plan.srcFiles().forEach { file ->
            val src = plan.options.src.resolve(file)
            Files.newInputStream(src).use { fis ->
                val zipEntry = ZipEntry(file.toString())
                zipOut.putNextEntry(zipEntry)
                fis.copyTo(zipOut)
            }
        }
    }

    private fun generateIv(): IvParameterSpec {
        val iv = ByteArray(IV_SIZE)
        SecureRandom().nextBytes(iv)
        return IvParameterSpec(iv)
    }

    private fun generateKey(key: String) : SecretKey {
        val sha = MessageDigest.getInstance("SHA-256")
        val keyBytes = sha.digest(key.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(keyBytes.copyOf(KEY_SIZE / 8), ALGORITHM)
    }

    override fun refresh(plan: SyncPlan) {
        val fileStream = plan.options.dst.outputStream()

        if (plan.options.encrypt) {
            val key = plan.options.encryptionKey
            val iv = generateIv()

            fileStream.use { fileStream ->
                fileStream.write(iv.iv)

                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.ENCRYPT_MODE, generateKey(key), iv)

                CipherOutputStream(fileStream, cipher).use { cipherStream ->
                    ZipOutputStream(cipherStream).use { zipOut ->
                        inZipOutput(plan, zipOut)
                    }
                }
            }
        } else {
            ZipOutputStream(fileStream).use { zipOut ->
                inZipOutput(plan, zipOut)
            }
        }
    }

    override fun restore(plan: SyncPlan) {
        val srcDir = plan.options.src
        val fileStream = plan.options.dst.inputStream()

        if (plan.options.encrypt) {
            val key = plan.options.encryptionKey

            fileStream.use { fileStream ->
                val ivBytes = ByteArray(IV_SIZE)
                fileStream.read(ivBytes)
                val iv = IvParameterSpec(ivBytes)

                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.DECRYPT_MODE, generateKey(key), iv)

                CipherInputStream(fileStream, cipher).use { cipherStream ->
                    ZipInputStream(cipherStream).use { zipIn ->
                        inZipInput(plan, zipIn, srcDir)
                    }
                }
            }
        } else {
            ZipInputStream(fileStream).use { zipIn ->
                inZipInput(plan, zipIn, srcDir)
            }
        }
    }

    private fun extractFile(zipIn: ZipInputStream, filePath: Path) {
        filePath.createParentDirectories()
        Files.newOutputStream(filePath).use { fos ->
            val buffer = ByteArray(4096)
            var len: Int
            while (zipIn.read(buffer).also { len = it } > 0) {
                fos.write(buffer, 0, len)
            }
        }
    }
}