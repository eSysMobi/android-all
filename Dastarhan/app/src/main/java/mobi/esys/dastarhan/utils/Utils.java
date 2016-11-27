package mobi.esys.dastarhan.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ZeyUzh on 27.11.2016.
 */
public class Utils {

    public static boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            return false;
        }
        return true;
    }

    public static boolean isJSONArrayValid(String test) {
        try {
            new JSONArray(test);
        } catch (JSONException ex) {
            return false;
        }
        return true;
    }
}
