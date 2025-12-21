package com.aastha.colorassistapp.ui.home;

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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.aastha.colorassistapp.R;
import com.aastha.colorassistapp.PixelIndicatorView;

import java.io.IOException;

public class HomeFragment extends Fragment {

    private View rootView;
    private ImageView imageView;
    private PixelIndicatorView pixelIndicator;
    private TextView infoText;
    private Bitmap currentBitmap;
    private String lastHexColor;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize the photo picker launcher
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                loadImageFromUri(uri);
            } else {
                Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.rootView = view;
        imageView = view.findViewById(R.id.image_view);
        pixelIndicator = view.findViewById(R.id.pixel_indicator);
        infoText = view.findViewById(R.id.info_text);
        Button pickImageBtn = view.findViewById(R.id.btn_pick_image);
        Button colorHexaBtn = view.findViewById(R.id.btn_colorhexa);
        // Pick image button click listener
        pickImageBtn.setOnClickListener(v -> launchPhotoPicker());
        colorHexaBtn.setOnClickListener(v -> openColorHexa());
        // Image tap listener
        imageView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN && currentBitmap != null) {
                handleImageTap(event);
            }
            return true;
        });
    }
    private void openColorHexa() {
        if (currentBitmap == null) return;

        // Get last selected color's hex (remove # for URL)
        String colorHex = lastHexColor != null ? lastHexColor.replace("#", "") : "000000";

        String url = "https://www.colorhexa.com/" + colorHex;

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    // Add this field to store last hex color

    private void launchPhotoPicker() {
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
            launchPhotoPicker();
        } else {
            Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadImageFromUri(Uri uri) {
        try {
            currentBitmap = android.provider.MediaStore.Images.Media.getBitmap(
                    requireContext().getContentResolver(), uri);

            imageView.setImageBitmap(currentBitmap);

            // NEW: Dynamic container height to fit image perfectly
            FrameLayout container = rootView.findViewById(R.id.image_container);
            float aspectRatio = (float) currentBitmap.getHeight() / currentBitmap.getWidth();
            int targetHeight = Math.min(600, (int) (imageView.getWidth() * aspectRatio)); // Max 600dp
            container.getLayoutParams().height = targetHeight;

            pixelIndicator.clearIndicator();
            infoText.setText("Tap on the image to pick colors");
            Button colorHexaBtn = rootView.findViewById(R.id.btn_colorhexa);
            colorHexaBtn.setVisibility(View.GONE);
        } catch (IOException e) {
            Log.e("HomeFragment", "Error loading image", e);
            Toast.makeText(getContext(), "Error loading image", Toast.LENGTH_SHORT).show();
        }
    }




    private void handleImageTap(MotionEvent event) {
        if (currentBitmap == null) return;

        float tapX = event.getX();
        float tapY = event.getY();

        int imageViewWidth = imageView.getWidth();
        int imageViewHeight = imageView.getHeight();
        int bitmapWidth = currentBitmap.getWidth();
        int bitmapHeight = currentBitmap.getHeight();

        // Calculate ACTUAL displayed bitmap size (accounts for letterbox)
        float scale = Math.min((float) imageViewWidth / bitmapWidth, (float) imageViewHeight / bitmapHeight);
        int displayWidth = (int) (bitmapWidth * scale);
        int displayHeight = (int) (bitmapHeight * scale);

        // Calculate letterbox padding
        float paddingX = (imageViewWidth - displayWidth) / 2f;
        float paddingY = (imageViewHeight - displayHeight) / 2f;

        // Adjust tap coordinates for padding
        float adjustedX = tapX - paddingX;
        float adjustedY = tapY - paddingY;

        // Scale to bitmap coordinates
        float scaleX = (float) bitmapWidth / displayWidth;
        float scaleY = (float) bitmapHeight / displayHeight;

        int bitmapX = Math.round(adjustedX * scaleX);
        int bitmapY = Math.round(adjustedY * scaleY);

        // Clamp to bitmap bounds
        bitmapX = Math.max(0, Math.min(bitmapX, bitmapWidth - 1));
        bitmapY = Math.max(0, Math.min(bitmapY, bitmapHeight - 1));

        // Only process valid taps
        if (tapX >= paddingX && tapX <= (imageViewWidth - paddingX) &&
                tapY >= paddingY && tapY <= (imageViewHeight - paddingY)) {

            int averageColor = getAverageColorInRadius(bitmapX, bitmapY, 3);
            String hexColor = colorToHex(averageColor);
            lastHexColor = hexColor;
            // Pass RAW ImageView coordinates to overlay (perfect position)
            pixelIndicator.updateIndicator((int) tapX, (int) tapY, averageColor, hexColor);
            infoText.setText("Selected Color: " + hexColor);
            Button colorHexaBtn = rootView.findViewById(R.id.btn_colorhexa);
            colorHexaBtn.setVisibility(View.VISIBLE);
            Log.d("HomeFragment", "Tap: (" + tapX + ", " + tapY + ") -> Bitmap: (" + bitmapX + ", " + bitmapY + ") = " + hexColor);
        }
    }



    private int getAverageColorInRadius(int centerX, int centerY, int radius) {
        int totalRed = 0;
        int totalGreen = 0;
        int totalBlue = 0;
        int pixelCount = 0;

        // Sample pixels within radius
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int y = centerY - radius; y <= centerY + radius; y++) {
                // Check if pixel is within bitmap bounds and within circular radius
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

        if (pixelCount == 0) {
            return android.graphics.Color.BLACK;
        }

        int avgRed = totalRed / pixelCount;
        int avgGreen = totalGreen / pixelCount;
        int avgBlue = totalBlue / pixelCount;

        return android.graphics.Color.rgb(avgRed, avgGreen, avgBlue);
    }

    private String colorToHex(int color) {
        int red = android.graphics.Color.red(color);
        int green = android.graphics.Color.green(color);
        int blue = android.graphics.Color.blue(color);
        
        return String.format("#%02X%02X%02X", red, green, blue);
    }
}
