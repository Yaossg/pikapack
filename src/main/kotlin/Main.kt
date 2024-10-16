package pikapack

import pikapack.gui.GUI
import pikapack.plan.AsyncPlan
import pikapack.plan.SyncPlan
import pikapack.util.Options
import java.util.Scanner
import javax.swing.UIManager
import javax.swing.UIManager.setLookAndFeel

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        GUI()
        // main thread quit normally after this function
    } else {
        val options = Options.parse(args)
        if (options == null) {
            println("Pikapack: invalid options")
            return
        }
        if (options.isAsync) {
            val asyncPlan = AsyncPlan(options)
            println("Pikapack: watching or/and scheduling...")
            println("Press enter key to exit")
            val s = Scanner(System.`in`)
            s.nextLine()
            println("Pikapack: shutdown")
            asyncPlan.shutdown()
        } else {
            val syncPlan = SyncPlan(options)
            syncPlan.execute()
            println("Pikapack: sync is done")
        }
    }
}