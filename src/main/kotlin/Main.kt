package pikapack

import pikapack.gui.GUI
import pikapack.util.Options
import javax.swing.UIManager
import javax.swing.UIManager.setLookAndFeel

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        GUI()
    } else {
        val options = Options.parse(args)

    }
}