package mobi.esys.upnews_tube;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import mobi.esys.upnews_tube.constants.DevelopersKeys;
import mobi.esys.upnews_tube.constants.OtherConst;
import mobi.esys.upnews_tube.constants.TimeConsts;
import mobi.esys.upnews_tube.system.RecyclerViewAdapter;
import mobi.esys.upnews_tube.system.YoutubePlaylistElement;


public class YouTubeSelectActivity extends Activity {

    private SharedPreferences prefs;
    private String mYouTubeChannelName;
    private ImageButton ibYouTubeSelect;
    private TextView tvYouTubeSelect_2;
    private List<YoutubePlaylistElement> channelList;
    private RecyclerView rvYouTubeSelect;
    private ProgressBar pbYouTubeSelect;
    private TextView tvYouTubeSelectNoInet;
    private LinearLayoutManager llm;

    //autostart
    private final Handler startHandler = new Handler();
    private Runnable youtubePlaylistAutostartRunnable;
    private boolean userInterrupt = false;

//get id channel from channel name
//https://www.googleapis.com/youtube/v3/channels?part=snippet&forUsername=USER_NAME&key=API_KEY
//https://www.googleapis.com/youtube/v3/channels?part=snippet&forUsername=Northernlion&key=AIzaSyAd_TjlIQ606ypYNLuJANzKP8AnSIKJu9Q

//get playlists from channel id
//https://www.googleapis.com/youtube/v3/playlists?part=snippet&channelId=UC3tNpTOHsTnkmbwztCs30sA&maxResults=50&order=date&type=playlist&key=AIzaSyAd_TjlIQ606ypYNLuJANzKP8AnSIKJu9Q
//https://www.googleapis.com/youtube/v3/search?part=id%2C+snippet&channelId=UC3tNpTOHsTnkmbwztCs30sA&maxResults=50&order=date&type=playlist&key=AIzaSyAd_TjlIQ606ypYNLuJANzKP8AnSIKJu9Q

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_you_tube_select);

        channelList = new ArrayList<>();

        tvYouTubeSelect_2 = (TextView) findViewById(R.id.tvYouTubeSelect_2);
        ibYouTubeSelect = (ImageButton) findViewById(R.id.ibYouTubeSelect);
        rvYouTubeSelect = (RecyclerView) findViewById(R.id.rvYouTubeSelect);
        pbYouTubeSelect = (ProgressBar) findViewById(R.id.pbYouTubeSelect);
        tvYouTubeSelectNoInet = (TextView) findViewById(R.id.tvYouTubeSelectNoInet);

        llm = new LinearLayoutManager(this);
        rvYouTubeSelect.setLayoutManager(llm);

        prefs = getSharedPreferences(OtherConst.APP_PREF, MODE_PRIVATE);
        mYouTubeChannelName = prefs.getString("YouTubeChannelName", "");

        ibYouTubeSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userInterrupt = true;
                stopAutostart();
                Intent intent = new Intent(YouTubeSelectActivity.this, EditYouTubeChannelName.class);
                intent.putExtra("lastChannelName", mYouTubeChannelName);
                startActivityForResult(intent, 422);
            }
        });

        if (!mYouTubeChannelName.isEmpty()) {
            GetYoutubePlaylists getPlaylists = new GetYoutubePlaylists();
            getPlaylists.execute();
            tvYouTubeSelect_2.setText(mYouTubeChannelName);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 422 && resultCode == RESULT_OK) {
            if (data == null) {
                return;
            }
            String name = data.getStringExtra("name");
            tvYouTubeSelect_2.setText(name);
            mYouTubeChannelName = name;

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("YouTubeChannelName", name);
            editor.apply();

            GetYoutubePlaylists getPlaylists = new GetYoutubePlaylists();
            getPlaylists.execute();
        }
    }

    class GetYoutubePlaylists extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            channelList.clear();
            tvYouTubeSelectNoInet.setVisibility(View.GONE);
            rvYouTubeSelect.setVisibility(View.GONE);
            pbYouTubeSelect.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean resultTask = false;
            HttpURLConnection urlConnection = null;
            String nextPageToken;
            try {
                String address = "https://www.googleapis.com/youtube/v3/channels?part=snippet&forUsername=" + mYouTubeChannelName + "&key=" + DevelopersKeys.YOUTUBE_KEY;
                Log.d("unTag_YouTubeSelectAct", "Request get channel ID to Youtube = " + address);
                URL url = new URL(address);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                //String result = convertStreamToString(in);
                String result = getIDChannel(in);
                in.close();
                Log.d("unTag_YouTubeSelectAct", "Id channel=" + result);

                if (!result.isEmpty()) {
                    //https://www.googleapis.com/youtube/v3/playlists?part=snippet&channelId=UC3tNpTOHsTnkmbwztCs30sA&maxResults=50&order=date&type=playlist&key=AIzaSyAd_TjlIQ606ypYNLuJANzKP8AnSIKJu9Q
                    String playlistsRequest = "https://www.googleapis.com/youtube/v3/playlists?part=snippet&channelId=" + result + "&maxResults=50&order=date&type=playlist&key=" + DevelopersKeys.YOUTUBE_KEY;
                    Log.d("unTag_YouTubeSelectAct", "Request get playlists to Youtube = " + playlistsRequest);
                    url = new URL(playlistsRequest);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    in = new BufferedInputStream(urlConnection.getInputStream());
                    nextPageToken = getPlaylistElements(in);
                    while (!nextPageToken.isEmpty()) {
                        playlistsRequest = "https://www.googleapis.com/youtube/v3/playlists?part=snippet&channelId=" + result + "&maxResults=50&order=date&type=playlist&key=" + DevelopersKeys.YOUTUBE_KEY + "&pageToken=" + nextPageToken;
                        Log.d("unTag_YouTubeSelectAct", "Request get playlists to Youtube = " + playlistsRequest);
                        url = new URL(playlistsRequest);
                        urlConnection = (HttpURLConnection) url.openConnection();
                        in = new BufferedInputStream(urlConnection.getInputStream());
                        nextPageToken = getPlaylistElements(in);
                    }

                } else {
                    Log.d("unTag_YouTubeSelectAct", "Can't get playlists because no id channel");
                }
                resultTask = true;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return resultTask;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                if (channelList.size() == 0) {
                    pbYouTubeSelect.setVisibility(View.GONE);
                    rvYouTubeSelect.setVisibility(View.GONE);
                    tvYouTubeSelectNoInet.setText(R.string.incorrect_youtube_username);
                    tvYouTubeSelectNoInet.setVisibility(View.VISIBLE);
                } else {
                    pbYouTubeSelect.setVisibility(View.GONE);
                    rvYouTubeSelect.setVisibility(View.VISIBLE);
                    setAdatperToRV();
                    autostart();
                }
            } else {
                pbYouTubeSelect.setVisibility(View.GONE);
                rvYouTubeSelect.setVisibility(View.GONE);
                tvYouTubeSelectNoInet.setText(R.string.no_inet);
                tvYouTubeSelectNoInet.setVisibility(View.VISIBLE);
            }
        }

        private String getPlaylistElements(InputStream inputStream) throws IOException {
            if (inputStream != null) {
                BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                String nextPage = "";
                String line = "";
                String playlistID = "";
                String playlistName = "";
                while ((line = r.readLine()) != null) {
                    if (line.contains("nextPageToken")) {
                        nextPage = line.substring(line.indexOf(":") + 3);
                        nextPage = nextPage.substring(0, nextPage.indexOf("\""));
                        Log.d("unTag_YouTubeSelectAct", "Have a next page token=" + nextPage);
                    }
                    //collect elements
                    if (line.contains("\"id\":")) {
                        playlistID = line.substring(line.indexOf(":") + 3);
                        //playlistID = playlistID.substring(0, playlistID.indexOf("\""));
                        playlistID = playlistID.substring(0, playlistID.length() - 2);
                        //Log.d("unTag_YouTubeSelectAct", "Playlist ID=" + playlistID);
                    }
                    if (line.contains("\"title")) {
                        if (!playlistID.isEmpty()) {
                            playlistName = line.substring(line.indexOf(":") + 3);
                            //playlistName = playlistName.substring(0, playlistName.lastIndexOf("\""));
                            playlistName = playlistName.substring(0, playlistName.length() - 2);
                            playlistName = playlistName.replace("\\", "");
                            //Log.d("unTag_YouTubeSelectAct", "Playlist name=" + playlistName);
                            channelList.add(new YoutubePlaylistElement(playlistName, playlistID));
                            playlistID = "";
                        }
                    }
                }
                return nextPage;
            } else {
                return "";
            }
        }

        private String getIDChannel(InputStream inputStream) throws IOException {
            if (inputStream != null) {
                BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                String result = "";
                String line;
                while ((line = r.readLine()) != null) {
                    if (line.contains("id\":")) {
                        result = line.substring(line.lastIndexOf(":") + 3);
                        result = result.substring(0, result.indexOf("\""));
                        break;
                    }
                }
                return result;
            } else {
                return "";
            }
        }
    }

    void setAdatperToRV() {
        Log.d("unTag_YouTubeSelectAct", "Size of channelList = " + channelList.size());
        RecyclerViewAdapter rvAdapter = new RecyclerViewAdapter(channelList, this, prefs);
        if (rvYouTubeSelect.getAdapter() == null) {
            Log.d("lg", "New adapter to rvYouTubeSelect");
            rvYouTubeSelect.setAdapter(rvAdapter);
        } else {
            Log.d("lg", "Swap adapter to rvYouTubeSelect");
            rvYouTubeSelect.swapAdapter(rvAdapter, true);
        }
        for (int i = 0; i < channelList.size(); i++) {
            if (channelList.get(i).getId().equals(prefs.getString(OtherConst.APP_PREF_PLAYLIST, ""))) {
                llm.scrollToPosition(i);
            }
        }
    }

    static String convertStreamToString(InputStream inputStream) throws IOException {
        if (inputStream != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 1024);
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                inputStream.close();
            }
            return writer.toString();
        } else {
            return "";
        }
    }

    private void autostart() {
        //if(!prefs.getString(OtherConst.APP_PREF_PLAYLIST,"").isEmpty() && !prefs.getString("instHashTag", "").isEmpty()){
        if (!prefs.getString(OtherConst.APP_PREF_PLAYLIST, "").isEmpty() && !userInterrupt) {
            youtubePlaylistAutostartRunnable = new Runnable() {
                @Override
                public void run() {
                    Log.d("unTag_YouTubeSelectAct", "Autostart PlayerActivityYouTube");
                    startActivity(new Intent(YouTubeSelectActivity.this, PlayerActivityYouTube.class));
                    //finish();
                }
            };
            startHandler.postDelayed(youtubePlaylistAutostartRunnable, TimeConsts.AUTOSTART);
        }
    }

    private void stopAutostart(){
        if (startHandler != null && youtubePlaylistAutostartRunnable != null) {
            Log.d("unTag_YouTubeSelectAct", "Cancel autostart PlayerActivityYouTube");
            startHandler.removeCallbacks(youtubePlaylistAutostartRunnable);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopAutostart();
    }
}
