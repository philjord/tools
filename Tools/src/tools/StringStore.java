package tools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;

public class StringStore
{

	public static Rectangle getRectangle(String str)
	{
		int x = Integer.parseInt(str.substring(str.indexOf("x=") + 2, str.indexOf(",y=")));
		int y = Integer.parseInt(str.substring(str.indexOf("y=") + 2, str.indexOf(",width=")));
		int width = Integer.parseInt(str.substring(str.indexOf("width=") + 6, str.indexOf(",height=")));
		int height = Integer.parseInt(str.substring(str.indexOf("height=") + 7, str.indexOf("]")));
		return new Rectangle(x, y, width, height);
	}

	public static Dimension getDimension(String str)
	{
		int width = Integer.parseInt(str.substring(str.indexOf("width=") + 6, str.indexOf(",height=")));
		int height = Integer.parseInt(str.substring(str.indexOf("height=") + 7, str.indexOf("]")));
		return new Dimension(width, height);
	}

	public static Color getColor(String str)
	{
		int r = Integer.parseInt(str.substring(str.indexOf("r=") + 2, str.indexOf(",g=")));
		int g = Integer.parseInt(str.substring(str.indexOf("g=") + 2, str.indexOf(",b=")));
		int b = Integer.parseInt(str.substring(str.indexOf("b=") + 2, str.indexOf("]")));
		return new Color(r, g, b);
	}

	public static Font getFont(String str)
	{
		String name = str.substring(str.indexOf("name=") + 5, str.indexOf(",style="));
		String styleStr = str.substring(str.indexOf("style=") + 6, str.indexOf(",size="));
		int style = styleStr.equals("bold") ? Font.BOLD : styleStr.equals("italic") ? Font.ITALIC : Font.PLAIN;
		int size = Integer.parseInt(str.substring(str.indexOf("size=") + 5, str.indexOf("]")));
		return new Font(name, style, size);
	}

	public static Rectangle getRectangleFromComponentParamString(String str)
	{
		// component event param string example
		// COMPONENT_MOVED (405,121 150x800)

		int commaPos = str.indexOf(",");
		int spacePos2 = str.indexOf(" ", commaPos);
		int xPos = str.indexOf("x", spacePos2);

		int x = Integer.parseInt(str.substring(str.indexOf("(") + 1, commaPos));
		int y = Integer.parseInt(str.substring(commaPos + 1, spacePos2));
		int width = Integer.parseInt(str.substring(spacePos2 + 1, xPos));
		int height = Integer.parseInt(str.substring(xPos + 1, str.length() - 1));
		return new Rectangle(x, y, width, height);
	}

}
