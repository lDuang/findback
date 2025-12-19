package com.lostfound.backend.service;

import com.lostfound.backend.dto.StatsResponse;
import com.lostfound.backend.repository.ClaimRepository;
import com.lostfound.backend.repository.LostItemRepository;
import com.lostfound.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class StatsService {

    private final LostItemRepository lostItemRepository;
    private final ClaimRepository claimRepository;
    private final UserRepository userRepository;

    public StatsService(LostItemRepository lostItemRepository,
                        ClaimRepository claimRepository,
                        UserRepository userRepository) {
        this.lostItemRepository = lostItemRepository;
        this.claimRepository = claimRepository;
        this.userRepository = userRepository;
    }

    public StatsResponse overview() {
        StatsResponse response = new StatsResponse();
        response.setTotalItems(lostItemRepository.count());
        response.setOpenItems(lostItemRepository.countByStatus("OPEN"));
        long claimed = lostItemRepository.countByStatus("CLAIMED")
                + lostItemRepository.countByStatus("RESOLVED")
                + lostItemRepository.countByStatus("CLOSED");
        response.setClaimedItems(claimed);
        response.setTotalClaims(claimRepository.count());
        response.setPendingClaims(claimRepository.countByStatus("PENDING"));
        response.setApprovedClaims(claimRepository.countByStatus("APPROVED"));
        response.setTotalUsers(userRepository.count());
        return response;
    }
}
