package com.will_russell.ev3bluetoothclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import android.app.AlertDialog;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView listview;
    private ArrayList<String> outputList = new ArrayList<>();
    ArrayAdapter<String> adapter;


    private AlertDialog.Builder builder;
    private Socket socket;
    Handler updateConversationHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        listview = (ListView) findViewById(R.id.output_view);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, outputList);
        listview.setAdapter(adapter);
        updateConversationHandler = new Handler();
        builder = new AlertDialog.Builder(this);


    }

    public void onClick(View v) {
        final TextInputEditText ipText = (TextInputEditText) findViewById(R.id.ip_box);
        final TextInputEditText portText = (TextInputEditText) findViewById(R.id.port_box);
        MaterialButton connectButton = (MaterialButton) findViewById(R.id.connection_button);
        if (connectButton.getText().toString().equals("Connect")) {
            if (ipText.getText().toString().trim().length() <= 0) {
                builder.setMessage("You need to enter an IP address").setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ipText.setFocusableInTouchMode(true);
                        ipText.requestFocus();
                    }
                });
                AlertDialog alert = builder.create();
                alert.setTitle("No IP Address");
                alert.show();
            } else if (portText.getText().toString().trim().length() <= 0) {
                builder.setMessage("You need to enter a Port number").setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        portText.setFocusableInTouchMode(true);
                        portText.requestFocus();
                    }
                });
                AlertDialog alert = builder.create();
                alert.setTitle("No Port Number");
                alert.show();
            } else {

                connectButton.setText("Disconnect");
                startConnection(ipText.getText().toString().trim(), Integer.valueOf(portText.getText().toString().trim()));
            }
        } else {
            try {
                connectButton.setText("Connect");
                endConnection();
            } catch(IOException e)
            {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void startConnection(String ip, int port) {
        try {
            getData(ip, port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            builder.setMessage(writer.toString()).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            AlertDialog alert = builder.create();
            alert.setTitle("Exception thrown");
            alert.show();
        }
    }

    private void endConnection() throws IOException {
        socket.close();
    }

    private void getData(String ip, int port) throws IOException {
        socket = new Socket(ip, port);
        ClientThread clientThread = new ClientThread(socket);
        new Thread(clientThread).start();
    }

    class ClientThread implements Runnable {
        private Socket socket;

        public ClientThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    InputStream in = socket.getInputStream();
                    DataInputStream dataIn = new DataInputStream(in);
                    String output = dataIn.readUTF();
                    updateConversationHandler.post(new OutputThread(output));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class OutputThread implements Runnable {

        private String output;

        public OutputThread(String output) {
            this.output = output;
        }

        @Override
        public void run() {
            outputList.add(output);
            adapter.notifyDataSetChanged();
        }

    }
}
