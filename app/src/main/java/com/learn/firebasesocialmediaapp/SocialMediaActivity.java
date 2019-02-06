package com.learn.firebasesocialmediaapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class SocialMediaActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private FirebaseAuth mAuth;
    private ImageView imgSocialMedia_PostImageView;
    private Button btnSocialMedia_Create;
    private EditText edtSocialMedia_Desc;
    private ListView lsvSocialMedia_UserListView;
    private Bitmap bitmap;
    private String imageIdentifier;
    private String imageDownloadLink;
    private ArrayList<String> usernamesArrayList;
    private ArrayAdapter adapter;
    private ArrayList<String> uids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_media);
        assignUI();
        initializeVars();

        // Call All OnClick Event Handler
        callAllOnClickEvent();

        // Call All OnItemClick Event Handler
         callAllOnItemClickEvent();
    }

    private void assignUI() {
        imgSocialMedia_PostImageView = findViewById(R.id.imgSocialMedia_PostImageView);
        btnSocialMedia_Create = findViewById(R.id.btnSocialMedia_Create);
        edtSocialMedia_Desc = findViewById(R.id.edtSocialMedia_Desc);
        lsvSocialMedia_UserListView = findViewById(R.id.lsvSocialMedia_UserListView);
    }

    private void initializeVars() {

        mAuth = FirebaseAuth.getInstance();
        usernamesArrayList = new ArrayList<>();
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, usernamesArrayList);
        uids = new ArrayList<>();

        lsvSocialMedia_UserListView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_socialmedia, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.menuSocialMedia_LogOut) :
                onOptionsItemSelected_LogOut();
                break;
            case (R.id.menuSocialMedia_ViewPostsItem) :
                Intent intent = new Intent(this, ViewPostsActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onOptionsItemSelected_LogOut() {
        mAuth.signOut();
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        onOptionsItemSelected_LogOut();
    }

    private void callAllOnClickEvent() {
        imgSocialMedia_PostImageView.setOnClickListener(this);
        btnSocialMedia_Create.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case (R.id.imgSocialMedia_PostImageView) :
                onClick_SelectImage();
                break;
            case (R.id.btnSocialMedia_Create) :
                onClick_UploadImageToServer();
                break;
        }

    }

    private void onClick_SelectImage() {
        if (Build.VERSION.SDK_INT < 23) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 1000);
        } else if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
            } else {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1000);
            }
        }
    }

    private void onClick_UploadImageToServer() {
        if (bitmap != null) {
            final ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage("Please wait...");
            dialog.show();

            // Get the data from an ImageView as bytes
            imgSocialMedia_PostImageView.setDrawingCacheEnabled(true);
            imgSocialMedia_PostImageView.buildDrawingCache();
            Bitmap bitmap = ((BitmapDrawable) imgSocialMedia_PostImageView.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();

            imageIdentifier = UUID.randomUUID() + ".png";

            UploadTask uploadTask = FirebaseStorage.getInstance().getReference().child("my_images").child(imageIdentifier).putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Handle unsuccessful uploads
                    dialog.dismiss();
                    Toast.makeText(SocialMediaActivity.this, "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                    dialog.dismiss();
                    Toast.makeText(SocialMediaActivity.this, "Successfully uploaded image", Toast.LENGTH_SHORT).show();
                    edtSocialMedia_Desc.setVisibility(View.VISIBLE);

                    FirebaseDatabase.getInstance().getReference().child("my_users").addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            // Get key for each user
                            uids.add(dataSnapshot.getKey());

                            // Get username
                            String username = (String) dataSnapshot.child("username").getValue();

                            // Put username to ArrayList
                            usernamesArrayList.add(username);

                            // Populate ListView
                            adapter.notifyDataSetChanged();
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

                    taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                imageDownloadLink = task.getResult().toString();
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1000 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onClick_SelectImage();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000 && resultCode == RESULT_OK && data != null) {
            Uri chosenImageData = data.getData();

            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), chosenImageData);
                imgSocialMedia_PostImageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void callAllOnItemClickEvent() {
        lsvSocialMedia_UserListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case (R.id.lsvSocialMedia_UserListView) :
                onItemClick_SendDataToUsers(position);
                break;
        }
    }

    private void onItemClick_SendDataToUsers(int position) {
        HashMap<String, String> dataMap = new HashMap<>();
        dataMap.put("fromWhom", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        dataMap.put("imageIdentifier", imageIdentifier);
        dataMap.put("imageLink", imageDownloadLink);
        dataMap.put("des", edtSocialMedia_Desc.getText().toString());

        FirebaseDatabase.getInstance().getReference().child("my_users").child(uids.get(position))
                .child("received_posts").push().setValue(dataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(SocialMediaActivity.this, "Data sent", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
