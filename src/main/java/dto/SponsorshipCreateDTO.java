package dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SponsorshipCreateDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private Double amount;
    // Later: UUID siteId or equipmentId if linking to specific entity
}