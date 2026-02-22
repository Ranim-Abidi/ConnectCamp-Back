package repository;

import entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ContractRepository extends JpaRepository<Contract, UUID> {
    List<Contract> findBySponsorId(UUID sponsorId);
}