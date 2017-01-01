package sg.ui

import javax.swing.UIManager

object Main {
  def main(args: Array[String]): Unit = {
    UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel")
    new MainFrame().setVisible(true)
  }
}
