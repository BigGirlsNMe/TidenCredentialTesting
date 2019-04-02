package com.miteksystems.misnap.misnapworkflow.ui.overlay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.miteksystems.misnap.events.AutoFocusOnceEvent;
import com.miteksystems.misnap.misnapworkflow.R;
import com.miteksystems.misnap.misnapworkflow.params.WorkflowParameterReader;
import com.miteksystems.misnap.params.ParameterManager;
import com.miteksystems.misnap.params.ParamsHelper;
import com.miteksystems.misnap.utils.Utils;

import de.greenrobot.event.EventBus;

/**
 * Created by awood on 9/9/2015.
 */
public class YourCameraOverlayFragment extends Fragment
        implements View.OnClickListener {

    private static final String TAG = YourCameraOverlayFragment.class.getSimpleName();
    public static final String KEY_LAYOUT_ID = "KEY_LAYOUT_ID";

    private OnFragmentInteractionListener mListener;
    private CameraOverlay mCameraOverlay;
    private UIManager mUiManager;
    private int mNumTapsToFocus;

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onHelpButtonClicked();
        void onCancelButtonClicked();
        void onTorchButtonClicked(boolean shouldTurnOn);
        void onCaptureButtonClicked();
    }

    // Will default to use R.layout.misnap_your_camera_overlay
    public static YourCameraOverlayFragment newInstance() {
        YourCameraOverlayFragment fragment = new YourCameraOverlayFragment();
        return fragment;
    }

    public static YourCameraOverlayFragment newInstance(int layoutId) {
        YourCameraOverlayFragment fragment = new YourCameraOverlayFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_LAYOUT_ID, layoutId);

        fragment.setArguments(args);
        return fragment;
    }

    // Required empty constructor
    public YourCameraOverlayFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int layoutId = R.layout.misnap_your_camera_overlay;
        if (getArguments() != null) {
            layoutId = getArguments().getInt(KEY_LAYOUT_ID, R.layout.misnap_your_camera_overlay);
        }

        // TODO: This ParameterManager is even clunkier now. Needs refactoring.
        Intent intent = getActivity().getIntent();
        ParameterManager miSnapParamsMgr = new ParameterManager();
        miSnapParamsMgr.verifyThem(ParamsHelper.getJobSettings(intent), 0);
        WorkflowParameterReader workflowParams = new WorkflowParameterReader(intent);
        mCameraOverlay = new CameraOverlay(getActivity(), miSnapParamsMgr, workflowParams, this, layoutId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mCameraOverlay = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        mUiManager = new UIManager(getActivity(), mCameraOverlay);
        mUiManager.initialize();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");

        if (mCameraOverlay != null) {
            mCameraOverlay.getViewTreeObserver().removeGlobalOnLayoutListener(mLayoutListener);
        }
        mLayoutListener = null;

        if (mUiManager != null) {
            mUiManager.cleanup();
            mUiManager = null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Set a Global Layout Listener to tell us when the Views have been measured.
        // This way we can scale the overlay images properly.
        mCameraOverlay.getViewTreeObserver().addOnGlobalLayoutListener(mLayoutListener);
        mCameraOverlay.setOnClickListener(this);

        return mCameraOverlay;
    }

    ViewTreeObserver.OnGlobalLayoutListener mLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if (mCameraOverlay != null && mCameraOverlay.getWidth() > 0) {
                mCameraOverlay.getViewTreeObserver().removeGlobalOnLayoutListener(mLayoutListener);
                mCameraOverlay.initialize();
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    // Handle overlay button presses
    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.misnap_overlay_help_button) {
            // Disable the button so that the user can't press it multiple times
            view.setEnabled(false);
            view.setClickable(false);
            // Alert the state machine that the Help button was pressed.
            mListener.onHelpButtonClicked();
        } else if (id == R.id.overlay_flash_toggle) {
            // Toggle torch button is pressed, so get the Torch state and invert it
            mListener.onTorchButtonClicked(!mCameraOverlay.getTorchStatus());
        } else if (id == R.id.overlay_cancel_button) {
            // Cancel button is pressed
            //disable the button so that user can't press multiple times
            view.setEnabled(false);
            view.setClickable(false);
            // Alert the state machine that the Cancel button was pressed
            mListener.onCancelButtonClicked();
        } else if (id == R.id.misnap_overlay_capture_button) {
            // Disable the button so that the user can't press it multiple times
            view.setEnabled(false);
            view.setClickable(false);
            mCameraOverlay.showManualCapturePressedPleaseWait(true);
            // Alert the state machine that the Manual Capture button was pressed
            mListener.onCaptureButtonClicked();
        } else {
            //uxp event
            Utils.uxpEvent(getActivity(), R.string.misnap_uxp_touch_screen, ++mNumTapsToFocus);

            EventBus.getDefault().post(new AutoFocusOnceEvent());
        }
    }

    @VisibleForTesting
    CameraOverlay getCameraOverlay() {
        return mCameraOverlay;
    }
}
