package com.example.tryme.models;

public class Receiver {
    private String upiId;
    private String name;
    private String phoneNumber;
    private SafetyStatus safetyStatus;
    private String source;

    public enum SafetyStatus {
        VERIFIED,
        CAUTION,
        RISK,
        UNAVAILABLE
    }

    public Receiver(String upiId, String name, String phoneNumber, SafetyStatus safetyStatus, String source) {
        this.upiId = upiId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.safetyStatus = safetyStatus;
        this.source = source;
    }

    public String getUpiId() {
        return upiId;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public SafetyStatus getSafetyStatus() {
        return safetyStatus;
    }

    public String getSource() {
        return source;
    }
}
