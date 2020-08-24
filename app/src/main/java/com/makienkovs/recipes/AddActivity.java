package com.makienkovs.recipes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Objects;

public class AddActivity extends AppCompatActivity {

    private ImageView photo;
    private TextView title, ingredients, howtocook;
    private static final int TAKE_PHOTO_REQUEST = 1;
    private static final int GALLERY_REQUEST = 2;
    private Uri photoURI;
    private String bigPhotoPath;

    private Recipe recipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        recipe = new Recipe();
        photo = findViewById(R.id.photo);
        title = findViewById(R.id.title);
        ingredients = findViewById(R.id.ingredients);
        howtocook = findViewById(R.id.howtocook);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteBigPhoto();
    }

    public void takeShot(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                if (bigPhotoPath != null) {
                    deleteBigPhoto();
                }
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                String prefix = String.valueOf(Calendar.getInstance().getTimeInMillis());
                String suffix = ".jpg";
                photoFile = File.createTempFile(prefix, suffix, storageDir);
                bigPhotoPath = photoFile.getAbsolutePath();
            } catch (IOException ex) {
                Snackbar
                        .make(findViewById(R.id.registration_layout), R.string.err_cam, Snackbar.LENGTH_SHORT)
                        .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                        .show();
            }
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.makienkovs.android.provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, TAKE_PHOTO_REQUEST);
            }
        }
    }

    public void loadImage(View v) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_PHOTO_REQUEST && resultCode == RESULT_OK) {
            photo.setImageURI(photoURI);
        } else if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            Bitmap bitmap = null;
            photoURI = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoURI);
            } catch (IOException e) {
                e.printStackTrace();
                Snackbar
                        .make(findViewById(R.id.registration_layout), R.string.err_gallery, Snackbar.LENGTH_SHORT)
                        .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                        .show();
            }
            photo.setImageBitmap(bitmap);
        }
    }

    public void publish(View v) {
        if (!saveData()) return;
        uploadData();
        uploadImageToStorage();
    }

    private void saveCompressedPhoto() {
        try {
            Drawable drawable = photo.getDrawable();
            if (drawable == null) return;
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            String id = String.valueOf(Calendar.getInstance().getTimeInMillis());
            File photoFile = File.createTempFile(id, ".jpg", storageDir);
            recipe.setId(photoFile.getName());
            OutputStream fOut = new FileOutputStream(photoFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, fOut);
            fOut.flush();
            fOut.close();
            photoURI = FileProvider.getUriForFile(this, "com.makienkovs.android.provider", photoFile);
            deleteBigPhoto();
        } catch (IOException e) {
            e.printStackTrace();
            Snackbar
                    .make(findViewById(R.id.registration_layout), R.string.err_save, Snackbar.LENGTH_SHORT)
                    .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                    .show();
        }
    }

    private void deleteBigPhoto() {
        if (bigPhotoPath == null) return;
        File bigPhoto = new File(bigPhotoPath);
        if (bigPhoto.exists()) {
            if (bigPhoto.delete()){
                System.out.println("!!!!! file deleted");
            } else {
                System.out.println("!!!!! file is not deleted");
            }
        }
    }

    private boolean saveData() {
        final String titleString = title.getText().toString();
        final String ingredientsString = ingredients.getText().toString();
        final String howtocookString = howtocook.getText().toString();

        if (titleString.isEmpty()) {
            Snackbar
                    .make(findViewById(R.id.add_layout), R.string.err_title, Snackbar.LENGTH_SHORT)
                    .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                    .show();
            return false;
        } else if (ingredientsString.isEmpty()) {
            Snackbar
                    .make(findViewById(R.id.add_layout), R.string.err_ingredients, Snackbar.LENGTH_SHORT)
                    .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                    .show();
            return false;
        } else if (howtocookString.isEmpty()) {
            Snackbar
                    .make(findViewById(R.id.add_layout), R.string.err_howtocook, Snackbar.LENGTH_SHORT)
                    .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                    .show();
            return false;
        }

        saveCompressedPhoto();

        if (recipe.getId() == null) {
            Snackbar
                    .make(findViewById(R.id.add_layout), R.string.err_photo, Snackbar.LENGTH_SHORT)
                    .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                    .show();
            return false;
        }
        recipe.setUser(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        recipe.setTitle(titleString);
        recipe.setIngredients(ingredientsString);
        recipe.setHowToCook(howtocookString);
        return true;
    }

    private void uploadImageToStorage() {
        if (photoURI == null) return;
        StorageReference ref = FirebaseStorage.getInstance().getReference().child("images/" + recipe.getId());
        ref.putFile(photoURI)
                .addOnSuccessListener(taskSnapshot -> {
                    Toast.makeText(AddActivity.this, R.string.uploaded, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(AddActivity.this, R.string.failed + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void uploadData() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Recipe");
        reference.push().setValue(recipe);
    }
}