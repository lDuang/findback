package com.lostfound.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lostfound.backend.entity.LostItem;

public interface LostItemRepository extends JpaRepository<LostItem, Long> {

    List<LostItem> findByUserId(Long userId);

    List<LostItem> findByStatus(String status);

    List<LostItem> findByUserIdAndStatus(Long userId, String status);

    long countByStatus(String status);
}
