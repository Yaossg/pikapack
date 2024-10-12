package pikapack.plan

import pikapack.core.CopySyncBehavior
import pikapack.core.PackSyncBehavior
import pikapack.core.SyncBehavior
import pikapack.util.Options
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path

class SyncPlan(val options: Options) {
    private fun files(root: Path) = Files.walk(root).use {
        val fs = FileSystems.getDefault()
        val excludes = fs.getPathMatcher("glob:${options.exclusion}")
        val includes = fs.getPathMatcher("glob:${options.inclusion}")
        it.filter(Files::isRegularFile).map(root::relativize).filter {
            !excludes.matches(it) && includes.matches(it)
        }.toList()
    }

    fun srcFiles() = files(options.src)
    fun dstFiles() = files(options.dst)

    fun behavior(): SyncBehavior = if (options.pack) PackSyncBehavior else CopySyncBehavior
    fun refresh() = behavior().refresh(this)
    fun restore() = behavior().restore(this)
}