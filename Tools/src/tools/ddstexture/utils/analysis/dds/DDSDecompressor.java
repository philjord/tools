package tools.ddstexture.utils.analysis.dds;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import tools.ddstexture.utils.analysis.AnalysisData;
import tools.ddstexture.utils.analysis.DDSImage;
import tools.ddstexture.utils.analysis.dds.NioImageBuffer.ImageType;

 

/**
 * Converts DDS files and streams into {@code BufferedImage} {@link http://en.wikipedia.org/wiki/S3_Texture_Compression}
 * Or NioImageBuffer
 */
public class DDSDecompressor {
	public static final int		BLOCK_SIZE	= 4;

	public DDSImage				ddsImage;

	private int					mipNumber;

	private DDSImage.ImageInfo	imageInfo;

	private ByteBuffer			buffer;

	private int					width;

	private int					height;

	private String				imageName	= "";
	
	public AnalysisData analysisData = new AnalysisData();

	/**
	 * 
	 * @param ddsImage
	 * @param mipNumber
	 * @param imageName
	 */
	public DDSDecompressor(DDSImage ddsImage, int mipNumber, String imageName) {
		this.imageName = imageName;
		this.ddsImage = ddsImage;
		this.mipNumber = mipNumber;
		this.imageInfo = ddsImage.getAllMipMaps() [mipNumber];
		this.width = imageInfo.getWidth();
		this.height = imageInfo.getHeight();
		this.buffer = imageInfo.getData();
		
		
		analysisData.imageName = imageName;
		analysisData.imageWidth = width;
		analysisData.imageHeight = height;
	}

