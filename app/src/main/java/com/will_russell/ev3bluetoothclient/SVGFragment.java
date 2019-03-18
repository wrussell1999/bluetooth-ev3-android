package com.will_russell.ev3bluetoothclient;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SVGFragment extends Fragment {

    public static SVGFragment newInstance() {
        SVGFragment svgFragment = new SVGFragment();
        return svgFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.svg_fragment, container, false);
    }
}
