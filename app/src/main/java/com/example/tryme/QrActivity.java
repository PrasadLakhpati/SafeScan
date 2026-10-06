package com.example.tryme;

import android.Manifest;
import android.content.pm.PackageManager;
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
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ExperimentalGetImage
public class QrActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;

    private PreviewView cameraPreview;

    private TextView scanStatus;
    private TextView scanHint;
    private TextView securityStatus;

    private ExecutorService cameraExecutor;

    private BarcodeScanner barcodeScanner;

    private Camera camera;

    private boolean qrDetected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_qr);

        cameraPreview =
                findViewById(R.id.cameraPreview);

        scanStatus =
                findViewById(R.id.scanStatus);

        scanHint =
                findViewById(R.id.scanHint);

        securityStatus =
                findViewById(R.id.securityStatus);

        cameraExecutor =
                Executors.newSingleThreadExecutor();

        setupBarcodeScanner();

        setupButtons();

        checkCameraPermission();
    }

    // ==================================================
    // BARCODE SCANNER
    // ==================================================

    private void setupBarcodeScanner() {

        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                                Barcode.FORMAT_QR_CODE
                        )
                        .build();

        barcodeScanner =
                BarcodeScanning.getClient(options);
    }

    // ==================================================
    // CAMERA PERMISSION
    // ==================================================

    private void checkCameraPermission() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED) {

            startCamera();

        } else {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.CAMERA
                    },
                    CAMERA_PERMISSION_CODE
            );
        }
    }

    // ==================================================
    // START CAMERA
    // ==================================================

    private void startCamera() {

        ListenableFuture<ProcessCameraProvider>
                cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {

            try {

                ProcessCameraProvider cameraProvider =
                        cameraProviderFuture.get();

                Preview preview =
                        new Preview.Builder().build();

                preview.setSurfaceProvider(
                        cameraPreview.getSurfaceProvider()
                );

                ImageAnalysis imageAnalysis =
                        new ImageAnalysis.Builder()
                                .setBackpressureStrategy(
                                        ImageAnalysis
                                                .STRATEGY_KEEP_ONLY_LATEST
                                )
                                .build();

                imageAnalysis.setAnalyzer(
                        cameraExecutor,
                        this::analyzeImage
                );

                CameraSelector cameraSelector =
                        CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();

                camera =
                        cameraProvider.bindToLifecycle(
                                this,
                                cameraSelector,
                                preview,
                                imageAnalysis
                        );

                runOnUiThread(() -> {

                    scanStatus.setText(
                            "Scanning for QR code..."
                    );

                    securityStatus.setText(
                            "●  Camera ready"
                    );

                });

            } catch (
                    ExecutionException |
                    InterruptedException e
            ) {

                runOnUiThread(() ->
                        Toast.makeText(
                                QrActivity.this,
                                "Unable to start camera",
                                Toast.LENGTH_LONG
                        ).show()
                );
            }

        }, ContextCompat.getMainExecutor(this));
    }

    // ==================================================
    // ANALYZE FRAME
    // ==================================================

    private void analyzeImage(
            ImageProxy imageProxy
    ) {

        if (qrDetected) {

            imageProxy.close();
            return;
        }

        if (imageProxy.getImage() == null) {

            imageProxy.close();
            return;
        }

        InputImage image =
                InputImage.fromMediaImage(
                        imageProxy.getImage(),
                        imageProxy.getImageInfo()
                                .getRotationDegrees()
                );

        barcodeScanner
                .process(image)

                .addOnSuccessListener(barcodes -> {

                    for (Barcode barcode : barcodes) {

                        String value =
                                barcode.getRawValue();

                        if (value != null &&
                                !value.trim().isEmpty()) {

                            handleQrDetected(value);

                            break;
                        }
                    }
                })

                .addOnFailureListener(e -> {
                    // Continue scanning.
                })

                .addOnCompleteListener(task ->
                        imageProxy.close()
                );
    }

    // ==================================================
    // QR DETECTED
    // ==================================================

    private void handleQrDetected(
            String qrValue
    ) {

        if (qrDetected) {
            return;
        }

        qrDetected = true;

        runOnUiThread(() -> {

            scanStatus.setText(
                    "QR code detected"
            );

            scanHint.setText(
                    "Verifying payment information..."
            );

            securityStatus.setText(
                    "●  Checking receiver details"
            );

            Toast.makeText(
                    QrActivity.this,
                    "QR detected",
                    Toast.LENGTH_SHORT
            ).show();

            /*
             * qrValue contains the actual QR data.
             *
             * Next step:
             * Open SafeScan verification screen.
             */
        });
    }

    // ==================================================
    // BUTTONS
    // ==================================================

    private void setupButtons() {

        findViewById(R.id.backButton)
                .setOnClickListener(v ->
                        finish()
                );

        findViewById(R.id.flashButton)
                .setOnClickListener(v ->
                        toggleFlash()
                );

        findViewById(R.id.manualUpiButton)
                .setOnClickListener(v ->
                        Toast.makeText(
                                QrActivity.this,
                                "Manual UPI coming next",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    // ==================================================
    // FLASH
    // ==================================================

    private void toggleFlash() {

        if (camera == null) {
            return;
        }

        if (!camera
                .getCameraInfo()
                .hasFlashUnit()) {

            Toast.makeText(
                    QrActivity.this,
                    "Flash not available",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Integer torchState =
                camera.getCameraInfo()
                        .getTorchState()
                        .getValue();

        boolean isOn =
                torchState != null &&
                        torchState ==
                                androidx.camera.core.TorchState.ON;

        camera.getCameraControl()
                .enableTorch(!isOn);
    }

    // ==================================================
    // PERMISSION RESULT
    // ==================================================

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

        if (requestCode ==
                CAMERA_PERMISSION_CODE) {

            if (grantResults.length > 0 &&
                    grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED) {

                startCamera();

            } else {

                Toast.makeText(
                        QrActivity.this,
                        "Camera permission is required",
                        Toast.LENGTH_LONG
                ).show();

                finish();
            }
        }
    }

    // ==================================================
    // CLEANUP
    // ==================================================

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }

        if (barcodeScanner != null) {
            barcodeScanner.close();
        }
    }
}
