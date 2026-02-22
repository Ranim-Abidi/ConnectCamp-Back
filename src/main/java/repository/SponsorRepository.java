package repository;  // adjust package

import entity.Sponsor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SponsorRepository extends JpaRepository<Sponsor, UUID> {
    Optional<Sponsor> findByUserId(UUID userId);
}