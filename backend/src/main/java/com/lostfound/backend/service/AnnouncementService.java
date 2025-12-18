package com.lostfound.backend.service;

import com.lostfound.backend.entity.Announcement;
import com.lostfound.backend.model.UserContext;
import com.lostfound.backend.repository.AnnouncementRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final AuthService authService;

    public AnnouncementService(AnnouncementRepository announcementRepository, AuthService authService) {
        this.announcementRepository = announcementRepository;
        this.authService = authService;
    }

    public List<Announcement> listAll() {
        return announcementRepository.findAll();
    }

    public Announcement create(Announcement announcement, UserContext context) {
        authService.ensureAdmin(context);
        if (!StringUtils.hasText(announcement.getTitle()) || !StringUtils.hasText(announcement.getContent())) {
            throw new IllegalArgumentException("Title and content are required");
        }
        return announcementRepository.save(announcement);
    }
}
