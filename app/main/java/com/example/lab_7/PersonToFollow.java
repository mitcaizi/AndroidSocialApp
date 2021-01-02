package com.example.lab_7;


import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.renderscript.Sampler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PersonToFollow extends AppCompatActivity {


    public static String posterID;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    //private static final int REQUEST_FOR_CAMERA=0011;
    private static final int OPEN_FILE=0012;
    private Uri imageUri=null;
    PTFadapter pTFadapter;
    DatabaseReference personRef;
    DatabaseReference followRef;

    ValueEventListener followRefListener;
    private ImageView followBtn;
    Context context = PersonToFollow.this;
    boolean SELF_OR_NOT=false;
    DatabaseReference hideandseekcurrentuser;
    DatabaseReference hideandseektargetuser;
    FirebaseDatabase firebaseDatabase;





    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_to_follow);
        Intent intent=getIntent();
        Bundle extras= intent.getExtras();
        posterID=extras.getString("authorID");
        mAuth=FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        final RecyclerView recyclerView=findViewById(R.id.persontofollowrecycerview);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        layoutManager.scrollToPosition(0);
        recyclerView.setLayoutManager(layoutManager);
        pTFadapter=new PTFadapter(recyclerView);
        recyclerView.setAdapter(pTFadapter);

        followBtn=findViewById(R.id.un_followbtn);
        final TextView textView_name=findViewById(R.id.persontofollowname);
        final ImageView posterprofilepicture=findViewById(R.id.persontofollowpicture);
        final  TextView textView_email=findViewById(R.id.persontofollowemail);
        final TextView texview_phone=findViewById(R.id.persontofollowphone);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();


        personRef = database.getReference("Users/" + posterID);
        personRef.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

              textView_name.setText(dataSnapshot.child("displayname").getValue().toString());
               textView_email.setText("Email: "+ dataSnapshot.child("email").getValue().toString());
               texview_phone.setText("Phone: " + dataSnapshot.child("phone").getValue().toString());
               Picasso.get().load(dataSnapshot.child("profilePicture").getValue().toString()).transform(new CircleTransform()).into(posterprofilepicture);
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });



        followRef = database.getReference("Users/"+posterID+"/follower");
        followRefListener=followRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()&&dataSnapshot.getValue().toString().contains(currentUser.getUid())){
                    followBtn.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.enable_follow));
                }
                else{
                    followBtn.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.un_follow));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database.getReference("Users/"+posterID).runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                            User user= mutableData.getValue(User.class);

                            if (!(currentUser.getUid().equals(posterID))) {
                                SELF_OR_NOT=false;
                                if (user == null) {
                                    return Transaction.success(mutableData);
                                }
                                if (user.follower.containsKey(currentUser.getUid())) {
                                    user.follower.remove(currentUser.getUid());
                                } else {
                                    user.follower.put(currentUser.getUid(), true);
                                }
                            }
                            mutableData.setValue(user);
                            return Transaction.success(mutableData);
                     }
                    @Override
                    public void onComplete( DatabaseError databaseError, boolean b,
                     DataSnapshot dataSnapshot) {

                    }
                });
                     if((currentUser.getUid().equals(posterID))){
                         Toast.makeText(context, "Following your own profile is forbidden ", Toast.LENGTH_SHORT).show();
                }
            }
        });



        updateUI();
   }

    private void updateUI() {
        firebaseDatabase= FirebaseDatabase.getInstance();
        hideandseekcurrentuser=firebaseDatabase.getReference("Users");
        hideandseekcurrentuser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               if(
                       (currentUser.getUid().equals(hideandseekcurrentuser.child(posterID).getKey()))
                               ||
                       (
                       dataSnapshot.child(posterID).child("follower").hasChild(currentUser.getUid())
                               &&
                       dataSnapshot.child(currentUser.getUid()).child("follower").hasChild(posterID)
                       )
               )
               {
                   findViewById(R.id.persontofollowemail).setVisibility(View.VISIBLE);
                   findViewById(R.id.persontofollowphone).setVisibility(View.VISIBLE);
                   findViewById(R.id.persontofollowrecycerview).setVisibility(View.VISIBLE);
               }

               else{
                        findViewById(R.id.persontofollowemail).setVisibility(View.GONE);
                        findViewById(R.id.persontofollowphone).setVisibility(View.GONE);
                       findViewById(R.id.persontofollowrecycerview).setVisibility(View.GONE);
               }


            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.persontofollowmenu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.signout:
                mAuth.signOut();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pTFadapter.removeListener();
    }


}
