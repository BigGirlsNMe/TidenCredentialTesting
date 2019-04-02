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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.miteksystems.misnap.events.TextToSpeechEvent;
import com.miteksystems.misnap.misnapworkflow.R;
import com.miteksystems.misnap.misnapworkflow.storage.MiSnapPreferencesManager;
import com.miteksystems.misnap.params.MiSnapApiConstants;

import de.greenrobot.event.EventBus;


public class FTManualTutorialFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {
    private static final String KEY_DOCTYPE = "KEY_DOCTYPE";
    private static final int TTS_DELAY_MS = 7000; // Increased delay to account for system saying app name and landscape mode.
    private OnFragmentInteractionListener mListener;
    private String mDocType;
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
        // TODO: Update argument type and name
        void onFTManualTutorialDone();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PlaceholderFragment.
     */
    public static FTManualTutorialFragment newInstance(String docType) {
    	FTManualTutorialFragment fragment = new FTManualTutorialFragment();
        Bundle args = new Bundle();
        args.putString(KEY_DOCTYPE, docType);

        fragment.setArguments(args);
        return fragment;
    }

    public FTManualTutorialFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean dontShowAgain) {
        MiSnapPreferencesManager.setIsFirstTimeUserManual(getContext(), !dontShowAgain, mDocType);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDocType = getArguments().getString(KEY_DOCTYPE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment based on doc type
        View rootView;
        if (isIdDocument(mDocType)) {
            rootView = inflater.inflate(R.layout.misnap_manual_first_time_tutorial_ids, container, false);

            TextView message = (TextView) rootView.findViewById(R.id.misnap_manual_ft_message);
            message.setText(Html.fromHtml(getString(R.string.misnap_manual_ft_message_2)));

            ImageView tutorialImage = (ImageView) rootView.findViewById(R.id.misnap_tutorial_image);
            switch (mDocType) {
                case MiSnapApiConstants.PARAMETER_DOCTYPE_PASSPORT:
                    tutorialImage.setImageResource(R.drawable.misnap_help_passport_plain);
                    break;
                case MiSnapApiConstants.PARAMETER_DOCTYPE_ID_CARD_FRONT:
                case MiSnapApiConstants.PARAMETER_DOCTYPE_DRIVER_LICENSE:
                    tutorialImage.setImageResource(R.drawable.misnap_help_id_plain);
                    break;
                case MiSnapApiConstants.PARAMETER_DOCTYPE_ID_CARD_BACK:
                    tutorialImage.setImageResource(R.drawable.misnap_help_id_back_plain);
                    break;
            }
        } else {
            rootView = inflater.inflate(R.layout.misnap_manual_first_time_tutorial_non_ids, container, false);
            TextView message1 = (TextView) rootView.findViewById(R.id.misnap_manual_ft_message_1);
            TextView message2 = (TextView) rootView.findViewById(R.id.misnap_manual_ft_message_2);

            message2.setText(Html.fromHtml(getString(R.string.misnap_manual_ft_message_2)));
            if (isCheck(mDocType)) {
                message1.setText(getString(R.string.misnap_manual_ft_message_1_check));
            } else {
                message1.setText(getString(R.string.misnap_manual_ft_message_1_document));
            }
        }
        
        //get the confirmation button handle
        Button bFTConfirmationBtn = (Button)rootView.findViewById(R.id.ft_manual_tut_btn);
        bFTConfirmationBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //pass the control to the activity
                if (mListener != null && !mButtonPressed) {
                    mButtonPressed = true;
                    mListener.onFTManualTutorialDone();
                }
            }
        });

        CheckBox checkBoxDontShowAgain = (CheckBox) rootView.findViewById(R.id.checkbox_dont_show_again);
        if (!isIdDocument(mDocType)) {
            checkBoxDontShowAgain.setVisibility(View.GONE);
        } else {
            checkBoxDontShowAgain.setOnCheckedChangeListener(this);
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        StringBuilder stringBuilder = new StringBuilder();
        if(isCheck(mDocType)){
            stringBuilder.append(getString(R.string.misnap_manual_ft_message_1_check));
            stringBuilder.append(Html.fromHtml(getString(R.string.misnap_manual_ft_message_2)));
        } else if(isIdDocument(mDocType)){
            stringBuilder.append(Html.fromHtml(getString(R.string.misnap_manual_ft_message_2)));
        } else {
            stringBuilder.append(getString(R.string.misnap_manual_ft_message_1_document));
            stringBuilder.append(Html.fromHtml(getString(R.string.misnap_manual_ft_message_2)));
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

    private boolean isCheck(String docType){
        return docType.startsWith(MiSnapApiConstants.PARAMETER_DOCTYPE_CHECK_FRONT)
                || docType.startsWith(MiSnapApiConstants.PARAMETER_DOCTYPE_CHECK_BACK)
                || docType.startsWith(MiSnapApiConstants.PARAMETER_DOCTYPE_AUTOMATIC_CHECK_HANDLING);
    }

    private boolean isIdDocument(String docType) {
        return docType.startsWith(MiSnapApiConstants.PARAMETER_DOCTYPE_DRIVER_LICENSE)
                || docType.startsWith(MiSnapApiConstants.PARAMETER_DOCTYPE_ID_CARD_FRONT)
                || docType.startsWith(MiSnapApiConstants.PARAMETER_DOCTYPE_ID_CARD_BACK)
                || docType.startsWith(MiSnapApiConstants.PARAMETER_DOCTYPE_PASSPORT);
    }
}
