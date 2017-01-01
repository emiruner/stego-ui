package sg.ui

import java.awt._
import javax.swing._
import BorderFactory._

object UIUtil {
  val I4 = new Insets(4, 4, 4, 4)

  def createTitledPanel(layout: LayoutManager, title: String): JPanel = addTitledBorder(new JPanel(layout), title)

  def addTitledBorder(panel: JPanel, title: String): JPanel = {
    panel.setBorder(createTitledBorder(createEtchedBorder, s" $title "))
    panel
  }

  def createButtonPanel(buttons: JButton*): JPanel = addButtons(new JPanel(new FlowLayout), buttons: _*)

  def addButtons(panel: JPanel, buttons: JButton*): JPanel = {
    buttons.foreach(panel.add(_))
    panel
  }

  def gbc(container: Container, child: Component, gridx: Int, gridy: Int, gridwidth: Int, gridheight: Int, weightx: Double, weighty: Double, anchor: Int, fill: Int): Container = {
    container.add(child, new GridBagConstraints(gridx, gridy, gridwidth, gridheight, weightx, weighty, anchor, fill, I4, 0, 0))
    container
  }

  def centeredItemInScrollPane(item: JComponent): JScrollPane = {
    val panel = new JPanel()
    panel.setLayout(new GridBagLayout)

    panel.add(item)

    new JScrollPane(panel)
  }
}