	public String getImageName() {
		return imageName;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getType() {
		return ImageType.TYPE_INT_ARGB;
	}

	/**
	 * 
	 * @return a new non-cached {@code BufferedImage}
	 */
	public NioImageBuffer convertImageNio() {

		//can't use width or height as it's been corrected to 1 already
		if (imageInfo.getWidth() < 1 || imageInfo.getHeight() < 1) {
			return new NioImageBuffer(1, 1, ImageType.TYPE_INT_ARGB);
		}

		//prep the buffer
		buffer.rewind();
		buffer.order(ByteOrder.LITTLE_ENDIAN);

		if (ddsImage.getPixelFormat() == DDSImage.D3DFMT_DXT1) {
			return decodeDxt1BufferNio();
		} else if (ddsImage.getPixelFormat() == DDSImage.D3DFMT_DXT3) {
			return decodeDxt3BufferNio();
		} else if (ddsImage.getPixelFormat() == DDSImage.D3DFMT_DXT5) {
			return decompressRGBA_S3TC_DXT5_EXTNio();
		} else if (ddsImage.getPixelFormat() == DDSImage.D3DFMT_ATI2) {
			// NOT correct but it gives you the idea a bit
			return decompressRGBA_S3TC_DXT5_EXTNio();
		} 
		System.err.println("BAD DXT format!! " + ddsImage.getPixelFormat());
		return null;
	}

	

	private NioImageBuffer decodeDxt1BufferNio() {
		int numBlocksWide = width / BLOCK_SIZE;
		int numBlocksHigh = height / BLOCK_SIZE;

		// always at least 1x1 tile
		numBlocksWide = numBlocksWide < 1 ? 1 : numBlocksWide;
		numBlocksHigh = numBlocksHigh < 1 ? 1 : numBlocksHigh;
		
		analysisData.blocksWide = numBlocksWide;
		analysisData.blocksHigh = numBlocksHigh;

		int blockWidth = Math.min(width, BLOCK_SIZE);
		int blockHeight = Math.min(height, BLOCK_SIZE);

		ByteBuffer directBuffer = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.nativeOrder());
		IntBuffer pixels = directBuffer.asIntBuffer();

		analysisData.inblockData = new AnalysisData.BlockData[numBlocksHigh][numBlocksWide];

		//One copy of table to minimises new calls
		Color24[] table = new Color24[4];
		table [0] = new Color24();
		table [1] = new Color24();
		table [2] = new Color24();
		table [3] = new Color24();

		for (int row = 0; row < numBlocksHigh; row++) {
			for (int col = 0; col < numBlocksWide; col++) {
				
				AnalysisData.DXT1 blockData = new AnalysisData.DXT1();	
				analysisData.inblockData[row][col] = blockData;		
				
				
				short c0 = buffer.getShort();
				short c1 = buffer.getShort();
				int colorIndexMask = buffer.getInt();
				
				blockData.c0=c0;
				blockData.c1=c1;
				blockData.colorIndexMask=colorIndexMask;

				//http://en.wikipedia.org/wiki/S3_Texture_Compression
				if (!Color24.hasAlphaBit(c0, c1)) {
					
					blockData.hasAlphaBit = false;
					
					
					Color24[] lookupTable = Color24.expandLookupTable(table, c0, c1);
					
					blockData.lookupTable = new Color24[4];
					System.arraycopy(lookupTable, 0, blockData.lookupTable, 0, 4);
					
					
					for (int br = 0; br < blockHeight; br++) {
						for (int bc = 0; bc < blockWidth; bc++) {
							int k = (br * blockWidth) + bc;
							int colorIndex = (colorIndexMask >>> k * 2) & 0x03;
							
							Color24 color = lookupTable [colorIndex];	
							int pixel8888 = (0xFF << 24) | (color.b << 16) | (color.g << 8) | (color.r << 0);
							
							pixels.put(((row * blockHeight) * (numBlocksWide*blockWidth))  // previous columns and row
									+ (br * (numBlocksWide*blockWidth))
									+ (col * blockWidth)
									+ bc
									,pixel8888);
						}
					}
				} else {
					
					blockData.hasAlphaBit = true;
					
					Color24[] lookupTable = Color24.expandLookupTableAlphable(table, c0, c1);
					
					blockData.lookupTable = new Color24[4];
					System.arraycopy(lookupTable, 0, blockData.lookupTable, 0, 4);
					
					for (int br = 0; br < blockHeight; br++) {
						for (int bc = 0; bc < blockWidth; bc++) {
							int k = (br * blockWidth) + bc;
							int colorIndex = (colorIndexMask >>> k * 2) & 0x03;
							int alpha = (colorIndex == 3) ? 0x00 : 0xFF;

							Color24 color = lookupTable [colorIndex];	
							int pixel8888 = (alpha << 24) | (color.b << 16) | (color.g << 8) | (color.r << 0);
							
							pixels.put(((row * blockHeight) * (numBlocksWide*blockWidth))  // previous columns and row
									+ (br * (numBlocksWide*blockWidth))
									+ (col * blockWidth)
									+ bc
									,pixel8888);
						}
					}
				}
			}
		}
		return new NioImageBuffer(width, height, ImageType.TYPE_4BYTE_RGBA, directBuffer);
	}

	private NioImageBuffer decodeDxt3BufferNio() {
		int numBlocksWide = width / BLOCK_SIZE;
		int numBlocksHigh = height / BLOCK_SIZE;

		// always at least 1x1 tile
		numBlocksWide = numBlocksWide < 1 ? 1 : numBlocksWide;
		numBlocksHigh = numBlocksHigh < 1 ? 1 : numBlocksHigh;
		
		analysisData.blocksWide = numBlocksWide;
		analysisData.blocksHigh = numBlocksHigh;

		int blockWidth = Math.min(width, BLOCK_SIZE);
		int blockHeight = Math.min(height, BLOCK_SIZE);

		ByteBuffer directBuffer = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.nativeOrder());
		IntBuffer pixels = directBuffer.asIntBuffer();
		
		analysisData.inblockData = new AnalysisData.BlockData[numBlocksHigh][numBlocksWide];

		//One copy of table to minimises new calls
		Color24[] table = new Color24[4];
		table [0] = new Color24();
		table [1] = new Color24();
		table [2] = new Color24();
		table [3] = new Color24();

