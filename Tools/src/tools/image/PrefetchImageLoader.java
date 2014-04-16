package tools.image;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Hashtable;

public class PrefetchImageLoader
{

	private static Hashtable<String, Image> imageHash = new Hashtable<String, Image>();

	// TODO: see package org.j3d.util.ImageLoader for better cache idea

	public static void loadImages(String dir)
	{

		// start by going through the given directory and finding
		// various totals
		Toolkit tk = Toolkit.getDefaultToolkit();

		Image currentImage;
		File fDir = new File(dir);
		String[] listOfNames = fDir.list();
		// a media tracker with a dummy component ( it's just used to force image loading )
		MediaTracker mediaTracker = new MediaTracker(new Panel());
		if (listOfNames == null)
		{
			System.out.println("yep it's empty " + dir);
			return;
		}
		for (int i = 0; i < listOfNames.length; i++)
		{
			if (listOfNames[i].endsWith(".gif") || listOfNames[i].endsWith(".jpg"))
			{
				currentImage = tk.getImage(dir + "/" + listOfNames[i]);
				mediaTracker.addImage(currentImage, 0);

				imageHash.put(listOfNames[i], currentImage);
			}
		}

		// now load all the images in
		try
		{
			mediaTracker.waitForAll();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		if (mediaTracker.isErrorAny())
		{
			System.out.println("MediaTracker has a problem!");
			Object[] errs = mediaTracker.getErrorsAny();
			for (int i = 0; i < errs.length; i++)
			{
				System.out.println(errs[i].toString());
			}
		}

	}

	public static Image getImage(String imageName)
	{
		return imageHash.get(imageName);
	}

	public static BufferedImage getBufferedImage(String imageName)
	{
		Image im = imageHash.get(imageName);
		if (im != null)
		{
			BufferedImage copy = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = copy.createGraphics();
			g2d.drawImage(im, 0, 0, null);
			g2d.dispose();
			return copy;
		}
		else
		{
			System.out.println("image is null " + imageName);
			return null;
		}
	}

}