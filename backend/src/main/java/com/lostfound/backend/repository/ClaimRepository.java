package com.lostfound.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lostfound.backend.entity.Claim;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    List<Claim> findByUserId(Long userId);

    List<Claim> findByItemId(Long itemId);

    List<Claim> findByUserIdAndItemId(Long userId, Long itemId);

    void deleteByItemId(Long itemId);
}
