package com.cybrixx.ecomart.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import com.cybrixx.ecomart.model.CartItem;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "EcoMartLocal.db";
    private static final int DATABASE_VERSION = 1;

    // Table and Column names
    private static final String TABLE_CART = "cart";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_PRODUCT_ID = "product_id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PRICE = "price";
    private static final String COLUMN_QUANTITY = "quantity";
    private static final String COLUMN_IMAGE_URL = "image_url";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the cart table
        String createTable = "CREATE TABLE " + TABLE_CART + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PRODUCT_ID + " INTEGER, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_PRICE + " REAL, " +
                COLUMN_QUANTITY + " INTEGER, " +
                COLUMN_IMAGE_URL + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        onCreate(db);
    }

    // --- Insert or Update Item in Cart ---
    public boolean addToCart(CartItem item) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if item already exists in the cart
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CART + " WHERE " + COLUMN_PRODUCT_ID + "=?", new String[]{String.valueOf(item.getProductId())});

        if (cursor.getCount() > 0) {
            // Item exists, update quantity
            cursor.moveToFirst();
            int currentQty = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY));
            ContentValues values = new ContentValues();
            values.put(COLUMN_QUANTITY, currentQty + 1);

            int result = db.update(TABLE_CART, values, COLUMN_PRODUCT_ID + "=?", new String[]{String.valueOf(item.getProductId())});
            cursor.close();
            return result > 0;
        } else {
            // New item, insert it
            ContentValues values = new ContentValues();
            values.put(COLUMN_PRODUCT_ID, item.getProductId());
            values.put(COLUMN_NAME, item.getName());
            values.put(COLUMN_PRICE, item.getPrice());
            values.put(COLUMN_QUANTITY, item.getQuantity());
            values.put(COLUMN_IMAGE_URL, item.getImageUrl());

            long result = db.insert(TABLE_CART, null, values);
            cursor.close();
            return result != -1;
        }
    }

    // --- Get All Cart Items ---
    public List<CartItem> getCartItems() {
        List<CartItem> cartList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CART, null);

        if (cursor.moveToFirst()) {
            do {
                CartItem item = new CartItem(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL))
                );
                item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                cartList.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return cartList;
    }

    // --- Clear Cart (Call this after successful checkout) ---
    public void clearCart() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_CART);
    }

    // --- Update Item Quantity ---
    public void updateQuantity(int cartId, int newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_QUANTITY, newQuantity);
        db.update(TABLE_CART, values, COLUMN_ID + "=?", new String[]{String.valueOf(cartId)});
    }

    // --- Delete Single Item ---
    public void deleteItem(int cartId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CART, COLUMN_ID + "=?", new String[]{String.valueOf(cartId)});
    }
}
