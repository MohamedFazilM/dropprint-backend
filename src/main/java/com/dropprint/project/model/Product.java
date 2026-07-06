package com.dropprint.project.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {

    @Id
    private String id;

    private String name;
    private String color;
    private double basePrice;
    private String fabricInfo;
    private int gsm;
    private String imageMain;
    private String imageBack;
    private String status = "active";
    private LocalDateTime createdAt = LocalDateTime.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }
    public String getFabricInfo() { return fabricInfo; }
    public void setFabricInfo(String fabricInfo) { this.fabricInfo = fabricInfo; }
    public int getGsm() { return gsm; }
    public void setGsm(int gsm) { this.gsm = gsm; }
    public String getImageMain() { return imageMain; }
    public void setImageMain(String imageMain) { this.imageMain = imageMain; }
    public String getImageBack() { return imageBack; }
    public void setImageBack(String imageBack) { this.imageBack = imageBack; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}