package service;

import dto.SponsorRegistrationDTO;
import entity.Role;
import entity.Sponsor;
import entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.SponsorRepository;
import repository.UserRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SponsorService {

    private final SponsorRepository sponsorRepository;
    private final UserRepository userRepository;

    @Transactional
    public Sponsor registerAsSponsor(User currentUser, SponsorRegistrationDTO dto) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(Role.SPONSOR);
        userRepository.save(user);

        Sponsor sponsor = Sponsor.builder()
                .user(user)
                .brandName(dto.getBrandName())
                .description(dto.getDescription())
                .verified(false)
                .build();

        return sponsorRepository.save(sponsor);
    }

    // New: Update brand info
    @Transactional
    public Sponsor updateSponsor(UUID sponsorId, SponsorRegistrationDTO dto, User currentUser) {
        Sponsor sponsor = sponsorRepository.findById(sponsorId)
                .orElseThrow(() -> new RuntimeException("Sponsor not found"));

        // Security check: only owner can update
        if (!sponsor.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only update your own sponsor profile");
        }

        sponsor.setBrandName(dto.getBrandName());
        sponsor.setDescription(dto.getDescription());

        return sponsorRepository.save(sponsor);
    }
}