package dto;

import entity.InvoiceStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class InvoiceDTO {
    private UUID id;
    private Double amount;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private InvoiceStatus status;
    private UUID relatedContractId;  // or bookingId later
}