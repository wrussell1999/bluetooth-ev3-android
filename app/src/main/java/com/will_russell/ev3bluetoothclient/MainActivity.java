package com.will_russell.ev3bluetoothclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import com.google.android.material.textfield.TextInputEditText;
import android.app.AlertDialog;

import java.io.*;
import java.net.*;

public class MainActivity extends AppCompatActivity {

    private ListView listview;
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listview = (ListView) findViewById(R.id.output_view);
        builder = new AlertDialog.Builder(this);
    }

    public void onClick(View v) {
        final TextInputEditText ipText = (TextInputEditText) findViewById(R.id.ip_box);
        final TextInputEditText portText = (TextInputEditText) findViewById(R.id.port_box);
        if (ipText.getText().toString().trim().length() <= 0) {
            builder.setMessage("You need to enter an IP address").setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ipText.setFocusableInTouchMode(true);
                    ipText.requestFocus();
                }
            });;
            AlertDialog alert = builder.create();
            alert.setTitle("No IP Address");
            alert.show();
        } else if (portText.getText().toString().trim().length() <= 0) {
            builder.setMessage("You need to enter a Port number").setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    portText.setFocusableInTouchMode(true);
                    portText.requestFocus();
                }
            });;;
            AlertDialog alert = builder.create();
            alert.setTitle("No Port Number");
            alert.show();
        } else {
            startConnection(ipText.getText().toString(), Integer.valueOf(portText.getText().toString()));
        }

    }

    private void startConnection(String ip, int port) {
        try {
            getData(ip, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getData(String ip, int port) throws IOException {
        Socket socket = new Socket(ip, port);
        InputStream in = socket.getInputStream();
        DataInputStream dataIn = new DataInputStream(in);
        String output = dataIn.readUTF();
        TextView tv = new TextView(this);
        tv.setText(output);
        listview.addView(tv);
    }
}
