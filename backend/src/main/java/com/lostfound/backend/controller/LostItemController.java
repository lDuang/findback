package com.lostfound.backend.controller;

import com.lostfound.backend.dto.LostItemRequest;
import com.lostfound.backend.dto.StatusUpdateRequest;
import com.lostfound.backend.entity.Claim;
import com.lostfound.backend.entity.LostItem;
import com.lostfound.backend.model.UserContext;
import com.lostfound.backend.service.AuthService;
import com.lostfound.backend.service.ClaimService;
import com.lostfound.backend.service.LostItemService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/items")
public class LostItemController {

    private final LostItemService lostItemService;
    private final AuthService authService;
    private final ClaimService claimService;

    public LostItemController(LostItemService lostItemService, AuthService authService, ClaimService claimService) {
        this.lostItemService = lostItemService;
        this.authService = authService;
        this.claimService = claimService;
    }

    @GetMapping
    public ResponseEntity<List<LostItem>> listAll(@RequestParam(value = "userId", required = false) Long userId,
                                                  @RequestParam(value = "status", required = false) String status,
                                                  HttpServletRequest servletRequest) {
        UserContext context = authService.extractOptionalContext(
                servletRequest.getHeader("X-User-Id"), servletRequest.getHeader("X-User-Role"));
        Long effectiveUserId = userId;
        if (context != null && !context.isAdmin()) {
            if (userId != null && !userId.equals(context.getUserId())) {
                throw new SecurityException("普通用户只能查看自己的物品");
            }
            effectiveUserId = context.getUserId();
        }
        List<LostItem> items = lostItemService.listFiltered(effectiveUserId, status);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LostItem> detail(@PathVariable("id") Long id) {
        return ResponseEntity.ok(lostItemService.findById(id));
    }

    @GetMapping("/{id}/claims")
    public ResponseEntity<List<Claim>> claimsForItem(@PathVariable("id") Long id, HttpServletRequest servletRequest) {
        LostItem item = lostItemService.findById(id);
        UserContext context = authService.extractOptionalContext(
                servletRequest.getHeader("X-User-Id"), servletRequest.getHeader("X-User-Role"));
        if (context == null) {
            throw new SecurityException("请登录后查看认领记录");
        }
        if (context.isAdmin() || item.getUserId().equals(context.getUserId())) {
            return ResponseEntity.ok(claimService.listByItem(id));
        }
        return ResponseEntity.ok(claimService.listByUserAndItem(context.getUserId(), id));
    }

    @PostMapping
    public ResponseEntity<LostItem> create(@RequestBody LostItemRequest request, HttpServletRequest servletRequest) {
        UserContext context = authService.extractContext(servletRequest.getHeader("X-User-Id"), servletRequest.getHeader("X-User-Role"));
        LostItem item = new LostItem();
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setStatus(request.getStatus());
        item.setLocation(request.getLocation());
        item.setImageUrl(request.getImageUrl());
        return ResponseEntity.ok(lostItemService.createItem(item, context));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LostItem> update(@PathVariable("id") Long id,
                                           @RequestBody LostItemRequest request,
                                           HttpServletRequest servletRequest) {
        UserContext context = authService.extractContext(servletRequest.getHeader("X-User-Id"), servletRequest.getHeader("X-User-Role"));
        LostItem updated = new LostItem();
        updated.setTitle(request.getTitle());
        updated.setDescription(request.getDescription());
        updated.setStatus(request.getStatus());
        updated.setLocation(request.getLocation());
        updated.setImageUrl(request.getImageUrl());
        return ResponseEntity.ok(lostItemService.updateItem(id, updated, context));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<LostItem> updateStatus(@PathVariable("id") Long id,
                                                 @RequestBody StatusUpdateRequest request,
                                                 HttpServletRequest servletRequest) {
        UserContext context = authService.extractContext(servletRequest.getHeader("X-User-Id"), servletRequest.getHeader("X-User-Role"));
        return ResponseEntity.ok(lostItemService.updateStatus(id, request.getStatus(), context));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id, HttpServletRequest servletRequest) {
        UserContext context = authService.extractContext(servletRequest.getHeader("X-User-Id"), servletRequest.getHeader("X-User-Role"));
        lostItemService.deleteItem(id, context);
        return ResponseEntity.noContent().build();
    }
}
