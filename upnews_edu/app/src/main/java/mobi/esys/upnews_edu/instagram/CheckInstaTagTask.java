package mobi.esys.upnews_edu.instagram;


import android.os.AsyncTask;
import android.util.Log;

import net.londatiga.android.instagram.InstagramRequest;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import mobi.esys.upnews_edu.UpnewsApp;
import mobi.esys.upnews_edu.constants.Folders;
import mobi.esys.upnews_edu.constants.TimeConsts;
import mobi.esys.upnews_edu.eventbus.EventIgCheckingComplete;
import mobi.esys.upnews_edu.eventbus.EventIgLoadingComplete;
import mobi.esys.upnews_edu.net.NetMonitor;


public class CheckInstaTagTask extends AsyncTask<String, Void, List<InstagramItem>> {
    private transient String mHashTag;
    private transient UpnewsApp mApp;
    private transient boolean needFull;

    private final int MAX_PAGES = TimeConsts.PAGINATION_MAX_PAGES;

    private EventBus bus = EventBus.getDefault();

    public CheckInstaTagTask(String hashTag, UpnewsApp app, boolean needFull) {
        mHashTag = hashTag;
        mApp = app;
        this.needFull = needFull;
    }

    @Override
    protected List<InstagramItem> doInBackground(String... params) {
        List<InstagramItem> photos = new ArrayList<>();
        String response = "";
        if (mHashTag.length() >= 2) {
            if (NetMonitor.isNetworkAvailable(mApp)) {
                try {
                    final InstagramRequest request = new InstagramRequest(params[0]);

                    String edTag = mHashTag.toLowerCase();

                    String max_tag_id = "";
                    boolean hasNext = true;

                    for (int i = 0; (i < MAX_PAGES) && hasNext; i++) {

                        List<NameValuePair> reqParams = new ArrayList<NameValuePair>();
                        reqParams.add(new BasicNameValuePair("count", String.valueOf(100)));
                        if (!max_tag_id.isEmpty()) {
                            reqParams.add(new BasicNameValuePair("max_tag_id", max_tag_id));
                        }
                        response = request.requestGet("/users/self/media/recent", reqParams);

                        if (isJSONValid(response)) {
                            JSONObject resObject = new JSONObject(response);
                            if (resObject.has("meta") && resObject.getJSONObject("meta").getInt("code") == 200) {
                                Log.d("unTag_CheckInstaTag", "Instagram tag is valid.");
                                if (resObject.has("pagination") && resObject.getJSONObject("pagination").has("next_max_tag_id")) {
                                    max_tag_id = resObject.getJSONObject("pagination").getString("next_max_tag_id");
                                } else {
                                    hasNext = false;
                                }
                                JSONArray results = resObject.getJSONArray("data");
                                for (int j = 0; j < results.length(); j++) {
                                    JSONObject result = results.getJSONObject(j);
                                    if (result.getString("type").equals("image")) {

                                        JSONArray tags = result.getJSONArray("tags");
                                        boolean proceed = false;
                                        for (int k = 0; k < tags.length(); k++) {
                                            if(edTag.equals(tags.getString(k))){
                                                proceed = true;
                                                break;
                                            }
                                        }
                                        if (!proceed) {
                                            continue;
                                        }

                                        String id = result.getString("id");
                                        JSONObject images = result.getJSONObject("images");

                                        JSONObject thumbnail = images.getJSONObject("thumbnail");
                                        String thumbnailUrl = thumbnail.getString("url");
                                        int end = thumbnailUrl.indexOf("?");
                                        if (end != -1) {
                                            thumbnailUrl = thumbnailUrl.substring(0, end);
                                        }

                                        InstagramItem igItem = new InstagramItem(id, thumbnailUrl, null);
                                        photos.add(igItem);
                                        if (!needFull) {
                                            hasNext = false;
                                            break;
                                        }
                                    }
                                }
                            } else {
                                hasNext = false;
                            }
                        } else {
                            hasNext = false;
                        }
                    }
                } catch (Exception e) {
                    Log.d("unTag_CheckInstaTag", "Error checking");
                }
            } else {
                Log.d("unTag_CheckInstaTag", "No inet, can't check instagram tag");
            }
        }

        return photos;
    }

    @Override
    protected void onPostExecute(List<InstagramItem> photos) {
        bus.post(new EventIgCheckingComplete(photos));
        if (photos.size() <= 0) {
            Log.d("unTag_CheckInstaTag", "Can't load from instagram. Use cached.");
            String folder = Folders.SD_CARD.concat(File.separator).
                    concat(Folders.BASE_FOLDER).
                    concat(File.separator).
                    concat(Folders.PHOTO_FOLDER);
            File[] folderList = new File(folder).listFiles();
            if (folderList.length > 0) {
                bus.post(new EventIgLoadingComplete());
            } else {
                Log.d("unTag_CheckInstaTag", "No cached files!");
            }
        }
        super.onPostExecute(photos);
    }

    public boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }
}

