package com.pratishjage.wallpaperbakend;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import id.zelory.compressor.Compressor;

public class UploadWallpaperActivity extends AppCompatActivity {
    FirebaseFirestore db;
    private ImageView wallImgview;
    private TextInputLayout textInputLayout;
    private TextInputEditText nameTxt;
    private TextView releaseDateTxt;
    private TextInputLayout textInputLayout2;
    private TextInputEditText descriptionTxt;
    private Spinner deviceSpinner;
    private Button addWallpaperBtn;
    private String TAG = getClass().getSimpleName();
    private ArrayList<String> devicesDocIds, devices;
    private String selecteddeviceId, selecteddevice;
    private int mYear, mMonth, mDay;
    final Calendar myCalendar = Calendar.getInstance();
    private int PICK_IMAGE_REQUEST = 109;
    Uri filePath;
    private StorageReference storageReference;
    private boolean isImageUploaded;
    Map<String, Object> data;
    private String imgurl;
    private String selectedPlatformId, selectedPlatform, selectedOSId, selectedOS, selectedbrandId, selectedbrandName, selectedOSReleaseDate, ModelNo, deviceDescription, deviceReleaseDate;

    private Double selectedOSversion;
    private File actualImage;
    private File compressedImageFile;
    private String compressedImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_wallpaper);
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        initView();
    }


    private void initView() {

        devicesDocIds = new ArrayList<>();
        devices = new ArrayList<>();
        wallImgview = findViewById(R.id.wall_imgview);
        textInputLayout = findViewById(R.id.textInputLayout);
        nameTxt = findViewById(R.id.name_txt);
        releaseDateTxt = findViewById(R.id.release_date_txt);
        textInputLayout2 = findViewById(R.id.textInputLayout2);
        descriptionTxt = findViewById(R.id.description_txt);
        deviceSpinner = findViewById(R.id.device_spinner);
        addWallpaperBtn = findViewById(R.id.add_wallpaper_btn);
        wallImgview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });
        releaseDateTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker();
            }
        });
        addWallpaperBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = nameTxt.getText().toString();
                String description = descriptionTxt.getText().toString();

                if (name.isEmpty() || description.isEmpty() || !isImageUploaded) {
                    Toast.makeText(UploadWallpaperActivity.this, "Add Fields", Toast.LENGTH_SHORT).show();
                } else {
                    data = new HashMap<>();
                    data.clear();
                    data.put("deviceName", selecteddevice);
                    data.put("deviceID", selecteddeviceId);
                    data.put("description", description);
                    data.put("name", name);
                    data.put("created_at", FieldValue.serverTimestamp());
                    data.put("release_date", myCalendar.getTime());
                    data.put("imgurl", imgurl);
                    data.put("compressed_imgurl", compressedImageUrl);

                    data.put("deviceModelNo", ModelNo);
                    data.put("deviceDescription", deviceDescription);
                    data.put("platform_id", selectedPlatformId);
                    data.put("platform_name", selectedPlatform);
                    //   data.put("created_at", FieldValue.serverTimestamp());
                    data.put("device_release_date", deviceReleaseDate);
                    data.put("osID", selectedOSId);
                    data.put("osName", selectedOS);
                    data.put("os_release_date", selectedOSReleaseDate);
                    data.put("os_version", selectedOSversion);
                    data.put("brandID", selectedbrandId);
                    data.put("brandName", selectedbrandName);

                    addWallpaper(data);
                }


            }
        });

        getDevices();
    }

    private void addWallpaper(Map<String, Object> data) {

        db.collection("debug_wallpaper")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                        Toast.makeText(UploadWallpaperActivity.this, "Wallpaper Added", Toast.LENGTH_SHORT).show();
                        isImageUploaded = false;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                        Toast.makeText(UploadWallpaperActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getDevices() {
        db.collection("devices")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            final QuerySnapshot snapshots = task.getResult();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                devices.add(document.getData().get("deviceName").toString());
                                devicesDocIds.add(document.getId());
                            }
                            ArrayAdapter<String> spinnerAdp = new ArrayAdapter<>(UploadWallpaperActivity.this, R.layout.support_simple_spinner_dropdown_item, devices);
                            deviceSpinner.setAdapter(spinnerAdp);
                            deviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                    selecteddeviceId = devicesDocIds.get(i);
                                    selecteddevice = devices.get(i);

                                    Map<String, Object> singleDoc = snapshots.getDocuments().get(i).getData();
                                    selectedPlatformId = singleDoc.get("platform_id").toString();
                                    selectedPlatform = singleDoc.get("platform_name").toString();
                                    selectedOSId = singleDoc.get("osID").toString();
                                    selectedOS = singleDoc.get("osName").toString();
                                    selectedbrandId = singleDoc.get("brandID").toString();
                                    selectedbrandName = singleDoc.get("brandName").toString();
                                    selectedOSReleaseDate = singleDoc.get("os_release_date").toString();
                                    ModelNo = singleDoc.get("modelNo").toString();
                                    deviceDescription = singleDoc.get("description").toString();
                                    deviceReleaseDate = singleDoc.get("device_release_date").toString();
                                    selectedOSversion = (Double) singleDoc.get("os_version");


                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> adapterView) {

                                }
                            });

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }


    private void showDatePicker() {
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        // final Calendar myCalendar = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {

                        // TODO Auto-generated method stub
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        // txtDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                        String myFormat = "MM/dd/yy"; //In which you need put here
                        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

                        releaseDateTxt.setText(sdf.format(myCalendar.getTime()));

                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
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
                            Toast.makeText(UploadWallpaperActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(UploadWallpaperActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(UploadWallpaperActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(UploadWallpaperActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
