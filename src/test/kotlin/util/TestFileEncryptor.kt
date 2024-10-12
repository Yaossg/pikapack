package pikapack.util

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.readBytes
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TestFileEncryptor {

    companion object {
        private val resources: Path = Path(".").toAbsolutePath().resolve("src/test/resources")
        private val baseFile: Path = resources.resolve("foo/a.txt")
        private val encryptedFile: Path = resources.resolve("foo/a.encrypt.txt")
        private val decryptedFile: Path = resources.resolve("foo/a.decrypt.txt")
    }

    @BeforeEach
    fun setup() {
        // Ensure the test environment is clean before each test
        encryptedFile.deleteIfExists()
        decryptedFile.deleteIfExists()
    }

    @AfterEach
    fun tearDown() {
        // Clean up the temporary files after each test
        encryptedFile.deleteIfExists()
        decryptedFile.deleteIfExists()
    }

    @Test
    fun testEncrypt() {
        FileEncryptor.encryptFile(baseFile, encryptedFile)

        val inputContent = baseFile.readBytes()
        val outputContent = encryptedFile.readBytes()

        assertTrue(outputContent.isNotEmpty(), "The output file should not be empty")
        assertNotEquals(inputContent.toList(), outputContent.toList(), "The encrypted content should differ from the input content")
    }

    @Test
    fun testDecrypt() {
        // Ensure the encrypted file exists before decrypting
        if (!encryptedFile.exists()) {
            FileEncryptor.encryptFile(baseFile, encryptedFile)
        }

        FileEncryptor.decryptFile(encryptedFile, decryptedFile)

        val outputContent = decryptedFile.readBytes()
        val baseContent = baseFile.readBytes()

        assertTrue(outputContent.isNotEmpty(), "The decrypted file should not be empty")
        assertEquals(baseContent.toList(), outputContent.toList(), "The decrypted content should match the original content")
    }
}