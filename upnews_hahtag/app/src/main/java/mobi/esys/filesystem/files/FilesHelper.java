package mobi.esys.filesystem.files;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mobi.esys.consts.ISConsts;
import mobi.esys.upnews_hashtag.R;

/**
 * Created by Артем on 14.04.2015.
 */
public class FilesHelper {
    private transient File file;
    private transient Context context = null;
    private transient final String TAG = this.getClass().getSimpleName().concat(ISConsts.globals.default_logtag_devider);


    public FilesHelper(String filePath) {
        super();
        this.file = new File(filePath);
    }

    public FilesHelper(String filePath, Context context) {
        super();
        this.file = new File(filePath);
        this.context = context;
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

        } catch (FileNotFoundException e) {
        } catch (NoSuchAlgorithmException e) {
        } catch (IOException e) {
        }

        return sum;
    }

    public Bitmap getLogoFromExternalStorage() {
        Bitmap result = null;
        if (file.exists()) {
            result = BitmapFactory.decodeFile(file.getAbsolutePath());
            Log.d("TAG1", "Logo from file decoded");
        }
        return result;
    }

    public boolean createLogoInExternalStorage() {
        if (file.exists()) {
            Log.d("TAG1", "Logo file exists. Not need to create.");
            return true;
        } else {
            //TODO check free space
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
                Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.drawable.upnews_logo_w2);
                logo.compress(Bitmap.CompressFormat.PNG, 100, bos);
                bos.flush();
                bos.close();
                Log.d("TAG1", "Success saving logo file in external storage.");
                return true;
            } catch (FileNotFoundException e) {
                Log.d("TAG1", "Error saving logo file in external storage: " + e.getMessage());
                return false;
            } catch (IOException e) {
                Log.d("TAG1", "Error saving logo file in external storage: " + e.getMessage());
                return false;
            }
        }
    }

    public String getFileMD5() {
        byte[] b;

        b = createChecksum(file.getAbsolutePath());

        String result = "";

        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
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
        Log.d(TAG, ext);
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

    public boolean renameFileExtension(String newExtension) {
        String target;
        String currentExtension = getFileExtension();
        String path = file.getAbsolutePath();

        if (currentExtension.equals("")) {
            target = path + "." + newExtension;
        } else {
            target = path.replaceFirst(Pattern.quote("." +
                    currentExtension) + "$", Matcher.quoteReplacement("." + newExtension));

        }
        return new File(path).renameTo(new File(target));
    }
}
