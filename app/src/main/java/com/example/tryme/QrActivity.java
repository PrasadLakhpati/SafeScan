
package com.example.tryme;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@ExperimentalGetImage
public class QrActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;

    private PreviewView cameraPreview;
    private TextView scanStatus;
    private TextView scanHint;
    private TextView securityStatus;

    private ExecutorService cameraExecutor;
    private BarcodeScanner barcodeScanner;
    private ProcessCameraProvider cameraProvider;
    private Camera camera;

    private final AtomicBoolean qrHandled = new AtomicBoolean(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);

        cameraPreview = findViewById(R.id.cameraPreview);
        scanStatus = findViewById(R.id.scanStatus);
        scanHint = findViewById(R.id.scanHint);
        securityStatus = findViewById(R.id.securityStatus);

        cameraExecutor = Executors.newSingleThreadExecutor();

        // Compatible mode helps with some emulator/device previews.
        cameraPreview.setImplementationMode(
                PreviewView.ImplementationMode.COMPATIBLE
        );

        setupBarcodeScanner();
        setupButtons();
        checkCameraPermission();
    }

    // --------------------------------------------------
    // ML KIT: QR CODES ONLY
    // --------------------------------------------------

    private void setupBarcodeScanner() {
        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                        .build();

        barcodeScanner = BarcodeScanning.getClient(options);
    }

    // --------------------------------------------------
    // CAMERA PERMISSION
    // --------------------------------------------------

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE
            );
        }
    }

    // --------------------------------------------------
    // START CAMERA
    // --------------------------------------------------

    private void startCamera() {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(this);

        future.addListener(() -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }

            try {
                cameraProvider = future.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(
                        cameraPreview.getSurfaceProvider()
                );

                ImageAnalysis analysis =
                        new ImageAnalysis.Builder()
                                .setBackpressureStrategy(
                                        ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                                )
                                .build();

                analysis.setAnalyzer(
                        cameraExecutor,
                        this::analyzeImage
                );

                cameraProvider.unbindAll();

                camera = cameraProvider.bindToLifecycle(
                        this,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analysis
                );

                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    scanStatus.setText("Scan a UPI payment QR");
                    scanHint.setText(
                            "Place the QR code inside the scanning frame."
                    );
                    securityStatus.setText("● Camera ready");
                });

            } catch (ExecutionException e) {
                showCameraError("Unable to open the camera.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                showCameraError("Camera startup was interrupted.");
            } catch (Exception e) {
                showCameraError("Camera unavailable on this device.");
            }

        }, ContextCompat.getMainExecutor(this));
    }

    private void showCameraError(String message) {
        runOnUiThread(() -> {
            if (!isFinishing() && !isDestroyed()) {
                scanStatus.setText("Camera unavailable");
                securityStatus.setText("● Camera error");

                Toast.makeText(
                        this,
                        message,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    // --------------------------------------------------
    // ANALYZE CAMERA FRAMES
    // --------------------------------------------------

    private void analyzeImage(ImageProxy imageProxy) {
        if (qrHandled.get()) {
            imageProxy.close();
            return;
        }

        if (imageProxy.getImage() == null) {
            imageProxy.close();
            return;
        }

        try {
            InputImage image = InputImage.fromMediaImage(
                    imageProxy.getImage(),
                    imageProxy.getImageInfo().getRotationDegrees()
            );

            barcodeScanner.process(image)
                    .addOnSuccessListener(barcodes -> {
                        if (qrHandled.get()) {
                            return;
                        }

                        for (Barcode barcode : barcodes) {
                            String value = barcode.getRawValue();

                            if (value != null && !value.trim().isEmpty()) {
                                handleQrDetected(value);
                                break;
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Ignore this frame and keep scanning.
                    })
                    .addOnCompleteListener(task -> imageProxy.close());

        } catch (Exception e) {
            imageProxy.close();
        }
    }

    // --------------------------------------------------
    // VALIDATE UPI PAYMENT QR
    // --------------------------------------------------

    private void handleQrDetected(String qrValue) {
        if (qrHandled.get()) {
            return;
        }

        Uri uri;

        try {
            uri = Uri.parse(qrValue.trim());
        } catch (Exception e) {
            showInvalidQr();
            return;
        }

        String scheme = uri.getScheme();
        String authority = uri.getAuthority();
        String upiId = uri.getQueryParameter("pa");

        boolean validUpi =
                scheme != null
                        && "upi".equalsIgnoreCase(scheme)
                        && authority != null
                        && "pay".equalsIgnoreCase(authority)
                        && upiId != null
                        && !upiId.trim().isEmpty()
                        && !upiId.contains(" ")
                        && !upiId.contains("\n");

        if (!validUpi) {
            showInvalidQr();
            return;
        }

        // Only one valid QR can launch the next screen.
        if (!qrHandled.compareAndSet(false, true)) {
            return;
        }

        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }

            scanStatus.setText("UPI QR detected");
            scanHint.setText("Opening receiver details...");
            securityStatus.setText("● UPI details extracted");

            try {
                Intent intent = new Intent(
                        QrActivity.this,
                        PaymentDetailsActivity.class
                );

                // Pass the original QR URI to the details screen.
                intent.putExtra("qr_value", qrValue);

                startActivity(intent);
                finish();

            } catch (Exception e) {
                qrHandled.set(false);
                scanStatus.setText("Unable to open details");

                Toast.makeText(
                        QrActivity.this,
                        "Could not open payment details.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void showInvalidQr() {
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed() || qrHandled.get()) {
                return;
            }

            scanStatus.setText("Not a supported UPI QR");
            scanHint.setText("Scan a valid UPI payment QR to continue.");
            securityStatus.setText("● Waiting for UPI QR");

            Toast.makeText(
                    QrActivity.this,
                    "Not a supported UPI payment QR. Keep scanning.",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    // --------------------------------------------------
    // BUTTONS
    // --------------------------------------------------

    private void setupButtons() {
        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        findViewById(R.id.flashButton).setOnClickListener(v -> toggleFlash());

        findViewById(R.id.manualUpiButton).setOnClickListener(v -> {
            // Manual UPI entry is a separate feature to implement.
            Toast.makeText(
                    QrActivity.this,
                    "Manual UPI entry coming next.",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    // --------------------------------------------------
    // FLASH / TORCH
    // --------------------------------------------------

    private void toggleFlash() {
        if (camera == null) {
            Toast.makeText(
                    this,
                    "Camera is not ready yet.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (!camera.getCameraInfo().hasFlashUnit()) {
            Toast.makeText(
                    this,
                    "Flash is not available on this camera.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        Integer torchState =
                camera.getCameraInfo()
                        .getTorchState()
                        .getValue();

        boolean currentlyOn =
                torchState != null
                        && torchState == androidx.camera.core.TorchState.ON;

        camera.getCameraControl().enableTorch(!currentlyOn);
    }

    // --------------------------------------------------
    // CAMERA PERMISSION RESULT
    // --------------------------------------------------

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(
                        this,
                        "Camera permission is required to scan QR codes.",
                        Toast.LENGTH_LONG
                ).show();
                finish();
            }
        }
    }

    // --------------------------------------------------
    // RELEASE RESOURCES
    // --------------------------------------------------

    @Override
    protected void onDestroy() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }

        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }

        if (barcodeScanner != null) {
            barcodeScanner.close();
        }

        super.onDestroy();
    }
}
