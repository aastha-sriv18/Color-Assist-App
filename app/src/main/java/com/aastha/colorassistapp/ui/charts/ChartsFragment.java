package com.aastha.colorassistapp.ui.charts;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.aastha.colorassistapp.R;

import java.io.IOException;

public class ChartsFragment extends Fragment {

    private ColorblindnessSimulationView chartView;
    private Spinner colorblindnessSpinner;
    private Button uploadChartBtn;
    private Button generateBtn;
    private Bitmap originalBitmap;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private ColorblindnessSimulationView.ColorblindnessMode currentMode = ColorblindnessSimulationView.ColorblindnessMode.NONE;
    private static final String TAG = "ChartsFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize the photo picker launcher
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                loadChartImage(uri);
            } else {
                Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_charts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        uploadChartBtn = view.findViewById(R.id.btn_upload_chart);
        chartView = view.findViewById(R.id.chart_simulation_view);
        colorblindnessSpinner = view.findViewById(R.id.spinner_colorblindness);
        generateBtn = view.findViewById(R.id.btn_generate);
        
        // Setup upload button
        uploadChartBtn.setOnClickListener(v -> launchImagePicker());
        
        // Setup generate button - initially disabled
        generateBtn.setOnClickListener(v -> generateChart());
        updateGenerateButtonState();
        
        // Setup colorblindness spinner
        setupColorblindnessSpinner();
    }

    private void setupColorblindnessSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            getContext(),
            android.R.layout.simple_spinner_item,
            new String[]{
                "None (Normal Vision)",
                "Deuteranopia (Red-Green, Green-sensitive)",
                "Protanopia (Red-Green, Red-sensitive)",
                "Tritanopia (Blue-Yellow)"
            }
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorblindnessSpinner.setAdapter(adapter);
        colorblindnessSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateColorblindnessMode(position);
                updateGenerateButtonState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateColorblindnessMode(int position) {
        switch (position) {
            case 0:
                currentMode = ColorblindnessSimulationView.ColorblindnessMode.NONE;
                break;
            case 1:
                currentMode = ColorblindnessSimulationView.ColorblindnessMode.DEUTERANOPIA;
                break;
            case 2:
                currentMode = ColorblindnessSimulationView.ColorblindnessMode.PROTANOPIA;
                break;
            case 3:
                currentMode = ColorblindnessSimulationView.ColorblindnessMode.TRITANOPIA;
                break;
        }
        Log.d(TAG, "Colorblindness mode selected: " + currentMode);
    }

    private void generateChart() {
        if (originalBitmap == null) {
            Toast.makeText(getContext(), "Please upload a chart image first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Apply current settings
        chartView.setBitmap(originalBitmap);
        chartView.setColorblindnessMode(currentMode);
        
        Toast.makeText(getContext(), "Chart generated with " + currentMode, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Chart generated with mode: " + currentMode);
    }

    private void updateGenerateButtonState() {
        boolean enabled = originalBitmap != null;
        generateBtn.setEnabled(enabled);
        generateBtn.setAlpha(enabled ? 1.0f : 0.5f);
    }

    private void launchImagePicker() {
        // Request READ_MEDIA_IMAGES permission for API 33+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 100);
                return;
            }
        }
        
        // Launch photo picker
        pickMedia.launch(new PickVisualMediaRequest.Builder()
            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
            .build());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            launchImagePicker();
        } else {
            Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadChartImage(Uri uri) {
        // Show loading state instantly
        chartView.setBitmap(null);

        // Load in background thread
        new Thread(() -> {
            try {
                Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(
                        requireContext().getContentResolver(), uri);

                // Scale down if too large (original method - works reliably)
                int maxDimension = 2048;
                if (bitmap.getWidth() > maxDimension || bitmap.getHeight() > maxDimension) {
                    float scale = Math.min((float) maxDimension / bitmap.getWidth(),
                            (float) maxDimension / bitmap.getHeight());
                    int newWidth = (int) (bitmap.getWidth() * scale);
                    int newHeight = (int) (bitmap.getHeight() * scale);
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
                    bitmap.recycle();
                    bitmap = scaledBitmap;
                }

                originalBitmap = bitmap;

                // Update UI on main thread
                requireActivity().runOnUiThread(() -> {
                    chartView.setBitmap(originalBitmap);
                    chartView.setColorblindnessMode(ColorblindnessSimulationView.ColorblindnessMode.NONE);
                    updateGenerateButtonState();
                    Toast.makeText(getContext(), "Chart loaded successfully", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Chart loaded: " + originalBitmap.getWidth() + "x" + originalBitmap.getHeight());
                });

            } catch (IOException e) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error loading image", Toast.LENGTH_SHORT).show();
                });
                Log.e(TAG, "Error loading chart image", e);
            }
        }).start();
    }


}
