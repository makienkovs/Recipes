package com.makienkovs.recipes.ui.content;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.makienkovs.recipes.Adapter;
import com.makienkovs.recipes.DBHandler;
import com.makienkovs.recipes.R;
import com.makienkovs.recipes.Recipe;

import java.util.ArrayList;
import java.util.Collections;

public class SavedFragment extends Fragment {

    private ListView list_saved;
    private DBHandler dbHandler;

    public SavedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_saved, container, false);
        list_saved = root.findViewById(R.id.list_saved);
        dbHandler = new DBHandler(getContext());
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
    }

    private void init() {
        ArrayList<Recipe> recipes = dbHandler.getAllDB();
        Collections.reverse(recipes);
        Adapter adapter = new Adapter(recipes, Adapter.ADAPTER_SAVE, getContext());
        list_saved.setAdapter(adapter);
    }
}