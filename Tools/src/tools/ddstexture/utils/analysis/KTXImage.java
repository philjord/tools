package tools.ddstexture.utils.analysis;

import java.io.IOException;
import java.nio.ByteBuffer;

import tools.ddstexture.utils.analysis.ktx.KTXFormatException;
import tools.ddstexture.utils.analysis.ktx.KTXHeader;
import tools.ddstexture.utils.analysis.ktx.KTXReader;
import tools.ddstexture.utils.analysis.ktx.KTXTextureData;

 

public class KTXImage  extends CompressedImage
{
	public KTXHeader headers;
	public KTXTextureData textureData;

	public KTXImage(ByteBuffer buf) throws IOException, KTXFormatException
	{
		KTXReader reader = new KTXReader(buf, false);
		headers = reader.getHeaders();
		textureData = reader.getTextureData();
		 
	}

	@Override
	public int getNumMipMaps()
	{
		return headers.getNumberOfMipmapLevels();
	}

	@Override
	public int getWidth()
	{
		return headers.getPixelWidth();
	}

	@Override
	public int getHeight()
	{
		return headers.getPixelHeight();
	}

}
