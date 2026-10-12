
package com.example.tryme;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
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

    private boolean qrHasAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_details);

        TextView receiverInitial = findViewById(R.id.receiverInitial);
        TextView receiverName = findViewById(R.id.receiverName);
        TextView receiverUpi = findViewById(R.id.receiverUpi);
        TextView amountNote = findViewById(R.id.amountNote);

        amountInput = findViewById(R.id.amountInput);
        continueButton = findViewById(R.id.continueButton);

        String qrValue = getIntent().getStringExtra("qr_value");

        if (qrValue == null) {
            showInvalidQr();
            return;
        }

        paymentUri = Uri.parse(qrValue);

        String upiId = paymentUri.getQueryParameter("pa");
        String name = paymentUri.getQueryParameter("pn");
        String amount = paymentUri.getQueryParameter("am");

        // Validate the payment QR again before displaying it.
        if (!"upi".equalsIgnoreCase(paymentUri.getScheme())
                || !"pay".equalsIgnoreCase(paymentUri.getAuthority())
                || upiId == null
                || upiId.trim().isEmpty()
                || upiId.contains(" ")) {
            showInvalidQr();
            return;
        }

        if (name == null || name.trim().isEmpty()) {
            name = "Unknown receiver";
        }

        receiverName.setText(name);
        receiverUpi.setText(upiId);
        receiverInitial.setText(
                name.substring(0, 1).toUpperCase()
        );

        qrHasAmount = amount != null && !amount.trim().isEmpty();

        if (qrHasAmount) {
            amountInput.setText(amount);
            amountInput.setEnabled(false);
            amountNote.setText("Amount supplied by the QR code.");
        } else {
            amountInput.setEnabled(true);
            amountNote.setText(
                    "Enter the amount you intend to pay."
            );
        }

        // Limit amount to two decimal places.
        amountInput.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(12)
        });

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        continueButton.setOnClickListener(v -> continueToUpiApp());
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

        Uri uriToOpen = paymentUri;

        // Add the amount only when the QR didn't supply one.
        if (!qrHasAmount) {
            uriToOpen = paymentUri.buildUpon()
                    .appendQueryParameter("am", amount)
                    .build();
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

    private void showInvalidQr() {
        Toast.makeText(
                this,
                "Invalid UPI payment QR.",
                Toast.LENGTH_LONG
        ).show();

        finish();
    }
}
