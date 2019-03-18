package com.will_russell.ev3bluetoothclient;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DebugFragment extends Fragment {

    public static DebugFragment newInstance(){

        DebugFragment fragment = new DebugFragment();
        return fragment;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.debug_fragment, container, false);
    }
}
