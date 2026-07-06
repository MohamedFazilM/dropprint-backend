package com.dropprint.project.dto;

public class OrderItemRequestDTO {
    private String productId;
    private String size;
    private int qty;
    private double price;
    private String designId;

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getDesignId() { return designId; }
    public void setDesignId(String designId) { this.designId = designId; }
}