package pikapack.plan

import pikapack.core.CopySyncBehavior
import pikapack.core.PackSyncBehavior
import pikapack.core.SyncBehavior
import pikapack.util.Options
import java.nio.file.FileSystems
import java.nio.file.Files

class SyncPlan(val options: Options) {
    fun files() = Files.walk(options.src).use {
        val root = options.src
        val fs = FileSystems.getDefault()
        val excludes = fs.getPathMatcher(options.exclusion)
        val includes = fs.getPathMatcher(options.inclusion)
        it.filter(Files::isRegularFile).map(root::relativize).filter {
            !excludes.matches(it) && includes.matches(it)
        }
    }

    fun behavior(): SyncBehavior = if (options.pack) PackSyncBehavior else CopySyncBehavior
    fun refresh() = behavior().refresh(this)
    fun restore() = behavior().restore(this)
}