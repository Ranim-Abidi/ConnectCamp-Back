package service;

import dto.SponsorshipCreateDTO;
import entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.ContractRepository;
import repository.InvoiceRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final InvoiceRepository invoiceRepository;

    @Transactional
    public Contract createSponsorship(User currentUser, SponsorshipCreateDTO dto, String documentPath) {
        Sponsor sponsor = currentUser.getSponsor();
        if (sponsor == null) {
            throw new IllegalStateException("User must register as sponsor first");
        }

        Contract contract = Contract.builder()
                .type(ContractType.SPONSORSHIP)
                .sponsor(sponsor)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .amount(dto.getAmount())
                .legalDocumentPath(documentPath)
                .build();

        Contract savedContract = contractRepository.save(contract);

        Invoice invoice = Invoice.builder()
                .amount(dto.getAmount())
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .status(InvoiceStatus.UNPAID)
                .issuedTo(currentUser)
                .contract(savedContract)
                .build();

        Invoice savedInvoice = invoiceRepository.save(invoice);

        savedContract.setInvoice(savedInvoice);
        contractRepository.save(savedContract);

        return savedContract;
    }

    public List<Contract> getMyCampaigns(User currentUser) {
        Sponsor sponsor = currentUser.getSponsor();
        if (sponsor == null) {
            return List.of();
        }
        return contractRepository.findBySponsorId(sponsor.getId());
    }

    @Transactional
    public void cancelSponsorship(UUID contractId, User currentUser) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        if (!contract.getSponsor().getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only cancel your own campaigns");
        }

        Invoice invoice = contract.getInvoice();
        if (invoice != null) {
            invoice.setStatus(InvoiceStatus.CANCELED);
            invoiceRepository.save(invoice);
        }

        contractRepository.delete(contract);
    }
}