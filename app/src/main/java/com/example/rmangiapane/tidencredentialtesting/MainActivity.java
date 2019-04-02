package com.example.rmangiapane.tidencredentialtesting;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements android.view.View.OnClickListener {
    public Button testCredentials, goToQR;
    public EditText clientID, password;

    @Override
    public void onClick(View view) {

        if (view == findViewById(R.id.testCredentialsButton)) {

            Intent i = new Intent(MainActivity.this , ProcessCredsActivity.class);
            i.putExtra("cID",clientID.getText().toString());
            i.putExtra("pWord",password.getText().toString());
            startActivity(i);
        }
        if (view == findViewById(R.id.qrCodeButton)) {
                Intent i = new Intent(MainActivity.this, QRCodeCaptureActivity.class);
                startActivity(i);
        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Credential Testing");
        setContentView(R.layout.activity_main);

        if(agoodNameforVariable.frontImg!=null && agoodNameforVariable.backImg!=null){
            Toast.makeText(getApplicationContext(), "Images Added From MiSnap. Continue with your Tiden Credentials",
                    Toast.LENGTH_LONG).show();

        }

        clientID = (EditText) findViewById(R.id.clientIdEditText);
        password = (EditText) findViewById(R.id.passwordEditText);
        testCredentials = (Button) findViewById(R.id.testCredentialsButton);
        goToQR = (Button) findViewById(R.id.qrCodeButton);
        testCredentials.setOnClickListener(this);
        goToQR.setOnClickListener(this);
    }
}
