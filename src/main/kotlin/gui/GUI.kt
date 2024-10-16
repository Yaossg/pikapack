package pikapack.gui

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

    val text get() = field.text

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

    fun updateEnable() {
        compress.isEnabled = pack.isSelected
        encrypt.isEnabled = pack.isSelected
        if (!compress.isEnabled) compress.isSelected = false
        if (!encrypt.isEnabled) encrypt.isSelected = false
    }

    init {
        val group = ButtonGroup()
        group.add(copy)
        group.add(pack)
        copy.addActionListener { updateEnable() }
        pack.addActionListener { updateEnable() }
        add(copy)
        add(pack)
        add(compress)
        add(encrypt)
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
        add(JLabel("seconds per sync"))
        schedule.preferredSize = Dimension(320, 24)
        scheduled.addActionListener { updateEnable() }
        updateEnable()
    }
}

class ContentPanel : JPanel() {
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
            )
            JOptionPane.showMessageDialog(parent, options.toString())
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
        contentPane = ContentPanel()
        defaultCloseOperation = EXIT_ON_CLOSE

        val url = GUI::class.java.getResource("/icon.png")
        iconImage = ImageIcon(url).image

        pack()
        isVisible = true
    }
}