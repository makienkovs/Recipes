package com.makienkovs.recipes;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class DBHandler {

    private SQLiteDatabase db;
    DBHelper dbHelper;
    Context c;
    public boolean isDBEnabled = true;

    public DBHandler(Context c) {
        this.c = c;
        dbHelper = new DBHelper(c);
        openDB(c);
    }

    private void openDB(Context c) {
        try {
            db = dbHelper.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            isDBEnabled = false;
        }

        if (db == null) {
            isDBEnabled = false;
        }
    }

    public boolean addToDB(Recipe recipe) {
        openDB(c);
        if (!isDBEnabled) return false;

        @SuppressLint("Recycle")
        Cursor c = db.query(DBHelper.MYTABLE,
                new String[]{DBHelper.ID, DBHelper.USER},
                DBHelper.ID + " = ?",
                new String[]{recipe.getId()},
                null, null, null);

        if (c.getCount() != 0) return false;
        else {
            new Thread(() -> {
                ContentValues cv = new ContentValues();
                cv.put(DBHelper.ID, recipe.getId());
                cv.put(DBHelper.USER, recipe.getUser());
                cv.put(DBHelper.TITLE, recipe.getTitle());
                cv.put(DBHelper.INGREDIENTS, recipe.getIngredients());
                cv.put(DBHelper.HOWTOCOOK, recipe.getHowToCook());
                db.insert(DBHelper.MYTABLE, null, cv);
                db.close();
            }).start();
            return true;
        }
    }

    public void removeFromDB(Recipe recipe) {
        openDB(c);
        if (!isDBEnabled) return;
        new Thread(() -> {
            db.delete(DBHelper.MYTABLE, "ID = ?", new String[]{recipe.getId()});
            db.close();
        }).start();
    }

    public void editDB(Recipe recipe) {
        openDB(c);
        if (!isDBEnabled) return;
        new Thread(() -> {
            ContentValues cv = new ContentValues();
            cv.put(DBHelper.ID, recipe.getId());
            cv.put(DBHelper.USER, recipe.getUser());
            cv.put(DBHelper.TITLE, recipe.getTitle());
            cv.put(DBHelper.INGREDIENTS, recipe.getIngredients());
            cv.put(DBHelper.HOWTOCOOK, recipe.getHowToCook());
            db.update(DBHelper.MYTABLE, cv, "ID = " + recipe.getId(), null);
            db.close();
        }).start();
    }

    public ArrayList<Recipe> getAllDB() {
        openDB(c);
        if (!isDBEnabled) return null;
        ArrayList<Recipe> recipes = new ArrayList<>();
        Cursor c = db.query(DBHelper.MYTABLE, null, null, null, null, null, null);
        if (c.moveToNext()) {
            int idIndex = c.getColumnIndex(DBHelper.ID);
            int userIndex = c.getColumnIndex(DBHelper.USER);
            int titleIndex = c.getColumnIndex(DBHelper.TITLE);
            int ingredientsIndex = c.getColumnIndex(DBHelper.INGREDIENTS);
            int howToCookIndex = c.getColumnIndex(DBHelper.HOWTOCOOK);
            do {
                Recipe recipe = new Recipe();
                recipe.setId(c.getString(idIndex));
                recipe.setUser(c.getString(userIndex));
                recipe.setTitle(c.getString(titleIndex));
                recipe.setIngredients(c.getString(ingredientsIndex));
                recipe.setHowToCook(c.getString(howToCookIndex));
                recipes.add(recipe);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return recipes;
    }
}