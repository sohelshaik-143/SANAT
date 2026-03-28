package com.civicguard.dto;

import jakarta.validation.constraints.*;

/**
 * DTO for submitting a new civic complaint.
 * Captures the minimum required information from citizens
 * along with auto-captured GPS coordinates.
 */
public class ComplaintRequest {

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10-2000 characters")
    private String description;

    private String category;            // Optional — AI will auto-classify if empty

    private String subcategory;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "6.0", message = "Invalid latitude for India")
    @DecimalMax(value = "37.0", message = "Invalid latitude for India")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "68.0", message = "Invalid longitude for India")
    @DecimalMax(value = "98.0", message = "Invalid longitude for India")
    private Double longitude;

    private String address;

    private String landmark;

    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid Indian pincode")
    private String pincode;

    private String district;

    private String state;

    private String ward;

    // Image is sent as multipart, not in this DTO

    // ─── Getters & Setters ────────────────────────────────────

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSubcategory() { return subcategory; }
    public void setSubcategory(String subcategory) { this.subcategory = subcategory; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getLandmark() { return landmark; }
    public void setLandmark(String landmark) { this.landmark = landmark; }
    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getWard() { return ward; }
    public void setWard(String ward) { this.ward = ward; }
}
