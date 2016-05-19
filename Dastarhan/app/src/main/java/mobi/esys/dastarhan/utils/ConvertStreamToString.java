package mobi.esys.dastarhan.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by ZeyUzh on 18.05.2016.
 */
public class ConvertStreamToString {

    public static String getString(InputStream is){
        String result = "";

        try {
            InputStreamReader isr = new InputStreamReader(is);

            BufferedReader br = new BufferedReader(isr);
            String line = "";
            StringBuilder responseOutput = new StringBuilder();
            while((line = br.readLine()) != null ) {
                responseOutput.append(line);
            }
            br.close();
            result = responseOutput.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
