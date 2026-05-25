package com.block.tenant;

/**
 * Thread-local holder for the current tenant (clinic) ID.
 * Populated by JwtAuthFilter from the JWT claim and cleared after each request.
 */
public final class TenantContext {

    private TenantContext() {}

    private static final ThreadLocal<Long> CURRENT_CLINIC = new ThreadLocal<>();

    public static void setClinicId(Long clinicId) {
        CURRENT_CLINIC.set(clinicId);
    }

    public static Long getClinicId() {
        return CURRENT_CLINIC.get();
    }

    public static void clear() {
        CURRENT_CLINIC.remove();
    }

    /** Throw if no tenant is set — use in service methods that require a tenant */
    public static Long requireClinicId() {
        Long id = CURRENT_CLINIC.get();
        if (id == null) {
            throw new IllegalStateException("No tenant (clinic) in context — unauthenticated call?");
        }
        return id;
    }
}
