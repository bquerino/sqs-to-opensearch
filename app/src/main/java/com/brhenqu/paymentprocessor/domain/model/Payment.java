package com.brhenqu.paymentprocessor.domain.model;

public class Payment{
    private String id;
    private String userId;
    private double amount;
    private String currency;
    private String status;

    public Payment(String id, String userId, double amount, String currency, String status) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
    }

    public Payment() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
