package com.aastha.colorassistapp.ui.marine;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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

import java.io.IOException;

public class MarineFragment extends Fragment {

    private TextView infoText;
    private Button uploadBtn, generateBtn;
    private ImageView imageViewMarine;
    private Spinner spinner;
    private Uri selectedImageUri;
    private String selectedTest = "";
    private Bitmap currentBitmap;
    private View tapIndicator;
    private int selectedColor = 0;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_marine, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        uploadBtn = view.findViewById(R.id.uploadBtn);
        imageViewMarine = view.findViewById(R.id.imageViewMarine);
        spinner = view.findViewById(R.id.dropdown);
        generateBtn = view.findViewById(R.id.generateBtn);
        tapIndicator = view.findViewById(R.id.tapIndicator);
        infoText = view.findViewById(R.id.infoText);

        imageViewMarine.setVisibility(View.GONE);

        pickMedia = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        loadImageFromUri(uri);
                    } else {
                        Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        uploadBtn.setOnClickListener(v -> launchPhotoPicker());
        setupSpinner();

        imageViewMarine.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN && currentBitmap != null) {
                handleImageTap(event);
            }
            return true;
        });

        generateBtn.setOnClickListener(v -> runTest());
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
                selectedTest = parent.getItemAtPosition(position).toString().toLowerCase();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedTest = "";
            }
        });
    }

    private void launchPhotoPicker() {
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

    private void loadImageFromUri(Uri uri) {
        try {
            currentBitmap = android.provider.MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), uri);
            imageViewMarine.setImageBitmap(currentBitmap);
            imageViewMarine.setVisibility(View.VISIBLE);
            infoText.setText("Tap on the image to select a test color");
        } catch (IOException e) {
            Log.e("MarineFragment", "Error loading image", e);
            Toast.makeText(getContext(), "Error loading image", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleImageTap(MotionEvent event) {
        float tapX = event.getX();
        float tapY = event.getY();

        int imageViewWidth = imageViewMarine.getWidth();
        int imageViewHeight = imageViewMarine.getHeight();
        int bitmapWidth = currentBitmap.getWidth();
        int bitmapHeight = currentBitmap.getHeight();

        float scaleX = (float) bitmapWidth / imageViewWidth;
        float scaleY = (float) bitmapHeight / imageViewHeight;

        int bitmapX = Math.round(tapX * scaleX);
        int bitmapY = Math.round(tapY * scaleY);
        bitmapX = Math.max(0, Math.min(bitmapX, bitmapWidth - 1));
        bitmapY = Math.max(0, Math.min(bitmapY, bitmapHeight - 1));

        selectedColor = getAverageColorInRadius(bitmapX, bitmapY, 3);

        tapIndicator.setVisibility(View.VISIBLE);
        tapIndicator.setX(tapX - tapIndicator.getWidth() / 2f);
        tapIndicator.setY(tapY - tapIndicator.getHeight() / 2f);
    }

    private int getAverageColorInRadius(int centerX, int centerY, int radius) {
        int totalRed = 0, totalGreen = 0, totalBlue = 0, pixelCount = 0;

        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int y = centerY - radius; y <= centerY + radius; y++) {
                if (x >= 0 && x < currentBitmap.getWidth() && y >= 0 && y < currentBitmap.getHeight()) {
                    int dx = x - centerX;
                    int dy = y - centerY;
                    if (dx * dx + dy * dy <= radius * radius) {
                        int pixel = currentBitmap.getPixel(x, y);
                        totalRed += android.graphics.Color.red(pixel);
                        totalGreen += android.graphics.Color.green(pixel);
                        totalBlue += android.graphics.Color.blue(pixel);
                        pixelCount++;
                    }
                }
            }
        }

        if (pixelCount == 0) return android.graphics.Color.BLACK;
        return android.graphics.Color.rgb(totalRed / pixelCount, totalGreen / pixelCount, totalBlue / pixelCount);
    }

    private void runTest() {
        if (selectedImageUri == null) {
            Toast.makeText(getContext(), "Please upload an image first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedTest.isEmpty() || selectedTest.equals("select test")) {
            Toast.makeText(getContext(), "Please select a test", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedColor == 0) {
            Toast.makeText(getContext(), "Please tap on a color in the image", Toast.LENGTH_SHORT).show();
            return;
        }

        String hex = colorToHex(selectedColor);
        String colorName = getNearestColorName(selectedColor);
        String result = getColorMatchResult(selectedTest, selectedColor, colorName);

        infoText.setText("Detected Color: " + colorName + "\nHEX: " + hex + "\n" + result);
    }

    // ---------- Improved color mapping ----------

    private String getColorMatchResult(String test, int color, String colorName) {
        String description;
        switch (test) {
            case "ph test":
                description = interpretPh(color);
                break;
            case "ammonia test":
                description = interpretAmmonia(color);
                break;
            case "nitrite test":
                description = interpretNitrite(color);
                break;
            case "nitrate test":
                description = interpretNitrate(color);
                break;
            case "chlorophyll test":
                description = interpretChlorophyll(color);
                break;
            default:
                description = "Unknown test type.";
        }
        return description;
    }

    // ---- Descriptions per test ----

    private String interpretPh(int color) {
        String nearest = getNearestColorName(color).toLowerCase();

        if (nearest.contains("red"))
            return "Strongly acidic water.";
        else if (nearest.contains("yellow") || nearest.contains("gold"))
            return "Weak acidic water.";
        else if (nearest.contains("lime") || nearest.contains("light green"))
            return "Slightly acidic water.";
        else if (nearest.contains("green") || nearest.contains("aqua") || nearest.contains("cyan"))
            return "Neutral water, ideal.";
        else if (nearest.contains("blue") || nearest.contains("sky blue"))
            return "Slightly basic water.";
        else if (nearest.contains("dark blue") || nearest.contains("violet") || nearest.contains("purple"))
            return "Highly basic water.";
        else
            return "Intermediate pH level.";
    }

    private String interpretAmmonia(int color) {
        String nearest = getNearestColorName(color).toLowerCase();

        if (nearest.contains("light yellow"))
            return "Safe: Ammonia levels are very low or negligible. Water quality is excellent for aquatic life.";
        else if (nearest.contains("lime") || nearest.contains("yellow green"))
            return "Slightly Elevated: Minor traces of ammonia; generally safe but monitor regularly.";
        else if (nearest.contains("green"))
            return "Moderate: Ammonia levels are increasing; can start to stress sensitive fish or aquatic organisms. Partial water change recommended.";
        else if (nearest.contains("blue green") || nearest.contains("teal"))
            return "High: Toxic ammonia concentration. Immediate action needed (water change, filtration improvement, reduce feeding).";
        else if (nearest.contains("blue"))
            return "Very High: Dangerous level. Can cause severe stress, gill damage, or death in fish. Urgent corrective action required.";
        else if (nearest.contains("dark blue") || nearest.contains("indigo"))
            return "Critical: Extremely toxic ammonia concentration. Likely lethal to aquatic life. Requires immediate remediation.";
        else
            return "Ammonia range indeterminate.";
    }

    private String interpretNitrite(int color) {
        String nearest = getNearestColorName(color).toLowerCase();

        if (nearest.contains("colorless"))
            return "Safe: Nitrite level is 0 ppm or negligible. Water quality is excellent for aquatic life.";
        else if (nearest.contains("light pink") || nearest.contains("rose pink"))
            return "Slightly Elevated: Low nitrite presence; not immediately harmful but should be monitored. Indicates early nitrogen cycle activity.";
        else if (nearest.contains("light purple") || nearest.contains("lavender"))
            return "Moderate: Nitrite level rising (typically 0.25–0.5 ppm). Can stress fish and inhibit oxygen transport. Partial water change recommended.";
        else if (nearest.contains("purple") || nearest.contains("violet"))
            return "High: Toxic level (around 1–2 ppm). Dangerous to fish and aquatic life; immediate corrective action needed.";
        else if (nearest.contains("dark purple") || nearest.contains("indigo"))
            return "Critical: Very high nitrite level (>2 ppm). Extremely toxic and potentially lethal to fish. Requires urgent and major water replacement.";
        else return "Nitrite range indeterminate.";
    }

    private String interpretNitrate(int color) {
        String nearest = getNearestColorName(color).toLowerCase();

        if (nearest.contains("light yellow") || nearest.contains("lemon"))
            return "Safe: Very low nitrate (0–10 ppm). Water quality is excellent; safe for aquatic life.";
        else if (nearest.contains("orange") || nearest.contains("light orange"))
            return "Moderate: Nitrate levels increasing (20–40 ppm). Acceptable short-term, but long-term exposure can stress aquatic organisms. Partial water change advised.";
        else if (nearest.contains("red") || nearest.contains("rose"))
            return "High: Nitrate level (40–80 ppm). Harmful to fish and plants; can lead to poor growth and disease. Immediate action recommended.";
        else if (nearest.contains("dark red") || nearest.contains("maroon"))
            return "Critical: Extremely high nitrate (>80 ppm). Highly toxic; prolonged exposure can cause death in fish and other aquatic organisms. Immediate large water change and system cleaning required.";
        else return "Nitrate range indeterminate.";
    }

    private String interpretChlorophyll(int color) {
        String nearest = getNearestColorName(color).toLowerCase();

        if (nearest.contains("light green") || nearest.contains("lime"))
            return "Low Chlorophyll: Indicates low algae concentration; water is clean and healthy. Normal nutrient levels.";
        else if (nearest.contains("green"))
            return "Moderate Chlorophyll: Moderate algal growth. Water quality acceptable but may indicate early eutrophication (nutrient buildup).";
        else if (nearest.contains("dark green") || nearest.contains("teal"))
            return "High Chlorophyll: Dense algal bloom forming. Reduced oxygen levels likely, stressing aquatic life.";
        else if (nearest.contains("brown") || nearest.contains("olive"))
            return "Very High Chlorophyll: Old or decaying algal bloom. Poor water quality, low oxygen levels, and potential toxicity. Immediate corrective actions needed.";
        else return "Chlorophyll level: Moderate – balanced productivity.";
    }


    // ---------- Color utilities ----------

    private String colorToHex(int color) {
        return String.format("#%02X%02X%02X",
                android.graphics.Color.red(color),
                android.graphics.Color.green(color),
                android.graphics.Color.blue(color));
    }

    // ---- 50+ shades recognition ----
    private String getNearestColorName(int color) {
        float[] hsv = new float[3];
        android.graphics.Color.colorToHSV(color, hsv);
        float hue = hsv[0];
        float sat = hsv[1];
        float val = hsv[2];

        if (val < 0.1f) return "Black";
        if (val > 0.95f && sat < 0.2f) return "White";
        if (sat < 0.15f) {
            if (val < 0.3f) return "Dark Gray";
            if (val < 0.6f) return "Gray";
            return "Light Gray";
        }

// REDS & ORANGES
        if (hue < 5) return "Red";
        if (hue < 10) return (val > 0.7f) ? "Light Red" : "Dark Red";
        if (hue < 20) return "Coral";
        if (hue < 25) return "Peach";
        if (hue < 30) return "Orange";
        if (hue < 40) return (val > 0.7f) ? "Light Orange" : "Dark Orange";
        if (hue < 45) return "Lemon";
        if (hue < 50) return "Gold";
        if (hue < 55) return "Golden Yellow";
        if (hue < 58) return "Yellow";
        if (hue < 60) return (val > 0.7f) ? "Light Yellow" : "Mustard";

// GREENS
        if (hue < 65) return "Yellow Green";
        if (hue < 70) return "Olive";
        if (hue < 80) return "Lime";
        if (hue < 90) return (val > 0.7f) ? "Light Green" : "Green";
        if (hue < 110) return "Sea Green";
        if (hue < 140) return "Teal";
        if (hue < 150) return "Blue Green";
        if (hue < 155) return "Cyan";
        if (hue < 160) return "Aqua";

// BLUES
        if (hue < 180) return "Sky Blue";
        if (hue < 200) return "Light Blue";
        if (hue < 220) return "Blue";
        if (hue < 230) return "Royal Blue";
        if (hue < 240) return "Dark Blue";
        if (hue < 250) return "Indigo";

// PURPLES
        if (hue < 260) return "Violet";
        if (hue < 270) return "Light Purple";
        if (hue < 275) return "Lavender";
        if (hue < 285) return "Purple";
        if (hue < 290) return "Dark Purple";

// PINKS & RED-PURPLE RANGE
        if (hue < 300) return "Rose";
        if (hue < 310) return "Light Pink";
        if (hue < 320) return "Rose Pink";
        if (hue < 330) return "Magenta";
        if (hue < 340) return "Fuchsia";
        if (hue < 350) return "Maroon";

// NEUTRALS & EARTH TONES
        if (hue >= 350 && hue <= 360) return "Brown"; // reddish-brown tone region
        if (sat < 0.05f && val > 0.95f) return "Colorless"; // near transparent water-like color

// Default fallback
        return "Colorless";


    }
}
