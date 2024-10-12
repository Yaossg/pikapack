package pikapack.core

import pikapack.plan.SyncPlan
import java.nio.file.StandardCopyOption
import kotlin.io.path.copyTo
import kotlin.io.path.createParentDirectories

object CopySyncBehavior: SyncBehavior {
    override fun refresh(plan: SyncPlan) {
        plan.srcFiles().forEach { file ->
            val src = plan.options.src.resolve(file)
            val dst = plan.options.dst.resolve(file)
            dst.createParentDirectories()
            src.copyTo(dst, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES)
        }
    }

    override fun restore(plan: SyncPlan) {
        plan.dstFiles().forEach { file ->
            val src = plan.options.src.resolve(file)
            val dst = plan.options.dst.resolve(file)
            src.createParentDirectories()
            dst.copyTo(src, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES)
        }
    }
}