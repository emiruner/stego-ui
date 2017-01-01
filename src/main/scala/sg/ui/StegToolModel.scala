package sg.ui

import java.awt.image.BufferedImage
import java.io._
import java.nio.file.{Files, StandardOpenOption}
import javax.imageio.ImageIO

import sg.core.{CryptStego, StegEngine}

trait Optional {
  def isLoaded: Boolean
}

trait Loaded extends Optional {
  def isLoaded = true
}

trait Unloaded extends Optional {
  def isLoaded = false
}

trait GuestData extends Optional {
  def load(guestDataFile: File): GuestData = {
    LoadedGuestData(Files.readAllBytes(guestDataFile.toPath))
  }

  def update(data: Array[Byte]): GuestData = LoadedGuestData(data)
  def save(file: File)
  def data: Array[Byte]
}

case object UnloadedGuestData extends GuestData with Unloaded {
  override def save(file: File): Unit = throw new UnsupportedOperationException
  override def data: Array[Byte] = throw new UnsupportedOperationException
}

case class LoadedGuestData(data: Array[Byte]) extends GuestData with Loaded {
  override def save(file: File): Unit = Files.write(file.toPath, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
}

trait HostImage extends Optional {
  def image: BufferedImage
  def cloneImage: HostImage
  def load(file: File) = LoadedHostImage(ImageIO.read(new FileInputStream(file)))
  def savePng(file: File): Boolean
  def stegImageCapacity(bitsUsed: Int): Option[Int]
}

case object UnloadedHostImage extends HostImage with Unloaded {
  override def cloneImage: HostImage = throw new UnsupportedOperationException
  override def savePng(file: File): Boolean = throw new UnsupportedOperationException
  override def image: BufferedImage = throw new UnsupportedOperationException
  override def stegImageCapacity(bitsUsed: Int): Option[Int] = None
}

case class LoadedHostImage(image: BufferedImage) extends HostImage with Loaded {
  override def cloneImage: HostImage = {
    val copyRaster = image.getRaster.createCompatibleWritableRaster
    copyRaster.setRect(image.getRaster)

    val copy = new BufferedImage(image.getWidth, image.getHeight, image.getType)
    copy.setData(copyRaster)

    LoadedHostImage(copy)
  }

  override def savePng(file: File): Boolean = ImageIO.write(image, "PNG", new FileOutputStream(file))
  override def stegImageCapacity(bitsToUse: Int): Option[Int] = Some(StegEngine.calculateImageBitCapacity(image.getRaster, bitsToUse))
}

class StegToolModel {
  var hostImage: HostImage = UnloadedHostImage
  var modifiedImage: HostImage = UnloadedHostImage
  var guestData: GuestData = UnloadedGuestData
  var imageModificationPercentage: Option[Double] = None

  def saveGuestData(guestDataFile: File): Unit = guestData.save(guestDataFile)

  def extractData(usedBits: Int, passphrase: String): Unit = {
    guestData = guestData.update(CryptStego.extractData(modifiedImage.image.getRaster, usedBits, passphrase))
  }

  def embedData(usedBits: Int, passphrase: String): Unit = {
    modifiedImage = hostImage.cloneImage
    imageModificationPercentage = Some(CryptStego.embedData(modifiedImage.image.getRaster, guestData.data, usedBits, passphrase))
  }

  def loadGuestData(guestDataFile: File): Unit = guestData = guestData.load(guestDataFile)
  def loadModifiedImage(modifiedImageFile: File): Unit = modifiedImage = modifiedImage.load(modifiedImageFile)
  def saveModifiedImage(modifiedImageFile: File): Boolean = modifiedImage.savePng(modifiedImageFile)
  def loadHostImage(hostImageFile: File): Unit = hostImage = hostImage.load(hostImageFile)
  def canExtractData: Boolean = modifiedImage.isLoaded
  def canEmbedData: Boolean = guestData.isLoaded && hostImage.isLoaded
}
