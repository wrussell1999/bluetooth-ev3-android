package com.will_russell.ev3bluetoothclient;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.caverock.androidsvg.SVGImageView;

public class SVGFragment extends Fragment {
    protected static SVGImageView svgImageView;

    public static SVGFragment newInstance() {
        SVGFragment svgFragment = new SVGFragment();
        return svgFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.svg_fragment, container, false);
        svgImageView = (SVGImageView) view.findViewById(R.id.maze_view);
        return view;
    }
}
