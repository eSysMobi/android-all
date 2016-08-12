package mobi.esys.tasks;

import android.os.AsyncTask;
import android.util.Log;

import net.londatiga.android.instagram.InstagramRequest;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import mobi.esys.consts.ISConsts;
import mobi.esys.eventbus.EventGetTagsIGComplete;
import mobi.esys.instagram.InstagramItem;

/**
 * Created by Артем on 29.04.2015.
 */
public class GetTagsIGTask extends AsyncTask<String, Void, List<InstagramItem>> {
    private transient String mHashTag;
    private transient String mIgToken;

    private final int MAX_PAGES = ISConsts.instagramconsts.PAGINATION_MAX_PAGES;

    private EventBus bus = EventBus.getDefault();

    public GetTagsIGTask(String hashTag, String igToken) {
        mHashTag = hashTag;
        mIgToken = igToken;
    }

    @Override
    protected List<InstagramItem> doInBackground(String... params) {
        List<InstagramItem> photos = new ArrayList<>();
        try {
            final InstagramRequest request = new InstagramRequest(mIgToken);

            String edTag = mHashTag.toLowerCase();

            String max_tag_id = "";
            boolean hasNext = true;

            for (int i = 0; (i < MAX_PAGES) && hasNext; i++) {

                List<NameValuePair> reqParams = new ArrayList<NameValuePair>();
                reqParams.add(new BasicNameValuePair("count", String.valueOf(100)));
                if (!max_tag_id.isEmpty()) {
                    reqParams.add(new BasicNameValuePair("max_tag_id", max_tag_id));
                }
                String response = request.requestGet("/tags/" + edTag + "/media/recent", reqParams);

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
                                //get id
                                String id = result.getString("id");

                                //get likes
                                JSONObject likes = result.getJSONObject("likes");
                                int likesCount = likes.getInt("count");

                                //get urls
                                JSONObject images = result.getJSONObject("images");

//                                JSONObject thumbnail = images.getJSONObject("thumbnail");
//                                String thumbnailUrl = thumbnail.getString("url");
//                                int end = thumbnailUrl.indexOf("?");
//                                if (end != -1) {
//                                    thumbnailUrl = thumbnailUrl.substring(0, end);
//                                }

                                JSONObject standard = images.getJSONObject("standard_resolution");
                                String standardUrl = standard.getString("url");
                                int end = standardUrl.indexOf("?");
                                if (end != -1) {
                                    standardUrl = standardUrl.substring(0, end);
                                }


                                InstagramItem igItem = new InstagramItem(id, null, standardUrl, likesCount);
                                photos.add(igItem);
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

        return photos;
    }

    @Override
    protected void onPostExecute(List<InstagramItem> photos) {
        bus.post(new EventGetTagsIGComplete(photos));
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