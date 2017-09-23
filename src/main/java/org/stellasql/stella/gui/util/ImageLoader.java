package org.stellasql.stella.gui.util;

import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

/**
 * If the image file can not be found returns a 'error image', otherwise returns the image
 *
 * @author Lord Commander Shockey
 *
 */
public class ImageLoader
{
  private static final Logger logger = LogManager.getLogger(ImageLoader.class);

  public static Image loadImage(String filename)
  {
    InputStream is = ImageLoader.class.getClassLoader().getResourceAsStream(filename);

    Image image = null;
    if (is != null)
    {
      ImageData imageData = new ImageData(is);
      image = new Image(Display.getCurrent(), imageData);
    }
    else
    {
      logger.error("Image file not found: " + filename);
      image = new ErrorImage().getImage();
    }

    return image;
  }
  
  public static ImageData loadImageData(String filename)
  {
    InputStream is = ImageLoader.class.getClassLoader().getResourceAsStream(filename);

    ImageData imageData = null;
    if (is != null)
    {
      imageData = new ImageData(is);
    }
    else
    {
      logger.error("Image file not found: " + filename);
      imageData = new ErrorImage().getImage().getImageData();
    }

    return imageData;
  }

}
