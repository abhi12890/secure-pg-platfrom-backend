package com.securepg.entity;

public enum ConcernCategory {
    PLUMBING,
    ELECTRICITY,
    WIFI,
    SECURITY,
    HOUSEKEEPING;

    public ConcernPriority priority() {
        return switch (this) {
            case SECURITY -> ConcernPriority.CRITICAL;
            case ELECTRICITY, PLUMBING -> ConcernPriority.HIGH;
            case WIFI -> ConcernPriority.MEDIUM;
            case HOUSEKEEPING -> ConcernPriority.LOW;
        };
    }
}
