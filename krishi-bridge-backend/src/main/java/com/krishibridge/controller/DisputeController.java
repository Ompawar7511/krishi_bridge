package com.krishibridge.controller;

import com.krishibridge.dto.request.DisputeCreateRequest;
import com.krishibridge.dto.request.DisputeResolveRequest;
import com.krishibridge.dto.response.DisputeResponse;
import com.krishibridge.dto.response.StandardResponse;
import com.krishibridge.entity.Dispute;
import com.krishibridge.service.DisputeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class DisputeController {

    private final DisputeService disputeService;

    public DisputeController(DisputeService disputeService) {
        this.disputeService = disputeService;
    }

    @PostMapping("/disputes/create")
    public ResponseEntity<StandardResponse<DisputeResponse>> createDispute(@Valid @RequestBody DisputeCreateRequest request) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Dispute dispute = disputeService.createDispute(request.getBookingId(), userId, request.getReason(), request.getDescription());
        DisputeResponse responseDto = mapToDto(dispute);
        return ResponseEntity.ok(StandardResponse.success(responseDto, "Dispute raised successfully. Status pending investigation."));
    }

    @PutMapping("/admin/disputes/resolve/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StandardResponse<DisputeResponse>> resolveDispute(
            @PathVariable("id") Long id,
            @Valid @RequestBody DisputeResolveRequest request) {
        
        Dispute dispute = disputeService.resolveDispute(id, request.getStatus(), request.getAdminNotes());
        DisputeResponse responseDto = mapToDto(dispute);
        return ResponseEntity.ok(StandardResponse.success(responseDto, "Dispute resolved successfully"));
    }

    @GetMapping("/admin/disputes/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StandardResponse<List<DisputeResponse>>> getPendingDisputes() {
        List<Dispute> disputes = disputeService.getPendingDisputes();
        List<DisputeResponse> responseList = disputes.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(StandardResponse.success(responseList));
    }

    private DisputeResponse mapToDto(Dispute dispute) {
        return new DisputeResponse(
                dispute.getId(),
                dispute.getBookingId(),
                dispute.getRaisedBy(),
                dispute.getReason(),
                dispute.getDescription(),
                dispute.getStatus(),
                dispute.getAdminNotes(),
                dispute.getCreatedAt(),
                dispute.getResolvedAt()
        );
    }
}
