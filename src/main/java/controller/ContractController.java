package controller;


import dto.SponsorshipCreateDTO;
import entity.Contract;
import entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import service.ContractService;
import service.FileStorageService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sponsorships")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;
    private final FileStorageService fileStorageService;

    // Existing: Create sponsorship
    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasRole('SPONSOR')")
    public ResponseEntity<Contract> createSponsorship(
            @AuthenticationPrincipal User user,
            @Valid @RequestPart("data") SponsorshipCreateDTO dto,
            @RequestPart(value = "legalDoc", required = false) MultipartFile legalDoc) {

        String docPath = null;
        if (legalDoc != null && !legalDoc.isEmpty()) {
            docPath = fileStorageService.saveFile(legalDoc);
        }

        Contract contract = contractService.createSponsorship(user, dto, docPath);
        return ResponseEntity.ok(contract);
    }

    // New: List my campaigns/sponsorships
    @GetMapping("/my")
    @PreAuthorize("hasRole('SPONSOR')")
    public ResponseEntity<List<Contract>> getMyCampaigns(@AuthenticationPrincipal User user) {
        List<Contract> campaigns = contractService.getMyCampaigns(user);
        return ResponseEntity.ok(campaigns);
    }

    // New: Cancel sponsorship
    @DeleteMapping("/{contractId}")
    @PreAuthorize("hasRole('SPONSOR')")
    public ResponseEntity<Void> cancelSponsorship(
            @PathVariable UUID contractId,
            @AuthenticationPrincipal User user) {

        contractService.cancelSponsorship(contractId, user);
        return ResponseEntity.noContent().build();
    }
}