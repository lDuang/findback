package com.lostfound.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lostfound.backend.entity.Banner;

public interface BannerRepository extends JpaRepository<Banner, Long> {
    java.util.List<Banner> findAllByOrderByCreatedAtDesc();
}
