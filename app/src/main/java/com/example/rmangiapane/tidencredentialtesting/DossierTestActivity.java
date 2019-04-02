package com.example.rmangiapane.tidencredentialtesting;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DossierTestActivity extends AppCompatActivity {
    public String Token, imgFrontStr, imgBackStr;
    public ImageView imgView;
    public TextView TVStatus, TVTest, TVName, TVAddress;
    public JSONObject jsnObjFrontImage, jsnObjBackImage, jjObj, jsnConfigObj, sendObj;
    public JSONArray jsonArrayEvidence, jsonArrayConfiguration, jsonImageArray;
    public static ProgressDialog progress;

    public String urlDossier = "https://globalidentity.us.sandbox.mitekcloud.com/api/Verify/v2/Dossier";
    public JsonObjectRequest jsonObjectRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Dossier Test");
        setContentView(R.layout.activity_document_test);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab2 = (FloatingActionButton)findViewById(R.id.fab);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fab2.getLayoutParams();
        params.setBehavior(null);
        fab2.requestLayout();
        fab2.setVisibility(View.GONE);

        imgView = (ImageView) findViewById(R.id.imageViewDossier);

        TVStatus = (TextView) findViewById(R.id.StatusTV);
        TVTest = (TextView)findViewById(R.id.TestTV);

        TVAddress = (TextView)findViewById(R.id.AddressTV);
        TVName = (TextView)findViewById(R.id.NameTV);

        progress = new ProgressDialog(this);

        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        //get Base64 Strings out of resources

        if(agoodNameforVariable.frontImg != null && agoodNameforVariable.backImg != null){

            imgFrontStr = agoodNameforVariable.frontImg;
            imgBackStr = agoodNameforVariable.backImg;


        }
        else{
            imgFrontStr = getString(R.string.imgFront);
            imgBackStr = getString(R.string.imgBack);

        }


        //make progress bar

        progress.setMessage("It's loading....");
        progress.setTitle("Request Processing");
        progress.show();

        TVTest.setText("Dossier Front and Back Image Test");
        Intent i = getIntent();
        Token = i.getStringExtra("Token");

        final Handler handle = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                progress.incrementProgressBy(1);

            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (progress.getProgress() <= progress
                            .getMax()) {
                        Thread.sleep(200);
                        handle.sendMessage(handle.obtainMessage());
                        if (progress.getProgress() == progress
                                .getMax()) {
                            progress.dismiss();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        handle.sendMessage(handle.obtainMessage());

        RequestQueue queue = Volley.newRequestQueue(this);

        //make JSON Structure to send to TIDEN
        jsnObjBackImage = new JSONObject();
        jsnObjFrontImage = new JSONObject();
        jsonImageArray = new JSONArray();
        jsonArrayConfiguration = new JSONArray();
        jjObj = new JSONObject();
        jsnConfigObj = new JSONObject();
        jsonArrayEvidence = new JSONArray();
        sendObj = new JSONObject();

        try{
            jsnObjFrontImage.put("data", imgFrontStr);
            jsnObjBackImage.put("data", imgBackStr);

            jsonImageArray.put(jsnObjFrontImage);
            jsonImageArray.put(jsnObjBackImage);

            jjObj.put("type", "IdDocument");
            jjObj.put("images",jsonImageArray);

            jsonArrayConfiguration.put("CroppedDocument");

            jsnConfigObj.put("responseImages",jsonArrayConfiguration);

            jsonArrayEvidence.put(jjObj);

            sendObj.put("evidence", jsonArrayEvidence);
            sendObj.put("configuration", jsnConfigObj);
        }
        catch (JSONException e){
            e.printStackTrace();
        }
        //Actual JSON Request to Dossier front and back Check
        jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, urlDossier, sendObj,
                         new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        imgView.setImageResource(R.mipmap.green_check);
                        TVStatus.setText("Success");
                        //stop progress bar
                        //try getting JSON result from Tiden
                    try {
                        JSONArray evidence = response.getJSONArray("evidence");

                        for (int i = 0; i < evidence.length(); i++) {
                            JSONObject eviObj = evidence.getJSONObject(i);
                            JSONObject extObj = eviObj.getJSONObject("extractedData");
                            JSONArray returnImg = eviObj.getJSONArray("images");
                            JSONObject ImgToBeRet = returnImg.getJSONObject(i);
                            JSONObject deeper = ImgToBeRet.getJSONObject("derivedImages");
                            JSONObject crpDcmnt = deeper.getJSONObject("croppedDocument");
                            String yourCroppedDoc = crpDcmnt.getString("data");

                            byte[] imageAsBytes = Base64.decode(yourCroppedDoc.getBytes(), Base64.DEFAULT);
                            ImageView image = (ImageView)findViewById(R.id.FrontImageView);
                            image.setImageBitmap(
                                    BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length)
                            );


                            //add image to library

                        //end adding image


                            String who = extObj.getJSONObject("name").getString("fullName");
                            String where = extObj.getJSONObject("address").getString("addressLine1");
                            TVName.setText(who);
                            TVAddress.setText(where);
                            progress.cancel();
                        }
                    }
                        catch (JSONException e){
                       e.printStackTrace();
                    }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        progress.cancel();
                        imgView.setImageResource(R.mipmap.red_x);
                        TVStatus.setText("Error: " + error
                                + ">>" + error.networkResponse.statusCode
                                + ">>" + error.networkResponse.data
                                + ">>" + error.getCause()
                                + ">>" + error.getMessage());
                    }

                }
                ) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=UTF-8";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer " + Token);
                return params;
            }
        };
        //change time out of volley to 60sec
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonObjectRequest);
    }
}
