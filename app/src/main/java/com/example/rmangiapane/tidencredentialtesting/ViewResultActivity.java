package com.example.rmangiapane.tidencredentialtesting;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.miteksystems.misnap.events.OnFrameProcessedEvent;
import com.miteksystems.misnap.params.MiSnapAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ViewResultActivity extends Activity implements OnClickListener {

    private Bitmap imageBitmap;
    private TouchImageView mTouchImageView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // hide the title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Suppress keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // set content
        setContentView(R.layout.view_result);

        Button menuButton = (Button) findViewById(R.id.menuButton);
        menuButton.setOnClickListener(this);

        mTouchImageView = (TouchImageView) findViewById(R.id.result_screen_touchimageview);
        mTouchImageView.setMaxZoom(4f);
        LoadImage();
        if (!mHasImageBeenLoaded) {
            mTouchImageView.setVisibility(View.GONE);
        }

        EditText resultCode = (EditText) findViewById(R.id.misnap_app_result_code);
        resultCode.setText(getIntent().getStringExtra(MiSnapAPI.RESULT_CODE));

        EditText warnings = (EditText) findViewById(R.id.misnap_app_warnings);
        ArrayList<Integer> warningCodes = getIntent().getIntegerArrayListExtra(MiSnapAPI.RESULT_WARNINGS);
        warnings.setText(getWarningCodeMessage(warningCodes));

        EditText mibiData = (EditText) findViewById(R.id.misnap_app_mibi_data);
        mibiData.setText(getIntent().getStringExtra(MiSnapAPI.RESULT_MIBI_DATA));
    }


    //@Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.menuButton:
                //startActivity(new Intent(this, HomeActivity.class));
                finish();
                break;
        }
    }

	/* METHODS =============================================================*/

    private boolean mHasImageBeenLoaded = false;

    private void LoadImage() {

        if (mHasImageBeenLoaded) return;

        // check the initial result for an image
        byte[] rawImage = this.getIntent().getByteArrayExtra(MiSnapAPI.RESULT_PICTURE_DATA);
        if (rawImage == null || rawImage.length == 0) {
            return;
        }

        if (rawImage != null && rawImage.length > 0) {
            imageBitmap = formBitmapImage(rawImage);
            mTouchImageView.setImageBitmap(imageBitmap);
            mHasImageBeenLoaded = true;
        } else {
            //new GetPhoneTransactionTask().execute();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mTouchImageView.setVisibility(View.GONE);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mTouchImageView.setVisibility(View.VISIBLE);
        }
    }

    private Bitmap formBitmapImage(byte[] byteImage) {
        System.gc();
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = 1;    // downscale by 2 as it's just a review

        Bitmap sourceBmp = BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length, options);
        int height = sourceBmp.getHeight();
        int width = sourceBmp.getWidth();


        Bitmap targetBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas targetCanvas = new Canvas(targetBmp);

        // draw original bitmap into mutable bitmap
        targetCanvas.drawBitmap(sourceBmp, 0, 0, null);

        sourceBmp.recycle();    // no use anymore

        // draw boxes
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setTextSize(20);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);

        return targetBmp;
    }

    private String getWarningCodeMessage(List<Integer> warningCodes) {
        StringBuilder result = new StringBuilder();

        for (int warningCode : warningCodes) {
            switch (warningCode) {
                case OnFrameProcessedEvent.FRAME_CONFIDENCE_CHECK:
                    result.append(this.getResources().getString(R.string.misnap_failure_reason_four_corner_confidence));
                    break;
                case OnFrameProcessedEvent.FRAME_HORIZONTAL_MINFILL_CHECK:
                    result.append(this.getResources().getString(R.string.misnap_failure_reason_horizontal_min_fill));
                    break;
                case OnFrameProcessedEvent.FRAME_MIN_BRIGHTNESS_CHECK:
                    result.append(this.getResources().getString(R.string.misnap_failure_reason_min_brightness));
                    break;
                case OnFrameProcessedEvent.FRAME_MAX_BRIGHTNESS_CHECK:
                    result.append(this.getResources().getString(R.string.misnap_failure_reason_max_brightness));
                    break;
                case OnFrameProcessedEvent.FRAME_MAX_SKEW_ANGLE_CHECK:
                    result.append(this.getResources().getString(R.string.misnap_failure_reason_skew_angle));
                    break;
                case OnFrameProcessedEvent.FRAME_SHARPNESS_CHECK:
                    result.append(this.getResources().getString(R.string.misnap_failure_reason_sharpness));
                    break;
                case OnFrameProcessedEvent.FRAME_MIN_PADDING_CHECK:
                    result.append(this.getResources().getString(R.string.misnap_failure_reason_min_padding));
                    break;
                case OnFrameProcessedEvent.FRAME_ROTATION_ANGLE_CHECK:
                    result.append(this.getResources().getString(R.string.misnap_failure_reason_rotation_angle));
                    break;
                case OnFrameProcessedEvent.FRAME_LOW_CONTRAST_CHECK:
                    result.append(this.getResources().getString(R.string.misnap_failure_reason_low_contrast));
                    break;
                case OnFrameProcessedEvent.FRAME_BUSY_BACKGROUND_CHECK:
                    result.append(this.getResources().getString(R.string.misnap_failure_reason_busy_background));
                    break;
                case OnFrameProcessedEvent.FRAME_WRONG_DOCUMENT_CHECK:
                    result.append(this.getResources().getString(R.string.misnap_failure_reason_wrong_doc_generic));
                    break;
                case OnFrameProcessedEvent.FRAME_GLARE_CHECK:
                    result.append(this.getResources().getString(R.string.misnap_failure_reason_glare));
                    break;
            }
            result.append('\n');
        }
        return result.toString();
    }
}