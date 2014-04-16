package tools.swing;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class LabeledEnumerate extends ImageIcon
{
	private int id;
	private Object userData;
	private Object labelObject;

	/* 
	 * One of filename or labelObject must be ""
	 */
	public LabeledEnumerate(int id, String filename, Object labelObject)
	{
		super();
		this.id = id;
		this.labelObject = labelObject;

		Image image;
	
		if (filename == null || filename.equals(""))
		{
			String text = labelObject.toString();
			JLabel l = new JLabel(text);
			l.setSize(l.getPreferredSize().width + 15, l.getPreferredSize().height);
			image = new BufferedImage(l.getWidth(), l.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics gr = ((BufferedImage) image).createGraphics();
			l.getUI().paint(gr, l);
		}
		else
		{
			image = Toolkit.getDefaultToolkit().getImage(filename);
			image = image.getScaledInstance(-1, 100, Image.SCALE_SMOOTH);			 
		}
		setImage(image);
	}

	public String toString()
	{
		if (labelObject != null)
			return labelObject.toString();
		else
			return "no label";
	}
	public int getId()
	{
		return id;
	}

	public Object getUserData()
	{
		return userData;
	}

	public void setUserData(Object object)
	{
		userData = object;
	}

}
