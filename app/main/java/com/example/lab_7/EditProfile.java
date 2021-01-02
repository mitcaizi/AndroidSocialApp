package com.example.lab_7;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.UUID;

public class EditProfile extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private EditText displayname, phonenumber;
    private static final int REQUEST_FOR_CAMERA=0011;
    private static final int OPEN_FILE=0012;
    private Uri imageUri=null;
    private DatabaseReference usersRef;
    private ImageView profileImage;


    private void uploadIamge(){
        FirebaseStorage storage= FirebaseStorage.getInstance();
        final String fileNameInStorage = UUID.randomUUID().toString();
        String path="images/"+fileNameInStorage+".jpg";
        final StorageReference imageRef=storage.getReference(path);
        imageRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(final Uri uri) {
                        usersRef.child("profilePicture").setValue(uri.toString()).addOnSuccessListener(

                                new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Picasso.get().load(uri.toString()).transform(new CircleTransform()).into(profileImage);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_FOR_CAMERA && resultCode==RESULT_OK){
            if (imageUri==null)
            {
                Toast.makeText(this, "Error taking photo.", Toast.LENGTH_SHORT).show();
                return;
            }
            uploadIamge();
            return;
        }
        if (requestCode==OPEN_FILE&&resultCode==RESULT_OK){
            imageUri=data.getData();
            uploadIamge();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        phonenumber=findViewById(R.id.phoneNumberText);
        displayname=findViewById(R.id.displayNameText);
        profileImage=findViewById(R.id.profileImage);
        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        final FirebaseDatabase database=FirebaseDatabase.getInstance();
        usersRef = database.getReference("Users/"+currentUser.getUid());
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot dataSnapshot) {
                phonenumber.setText(dataSnapshot.child("phone").getValue().toString());
                displayname.setText(dataSnapshot.child("displayname").getValue().toString());
                if (dataSnapshot.child("profilePicture").exists()){
                    Picasso.get().load(dataSnapshot.child("profilePicture").getValue().toString()).transform(new CircleTransform()).into(profileImage);
                }
            }

            @Override
            public void onCancelled( DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED && requestCode==REQUEST_FOR_CAMERA){
            if (ContextCompat.checkSelfPermission(getBaseContext(),
                    Manifest.permission.CAMERA)
                    ==PackageManager.PERMISSION_GRANTED||ContextCompat.checkSelfPermission(getBaseContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    ==PackageManager.PERMISSION_GRANTED){
                takePhoto();
            }
        }
        else {
            Toast.makeText(this, "We need to access your camera and photos to upload.", Toast.LENGTH_LONG).show();
        }
    }
    private void takePhoto(){
        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION,"From your Camera");
        imageUri=getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        Intent chooser=Intent.createChooser(intent, "Select a Camera App.");
        if (intent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(chooser, REQUEST_FOR_CAMERA); }
    }

    private void checkPermissions(){
        if (ContextCompat.checkSelfPermission(getBaseContext(),
                Manifest.permission.CAMERA)
                !=PackageManager.PERMISSION_GRANTED||ContextCompat.checkSelfPermission(getBaseContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                !=PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "We need permission to access your camera and photo.", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_FOR_CAMERA);
        }
        else {
            takePhoto();
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.takephoto:
                checkPermissions();
                return true;
            case R.id.upload:
                Intent intent= new Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select a file"),OPEN_FILE);
                return true;
            default:
                return false;
        }
    }

    public void uploadProfilePhoto(View view){
        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater= popup.getMenuInflater();
        inflater.inflate(R.menu.popup,popup.getMenu());
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    public void Save(View view){
        if (displayname.getText().toString().equals("")||phonenumber.getText().toString().equals("")){
            Toast.makeText(this, "Please enter your display name and phone number", Toast.LENGTH_SHORT).show();
            return;
        }
        usersRef.child("phone").setValue(phonenumber.getText().toString());
        usersRef.child("displayname").setValue(displayname.getText().toString());
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        finish();
    }

}
