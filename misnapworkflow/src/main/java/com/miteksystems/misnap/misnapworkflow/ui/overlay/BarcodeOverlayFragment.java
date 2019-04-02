package com.miteksystems.misnap.misnapworkflow.ui.overlay;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.miteksystems.misnap.events.OnTorchStateEvent;
import com.miteksystems.misnap.events.TextToSpeechEvent;
import com.miteksystems.misnap.events.TorchStateEvent;
import com.miteksystems.misnap.misnapworkflow.R;

import de.greenrobot.event.EventBus;

/**
 * The barcode reader activity itself. This is loosely based on the
 * CameraPreview example included in the Android SDK.
 */
public class BarcodeOverlayFragment extends Fragment implements View.OnClickListener {

	public static final OverlayMode OVERLAY_MODE = OverlayMode.OM_MWOVERLAY;
	private static final int SEC_TO_MS = 1000;
    private static final int TTS_DELAY_MS = 2000;
    private static final int GHOST_IMAGE_TIMEOUT = 10 * SEC_TO_MS;
	private static final boolean enableGhostImage = true;

	private ImageButton buttonFlash;
	private Handler timeoutHandler;


	boolean flashOn = false;
	boolean flashSupported;

	private enum OverlayMode {
		OM_IMAGE, OM_MWOVERLAY, OM_NONE
	}


	Runnable mBarcodeGhostImageTimeout = new Runnable() {
		@Override
		public void run() {
            MWOverlay.hideOverlay();
			getView().findViewById(R.id.imageOverlay).setVisibility(View.VISIBLE);
			getView().findViewById(R.id.misnap_barcode_tooltip).setVisibility(View.VISIBLE);
            EventBus.getDefault().post(new TextToSpeechEvent(R.string.misnap_ghost_barcode_tooltip, TTS_DELAY_MS));
		}
	};

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.misnap_barcode_overlay, container, false);

        if (OVERLAY_MODE == OverlayMode.OM_IMAGE) {
            ImageView imageOverlay = (ImageView) rootView.findViewById(R.id.imageOverlay);
            imageOverlay.setVisibility(View.VISIBLE);
        }

        buttonFlash = (ImageButton) rootView.findViewById(R.id.flashButton);
        buttonFlash.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.flashButton){
            toggleFlash();
        }
    }

    @Override
	public void onResume() {
		super.onResume();

        Log.i("TAG", "onResume");

		if (OVERLAY_MODE == OverlayMode.OM_MWOVERLAY) {
			MWOverlay.addOverlay(getActivity(), getView().findViewById(R.id.imageOverlay));//surfaceView);
		}

		if(enableGhostImage){
			timeoutHandler = new Handler();
			timeoutHandler.postDelayed(mBarcodeGhostImageTimeout, GHOST_IMAGE_TIMEOUT);
		}
		EventBus.getDefault().register(this);
		EventBus.getDefault().post(new TorchStateEvent("GET"));
	}

	@Override
	public void onPause() {
		super.onPause();
        Log.i("TAG", "onPause");

		if (EventBus.getDefault().isRegistered(this)) {
			EventBus.getDefault().unregister(this);
		}

		if (OVERLAY_MODE == OverlayMode.OM_MWOVERLAY) {
			MWOverlay.removeOverlay();
		}

		if(enableGhostImage){
			timeoutHandler.removeCallbacksAndMessages(null);
			timeoutHandler = null;
		}

		flashOn = false;

		updateFlash();
	}

    @Override
    public void onDestroy(){
        super.onDestroy();
        MWOverlay.resetOverlay();
    }

	public void onEventMainThread(OnTorchStateEvent event) {
		switch (event.currentTorchState) {
			case -1:
				flashOn = false;
				flashSupported = false;
				break;
			case 0:
				flashOn = false;
				flashSupported = true;
				break;
			case 1:
				flashOn = true;
				flashSupported = true;
				break;
		}

		updateFlash();
	}

	private void toggleFlash() {
		flashOn = !flashOn;
		EventBus.getDefault().post(new TorchStateEvent("SET", flashOn));
	}

	private void updateFlash() {
		if (!flashSupported) {
			buttonFlash.setVisibility(View.GONE);
			return;
		} else {
			buttonFlash.setVisibility(View.VISIBLE);
		}

		if (flashOn) {
			buttonFlash.setImageResource(R.drawable.misnap_barcode_flashbuttonon);
		} else {
			buttonFlash.setImageResource(R.drawable.misnap_barcode_flashbuttonoff);
		}

		buttonFlash.postInvalidate();
	}
}
