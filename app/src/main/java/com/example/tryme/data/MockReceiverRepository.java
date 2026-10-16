package com.example.tryme.data;

import android.os.Handler;
import android.os.Looper;

import com.example.tryme.models.Receiver;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MockReceiverRepository implements ReceiverRepository {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void lookupReceiver(String query, LookupCallback callback) {
        executorService.execute(() -> {
            // Simulate network/database delay
            try {
                Thread.sleep(1200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                postError(callback, "Lookup interrupted.");
                return;
            }

            String cleanQuery = query.trim().toLowerCase();

            // Mock Neon Database Result (VERIFIED)
            if (cleanQuery.equals("john@okhdfcbank") || cleanQuery.equals("9876543210") || cleanQuery.equals("8888888888")) {
                postSuccess(callback, new Receiver(
                        "john@okhdfcbank",
                        "John Doe",
                        cleanQuery.matches("^[6-9]\\d{9}$") ? cleanQuery : "9876543210",
                        Receiver.SafetyStatus.VERIFIED,
                        "Neon Verified Database"
                ));
            } 
            // Mock External Service (CAUTION)
            else if (cleanQuery.equals("jane@ybl") || cleanQuery.equals("9998887776") || cleanQuery.equals("9999999999")) {
                 postSuccess(callback, new Receiver(
                        "jane@ybl",
                        "Jane Smith",
                        null, // Phone not available from external
                        Receiver.SafetyStatus.CAUTION,
                        "External Lookup (gUPI-recon)"
                ));
            }
            // Mock Risk Scenario (RISK)
            else if (cleanQuery.equals("scammer@icici") || cleanQuery.equals("7777777777")) {
                 postSuccess(callback, new Receiver(
                        "scammer@icici",
                        "Unknown Fraudster",
                        "7777777777",
                        Receiver.SafetyStatus.RISK,
                        "Community Reports"
                ));
            }
            else {
                postNotFound(callback);
            }
        });
    }

    private void postSuccess(LookupCallback callback, Receiver receiver) {
        mainHandler.post(() -> callback.onSuccess(receiver));
    }

    private void postError(LookupCallback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }

    private void postNotFound(LookupCallback callback) {
        mainHandler.post(callback::onNotFound);
    }
}
