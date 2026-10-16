package com.example.tryme;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;

import com.example.tryme.data.MockReceiverRepository;
import com.example.tryme.data.ReceiverRepository;
import com.example.tryme.models.Receiver;

import java.util.regex.Pattern;

public class UPIActivity extends AppCompatActivity {

    private EditText searchInput;
    private Button searchButton;
    private ProgressBar searchProgress;
    private TextView searchErrorText;
    
    private CardView resultCard;
    private TextView resultStatusTag;
    private TextView resultName;
    private TextView resultUpiId;
    private TextView resultPhone;
    private TextView resultSource;

    private Button safeScanButton;
    private SwitchCompat themeToggle;
    private LinearLayout searchContainer;
    private LinearLayout safetyInfoContainer;
    private View resultDivider;

    private ReceiverRepository repository;
    private Receiver selectedReceiver;
    
    private boolean isLightTheme = false;
    private boolean isSearchRunning = false;

    // Theme Colors
    private static final int DARK_BACKGROUND = Color.parseColor("#0B1020");
    private static final int DARK_CARD = Color.parseColor("#151C32");
    private static final int DARK_RESULT_CARD = Color.parseColor("#171D32");
    private static final int DARK_SAFETY_CARD = Color.parseColor("#202039");
    private static final int DARK_DIVIDER = Color.parseColor("#292348");

    private static final int LIGHT_BACKGROUND = Color.parseColor("#F7F5FF");
    private static final int LIGHT_CARD = Color.parseColor("#FFFFFF");
    private static final int LIGHT_RESULT_CARD = Color.parseColor("#FFFFFF");
    private static final int LIGHT_SAFETY_CARD = Color.parseColor("#E9E3FF");
    private static final int LIGHT_DIVIDER = Color.parseColor("#E5E1F2");

    private static final int DARK_PRIMARY = Color.parseColor("#F8FAFC");
    private static final int DARK_SECONDARY = Color.parseColor("#7F89A6");
    private static final int DARK_MUTED = Color.parseColor("#69748F");

    private static final int LIGHT_PRIMARY = Color.parseColor("#17152A");
    private static final int LIGHT_SECONDARY = Color.parseColor("#5F6475");
    private static final int LIGHT_MUTED = Color.parseColor("#737789");

    private static final int PURPLE = Color.parseColor("#7C5CFC");
    private static final int LIGHT_PURPLE = Color.parseColor("#6D4FF5");
    
    private static final int GREEN = Color.parseColor("#22D3A3");
    private static final int RED = Color.parseColor("#EF4444");
    private static final int AMBER = Color.parseColor("#FBBF24");
    
    private static final int DARK_DISABLED_BTN = Color.parseColor("#292348");
    private static final int DARK_DISABLED_TEXT = Color.parseColor("#7F89A6");
    private static final int LIGHT_DISABLED_BTN = Color.parseColor("#E5E1F2");
    private static final int LIGHT_DISABLED_TEXT = Color.parseColor("#A3A8B8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upi);

        repository = new MockReceiverRepository();
        
        initializeViews();
        setupListeners();
        applyDarkTheme(); // Default to dark theme to match existing flow
    }

