package com.ipeirotis.entity;

public class Selection {
    
    private String identifier;
    private String text;
    private String binaryContentUrl;

    public Selection() {
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getBinaryContentUrl() {
        return binaryContentUrl;
    }

    public void setBinaryContentUrl(String binaryContentUrl) {
        this.binaryContentUrl = binaryContentUrl;
    }


}
