package tools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4d;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

public class StringStore
{

	public static void strings()
	{
		Object o = new Rectangle();
		System.out.println("" + o);
		o = new Dimension();
		System.out.println("" + o);
		o = new Color(10, 10, 10);
		System.out.println("" + o);
		o = new Font("name", 1, 2);
		System.out.println("" + o);
		o = new Vector3d();
		System.out.println("" + o);
		o = new Point3d();
		System.out.println("" + o);
		o = new Vector3f();
		System.out.println("" + o);
		o = new Point3f();
		System.out.println("" + o);
		o = new Quat4d();
		System.out.println("" + o);
		o = new AxisAngle4d();
		System.out.println("" + o);
		o = new Quat4f();
		System.out.println("" + o);
		o = new AxisAngle4f();
		System.out.println("" + o);
		o = new Color3f();
		System.out.println("" + o);

		/*
		 * java.awt.Rectangle[x=0,y=0,width=0,height=0] java.awt.Dimension[width=0,height=0]
		 * java.awt.Color[r=10,g=10,b=10] java.awt.Font[family=dialog,name=name,style=bold,size=2] (0.0, 0.0, 0.0) (0.0,
		 * 0.0, 0.0) (0.0, 0.0, 0.0) (0.0, 0.0, 0.0) (0.0, 0.0, 0.0, 0.0) (0.0, 0.0, 1.0, 0.0) (0.0, 0.0, 0.0, 0.0)
		 * (0.0, 0.0, 1.0, 0.0)
		 * 
		 * 
		 * 
		 */

	}

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

	public static Vector3d getVector3d(String str)
	{
		int commaPos1 = str.indexOf(",");
		int commaPos2 = str.indexOf(",", commaPos1 + 1);
		double x = Double.parseDouble(str.substring(1, commaPos1));
		double y = Double.parseDouble(str.substring(commaPos1 + 1, commaPos2));
		double z = Double.parseDouble(str.substring(commaPos2 + 1, str.length() - 1));
		return new Vector3d(x, y, z);
	}

	public static Point3d getPoint3d(String str)
	{
		int commaPos1 = str.indexOf(",");
		int commaPos2 = str.indexOf(",", commaPos1 + 1);
		double x = Double.parseDouble(str.substring(1, commaPos1));
		double y = Double.parseDouble(str.substring(commaPos1 + 1, commaPos2));
		double z = Double.parseDouble(str.substring(commaPos2 + 1, str.length() - 1));
		return new Point3d(x, y, z);
	}

	public static Vector3f getVector3f(String str)
	{
		int commaPos1 = str.indexOf(",");
		int commaPos2 = str.indexOf(",", commaPos1 + 1);
		float x = Float.parseFloat(str.substring(1, commaPos1));
		float y = Float.parseFloat(str.substring(commaPos1 + 1, commaPos2));
		float z = Float.parseFloat(str.substring(commaPos2 + 1, str.length() - 1));
		return new Vector3f(x, y, z);
	}

	public static Point3f getPoint3f(String str)
	{
		int commaPos1 = str.indexOf(",");
		int commaPos2 = str.indexOf(",", commaPos1 + 1);
		float x = Float.parseFloat(str.substring(1, commaPos1));
		float y = Float.parseFloat(str.substring(commaPos1 + 1, commaPos2));
		float z = Float.parseFloat(str.substring(commaPos2 + 1, str.length() - 1));
		return new Point3f(x, y, z);
	}

	public static Quat4d getQuat4d(String str)
	{
		int commaPos1 = str.indexOf(",");
		int commaPos2 = str.indexOf(",", commaPos1 + 1);
		int commaPos3 = str.indexOf(",", commaPos2 + 1);
		double x = Double.parseDouble(str.substring(1, commaPos1));
		double y = Double.parseDouble(str.substring(commaPos1 + 1, commaPos2));
		double z = Double.parseDouble(str.substring(commaPos2 + 1, commaPos3));
		double w = Double.parseDouble(str.substring(commaPos3 + 1, str.length() - 1));
		return new Quat4d(x, y, z, w);
	}

	public static AxisAngle4d getAxisAngle4d(String str)
	{
		int commaPos1 = str.indexOf(",");
		int commaPos2 = str.indexOf(",", commaPos1 + 1);
		int commaPos3 = str.indexOf(",", commaPos2 + 1);
		double x = Double.parseDouble(str.substring(1, commaPos1));
		double y = Double.parseDouble(str.substring(commaPos1 + 1, commaPos2));
		double z = Double.parseDouble(str.substring(commaPos2 + 1, commaPos3));
		double w = Double.parseDouble(str.substring(commaPos3 + 1, str.length() - 1));
		return new AxisAngle4d(x, y, z, w);
	}

	public static Quat4f getQuat4f(String str)
	{
		int commaPos1 = str.indexOf(",");
		int commaPos2 = str.indexOf(",", commaPos1 + 1);
		int commaPos3 = str.indexOf(",", commaPos2 + 1);
		float x = Float.parseFloat(str.substring(1, commaPos1));
		float y = Float.parseFloat(str.substring(commaPos1 + 1, commaPos2));
		float z = Float.parseFloat(str.substring(commaPos2 + 1, commaPos3));
		float w = Float.parseFloat(str.substring(commaPos3 + 1, str.length() - 1));
		return new Quat4f(x, y, z, w);
	}

	public static AxisAngle4f getAxisAngle4f(String str)
	{
		int commaPos1 = str.indexOf(",");
		int commaPos2 = str.indexOf(",", commaPos1 + 1);
		int commaPos3 = str.indexOf(",", commaPos2 + 1);
		float x = Float.parseFloat(str.substring(1, commaPos1));
		float y = Float.parseFloat(str.substring(commaPos1 + 1, commaPos2));
		float z = Float.parseFloat(str.substring(commaPos2 + 1, commaPos3));
		float w = Float.parseFloat(str.substring(commaPos3 + 1, str.length() - 1));
		return new AxisAngle4f(x, y, z, w);
	}

	public static Color3f getColor3f(String str)
	{
		int commaPos1 = str.indexOf(",");
		int commaPos2 = str.indexOf(",", commaPos1 + 1);
		float x = Float.parseFloat(str.substring(1, commaPos1));
		float y = Float.parseFloat(str.substring(commaPos1 + 1, commaPos2));
		float z = Float.parseFloat(str.substring(commaPos2 + 1, str.length() - 1));
		return new Color3f(x, y, z);
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
