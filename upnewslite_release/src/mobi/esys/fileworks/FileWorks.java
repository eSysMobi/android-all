package mobi.esys.fileworks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.util.Log;

/**
 * Helper to work with files in android file system
 * 
 * @author Артем
 * @since 1.0
 * 
 *
 */
public class FileWorks {
	/**
	 * Reference to file on SD card
	 */
	private transient String filePath;
	/**
	 * Instance of file object
	 * 
	 * @see {@link File}
	 */
	private transient File file;
	/**
	 * Tag for logging
	 */
	private transient static final String TAG = "fileworks";

	/**
	 * Empty constructor
	 */
	public FileWorks() {
		super();
	}

	/**
	 * Costructor
	 */
	public FileWorks(final String filePath) {
		super();
		this.file = new File(filePath);
		this.filePath = filePath;
	}

	/**
	 * Create file on filepath if this don't exists
	 */
	public void createFile() {
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				if (e.getLocalizedMessage() != null) {
					Log.d(TAG, e.getLocalizedMessage());
				}
			}
		}
	}

	/**
	 * Delete files
	 */
	public void deleteFile() {
		file.delete();
	}

	/**
	 * Get byte from filepath for following md5 sum get method
	 * 
	 * @param filename
	 *            Name of file
	 * @return Byte mask
	 */
	private static byte[] createChecksum(final String filename) {
		InputStream fis;
		byte[] sum = new byte[1];
		try {
			fis = new FileInputStream(filename);

			final byte[] buffer = new byte[1024];
			final MessageDigest complete = MessageDigest.getInstance("MD5");
			int numRead;

			do {
				numRead = fis.read(buffer);
				if (numRead > 0) {
					complete.update(buffer, 0, numRead);
				}
			} while (numRead != -1);

			fis.close();
			sum = complete.digest();

		} catch (FileNotFoundException e) {
			if (e.getLocalizedMessage() != null) {
				Log.d(TAG, e.getLocalizedMessage());
			}
		} catch (NoSuchAlgorithmException e) {
			if (e.getLocalizedMessage() != null) {
				Log.d(TAG, e.getLocalizedMessage());
			}
		} catch (IOException e) {
			if (e.getLocalizedMessage() != null) {
				Log.d(TAG, e.getLocalizedMessage());
			}
		}

		return sum;
	}

	/**
	 * Get md5 sum from byte from byte[] createChecksum(String filename)
	 * 
	 * @see FileWorks#createChecksum(String)
	 * @return md5 sum of file
	 */
	public String getFileMD5() {
		byte[] bytesMask;

		bytesMask = createChecksum(filePath);

		final StringBuilder result = new StringBuilder();

		for (int i = 0; i < bytesMask.length; i++) {
			result.append(Integer.toString((bytesMask[i] & 0xff) + 0x100, 16)
					.substring(1));
		}
		return result.toString();
	}

	/**
	 * getter for file name
	 * 
	 * @return file name
	 */
	public String getFileName() {
		return file.getName();
	}

	/**
	 * getter for file path
	 * 
	 * @return file path
	 */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * setter for file path
	 * 
	 */
	public void setFilePath(final String filePath) {
		this.filePath = filePath;
	}

	/**
	 * getter for file class object
	 * 
	 * @return instance of file clas object
	 * 
	 */
	public File getFile() {
		return file;
	}

}
