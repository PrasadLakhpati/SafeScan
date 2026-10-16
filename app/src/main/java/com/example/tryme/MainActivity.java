package com.example.tryme;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.camera.core.ExperimentalGetImage;
import androidx.cardview.widget.CardView;

public class MainActivity extends AppCompatActivity {

    private CardView scanQrCard;
    private CardView manualPaymentCard;
    private CardView checkUpiCard;
    private CardView paymentInfoCard;
    private CardView paymentCard;
    private CardView protectionCard;
    private CardView securityBadge;
    private CardView bottomNav;

    private Button payButton;
    private SwitchCompat themeToggle;

    private static final int DARK_BACKGROUND =
            Color.parseColor("#0B1020");

    private static final int DARK_CARD =
            Color.parseColor("#151C32");

    private static final int DARK_CARD_HOVER =
            Color.parseColor("#1B2440");

    private static final int DARK_PAYMENT =
            Color.parseColor("#292348");

    private static final int DARK_PAYMENT_HOVER =
            Color.parseColor("#342D5A");

    private static final int DARK_PROTECTION =
            Color.parseColor("#111A2D");

    private static final int DARK_PROTECTION_HOVER =
            Color.parseColor("#17233C");

    private static final int LIGHT_BACKGROUND =
            Color.parseColor("#F7F5FF");

    private static final int LIGHT_CARD =
            Color.parseColor("#FFFFFF");

    private static final int LIGHT_CARD_HOVER =
            Color.parseColor("#F0EDFF");

    private static final int LIGHT_PAYMENT =
            Color.parseColor("#E9E3FF");

    private static final int LIGHT_PAYMENT_HOVER =
            Color.parseColor("#DDD5FF");

    private static final int LIGHT_PROTECTION =
            Color.parseColor("#F0EEFA");

    private static final int LIGHT_PROTECTION_HOVER =
            Color.parseColor("#E5E1F2");

    private static final int DARK_PRIMARY =
            Color.parseColor("#F8FAFC");

    private static final int DARK_SECONDARY =
            Color.parseColor("#7F89A6");

    private static final int DARK_MUTED =
            Color.parseColor("#69748F");

    private static final int LIGHT_PRIMARY =
            Color.parseColor("#17152A");

    private static final int LIGHT_SECONDARY =
            Color.parseColor("#5F6475");

    private static final int LIGHT_MUTED =
            Color.parseColor("#737789");

    private static final int PURPLE =
            Color.parseColor("#7C5CFC");

    private static final int LIGHT_PURPLE =
            Color.parseColor("#6D4FF5");

    private static final int GREEN =
            Color.parseColor("#22D3A3");

    private boolean isLightTheme = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initializeViews();
        setupInteractions();
        setupAnimations();

