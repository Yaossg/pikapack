import java.io.InputStream
import java.util.zip.CRC32

// usage:
//   in.transferAllBytes(out::write)
//   in.transferAllBytes(crc32::update)
fun InputStream.transferAllBytes(action: (buf: ByteArray, off: Int, len: Int) -> Unit) {
    val buffer = ByteArray(4096)
    var len: Int
    while (read(buffer).also { len = it } > 0) {
        action(buffer, 0, len)
    }
}

fun checksum(streams: List<InputStream>): Long {
    val crc32 = CRC32()
    streams.forEach {
        it.use {
            it.transferAllBytes(crc32::update)
        }
    }
    return crc32.value
}