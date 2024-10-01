package pikapack.util

import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class TestOptions {
    @Test
    fun testParseSuccess() {
        val src = "/home/folder1"
        val dst = "/home/folder2"
        assertEquals(
            Options(Path(src), Path(dst), watch = true),
            Options.parse(arrayOf("-src", src, "-dst", dst, "--watch"))
        )
        assertEquals(
            Options(Path(src), Path(dst), schedule = 25),
            Options.parse(arrayOf("-src", src, "-dst", dst, "-sched", "25"))
        )
    }

    @Test
    fun testParseFailure() {
        val src = "/home/folder1"
        val dst = "/home/folder2"
        assertEquals(null, Options.parse(arrayOf("-src", src)))
        assertEquals(null, Options.parse(arrayOf("-src", src, "-dst", dst, "--sausage")))
    }
}