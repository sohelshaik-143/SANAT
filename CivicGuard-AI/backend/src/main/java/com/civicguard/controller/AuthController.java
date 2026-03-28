package com.civicguard.controller;

import com.civicguard.config.SecurityConfig;
import com.civicguard.model.Officer;
import com.civicguard.model.User;
import com.civicguard.repository.OfficerRepository;
import com.civicguard.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentication controller handling citizen registration, officer registration,
 * and JWT-based login for all user types.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private final UserRepository userRepo;
    private final OfficerRepository officerRepo;
    private final PasswordEncoder passwordEncoder;
    private final SecurityConfig securityConfig;

    public AuthController(UserRepository userRepo,
                          OfficerRepository officerRepo,
                          PasswordEncoder passwordEncoder,
                          SecurityConfig securityConfig) {
        this.userRepo = userRepo;
        this.officerRepo = officerRepo;
        this.passwordEncoder = passwordEncoder;
        this.securityConfig = securityConfig;
    }

    // ═══════════════════════════════════════════════════════════
    //  CITIZEN REGISTRATION
    // ═══════════════════════════════════════════════════════════

    @PostMapping("/register")
    public ResponseEntity<?> registerCitizen(@Valid @RequestBody CitizenRegisterRequest request) {

        // Check duplicates
        if (userRepo.existsByEmail(request.email)) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Email already registered"));
        }
        if (userRepo.existsByPhone(request.phone)) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Phone number already registered"));
        }

        // Create user
        User user = new User();
        user.setName(request.name);
        user.setEmail(request.email);
        user.setPhone(request.phone);
        user.setPassword(passwordEncoder.encode(request.password));
        user.setRole("CITIZEN");
        user.setDefaultPincode(request.pincode);
        user.setDefaultCity(request.city);
        user.setDefaultState(request.state);
        user.setPreferredLanguage(request.language != null ? request.language : "en");
        user.setTrustScore(50); // Start at neutral
        user.setActive(true);

        User saved = userRepo.save(user);

        // Generate JWT token
        String token = securityConfig.generateToken(saved.getId(), saved.getEmail(), "CITIZEN");

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "message", "Registration successful",
            "userId", saved.getId(),
            "token", token,
            "role", "CITIZEN"
        ));
    }

    // ═══════════════════════════════════════════════════════════
    //  OFFICER REGISTRATION (Admin only in production)
    // ═══════════════════════════════════════════════════════════

    @PostMapping("/register/officer")
    public ResponseEntity<?> registerOfficer(@Valid @RequestBody OfficerRegisterRequest request) {

        if (officerRepo.existsByEmail(request.email)) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Email already registered"));
        }

        Officer officer = new Officer();
        officer.setName(request.name);
        officer.setEmail(request.email);
        officer.setPhone(request.phone);
        officer.setPassword(passwordEncoder.encode(request.password));
        officer.setRole(request.role != null ? request.role : "OFFICER");
        officer.setDesignation(request.designation);
        officer.setEscalationTier(request.escalationTier);
        officer.setDepartment(request.department);
        officer.setEmployeeId(request.employeeId);
        officer.setState(request.state);
        officer.setDistrict(request.district);
        officer.setCity(request.city);
        officer.setPerformanceScore(50.0);
        officer.setActive(true);

        if (request.assignedPincodes != null) {
            officer.setAssignedPincodes(request.assignedPincodes);
        }

        Officer saved = officerRepo.save(officer);

        String token = securityConfig.generateToken(saved.getId(), saved.getEmail(),
            saved.getRole());

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "message", "Officer registered successfully",
            "officerId", saved.getId(),
            "token", token,
            "role", saved.getRole()
        ));
    }

    // ═══════════════════════════════════════════════════════════
    //  LOGIN (Citizens + Officers)
    // ═══════════════════════════════════════════════════════════

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {

        // Try citizen login first
        var userOpt = userRepo.findByEmail(request.email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(request.password, user.getPassword())) {
                String token = securityConfig.generateToken(
                    user.getId(), user.getEmail(), user.getRole());
                return ResponseEntity.ok(Map.of(
                    "token", token,
                    "userId", user.getId(),
                    "name", user.getName(),
                    "role", user.getRole(),
                    "language", user.getPreferredLanguage() != null ? user.getPreferredLanguage() : "en"
                ));
            }
        }

        // Try officer login
        var officerOpt = officerRepo.findByEmail(request.email);
        if (officerOpt.isPresent()) {
            Officer officer = officerOpt.get();
            if (passwordEncoder.matches(request.password, officer.getPassword())) {
                String token = securityConfig.generateToken(
                    officer.getId(), officer.getEmail(), officer.getRole());
                return ResponseEntity.ok(Map.of(
                    "token", token,
                    "userId", officer.getId(),
                    "name", officer.getName(),
                    "role", officer.getRole(),
                    "department", officer.getDepartment(),
                    "designation", officer.getDesignation()
                ));
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("error", "Invalid email or password"));
    }

    // ═══════════════════════════════════════════════════════════
    //  REQUEST DTOs
    // ═══════════════════════════════════════════════════════════

    public record CitizenRegisterRequest(
        @NotBlank String name,
        @Email @NotBlank String email,
        @Pattern(regexp = "^\\+91[6-9]\\d{9}$", message = "Invalid Indian mobile number")
        String phone,
        @Size(min = 8, message = "Password must be at least 8 characters")
        @NotBlank String password,
        String pincode,
        String city,
        String state,
        String language
    ) {}

    public record OfficerRegisterRequest(
        @NotBlank String name,
        @Email @NotBlank String email,
        @NotBlank String phone,
        @NotBlank String password,
        String role,
        @NotBlank String designation,
        int escalationTier,
        @NotBlank String department,
        @NotBlank String employeeId,
        String state,
        String district,
        String city,
        java.util.List<String> assignedPincodes
    ) {}

    public record LoginRequest(
        @Email @NotBlank String email,
        @NotBlank String password
    ) {}
}
