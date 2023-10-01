package tools.ddstexture.utils.analysis.dds;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class NioImageBuffer {
	public class ImageType {

		public static final int TYPE_3BYTE_RGB = 0;
		public static final int TYPE_4BYTE_RGBA = 1;
		public static final int TYPE_INT_RGB = 2;
		public static final int TYPE_INT_ARGB = 3;

	}
	public int width;public  int height;
	public int type;
	public Buffer buffer;
	
	public NioImageBuffer(int width, int height, int type, ByteBuffer pixels) {
		this.width=width;
		this.height=height;
		this.type=type;
		this.buffer=pixels;
	}

	public NioImageBuffer(int width, int height, int type) {
		this.width=width;
		this.height=height;
		this.type=type;
	}

	public NioImageBuffer(int width, int height, int type, IntBuffer pixels) {
		this.width=width;
		this.height=height;
		this.type=type;
		this.buffer=pixels;
	}

	public Buffer getDataBuffer() {
		return buffer;
	}

	public int getImageType() {
		return type;
	}

}