    private void initializeViews() {
        searchInput = findViewById(R.id.searchInput);
        searchButton = findViewById(R.id.searchButton);
        searchProgress = findViewById(R.id.searchProgress);
        searchErrorText = findViewById(R.id.searchErrorText);
        
        resultCard = findViewById(R.id.resultCard);
        resultStatusTag = findViewById(R.id.resultStatusTag);
        resultName = findViewById(R.id.resultName);
        resultUpiId = findViewById(R.id.resultUpiId);
        resultPhone = findViewById(R.id.resultPhone);
        resultSource = findViewById(R.id.resultSource);
        
        safeScanButton = findViewById(R.id.safeScanButton);
        themeToggle = findViewById(R.id.themeToggle);
        
        searchContainer = findViewById(R.id.searchContainer);
        safetyInfoContainer = findViewById(R.id.safetyInfoContainer);
        resultDivider = findViewById(R.id.resultDivider);
        
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        searchButton.setOnClickListener(v -> performSearch());
        
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

        safeScanButton.setOnClickListener(v -> proceedToPayment());

        themeToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) applyLightTheme();
            else applyDarkTheme();
        });
    }

    private void performSearch() {
        if (isSearchRunning) return;

        String query = searchInput.getText().toString().trim();
        
        if (query.isEmpty()) {
            showError("Please enter a UPI ID or phone number.");
            return;
        }

        if (!isValidQuery(query)) {
            showError("Invalid format. Please enter a valid 10-digit phone number or UPI ID (e.g., name@bank).");
            return;
        }

        hideError();
        hideKeyboard();
        setSearchState(true);
        selectedReceiver = null;
        updateSafeScanButton();
        resultCard.setVisibility(View.GONE);

        repository.lookupReceiver(query, new ReceiverRepository.LookupCallback() {
            @Override
            public void onSuccess(Receiver receiver) {
                setSearchState(false);
                displayResult(receiver);
            }

            @Override
            public void onError(String message) {
                setSearchState(false);
                showError(message);
            }

            @Override
            public void onNotFound() {
                setSearchState(false);
                showError("No receiver found for this details.");
            }
        });
    }

    private boolean isValidQuery(String query) {
        // Basic Phone validation (10 digits)
        boolean isPhone = Pattern.matches("^[6-9]\\d{9}$", query);
        // Basic UPI validation (alphanumeric/dot/dash @ bank)
        boolean isUpi = Pattern.matches("^[a-zA-Z0-9.-]{2,256}@[a-zA-Z][a-zA-Z]{2,64}$", query);
        
        return isPhone || isUpi;
    }

    private void displayResult(Receiver receiver) {
        selectedReceiver = receiver;
        resultCard.setVisibility(View.VISIBLE);
        
        resultName.setText(receiver.getName());
        resultUpiId.setText(receiver.getUpiId());
        
        if (receiver.getPhoneNumber() != null && !receiver.getPhoneNumber().isEmpty()) {
            resultPhone.setVisibility(View.VISIBLE);
            resultPhone.setText("+91 " + receiver.getPhoneNumber());
        } else {
            resultPhone.setVisibility(View.GONE);
        }
        
        resultSource.setText("Source: " + receiver.getSource());

        // Status Tag Styling
        switch (receiver.getSafetyStatus()) {
            case VERIFIED:
                resultStatusTag.setText("✓ VERIFIED");
                resultStatusTag.setTextColor(GREEN);
                resultStatusTag.setBackgroundColor(Color.parseColor(isLightTheme ? "#DDF7F0" : "#12372F"));
                break;
            case CAUTION:
                resultStatusTag.setText("⚠ CAUTION");
                resultStatusTag.setTextColor(AMBER);
                resultStatusTag.setBackgroundColor(Color.parseColor(isLightTheme ? "#FEF3C7" : "#453205"));
                break;
            case RISK:
                resultStatusTag.setText("✕ RISK IDENTIFIED");
                resultStatusTag.setTextColor(RED);
                resultStatusTag.setBackgroundColor(Color.parseColor(isLightTheme ? "#FEE2E2" : "#450A0A"));
                break;
            case UNAVAILABLE:
            default:
                resultStatusTag.setText("UNKNOWN");
                resultStatusTag.setTextColor(isLightTheme ? LIGHT_SECONDARY : DARK_SECONDARY);
                resultStatusTag.setBackgroundColor(isLightTheme ? LIGHT_DIVIDER : DARK_DIVIDER);
                break;
        }

        updateSafeScanButton();
    }

    private void updateSafeScanButton() {
        if (selectedReceiver != null && selectedReceiver.getUpiId() != null && !selectedReceiver.getUpiId().isEmpty()) {
            safeScanButton.setEnabled(true);
            safeScanButton.setBackgroundTintList(ColorStateList.valueOf(isLightTheme ? LIGHT_PURPLE : PURPLE));
            safeScanButton.setTextColor(Color.WHITE);
        } else {
            safeScanButton.setEnabled(false);
            safeScanButton.setBackgroundTintList(ColorStateList.valueOf(isLightTheme ? LIGHT_DISABLED_BTN : DARK_DISABLED_BTN));
            safeScanButton.setTextColor(isLightTheme ? LIGHT_DISABLED_TEXT : DARK_DISABLED_TEXT);
        }
    }

    private void proceedToPayment() {
        if (selectedReceiver == null) return;
        
        Intent intent = new Intent(this, PaymentDetailsActivity.class);
        intent.putExtra("upi_id", selectedReceiver.getUpiId());
        intent.putExtra("upi_name", selectedReceiver.getName());
        intent.putExtra("safety_status", selectedReceiver.getSafetyStatus().name());
        startActivity(intent);
    }

    private void setSearchState(boolean running) {
        isSearchRunning = running;
        searchProgress.setVisibility(running ? View.VISIBLE : View.GONE);
        searchButton.setEnabled(!running);
        searchInput.setEnabled(!running);
        if (running) {
             searchButton.setText("");
        } else {
             searchButton.setText("Search");
        }
    }

    private void showError(String message) {
        searchErrorText.setText(message);
        searchErrorText.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        searchErrorText.setVisibility(View.GONE);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    // ==================================================
    // THEME HANDLING
    // ==================================================

    private void applyDarkTheme() {
        isLightTheme = false;
        View root = findViewById(R.id.upiRoot);
        root.setBackgroundColor(DARK_BACKGROUND);
        
        themeToggle.setText("Light");
        themeToggle.setTextColor(DARK_PRIMARY);
        
        searchContainer.setBackgroundColor(DARK_CARD);
        resultCard.setCardBackgroundColor(DARK_RESULT_CARD);
        safetyInfoContainer.setBackgroundColor(DARK_SAFETY_CARD);
        resultDivider.setBackgroundColor(DARK_DIVIDER);

        searchInput.setTextColor(DARK_PRIMARY);
        searchInput.setHintTextColor(DARK_MUTED);

        updateTextColors(root, false);
        updateSafeScanButton();
        if (selectedReceiver != null) displayResult(selectedReceiver); // Refresh tag colors
    }

    private void applyLightTheme() {
        isLightTheme = true;
        View root = findViewById(R.id.upiRoot);
        root.setBackgroundColor(LIGHT_BACKGROUND);
        
        themeToggle.setText("Dark");
        themeToggle.setTextColor(LIGHT_PRIMARY);
        
        searchContainer.setBackgroundColor(LIGHT_CARD);
        resultCard.setCardBackgroundColor(LIGHT_RESULT_CARD);
        safetyInfoContainer.setBackgroundColor(LIGHT_SAFETY_CARD);
        resultDivider.setBackgroundColor(LIGHT_DIVIDER);

        searchInput.setTextColor(LIGHT_PRIMARY);
        searchInput.setHintTextColor(LIGHT_MUTED);

        updateTextColors(root, true);
        updateSafeScanButton();
        if (selectedReceiver != null) displayResult(selectedReceiver); // Refresh tag colors
    }

    private void updateTextColors(View view, boolean light) {
        if (view instanceof TextView && !(view instanceof Button) && view != searchInput) {
            TextView textView = (TextView) view;
            String tag = textView.getTag() == null ? "" : textView.getTag().toString();

            if (tag.equals("secondary")) {
                textView.setTextColor(light ? LIGHT_SECONDARY : DARK_SECONDARY);
            } else if (tag.equals("muted")) {
                textView.setTextColor(light ? LIGHT_MUTED : DARK_MUTED);
            } else if (tag.equals("accent")) {
                textView.setTextColor(light ? LIGHT_PURPLE : Color.parseColor("#A78BFA"));
            } else if (tag.equals("primary")) {
                textView.setTextColor(light ? LIGHT_PRIMARY : DARK_PRIMARY);
            }
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                updateTextColors(group.getChildAt(i), light);
            }
        }
    }
}