		for (int row = 0; row < numBlocksHigh; row++) {
			for (int col = 0; col < numBlocksWide; col++) {
				
				AnalysisData.DXT3 blockData = new AnalysisData.DXT3();	
				analysisData.inblockData[row][col] = blockData;
				
				
				long alphaData = buffer.getLong();
				short minColor = buffer.getShort();
				short maxColor = buffer.getShort();
				int colorIndexMask = buffer.getInt();
							

				Color24[] lookupTable = Color24.expandLookupTable(table, minColor, maxColor);
				
				blockData.alphaData = alphaData;
				blockData.minColor = minColor;
				blockData.maxColor = maxColor;
				blockData.colorIndexMask = colorIndexMask;
				blockData.lookupTable = new Color24[4];
				System.arraycopy(lookupTable, 0, blockData.lookupTable, 0, 4);

				for (int br = 0; br < blockHeight; br++) {
					for (int bc = 0; bc < blockWidth; bc++) {
						int k = (br * blockWidth) + bc;
						int alpha = (int)(alphaData >>> (k * 4)) & 0xF; // Alphas are just 4 bits per pixel
						// the original code is like *16 =>   alpha <<= 4;
						// but 0-15 needs *17 for 15==255
						// Here, we should really multiply by 17 instead of 16. This can
						// be done by just copying the four lower bits to the upper ones
						// while keeping the lower bits.
						alpha = (byte)(alpha | (alpha <<4));

						int colorIndex = (colorIndexMask >>> k * 2) & 0x03;

						Color24 color = lookupTable [colorIndex];						
						
						//0xFF0000FF = red
						//0xFF00FF00 = green						
						//0xFFFF0000 = blue
						//ABGR! so can't use the color24 ARGB system
						int pixel8888 = (alpha << 24) | (color.b << 16) | (color.g << 8) | (color.r << 0);

						pixels.put(((row * blockHeight) * (numBlocksWide*blockWidth))  // previous columns and row
								+ (br * (numBlocksWide*blockWidth))
								+ (col * blockWidth)
								+ bc
								,pixel8888);
					}
				}
				//notice notice no flipping for theNioImageBuffer version!
			}

		}
		
