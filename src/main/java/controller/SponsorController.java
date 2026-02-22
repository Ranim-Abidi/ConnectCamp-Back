package controller;

import dto.SponsorRegistrationDTO;
import entity.Sponsor;
import entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import service.FileStorageService;
import service.SponsorService;

import java.util.UUID;

@RestController
@RequestMapping("/api/sponsors")
@RequiredArgsConstructor
public class SponsorController {

    private final SponsorService sponsorService;
    private final FileStorageService fileStorageService;

    // Existing: Register as sponsor
    @PostMapping(value = "/register", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('CAMPER', 'SPONSOR')")
    public ResponseEntity<Sponsor> registerAsSponsor(
            @AuthenticationPrincipal User user,
            @Valid @RequestPart("data") SponsorRegistrationDTO dto,
            @RequestPart(value = "legalDoc", required = false) MultipartFile legalDoc) {

        Sponsor sponsor = sponsorService.registerAsSponsor(user, dto);

        if (legalDoc != null && !legalDoc.isEmpty()) {
            String path = fileStorageService.saveFile(legalDoc);
            // Optional: store path in sponsor or contract
        }

        return ResponseEntity.ok(sponsor);
    }

    // New: Update brand info
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SPONSOR')")
    public ResponseEntity<Sponsor> updateBrandInfo(
            @PathVariable UUID id,
            @Valid @RequestBody SponsorRegistrationDTO dto,
            @AuthenticationPrincipal User user) {

        Sponsor updated = sponsorService.updateSponsor(id, dto, user);
        return ResponseEntity.ok(updated);
    }
}