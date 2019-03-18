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
import android.widget.TextView;

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
        getData(ip, port);
    }

    private void endConnection() throws IOException {
        connectButton.setText("Connect to EV3");
        socket.close();
    }


    private void getData(String ip, int port){
        final TextView status = (TextView) findViewById(R.id.status_view);
        try {
            socket = new Socket();
            status.setText("Status: Connecting");
            socket.connect(new InetSocketAddress(ip, port), 2000);
            status.setText("Status: Connected");
            ClientThread clientThread = new ClientThread(socket);
            new Thread(clientThread).start();
            connectButton.setText("Connect to EV3");
        } catch (UnknownHostException e) {
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            builder.setMessage(writer.toString()).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    connectButton.setText("Connect to EV3");
                    status.setText("Status: Failed - Host Unknown");
                }
            });
            AlertDialog alert = builder.create();
            alert.setTitle("UnknownHostException thrown");
            alert.show();
        } catch (IOException e) {
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            builder.setMessage(writer.toString()).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    connectButton.setText("Connect to EV3");
                    status.setText("Status: Failed - IOException");
                }
            });
            AlertDialog alert = builder.create();
            alert.setTitle("IOException thrown");
            alert.show();
        } catch  (Exception e) {
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            builder.setMessage(writer.toString()).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    connectButton.setText("Connect to EV3");
                    status.setText("Status: Failed - Exception");
                }
            });
            AlertDialog alert = builder.create();
            alert.setTitle("Exception thrown");
            alert.show();
        }
    }

    class ClientThread implements Runnable {
        private Socket socket;

        public ClientThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    InputStream in = socket.getInputStream();
                    DataInputStream dataIn = new DataInputStream(in);
                    String output = dataIn.readUTF();
                    if ((output.length > 4) && (output.substring(0, 4).equals("<svg")) {
                        updateConversationHandler.post(new OutputThread(output));
                    } else {
                        builder.setMessage("The grid wasn't sent. Instead, the following was sent: " + output).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.setTitle("Grid not sent");
                        alert.show();
                    }
                } catch (IOException e) {
                    builder.setMessage("The EV3 connection has been lost. Please restart the EV3 server to connect.").setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.setTitle("Server connection closed");
                    alert.show();
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
