package com.pratishjage.wallpaperbakend;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
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

public class NewDeviceActivity extends AppCompatActivity {
    FirebaseFirestore db;
    private TextInputEditText mDeviceNameEdt;
    private TextInputEditText mModelNoEdt;
    private TextInputEditText mDescriptionEdt;
    private TextView mReleaseDateTxt;
    private Spinner mOsSpinner;
    private Spinner mPlatformSpinner;
    private Spinner mBrandSpinner;
    private Button mAddDeviceBtn;
    private ArrayList<String> platformDocIds, Platforms, osDocIDs, osList, brandDocIds, brands;
    private String selectedPlatformId, selectedPlatform, selectedOSId, selectedOS, selectedbrandId, selectedbrandName;
    private int mYear, mMonth, mDay;
    final Calendar myCalendar = Calendar.getInstance();
    String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_device);

        db = FirebaseFirestore.getInstance();
        initView();
    }

    private void initView() {
        mDeviceNameEdt = findViewById(R.id.device_name_edt);
        mModelNoEdt = findViewById(R.id.model_no_edt);
        mDescriptionEdt = findViewById(R.id.description_edt);
        mReleaseDateTxt = findViewById(R.id.release_date_txt);
        mOsSpinner = findViewById(R.id.os_spinner);
        mPlatformSpinner = findViewById(R.id.platform_spinner);
        mBrandSpinner = findViewById(R.id.brand_spinner);
        mAddDeviceBtn = findViewById(R.id.add_device_btn);


        platformDocIds = new ArrayList<>();
        Platforms = new ArrayList<>();
        osDocIDs = new ArrayList<>();
        osList = new ArrayList<>();
        brandDocIds = new ArrayList<>();
        brands = new ArrayList<>();

        mReleaseDateTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker();
            }
        });

        getBrands();
        getOS();
        getPlatforms();
    }

    private void getPlatforms() {
        db.collection("platform")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                Platforms.add(document.getData().get("name").toString());
                                platformDocIds.add(document.getId());
                            }
                            ArrayAdapter<String> spinnerAdp = new ArrayAdapter<>(NewDeviceActivity.this, R.layout.support_simple_spinner_dropdown_item, Platforms);
                            mPlatformSpinner.setAdapter(spinnerAdp);
                            mPlatformSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                    selectedPlatformId = platformDocIds.get(i);
                                    selectedPlatform = Platforms.get(i);
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


    private void getOS() {
        db.collection("os")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                osList.add(document.getData().get("name").toString());
                                osDocIDs.add(document.getId());
                            }
                            ArrayAdapter<String> spinnerAdp = new ArrayAdapter<>(NewDeviceActivity.this, R.layout.support_simple_spinner_dropdown_item, osList);
                            mOsSpinner.setAdapter(spinnerAdp);
                            mOsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                    selectedOSId = osDocIDs.get(i);
                                    selectedOS = osList.get(i);
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


    private void getBrands() {
        db.collection("brands")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                brands.add(document.getData().get("name").toString());
                                brandDocIds.add(document.getId());
                            }
                            ArrayAdapter<String> spinnerAdp = new ArrayAdapter<>(NewDeviceActivity.this, R.layout.support_simple_spinner_dropdown_item, brands);
                            mBrandSpinner.setAdapter(spinnerAdp);
                            mBrandSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                    selectedbrandId = brandDocIds.get(i);
                                    selectedbrandName = brands.get(i);
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

                        mReleaseDateTxt.setText(sdf.format(myCalendar.getTime()));

                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }
}
