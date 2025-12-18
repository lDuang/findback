package com.lostfound.backend.service;

import com.lostfound.backend.entity.Banner;
import com.lostfound.backend.model.UserContext;
import com.lostfound.backend.repository.BannerRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class BannerService {

    private final BannerRepository bannerRepository;
    private final AuthService authService;

    public BannerService(BannerRepository bannerRepository, AuthService authService) {
        this.bannerRepository = bannerRepository;
        this.authService = authService;
    }

    public List<Banner> listAll() {
        return bannerRepository.findAll();
    }

    public Banner create(Banner banner, UserContext context) {
        authService.ensureAdmin(context);
        if (!StringUtils.hasText(banner.getTitle())) {
            throw new IllegalArgumentException("Title is required");
        }
        return bannerRepository.save(banner);
    }
}
