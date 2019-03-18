package com.will_russell.ev3bluetoothclient;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class DebugFragment extends Fragment {
    protected static ListView debugListView;
    protected static ArrayAdapter<String> adapter;
    protected static ArrayList<String> outputList = new ArrayList<>();

    public static DebugFragment newInstance(){
        DebugFragment fragment = new DebugFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.debug_fragment, container, false);
        debugListView = (ListView) view.findViewById(R.id.debug_listview);
        adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, outputList);
        debugListView.setAdapter(adapter);
        return view;
    }
}
