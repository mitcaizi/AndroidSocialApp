package com.example.lab_7;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder> {
    private Context context;

    SimpleDateFormat localDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");



    class UserModel {
        public String postKey;
        public String uid;
        public String description;
        public String url;
        public String date;

        public UserModel(String uid, String description, String url, String date, String key) {
            this.uid = uid;
            this.description = description;
            this.url = url;
            this.date = date;
            this.postKey = key;
        }
    }

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference usersRef = database.getReference("Posts");
    ChildEventListener usersRefListener;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    public List<UserModel> usersList;
    private RecyclerView r;
    private List<UserModel> filteredpost;

    public MyRecyclerAdapter(RecyclerView recyclerView) {
        usersList = filteredpost = new ArrayList<>();
        r = recyclerView;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        usersRefListener = usersRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //Log.d("LAB7Test", dataSnapshot.toString());
                UserModel userModel = new UserModel(dataSnapshot.child("author").getValue().toString(),
                        dataSnapshot.child("description").getValue().toString(),
                        dataSnapshot.child("url").getValue().toString(),
                        localDateFormat.format(new Date(Long.parseLong(dataSnapshot.child("timestamp").getValue().toString())))
                        , dataSnapshot.getKey());
                usersList.add(userModel);
                MyRecyclerAdapter.this.notifyItemInserted(usersList.size() - 1);
                r.scrollToPosition(usersList.size() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view, parent, false);
        final ViewHolder vh = new ViewHolder(v);
        context = parent.getContext();

        return vh;
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final UserModel u = usersList.get(position);
        String uid = u.uid;
        Picasso.get().load(u.url).into(holder.imageView);
        holder.description_v.setText("Discription: " + u.description);

        if (holder.uref != null && holder.urefListener != null) {
            holder.uref.removeEventListener(holder.urefListener);
        }
        if (holder.likesRef != null && holder.likesRefListener != null) {
            holder.likesRef.removeEventListener(holder.likesRefListener);
        }
        if (holder.likeCountRef != null && holder.likeCountRefListener != null) {
            holder.likeCountRef.removeEventListener(holder.likeCountRefListener);
        }
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        holder.uref = database.getReference("Users").child(uid);
        holder.uref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                holder.fname_v.setText(dataSnapshot.child("displayname").getValue().toString());
                //holder.email_v.setText("Email: " + dataSnapshot.child("email").getValue().toString());
                Picasso.get().load(dataSnapshot.child("profilePicture").getValue().toString()).transform(new CircleTransform()).into(holder.posterprofilepicture);
                //holder.phone_v.setText("Phone Num: " + dataSnapshot.child("phone").getValue().toString());
                holder.date_v.setText("Date Created: " + u.date);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        holder.followref = database.getReference("Posts/" + u.postKey + "/author");
        holder.followref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                holder.fname_v.setOnClickListener(new View.OnClickListener() {


                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), PersonToFollow.class);
                        intent.putExtra("authorID", dataSnapshot.getValue().toString());
                        v.getContext().startActivity(intent);
                    }
                });
                holder.posterprofilepicture.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), PersonToFollow.class);
                        intent.putExtra("authorID", dataSnapshot.getValue().toString());
                        v.getContext().startActivity(intent);

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        holder.likeCountRef = database.getReference("Posts/" + u.postKey + "/likeCount");
        holder.likeCountRefListener = holder.likeCountRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                holder.likeCount.setText(dataSnapshot.getValue().toString() + " Likes");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        holder.likesRef = database.getReference("Posts/" + u.postKey + "/likes/" + currentUser.getUid());
        holder.likesRefListener = holder.likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue().toString().equals("true")) {
                    holder.likeBtn.setImageDrawable(ContextCompat.getDrawable(r.getContext(), R.drawable.like_active));
                } else {
                    holder.likeBtn.setImageDrawable(ContextCompat.getDrawable(r.getContext(), R.drawable.like_disabled));
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database.getReference("Posts/" + u.postKey).runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                        PhotoPreview.Post p = mutableData.getValue(PhotoPreview.Post.class);
                        if (p == null) {
                            return Transaction.success(mutableData);
                        }
                        if (p.likes.containsKey(currentUser.getUid())) {
                            p.likeCount = p.likeCount - 1;
                            p.likes.remove(currentUser.getUid());
                        } else {
                            p.likeCount = p.likeCount + 1;
                            p.likes.put(currentUser.getUid(), true);
                        }

                        mutableData.setValue(p);

                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, boolean b,
                                           @Nullable DataSnapshot dataSnapshot) {
                    }
                });
            }
        });

        DatabaseReference expendcomment=database.getReference("Posts/"+u.postKey+"/comments");
        expendcomment.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot,String s) {
                if (dataSnapshot.exists()){
                    Log.d("LAB7Test", dataSnapshot.toString());
                    List<commentandID> list = new ArrayList<>();

                    String ID= (String) dataSnapshot.child("commenter_id").getValue();
                    String content=(String) dataSnapshot.child("comments_content").getValue();
                    String URL=(String) dataSnapshot.child("comments_URL").getValue();

                    commentandID block= new commentandID(content, ID,URL);
                    //commentandid.getcomments_content(content);
                    //.getcommenter_id(ID);
                    Log.d("LAB7Test", ID.toString());
                    Log.d("LAB7Test", content.toString());
                    list.add(block);


                    for (commentandID item: list){

                        LinearLayout l1 = new LinearLayout(context);
                        l1.setOrientation(LinearLayout.HORIZONTAL);
                        l1.setPadding(0,5,0,0);
                        l1.setWeightSum(6);

                        final TextView tID=new TextView(context);
                       // final ImageView imageView =new ImageView(context);
                        //LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
                        //imageView.setLayoutParams(params1);
                       // Picasso.get().load(item.commenter_URL).transform(new CircleTransform()).into(imageView);


                        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 6);
                        tID.setLayoutParams(params2);
                        tID.setText(item.commenter_id+":  "+item.comments_content);

                       // l1.addView(imageView);
                        l1.addView(tID);


                        holder.commentsection.addView(l1);
                    }

                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference getuserinfor=database.getReference("Users/"+currentUser.getUid());
        holder.arrayList=new ArrayList<>();
        getuserinfor.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    holder.UserName_comment= String.valueOf(dataSnapshot.child("displayname").getValue());
                    holder.UserURL_comment=String.valueOf(dataSnapshot.child("profilePicture").getValue());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        holder.comment_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View view) {


                DatabaseReference comment_reference = database.getReference("Posts/" + u.postKey + "/comments");
                DatabaseReference newcomment = comment_reference.push();
                newcomment.setValue(new commentandID(holder.comment.getText().toString(), holder.UserName_comment, holder.UserURL_comment));

            }
        });
    }


    public void removeListener() {
        if (usersRef != null && usersRefListener != null)
            usersRef.removeEventListener(usersRefListener);
    }


    @Override
    public int getItemCount() {
        return usersList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView fname_v;
        //public TextView email_v;
        //public TextView phone_v;
        public TextView date_v;
        public TextView description_v;
        public ImageView imageView;
        public ImageView posterprofilepicture;
        public ImageView likeBtn;
        public TextView likeCount;
        public EditText comment;
        public Button comment_button;
        LinearLayout commentsection;
        public String UserName_comment;
        public String UserURL_comment;
        public List<commentandID> Comment_items;
        public ArrayList<String> arrayList;
        DatabaseReference CommentChildref;
        ChildEventListener CommentChildListener;
        DatabaseReference followref;
        //ValueEventListener followrefListener;
        DatabaseReference uref;
        ValueEventListener urefListener;
        DatabaseReference likeCountRef;
        ValueEventListener likeCountRefListener;
        DatabaseReference likesRef;
        ValueEventListener likesRefListener;
        DatabaseReference used_for_comment;
        DatabaseReference commentsaving;



        public ViewHolder(View v) {
            super(v);
            fname_v = (TextView) v.findViewById(R.id.fname_view);
            posterprofilepicture = (ImageView) v.findViewById(R.id.posterprofilepicture);
            //email_v = (TextView) v.findViewById(R.id.email_view);
            //phone_v = (TextView) v.findViewById(R.id.phone_view);
            date_v = (TextView) v.findViewById(R.id.date_view);
            description_v = v.findViewById(R.id.description);
            imageView = v.findViewById(R.id.postImg);
            likeBtn = v.findViewById(R.id.likeBtn);
            likeCount = v.findViewById(R.id.likeCount);
            comment = v.findViewById(R.id.comment_TXT_ID);
            comment_button = v.findViewById(R.id.comment_btn_ID);
            commentsection = v.findViewById(R.id.putcommentunderme);




        }
    }
}