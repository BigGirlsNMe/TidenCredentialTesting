package com.miteksystems.misnap.misnapworkflow.ui.screen;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.miteksystems.misnap.events.TextToSpeechEvent;
import com.miteksystems.misnap.misnapworkflow.R;

import de.greenrobot.event.EventBus;


public class ManualHelpFragment extends Fragment {
    private static final String KEY_WILL_CAPTURE_CHECK = "KEY_WILL_CAPTURE_CHECK";
    private static final int TTS_DELAY_MS = 2000;
    private OnFragmentInteractionListener mListener;
    private boolean mButtonPressed;
    private boolean mWillCaptureCheck;

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
        void onManualHelpRestartMiSnapSession();
        void onManualHelpAbortMiSnap();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PlaceholderFragment.
     */
    public static ManualHelpFragment newInstance(boolean willCaptureCheck) {
        ManualHelpFragment fragment = new ManualHelpFragment();
        Bundle args = new Bundle();
        args.putBoolean(KEY_WILL_CAPTURE_CHECK, willCaptureCheck);

        fragment.setArguments(args);
        return fragment;
    }

    public ManualHelpFragment() {
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
        View rootView = inflater.inflate(R.layout.misnap_manual_help_tutorial, container, false);

        TextView message1 = (TextView) rootView.findViewById(R.id.misnap_manual_help_message_1);
        TextView message2 = (TextView) rootView.findViewById(R.id.misnap_manual_help_message_2);

        message2.setText(Html.fromHtml(getString(R.string.misnap_manual_help_message_2)));
        if (mWillCaptureCheck) {
            message1.setText(getString(R.string.misnap_manual_help_message_1_check));
        } else {
            message1.setText(getString(R.string.misnap_manual_help_message_1_document));
        }

        Button bCancelBtn = (Button) rootView.findViewById(R.id.manual_help_cancel_btn);
        bCancelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //pass the control to the activity
                if (mListener != null && !mButtonPressed) {
                    mButtonPressed = true;
                    mListener.onManualHelpAbortMiSnap();
                }
            }
        });

        Button bContinueBtn = (Button) rootView.findViewById(R.id.manual_help_continue_btn);
        bContinueBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //pass the control to the activity
                if (mListener != null && !mButtonPressed) {
                    mButtonPressed = true;
                    mListener.onManualHelpRestartMiSnapSession();
                }
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        StringBuilder stringBuilder = new StringBuilder();
        if (mWillCaptureCheck) {
            stringBuilder.append(getString(R.string.misnap_manual_help_message_1_check));
            stringBuilder.append(Html.fromHtml(getString(R.string.misnap_manual_help_message_2)));
        } else {
            stringBuilder.append(getString(R.string.misnap_manual_help_message_1_document));
            stringBuilder.append(Html.fromHtml(getString(R.string.misnap_manual_help_message_2)));
        }

        EventBus.getDefault().post(new TextToSpeechEvent(stringBuilder.toString(), TTS_DELAY_MS));
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
