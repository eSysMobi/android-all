package mobi.esys.upnews_tube.twitter;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.text.Spanned;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import mobi.esys.upnews_tube.R;


public class TwitterLine implements ViewSwitcher.ViewFactory {
    private transient RelativeLayout mParentView;
    private transient TextSwitcher textSwitcher;
    private transient Context mContext;
    private transient boolean isTextSwitcherInitialized;
    private transient Handler twitterLineHandler;
    private transient Runnable twitterLineRunnable;
    private transient int tweetsCounter;
    private transient List<String> mTwProfImagesUrls;
    private transient ImageView profileImage;
    private transient boolean isFirst;


    public TwitterLine(RelativeLayout parentView,
                       Context context,
                       List<String> twProfImagesUrls,
                       boolean isFirst) {
        mParentView = parentView;
        mContext = context;
        isTextSwitcherInitialized = false;
        mTwProfImagesUrls = twProfImagesUrls;
        tweetsCounter = 0;
        this.isFirst=isFirst;
    }

    public void start(final List<Spanned> textToSwitch) {
        if (isTextSwitcherInitialized) {
            removeTextSwitcher();
        }
        initTextSwitcher();

        twitterLineHandler = new Handler();
        twitterLineRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    textSwitcher.setText(textToSwitch.get(tweetsCounter));
                    twitterLineHandler.postDelayed(this, 10000);

                    Glide.with(mContext).load(mTwProfImagesUrls.get(tweetsCounter)).diskCacheStrategy(DiskCacheStrategy.ALL).fitCenter().error(R.drawable.twitter_128).into(profileImage);
                    if (tweetsCounter == textToSwitch.size() - 1) {
                        tweetsCounter = 0;
                    } else {
                        tweetsCounter++;
                    }
                }
                catch (Exception e){
                    Log.d("esys_upn","Can not load twits");
                }

            }
        };

        twitterLineHandler.postDelayed(twitterLineRunnable, 10000);

    }

    @Override
    public View makeView() {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        TextView textView = new TextView(mContext);
        textView.setLayoutParams(layoutParams);
        textView.setTextColor(Color.WHITE);
        textView.setGravity(Gravity.LEFT);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        textView.setMaxLines(4);
        textView.setMaxWidth(650);
        return textView;
    }

    private void initTextSwitcher() {
        if(!isFirst) {
            mParentView.removeAllViews();
        }
        textSwitcher = new TextSwitcher(mContext);
        textSwitcher.setFactory(this);

        textSwitcher.setInAnimation(AnimationUtils.loadAnimation(mContext, android.R.anim.fade_in));
        textSwitcher.setOutAnimation(AnimationUtils.loadAnimation(mContext, android.R.anim.fade_out));

        mParentView.setBackgroundColor(mContext.getResources().getColor(R.color.rss_line));

        profileImage = new ImageView(mContext);
        profileImage.setId(R.id.profileImage);


        RelativeLayout.LayoutParams piLp = new RelativeLayout.LayoutParams(80, 80);
        piLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        piLp.addRule(RelativeLayout.CENTER_VERTICAL);
        piLp.setMargins(10, 5, 3, 5);
        profileImage.setLayoutParams(piLp);

        RelativeLayout.LayoutParams tsiLp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        tsiLp.addRule(RelativeLayout.RIGHT_OF, R.id.profileImage);
        tsiLp.addRule(RelativeLayout.CENTER_VERTICAL);
        tsiLp.setMargins(10, 1, 0, 1);
        textSwitcher.setLayoutParams(tsiLp);


        mParentView.addView(profileImage);
        mParentView.addView(textSwitcher);

        isTextSwitcherInitialized = true;
    }


    public void removeTextSwitcher() {
        mParentView.removeAllViews();
        isTextSwitcherInitialized = false;
        tweetsCounter = 0;
    }
}
