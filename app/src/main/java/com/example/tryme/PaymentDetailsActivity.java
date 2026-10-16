package com.example.tryme;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.math.BigDecimal;

public class PaymentDetailsActivity extends AppCompatActivity {

    private Uri paymentUri;
    private EditText amountInput;
    private Button continueButton;
    private TextView safetyStatus;
    
    private String upiId;
    private String receiverNameStr;

    private boolean qrHasAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_details);

        TextView receiverInitial = findViewById(R.id.receiverInitial);
        TextView receiverName = findViewById(R.id.receiverName);
        TextView receiverUpi = findViewById(R.id.receiverUpi);
        TextView amountNote = findViewById(R.id.amountNote);
        safetyStatus = findViewById(R.id.safetyStatus);

        amountInput = findViewById(R.id.amountInput);
        continueButton = findViewById(R.id.continueButton);

        Intent intent = getIntent();
        String qrValue = intent.getStringExtra("qr_value");
        String manualUpiId = intent.getStringExtra("upi_id");
        String manualUpiName = intent.getStringExtra("upi_name");
        String manualSafetyStatus = intent.getStringExtra("safety_status");

        if (qrValue != null) {
            handleQrFlow(qrValue);
        } else if (manualUpiId != null) {
            handleManualFlow(manualUpiId, manualUpiName, manualSafetyStatus);
        } else {
            showInvalidFlow();
            return;
        }

        if (receiverNameStr == null || receiverNameStr.trim().isEmpty()) {
            receiverNameStr = "Unknown receiver";
        }

        receiverName.setText(receiverNameStr);
        receiverUpi.setText(upiId);
        receiverInitial.setText(receiverNameStr.substring(0, 1).toUpperCase());

        if (qrHasAmount) {
            amountInput.setEnabled(false);
            amountNote.setText("Amount supplied by the QR code.");
        } else {
            amountInput.setEnabled(true);
            amountNote.setText("Enter the amount you intend to pay.");
        }

        // Limit amount to two decimal places (overall length of 12 digits roughly)
        amountInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        continueButton.setOnClickListener(v -> continueToUpiApp());
    }

    private void handleQrFlow(String qrValue) {
        paymentUri = Uri.parse(qrValue);

        upiId = paymentUri.getQueryParameter("pa");
        receiverNameStr = paymentUri.getQueryParameter("pn");
        String amount = paymentUri.getQueryParameter("am");

        if (!"upi".equalsIgnoreCase(paymentUri.getScheme())
                || !"pay".equalsIgnoreCase(paymentUri.getAuthority())
                || upiId == null
                || upiId.trim().isEmpty()
                || upiId.contains(" ")) {
            showInvalidFlow();
            return;
        }

        qrHasAmount = amount != null && !amount.trim().isEmpty();
        if (qrHasAmount) {
            amountInput.setText(amount);
        }
        
        // For QR Flow, we don't have the status yet since it's just a raw QR
        updateSafetyStatusUI("UNAVAILABLE");
    }

    private void handleManualFlow(String manualUpiId, String manualUpiName, String manualSafetyStatus) {
        upiId = manualUpiId;
        receiverNameStr = manualUpiName;
        qrHasAmount = false;
        paymentUri = null; // Will build it on continue

        if (upiId == null || upiId.trim().isEmpty() || upiId.contains(" ")) {
            showInvalidFlow();
            return;
        }
        
        updateSafetyStatusUI(manualSafetyStatus != null ? manualSafetyStatus : "UNAVAILABLE");
    }
    
    private void updateSafetyStatusUI(String status) {
        switch (status) {
            case "VERIFIED":
                safetyStatus.setText("✓  Verified receiver");
                safetyStatus.setTextColor(android.graphics.Color.parseColor("#22D3A3")); // Mint Green
                break;
            case "CAUTION":
                safetyStatus.setText("⚠  Proceed with caution");
                safetyStatus.setTextColor(android.graphics.Color.parseColor("#FBBF24")); // Amber
                break;
            case "RISK":
                safetyStatus.setText("✕  Risk Identified");
                safetyStatus.setTextColor(android.graphics.Color.parseColor("#EF4444")); // Red
                break;
            case "UNAVAILABLE":
            default:
                safetyStatus.setText("⚠  Receiver not verified yet");
                safetyStatus.setTextColor(android.graphics.Color.parseColor("#FBBF24")); // Amber
                break;
        }
    }

    private void continueToUpiApp() {
        String amount = amountInput.getText().toString().trim();

        if (amount.isEmpty()) {
            amountInput.setError("Enter an amount");
            return;
        }

        try {
            BigDecimal value = new BigDecimal(amount);

            if (value.compareTo(BigDecimal.ZERO) <= 0
                    || value.scale() > 2
                    || value.precision() > 10) {
                amountInput.setError("Enter a valid amount");
                return;
            }
        } catch (NumberFormatException e) {
            amountInput.setError("Enter a valid amount");
            return;
        }

        Uri uriToOpen;

        if (paymentUri != null) {
            // Started via QR Code
            if (!qrHasAmount) {
                uriToOpen = paymentUri.buildUpon()
                        .appendQueryParameter("am", amount)
                        .build();
            } else {
                uriToOpen = paymentUri;
            }
        } else {
            // Started via Search Flow
            Uri.Builder builder = new Uri.Builder()
                    .scheme("upi")
                    .authority("pay")
                    .appendQueryParameter("pa", upiId)
                    .appendQueryParameter("am", amount);
            
            if (receiverNameStr != null && !receiverNameStr.equals("Unknown receiver")) {
                builder.appendQueryParameter("pn", receiverNameStr);
            }
            uriToOpen = builder.build();
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, uriToOpen);

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(
                    this,
                    "No compatible UPI app was found.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void showInvalidFlow() {
        Toast.makeText(
                this,
                "Invalid payment details.",
                Toast.LENGTH_LONG
        ).show();

        finish();
    }
}