package com.pratishjage.wallpaperbakend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import id.zelory.compressor.Compressor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PremiumWall extends AppCompatActivity {
    FirebaseFirestore db;
    private ImageView wallImgview;
    private TextInputEditText nameTxt;
    private Button addWallpaperBtn;
    private String TAG = getClass().getSimpleName();
    Uri filePath;
    private StorageReference storageReference;
    private boolean isImageUploaded;
    private File actualImage;
    private File compressedImageFile;
    private int PICK_IMAGE_REQUEST = 109;
    private String compressedImageUrl;
    Map<String, Object> data;
    private String imgurl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premium_wall);
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        nameTxt = findViewById(R.id.name_txt);
        wallImgview = findViewById(R.id.wall_imgview);
        addWallpaperBtn = findViewById(R.id.add_wallpaper_btn);
        wallImgview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

        addWallpaperBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = nameTxt.getText().toString();


                if (name.isEmpty() || !isImageUploaded) {
                    Toast.makeText(PremiumWall.this, "Add Fields", Toast.LENGTH_SHORT).show();
                } else {
                    data = new HashMap<>();
                    data.clear();

                    data.put("name", name);
                    data.put("created_at", FieldValue.serverTimestamp());

                    data.put("imgurl", imgurl);
                    data.put("compressed_imgurl", compressedImageUrl);


                    addWallpaper(data);
                }


            }
        });

    }

    private void addWallpaper(Map<String, Object> data) {

        db.collection("premiumcollection")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                        Toast.makeText(PremiumWall.this, "Wallpaper Added", Toast.LENGTH_SHORT).show();
                        isImageUploaded = false;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                        Toast.makeText(PremiumWall.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                actualImage = FileUtil.from(this, data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                compressedImageFile = new Compressor(this).compressToFile(actualImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            uploadCompressed();


        }
    }

    private void uploadCompressed() {

        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            Uri file = Uri.fromFile(compressedImageFile);

            final StorageReference ref = storageReference.child("compress_wallpaper/" + UUID.randomUUID().toString());
            ref.putFile(file)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(PremiumWall.this, "Uploaded", Toast.LENGTH_SHORT).show();
                            Task<Uri> downloadUUri = ref.getDownloadUrl();
                            downloadUUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Log.d(TAG, "compress_wallpaper: " + uri.toString());
                                    compressedImageUrl = uri.toString();
                                    //isImageUploaded = true;
                                    uploadImage();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(PremiumWall.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        }
    }

    private void uploadImage() {

        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            final StorageReference ref = storageReference.child("debug_wallpaper/" + UUID.randomUUID().toString());
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(PremiumWall.this, "Uploaded", Toast.LENGTH_SHORT).show();
                            Task<Uri> downloadUUri = ref.getDownloadUrl();
                            downloadUUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Log.d(TAG, "Wallp_onSuccess: " + uri.toString());
                                    imgurl = uri.toString();
                                    isImageUploaded = true;
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(PremiumWall.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        }
    }
}
