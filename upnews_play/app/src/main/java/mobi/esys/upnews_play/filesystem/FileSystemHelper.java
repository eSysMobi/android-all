package mobi.esys.upnews_play.filesystem;


import android.util.Log;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class FileSystemHelper {
    public static boolean createFolder(File folderInstance){
        boolean isCreateSuccess=false;
        if(!folderInstance.exists()){
            isCreateSuccess=folderInstance.mkdir();
        }
        return  isCreateSuccess;
    }

    public static List<File> getFileListByExts(File folderInstance, String[] fileTypes){
        List<File> extsFileList=new ArrayList<>();
        File[] originalFileList=folderInstance.listFiles();
        for (File anOriginalFileList : originalFileList) {
            if(Arrays.asList(fileTypes).
                    contains(FilenameUtils.getExtension(anOriginalFileList.getName()))){
                extsFileList.add(anOriginalFileList);
            }
        }
        Log.d("fsh by exts",extsFileList.toString());
        return  extsFileList;
    }

    public static List<String> getFolderSubFoldersNames(File folder){
        List<String> subfoldersNames=new ArrayList<>();
        if(folder.listFiles()!=null&&folder.listFiles().length>0) {
            for (File sub : folder.listFiles()) {
                if (sub.isDirectory()) {
                    subfoldersNames.add(FilenameUtils.getBaseName(sub.getName()));
                }
            }
        }
        return subfoldersNames;
    }

    public static List<File> getFileListByName(List<File> files,String name){
        List<File> extsFileList=new ArrayList<>();
        Log.d("files",files.toString());
        for(File file:files){
            if(FilenameUtils.getBaseName(file.getName()).toLowerCase()
                    .contains(name.toLowerCase())){
                extsFileList.add(file);
            }
        }
        Log.d("fsh by name",extsFileList.toString());
        return  extsFileList;
    }

    private static class SortFileName implements Comparator<File> {
        @Override
        public int compare(File f1, File f2) {
            return f1.getName().compareTo(f2.getName());
        }
    }

    private static class SortFileType implements Comparator<File> {
        @Override
        public int compare(File f1, File f2) {

            return FilenameUtils.getExtension(f2.getName())
                    .compareTo(FilenameUtils.getExtension(f1.getName()));
        }
    }
}
