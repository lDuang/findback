package com.lostfound.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lostfound.backend.entity.Announcement;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
}
