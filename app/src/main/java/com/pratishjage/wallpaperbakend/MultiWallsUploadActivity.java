package com.pratishjage.wallpaperbakend;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.common.base.Splitter;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.zfdang.multiple_images_selector.ImagesSelectorActivity;
import com.zfdang.multiple_images_selector.SelectorSettings;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import id.zelory.compressor.Compressor;

import static com.pratishjage.wallpaperbakend.Cnstants.IMAGES_ARRAY;

public class MultiWallsUploadActivity extends AppCompatActivity {
    String TAG = getClass().getSimpleName();
    private static final int REQUEST_CODE = 123;
    private ArrayList<String> mResults = new ArrayList<>();
    private String[] imgsArray;
    FirebaseFirestore db;
    private StorageReference storageReference;
    private ArrayList<Uri> mFiles = new ArrayList<>();
    private TextInputEditText descriptionTxt, nameTxt;
    private Button addWallpaperBtn;
    private Spinner deviceSpinner;
    private ArrayList<String> devicesDocIds, devices;
    private String selecteddeviceId, selecteddevice;
    private int mYear, mMonth, mDay;
    final Calendar myCalendar = Calendar.getInstance();
    private String selectedPlatformId, selectedPlatform, selectedOSId, selectedOS, selectedbrandId, selectedbrandName, selectedOSReleaseDate, ModelNo, deviceDescription;
    private Timestamp deviceReleaseDate;
    private TextView releaseDateTxt;
    private Double selectedOSversion;
    private boolean isJobScheduled;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multiple_walls_layout);
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        nameTxt = findViewById(R.id.name_txt);
        descriptionTxt = findViewById(R.id.description_txt);
        deviceSpinner = findViewById(R.id.device_spinner);
        addWallpaperBtn = findViewById(R.id.add_wallpaper_btn);
        releaseDateTxt = findViewById(R.id.release_date_txt);
        devicesDocIds = new ArrayList<>();
        devices = new ArrayList<>();
        findViewById(R.id.wall_imgview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent intent = new Intent(MultiWallsUploadActivity.this, Gallery.class);

                intent.putExtra("title","Select media");
                // Mode 1 for both images and videos selection, 2 for images only and 3 for videos!
                intent.putExtra("mode",1);
                intent.putExtra("maxSelection",3);
                startActivityForResult(intent,23);*/

// start multiple photos selector
                Intent intent = new Intent(MultiWallsUploadActivity.this, ImagesSelectorActivity.class);
// max number of images to be selected
                intent.putExtra(SelectorSettings.SELECTOR_MAX_IMAGE_NUMBER, 25);
// min size of image which will be shown; to filter tiny images (mainly icons)
                intent.putExtra(SelectorSettings.SELECTOR_MIN_IMAGE_SIZE, 100000);
// show camera or not
                intent.putExtra(SelectorSettings.SELECTOR_SHOW_CAMERA, true);
// pass current selected images as the initial value
                intent.putStringArrayListExtra(SelectorSettings.SELECTOR_INITIAL_SELECTED_LIST, mResults);
// start the selector
                startActivityForResult(intent, REQUEST_CODE);

            }
        });
        getDevices();
        addWallpaperBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFiles.size() > 0 && !nameTxt.getText().toString().isEmpty() && !descriptionTxt.getText().toString().isEmpty() && !releaseDateTxt.getText().toString().isEmpty()) {
                    //  startUpload(mFiles);
                    if (!isJobScheduled) {
                        isJobScheduled = true;
                        Data imageData = new Data.Builder().putStringArray(IMAGES_ARRAY, imgsArray).putAll(getWorkdata())
                                .build();

                        OneTimeWorkRequest uploadWorkRequest = new OneTimeWorkRequest.Builder(MultiUploadWork.class)
                                .setInputData(imageData)
                                .build();

                        WorkManager.getInstance().enqueue(uploadWorkRequest);

                    }
                } else {
                    Toast.makeText(MultiWallsUploadActivity.this, "Add Fields", Toast.LENGTH_SHORT).show();
                }


                // buildDataToUpload("test","test",2);
            }
        });

        findViewById(R.id.release_date_txt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                mResults = data.getStringArrayListExtra(SelectorSettings.SELECTOR_RESULTS);
                assert mResults != null;
                imgsArray = mResults.toArray(new String[mResults.size()]);
                // show results in textview
                StringBuffer sb = new StringBuffer();
                sb.append(String.format("Totally %d images selected:", mResults.size())).append("\n");
                for (String result : mResults) {
                    Uri uri = Uri.fromFile(new File(result));
                    mFiles.add(uri);
                }


                //startUpload(mFiles);
                Log.d(TAG, "onActivityResult: " + sb.toString());
                // tvResults.setText(sb.toString());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void startUpload(ArrayList<Uri> mFiles) {
        for (int i = 0; i < mFiles.size(); i++) {
            File compressImage = null;

            compressImage = compressImage(mFiles.get(i));
            if (compressImage != null) {
                uploadCompressed(mFiles.get(i), compressImage, i);
            }


        }
    }

    private File compressImage(Uri uri) {
        File actualImage = null, compressedImageFile = null;
        try {
            actualImage = FileUtil.from(this, uri);
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

        if (actualImage != null && compressedImageFile != null) {
            return compressedImageFile;
        } else {
            return null;
        }
    }


    private void uploadCompressed(final Uri filePath, File compressedImageFile, final int position) {

        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            Uri file = Uri.fromFile(compressedImageFile);

            final StorageReference ref = storageReference.child("testing_compress_wallpaper/" + UUID.randomUUID().toString());
            ref.putFile(file)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(MultiWallsUploadActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                            Task<Uri> downloadUUri = ref.getDownloadUrl();
                            downloadUUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Log.d(TAG, "compress_wallpaper: " + uri.toString());
                                    String compressedImageUrl = uri.toString();
                                    //isImageUploaded = true;
                                    uploadImage(filePath, compressedImageUrl, position);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(MultiWallsUploadActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void uploadImage(Uri filePath, final String compressedImageUrl, final int position) {

        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            final StorageReference ref = storageReference.child("testing_wallpaper/" + UUID.randomUUID().toString());
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(MultiWallsUploadActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                            Task<Uri> downloadUUri = ref.getDownloadUrl();
                            downloadUUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Log.d(TAG, "Wallp_onSuccess: " + uri.toString());
                                    String imgurl = uri.toString();
                                    buildDataToUpload(imgurl, compressedImageUrl, position);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(MultiWallsUploadActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void buildDataToUpload(String imgurl, String compressedImageUrl, int position) {
        String name = nameTxt.getText().toString();
        String description = descriptionTxt.getText().toString();
        HashMap<String, Object> data = new HashMap<>();
        data.clear();
        data.put("deviceName", selecteddevice);
        data.put("deviceID", selecteddeviceId);
        data.put("description", description);
        data.put("name", name + "_" + position);
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
        Log.d(TAG, "buildDataToUpload: " + data.toString());
        addWallpaper(data);
    }

    private void addWallpaper(HashMap<String, Object> data) {
        db.collection("testing_walls")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                        Toast.makeText(MultiWallsUploadActivity.this, "Wallpaper Added", Toast.LENGTH_SHORT).show();
                        //   isImageUploaded = false;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error_adding_document", e);
                        Toast.makeText(MultiWallsUploadActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                            ArrayAdapter<String> spinnerAdp = new ArrayAdapter<>(MultiWallsUploadActivity.this, R.layout.support_simple_spinner_dropdown_item, devices);
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
                                    deviceReleaseDate = (Timestamp) singleDoc.get("device_release_date");
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


    private HashMap<String, Object> getWorkdata() {
        String name = nameTxt.getText().toString();
        String description = descriptionTxt.getText().toString();
        HashMap<String, Object> data = new HashMap<>();
        data.clear();
        data.put("deviceName", selecteddevice);
        data.put("deviceID", selecteddeviceId);
        data.put("description", description);
        data.put("name", name);
        //  data.put("created_at", "created");
        data.put("release_date", myCalendar.getTime().toString());
        data.put("deviceModelNo", ModelNo);
        data.put("deviceDescription", deviceDescription);
        data.put("platform_id", selectedPlatformId);
        data.put("platform_name", selectedPlatform);
        //   data.put("created_at", FieldValue.serverTimestamp());
        data.put("device_release_date", deviceReleaseDate.toDate().toString());
        data.put("osID", selectedOSId);
        data.put("osName", selectedOS);
        data.put("os_release_date", selectedOSReleaseDate);
        data.put("os_version", selectedOSversion);
        data.put("brandID", selectedbrandId);
        data.put("brandName", selectedbrandName);

        data.put("compressed_imgurl", "compress");
        data.put("imgurl", "imageurl");
        Log.d(TAG, "getWorkdata: " + data.toString());
        return data;
    }

    public Map<String, String> convertWithGuava(String mapAsString) {
        return Splitter.on(',').withKeyValueSeparator('=').split(mapAsString);
    }
}
