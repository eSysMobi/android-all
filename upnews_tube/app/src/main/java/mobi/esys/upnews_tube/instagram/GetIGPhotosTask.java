package mobi.esys.upnews_tube.instagram;


import android.os.AsyncTask;

import net.londatiga.android.instagram.InstagramRequest;

public class GetIGPhotosTask extends AsyncTask<String, Void, String> {
    private transient String tag;

    public GetIGPhotosTask(String tag) {
        this.tag = tag;
    }

    @Override
    protected String doInBackground(final String... params) {
        String response = "";

        final InstagramRequest request = new InstagramRequest(params[0]);

        try {
            response = request.requestGet("tags/" + tag + "/media/recent/", null);
        } catch (Exception ignored) {
        }

        return response;
    }

}
