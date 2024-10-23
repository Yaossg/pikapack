package pikapack.core

import pikapack.plan.SyncPlan

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.util.zip.ZipInputStream
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import kotlin.io.path.createDirectories
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.outputStream
import kotlin.io.path.inputStream
import kotlin.io.path.setLastModifiedTime

import pikapack.util.transferAllBytes
import java.util.zip.CRC32

object PackSyncBehavior: SyncBehavior {
    private const val ALGORITHM = "AES"
    private const val KEY_SIZE = 256
    private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
    private const val IV_SIZE = 16

    private fun inZipInput(plan: SyncPlan, zipIn : ZipInputStream) {
        val srcDir = plan.options.src
        var entry : ZipEntry? = zipIn.nextEntry
        while (entry != null) {
            var filePath = srcDir.resolve(entry.name)
            if (!entry.isDirectory) {
                filePath.createParentDirectories()
                Files.newOutputStream(filePath).use { fos ->
                    zipIn.transferAllBytes(fos::write)
                }
                filePath.setLastModifiedTime(entry.lastModifiedTime)
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
                if (plan.options.compress) {
                    zipEntry.method = ZipEntry.DEFLATED
                } else {
                    zipEntry.method = ZipEntry.STORED
                    zipEntry.size = src.fileSize()
                    val crc32 = CRC32()
                    src.inputStream().use {
                        it.transferAllBytes(crc32::update)
                    }
                    zipEntry.crc = crc32.value
                }
                zipEntry.lastModifiedTime = src.getLastModifiedTime()
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
                        inZipInput(plan, zipIn)
                    }
                }
            }
        } else {
            ZipInputStream(fileStream).use { zipIn ->
                inZipInput(plan, zipIn)
            }
        }
    }

    private fun checkZipInput(plan: SyncPlan, zipIn : ZipInputStream) : Boolean {
        val srcDir = plan.options.src
        var entry : ZipEntry? = zipIn.nextEntry
        while (entry != null) {
            var filePath = srcDir.resolve(entry.name)
            val crcSrc = CRC32()
            val crcDst = CRC32()

            if (!entry.isDirectory) {
                zipIn.transferAllBytes(crcDst::update)
                Files.newInputStream(filePath).use {
                    it.transferAllBytes(crcSrc::update)
                }
            }

            // println("file: ${filePath}, src=${crcSrc.value} dst=${crcDst.value}")

            if (crcSrc.value != crcDst.value)
                return false

            zipIn.closeEntry()
            entry = zipIn.nextEntry
        }

        return true
    }

    override fun check(plan : SyncPlan) : Boolean {
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
                        return checkZipInput(plan, zipIn)
                    }
                }
            }
        }

        ZipInputStream(fileStream).use { zipIn ->
            return checkZipInput(plan, zipIn)
        }
    }
}