package mobi.esys.dastarhan.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import mobi.esys.dastarhan.Constants;
import mobi.esys.dastarhan.R;
import mobi.esys.dastarhan.Restaurants;

/**
 * Created by ZeyUzh on 19.05.2016.
 */
public class RVCuisinesAdapter extends RecyclerView.Adapter<RVCuisinesAdapter.CuisinesViewHolder> {
    private Cursor cursor;
    private Context mContext;
    private SQLiteDatabase db;
    private String locale;

    //constructor
    public RVCuisinesAdapter(DatabaseHelper dbHelper, Context mContext, String locale) {
        this.mContext = mContext;
        this.locale = locale;
        db = dbHelper.getReadableDatabase();
        cursor = db.query(Constants.DB_TABLE_CUISINES, null, null, null, null, null, null);
    }

    //preparing ViewHolder
    public static class CuisinesViewHolder extends RecyclerView.ViewHolder {
        TextView tvCuisine;
        ImageView ivCuisine;
        int cuisine_id = 0;

        CuisinesViewHolder(View itemView) {
            super(itemView);
            tvCuisine = (TextView) itemView.findViewById(R.id.tvCuisine);
            ivCuisine = (ImageView) itemView.findViewById(R.id.ivCuisine);
        }
    }

    @Override
    public RVCuisinesAdapter.CuisinesViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.element_cuisine_recyclerview, viewGroup, false);
        CuisinesViewHolder cuisinesViewHolder = new CuisinesViewHolder(v);
        return cuisinesViewHolder;
    }

    @Override
    public void onBindViewHolder(RVCuisinesAdapter.CuisinesViewHolder viewHolder, int i) {
        if (i == 0) {
            viewHolder.tvCuisine.setText(R.string.all_cuisines);
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("dTagRecyclerView", "Choose ALL CUISINES in RecyclerView");
                    Intent intent = new Intent(mContext, Restaurants.class);
                    intent.putExtra("restID",-42);
                    mContext.startActivity(intent);
                }
            });
        } else {
            cursor.moveToPosition(i - 1);
            viewHolder.cuisine_id = cursor.getInt(cursor.getColumnIndex("server_id"));
            if (locale.equals("ru")) {
                viewHolder.tvCuisine.setText(cursor.getString(cursor.getColumnIndex("ru_name")));
            } else {
                viewHolder.tvCuisine.setText(cursor.getString(cursor.getColumnIndex("en_name")));
            }
            //viewHolder.ivCuisine.setImageBitmap(...);

            CustomClickListener customClickListener = new CustomClickListener(mContext, viewHolder.cuisine_id);
            viewHolder.itemView.setOnClickListener(customClickListener);
        }

        if (i == getItemCount() - 1) {
            //cursor.close();
            db.close();
        }
    }

    private static class CustomClickListener implements View.OnClickListener {
        private int id;
        private Context mContext;

        public CustomClickListener(Context mContext, int id){
            this.mContext = mContext;
            this.id = id;
        }

        @Override
        public void onClick(View v) {
            Log.d("dTagRecyclerView", "Choose CUISINE in RecyclerView with id = " + id);
            Intent intent = new Intent(mContext, Restaurants.class);
            intent.putExtra("cuisineID",id);
            mContext.startActivity(intent);
        }
    }

    @Override
    public int getItemCount() {
        return cursor.getCount() + 1;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}