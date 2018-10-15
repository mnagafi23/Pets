/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;
//import inner class of PetsContract to use it directly inside app
import com.example.android.pets.data.PetDbHelper;
import com.example.android.pets.data.PetsContract.PetEntry;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.net.URI;


/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    //subclass of SqliteOpenHelper
    PetDbHelper mDbHelper;

    /** EditText field to enter the pet's name */
    private EditText mNameEditText;

    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;

    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;

    //global variable for uri of current pet in case u want to edit one
    private Uri mUri;

    //Loader id
    private static final int LOADER_ID=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        //initiate subclass of SqliteDbhelper
        mDbHelper = new PetDbHelper(this);
        setupSpinner();

        //get intent if exist
        Intent intent = getIntent();
        if (intent.hasExtra("itemUri")){
            mUri = Uri.parse(intent.getExtras().getString("itemUri")) ;
            setTitle(R.string.edit_page_title);
        }else{
            setTitle(R.string.add_new_pet_page_title);
        }

        //Initiate loader for update hint of editText in UI
        getLoaderManager().initLoader(LOADER_ID,null,this);
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }

    //A method to insert data into database and make an intent
    //into CatalogActivity
    private void insertPet(){


        //get values from view objects
        String petName =mNameEditText.getText().toString().trim();
        String petBreed = mBreedEditText.getText().toString().trim();
        String weightString =mWeightEditText.getText().toString().trim();
        Integer petWeight;
        if (!weightString.isEmpty()){
            petWeight= Integer.parseInt(weightString);
        }else{
            petWeight =null;
        }

        //save data into ContentValue object
        ContentValues values =new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME,petName);
        values.put(PetEntry.COLUMN_PET_BREED,petBreed);
        values.put(PetEntry.COLUMN_PET_WEIGHT,petWeight);
        values.put(PetEntry.COLUMN_PET_GENDER,mGender);

        //uri for return value of insert in PetProvider class
        Uri newRowUri=null;
        try {
            //insert into database using PetProvider class
            newRowUri = getContentResolver().insert(PetEntry.CONTENT_URI,values);
        }catch (IllegalArgumentException e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }


        //check if insertion was successful
        if (newRowUri ==null){
            Toast.makeText(this, R.string.editor_insert_pet_failed, Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, R.string.editor_insert_pet_successful, Toast.LENGTH_SHORT).show();
        }

//        //insert data into database with direct use of PetDbHelper class
//        SQLiteDatabase db = mDbHelper.getWritableDatabase();
//        long newRowId=db.insert(PetEntry.TABLE_NAME,null,values);
//
//        //make intent into Catalog Activity
//        Intent intent = new Intent(this,CatalogActivity.class);
//        //add extra to activity using bundle
//        Bundle extras =new Bundle();
//        extras.putLong("rowId",newRowId);
//        intent.putExtras(extras);
//        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                //insert new pet into database
                insertPet();
                //exit this activity and return to previous one
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    //A method that shows current pets info as hint for textEditors
//    private void showCurrentPet(Uri uri){
//        String [] projection ={
//                PetEntry.COLUMN_PET_NAME,
//                PetEntry.COLUMN_PET_BREED,
//                PetEntry.COLUMN_PET_WEIGHT,
//                PetEntry.COLUMN_PET_GENDER
//        };
//        //cursor that contain info of pet
//        Cursor cursor = getContentResolver().query(uri,projection,null,null,null);
//
//        if (cursor!= null && cursor.moveToFirst()){
//
//            mNameEditText.setHint(cursor.getString(cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME)));
//            mBreedEditText.setHint(cursor.getString(cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED)));
//            mWeightEditText.setHint(cursor.getString(cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT)));
//            mGenderSpinner.setSelection(cursor.getInt(cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER)));
//
//            //free the cursor
//            cursor.close();
//        }
//
//    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String [] projection ={
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_WEIGHT,
                PetEntry.COLUMN_PET_GENDER
        };

        //return the cursor
        if (mUri!= null){
            return new CursorLoader(this,mUri,projection,null,null,null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor!= null && cursor.moveToFirst()){
            //get pet properties from cursor
            String petName =cursor.getString(cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME));
            String petBreed =cursor.getString(cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED));
            Integer petWeight = cursor.getInt(cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT));
            Integer petGender = cursor.getInt(cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER));

            mNameEditText.setText(petName);
            mBreedEditText.setText(petBreed);
            mWeightEditText.setText(Integer.toString(petWeight));
            mGenderSpinner.setSelection(petGender);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Toast.makeText(this, "Loader in EditorActivity reseted", Toast.LENGTH_SHORT).show();
    }
}