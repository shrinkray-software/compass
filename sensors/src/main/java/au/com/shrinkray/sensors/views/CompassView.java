package au.com.shrinkray.sensors.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

import au.com.shrinkray.sensors.R;

/**
 * Created by neal on 28/01/2014.
 */
public class CompassView extends View {

    private float mAzimuth;

    private Bitmap mBackgroundBitmap;
    private Paint mBackgroundBitmapPaint;
    private Rect mBackgroundBitmapSrcRect;
    private Rect mBackgroundBitmapDestRect;
    private Paint mMagneticNorthPaint;

    private float mDeclination;

    public CompassView(Context context) {
        super(context);
    }

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CompassView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawARGB(255,255,255,255);

        // Translate the canvas to the centre.
        canvas.translate(canvas.getWidth()/2,canvas.getHeight()/2);

        if ( mBackgroundBitmap == null ) {
            BitmapDrawable mBackgroundDrawable =
                    (BitmapDrawable) getContext().getResources().getDrawable(R.drawable.compass_background);

            mBackgroundBitmap = mBackgroundDrawable.getBitmap();
            mBackgroundBitmapSrcRect = new Rect(0,0,mBackgroundBitmap.getWidth(),mBackgroundBitmap.getHeight());
            mBackgroundBitmapDestRect =
                    new Rect(-canvas.getWidth()/2,-canvas.getHeight()/2,canvas.getWidth()/2,canvas.getHeight()/2);
            mBackgroundBitmapPaint = new Paint();
            mMagneticNorthPaint = new Paint();
            mMagneticNorthPaint.setColor(Color.RED);
            mMagneticNorthPaint.setStrokeWidth(2);
        }

        // Declination is variance from true North of magnetic North,
        canvas.rotate( - mAzimuth + mDeclination );

        canvas.drawBitmap(mBackgroundBitmap,mBackgroundBitmapSrcRect,
                mBackgroundBitmapDestRect,mBackgroundBitmapPaint);

        canvas.rotate( - mDeclination );

        canvas.drawLine(0,-canvas.getWidth(),0,0,mMagneticNorthPaint);


    }

    /**
     * Set the azimuth in degrees.
     *
     * @param azimuth
     */
    public void setAzimuth(float azimuth) {
        mAzimuth = azimuth;
        postInvalidate();
    }

    public void setDeclination(float declination) {
        mDeclination = declination;
        postInvalidate();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


}