        applyDarkTheme();
    }

    private void initializeViews() {

        scanQrCard = findViewById(R.id.scanQrCard);
        manualPaymentCard = findViewById(R.id.manualPaymentCard);
        checkUpiCard = findViewById(R.id.checkUpiCard);
        paymentInfoCard = findViewById(R.id.paymentInfoCard);

        paymentCard = findViewById(R.id.paymentCard);
        protectionCard = findViewById(R.id.protectionCard);
        securityBadge = findViewById(R.id.securityBadge);
        bottomNav = findViewById(R.id.bottomNav);

        payButton = findViewById(R.id.payButton);
        themeToggle = findViewById(R.id.themeToggle);
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void setupInteractions() {

        scanQrCard.setOnClickListener(v -> {

            Intent intent =
                    new Intent(MainActivity.this, QrActivity.class);

            startActivity(intent);
        });

        manualPaymentCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UPIActivity.class);
            startActivity(intent);
        });

        checkUpiCard.setOnClickListener(v ->
                showToast("UPI verification coming soon")
        );

        paymentInfoCard.setOnClickListener(v ->
                showToast("Payment information")
        );

        paymentCard.setOnClickListener(v ->
                showToast("Secure payment selected")
        );

        securityBadge.setOnClickListener(v ->
                showToast("Payment protection is active")
        );

        themeToggle.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    if (isChecked) {
                        applyLightTheme();
                    } else {
                        applyDarkTheme();
                    }
                }
        );
    }

    // ==================================================
    // DARK THEME
    // ==================================================

    private void applyDarkTheme() {

        isLightTheme = false;

        View root = findViewById(R.id.mainRoot);

        root.setBackgroundColor(DARK_BACKGROUND);

        themeToggle.setText("Light");
        themeToggle.setTextColor(DARK_PRIMARY);

        scanQrCard.setCardBackgroundColor(DARK_CARD);
        manualPaymentCard.setCardBackgroundColor(DARK_CARD);
        checkUpiCard.setCardBackgroundColor(DARK_CARD);
        paymentInfoCard.setCardBackgroundColor(DARK_CARD);

        securityBadge.setCardBackgroundColor(DARK_CARD);
        paymentCard.setCardBackgroundColor(DARK_PAYMENT);
        protectionCard.setCardBackgroundColor(DARK_PROTECTION);
        bottomNav.setCardBackgroundColor(DARK_CARD);

        updateTextColors(root, false);
        updateIconBackgrounds(false);

        payButton.setBackgroundTintList(
                ColorStateList.valueOf(PURPLE)
        );

        payButton.setTextColor(Color.WHITE);
    }

    // ==================================================
    // LIGHT THEME
    // ==================================================

    private void applyLightTheme() {

        isLightTheme = true;

        View root = findViewById(R.id.mainRoot);

        root.setBackgroundColor(LIGHT_BACKGROUND);

        themeToggle.setText("Dark");
        themeToggle.setTextColor(LIGHT_PRIMARY);

        scanQrCard.setCardBackgroundColor(LIGHT_CARD);
        manualPaymentCard.setCardBackgroundColor(LIGHT_CARD);
        checkUpiCard.setCardBackgroundColor(LIGHT_CARD);
        paymentInfoCard.setCardBackgroundColor(LIGHT_CARD);

        securityBadge.setCardBackgroundColor(LIGHT_CARD);
        paymentCard.setCardBackgroundColor(LIGHT_PAYMENT);
        protectionCard.setCardBackgroundColor(LIGHT_PROTECTION);
        bottomNav.setCardBackgroundColor(LIGHT_CARD);

        updateTextColors(root, true);
        updateIconBackgrounds(true);

        payButton.setBackgroundTintList(
                ColorStateList.valueOf(LIGHT_PURPLE)
        );

        payButton.setTextColor(Color.WHITE);
    }

    // ==================================================
    // TEXT COLORS
    // ==================================================

    private void updateTextColors(
            View view,
            boolean light
    ) {

        if (view instanceof TextView) {

            TextView textView =
                    (TextView) view;

            String tag =
                    textView.getTag() == null
                            ? ""
                            : textView.getTag().toString();

            if (tag.equals("secondary")) {

                textView.setTextColor(
                        light
                                ? LIGHT_SECONDARY
                                : DARK_SECONDARY
                );

            } else if (tag.equals("muted")) {

                textView.setTextColor(
                        light
                                ? LIGHT_MUTED
                                : DARK_MUTED
                );

            } else if (tag.equals("accent")) {

                textView.setTextColor(
                        light
                                ? LIGHT_PURPLE
                                : Color.parseColor("#A78BFA")
                );

            } else if (tag.equals("green")) {

                textView.setTextColor(GREEN);

            } else if (tag.equals("button")) {

                textView.setTextColor(Color.WHITE);

            } else if (tag.equals("white")) {

                textView.setTextColor(
                        light
                                ? LIGHT_PRIMARY
                                : Color.WHITE
                );

            } else {

                textView.setTextColor(
                        light
                                ? LIGHT_PRIMARY
                                : DARK_PRIMARY
                );
            }
        }

        if (view instanceof ViewGroup) {

            ViewGroup group =
                    (ViewGroup) view;

            for (int i = 0;
                 i < group.getChildCount();
                 i++) {

                updateTextColors(
                        group.getChildAt(i),
                        light
                );
            }
        }
    }

    // ==================================================
    // ICON COLORS
    // ==================================================

    private void updateIconBackgrounds(
            boolean light
    ) {

        TextView qrIcon =
                findViewById(R.id.qrIcon);

        TextView upiIcon =
                findViewById(R.id.upiIcon);

        TextView protectionIcon =
                findViewById(R.id.protectionIcon);

        TextView checkUpiIcon =
                findViewById(R.id.checkUpiIcon);

        TextView paymentInfoIcon =
                findViewById(R.id.paymentInfoIcon);

        if (light) {

            qrIcon.setBackgroundColor(
                    Color.parseColor("#E9E3FF")
            );

            upiIcon.setBackgroundColor(
                    Color.parseColor("#EEEAFE")
            );

            protectionIcon.setBackgroundColor(
                    Color.parseColor("#DDF7F0")
            );

            checkUpiIcon.setBackgroundColor(
                    Color.parseColor("#DDF7F0")
            );

            paymentInfoIcon.setBackgroundColor(
                    Color.parseColor("#E9E3FF")
            );

        } else {

            qrIcon.setBackgroundColor(
                    DARK_PAYMENT
            );

            upiIcon.setBackgroundColor(
                    Color.parseColor("#18213A")
            );

            protectionIcon.setBackgroundColor(
                    Color.parseColor("#12372F")
            );

            checkUpiIcon.setBackgroundColor(
                    Color.parseColor("#12372F")
            );

            paymentInfoIcon.setBackgroundColor(
                    DARK_PAYMENT
            );
        }
    }

    // ==================================================
    // ANIMATIONS
    // ==================================================

    private void setupAnimations() {

        addCardAnimation(scanQrCard);
        addCardAnimation(manualPaymentCard);
        addCardAnimation(checkUpiCard);
        addCardAnimation(paymentInfoCard);
        addCardAnimation(paymentCard);
        addCardAnimation(protectionCard);
        addCardAnimation(securityBadge);

        addButtonAnimation(payButton);
    }

    private void addCardAnimation(
            CardView card
    ) {

        card.setOnTouchListener(
                (view, event) -> {

                    switch (event.getAction()) {

                        case MotionEvent.ACTION_DOWN:

                            card.setCardBackgroundColor(
                                    getHoverColor(card)
                            );

                            animateScale(
                                    card,
                                    0.97f
                            );

                            break;

                        case MotionEvent.ACTION_UP:

                            card.setCardBackgroundColor(
                                    getNormalColor(card)
                            );

                            animateScale(
                                    card,
                                    1.0f
                            );

                            break;

                        case MotionEvent.ACTION_CANCEL:

                            card.setCardBackgroundColor(
                                    getNormalColor(card)
                            );

                            animateScale(
                                    card,
                                    1.0f
                            );

                            break;
                    }

                    return false;
                }
        );
    }

    private int getNormalColor(
            CardView card
    ) {

        if (card == paymentCard) {

            return isLightTheme
                    ? LIGHT_PAYMENT
                    : DARK_PAYMENT;
        }

        if (card == protectionCard) {

            return isLightTheme
                    ? LIGHT_PROTECTION
                    : DARK_PROTECTION;
        }

        return isLightTheme
                ? LIGHT_CARD
                : DARK_CARD;
    }

    private int getHoverColor(
            CardView card
    ) {

        if (card == paymentCard) {

            return isLightTheme
                    ? LIGHT_PAYMENT_HOVER
                    : DARK_PAYMENT_HOVER;
        }

        if (card == protectionCard) {

            return isLightTheme
                    ? LIGHT_PROTECTION_HOVER
                    : DARK_PROTECTION_HOVER;
        }

        return isLightTheme
                ? LIGHT_CARD_HOVER
                : DARK_CARD_HOVER;
    }

    private void addButtonAnimation(
            Button button
    ) {

        button.setOnTouchListener(
                (view, event) -> {

                    switch (event.getAction()) {

                        case MotionEvent.ACTION_DOWN:
                            animateScale(button, 0.96f);
                            break;

                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            animateScale(button, 1.0f);
                            break;
                    }

                    return false;
                }
        );
    }

    private void animateScale(
            View view,
            float scale
    ) {

        AnimatorSet animatorSet =
                new AnimatorSet();

        ObjectAnimator scaleX =
                ObjectAnimator.ofFloat(
                        view,
                        View.SCALE_X,
                        scale
                );

        ObjectAnimator scaleY =
                ObjectAnimator.ofFloat(
                        view,
                        View.SCALE_Y,
                        scale
                );

        animatorSet.playTogether(
                scaleX,
                scaleY
        );

        animatorSet.setDuration(120);

        animatorSet.setInterpolator(
                new DecelerateInterpolator()
        );

        animatorSet.start();
    }

    // ==================================================
    // PAYMENT
    // ==================================================

    private void startPayment() {

        payButton.setEnabled(false);
        payButton.setText("Preparing payment...");

        new Handler().postDelayed(
                () -> {

                    payButton.setEnabled(true);

                    payButton.setText(
                            "Pay Securely  →"
                    );

                    showToast(
                            "Payment flow started"
                    );

                },
                800
        );
    }

    private void showToast(
            String message
    ) {

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }
}