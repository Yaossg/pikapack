package pikapack.gui

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.*
import kotlin.concurrent.thread
import kotlin.system.exitProcess

class FolderSelector(label: String): JPanel() {
    val field = JTextField().apply {
        preferredSize = Dimension(320, 18)
    }
    val button = JButton("Open...")
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
    init {
        layout = BorderLayout()
        add(JLabel(label), BorderLayout.WEST)
        add(field, BorderLayout.CENTER)
    }
}

class OperationSelector: JPanel() {
    val refresh = JRadioButton("Refresh (src -> dst)")
    val restore = JRadioButton("Restore (src <- dst)")
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

class ContentPanel : JPanel() {
    val src = FolderSelector("src: ")
    val dst = FolderSelector("dst: ")
    val operation = OperationSelector()
    val behavior = BehaviorSelector()
    val excludes = FileSelector("excludes: ")
    val includes = FileSelector("includes: ")
    val progress = JProgressBar()
    val button = JButton("pikapack!").apply {
        font = font.deriveFont(18.0F)
        addActionListener {
            progress.value = 0
            thread {
                while (++progress.value < progress.maximum) Thread.sleep(10)
                JOptionPane.showMessageDialog(parent, "backup complete")
            }
        }
    }
    init {
        layout = GridLayout(8, 1)
        add(src)
        add(dst)
        add(operation)
        add(behavior)
        add(excludes)
        add(includes)
        add(progress)
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
        add(JMenu("Sync").apply {
            add(JMenuItem("Watch").apply {
            })
            add(JMenuItem("Schedule")).apply {
                addActionListener {
                    val input = JOptionPane.showInputDialog(parent, "schedule period")
                }
            }
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