		return new NioImageBuffer(width, height, ImageType.TYPE_4BYTE_RGBA, directBuffer);
	}

	private NioImageBuffer decompressRGBA_S3TC_DXT5_EXTNio() {
		int numBlocksWide = width / BLOCK_SIZE;
		int numBlocksHigh = height / BLOCK_SIZE;

		// always at least 1x1 tile
		numBlocksWide = numBlocksWide < 1 ? 1 : numBlocksWide;
		numBlocksHigh = numBlocksHigh < 1 ? 1 : numBlocksHigh;
		
		analysisData.blocksWide = numBlocksWide;
		analysisData.blocksHigh = numBlocksHigh;

		int blockWidth = Math.min(width, BLOCK_SIZE);
		int blockHeight = Math.min(height, BLOCK_SIZE);

		ByteBuffer directBuffer = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.nativeOrder());
		IntBuffer pixels = directBuffer.asIntBuffer();	
		
		analysisData.inblockData = new AnalysisData.BlockData[numBlocksHigh][numBlocksWide];

		//One copy of table to minimises new calls
		Color24[] table = new Color24[4];
		table [0] = new Color24();
		table [1] = new Color24();
		table [2] = new Color24();
		table [3] = new Color24();

		for (int row = 0; row < numBlocksHigh; row++) {
			for (int col = 0; col < numBlocksWide; col++) {
				
				AnalysisData.DXT5 blockData = new AnalysisData.DXT5();	
				analysisData.inblockData[row][col] = blockData;
				
				
				int alpha0 = buffer.get() & 0xff; //unsigned byte
				int alpha1 = buffer.get() & 0xff; //unsigned byte

				// next 6 bytes are a look up list (note long casts, important!)
				long alphaBits = (buffer.get() & 0xffL) << 0L //
									| (buffer.get() & 0xffL) << 8L //
									| (buffer.get() & 0xffL) << 16L//
									| (buffer.get() & 0xffL) << 24L //
									| (buffer.get() & 0xffL) << 32L //
									| (buffer.get() & 0xffL) << 40L;//

				short minColor = buffer.getShort();
				short maxColor = buffer.getShort();
				int colorIndexMask = buffer.getInt();

				Color24[] lookupTable = Color24.expandLookupTable(table, minColor, maxColor);
				
				
				blockData.alpha0 = alpha0; //unsigned byte
				blockData.alpha1 = alpha1; //unsigned byte
				blockData.alphaBits = alphaBits;

				blockData.minColor = minColor;
				blockData.maxColor = maxColor;
				blockData.colorIndexMask = colorIndexMask;
				
				blockData.lookupTable = new Color24[4];
				System.arraycopy(lookupTable, 0, blockData.lookupTable, 0, 4);

				for (int br = 0; br < blockHeight; br++) {
					for (int bc = 0; bc < blockWidth; bc++) {
						int k = (br * blockWidth) + bc;

						int alphaCode = (int)(alphaBits >> (3 * k) & 0x07); // bottom 3 bits

						int alpha = 0;

						if (alphaCode == 0) {
							alpha = alpha0;
						} else if (alphaCode == 1) {
							alpha = alpha1;
						} else if (alpha0 > alpha1) {
							alpha = ((8 - alphaCode) * alpha0 + (alphaCode - 1) * alpha1) / 7;
						} else {
							if (alphaCode == 6)
								alpha = 0;
							else if (alphaCode == 7)
								alpha = 255;
							else
								alpha = ((6 - alphaCode) * alpha0 + (alphaCode - 1) * alpha1) / 5;

						}

						int colorIndex = (colorIndexMask >>> k * 2) & 0x03;

						Color24 color = lookupTable [colorIndex];
						int pixel8888 = (alpha << 24) | (color.b << 16) | (color.g << 8) | (color.r << 0);
						
						pixels.put(((row * blockHeight) * (numBlocksWide*blockWidth))  // previous columns and row
								+ (br * (numBlocksWide*blockWidth))
								+ (col * blockWidth)
								+ bc
								,pixel8888);
					}
				}
			}

		}
		return new NioImageBuffer(width, height, ImageType.TYPE_4BYTE_RGBA, directBuffer);
	}

	
	

	public static class MiniFloat {
		// ignores the higher 16 bits
		public static float toFloat(int hbits) {
			int mant = hbits & 0x03ff; // 10 bits mantissa
			int exp = hbits & 0x7c00; // 5 bits exponent
			if (exp == 0x7c00) // NaN/Inf
				exp = 0x3fc00; // -> NaN/Inf
			else if (exp != 0) // normalized value
			{
				exp += 0x1c000; // exp - 15 + 127
				if (mant == 0 && exp > 0x1c400) // smooth transition
					return Float.intBitsToFloat((hbits & 0x8000) << 16 | exp << 13 | 0x3ff);
			} else if (mant != 0) // && exp==0 -> subnormal
			{
				exp = 0x1c400; // make it normal
				do {
					mant <<= 1; // mantissa * 2
					exp -= 0x400; // decrease exp by 1
				} while ((mant & 0x400) == 0); // while not normal
				mant &= 0x3ff; // discard subnormal bit
			} // else +/-0 -> +/-0
			return Float.intBitsToFloat( // combine all parts
					(hbits & 0x8000) << 16 // sign  << ( 31 - 15 )
										| (exp | mant) << 13); // value << ( 23 - 10 )
		}

		// returns all higher 16 bits as 0 for all results
		public static int fromFloat(float fval) {
			int fbits = Float.floatToIntBits(fval);
			int sign = fbits >>> 16 & 0x8000; // sign only
			int val = (fbits & 0x7fffffff) + 0x1000; // rounded value

			if (val >= 0x47800000) // might be or become NaN/Inf
			{ // avoid Inf due to rounding
				if ((fbits & 0x7fffffff) >= 0x47800000) { // is or must become NaN/Inf
					if (val < 0x7f800000) // was value but too large
						return sign | 0x7c00; // make it +/-Inf
					return sign | 0x7c00 | // remains +/-Inf or NaN
							(fbits & 0x007fffff) >>> 13; // keep NaN (and Inf) bits
				}
				return sign | 0x7bff; // unrounded not quite Inf
			}
			if (val >= 0x38800000) // remains normalized value
				return sign | val - 0x38000000 >>> 13; // exp - 127 + 15
			if (val < 0x33000000) // too small for subnormal
				return sign; // becomes +/-0
			val = (fbits & 0x7fffffff) >>> 23; // tmp exp for subnormal calc
			return sign | ((fbits & 0x7fffff | 0x800000) // add subnormal bit
							+ (0x800000 >>> val - 102) // round depending on cut off
					>>> 126 - val); // div by 2^(1-(exp-127+15)) and >> 13 | exp=0
		}
	}

	/**
	 * 24 bit 888 RGB color
	 *
	 * @author Lado Garakanidze
	 * @version $Id$
	 */

	public static class Color24 {
		/**
		 * The red color component.
		 */
		public int	r		= 0;

		/**
		 * The green color component.
		 */
		public int	g		= 0;

		/**
		 * The blue color component.
		 */
		public int	b		= 0;

		// as a regular pixel 888 format
		public int	pix888	= 0;

		/**
		 * Creates a 24 bit 888 RGB color with all values set to 0.
		 */
		public Color24() {

		}

		public void set(int r, int g, int b) {
			this.r = r;
			this.g = g;
			this.b = b;
			pix888 = (r << 16 | g << 8 | b);
		}

		private static Color24 fromShort565(Color24 out, short pixel) {
			//out.set((int) (((long) pixel) & 0xf800) >>> 8, (int) (((long) pixel) & 0x07e0) >>> 3, (int) (((long) pixel) & 0x001f) << 3);
			out.set((pixel & 0xf800) >>> 8, (pixel & 0x07e0) >>> 3, (pixel & 0x001f) << 3);
			return out;
		}

		/**
		 * for DXT1 only the short need to be treated as unsigned and value compared c0 and c1 is a signed short, but we
		 * need to treat it as unsigned for comparision (it's 16 bits of info not a short at all)
		 * @param c0
		 * @param c1
		 * @return
		 */
		public static boolean hasAlphaBit(short c0, short c1) {
			// & 0xFFFF makes it an int of unsigned short value
			return (c0 & 0xFFFF) <= (c1 & 0xFFFF);
		}

		/**
		 * for DXT1 only If inC0 > inC1 c4 will be left as black to be used as full transparent
		 * http://en.wikipedia.org/wiki/S3_Texture_Compression
		 * @param inC0
		 * @param inC1
		 * @return
		 */
		public static Color24[] expandLookupTableAlphable(Color24[] out, short inC0, short inC1) {
			fromShort565(out [0], inC0);
			fromShort565(out [1], inC1);
			out [2].set((out [0].r + out [1].r) / 2, (out [0].g + out [1].g) / 2, (out [0].b + out [1].b) / 2);
			out [3].set(0, 0, 0);

			return out;
		}

		public static Color24[] expandLookupTable(Color24[] out, short inC0, short inC1) {
			fromShort565(out [0], inC0);
			fromShort565(out [1], inC1);
			out [2].set((2 * out [0].r + out [1].r) / 3, (2 * out [0].g + out [1].g) / 3,
					(2 * out [0].b + out [1].b) / 3);
			out [3].set((out [0].r + 2 * out [1].r) / 3, (out [0].g + 2 * out [1].g) / 3,
					(out [0].b + 2 * out [1].b) / 3);
			return out;

		}

	}
}
