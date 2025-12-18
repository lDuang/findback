package com.lostfound.backend.controller;

import com.lostfound.backend.dto.AnnouncementRequest;
import com.lostfound.backend.entity.Announcement;
import com.lostfound.backend.exception.NotFoundException;
import com.lostfound.backend.model.UserContext;
import com.lostfound.backend.repository.AnnouncementRepository;
import com.lostfound.backend.service.AnnouncementService;
import com.lostfound.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final AnnouncementRepository announcementRepository;
    private final AuthService authService;

    public AnnouncementController(AnnouncementService announcementService,
                                  AnnouncementRepository announcementRepository,
                                  AuthService authService) {
        this.announcementService = announcementService;
        this.announcementRepository = announcementRepository;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<List<Announcement>> list() {
        return ResponseEntity.ok(announcementService.listAll());
    }

    @PostMapping
    public ResponseEntity<Announcement> create(@RequestBody AnnouncementRequest request, HttpServletRequest servletRequest) {
        UserContext context = authService.extractContext(servletRequest.getHeader("X-User-Id"), servletRequest.getHeader("X-User-Role"));
        authService.ensureAdmin(context);
        Announcement announcement = new Announcement();
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).body(announcementService.create(announcement, context));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpServletRequest servletRequest) {
        UserContext context = authService.extractContext(servletRequest.getHeader("X-User-Id"), servletRequest.getHeader("X-User-Role"));
        authService.ensureAdmin(context);
        if (!announcementRepository.existsById(id)) {
            throw new NotFoundException("Announcement not found");
        }
        announcementRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
