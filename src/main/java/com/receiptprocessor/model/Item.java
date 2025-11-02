package com.receiptprocessor.model;

public class Item {
    private int id;
    private int receiptId;
    private String name;
    private double price;
    private double estimatedProtein; // in grams
    private double estimatedCalories; // in kcal

    public Item() {
    }

    public Item(String name, double price, double estimatedProtein, double estimatedCalories) {
        this.name = name;
        this.price = price;
        this.estimatedProtein = estimatedProtein;
        this.estimatedCalories = estimatedCalories;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(int receiptId) {
        this.receiptId = receiptId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getEstimatedProtein() {
        return estimatedProtein;
    }

    public void setEstimatedProtein(double estimatedProtein) {
        this.estimatedProtein = estimatedProtein;
    }

    public double getEstimatedCalories() {
        return estimatedCalories;
    }

    public void setEstimatedCalories(double estimatedCalories) {
        this.estimatedCalories = estimatedCalories;
    }
}
