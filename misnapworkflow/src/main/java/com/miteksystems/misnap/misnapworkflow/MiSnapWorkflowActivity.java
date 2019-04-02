package com.miteksystems.misnap.misnapworkflow;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.miteksystems.misnap.events.OnShutdownEvent;
import com.miteksystems.misnap.misnapworkflow.ui.overlay.YourCameraOverlayFragment;
import com.miteksystems.misnap.misnapworkflow.ui.screen.FTManualTutorialFragment;
import com.miteksystems.misnap.misnapworkflow.ui.screen.FTVideoTutorialFragment;
import com.miteksystems.misnap.misnapworkflow.ui.screen.ManualHelpFragment;
import com.miteksystems.misnap.misnapworkflow.ui.screen.VideoDetailedFailoverFragment;
import com.miteksystems.misnap.misnapworkflow.ui.screen.VideoFailoverFragment;
import com.miteksystems.misnap.misnapworkflow.ui.screen.VideoHelpFragment;
import com.miteksystems.misnap.misnapworkflow.ui.screen.VideoTimeoutFragment;
import com.miteksystems.misnap.misnapworkflow.workflow.UxStateMachine;
import com.miteksystems.misnap.params.MiSnapIntentCheck;
import com.miteksystems.misnap.params.ParamsHelper;
import com.miteksystems.misnap.utils.Utils;
import com.miteksystems.misnap.params.MiSnapAPI;
import de.greenrobot.event.EventBus;

public class MiSnapWorkflowActivity extends AppCompatActivity implements
        FTVideoTutorialFragment.OnFragmentInteractionListener,
        FTManualTutorialFragment.OnFragmentInteractionListener,
        VideoTimeoutFragment.OnFragmentInteractionListener,
        VideoFailoverFragment.OnFragmentInteractionListener,
        VideoDetailedFailoverFragment.OnFragmentInteractionListener,
        VideoHelpFragment.OnFragmentInteractionListener,
        ManualHelpFragment.OnFragmentInteractionListener,
        YourCameraOverlayFragment.OnFragmentInteractionListener {
    private static final String TAG = MiSnapWorkflowActivity.class.getSimpleName();
    private UxStateMachine mUxStateMachine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (MiSnapIntentCheck.isDangerous(getIntent())) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        // B-03373: Prevent Intent manipulation vulnerabilities
        // Verify that the starting Intent is not null before proceeding


        // Set the application flags/settings here
        // NOTE: You must call these BEFORE calling setContentView!
        // Go full screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Prevent screenshots and viewing on non-secure displays
        if (!ParamsHelper.areScreenshotsAllowed(getIntent())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
            }
        }

        // Unlock the screen when running unit and regression tests
        if (BuildConfig.DEBUG) {
            Utils.unlockAndTurnOnScreen(this);
        }

        // NOTE: Make sure you've set the flags first.
        setContentView(R.layout.misnap_activity_misnapworkflow);

        // GPU optimization - prevents unnecessary frame overdraws
        getWindow().setBackgroundDrawable(null);

        mUxStateMachine = new UxStateMachine(this);
        // if(getIntent().hasExtra("EXTRA_ENABLE_LEAK_DETECTION")){
        //     CanaryCreator creator = new CanaryCreator(getApplication());
        // }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUxStateMachine != null) {
            mUxStateMachine.destroy();
            mUxStateMachine = null;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        // You can optionally pass in a locale to override the user's current locale
//        LocaleHelper.changeLanguage(this, "es"); // e.g. Spanish
        mUxStateMachine.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUxStateMachine.pause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (!mUxStateMachine.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        setResult(resultCode, data);

        String miSnapReason = data.getStringExtra(MiSnapAPI.RESULT_CODE);
        EventBus.getDefault().post(new OnShutdownEvent(resultCode, miSnapReason)); // Send STOP event to MiSnap
    }


    /**************************************************************************
     * All events from fragments are forwarded from this Activity
     * to the state machine for processing.
     */

    // From FTVideoTutorialFragment
    @Override
    public void onFTVideoTutorialDone() {
        mUxStateMachine.onFirstTimeVideoTutorialFragmentDone();
    }

    // From FTManualTutorialFragment
    @Override
    public void onFTManualTutorialDone() {
        mUxStateMachine.onFirstTimeManualTutorialFragmentDone();
    }

    // From VideoTimeoutFragment
    @Override
    public void onRetryAfterTimeout() {
        mUxStateMachine.onRetryAfterTimeout();
    }
    @Override
    public void onAbortAfterTimeout() {
        mUxStateMachine.onAbortAfterTimeout();
    }

    // From VideoFailoverFragment
    @Override
    public void onContinueToManualCapture() {
        mUxStateMachine.onContinueToManualCapture();
    }
    @Override
    public void onAbortCapture() {
        mUxStateMachine.onAbortCapture();
    }

    // From VideoDetailedFailoverFragment
    @Override
    public void onManualCaptureAfterDetailedFailover() {
        mUxStateMachine.onManualCaptureAfterDetailedFailover();
    }
    @Override
    public void onAbortAfterDetailedFailover() {
        mUxStateMachine.onAbortAfterDetailedFailover();
    }
    @Override
    public void onRetryAfterDetailedFailover() {
        mUxStateMachine.onRetryAfterDetailedFailover();
    }

    // From VideoHelpFragment
    @Override
    public void onVideoHelpRestartMiSnapSession() {
        mUxStateMachine.onVideoHelpRestartMiSnapSession();
    }
    @Override
    public void onVideoHelpAbortMiSnap() {
        mUxStateMachine.onVideoHelpAbortMiSnap();
    }

    // From ManualHelpFragment
    @Override
    public void onManualHelpRestartMiSnapSession() {
        mUxStateMachine.onManualHelpRestartMiSnapSession();
    }
    @Override
    public void onManualHelpAbortMiSnap() {
        mUxStateMachine.onManualHelpAbortMiSnap();
    }

    // From YourCameraOverlayFragment
    @Override
    public void onHelpButtonClicked() {
        mUxStateMachine.onHelpButtonClicked();
    }
    @Override
    public void onCancelButtonClicked() {
        mUxStateMachine.onCancelButtonClicked();
    }
    @Override
    public void onTorchButtonClicked(boolean shouldTurnOn) {
        mUxStateMachine.onTorchButtonClicked(shouldTurnOn);
    }
    @Override
    public void onCaptureButtonClicked() {
        mUxStateMachine.onCaptureButtonClicked();
    }
}
