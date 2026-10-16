package com.example.tryme.data;

import com.example.tryme.models.Receiver;

public interface ReceiverRepository {
    
    interface LookupCallback {
        void onSuccess(Receiver receiver);
        void onError(String message);
        void onNotFound();
    }

    void lookupReceiver(String query, LookupCallback callback);
}
