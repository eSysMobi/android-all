package mobi.esys.view;

import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by ZeyUzh on 26.06.2016.
 */
public class PhotoElement {
    private RelativeLayout relativeLayout;
    private ImageView imageView;
    private TextView textView;

    public PhotoElement(RelativeLayout relativeLayout, ImageView imageView, TextView textView) {
        this.relativeLayout = relativeLayout;
        this.imageView = imageView;
        this.textView = textView;
    }

    public RelativeLayout getRelativeLayout() {
        return relativeLayout;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public TextView getTextView() {
        return textView;
    }
}
