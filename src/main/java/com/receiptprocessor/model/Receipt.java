package com.receiptprocessor.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Receipt {
    private int id;
    private String filename;
    private LocalDateTime processedDate;
    private double totalPrice;
    private List<Item> items;

    public Receipt() {
        this.items = new ArrayList<>();
    }

    public Receipt(String filename, LocalDateTime processedDate, double totalPrice) {
        this.filename = filename;
        this.processedDate = processedDate;
        this.totalPrice = totalPrice;
        this.items = new ArrayList<>();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public LocalDateTime getProcessedDate() {
        return processedDate;
    }

    public void setProcessedDate(LocalDateTime processedDate) {
        this.processedDate = processedDate;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public void addItem(Item item) {
        this.items.add(item);
    }
}
