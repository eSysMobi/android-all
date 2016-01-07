package mobi.esys.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import mobi.esys.consts.ISConsts;
import mobi.esys.upnews_hashtag.R;

/**
 * Created by ZeyUzh on 03.01.2016.
 */
public class DrawProgress {
    private Bitmap screenBackground = null;
    private Bitmap littleDot = null;
    private Bitmap progressLine = null;
    private static int dots[] = new int[4];
    private int backgroundWidth;
    private int backgroundHeight;
    private int color = Color.GREEN;
    private int backgroundColor = Color.GRAY;

    public DrawProgress(){
        backgroundWidth = ISConsts.progressSizes.screenWidth/3;
        backgroundHeight = (int) (ISConsts.progressSizes.screenWidth * ISConsts.progressSizes.progressDotDelta * 2);

        for (int i=0; i<4; i++){
            float tmp = ISConsts.progressSizes.screenWidth*ISConsts.progressSizes.progressDotDelta
                    + i*(ISConsts.progressSizes.screenWidth/3-2*ISConsts.progressSizes.screenWidth*ISConsts.progressSizes.progressDotDelta)/3;
            dots[i] = (int) tmp;
        }
    }

    public DrawProgress(int newBackgroundColor, int newColor){
        backgroundWidth = ISConsts.progressSizes.screenWidth/3;
        backgroundHeight = (int) (ISConsts.progressSizes.screenWidth * ISConsts.progressSizes.progressDotDelta * 2);

        for (int i=0; i<4; i++){
            float tmp = ISConsts.progressSizes.screenWidth*ISConsts.progressSizes.progressDotDelta
                    + i*(ISConsts.progressSizes.screenWidth/3-2*ISConsts.progressSizes.screenWidth*ISConsts.progressSizes.progressDotDelta)/3;
            dots[i] = (int) tmp;
        }

        color = newColor;
        backgroundColor = newBackgroundColor;
    }


    public Bitmap getScreenBackground() {
        if (screenBackground == null) {
            int radius = ISConsts.progressSizes.progressDotSize;
            screenBackground = Bitmap.createBitmap(ISConsts.progressSizes.screenWidth, ISConsts.progressSizes.screenHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(screenBackground);

            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(backgroundColor);
            canvas.drawRect(0,0,ISConsts.progressSizes.screenWidth,ISConsts.progressSizes.screenHeight,paint);

            paint.setColor(Color.WHITE);
            int halfHeight = (int)(ISConsts.progressSizes.screenHeight/2);

            for (int i=0; i<4; i++){
                canvas.drawCircle(ISConsts.progressSizes.progressDots[i], halfHeight, radius, paint);
            }

            float lineHalfThickness = ISConsts.progressSizes.progressLineSize;
            canvas.drawRect(ISConsts.progressSizes.progressDots[0], halfHeight - lineHalfThickness, ISConsts.progressSizes.progressDots[3], halfHeight + lineHalfThickness, paint);
        }
        return screenBackground;
    }

    public Bitmap getLittleDotProgress() {
        if (littleDot == null) {
            int radius = ISConsts.progressSizes.progressLineSize;
            littleDot = Bitmap.createBitmap(radius*2, radius*2, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(littleDot);

            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(color);

            canvas.drawCircle(radius, radius, radius, paint);
        }
        return littleDot;
    }

    public Bitmap getProgressLine() {
        if (progressLine == null) {
            int thickness = (int) (ISConsts.progressSizes.progressLineSize*0.8);
            progressLine = Bitmap.createBitmap(2, thickness*2, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(progressLine);

            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(color);

            canvas.drawRect(0, 0, 2, thickness*2, paint);
        }
        return progressLine;
    }

}
