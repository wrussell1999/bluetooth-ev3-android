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
                builder.setMessage(R.string.no_ip_message).setCancelable(false).setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ipText.setFocusableInTouchMode(true);
                        ipText.requestFocus();
                    }
                });
                AlertDialog alert = builder.create();
                alert.setTitle(R.string.no_ip_title);
                alert.show();
            } else if (portText.getText().toString().trim().length() <= 0) {
                builder.setMessage(R.string.no_port_message).setCancelable(false).setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        portText.setFocusableInTouchMode(true);
                        portText.requestFocus();
                    }
                });
                AlertDialog alert = builder.create();
                alert.setTitle(R.string.no_port_title);
                alert.show();
            } else {
                connectButton.setText(R.string.disconnect_button_name);
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
        connectButton.setText(R.string.connect_button_name);
        socket.close();
    }


    private void getData(String ip, int port){
        final TextView status = (TextView) findViewById(R.id.status_view);
        try {
            socket = new Socket();
            status.setText(R.string.connection_text_connecting);
            socket.connect(new InetSocketAddress(ip, port), 2000);
            status.setText(R.string.connection_text_connected);
            ClientThread clientThread = new ClientThread(socket);
            new Thread(clientThread).start();
            connectButton.setText(R.string.connect_button_name);
        } catch (UnknownHostException e) {
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            builder.setMessage(writer.toString()).setCancelable(false).setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    connectButton.setText(R.string.connect_button_name);
                    status.setText(R.string.connection_text_fail_host);
                }
            });
            AlertDialog alert = builder.create();
            alert.setTitle(R.string.connection_title_fail_host);
            alert.show();
        } catch (IOException e) {
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            builder.setMessage(writer.toString()).setCancelable(false).setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    connectButton.setText(R.string.connect_button_name);
                    status.setText(R.string.connection_text_fail_io);
                }
            });
            AlertDialog alert = builder.create();
            alert.setTitle(R.string.connection_title_fail_io);
            alert.show();
        } catch  (Exception e) {
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            builder.setMessage(writer.toString()).setCancelable(false).setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    connectButton.setText(R.string.connect_button_name);
                    status.setText(R.string.connection_text_fail);
                }
            });
            AlertDialog alert = builder.create();
            alert.setTitle(R.string.connection_title_fail);
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
                    if ((output.length() > 4) && (output.substring(0, 4).equals("<svg"))) {
                        updateConversationHandler.post(new OutputThread(output));
                    } else {
                        /*
                        connectionBuilder.setMessage("The grid wasn't sent. Instead, the following was sent: " + output).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.setTitle("Grid not sent");
                        alert.show();
                        */
                        System.out.println("Real output: " + output);
                    }
                } catch (IOException e) {
                    /*
                    builder.setMessage("The EV3 connection has been lost. Please restart the EV3 server to connect.").setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.setTitle("EV3 connection closed");
                    alert.show();
                    */
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
            System.out.println("SVG");
            svgToImageView(SVG.getFromString(svgString));
        }

        public void svgToImageView(SVG svg) {
            iv.setSVG(svg);
        }
    }
}
