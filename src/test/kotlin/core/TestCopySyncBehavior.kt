package pikapack.core

import pikapack.plan.SyncPlan
import pikapack.util.Options
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.io.path.appendText
import kotlin.io.path.exists
import kotlin.io.path.readBytes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class TestCopySyncBehavior {
    companion object {
        val resources = Path(".").toAbsolutePath().resolve("src/test/resources")
        fun tempDir() = Files.createTempDirectory("pikapack")
    }

    @Test
    fun testCopyRefresh() {
        val tempDir = tempDir()
        val options = Options(src=resources.resolve("foo"), dst=tempDir)
        val plan = SyncPlan(options)
        plan.refresh()
        val src = options.src
        val dst = options.dst
        assertTrue(dst.exists())
        assertTrue(dst.resolve("a.txt").exists())
        assertTrue(dst.resolve("b.md").exists())
        assertTrue(dst.resolve("bar").exists())
        assertTrue(dst.resolve("bar/c.c").exists())
        assertEquals(src.resolve("a.txt").readBytes().toList(), dst.resolve("a.txt").readBytes().toList())
        assertEquals(src.resolve("b.md").readBytes().toList(), dst.resolve("b.md").readBytes().toList())
        assertEquals(src.resolve("bar/c.c").readBytes().toList(), dst.resolve("bar/c.c").readBytes().toList())

        assertTrue(plan.check())
        tempDir.resolve("a.txt").appendText("hi")
        assertFalse(plan.check())
    }

    @Test
    fun testCopyRestore() {
        val tempDir = tempDir()
        val options = Options(src=tempDir, dst=resources.resolve("foo"))
        val plan = SyncPlan(options)
        plan.restore()
        val src = options.src
        val dst = options.dst
        assertTrue(src.exists())
        assertTrue(src.resolve("a.txt").exists())
        assertTrue(src.resolve("b.md").exists())
        assertTrue(src.resolve("bar").exists())
        assertTrue(src.resolve("bar/c.c").exists())
        assertEquals(src.resolve("a.txt").readBytes().toList(), dst.resolve("a.txt").readBytes().toList())
        assertEquals(src.resolve("b.md").readBytes().toList(), dst.resolve("b.md").readBytes().toList())
        assertEquals(src.resolve("bar/c.c").readBytes().toList(), dst.resolve("bar/c.c").readBytes().toList())

        assertTrue(plan.check())
        tempDir.resolve("bar/c.c").appendText("hi")
        assertFalse(plan.check())
    }
}