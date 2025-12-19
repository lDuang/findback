package com.lostfound.backend.dto;

public class StatsResponse {

    private long totalItems;
    private long openItems;
    private long claimedItems;
    private long totalClaims;
    private long pendingClaims;
    private long approvedClaims;
    private long totalUsers;

    public long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }

    public long getOpenItems() {
        return openItems;
    }

    public void setOpenItems(long openItems) {
        this.openItems = openItems;
    }

    public long getClaimedItems() {
        return claimedItems;
    }

    public void setClaimedItems(long claimedItems) {
        this.claimedItems = claimedItems;
    }

    public long getTotalClaims() {
        return totalClaims;
    }

    public void setTotalClaims(long totalClaims) {
        this.totalClaims = totalClaims;
    }

    public long getPendingClaims() {
        return pendingClaims;
    }

    public void setPendingClaims(long pendingClaims) {
        this.pendingClaims = pendingClaims;
    }

    public long getApprovedClaims() {
        return approvedClaims;
    }

    public void setApprovedClaims(long approvedClaims) {
        this.approvedClaims = approvedClaims;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }
}
