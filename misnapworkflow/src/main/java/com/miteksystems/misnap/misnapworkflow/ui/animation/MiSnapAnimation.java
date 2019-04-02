package com.miteksystems.misnap.misnapworkflow.ui.animation;

import android.content.Context;
import android.content.res.TypedArray;
import android.widget.ImageView;

import com.miteksystems.misnap.misnapworkflow.R;

public class MiSnapAnimation  {
    public int FPS = 30;  // animation FPS

    public static FrameSequenceAnimation createBugAnim(ImageView imageView, Context context) {
        TypedArray lXmlRes =
                context.getResources().obtainTypedArray(R.array.bug_animation);
        int[] bugAnimation = new int[lXmlRes.length()];
        for(int i = 0; i < lXmlRes.length(); i++) {
            try {
                bugAnimation[i] = lXmlRes.getResourceId(i,-1);
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return new FrameSequenceAnimation(imageView, bugAnimation, 25);	// rough FPS
    }

    public static FrameSequenceAnimation createBugStill(ImageView imageView, Context context) {
        TypedArray lXmlRes =
                context.getResources().obtainTypedArray(R.array.bug_still);
        int[] bugStill = new int[lXmlRes.length()];
        for(int i = 0; i < lXmlRes.length(); i++) {
            try {
                bugStill[i] = lXmlRes.getResourceId(i,-1);
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return new FrameSequenceAnimation(imageView, bugStill, 1);
    }

    public static FrameSequenceAnimation createGaugeOpenAnim(ImageView imageView, Context context) {
        TypedArray lXmlRes =
                context.getResources().obtainTypedArray(R.array.gauge_open_imgs);
        int[] gaugeOpenAnimation = new int[lXmlRes.length()];
        for(int i = 0; i < lXmlRes.length(); i++) {
            try {
                gaugeOpenAnimation[i] = lXmlRes.getResourceId(i,-1);
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return new FrameSequenceAnimation(imageView, gaugeOpenAnimation, 30);	// rough FPS
    }

    public static FrameSequenceAnimation createGaugeFinishAnim(ImageView imageView, Context context) {
        TypedArray lXmlRes =
                context.getResources().obtainTypedArray(R.array.gauge_finish_imgs);
        int[] gaugeFinishAnimation = new int[lXmlRes.length()];
        for(int i = 0; i < lXmlRes.length(); i++) {
            try {
                gaugeFinishAnimation[i] = lXmlRes.getResourceId(i,-1);
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return new FrameSequenceAnimation(imageView, gaugeFinishAnimation, 25); // rough FPS
    }

    public static FrameSequenceAnimation createGaugeCloseAnim(ImageView imageView, Context context) {
        TypedArray lXmlRes =
                context.getResources().obtainTypedArray(R.array.gauge_close_imgs);
        int[] gaugeCloseAnimation = new int[lXmlRes.length()];
        for(int i = 0; i < lXmlRes.length(); i++) {
            try {
                gaugeCloseAnimation[i] = lXmlRes.getResourceId(i,-1);
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return new FrameSequenceAnimation(imageView, gaugeCloseAnimation, 40); // rough FPS
    }
}