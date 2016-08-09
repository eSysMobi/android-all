package mobi.esys.upnews_tube.system;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import mobi.esys.upnews_tube.InstagramLoginActivity;
import mobi.esys.upnews_tube.R;
import mobi.esys.upnews_tube.constants.OtherConst;

/**
 * Created by ZeyUzh on 17.04.2016.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    List<YoutubePlaylistElement> elements;
    Context mContext;
    SharedPreferences mPrefs;

    //constructor
    public RecyclerViewAdapter(List<YoutubePlaylistElement> incomingElements, Context mContext, SharedPreferences prefs) {
        this.elements = incomingElements;
        this.mContext = mContext;
        this.mPrefs = prefs;
    }

    //preparing ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mtvElementTitle;
        View mitemView;
        String playlistID;

        ViewHolder(View itemView, Drawable dr) {
            super(itemView);
            mitemView = itemView;
            itemView.setClickable(true);
            itemView.setFocusable(true);
            itemView.setFocusableInTouchMode(true);
            itemView.setBackground(dr);
            mtvElementTitle = (TextView) itemView.findViewById(R.id.tvElementTitle);
        }
    }

    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.element_of_listview, parent, false);
        ViewHolder viewHolder = new ViewHolder(v, parent.getContext().getResources().getDrawable(R.drawable.youtube_element_background_selector));
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return elements.size();
    }

    @Override
    public void onBindViewHolder(final RecyclerViewAdapter.ViewHolder viewHolder, int i) {
        viewHolder.playlistID = elements.get(i).getId();
        viewHolder.mtvElementTitle.setText(elements.get(i).getName());
        //set last chosen element
        if (elements.get(i).getId().equals(mPrefs.getString(OtherConst.APP_PREF_PLAYLIST, ""))) {
            Log.d("unTag_RecyclerView", "Find last chosen element of RecyclerView. Number " + i);
            //viewHolder.itemView.setSelected(true);
            viewHolder.itemView.requestFocus();
        }
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("unTag_RecyclerView", "onClick on element of RecyclerView. Need go to playlist " + viewHolder.playlistID);
                SharedPreferences.Editor editor = mPrefs.edit();
                editor.putString(OtherConst.APP_PREF_PLAYLIST, viewHolder.playlistID);
                editor.apply();
                Intent nextActivity = new Intent(mContext, InstagramLoginActivity.class);
                mContext.startActivity(nextActivity);
            }
        });
    }

}