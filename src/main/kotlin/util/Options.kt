package pikapack.util

import java.nio.file.Path
import kotlin.io.path.Path

data class Options(val src: Path, val dst: Path,
                   val operation: Operation = Operation.REFRESH,
                   val pack: Boolean = false, val compress: Boolean = false, val encrypt: Boolean = false,
                   val watch: Boolean = false, val schedule: Int = -1,
                   val exclusion: String = "", val inclusion: String = "**",
                   val encryptionKey: String = "key") {
    enum class Operation {
        REFRESH, RESTORE
    }

    val isAsync get() = watch || schedule > 0

    companion object {
        fun parse(args: Array<String>): Options? = runCatching {
            if (args.isEmpty()) return null
            var src: Path? = null
            var dst: Path? = null
            var operation: Operation = Operation.REFRESH
            var pack: Boolean = false
            var compress: Boolean = false
            var encrypt: Boolean = false
            var watch: Boolean = false
            var schedule: Int = -1
            var exclusion: String = ""
            var inclusion: String = "**"
            var encryptionKey: String = "key"
            var i = 0
            while (i < args.size) {
                val option = args[i]
                when (option) {
                    "-src" -> src = Path(args[++i])
                    "-dst" -> dst = Path(args[++i])
                    "--refresh" -> operation = Operation.REFRESH
                    "--restore" -> operation = Operation.RESTORE
                    "--copy" -> pack = false
                    "--pack" -> pack = true
                    "--compress" -> compress = true
                    "--encrypt" -> encrypt = true
                    "--watch" -> watch = true
                    "-sched" -> schedule = args[++i].toInt()
                    "-excl" -> exclusion = args[++i]
                    "-incl" -> inclusion = args[++i]
                    "-key" -> encryptionKey = args[++i]
                    else -> return null
                }
                ++i
            }
            if (!pack && (compress || encrypt)) {
                return null
            }
            return Options(src!!, dst!!, operation, pack, compress, encrypt, watch, schedule, exclusion, inclusion, encryptionKey)
        }.getOrNull()
    }
}