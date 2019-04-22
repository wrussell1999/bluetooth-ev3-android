package com.will_russell.ev3bluetoothclient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import android.app.AlertDialog;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import com.caverock.androidsvg.SVGImageView;
import com.caverock.androidsvg.SVG;

public class MainActivity extends AppCompatActivity {
    MaterialButton connectButton;
    private AlertDialog.Builder builder;
    private Socket socket;
    Handler uiHandler;
    TextView status;
    CheckBox toggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        connectButton = (MaterialButton) findViewById(R.id.connection_button);
        status = (TextView) findViewById(R.id.status_view);
        toggle = (CheckBox) findViewById(R.id.frame_check);

        uiHandler = new Handler();

        builder = new AlertDialog.Builder(this);

        toggle.setOnClickListener(new View.OnClickListener() {
            Fragment selectedFragment = SVGFragment.newInstance();
            @Override
            public void onClick(View v) {
                if (toggle.isChecked()) {
                    selectedFragment = DebugFragment.newInstance();
                } else {
                    selectedFragment = SVGFragment.newInstance();
                }
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.frame_layout, selectedFragment);
                transaction.commit();
            }
        });
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, SVGFragment.newInstance());
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tasks, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent;
        switch (id) {
            case R.id.action_about:
                intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
                getData(ipText.getText().toString().trim(), Integer.valueOf(portText.getText().toString().trim()));
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

    private void endConnection() throws IOException {
        connectButton.setText(R.string.connect_button_name);
        socket.close();
    }

    private void getData(String ip, int port){
        try {
            status.setText(R.string.connection_text_connecting);
            socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), 2000);
            status.setText(R.string.connection_text_connected);

            InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);

            ClientThread clientThread = new ClientThread(socket);
            new Thread(clientThread).start();
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
                    uiHandler.post(new UIThread(output));
                } catch (IOException e) {
                    break;
                }
            }
        }
    }

    class UIThread implements Runnable {
        private String output;

        public UIThread(String output) {
            this.output = output;
        }

        @Override
        public void run() {
            try {
                if ((output.length() > 4) && (output.substring(0, 4).equals("<svg"))) {
                    SVG svg = SVG.getFromString(output);
                    SVGFragment.mazeList.add(svg);
                    SVGFragment.svgImageView.setSVG(svg);
                } else {
                    DebugFragment.outputList.add(output);
                    DebugFragment.adapter.notifyDataSetChanged();
                    DebugFragment.debugListView.setSelection(DebugFragment.adapter.getCount() - 1);
                }
            } catch (com.caverock.androidsvg.SVGParseException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
