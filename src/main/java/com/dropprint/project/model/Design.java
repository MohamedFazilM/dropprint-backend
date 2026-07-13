package com.dropprint.project.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "designs")
public class Design {

    @Id
    private String id;

    private String fileUrl;
    private String fileUrlBack;
    private String printArea;
    private String position;
    private Double positionX;
    private Double positionY;
    private Double scale;
    private Double rotation;
    private String description;
    private LocalDateTime createdAt = LocalDateTime.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public String getFileUrlBack() { return fileUrlBack; }
    public void setFileUrlBack(String fileUrlBack) { this.fileUrlBack = fileUrlBack; }
    public String getPrintArea() { return printArea; }
    public void setPrintArea(String printArea) { this.printArea = printArea; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public Double getPositionX() { return positionX; }
    public void setPositionX(Double positionX) { this.positionX = positionX; }
    public Double getPositionY() { return positionY; }
    public void setPositionY(Double positionY) { this.positionY = positionY; }
    public Double getScale() { return scale; }
    public void setScale(Double scale) { this.scale = scale; }
    public Double getRotation() { return rotation; }
    public void setRotation(Double rotation) { this.rotation = rotation; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}