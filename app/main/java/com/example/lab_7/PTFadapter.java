package com.example.lab_7;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageView;
        import android.widget.TextView;

        import androidx.annotation.NonNull;
        import androidx.annotation.Nullable;
        import androidx.core.content.ContextCompat;
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

        import java.text.SimpleDateFormat;
        import java.util.ArrayList;
        import java.util.Date;
        import java.util.List;

public class PTFadapter extends RecyclerView.Adapter<PTFadapter.ViewHolder> {

    SimpleDateFormat localDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private String userID = PersonToFollow.posterID;

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



    public PTFadapter(RecyclerView recyclerView) {
        usersList =filteredpost= new ArrayList<>();
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

                if (userModel.uid.equals(userID)){
                usersList.add(userModel);
                PTFadapter.this.notifyItemInserted(usersList.size() - 1);
                r.scrollToPosition(usersList.size() - 1);}

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
        return vh;
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {


        final UserModel u = usersList.get(position);
        String uid = u.uid;
        Picasso.get().load(u.url).into(holder.imageView);
        holder.description_v.setText("Discription: "+u.description);


        if (holder.uref != null && holder.urefListener != null)
        {
            holder.uref.removeEventListener(holder.urefListener);
        }
        if (holder.likesRef != null && holder.likesRefListener != null)
        {
            holder.likesRef.removeEventListener(holder.likesRefListener);
        }
        if (holder.likeCountRef != null && holder.likeCountRefListener != null)
        {
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
        DatabaseReference followref;
        ValueEventListener followrefListener;

        DatabaseReference uref;
        ValueEventListener urefListener;
        DatabaseReference likeCountRef;
        ValueEventListener likeCountRefListener;
        DatabaseReference likesRef;
        ValueEventListener likesRefListener;

        public ViewHolder(View v) {
            super(v);
            fname_v = (TextView) v.findViewById(R.id.fname_view);
            posterprofilepicture=(ImageView) v.findViewById(R.id.posterprofilepicture);
            //email_v = (TextView) v.findViewById(R.id.email_view);
            //phone_v = (TextView) v.findViewById(R.id.phone_view);
            date_v = (TextView) v.findViewById(R.id.date_view);
            description_v = v.findViewById(R.id.description);
            imageView = v.findViewById(R.id.postImg);
            likeBtn = v.findViewById(R.id.likeBtn);
            likeCount = v.findViewById(R.id.likeCount);
        }

    }
}