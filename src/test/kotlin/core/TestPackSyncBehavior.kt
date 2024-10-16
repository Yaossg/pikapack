package pikapack.core

import org.junit.jupiter.api.Test
import pikapack.plan.SyncPlan
import pikapack.util.Options
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readBytes
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestPackSyncBehavior {
    companion object {
        val resources = Path(".").toAbsolutePath().resolve("src/test/resources")
        fun tempZip() = Files.createTempFile("pikapack", "zip")
        fun tempDir() = Files.createTempDirectory("pikapack_restore")
    }

    @Test
    fun testPackRefreshAndRestore() {
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
    }
}