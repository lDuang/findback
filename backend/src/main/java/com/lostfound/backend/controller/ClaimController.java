package com.lostfound.backend.controller;

import com.lostfound.backend.dto.ClaimRequest;
import com.lostfound.backend.dto.StatusUpdateRequest;
import com.lostfound.backend.entity.Claim;
import com.lostfound.backend.model.UserContext;
import com.lostfound.backend.service.AuthService;
import com.lostfound.backend.service.ClaimService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;

@RestController
@RequestMapping("/claims")
public class ClaimController {

    private final ClaimService claimService;
    private final AuthService authService;

    public ClaimController(ClaimService claimService, AuthService authService) {
        this.claimService = claimService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<List<Claim>> list(@RequestParam(required = false) Long userId,
                                            @RequestParam(required = false) Long itemId,
                                            HttpServletRequest servletRequest) {
        UserContext context = authService.extractOptionalContext(
                servletRequest.getHeader("X-User-Id"), servletRequest.getHeader("X-User-Role"));
        return ResponseEntity.ok(claimService.listVisibleClaims(userId, itemId, context));
    }

    @PostMapping
    public ResponseEntity<Claim> create(@RequestBody ClaimRequest request, HttpServletRequest servletRequest) {
        UserContext context = authService.extractContext(servletRequest.getHeader("X-User-Id"), servletRequest.getHeader("X-User-Role"));
        Claim claim = new Claim();
        claim.setItemId(request.getItemId());
        claim.setReason(request.getReason());
        claim.setStatus(request.getStatus());
        claim.setEvidenceUrl(request.getEvidenceUrl());
        return ResponseEntity.status(HttpStatus.CREATED).body(claimService.createClaim(claim, context));
    }

    @PutMapping({"/{id}", "/{id}/status"})
    public ResponseEntity<Claim> updateStatus(@PathVariable Long id,
                                              @RequestBody StatusUpdateRequest request,
                                              HttpServletRequest servletRequest) {
        UserContext context = authService.extractContext(servletRequest.getHeader("X-User-Id"), servletRequest.getHeader("X-User-Role"));
        return ResponseEntity.ok(claimService.updateStatus(id, request.getStatus(), context));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpServletRequest servletRequest) {
        UserContext context = authService.extractContext(servletRequest.getHeader("X-User-Id"), servletRequest.getHeader("X-User-Role"));
        claimService.deleteClaim(id, context);
        return ResponseEntity.noContent().build();
    }
}
