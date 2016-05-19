package mobi.esys.dastarhan.utils;

import android.content.Context;
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

/**
 * Created by ZeyUzh on 19.05.2016.
 */
public class RVCuisinesAdapter extends RecyclerView.Adapter<RVCuisinesAdapter.CuisinesViewHolder> {
    private Cursor cursor;
    private Context mContext;
    private SQLiteDatabase db;

    //constructor
    public RVCuisinesAdapter(DatabaseHelper dbHelper, Context mContext) {
        this.mContext = mContext;
        db = dbHelper.getReadableDatabase();
        cursor = db.query(Constants.DB_TABLE_CUISINES, null, null, null, null, null, null);
    }

    //preparing ViewHolder
    public static class CuisinesViewHolder extends RecyclerView.ViewHolder {
        TextView tvCuisine;
        ImageView ivCuisine;

        CuisinesViewHolder(View itemView) {
            super(itemView);
            tvCuisine = (TextView) itemView.findViewById(R.id.tvCuisine);
            ivCuisine = (ImageView) itemView.findViewById(R.id.ivCuisine);
        }
    }

    @Override
    public RVCuisinesAdapter.CuisinesViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.element_of_cuisines_recyclerview, viewGroup, false);
        CuisinesViewHolder cuisinesViewHolder = new CuisinesViewHolder(v);
        return cuisinesViewHolder;
    }

    @Override
    public void onBindViewHolder(RVCuisinesAdapter.CuisinesViewHolder viewHolder, int i) {
        cursor.moveToPosition(i);
        viewHolder.tvCuisine.setText(cursor.getString(cursor.getColumnIndex("ru_name")));
        //viewHolder.ivCuisine.setImageBitmap(...);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("dTagRecyclerView", "onClick on element of RecyclerView.");

            }
        });

        if(i==getItemCount()-1){
            db.close();
        }
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}