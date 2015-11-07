package mobi.esys.upnews_play.consts;


import android.os.Environment;

import java.io.File;

public final class  Consts {
    public static final File STORAGE_FOLDER=new File(File.separator.concat("storage"));
    public static final File MOUNT_FOLDER=new File(File.separator.concat("mnt"));
    public static final File APP_FOLDER=new File(
            Environment.getExternalStorageDirectory().getAbsolutePath()
                    .concat(File.separator).concat("PLAY"));
    public static final int APP_DELAY=10000;
    public static final int ANIM_DELAY=7000;
    public static final String[] PICTURES_EXTS={"png","jpg","jpeg"};
    public static final String[] VIDEOS_EXTS={"avi","mp4"};
    public static final String[] MUSIC_EXTS={"mp3"};
    public static final String[] MEDIA_EXTS={"avi","mp4","mp3"};
}
