package com.pratishjage.wallpaperbakend;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class UploadWallpaperActivity extends AppCompatActivity {
    FirebaseFirestore db;
    private ImageButton wallImgview;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_wallpaper);
        db = FirebaseFirestore.getInstance();

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
    }

    private void getPlatforms() {
        db.collection("devices")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                devices.add(document.getData().get("name").toString());
                                devicesDocIds.add(document.getId());
                            }
                            ArrayAdapter<String> spinnerAdp = new ArrayAdapter<>(UploadWallpaperActivity.this, R.layout.support_simple_spinner_dropdown_item, devices);
                            deviceSpinner.setAdapter(spinnerAdp);
                            deviceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                    selecteddeviceId = devicesDocIds.get(i);
                                    selecteddevice = devices.get(i);
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

}
