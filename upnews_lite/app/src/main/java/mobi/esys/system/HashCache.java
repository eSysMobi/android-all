package mobi.esys.system;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZeyUzh on 28.04.2016.
 */
public class HashCache {
    private String name;
    private String md5;

    public HashCache(){
        name = "";
        md5 = "";
    }

    public HashCache(String incName, String incMD5){
        name = incName;
        md5 = incMD5;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getName() {
        return name;
    }

    public String getMd5() {
        return md5;
    }
}
