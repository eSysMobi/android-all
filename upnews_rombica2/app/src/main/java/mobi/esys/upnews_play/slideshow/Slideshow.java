package mobi.esys.upnews_play.slideshow;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Random;

import mobi.esys.upnews_play.R;

public class Slideshow {
    private int animDuration;
    private Context context;
    private List<File> picFiles;
    private ImageView slider;

    private transient Handler slideHandler;
    private transient Runnable sliderRunnable;
    private transient int sliderIndex=0;

    private transient int[] insAnimations={android.R.anim.fade_in,
            android.R.anim.slide_in_left, R.anim.scale_up};
    private transient int[] outsAnimations={android.R.anim.fade_out,
            android.R.anim.slide_out_right,R.anim.scale_down};


    public Slideshow(final int animDuration,final Context context,
                     final List<File> picFiles,final ImageView slider) {
        this.animDuration = animDuration;
        this.context=context;
        this.picFiles=picFiles;
        this.slider=slider;
    }

    public void startAnimation(){
        slideHandler=new Handler();
        sliderRunnable=new Runnable() {
            @Override
            public void run() {
                imageAnimatedChange(context,slider,picFiles.get(sliderIndex));
                increaseSliderIndex();
                slideHandler.postDelayed(sliderRunnable,animDuration);
            }
        };
        slideHandler.postDelayed(sliderRunnable,1);
    }

    public void stopAnimation(){
        if(slideHandler!=null){
            slideHandler.removeCallbacks(sliderRunnable);
        }
    }

    private void imageAnimatedChange(Context context, final ImageView imageView, final File new_image) {
        Random random=new Random();
        int index=random.nextInt(insAnimations.length);
        final Animation anim_out = AnimationUtils.loadAnimation(context, outsAnimations[index]);
        final Animation anim_in  = AnimationUtils.loadAnimation(context, insAnimations[index]);
        anim_out.setAnimationListener(new Animation.AnimationListener()
        {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation)
            {

                Bitmap bitmap=null;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                try {
                   bitmap = BitmapFactory.decodeStream(
                            new FileInputStream(new_image), null, options);

                } catch (FileNotFoundException ignored) {
                }



                imageView.setImageBitmap(bitmap);
                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) {}
                    @Override public void onAnimationRepeat(Animation animation) {}
                    @Override public void onAnimationEnd(Animation animation) {}
                });
                imageView.startAnimation(anim_in);
            }
        });
        imageView.startAnimation(anim_out);
    }

    private void increaseSliderIndex(){
        sliderIndex++;
        if(sliderIndex>picFiles.size()-1){
            sliderIndex=0;
        }
    }

}
