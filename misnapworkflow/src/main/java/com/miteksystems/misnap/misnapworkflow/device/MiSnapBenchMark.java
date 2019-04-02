package com.miteksystems.misnap.misnapworkflow.device;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;

import com.miteksystems.misnap.params.MiSnapAPI;
import com.miteksystems.misnap.params.MiSnapApiConstants;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by asingal on 11/10/2015.
 */
public class MiSnapBenchMark {

    public static void checkDeviceCamera(final Context context, Intent misnapIntent) {
        if (context == null || misnapIntent == null) {
            return;
        }

        boolean testFail=false;

        try {
            //create a Camera Object
            Camera mCamera = getCameraInstance(context);

            //Check if the device supports the required focus modes
            if (!supportsRequiredResolutions(context, mCamera)) {//check if the device supports 108op or 720 resolution
                    testFail = true;
            } else if(!supportsAutoFocusMode(context, mCamera)) {
                testFail = true;
            }

            if(testFail) {
                //change the MiSnap Focus mode to MANUAL
                JSONObject jsonObj = new JSONObject(misnapIntent.getStringExtra(MiSnapAPI.JOB_SETTINGS));
                if(jsonObj != null) {
                    jsonObj.remove(MiSnapAPI.MiSnapCaptureMode);
                    jsonObj.put(MiSnapAPI.MiSnapCaptureMode, MiSnapApiConstants.PARAMETER_CAPTURE_MODE_MANUAL);
                    //put this modified string back in the intent
                    misnapIntent.putExtra(MiSnapAPI.JOB_SETTINGS,jsonObj.toString());
                }
            }
            //release the Camera obj
            release(mCamera);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }


    /** Check if this device has a camera */
    private static boolean cameraHardwareExists(Context context) {
        return context
                .getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /** A safe way to get an instance of the Camera object.
     * @throws Exception */
    private static Camera getCameraInstance(Context context) throws Exception {
        if (!cameraHardwareExists(context)) {
            throw new Exception("MiSnap: Camera Hardware does not exits");
        }

        Camera cameraObj;
        try {
            cameraObj = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("MiSnap: Trouble starting native Camera");
        }
        return cameraObj;
    }

    private static boolean supportsAutoFocusMode(Context context,Camera cameraObj){
        Camera.Parameters camParams = cameraObj.getParameters();
        if (camParams == null) {
            return false;
        }

        boolean supportsAutoFocus=false,supportsContVideoFocus=false,supportsContPictureFocus=false;
        List<String> focusModes = camParams.getSupportedFocusModes();
        if (focusModes != null) {
            //auto-focus
            supportsAutoFocus = focusModes
                    .contains(Camera.Parameters.FOCUS_MODE_AUTO);
            if (!supportsAutoFocus) {
                supportsAutoFocus
                        = context
                        .getPackageManager()
                        .hasSystemFeature("android.hardware.camera.autofocus");
            }
            //continuous video mode
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                supportsContVideoFocus = focusModes
                        .contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }
            //continuous picture mode
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                supportsContPictureFocus = focusModes
                        .contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
        }
        // If autofocus is unsupported, auto-capture is unlikely. Save some time and start in manual mode.
        if (!supportsAutoFocus && !supportsContVideoFocus && !supportsContPictureFocus) {
            return false;
        }

        return true;
    }

    private static boolean supportsRequiredResolutions(Context context,Camera cameraObj) {
        //check if the calculations have already been done
            Camera.Parameters camParams = cameraObj.getParameters();
            if (camParams == null) {
                return false;
            }

            boolean supports1080p=false,supports720p=false;
            List<Camera.Size> previewSizes = camParams.getSupportedPreviewSizes();
            if (null != previewSizes && 0 != previewSizes.size()) {
                final Camera.Size tenEightyP = cameraObj.new Size(1920, 1080);
                final Camera.Size sevenTwentyP = cameraObj.new Size(1280, 720);

                supports1080p = previewSizes.contains(tenEightyP);
                supports720p = previewSizes.contains(sevenTwentyP);
            }

            if (supports1080p== false && supports720p == false) {
                return false;
            }
        return true;
    }

     private static void release(Camera cameraObj) {
        try{

            if (null != cameraObj) {
                cameraObj.release();
                cameraObj = null;
            }
        } catch(Exception e) {
            //something wrong happened
        }
    }

}
