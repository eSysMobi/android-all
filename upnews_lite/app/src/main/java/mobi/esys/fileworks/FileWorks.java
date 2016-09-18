package mobi.esys.fileworks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileWorks {
    private transient File file;

    public FileWorks(String filePath) {
        super();
        this.file = new File(filePath);
    }

    public FileWorks(File incomingFile) {
        super();
        this.file = incomingFile;
    }

    public File getFile() {
        return file;
    }

    private static byte[] createChecksum(String filename) {
        InputStream fis;
        byte[] sum = new byte[1];
        try {
            fis = new FileInputStream(filename);

            byte[] buffer = new byte[1024];
            MessageDigest complete = MessageDigest.getInstance("MD5");
            int numRead;

            do {
                numRead = fis.read(buffer);
                if (numRead > 0) {
                    complete.update(buffer, 0, numRead);
                }
            } while (numRead != -1);

            fis.close();
            sum = complete.digest();
            buffer = null;

        } catch (FileNotFoundException e) {
            Log.i("unTag_FileWorks", "Error FileNotFoundException in createChecksum");
        } catch (NoSuchAlgorithmException e) {
            Log.i("unTag_FileWorks", "Error NoSuchAlgorithmException in createChecksum");
        } catch (IOException e) {
            Log.i("unTag_FileWorks", "Error IOException in createChecksum");
        }

        return sum;
    }

    public String getFileMD5() {
        byte[] b;

        b = createChecksum(file.getAbsolutePath());

        String result = "";

        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        b = null;
        return result;
    }

    public String getFileName() {
        return file.getName();
    }

    public String getFileExtension() {
        String ext = "";
        String name = getFileName();
        int i = name.lastIndexOf('.');
        if (i > 0 && i < name.length() - 1) {
            ext = name.substring(i + 1);
        }
        Log.d("fw_tag", ext);
        return ext;
    }

    public byte[] fileToBytes() {
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public String renameFileExtension(String newExtension) {
        String target = "";
        String currentExtension = getFileExtension();
        String path = file.getAbsolutePath();
        String result = "";

        if (currentExtension.equals("")) {
            target = path + "." + newExtension;
        } else {
            target = path.replaceFirst(Pattern.quote("." +
                    currentExtension) + "$", Matcher.quoteReplacement("." + newExtension));

        }
        if (new File(path).renameTo(new File(target))) {
            result = target;
        }

        return result;
    }

    public Bitmap getLogoFromExternalStorage() {
        Bitmap result = null;
        if (file.exists()) {
            result = BitmapFactory.decodeFile(file.getAbsolutePath());
            Log.d("unTag_FileWorks", "Logo from file decoded");
        }
        return result;
    }
}
