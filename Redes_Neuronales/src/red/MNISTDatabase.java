package red;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 * MNIST database utilities.
 * 
 * @author Fernando Berzal (berzal@acm.org)
 */
public class MNISTDatabase 
{
	// Logger
	protected static final Logger log = Logger.getLogger(MNISTDatabase.class.getName());

	// Read data from files
	
	/**
	 * Read MNIST image data.
	 * 
	 * @param filename File name
	 * @return 3D int array
	 * @throws IOException
	 */
	public int[][][] readImages (String filename)
			throws IOException
	{
		FileInputStream file = null;
		InputStream gzip = null;
		DataInputStream data = null;
		int images[][][] = null;

		try {
			file = new FileInputStream(filename);
			gzip = new GZIPInputStream(file);
			data = new DataInputStream(gzip);

			log.info("Reading MNIST data...");

			int magicNumber = data.readInt();

			if (magicNumber!=2051) // 0x00000801 == 08 (unsigned byte) + 03 (3D tensor, i.e. multiple 2D images)
				throw new IOException("Error while reading MNIST data from "+filename);

			int size = data.readInt();
			int rows = data.readInt();
			int columns = data.readInt();

			images = new int[size][rows][columns];
			
			log.info("Reading "+size+" "+rows+"x"+columns+" images...");

			for (int i=0; i<size; i++)
				for (int j=0; j<rows; j++)
					for (int k=0; k<columns; k++)
						images[i][j][k] = data.readUnsignedByte();

			log.info("MNIST images read from "+filename);

		} finally {

			if (data!=null)
				data.close();
			if (gzip!=null)
				gzip.close();
			if (file!=null)
				file.close();
		}

		return images;
	}
	
	/**
	 * Read MNIST labels
	 * 
	 * @param filename File name
	 * @return Label array
	 * @throws IOException
	 */
	public int[] readLabels (String filename)
		throws IOException
	{
		FileInputStream file = null;
		InputStream gzip = null;
		DataInputStream data = null;
		int labels[] = null;

		try {
			file = new FileInputStream(filename);
			gzip = new GZIPInputStream(file);
			data = new DataInputStream(gzip);

			log.info("Reading MNIST labels...");

			int magicNumber = data.readInt();

			if (magicNumber!=2049) // 0x00000801 == 08 (unsigned byte) + 01 (vector)
				throw new IOException("Error while reading MNIST labels from "+filename);

			int size = data.readInt();
			
			labels = new int[size];

			for (int i=0; i<size; i++)
				labels[i] = data.readUnsignedByte();

			log.info("MNIST labels read from "+filename);

		} finally {
			
			if (data!=null)
				data.close();
			if (gzip!=null)
				gzip.close();
			if (file!=null)
				file.close();
		}
		
		return labels;
	}

	
	/**
	 * Normalize raw image data, i.e. convert to floating-point and rescale to [0,1].
	 * 
	 * @param image Raw image data
	 * @return Floating-point 2D array
	 */
	public float[][] normalize (int image[][])
	{
		int rows = image.length;
		int columns = image[0].length;
		float data[][] = new float[rows][columns];
		
		for (int i=0; i<rows; i++)
			for (int j=0; j<rows; j++)
				data[i][j] = (float)image[i][j] / 255f;
		
		return data;
	}

	
	// Standard I/O
	
	public String toString (int label)
	{
		return Integer.toString(label);
	}
	
	public String toString (int image[][])
	{
		StringBuilder builder = new StringBuilder();

		for (int i=0; i<image.length; i++) {
			for (int j=0; j<image[i].length; j++) {
				String hex = Integer.toHexString(image[i][j]);
				if (hex.length()==1) 
					builder.append("0");
				builder.append(hex);
				builder.append(' ');				
			}
			builder.append('\n');
		}
		
		return builder.toString();
	}

	public String toString (float image[][])
	{
		StringBuilder builder = new StringBuilder();

		for (int i=0; i<image.length; i++) {
			for (int j=0; j<image[i].length; j++) {
				builder.append( String.format(Locale.US, "%.3f ", image[i][j]) );
			}
			builder.append('\n');
		}
		
		return builder.toString();
	}

}
