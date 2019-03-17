package com.will_russell.ev3bluetoothclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.view.View;
import android.widget.ArrayAdapter;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import android.app.AlertDialog;

import java.io.*;
import java.net.*;

import com.caverock.androidsvg.SVGImageView;
import com.caverock.androidsvg.SVG;

public class MainActivity extends AppCompatActivity {
    MaterialButton connectButton;
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
        connectButton = (MaterialButton) findViewById(R.id.connection_button);
        updateConversationHandler = new Handler();
        builder = new AlertDialog.Builder(this);
        SVGImageView svgImageView = (SVGImageView) findViewById(R.id.maze_view);
        svgImageView.setImageAsset("maze-empty.svg");
    }

    public void onClick(View v) {
        final TextInputEditText ipText = (TextInputEditText) findViewById(R.id.ip_box);
        final TextInputEditText portText = (TextInputEditText) findViewById(R.id.port_box);
        if (connectButton.getText().toString().equals("Connect to EV3")) {
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
                connectButton.setText("Disconnect from EV3");
                startConnection(ipText.getText().toString().trim(), Integer.valueOf(portText.getText().toString().trim()));
            }
        } else {
            try {
                connectButton.setText("Connect to EV3");
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
        System.out.println(ip);
        System.out.println(port);
        ClientThread clientThread = new ClientThread(socket);
        new Thread(clientThread).start();
        connectButton.setText("Connect");
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
                    break;
                }
            }
        }
    }

    class OutputThread implements Runnable {
        private String output;
        private SVGImageView iv;

        public OutputThread(String output) {
            this.output = output;
            iv = (SVGImageView) findViewById(R.id.maze_view);
        }

        @Override
        public void run() {
            try {
                stringToSvg(output);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void stringToSvg(String svgString) throws com.caverock.androidsvg.SVGParseException {
            svgToImageView(SVG.getFromString(svgString));
        }

        public void svgToImageView(SVG svg) {
            iv.setSVG(svg);
        }
    }
}
