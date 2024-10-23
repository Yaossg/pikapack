package pikapack.core

import pikapack.plan.SyncPlan
import pikapack.util.transferAllBytes
import java.nio.file.StandardCopyOption
import kotlin.io.path.copyTo
import kotlin.io.path.createParentDirectories

import java.nio.file.Files
import java.util.zip.CRC32

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

    override fun check(plan : SyncPlan) : Boolean {
        plan.srcFiles().forEach { file ->
            val src = plan.options.src.resolve(file)
            val dst = plan.options.dst.resolve(file)
            val crcSrc = CRC32()
            val crcDst = CRC32()
            Files.newInputStream(src).use {
                it.transferAllBytes(crcSrc::update)
            }
            Files.newInputStream(dst).use {
                it.transferAllBytes(crcDst::update)
            }
            if (crcSrc.value != crcDst.value)
                return false
        }
        return true
    }
}