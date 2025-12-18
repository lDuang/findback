package com.lostfound.backend.service;

import com.lostfound.backend.entity.Claim;
import com.lostfound.backend.entity.LostItem;
import com.lostfound.backend.exception.NotFoundException;
import com.lostfound.backend.model.UserContext;
import com.lostfound.backend.repository.ClaimRepository;
import com.lostfound.backend.repository.LostItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final LostItemRepository lostItemRepository;
    private final AuthService authService;

    public ClaimService(ClaimRepository claimRepository, LostItemRepository lostItemRepository, AuthService authService) {
        this.claimRepository = claimRepository;
        this.lostItemRepository = lostItemRepository;
        this.authService = authService;
    }

    public List<Claim> listAll() {
        return claimRepository.findAll();
    }

    public List<Claim> listByItem(Long itemId) {
        return claimRepository.findByItemId(itemId);
    }

    public List<Claim> listByUser(Long userId) {
        return claimRepository.findByUserId(userId);
    }

    public List<Claim> listByUserAndItem(Long userId, Long itemId) {
        return claimRepository.findByUserIdAndItemId(userId, itemId);
    }

    public List<Claim> listVisibleClaims(Long userId, Long itemId, UserContext context) {
        if (context == null || context.isAdmin()) {
            if (itemId != null && userId != null) {
                return claimRepository.findByUserIdAndItemId(userId, itemId);
            }
            if (itemId != null) {
                return claimRepository.findByItemId(itemId);
            }
            if (userId != null) {
                return claimRepository.findByUserId(userId);
            }
            return claimRepository.findAll();
        }

        Long requesterId = context.getUserId();

        if (itemId != null) {
            LostItem item = lostItemRepository.findById(itemId)
                    .orElseThrow(() -> new NotFoundException("Item not found"));
            if (item.getUserId().equals(requesterId)) {
                return claimRepository.findByItemId(itemId);
            }
            if (userId != null && !userId.equals(requesterId)) {
                throw new SecurityException("普通用户只能查看自己的认领记录");
            }
            return claimRepository.findByUserIdAndItemId(requesterId, itemId);
        }

        if (userId != null && !userId.equals(requesterId)) {
            throw new SecurityException("普通用户只能查看自己的认领记录");
        }

        return claimRepository.findByUserId(requesterId);
    }

    public Claim createClaim(Claim claim, UserContext context) {
        authService.ensureUserExists(context.getUserId());
        if (claim.getItemId() == null) {
            throw new IllegalArgumentException("Item id is required");
        }
        LostItem item = lostItemRepository.findById(claim.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        claim.setUserId(context.getUserId());
        if (!StringUtils.hasText(claim.getStatus())) {
            claim.setStatus("PENDING");
        }
        claim.setItemId(item.getId());
        return claimRepository.save(claim);
    }

    public Claim updateStatus(Long claimId, String status, UserContext context) {
        if (!StringUtils.hasText(status)) {
            throw new IllegalArgumentException("Status is required");
        }
        authService.ensureUserExists(context.getUserId());
        Claim existing = claimRepository.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));
        LostItem item = lostItemRepository.findById(existing.getItemId())
                .orElseThrow(() -> new NotFoundException("Related item missing"));
        boolean isClaimOwner = existing.getUserId() != null && existing.getUserId().equals(context.getUserId());
        boolean isItemOwner = item.getUserId() != null && item.getUserId().equals(context.getUserId());
        if (!context.isAdmin() && !isClaimOwner && !isItemOwner) {
            throw new SecurityException("Operation not permitted for this user");
        }
        existing.setStatus(status);
        return claimRepository.save(existing);
    }

    public void deleteClaim(Long claimId, UserContext context) {
        authService.ensureUserExists(context.getUserId());
        Claim existing = claimRepository.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));
        LostItem item = lostItemRepository.findById(existing.getItemId())
                .orElseThrow(() -> new NotFoundException("Related item missing"));
        boolean isClaimOwner = existing.getUserId() != null && existing.getUserId().equals(context.getUserId());
        boolean isItemOwner = item.getUserId() != null && item.getUserId().equals(context.getUserId());
        if (!context.isAdmin() && !isClaimOwner && !isItemOwner) {
            throw new SecurityException("Operation not permitted for this user");
        }
        claimRepository.delete(existing);
    }
}
