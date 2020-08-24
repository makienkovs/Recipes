package com.makienkovs.recipes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;

public class Adapter extends BaseAdapter {

    public static int ADAPTER_ALL = 1;
    public static int ADAPTER_YOUR = 2;
    public static int ADAPTER_SAVE = 3;

    private ArrayList<Recipe> recipes;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private int adapterMode;
    private Context context;
    private DBHandler dbHandler;

    public Adapter(ArrayList<Recipe> recipes, int adapterMode, Context context) {
        this.recipes = recipes;
        this.adapterMode = adapterMode;
        this.context = context;
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        dbHandler = new DBHandler(context);
    }

    @Override
    public int getCount() {
        if (recipes == null) return 0;
        return recipes.size();
    }

    @Override
    public Object getItem(int position) {
        if (recipes == null) return null;
        return recipes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe, null);

        switch (adapterMode) {
            case 2:
                convertView.setOnLongClickListener(v -> deleteFromYour(position));
                fillViewFromFirebase(convertView, position);
                break;
            case 3:
                convertView.setOnLongClickListener(v -> deleteFromSaved(position));
                fillViewSaved(convertView, position);
                break;
            default:
                convertView.setOnLongClickListener(v -> addToSaved(position));
                fillViewFromFirebase(convertView, position);
        }
        return convertView;
    }

    private boolean deleteFromYour(int position) {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.remove))
                .setMessage(context.getString(R.string.sureToRemove))
                .setCancelable(false)
                .setNegativeButton(context.getText(R.string.cancel), null)
                .setPositiveButton(context.getText(R.string.Ok), (dialog, which) -> {
                    Recipe recipe = recipes.get(position);
                    deleteFromFirebase(recipe);
                    recipes.remove(recipe);
                    Toast.makeText(context, R.string.deleted, Toast.LENGTH_SHORT).show();
                    this.notifyDataSetChanged();
                })
                .create()
                .show();
        this.notifyDataSetChanged();
        return true;
    }

    private boolean deleteFromSaved(int position) {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.remove))
                .setMessage(context.getString(R.string.sureToRemove))
                .setCancelable(false)
                .setNegativeButton(context.getText(R.string.cancel), null)
                .setPositiveButton(context.getText(R.string.Ok), (dialog, which) -> {
                    Recipe recipe = recipes.get(position);
                    deleteFromLocal(recipe);
                    Toast.makeText(context, R.string.deleted, Toast.LENGTH_SHORT).show();
                    this.notifyDataSetChanged();
                })
                .create()
                .show();
        this.notifyDataSetChanged();
        return true;
    }

    private void deleteFromFirebase(Recipe recipe) {
        String id = recipe.getId();
        recipes.remove(recipe);
        Query idQuery = mDatabaseRef.child("Recipe").orderByChild("id").equalTo(id);
        idQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot idSnapshot : snapshot.getChildren()) {
                    idSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("!!!!!" + error.toException().getMessage());
            }
        });
        StorageReference photoFileRef = mStorageRef.child("images/" + id);
        photoFileRef.delete().addOnSuccessListener(aVoid -> System.out.println("!!!!! deleted from Firebase storage"));
    }

    private void deleteFromLocal(Recipe recipe) {
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir != null) {
            String fileFullPath = storageDir.toString() + "/" + recipe.getId();
            File photoFile = new File(fileFullPath);
            if (photoFile.exists()) {
                if (photoFile.delete()) {
                    System.out.println("!!!!! photo file is deleted");
                } else {
                    System.out.println("!!!!! photo file is not deleted");
                }
            }
        }
        dbHandler.removeFromDB(recipe);
        recipes.remove(recipe);
    }

    private boolean addToSaved(int position) {
        Recipe recipe = recipes.get(position);
        if (dbHandler.addToDB(recipe)){
            Toast.makeText(context, R.string.added, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.allready_added, Toast.LENGTH_SHORT).show();
        }

        this.notifyDataSetChanged();
        return true;
    }

    private void fillViewFromFirebase(View convertView, int position) {
        long ONE_MEGABYTE = 1024 * 1024;
        final ImageView imageViewPhoto = convertView.findViewById(R.id.imageViewPhoto);
        TextView textViewTitle = convertView.findViewById(R.id.textViewTitle);
        TextView textViewIngredients = convertView.findViewById(R.id.textViewIngredients);
        TextView textViewHowToCook = convertView.findViewById(R.id.textViewHowToCook);

        StorageReference photoRef = mStorageRef.child("images/" + recipes.get(position).getId());
        photoRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            imageViewPhoto.setImageBitmap(bmp);
        });

        textViewTitle.setText(recipes.get(position).getTitle());
        textViewIngredients.setText(recipes.get(position).getIngredients());
        textViewHowToCook.setText(recipes.get(position).getHowToCook());
    }

    private void fillViewSaved(View convertView, int position) {

        final ImageView imageViewPhoto = convertView.findViewById(R.id.imageViewPhoto);
        TextView textViewTitle = convertView.findViewById(R.id.textViewTitle);
        TextView textViewIngredients = convertView.findViewById(R.id.textViewIngredients);
        TextView textViewHowToCook = convertView.findViewById(R.id.textViewHowToCook);

        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir != null) {
            String fileFullPath = storageDir.toString() + "/" + recipes.get(position).getId();
            File photoFile = new File(fileFullPath);
            if (!photoFile.exists()) {
                StorageReference photoRef = mStorageRef.child("images/" + recipes.get(position).getId());
                photoRef.getFile(photoFile).addOnSuccessListener(taskSnapshot -> {
                    imageViewPhoto.setImageURI(Uri.parse(fileFullPath));
                    this.notifyDataSetChanged();
                });
            }
            imageViewPhoto.setImageURI(Uri.parse(fileFullPath));
            textViewTitle.setText(recipes.get(position).getTitle());
            textViewIngredients.setText(recipes.get(position).getIngredients());
            textViewHowToCook.setText(recipes.get(position).getHowToCook());
        }
    }
}