package pikapack.plan

import pikapack.util.Options
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class TestSyncPlan {
    companion object {
        val resources = Path(".").toAbsolutePath().resolve("src/test/resources")
        fun tempDir() = Files.createTempDirectory("pikapack")
    }

    @Test
    fun testFiles() {
        val options = Options(src=resources.resolve("foo"), dst=tempDir())
        val plan = SyncPlan(options)
        assertEquals(listOf(Path("a.txt"), Path("b.md"), Path("bar/c.c")), plan.srcFiles())
    }

    @Test
    fun testFilesExclude() {
        val options = Options(src=resources.resolve("foo"), dst=tempDir(),
            exclusion = "**/*.c")
        val plan = SyncPlan(options)
        assertEquals(listOf(Path("a.txt"), Path("b.md")), plan.srcFiles())
    }

    @Test
    fun testFilesInclude() {
        val options = Options(src=resources.resolve("foo"), dst=tempDir(),
            inclusion = "**/*.c")
        val plan = SyncPlan(options)
        assertEquals(listOf(Path("bar/c.c")), plan.srcFiles())
    }

    @Test
    fun testFilesExcludeInclude() {
        val options = Options(src=resources.resolve("foo"), dst=tempDir(),
            exclusion = "**/*.c", inclusion = "*.txt")
        val plan = SyncPlan(options)
        assertEquals(listOf(Path("a.txt")), plan.srcFiles())
    }
}