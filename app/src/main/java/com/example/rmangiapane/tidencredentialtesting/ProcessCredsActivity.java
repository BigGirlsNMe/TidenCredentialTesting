package com.example.rmangiapane.tidencredentialtesting;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class ProcessCredsActivity extends AppCompatActivity {

    public TextView TVclientID, TvPassword, TVToken,  TVDuration;
    public static ProgressDialog progress;

    public StringRequest stringRequest;
   public ImageView imageView;
    public String Token;
    private static final String urlTolken = "https://identity.us.sandbox.mitekcloud.com/connect/token";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_creds);

        imageView = (ImageView)findViewById(R.id.imgView);

        progress = new ProgressDialog(this);

        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        TVclientID = (TextView) findViewById(R.id.clientIDTV);
        TvPassword = (TextView) findViewById(R.id.passwordTV);
        TVToken = (TextView) findViewById(R.id.TolkenTV);
        TVDuration = (TextView) findViewById(R.id.DurationTV);

        Intent intent = getIntent();

        String clientID = intent.getExtras().getString("cID");
        String password = intent.getExtras().getString("pWord");

        TvPassword.setText(password);
        TVclientID.setText(clientID);

        RequestQueue queue = Volley.newRequestQueue(this);

        progress.setMessage("It's loading....");
        progress.setTitle("Request Processing");
        progress.show();

        stringRequest = new StringRequest(Request.Method.POST, urlTolken,
                new Response.Listener<String>() {

                        @Override
                    public void onResponse(String response) {
                            try {
                                JSONObject parentObject = new JSONObject(response);
                                Token = parentObject.get("access_token").toString();
                                TVDuration.setText("Your Token is valid for: " + parentObject.get("expires_in").toString());
                                TVToken.setText(Token);
                                TVToken.setMovementMethod(new ScrollingMovementMethod());
                                imageView.setImageResource(R.mipmap.green_check);
                                imageView.setTag(R.mipmap.green_check);
                                progress.cancel();
                            } catch (JSONException e) {
                                imageView.setImageResource(R.mipmap.red_x);
                                imageView.setTag(R.mipmap.red_x);
                                e.printStackTrace();
                            }

                        }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                imageView.setImageResource(R.mipmap.red_x);
                imageView.setTag(R.mipmap.red_x);
                TVToken.setText("Error: " + error
                        + ">>" + error.networkResponse.statusCode
                        + ">>" + error.networkResponse.data
                        + ">>" + error.getCause()
                        + ">>" + error.getMessage());
                progress.cancel();
            }
        })
        {
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("grant_type","client_credentials");
                params.put("client_id", TVclientID.getText().toString());
                params.put("client_secret", TvPassword.getText().toString());
                params.put("scope","global.identity.api gip.document.creator dossier.creator");
                return params;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

        };
        queue.add(stringRequest);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((Integer)imageView.getTag()== R.mipmap.green_check) {
                    Intent i = new Intent(ProcessCredsActivity.this, DossierTestActivity.class);
                    i.putExtra("Token",Token);
                    startActivity(i);
                }
               else
                {
                    Snackbar.make(view, "Bad Request. Token Is Required. Please return to the Splash Screen and Enter Correct Credentials", Snackbar.LENGTH_LONG).setAction("Action", null).show();
              }


            }
        });
            }

    }