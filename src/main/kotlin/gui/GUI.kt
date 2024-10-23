package pikapack.gui

import pikapack.plan.AsyncPlan
import pikapack.plan.SyncPlan
import pikapack.util.Options
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.*
import kotlin.io.path.Path
import kotlin.system.exitProcess

class FolderSelector(label: String): JPanel() {
    val field = JTextField().apply {
        preferredSize = Dimension(320, 18)
    }
    val button = JButton("Open...")

    val path get() = Path(field.text)

    init {
        layout = BorderLayout()
        add(JLabel(label), BorderLayout.WEST)
        add(field, BorderLayout.CENTER)
        add(button, BorderLayout.EAST)
        button.addActionListener {
            val chooser = JFileChooser()
            chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            val returnVal = chooser.showOpenDialog(parent)
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                field.text = chooser.selectedFile.absolutePath
            }
        }
    }
}


class FileSelector(label: String): JPanel() {
    val field = JTextField().apply {
        preferredSize = Dimension(320, 18)
    }

    var text get() = field.text
        set(value) {
            this.field.text = value
        }

    init {
        layout = BorderLayout()
        add(JLabel(label), BorderLayout.WEST)
        add(field, BorderLayout.CENTER)
    }
}

class OperationSelector: JPanel() {
    val refresh = JRadioButton("Refresh (src -> dst)")
    val restore = JRadioButton("Restore (src <- dst)")

    val operation get() =
        if (refresh.isSelected) Options.Operation.REFRESH else Options.Operation.RESTORE

    init {
        val group = ButtonGroup()
        group.add(refresh)
        group.add(restore)
        add(refresh)
        add(restore)
        refresh.isSelected = true
    }
}

class BehaviorSelector: JPanel() {
    val copy = JRadioButton("Copy")
    val pack = JRadioButton("Pack")
    val compress = JCheckBox("Compress")
    val encrypt = JCheckBox("Encrypt")
    val key = JPasswordField()

    @Suppress("DEPRECATION")
    val encryptionKey get() = key.text

    fun updateEnable() {
        compress.isEnabled = pack.isSelected
        encrypt.isEnabled = pack.isSelected
        if (!compress.isEnabled) compress.isSelected = false
        if (!encrypt.isEnabled) encrypt.isSelected = false
        key.isEnabled = encrypt.isSelected
    }

    init {
        val group = ButtonGroup()
        group.add(copy)
        group.add(pack)
        copy.addActionListener { updateEnable() }
        pack.addActionListener { updateEnable() }
        encrypt.addActionListener { updateEnable() }
        add(copy)
        add(pack)
        add(compress)
        add(encrypt)
        add(JLabel("Encryption Key: "))
        add(key)
        key.preferredSize = Dimension(240, 24)
        copy.isSelected = true
        updateEnable()
    }
}

class Watchers: JPanel() {
    val watch = JCheckBox("Watch")
    val scheduled = JCheckBox("Schedule")
    val schedule = JTextField()

    val scheduling get() = if (scheduled.isSelected) scheduled.text.toIntOrNull() ?: -1 else -1

    fun updateEnable() {
        schedule.isEnabled = scheduled.isSelected
    }

    init {
        add(watch)
        add(scheduled)
        add(schedule)
        add(JLabel("minutes per sync"))
        schedule.preferredSize = Dimension(240, 24)
        scheduled.addActionListener { updateEnable() }
        updateEnable()
    }
}

class ContentPanel(frame: JFrame) : JPanel() {
    val src = FolderSelector("src: ")
    val dst = FolderSelector("dst: ")
    val operation = OperationSelector()
    val behavior = BehaviorSelector()
    val excludes = FileSelector("excludes: ")
    val includes = FileSelector("includes: ")
    val watchers = Watchers()
    val button = JButton("pikapack!").apply {
        font = font.deriveFont(16.0F)
        addActionListener {
            val options = Options(
                src = src.path,
                dst = dst.path,
                operation = operation.operation,
                pack = behavior.pack.isSelected,
                compress = behavior.compress.isSelected,
                encrypt = behavior.encrypt.isSelected,
                watch = watchers.watch.isSelected,
                schedule = watchers.scheduling,
                inclusion = includes.text,
                exclusion = excludes.text,
                encryptionKey = behavior.encryptionKey
            )
            fun showDialog(OK: String, message: String) {
                val selections = arrayOf(OK)
                JOptionPane.showOptionDialog(frame, message, "Pikapack",
                    JOptionPane.OK_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, selections, selections[0])
            }
            if (options.isAsync) {
                val plan = AsyncPlan(options)
                showDialog("Stop", "Watching or/and Scheduling...")
                plan.shutdown()
            } else {
                val plan = SyncPlan(options)
                plan.execute()
                showDialog("OK", "Sync is done")
            }

        }
    }
    init {
        layout = GridLayout(8, 1).apply {
            vgap = 1
        }
        add(src)
        add(dst)
        add(operation)
        add(behavior)
        add(excludes)
        includes.text = "**"
        add(includes)
        add(watchers)
        add(button)
    }
}

class MenuBar: JMenuBar() {
    init {
        add(JMenu("File").apply {
            add(JMenuItem("Exit").apply {
                addActionListener {
                    exitProcess(0)
                }
            })
        })
        add(JMenu("Help").apply {
            add(JMenuItem("About").apply {
                addActionListener {
                    JOptionPane.showMessageDialog(parent, "pikapack, by Yaossg, XuKaFy and ZbWeR")
                }
            })
        })
    }
}

class GUI: JFrame("pikapack") {
    init {
        jMenuBar = MenuBar()
        contentPane = ContentPanel(this)
        defaultCloseOperation = EXIT_ON_CLOSE

        val url = GUI::class.java.getResource("/icon.png")
        iconImage = ImageIcon(url).image

        pack()
        isVisible = true
    }
}