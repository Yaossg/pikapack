package pikapack.core

import pikapack.plan.SyncPlan

import java.nio.file.Path
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.util.zip.ZipInputStream
import kotlin.io.path.createDirectories
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists
import kotlin.io.path.outputStream
import kotlin.io.path.inputStream

object PackSyncBehavior: SyncBehavior {
    override fun refresh(plan: SyncPlan) {
        val stream = plan.options.dst.outputStream()
        ZipOutputStream(stream).use { zipOut ->
            plan.srcFiles().forEach { file ->
                val src = plan.options.src.resolve(file)
                Files.newInputStream(src).use { fis ->
                    val zipEntry = ZipEntry(file.toString())
                    zipOut.putNextEntry(zipEntry)
                    fis.copyTo(zipOut)
                }
            }
        }
    }

    override fun restore(plan: SyncPlan) {
        val srcDir = plan.options.src
        val stream = plan.options.dst.inputStream()
        ZipInputStream(stream).use { zipIn ->
            var entry : ZipEntry? = zipIn.nextEntry
            while (entry != null) {
                var filePath = srcDir.resolve(entry.name)
                if (!entry.isDirectory) {
                    extractFile(zipIn, filePath)
                } else {
                    if (!filePath.exists()) {
                        filePath.createDirectories()
                    }
                }
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
        }
    }

    private fun extractFile(zipIn: ZipInputStream, filePath: Path) {
        filePath.createParentDirectories()
        Files.newOutputStream(filePath).use { fos ->
            val buffer = ByteArray(4096)
            var len: Int
            while (zipIn.read(buffer).also { len = it } > 0) {
                fos.write(buffer, 0, len)
            }
        }
    }
}