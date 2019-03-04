package com.will_russell.ev3bluetoothclient;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.*;
import java.net.*;

public class MainActivity extends AppCompatActivity {

    private ListView listview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listview = (ListView) findViewById(R.id.output_view);
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

    private void onClick(View v) {

        TextInputEditText ipText = (TextInputEditText) findViewById(R.id.ip_box);
        TextInputEditText portText = (TextInputEditText) findViewById(R.id.port_box);
        try {
            getData(ipText.getText().toString(), Integer.valueOf(portText.getText().toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
