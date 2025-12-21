package com.aastha.colorassistapp;

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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;

public class EmailsFragment extends Fragment {

    private TextView headingEmailTxt, descEmailTxt, uploadImageTxt, resultTxtEmail;
    private Button generateBtnEmail;
    private Button uploadMailsButton;
    private ImageView imageViewEmail;

    private Bitmap currentBitmap;
    private Uri currentImageUri;

    // Modern photo picker launcher
    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    loadImageFromUri(uri);
                } else {
                    Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_emails, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        headingEmailTxt = view.findViewById(R.id.headingEmailTxt);
        descEmailTxt = view.findViewById(R.id.descEmailTxt);
        uploadImageTxt = view.findViewById(R.id.uploadImageTxt);
        uploadMailsButton = view.findViewById(R.id.uploadMailsBtn);
        generateBtnEmail = view.findViewById(R.id.generateBtnEmail);
        resultTxtEmail = view.findViewById(R.id.resultTxtEmail);
        imageViewEmail = view.findViewById(R.id.imageViewEmail);

        // ImageView stays hidden at start (defined in XML)
        imageViewEmail.setVisibility(View.GONE);

        // Upload image
        uploadMailsButton.setOnClickListener(v -> launchPhotoPicker());

        // Extract text
        generateBtnEmail.setOnClickListener(v -> {
            if (currentImageUri != null) {
                runTextRecognition(currentImageUri);
            } else {
                Toast.makeText(getContext(), "Please select an image first!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void launchPhotoPicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 100);
                return;
            }
        }
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void loadImageFromUri(Uri uri) {
        try {
            currentBitmap = android.provider.MediaStore.Images.Media.getBitmap(
                    requireContext().getContentResolver(), uri);
            currentImageUri = uri;

            // ✅ Show ImageView once image is loaded
            imageViewEmail.setImageBitmap(currentBitmap);
            imageViewEmail.setVisibility(View.VISIBLE);

            resultTxtEmail.setText("Image loaded. Tap Extract Text to start OCR.");
        } catch (IOException e) {
            Log.e("FragmentEmails", "Error loading image", e);
            Toast.makeText(getContext(), "Error loading image", Toast.LENGTH_SHORT).show();
        }
    }

    private void runTextRecognition(Uri uri) {
        try {
            InputImage image = InputImage.fromFilePath(requireContext(), uri);
            com.google.mlkit.vision.text.TextRecognizer recognizer =
                    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            resultTxtEmail.setText("Extracting text... please wait.");

            recognizer.process(image)
                    .addOnSuccessListener(this::processTextRecognitionResult)
                    .addOnFailureListener(e ->
                            resultTxtEmail.setText("Error: " + e.getMessage()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processTextRecognitionResult(Text result) {
        StringBuilder resultText = new StringBuilder();
        for (Text.TextBlock block : result.getTextBlocks()) {
            resultText.append(block.getText()).append("\n");
        }

        if (resultText.length() > 0)
            resultTxtEmail.setText(resultText.toString());
        else
            resultTxtEmail.setText("No text detected in the image.");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            launchPhotoPicker();
        } else {
            Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}
