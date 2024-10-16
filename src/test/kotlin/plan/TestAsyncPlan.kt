package pikapack.plan

import pikapack.util.Options
import java.nio.file.Files
import kotlin.io.path.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestAsyncPlan {
    companion object {
        val resources = Path(".").toAbsolutePath().resolve("src/test/resources")
        fun tempDir() = Files.createTempDirectory("pikapack")
    }

    @Test
    fun testSchedule() {
        val originSrc = resources.resolve("foo")
        val tempDir = tempDir()

        val options = Options(src = originSrc, dst = tempDir, schedule = 1)
        val asyncPlan = AsyncPlan(options)

        // 非立即执行, 最初时并未进行备份
        assertTrue(tempDir.resolve("a.txt").notExists())

        // 1min 之后检查是否备份成功
        Thread.sleep(65_000)
        assertTrue(tempDir.resolve("a.txt").exists())
        assertEquals(
            originSrc.resolve("a.txt").readBytes().toList(),
            tempDir.resolve("a.txt").readBytes().toList()
        )
        asyncPlan.shutdown()
    }

    @Test
    fun testWatch() {
        val originSrc = resources.resolve("foo")
        val tempDir = tempDir()

        val options = Options(src = originSrc, dst = tempDir, watch = true)
        val asyncPlan = AsyncPlan(options)

        assertTrue(tempDir.resolve("a.txt").notExists())

        originSrc.resolve("a.txt").writeText("喵喵喵")
        Thread.sleep(10_000)
        assertTrue(tempDir.resolve("a.txt").exists())
        assertEquals(
            originSrc.resolve("a.txt").readBytes().toList(),
            tempDir.resolve("a.txt").readBytes().toList()
        )
        asyncPlan.shutdown()
    }
}