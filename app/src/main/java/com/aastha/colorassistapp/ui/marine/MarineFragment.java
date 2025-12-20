package com.aastha.colorassistapp.ui.marine;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.aastha.colorassistapp.R;

public class MarineFragment extends Fragment {

    private TextView marineHeadingTxt, descTxt, uploadTxt;
    private Button uploadBtn;
    private ImageView imageViewMarine;
    private Spinner spinner;
    private Uri selectedImageUri;
    private Button generateBtn;
    private String selectedTest = "";
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_marine, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        marineHeadingTxt = view.findViewById(R.id.marineHeadingTxt);
        descTxt = view.findViewById(R.id.descTxt);
        uploadTxt = view.findViewById(R.id.uploadTxt);
        uploadBtn = view.findViewById(R.id.uploadBtn);
        uploadBtn = view.findViewById(R.id.uploadBtn);
        imageViewMarine = view.findViewById(R.id.imageViewMarine);
        spinner = view.findViewById(R.id.dropdown);
        generateBtn = view.findViewById(R.id.generateBtn);

        // Hide image initially
        imageViewMarine.setVisibility(View.GONE);

        // Register image picker
        pickMedia = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        imageViewMarine.setVisibility(View.VISIBLE);
                        imageViewMarine.setImageURI(uri);
                        Log.d("MarineFragment", "Image selected: " + uri);
                    } else {
                        Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );


        if (uploadBtn != null) {
            uploadBtn.setOnClickListener(v -> launchPhotoPicker());
        }
        setupSpinner();
    }


    private void setupSpinner() {
        if (getContext() == null) return;

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.test_names,
                android.R.layout.simple_spinner_item
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String test = parent.getItemAtPosition(position).toString();
                Toast.makeText(getContext(), "Selected: " + selectedTest, Toast.LENGTH_SHORT).show();

                if (position == 0) { // placeholder "Select Test"
                    selectedTest = "";
                } else {
                    selectedTest = test;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedTest = "";
            }
        });
    }

    private void launchPhotoPicker() {
        if (getContext() == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 100);
                return;
            }
        }

        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchPhotoPicker();
            } else {
                Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Generate button runs the selected test
    generateBtn.setOnClickListener(v -> {
            if (selectedImageUri == null) {
                Toast.makeText(getContext(), "Please upload an image first", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedTest.isEmpty()) {
                Toast.makeText(getContext(), "Please select a test", Toast.LENGTH_SHORT).show();
                return;
            }

            switch (selectedTest.toLowerCase()) {
                case "ph test":
                    performPhTest();
                    break;
                case "nitrate test":
                    performNitrateTest();
                    break;
                case "chlorophyll test":
                    performChlorophyllTest();
                    break;
                case "ammonia test":
                    performAmmoniaTest();
                    break;
            }
    });

    // Placeholder functions (currently empty)
    private void performPhTest() {
        // TODO: Implement logic for pH test
    }

    private void performNitrateTest() {
        // TODO: Implement logic for Nitrate test
    }

    private void performChlorophyllTest() {
        // TODO: Implement logic for Chlorophyll test
    }

    private void performAmmoniaTest() {
        // TODO: Implement logic for Ammonia test
    }
}
