package sg.ui

import java.awt.GridBagConstraints._
import java.awt._
import java.awt.event._
import java.io._
import javax.swing._
import javax.swing.event._

import sg.ui.UIUtil._

class MainFrame extends JFrame {
  val iconHostImage = new JLabel
  val iconModifiedImage = new JLabel
  val inputUsedBits = new JSlider(new DefaultBoundedRangeModel(1, 0, 1, 8))
  val inputPassphrase = new JTextField
  val txtGuestDataStatus = new JTextField
  val txtImageCapacity = new JTextField
  val txtImageModification = new JTextField
  val jfc = new JFileChooser

  val cmdLoadHostImage = new JButton("Load Image")
  val cmdLoadGuestData = new JButton("Load")
  val cmdSaveGuestData = new JButton("Save")
  val cmdEmbedData = new JButton("Hide Data")
  val cmdLoadModifiedImage = new JButton("Load Image")
  val cmdExtractData = new JButton("Extract Data")
  val cmdSaveModifiedImage = new JButton("Save Image")

  val model = new StegToolModel

  setupComponents()

  inputUsedBits.addChangeListener((_: ChangeEvent) => updateStates())

  onClick(cmdLoadHostImage) {
    withOpenFile {
      selected =>
        model.loadHostImage(selected)
        iconHostImage.setIcon(new ImageIcon(model.hostImage.image))
    }
  }

  onClick(cmdLoadGuestData) {
    withOpenFile {
      model.loadGuestData
    }
  }

  onClick(cmdSaveGuestData) {
    withSaveFile {
      model.saveGuestData
    }
  }

  onClick(cmdExtractData) {
    model.extractData(inputUsedBits.getValue, inputPassphrase.getText)
  }

  onClick(cmdSaveModifiedImage) {
    withSaveFile {
      model.saveModifiedImage
    }
  }

  onClick(cmdEmbedData) {
    model.embedData(inputUsedBits.getValue, inputPassphrase.getText)
    iconModifiedImage.setIcon(new ImageIcon(model.modifiedImage.image))
  }

  onClick(cmdLoadModifiedImage) {
    withOpenFile { selected =>
      model.loadModifiedImage(selected)
      iconModifiedImage.setIcon(new ImageIcon(model.modifiedImage.image))
    }
  }

  def updateStates(): Unit = {
    cmdExtractData.setEnabled(model.canExtractData)
    cmdSaveModifiedImage.setEnabled(model.modifiedImage.isLoaded)
    cmdEmbedData.setEnabled(model.canEmbedData)
    cmdSaveGuestData.setEnabled(model.guestData.isLoaded)
    cmdLoadGuestData.setEnabled(model.hostImage.isLoaded)

    txtGuestDataStatus.setText(if (model.guestData.isLoaded) s"Guest data size: ${model.guestData.data.length} bytes." else "No guest data loaded.")
    txtImageCapacity.setText(model.hostImage.stegImageCapacity(inputUsedBits.getValue).map(_.toString).getOrElse(""))
    txtImageModification.setText(model.imageModificationPercentage.map(imp => f"$imp%2.2f%%").getOrElse(""))
  }

  def setupComponents(): Unit = {
    setTitle("Stegood ;-)")
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)

    setLayout(new GridBagLayout)

    txtGuestDataStatus.setEditable(false)
    txtImageCapacity.setEditable(false)
    txtImageModification.setEditable(false)

    inputUsedBits.setPaintTicks(true)
    inputUsedBits.setPaintLabels(true)
    inputUsedBits.setSnapToTicks(true)
    inputUsedBits.setMajorTickSpacing(1)

    updateStates()

    val panelTop = new JPanel(new GridLayout(1, 2))
    panelTop.add(createLeftPanel)
    panelTop.add(createRightPanel)

    gbc(this, panelTop, 0, 0, 1, 1, 1, 1, CENTER, BOTH)
    gbc(this, createMiddlePanel, 0, 1, 1, 1, 1, 0, CENTER, HORIZONTAL)
    gbc(this, createBottomPanel, 0, 2, 1, 1, 1, 0, CENTER, HORIZONTAL)

    pack()
    setSize(1000, 700)
  }

  def createMiddlePanel: Container = {
    val p = createTitledPanel(new GridBagLayout, "Guest Data")

    gbc(p, txtGuestDataStatus, 0, 0, 1, 1, 1, 0, WEST, HORIZONTAL)
    gbc(p, createButtonPanel(cmdLoadGuestData, cmdSaveGuestData), 0, 1, 1, 1, 1, 0, WEST, NONE)
  }

  def createLeftPanel: Container = {
    val p = createTitledPanel(new GridBagLayout, "Host Image")

    gbc(p, centeredItemInScrollPane(iconHostImage), 0, 0, 4, 1, 1, 1, SOUTH, BOTH)
    gbc(p, cmdLoadHostImage, 0, 1, 1, 1, 0, 0, CENTER, NONE)
    gbc(p, cmdEmbedData, 1, 1, 1, 1, 0, 0, CENTER, NONE)
    gbc(p, new JLabel(" Image Capacity:"), 2, 1, 1, 1, 0, 0, CENTER, NONE)
    gbc(p, txtImageCapacity, 3, 1, 1, 1, 1, 0, CENTER, HORIZONTAL)
  }

  def createRightPanel: Container = {
    val p = createTitledPanel(new GridBagLayout, "Modified Image")

    gbc(p, centeredItemInScrollPane(iconModifiedImage), 0, 0, 5, 1, 1, 1, CENTER, BOTH)
    gbc(p, cmdLoadModifiedImage, 0, 1, 1, 1, 0, 0, CENTER, NONE)
    gbc(p, cmdExtractData, 1, 1, 1, 1, 0, 0, CENTER, NONE)
    gbc(p, cmdSaveModifiedImage, 2, 1, 1, 1, 0, 0, CENTER, NONE)
    gbc(p, new JLabel(" Modification:"), 3, 1, 1, 1, 0, 0, CENTER, NONE)
    gbc(p, txtImageModification, 4, 1, 1, 1, 1, 0, CENTER, HORIZONTAL)
  }

  def createBottomPanel: Container = {
    val p = createTitledPanel(new GridBagLayout, "Parameters")

    gbc(p, new JLabel("Passphrase"), 0, 0, 1, 1, 0, 0, WEST, NONE)
    gbc(p, inputPassphrase, 1, 0, 1, 1, 1, 0, WEST, HORIZONTAL)
    gbc(p, new JLabel("Used Bits"), 0, 1, 1, 1, 0, 0, WEST, NONE)
    gbc(p, inputUsedBits, 1, 1, 1, 1, 1, 0, WEST, HORIZONTAL)
  }

  def withSaveFile(block: (File) => Unit): Unit =
    if (jfc.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
      block(jfc.getSelectedFile)
    }

  def withOpenFile(block: (File) => Unit): Unit =
    if (jfc.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
      block(jfc.getSelectedFile)
    }

  def onClick(button: JButton)(block: => Unit): Unit =
    button.addActionListener((_: ActionEvent) => {
      try {
        block
      } catch {
        case ex: Exception =>
          JOptionPane.showMessageDialog(MainFrame.this, "An exception occured:\n" + ex.getMessage, "Error", JOptionPane.ERROR_MESSAGE)
          ex.printStackTrace()
      }

      updateStates()
    })
}
