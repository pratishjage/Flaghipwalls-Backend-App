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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class NewOSActivity extends AppCompatActivity {
    FirebaseFirestore db;
    private String TAG = getClass().getSimpleName();
    Map<String, Object> data;
    private TextInputEditText mLocalNameTxt;
    private TextInputEditText mNameTxt;
    private TextInputEditText mVersionNumberTxt, release_date_txt;
    private Button mSaveBtn;
    private Spinner mPlatformSpinner;
    private ArrayList<String> platformDocIds, Platforms;
    private String selectedPlatformId, selectedPlatform;
    private int mYear, mMonth, mDay;
    final Calendar myCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_os);
        db = FirebaseFirestore.getInstance();
        data = new HashMap<>();
        platformDocIds = new ArrayList<>();
        Platforms = new ArrayList<>();

        initView();
    }


    private void addOs(Map<String, Object> data) {

        db.collection("os")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    private void initView() {
        mLocalNameTxt = findViewById(R.id.local_name_txt);
        mNameTxt = findViewById(R.id.name_txt);
        mVersionNumberTxt = findViewById(R.id.version_number_txt);
        release_date_txt = findViewById(R.id.release_date_txt);
        mSaveBtn = findViewById(R.id.save_btn);
        mPlatformSpinner = findViewById(R.id.platform_spinner);
        getPlatforms();
        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String localName = mLocalNameTxt.getText().toString();
                String name = mLocalNameTxt.getText().toString();
                String versionNumber = mVersionNumberTxt.getText().toString();
                if (localName.isEmpty() || name.isEmpty() || versionNumber.isEmpty()) {

                } else {
                    data.put("name", name);
                    data.put("version_number", Double.parseDouble(versionNumber));
                    data.put("local_name", localName);
                    data.put("platform_id", selectedPlatformId);
                    data.put("platform_name", selectedPlatform);
                    data.put("created_at", FieldValue.serverTimestamp());
                    data.put("release_date", myCalendar.getTime());
                    addOs(data);
                }
            }
        });

        release_date_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker();
            }
        });

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
                            ArrayAdapter<String> spinnerAdp = new ArrayAdapter<>(NewOSActivity.this, R.layout.support_simple_spinner_dropdown_item, Platforms);
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

                        release_date_txt.setText(sdf.format(myCalendar.getTime()));

                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }
}
