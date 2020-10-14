package android.support.motionlayout.experiments;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.constraintlayout.motion.widget.Debug;
import androidx.constraintlayout.motion.widget.MotionHelper;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.R;

import java.util.Timer;
import java.util.TimerTask;

public class MotionBlur extends MotionHelper {
    private static final String TAG = "MotionBlur";
    RectF []boxes = new RectF[10];
    Bitmap[]images = new Bitmap[10];
    float []rotation = new float[10];
    int box_off = 0;
    Paint mPaint = new Paint();
    {
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.RED);
    }
    private ConstraintLayout mContainer;
    View[] mViews = new View[0]; // used to reduce the getViewById() cost

    public MotionBlur(Context context) {
        super(context);
        myContext = context;
     }

    public MotionBlur(Context context, AttributeSet attrs) {
        super(context, attrs);
        myContext = context;
        init(attrs);
    }

    public MotionBlur(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
      }
    /**
     * @param attrs
     * @hide
     */
    protected void init(AttributeSet attrs) {
        super.init(attrs);
        mUseViewMeasure = false;
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ConstraintLayout_Layout);
            final int N = a.getIndexCount();
            for (int i = 0; i < N; i++) {
                int attr = a.getIndex(i);
                // read attributes
            }
        }
        Log.v(TAG,Debug.getLoc()+"mReferenceIds = "+ mReferenceIds);

    }


    /**
     * @param container
     * @hide
     */
    @Override
    public void updatePreDraw(ConstraintLayout container) {
        mContainer = container;
    }

    /**
     * Helpers typically reference a collection of ids
     * @return ids referenced
     */
    public void setReferencedIds(int[] ids) {
        Log.v(TAG,Debug.getLoc()+"setReferencedIds = "+Debug.getName(this.getContext(),ids));
    }

    private void reCacheViews() {
        Log.v(TAG,Debug.getLoc()+"  " );

        if (mContainer == null) {
            return;
        }
        if (mCount == 0) {
            return;
        }

        if (mViews == null || mViews.length != mCount) {
            mViews = new View[mCount];
        }
        for (int i = 0; i < mCount; i++) {
            int id = mIds[i];
            mViews[i] = mContainer.getViewById(id);
            Log.v(TAG,Debug.getLoc()+"  "+Debug.getName(this.getContext(),id) );

        }
    }

    @Override
    public void onTransitionStarted(MotionLayout motionLayout, int startId, int endId) {
        reCacheViews();

        Log.v(TAG,Debug.getLoc()+"startId = "+ Debug.getName(motionLayout.getContext(),startId)+" endId ="+Debug.getName(motionLayout.getContext(),endId));
//        for (int i = 0; i < mCount; i++) {
//            mViews[i].setVisibility(INVISIBLE);
//        }
    }
    @Override
    public void onTransitionChange(MotionLayout motionLayout, int startId, int endId, float progress) {
        getBox();
        Log.v(TAG, Debug.getLoc()+"startId = "+ Debug.getName(motionLayout.getContext(),startId)+" endId ="+Debug.getName(motionLayout.getContext(),endId));
//         for (int i = 0; i < mCount; i++) {
//            mViews[i].setVisibility((System.currentTimeMillis()%60)>30? INVISIBLE:VISIBLE);
//        }
    }
    private void getBox() {
        int off = box_off% boxes.length;
        if (boxes[off] == null) {
            boxes[off] = new RectF();
        }
        boxes[off].top = mViews[0].getTop();
        boxes[off].bottom = mViews[0].getBottom();
        boxes[off].left = mViews[0].getLeft();
        boxes[off].right = mViews[0].getRight();
//        images[off] =   Bitmap.createBitmap(boxes[off].width(),boxes[off].height());
        rotation[off] = mViews[0].getRotation();
        box_off++;
    }

    @Override
    public void onTransitionCompleted(MotionLayout motionLayout, int currentId) {
        Log.v(TAG,Debug.getLoc()+"startId = "+ Debug.getName(motionLayout.getContext(),currentId) );
//        for (int i = 0; i < mCount; i++) {
//            mViews[i].setVisibility(VISIBLE);
//        }
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {

            Runnable runnable = new Runnable() {
                int count=0;
                @Override
                public void run() {
                    getBox();
                    count++;
                    Log.v(TAG, " .(MotionBlur.java:156) "+" timer task "+count);

                    motionLayout.invalidate();
                    if (count > boxes.length) {
                        Log.v(TAG, " .(MotionBlur.java:158) "+" timer task "+count);
                        for (int b = 0; b < boxes.length; b++) {
                            boxes[b] = null;

                        }
                        timer.cancel();
                    }
                }
            };
            @Override
            public void run() {
                // use runOnUiThread(Runnable action)
                motionLayout.post(runnable);
            }
        }, 16,16);

    }

    public boolean isDecorator() {
        return true;
    }
    public void onPreDraw(Canvas canvas) {
        Log.v(TAG,  Debug.getLoc()+" "+System.currentTimeMillis());

        for (int i = box_off; i >= 0 && i > box_off-boxes.length; i--) {
         int off = i%boxes.length;
         if (boxes[off] == null) {
             continue;
         }

           int saveCount = canvas.save();  // save canvas state
         canvas.rotate(rotation[off],(boxes[off].left+boxes[off].right)/2,(boxes[off].top+boxes[off].bottom)/2);
           canvas.drawRect(boxes[off],mPaint);
            canvas.restoreToCount(saveCount);
        }

    }

}
