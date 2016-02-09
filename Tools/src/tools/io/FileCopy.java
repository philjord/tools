package tools.io;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sun.istack.internal.Nullable;

public class FileCopy
{
	public static void copyInputStreamToFile(InputStream in, File file)
	{
		OutputStream out = null;
		try
		{
			out = new FileOutputStream(file);
			byte[] buf = new byte[1024 * 1024];
			int len;
			while ((len = in.read(buf)) > 0)
			{
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (out != null)
			{
				try
				{
					out.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	// Copy one file to another using NIO
	public static void doCopy(final File source, final File destination) throws IOException
	{
		final Closer closer = new Closer();
		final RandomAccessFile src, dst;
		final FileChannel in, out;

		try
		{
			src = closer.add(new RandomAccessFile(source.getCanonicalFile(), "r"));
			dst = closer.add(new RandomAccessFile(destination.getCanonicalFile(), "rw"));
			in = closer.add(src.getChannel());
			out = closer.add(dst.getChannel());
			in.transferTo(0L, in.size(), out);
			out.force(false);
		}
		finally
		{
			closer.close();
		}
	}

	public static class Closer implements Closeable
	{
		private final List<Closeable> closeables = new ArrayList<Closeable>();

		// @Nullable is a JSR 305 annotation
		public <T extends Closeable> T add(@Nullable final T closeable)
		{
			closeables.add(closeable);
			return closeable;
		}

		public void closeQuietly()
		{
			try
			{
				close();
			}
			catch (IOException ignored)
			{
			}
		}

		@Override
		public void close() throws IOException
		{
			IOException toThrow = null;
			final List<Closeable> l = new ArrayList<Closeable>(closeables);
			Collections.reverse(l);

			for (final Closeable closeable : l)
			{
				if (closeable == null)
					continue;
				try
				{
					closeable.close();
				}
				catch (IOException e)
				{
					if (toThrow == null)
						toThrow = e;
				}
			}

			if (toThrow != null)
				throw toThrow;
		}
	}

}
