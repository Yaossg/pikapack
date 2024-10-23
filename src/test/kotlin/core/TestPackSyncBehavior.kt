package pikapack.core

import org.junit.jupiter.api.Test
import pikapack.plan.SyncPlan
import pikapack.util.Options
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.appendText
import kotlin.io.path.exists
import kotlin.io.path.readBytes
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestPackSyncBehavior {
    companion object {
        val resources = Path(".").toAbsolutePath().resolve("src/test/resources")
        fun tempZip() = Files.createTempFile("pikapack", "zip")
        fun tempDir() = Files.createTempDirectory("pikapack_restore")
    }


    @Test
    fun testPack() {
        val tempDir = tempDir()
        val tempZip = tempZip()
        val originSrc = resources.resolve("foo")

        val options1 = Options(src= originSrc, dst= tempZip, pack=true)
        val options2 = Options(src= tempDir, dst= tempZip, pack=true)
        val plan1 = SyncPlan(options1)
        val plan2 = SyncPlan(options2)

        plan1.refresh()
        assertTrue(tempZip.exists())

        plan2.restore()
        assertTrue(originSrc.resolve("a.txt").exists())
        assertTrue(originSrc.resolve("b.md").exists())
        assertTrue(originSrc.resolve("bar").exists())
        assertTrue(originSrc.resolve("bar/c.c").exists())
        assertTrue(tempDir.resolve("a.txt").exists())
        assertTrue(tempDir.resolve("b.md").exists())
        assertTrue(tempDir.resolve("bar").exists())
        assertTrue(tempDir.resolve("bar/c.c").exists())
        assertEquals(originSrc.resolve("a.txt").readBytes().toList(), tempDir.resolve("a.txt").readBytes().toList())
        assertEquals(originSrc.resolve("b.md").readBytes().toList(), tempDir.resolve("b.md").readBytes().toList())
        assertEquals(originSrc.resolve("bar/c.c").readBytes().toList(), tempDir.resolve("bar/c.c").readBytes().toList())

        assertTrue(plan2.check())
        tempDir.resolve("a.txt").appendText("hi")
        assertFalse(plan2.check())
    }

    @Test
    fun testPackCompressed() {
        val tempDir = tempDir()
        val tempZip = tempZip()
        val originSrc = resources.resolve("foo")

        val options1 = Options(src= originSrc, dst= tempZip, pack=true, compress=true)
        val options2 = Options(src= tempDir, dst= tempZip, pack=true, compress=true)
        val plan1 = SyncPlan(options1)
        val plan2 = SyncPlan(options2)

        plan1.refresh()
        assertTrue(tempZip.exists())

        plan2.restore()
        assertTrue(originSrc.resolve("a.txt").exists())
        assertTrue(originSrc.resolve("b.md").exists())
        assertTrue(originSrc.resolve("bar").exists())
        assertTrue(originSrc.resolve("bar/c.c").exists())
        assertTrue(tempDir.resolve("a.txt").exists())
        assertTrue(tempDir.resolve("b.md").exists())
        assertTrue(tempDir.resolve("bar").exists())
        assertTrue(tempDir.resolve("bar/c.c").exists())
        assertEquals(originSrc.resolve("a.txt").readBytes().toList(), tempDir.resolve("a.txt").readBytes().toList())
        assertEquals(originSrc.resolve("b.md").readBytes().toList(), tempDir.resolve("b.md").readBytes().toList())
        assertEquals(originSrc.resolve("bar/c.c").readBytes().toList(), tempDir.resolve("bar/c.c").readBytes().toList())

        assertTrue(plan2.check())
        tempDir.resolve("a.txt").appendText("hi")
        assertFalse(plan2.check())
    }

    @Test
    fun testPackEncrypted() {
        val tempDir = tempDir()
        val tempZip = tempZip()
        val originSrc = resources.resolve("foo")

        val options1 = Options(src= originSrc, dst= tempZip, pack=true, encrypt=true, encryptionKey = "123")
        val options2 = Options(src= tempDir, dst= tempZip, pack=true, encrypt=true, encryptionKey = "123")
        val plan1 = SyncPlan(options1)
        val plan2 = SyncPlan(options2)

        plan1.refresh()
        assertTrue(tempZip.exists())

        plan2.restore()
        assertTrue(originSrc.resolve("a.txt").exists())
        assertTrue(originSrc.resolve("b.md").exists())
        assertTrue(originSrc.resolve("bar").exists())
        assertTrue(originSrc.resolve("bar/c.c").exists())
        assertTrue(tempDir.resolve("a.txt").exists())
        assertTrue(tempDir.resolve("b.md").exists())
        assertTrue(tempDir.resolve("bar").exists())
        assertTrue(tempDir.resolve("bar/c.c").exists())
        assertEquals(originSrc.resolve("a.txt").readBytes().toList(), tempDir.resolve("a.txt").readBytes().toList())
        assertEquals(originSrc.resolve("b.md").readBytes().toList(), tempDir.resolve("b.md").readBytes().toList())
        assertEquals(originSrc.resolve("bar/c.c").readBytes().toList(), tempDir.resolve("bar/c.c").readBytes().toList())

        assertTrue(plan2.check())
        tempDir.resolve("b.md").appendText("hi")
        assertFalse(plan2.check())
    }


    @Test
    fun testPackCompressedEncrypted() {
        val tempDir = tempDir()
        val tempZip = tempZip()
        val originSrc = resources.resolve("foo")

        val options1 = Options(src= originSrc, dst= tempZip, pack=true, compress = true, encrypt=true, encryptionKey = "123")
        val options2 = Options(src= tempDir, dst= tempZip, pack=true, compress = true, encrypt=true, encryptionKey = "123")
        val plan1 = SyncPlan(options1)
        val plan2 = SyncPlan(options2)

        plan1.refresh()
        assertTrue(tempZip.exists())

        plan2.restore()
        assertTrue(originSrc.resolve("a.txt").exists())
        assertTrue(originSrc.resolve("b.md").exists())
        assertTrue(originSrc.resolve("bar").exists())
        assertTrue(originSrc.resolve("bar/c.c").exists())
        assertTrue(tempDir.resolve("a.txt").exists())
        assertTrue(tempDir.resolve("b.md").exists())
        assertTrue(tempDir.resolve("bar").exists())
        assertTrue(tempDir.resolve("bar/c.c").exists())
        assertEquals(originSrc.resolve("a.txt").readBytes().toList(), tempDir.resolve("a.txt").readBytes().toList())
        assertEquals(originSrc.resolve("b.md").readBytes().toList(), tempDir.resolve("b.md").readBytes().toList())
        assertEquals(originSrc.resolve("bar/c.c").readBytes().toList(), tempDir.resolve("bar/c.c").readBytes().toList())

        assertTrue(plan2.check())
        tempDir.resolve("bar/c.c").appendText("hi")
        assertFalse(plan2.check())
    }
}