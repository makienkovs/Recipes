package com.makienkovs.recipes.ui.content;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;

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

public class AllFragment extends Fragment {

    private SearchView searchView;
    private ListView list_all;
    private ArrayList<Recipe> recipes;
    private Adapter adapter;
    private DatabaseReference reference;

    public AllFragment() {
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
        View root = inflater.inflate(R.layout.fragment_all, container, false);
        list_all = root.findViewById(R.id.list_all);
        searchView = root.findViewById(R.id.searchView);
        searchView.setInputType(InputType.TYPE_CLASS_TEXT);
        searchView.setOnCloseListener(() -> {
            adapter = new Adapter(recipes, Adapter.ADAPTER_ALL, getContext());
            list_all.setAdapter(adapter);
            return false;
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search();
                searchView.setQuery("", false);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        reference = FirebaseDatabase.getInstance().getReference("Recipe");
        recipes = new ArrayList<>();
        adapter = new Adapter(recipes, Adapter.ADAPTER_ALL, getContext());
        list_all.setAdapter(adapter);
        getData();
        return root;
    }

    private void search() {
        ArrayList<Recipe> recipesTemp = new ArrayList<>();
        CharSequence search = searchView.getQuery();
        for (int i = 0; i < recipes.size(); i++) {
            if (recipes.get(i).getTitle().toLowerCase().contains(search.toString().toLowerCase())) {
                recipesTemp.add(recipes.get(i));
            }
        }
        adapter = new Adapter(recipesTemp, Adapter.ADAPTER_ALL, getContext());
        list_all.setAdapter(adapter);
    }

    private void getData() {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (recipes.size() > 0) {
                    recipes.clear();
                }
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Recipe recipe = ds.getValue(Recipe.class);
                    if (recipe != null) {
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