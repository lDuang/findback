package com.lostfound.backend.controller;

import com.lostfound.backend.entity.Banner;
import com.lostfound.backend.model.UserContext;
import com.lostfound.backend.service.AuthService;
import com.lostfound.backend.service.BannerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/banners")
public class AdminBannerController {

    private final BannerService bannerService;
    private final AuthService authService;

    public AdminBannerController(BannerService bannerService, AuthService authService) {
        this.bannerService = bannerService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<List<Banner>> list(HttpServletRequest servletRequest) {
        UserContext context = authService.extractContext(
                servletRequest.getHeader("X-User-Id"),
                servletRequest.getHeader("X-User-Role"));
        authService.ensureAdmin(context);
        return ResponseEntity.ok(bannerService.listAll());
    }

    @PostMapping
    public ResponseEntity<Banner> create(@RequestBody Banner banner, HttpServletRequest servletRequest) {
        UserContext context = authService.extractContext(
                servletRequest.getHeader("X-User-Id"),
                servletRequest.getHeader("X-User-Role"));
        authService.ensureAdmin(context);
        return ResponseEntity.status(HttpStatus.CREATED).body(bannerService.create(banner, context));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Banner> update(@PathVariable Long id,
                                         @RequestBody Banner banner,
                                         HttpServletRequest servletRequest) {
        UserContext context = authService.extractContext(
                servletRequest.getHeader("X-User-Id"),
                servletRequest.getHeader("X-User-Role"));
        authService.ensureAdmin(context);
        return ResponseEntity.ok(bannerService.update(id, banner, context));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpServletRequest servletRequest) {
        UserContext context = authService.extractContext(
                servletRequest.getHeader("X-User-Id"),
                servletRequest.getHeader("X-User-Role"));
        authService.ensureAdmin(context);
        bannerService.delete(id, context);
        return ResponseEntity.noContent().build();
    }
}
