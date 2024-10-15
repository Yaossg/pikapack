package pikapack.core

import pikapack.plan.SyncPlan

import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.util.zip.ZipInputStream
import kotlin.io.path.exists
import kotlin.io.path.outputStream
import kotlin.io.path.inputStream
import kotlin.io.path.name

object PackSyncBehavior: SyncBehavior {
    override fun refresh(plan: SyncPlan) {
        println("dst = ${plan.options.dst}")
        val stream = plan.options.dst.outputStream()
        ZipOutputStream(stream).use { zipOut ->
            plan.srcFiles().forEach { file ->
                val src = plan.options.src.resolve(file).toFile()
                FileInputStream(src).use { fis ->
                    val zipEntry = ZipEntry(file.toString())
                    println("saving ${file.toString()}")
                    println("content: ${fis.available()}")
                    zipOut.putNextEntry(zipEntry)
                    println("copy size: ${fis.copyTo(zipOut)}")
                }
            }
        }
    }

    override fun restore(plan: SyncPlan) {
        val dstDir = plan.options.dst
        val stream = plan.options.src.inputStream()
        println("src = ${plan.options.src}")
        ZipInputStream(stream).use { zipIn ->
            println("come")
            var entry : ZipEntry? = zipIn.nextEntry
            while (entry != null) {
                var filePath = dstDir.resolve(entry.name)
                println(filePath)
                if (!entry.isDirectory) {
                    extractFile(zipIn, filePath.toString())
                } else {
                    val dir = File(filePath.name)
                    if (dir.exists()) {
                        dir.mkdir()
                    }
                }
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
        }
    }

    private fun extractFile(zipIn: ZipInputStream, filePath: String) {
        if (!File(filePath).parentFile.exists())
            File(filePath).parentFile.mkdir()
        FileOutputStream(filePath).use { fos ->
            val buffer = ByteArray(4096)
            var len: Int
            while (zipIn.read(buffer).also { len = it } > 0) {
                fos.write(buffer, 0, len)
            }
        }
    }
}