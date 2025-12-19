package com.lostfound.backend.service;

import com.lostfound.backend.entity.LostItem;
import com.lostfound.backend.exception.NotFoundException;
import com.lostfound.backend.model.UserContext;
import com.lostfound.backend.repository.ClaimRepository;
import com.lostfound.backend.repository.LostItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class LostItemService {

    private final LostItemRepository lostItemRepository;
    private final ClaimRepository claimRepository;
    private final AuthService authService;

    public LostItemService(LostItemRepository lostItemRepository, ClaimRepository claimRepository, AuthService authService) {
        this.lostItemRepository = lostItemRepository;
        this.claimRepository = claimRepository;
        this.authService = authService;
    }

    public List<LostItem> listAll() {
        return lostItemRepository.findAll();
    }

    public List<LostItem> listForUser(Long userId) {
        return lostItemRepository.findByUserId(userId);
    }

    public List<LostItem> listFiltered(Long userId, String status) {
        if (userId != null && status != null) {
            return lostItemRepository.findByUserIdAndStatus(userId, status);
        }
        if (userId != null) {
            return lostItemRepository.findByUserId(userId);
        }
        if (status != null) {
            return lostItemRepository.findByStatus(status);
        }
        return lostItemRepository.findAll();
    }

    public LostItem createItem(LostItem item, UserContext context) {
        authService.ensureUserExists(context.getUserId());
        if (!StringUtils.hasText(item.getTitle())) {
            throw new IllegalArgumentException("Title is required");
        }
        item.setUserId(context.getUserId());
        if (!StringUtils.hasText(item.getStatus())) {
            item.setStatus("OPEN");
        }
        return lostItemRepository.save(item);
    }

    public LostItem findById(Long id) {
        return lostItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Item not found"));
    }

    public LostItem updateItem(Long itemId, LostItem updated, UserContext context) {
        LostItem existing = lostItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));
        authService.ensureOwnershipOrAdmin(context, existing.getUserId());
        if (StringUtils.hasText(updated.getTitle())) {
            existing.setTitle(updated.getTitle());
        }
        existing.setDescription(updated.getDescription());
        if (StringUtils.hasText(updated.getStatus())) {
            existing.setStatus(updated.getStatus());
        }
        existing.setLocation(updated.getLocation());
        existing.setImageUrl(updated.getImageUrl());
        return lostItemRepository.save(existing);
    }

    public LostItem updateStatus(Long itemId, String status, UserContext context) {
        if (!StringUtils.hasText(status)) {
            throw new IllegalArgumentException("Status is required");
        }
        LostItem existing = lostItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));
        authService.ensureOwnershipOrAdmin(context, existing.getUserId());
        existing.setStatus(status);
        return lostItemRepository.save(existing);
    }

    @Transactional
    public void deleteItem(Long itemId, UserContext context) {
        LostItem existing = lostItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));
        authService.ensureOwnershipOrAdmin(context, existing.getUserId());
        claimRepository.deleteByItemId(itemId);
        lostItemRepository.delete(existing);
    }
}
