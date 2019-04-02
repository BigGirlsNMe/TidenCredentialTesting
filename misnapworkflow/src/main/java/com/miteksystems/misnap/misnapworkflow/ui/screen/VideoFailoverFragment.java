package com.miteksystems.misnap.misnapworkflow.ui.screen;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.miteksystems.misnap.events.TextToSpeechEvent;
import com.miteksystems.misnap.misnapworkflow.R;

import de.greenrobot.event.EventBus;

public class VideoFailoverFragment extends Fragment{
    private static final String KEY_WILL_CAPTURE_CHECK = "KEY_WILL_CAPTURE_CHECK";
    private OnFragmentInteractionListener mListener;
    private boolean mWillCaptureCheck;
    private boolean mButtonPressed;

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
        void onContinueToManualCapture();
        void onAbortCapture();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PlaceholderFragment.
     */
    public static VideoFailoverFragment newInstance(boolean willCaptureCheck) {
        VideoFailoverFragment fragment = new VideoFailoverFragment();
        Bundle args = new Bundle();
        args.putBoolean(KEY_WILL_CAPTURE_CHECK, willCaptureCheck);

        fragment.setArguments(args);
        return fragment;
    }

    public VideoFailoverFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWillCaptureCheck = getArguments().getBoolean(KEY_WILL_CAPTURE_CHECK);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.misnap_failover_tutorial, container, false);

        try {
            ImageView tutorialImage = (ImageView) rootView.findViewById(R.id.failoverHelpScr);
            if (mWillCaptureCheck) {
                tutorialImage.setImageResource(R.drawable.misnap_failover_scr_check);
                tutorialImage.setContentDescription(getString(R.string.misnap_tts_failover_check));
            } else {
                tutorialImage.setImageResource(R.drawable.misnap_failover_scr_document);
                tutorialImage.setContentDescription(getString(R.string.misnap_tts_failover_document));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Button bCancelBtn = (Button) rootView.findViewById(R.id.failover_cancel_btn);
        bCancelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //pass the control to the activity
                if (mListener != null && !mButtonPressed) {
                    mButtonPressed = true;
                    mListener.onAbortCapture();
                }
            }
        });

        Button bContinueBtn = (Button) rootView.findViewById(R.id.failover_continue_btn);
        bContinueBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //pass the control to the activity
                if (mListener != null && !mButtonPressed) {
                    mButtonPressed = true;
                    mListener.onContinueToManualCapture();
                }
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        int spokenTextId = mWillCaptureCheck ? R.string.misnap_tts_failover_check : R.string.misnap_tts_failover_document;
        EventBus.getDefault().post(new TextToSpeechEvent(spokenTextId));
    }

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
}
