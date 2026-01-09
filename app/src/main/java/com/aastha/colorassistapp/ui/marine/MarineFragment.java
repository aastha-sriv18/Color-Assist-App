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
        generateBtn.setEnabled(false);
        generateBtn.setAlpha(0.5f);

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
    private void updateGenerateButtonState() {
        boolean enabled = currentBitmap != null;
        generateBtn.setEnabled(enabled);
        generateBtn.setAlpha(enabled ? 1.0f : 0.5f);
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
            updateGenerateButtonState();
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


        if (nearest.contains("red") || nearest.contains("dark red") || nearest.contains("tomato") || nearest.contains("crimson") || nearest.contains("orange red") || nearest.contains("coral"))
            return "Strongly acidic water.";
        else if (nearest.contains("yellow") || nearest.contains("gold") || nearest.contains("orange") || nearest.contains("carrot orange") || nearest.contains("goldenrod") || nearest.contains("yellow orange"))
            return "Weak acidic water.";
        else if (nearest.contains("lime green") || nearest.contains("yellow green") || nearest.contains("light green") || nearest.contains("green yellow"))
            return "Slightly acidic water.";
        else if (nearest.contains("green") || nearest.contains("pure green") || nearest.contains("emerald"))
            return "Neutral water, ideal.";
        else if (nearest.contains("turquoise") || nearest.contains("aquamarine") || nearest.contains("blue green") || nearest.contains("cyan") || nearest.contains("teal") || nearest.contains("teal blue") || nearest.contains("sky blue") || nearest.contains("light blue") || nearest.contains("blue") || nearest.contains("royal blue"))
            return "Slightly basic water.";
        else if (nearest.contains("indigo blue") || nearest.contains("indigo") || nearest.contains("dark blue") || nearest.contains("violet") || nearest.contains("blue violet") || nearest.contains("purple") || nearest.contains("medium purple"))
            return "Highly basic water.";
        else
            return "Intermediate pH level.";
    }

    private String interpretAmmonia(int color) {
        String nearest = getNearestColorName(color).toLowerCase();


        if (nearest.contains("bright yellow") || nearest.contains("yellow") || nearest.contains("lemon yellow") || nearest.contains("golden yellow"))
            return "Safe: Ammonia levels are very low or negligible. Water quality is excellent for aquatic life.";
        else if (nearest.contains("pastel yellow") || nearest.contains("pale yellow green") || nearest.contains("light green yellow") || nearest.contains("olive") || nearest.contains("light yellow"))
            return "Slightly Elevated: Minor traces of ammonia; generally safe but monitor regularly.";
        else if (nearest.contains("yellow green") || nearest.contains("inchworm") || nearest.contains("Chartreuse") || nearest.contains("teal") || nearest.contains("light green"))
            return "Moderate: Ammonia levels are increasing; can start to stress sensitive fish or aquatic organisms. Partial water change recommended.";
        else if (nearest.contains("light olive green") || nearest.contains("olivine") || nearest.contains("moss green"))
            return "High: Toxic ammonia concentration. Immediate action needed (water change, filtration improvement, reduce feeding).";
        else if (nearest.contains("medium green") || nearest.contains("green") || nearest.contains("fern green") || nearest.contains("dark moss green") || nearest.contains("blue green"))
            return "Very High: Dangerous level. Can cause severe stress, gill damage, or death in fish. Urgent corrective action required.";
        else
            return "Ammonia range indeterminate.";
    }

    private String interpretNitrite(int color) {
        String nearest = getNearestColorName(color).toLowerCase();


        if (nearest.contains("white") || nearest.contains("light cyan") || nearest.contains("sky blue") || nearest.contains("pale lavender blue"))
            return "Safe: Very low nitrite. The only safe level for a cycled tank.";
        else if (nearest.contains("very pale pink") || nearest.contains("light violet"))
            return "Stressful: Nitrite levels increasing. Minor toxicity; monitor closely.";
        else if (nearest.contains("pale pink") || nearest.contains("soft pinkish purple") || nearest.contains("medium magenta"))
            return "Unsafe: Nitrite levels increasing. Perform a water change.";
        else if (nearest.contains("pink") || nearest.contains("deep fuchsia pink"))
            return "Dangerous: High toxicity; fish will show signs of gasping.";
        else if (nearest.contains("magenta") || nearest.contains("bright reddish pink") || nearest.contains("reddish magenta"))
            return "Toxic: Extremely high nitrite. Severe danger; immediate intervention needed.";
        else if (nearest.contains("deep purple") || nearest.contains("dark purple") || nearest.contains("purple") || nearest.contains("dark pinkish red") || nearest.contains("deep crimson red"))
            return "Lethal: Extremely high nitrite. Most fish will not survive this.";
        else return "Nitrite range indeterminate.";
    }

    private String interpretNitrate(int color) {
        String nearest = getNearestColorName(color).toLowerCase();


        if (nearest.contains("white") || nearest.contains("bright lemon yellow") || nearest.contains("light golden yellow") || nearest.contains("sunflower yellow") || nearest.contains("goldenrod") || nearest.contains("lemon yellow"))
            return "Ideal: Very low nitrate. Water quality is excellent; safe for aquatic life.";
        else if (nearest.contains("amber") || nearest.contains("orange") || nearest.contains("coral"))
            return "Caution: Nitrate levels increasing. Acceptable short-term, but long-term exposure can stress aquatic organisms. Partial water change advised.";
        else if (nearest.contains("tangerine"))
            return "Unsafe: Nitrate levels increasing. Perform a 25% water change.";
        else if (nearest.contains("tomato red") || nearest.contains("scarlet red"))
            return "Dangerous: Perform a 50% water change immediately";
        else if (nearest.contains("crimson") || nearest.contains("dark crimson") || nearest.contains("dark pinkish red"))
            return "Toxic: Extremely high nitrate. Immediate large water change and system cleaning required.";
        else return "Nitrate range indeterminate.";
    }

    private String interpretChlorophyll(int color) {
        String nearest = getNearestColorName(color).toLowerCase();


        if (nearest.contains("light green") || nearest.contains("pale green") || nearest.contains("light yellow"))
            return "Very Low Chlorophyll: Plants show severe nitrogen deficiency and poor growth; water supports very few fish due to low plankton.";
        else if (nearest.contains("yellow green") || nearest.contains("green yellow"))
            return "Low Chlorophyll: Plants are nitrogen-deficient with weak growth; fish presence is limited and growth is slow.";
        else if (nearest.contains("green") || nearest.contains("medium green"))
            return "Moderate Chlorophyll: Plants are healthy with adequate nitrogen; water is ideal for diverse and fast-growing fish.";
        else if (nearest.contains("dark green") || nearest.contains("teal"))
            return "High Chlorophyll: Plants have excess nitrogen and dark green leaves; water favors hardy, plankton-feeding fish with some oxygen risk.";
        else if (nearest.contains("brown") || nearest.contains("blue green"))
            return "Very High Chlorophyll: Plants suffer from nitrogen toxicity; water has algal blooms causing stress or mortality in fish.";
        else return "Chlorophyll level indeterminate.";
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

        // ⚪ Fast grayscale detection
        if (hsv[1] < 0.08f) {
            if (hsv[2] > 0.92f) return "white";
            if (hsv[2] > 0.7f)  return "very pale pink";
            if (hsv[2] > 0.4f)  return "pale pink";
            return "brown";
        }

        // name, H, S, V (parallel arrays for speed)
        final String[] names = {
                "Red","Dark Red","Tomato","Crimson","Orange Red","Coral","Maroon",
                "Orange","Carrot Orange","Gold","Goldenrod","Yellow","Bright Yellow",
                "Lemon Yellow","Golden Yellow","Light Yellow","Pastel Yellow","Yellow Orange",
                "Green Yellow","Yellow Green","Inchworm","Chartreuse","Lime Green",
                "Pure Green","Green","Medium Green","Light Green","Pale Green","Emerald",
                "Fern Green","Moss Green","Dark Moss Green","Olive","Olivine","Light Olive Green",
                "Cyan","Teal","Teal Blue","Blue Green","Aquamarine","Turquoise",
                "Light Blue","Sky Blue","Blue","Royal Blue","Dark Blue","Indigo Blue","Indigo",
                "Violet","Blue Violet","Purple","Medium Purple","Deep Purple","Dark Purple",
                "Magenta","Deep Pink","Pink","Pale Pink","Very Pale Pink","Rose",
                "White","Brown","Light Cyan", "Pale Lavender Blue", "Light Violet",
                "Soft Pinkish Purple", "Medium Magenta", "Deep Fuchsia Pink", "Bright Reddish Pink",
                "Reddish Magenta", "Dark Pinkish Red", "Deep Crimson Red", "Bright Lemon Yellow",
                "Light Golden Yellow", "Sunflower Yellow", "Amber", "Tangerine", "Scarlet Red", "Dark Crimson"

        };

        final float[] H = {
                0,0,9,348,16,16,0,
                30,28,51,43,60,60,
                58,52,60,60,45,
                75,85,90,90,120,
                120,120,120,120,120,140,
                110,95,95,60,80,75,
                180,180,190,170,160,174,
                200,197,240,225,240,260,275,
                270,275,285,290,285,285,
                300,330,350,350,350,345,
                0,30, 180f,220f,270f, 295f,
                300f, 315f,345f, 330f, 350f,
                355f, 58f,50f, 54f, 45f, 25f,
                8f, 350f

        };

        final float[] S = {
                1,1,0.72f,0.83f,1,0.68f,1,
                1,0.85f,1,0.74f,1,1,
                0.9f,0.95f,0.25f,0.3f,1,
                1,1,0.75f,1,1,
                1,0.8f,0.7f,0.4f,0.3f,0.8f,
                0.6f,0.6f,0.7f,0.8f,0.55f,0.4f,
                1,0.8f,0.8f,0.7f,0.5f,0.72f,
                0.4f,0.71f,1,0.73f,1,0.8f,0.75f,
                0.6f,0.76f,0.8f,0.5f,0.85f,0.9f,
                1,0.9f,0.4f,0.25f,0.15f,0.6f,
                0,0.8f, 0.25f, 0.35f, 0.45f, 0.45f, 0.65f,
                0.85f, 0.85f, 0.8f, 0.75f, 0.9f,
                1f, 0.6f, 0.9f, 0.95f, 0.9f,
                1f, 0.85f

        };

        final float[] V = {
                1,0.55f,1,0.86f,1,1,0.4f,
                1,0.9f,1,0.85f,1,1,
                1,0.95f,1,0.97f,1,
                1,1,0.9f,1,1,
                1,0.8f,0.7f,1,0.9f,0.8f,
                0.5f,0.5f,0.35f,0.5f,0.7f,0.8f,
                1,0.6f,0.7f,0.7f,1,0.88f,
                1,0.9f,1,0.88f,0.5f,0.5f,0.5f,
                0.9f,0.85f,0.7f,0.85f,0.4f,0.3f,
                1,1,1,1,1,0.8f,1,0.4f, 1f,
                0.95f, 0.9f, 0.85f, 0.8f,
                0.8f, 1f, 0.9f, 0.75f, 0.5f,
                1f, 0.95f, 0.95f, 0.95f, 1f,
                1f, 0.4f

        };

        float best = Float.MAX_VALUE;
        int bestIndex = 0;

        for (int i = 0; i < H.length; i++) {
            float dh = Math.abs(hsv[0] - H[i]);
            if (dh > 180f) dh = 360f - dh;
            dh /= 180f;

            float ds = hsv[1] - S[i];
            float dv = hsv[2] - V[i];

            // Weighted distance (Hue matters most)
            float d = dh * dh * 2.5f + ds * ds + dv * dv;

            if (d < best) {
                best = d;
                bestIndex = i;
            }
        }

        return names[bestIndex];
    }

}