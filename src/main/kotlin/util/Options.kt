package pikapack.util

import java.nio.file.Path
import kotlin.io.path.Path

data class Options(val src: Path, val dst: Path,
                   val operation: Operation = Operation.REFRESH,
                   val behavior: Behavior = Behavior.DEFAULT,
                   val watch: Boolean = false, val schedule: Int = -1,
                   val exclusion: String = "",
                   val inclusion: String = "**") {
    enum class Operation {
        REFRESH, RESTORE
    }
    enum class Behavior {
        DEFAULT, COMPRESS, ENCRYPT
    }

    companion object {
        fun parse(args: Array<String>): Options? = runCatching {
            if (args.isEmpty()) return null
            var src: Path? = null
            var dst: Path? = null
            var operation: Operation = Operation.REFRESH
            var behavior: Behavior = Behavior.DEFAULT
            var watch: Boolean = false
            var schedule: Int = -1
            var exclusion: String = ""
            var inclusion: String = "**"
            var i = 0
            while (i < args.size) {
                val option = args[i]
                when (option) {
                    "-src" -> src = Path(args[++i])
                    "-dst" -> dst = Path(args[++i])
                    "--refresh" -> operation = Operation.REFRESH
                    "--restore" -> operation = Operation.RESTORE
                    "--compress" -> behavior = Behavior.COMPRESS
                    "--encrypt" -> behavior = Behavior.ENCRYPT
                    "--watch" -> watch = true
                    "-sched" -> schedule = args[++i].toInt()
                    "-excl" -> exclusion = args[++i]
                    "-incl" -> inclusion = args[++i]
                    else -> return null
                }
                ++i
            }
            return Options(src!!, dst!!, operation, behavior, watch, schedule, exclusion, inclusion)
        }.getOrNull()
    }
}