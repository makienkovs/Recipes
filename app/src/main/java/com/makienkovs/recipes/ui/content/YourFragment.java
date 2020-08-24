package com.makienkovs.recipes.ui.content;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makienkovs.recipes.Adapter;
import com.makienkovs.recipes.R;
import com.makienkovs.recipes.Recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class YourFragment extends Fragment {

    private ArrayList<Recipe> recipes;
    private Adapter adapter;
    private DatabaseReference reference;

    public YourFragment() {
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
        View root = inflater.inflate(R.layout.fragment_your, container, false);
        ListView list_your = root.findViewById(R.id.list_your);
        reference = FirebaseDatabase.getInstance().getReference("Recipe");
        recipes = new ArrayList<>();
        adapter = new Adapter(recipes, Adapter.ADAPTER_YOUR, getContext());
        list_your.setAdapter(adapter);
        getData();
        return root;
    }

    private void getData() {
        String user = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (recipes.size() > 0) {
                    recipes.clear();
                }
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Recipe recipe = ds.getValue(Recipe.class);
                    if (recipe != null && recipe.getUser().equals(user)) {
                        recipes.add(recipe);
                    }
                }
                Collections.reverse(recipes);
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        reference.addValueEventListener(valueEventListener);
    }
}