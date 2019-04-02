package com.miteksystems.misnap.misnapworkflow.ui.overlay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.miteksystems.misnap.misnapworkflow.ui.overlay.OverlayConstants;
import com.miteksystems.misnap.params.SDKConstants;

public class UIManager {
    private static final String TAG = "UIManager";
	Context mContext=null;
	CameraOverlay mCameraOverlay=null;
    static boolean mVibrated=false;
    static boolean mRegisteredRegister=false;
    private MediaPlayer mMediaPlayer; // Fix W/MediaPlayer-JNI(16888): MediaPlayer finalized without being released

	public UIManager(Context lContext, CameraOverlay lCameraOverlay) {
		mContext = lContext;
		mVibrated = false;
        mCameraOverlay = lCameraOverlay;
	}

    public void initialize() {
        //register the broadcaster
        IntentFilter filter = new IntentFilter();
        filter.addAction(SDKConstants.UI_FRAGMENT_BROADCASTER);
        //register the BR
        LocalBroadcastManager.
                getInstance(mContext).
                registerReceiver(mUIBroadcastReceiver, filter);
        mRegisteredRegister = true;
    }

    public void cleanup() {
        Log.d(TAG, "cleanup");
        //unregister the BR
        if(mUIBroadcastReceiver != null && mRegisteredRegister) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mUIBroadcastReceiver);
            mRegisteredRegister = false;
        }

        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        if(mCameraOverlay != null) {
            mCameraOverlay.cleanup();
            mCameraOverlay = null;
        }
        mVibrated = false;
    }

    private BroadcastReceiver mUIBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
            Log.d(TAG, "mUIBroadcastReceiver message received.");

			int lMsgWhat = arg1.getIntExtra(SDKConstants.UI_FRAGMENT_BROADCAST_MESSAGE_ID, 0);
            Log.d(TAG, "mUIBroadcastReceiver: " + lMsgWhat);
			switch(lMsgWhat) {

			case OverlayConstants.UI_FRAGMENT_FREEZE_FRAME_AND_PLAY_ANIMATION:{
				processGoodFrameStuff(arg1);
				break;
			}
            case SDKConstants.UI_FRAGMENT_SHOW_REPLAY_FRAME: {
                hideOverlayButtons();
                if(mCameraOverlay != null) {
                    mCameraOverlay.drawReplayFrame(arg1);
                }
                break;
            }

			case SDKConstants.UI_FRAGMENT_DISPLAY_FPS_DATA:{
				processFPSData(arg1);
				break;
			}

            case OverlayConstants.UI_FRAGMENT_SNAP_BUTTON_CLICKED:{
                processSnapButtonClick();
                break;
            }
		}
	}
    };

    private void processSnapButtonClick() {
        //disable the guide image
        processHideGhostImage(true);
        //disable the UI buttons
        hideOverlayButtons();
    }

    private void hideOverlayButtons() {
        if(mCameraOverlay != null) {
            mCameraOverlay.hideButtons();
        }
    }

	protected void processFPSData(Intent arg1) {
		if(mCameraOverlay != null) {
			//query the camera torch state and set in UI accordingly
			// use the correct ghost image
			mCameraOverlay.showFPSData(arg1.getStringExtra(SDKConstants.UI_FRAGMENT_INTENT_STRING_PARAM1));
		}
	}

	protected void processGoodFrameStuff(Intent lIntent) {
        if(mVibrated == false) {
            mVibrated = true;
            //hide buttons
            hideOverlayButtons();
            if (mCameraOverlay == null) {
                Log.d(TAG, "Camera overlay was null");
            }
            //drawRectangle
            if(mCameraOverlay != null) {
                // use the correct ghost image
                mCameraOverlay.snapshotGood(lIntent);
//                mCameraOverlay.startGaugeFullAnimation();
            }
            // vibrate
            snapShotSoundAndVibrate();
        }
	}

	protected void processHideGhostImage(final boolean lImmediateHide) {
		if(mCameraOverlay != null) {
			mCameraOverlay.removeGhostImage(lImmediateHide);
		}
	}

    void snapShotSoundAndVibrate() {
        Log.w(TAG, "snapShotSoundAndVibrate");
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        // Play system sound
        AudioManager meng = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        final int volume = meng.getStreamVolume( AudioManager.STREAM_NOTIFICATION);

        if (0 != volume) {
            mMediaPlayer = MediaPlayer.create(mContext, SDKConstants.CAMERA_CLICK_SOUND);
            if (mMediaPlayer != null) {
                mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.setVolume(volume, volume);
                        mp.start();
                    }
                });
                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.release();
                    }
                });
            } else {
                // TODO need to have alternative for sound
                Log.w(TAG, SDKConstants.CAMERA_CLICK_SOUND + " still null after create()");
            }
        }

		// Get instance of Vibrator from current Context
		Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
		 
		// The following numbers represent millisecond lengths
		int dot = 100;	  // Length of a Morse Code "dot" in milliseconds
		int dash = 200;	 // Length of a Morse Code "dash" in milliseconds
		int short_gap = 100;	// Length of Gap Between dots/dashes
		long[] pattern = {0, dot, short_gap, dash};

		// Only perform this pattern one time (-1 means "do not repeat")
		v.vibrate(pattern, -1);
	}
}
