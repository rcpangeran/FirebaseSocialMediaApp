package com.learn.firebasesocialmediaapp;

import android.content.DialogInterface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ViewPostsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private ListView lsvViewPosts_PostsList;
    private ArrayList<String> usernames;
    private ArrayAdapter adapter;
    private FirebaseAuth firebaseAuth;
    private ImageView imgViewPosts_SentPost;
    private TextView txtViewPosts_Description;
    private ArrayList<DataSnapshot> dataSnapshots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_posts);
        assignUI();
        initializeVars();

        // Get posts from someone
        getPostsFromSomeone();

        // Call All OnItemClick Event Handler
        callAllOnItemClickEvent();

        // Call All OnItemLongClick Event Handler
        callAllOnItemLongClickEvent();
    }

    private void assignUI() {
        lsvViewPosts_PostsList = findViewById(R.id.lsvViewPosts_PostsList);
        imgViewPosts_SentPost = findViewById(R.id.imgViewPosts_SentPost);
        txtViewPosts_Description = findViewById(R.id.txtViewPosts_Description);
    }

    private void initializeVars() {
        firebaseAuth = FirebaseAuth.getInstance();
        usernames = new ArrayList<>();
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, usernames);
        dataSnapshots = new ArrayList<>();

        lsvViewPosts_PostsList.setAdapter(adapter);
    }

    private void getPostsFromSomeone() {
        FirebaseDatabase.getInstance().getReference().child("my_users").child(firebaseAuth.getCurrentUser().getUid())
                .child("received_posts").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                dataSnapshots.add(dataSnapshot);
                String fromWhomUsername = (String) dataSnapshot.child("fromWhom").getValue();
                usernames.add(fromWhomUsername);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot snapshot : dataSnapshots) {
                    if (snapshot.getKey().equals(dataSnapshot.getKey())) {
                        dataSnapshots.remove(i);
                        usernames.remove(i);
                    }
                    i++;
                }
                adapter.notifyDataSetChanged();
                imgViewPosts_SentPost.setImageResource(R.drawable.placeholder);
                txtViewPosts_Description.setText("");
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void callAllOnItemClickEvent() {
        lsvViewPosts_PostsList.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case (R.id.lsvViewPosts_PostsList) :
                onItemClick_getPostDetail(position);
                break;
        }
    }

    private void onItemClick_getPostDetail(int position) {
        DataSnapshot myDataSnapShot = dataSnapshots.get(position);
        String downloadLink = (String) myDataSnapShot.child("imageLink").getValue();

        // Set image to ImageView
        Picasso.get().load(downloadLink).into(imgViewPosts_SentPost);

        // Set description to TextView
        txtViewPosts_Description.setText((String) myDataSnapShot.child("des").getValue());
    }

    private void callAllOnItemLongClickEvent() {
        lsvViewPosts_PostsList.setOnItemLongClickListener(this);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        switch (parent.getId()) {
            case (R.id.lsvViewPosts_PostsList) :

                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(ViewPostsActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(ViewPostsActivity.this);
                }
                builder.setTitle("Delete entry")
                        .setMessage("Are you sure you want to delete this entry?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                                FirebaseStorage.getInstance().getReference()
                                        .child("my_images")
                                        .child((String) dataSnapshots.get(position).child("imageIdentifier").getValue())
                                        .delete();

                                FirebaseDatabase.getInstance().getReference()
                                        .child("my_users").child(firebaseAuth.getCurrentUser().getUid())
                                        .child("received_posts")
                                        .child(dataSnapshots.get(position).getKey())
                                        .removeValue();



                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

                break;
        }
        return false;
    }
}
