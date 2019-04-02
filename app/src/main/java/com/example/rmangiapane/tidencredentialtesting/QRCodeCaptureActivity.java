package com.example.rmangiapane.tidencredentialtesting;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.miteksystems.misnap.events.OnFrameProcessedEvent;
import com.miteksystems.misnap.misnapworkflow.MiSnapWorkflowActivity;

import com.miteksystems.misnap.misnapworkflow.params.WorkflowApi;
import com.miteksystems.misnap.params.MiSnapAPI;
import com.miteksystems.misnap.params.MiSnapApiConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Console;
import java.io.File;
import java.io.FileOutputStream;

public class QRCodeCaptureActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final long PREVENT_DOUBLE_CLICK_TIME_MS = 1000;
    private long mTime;

    public Button btnCapFront, btnCapBack;
    public TextView Mibi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_capture);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        Mibi = (TextView)findViewById(R.id.MibiTV);


        btnCapFront = (Button)findViewById(R.id.captureImgFrontBtn);
        btnCapBack = (Button)findViewById(R.id.captureImgBackBtn);

        btnCapFront.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                agoodNameforVariable.determineSide = agoodNameforVariable.FRONT;
                startMiSnapWorkflow(MiSnapApiConstants.PARAMETER_DOCTYPE_ID_CARD_FRONT);
            }
        });

        btnCapBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                agoodNameforVariable.determineSide = agoodNameforVariable.BACK;
                startMiSnapWorkflow(MiSnapApiConstants.PARAMETER_DOCTYPE_ID_CARD_BACK);
            }
        });

    }

    private void startMiSnapWorkflow(String docType) {
        // Prevent multiple MiSnap instances by preventing multiple button presses
        if (System.currentTimeMillis() - mTime < PREVENT_DOUBLE_CLICK_TIME_MS) {
            Log.e("UxStateMachine", "Double-press detected");
            return;
        }

        mTime = System.currentTimeMillis();
        JSONObject misnapParams = new JSONObject();
        try {
            misnapParams.put(MiSnapAPI.MiSnapDocumentType, docType);

            // Example of how to add additional barcode scanning options
            if (docType.equals(MiSnapApiConstants.PARAMETER_DOCTYPE_BARCODES)) {
                // Set everything except Code 128 because it conflicts w/ the PDF417 barcode on the back of most drivers licenses.
                misnapParams.put(MiSnapAPI.BarCodeTypes, MiSnapAPI.BARCODE_ALL - MiSnapAPI.BARCODE_CODE_128);
                misnapParams.put(MiSnapAPI.BarCodeDirections, MiSnapAPI.BARCODE_SCAN_DIRECTION_OMNI);
            }

            // Here you can override optional API parameter defaults
            misnapParams.put(MiSnapAPI.MiSnapAllowScreenshots, 1);
            // e.g. misnapParams.put(MiSnapAPI.AppVersion, "1.0");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // TODO: I think I want to move all the parameters into 1 Extra so the integration
        // engineer can't accidentally put parameters in the wrong Extra.
        JSONObject workflowParams = new JSONObject();
        try {
            workflowParams.put(WorkflowApi.MISNAP_WORKFLOW_TRACK_GLARE, "1");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent intentMiSnap;
        intentMiSnap = new Intent(this, MiSnapWorkflowActivity.class);
        intentMiSnap.putExtra(MiSnapAPI.JOB_SETTINGS, misnapParams.toString());
        intentMiSnap.putExtra(WorkflowApi.WORKFLOW_SETTINGS, workflowParams.toString());
        startActivityForResult(intentMiSnap, MiSnapAPI.RESULT_PICTURE_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (MiSnapAPI.RESULT_PICTURE_CODE == requestCode) {
            if (RESULT_OK == resultCode) {
                if (data != null) {
                    Bundle extras = data.getExtras();
                    String miSnapResultCode = extras.getString(MiSnapAPI.RESULT_CODE);

                    switch (miSnapResultCode) {
                        // MiSnap check capture
                        case MiSnapAPI.RESULT_SUCCESS_VIDEO:
                        case MiSnapAPI.RESULT_SUCCESS_STILL:
                            Log.i(TAG, "MIBI: " + extras.getString(MiSnapAPI.RESULT_MIBI_DATA));
                            try {
                                JSONObject MibiJSON = new JSONObject(extras.getString(MiSnapAPI.RESULT_MIBI_DATA));
                                String SDKVersion = MibiJSON.getString("MiSnapVersion");
                                Mibi.setText(SDKVersion);



                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            // Image returned successfully
                            byte[] sImage = data.getByteArrayExtra(MiSnapAPI.RESULT_PICTURE_DATA);

                            String sEncodedImage = Base64.encodeToString(sImage, Base64.DEFAULT);


                            if(agoodNameforVariable.determineSide.equalsIgnoreCase(agoodNameforVariable.FRONT)){
                                agoodNameforVariable.frontImg = sEncodedImage;
                                agoodNameforVariable.determineSide = null;
                            }
                            else if(agoodNameforVariable.determineSide.equalsIgnoreCase(agoodNameforVariable.BACK)){
                                agoodNameforVariable.backImg = sEncodedImage;
                                agoodNameforVariable.determineSide = null;
                            }

                            if(agoodNameforVariable.frontImg!=null && agoodNameforVariable.backImg!=null){
                                Intent i = new Intent(this, MainActivity.class);
                                startActivity(i);
                            }

                            //      sendToServer(sEncodedImage);
                            //byte array
                            //convert into base 64 and send
                            // Now Base64-encode the byte array before sending it to the server.
                            // e.g. byte[] sEncodedImage = Base64.encode(sImage, Base64.DEFAULT);
                            //      sendToServer(sEncodedImage);

                            int warnings = extras.getInt(MiSnapAPI.RESULT_WARNINGS);
                            if (warnings != 0) {
                                String message = "WARNINGS:";
                                if ((warnings & OnFrameProcessedEvent.FRAME_WRONG_DOCUMENT_CHECK) != 0) {
                                    message += "\nWrong document detected";
                                }
                                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                            }

                            break;

                        // Card.io credit card capture
                        case MiSnapAPI.RESULT_SUCCESS_CREDIT_CARD:
                            StringBuilder creditCardInfo = new StringBuilder();
                            creditCardInfo.append("Redacted number: " + extras.getString(MiSnapAPI.CREDIT_CARD_REDACTED_NUMBER) + "\n");
                            // Never log a raw card number. Avoid displaying it.
                            creditCardInfo.append("Card number: *HIDDEN*\n"); // + extras.getString(MiSnapAPI.CREDIT_CARD_NUMBER));
                            creditCardInfo.append("Formatted number: *HIDDEN*\n"); //+ extras.getString(MiSnapAPI.CREDIT_CARD_FORMATTED_NUMBER));
                            creditCardInfo.append("Card type: " + extras.getString(MiSnapAPI.CREDIT_CARD_TYPE) + "\n");
                            creditCardInfo.append("CVV: " + extras.getInt(MiSnapAPI.CREDIT_CARD_CVV) + "\n");
                            creditCardInfo.append("Expiration month: " + extras.getInt(MiSnapAPI.CREDIT_CARD_EXPIRY_MONTH) + "\n");
                            creditCardInfo.append("Expiration year: " + extras.getInt(MiSnapAPI.CREDIT_CARD_EXPIRY_YEAR) + "\n");
                            new AlertDialog.Builder(this).setTitle("Card.io Data").setMessage(creditCardInfo.toString()).show();
                            Log.i(TAG, creditCardInfo.toString());

                            break;

                        // Barcode capture
                        case MiSnapAPI.RESULT_SUCCESS_PDF417:
                            String sUnformattedStr = extras.getString(MiSnapAPI.RESULT_PDF417_DATA);
                            Log.i(TAG, "PDF417 data: " + sUnformattedStr);
                            if (sUnformattedStr != null) {
                                //display the pdf417 data
                                new AlertDialog.Builder(this).setTitle("Barcode Data").setMessage(sUnformattedStr).show();
                            }
                            break;
                    }
                } else {
                    // Image canceled, stop
                    Toast.makeText(this, "MiSnap canceled", Toast.LENGTH_SHORT).show();
                }
            } else if (RESULT_CANCELED == resultCode) {
                // Camera not working or not available, stop
                Toast.makeText(this, "Operation canceled!!!", Toast.LENGTH_SHORT).show();
                if (data != null) {
                    Bundle extras = data.getExtras();
                    String miSnapResultCode = extras.getString(MiSnapAPI.RESULT_CODE);
                    if (!miSnapResultCode.isEmpty()) {
                        Toast.makeText(this, "Shutdown reason: " + miSnapResultCode, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

}
