package com.civicguard.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * User entity representing citizens and administrators.
 * Citizens can submit complaints and track their status.
 */
@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String name;

    @Indexed(unique = true)
    private String email;

    @Indexed(unique = true)
    private String phone;               // Indian mobile: +91XXXXXXXXXX

    private String password;             // BCrypt hashed

    private String role;                 // CITIZEN, ADMIN

    // ─── Aadhaar-based verification (optional) ────────────────
    private String aadhaarHash;          // SHA-256 hash, never store raw
    private boolean aadhaarVerified;

    // ─── Location defaults ────────────────────────────────────
    private String defaultPincode;
    private String defaultCity;
    private String defaultState;
    private String preferredLanguage;    // hi, te, ta, kn, ml, bn, mr, gu, pa, od, as, ur

    // ─── Stats ────────────────────────────────────────────────
    private int totalComplaints;
    private int resolvedComplaints;
    private int trustScore;              // 0-100, affects complaint priority

    private boolean active;

    @CreatedDate
    private LocalDateTime createdAt;

    // ─── Getters & Setters ────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getAadhaarHash() { return aadhaarHash; }
    public void setAadhaarHash(String aadhaarHash) { this.aadhaarHash = aadhaarHash; }
    public boolean isAadhaarVerified() { return aadhaarVerified; }
    public void setAadhaarVerified(boolean aadhaarVerified) { this.aadhaarVerified = aadhaarVerified; }
    public String getDefaultPincode() { return defaultPincode; }
    public void setDefaultPincode(String defaultPincode) { this.defaultPincode = defaultPincode; }
    public String getDefaultCity() { return defaultCity; }
    public void setDefaultCity(String defaultCity) { this.defaultCity = defaultCity; }
    public String getDefaultState() { return defaultState; }
    public void setDefaultState(String defaultState) { this.defaultState = defaultState; }
    public String getPreferredLanguage() { return preferredLanguage; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }
    public int getTotalComplaints() { return totalComplaints; }
    public void setTotalComplaints(int totalComplaints) { this.totalComplaints = totalComplaints; }
    public int getResolvedComplaints() { return resolvedComplaints; }
    public void setResolvedComplaints(int resolvedComplaints) { this.resolvedComplaints = resolvedComplaints; }
    public int getTrustScore() { return trustScore; }
    public void setTrustScore(int trustScore) { this.trustScore = trustScore; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
