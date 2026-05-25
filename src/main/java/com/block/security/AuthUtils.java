package com.block.security;

/**
 * Helpers for resolving doctor-scoped filters from the authenticated principal.
 *
 * Multi-doctor clinics auto-scope DOCTOR-role users to their own data.
 * ADMIN/RECEPTION get an optional doctorId filter — null means all doctors.
 *
 * DOCTOR_ADMIN is a hybrid: clinical (sees own data by default) AND managerial
 * (can override via doctorId, can edit anyone, has admin URL access).
 */
public final class AuthUtils {

    public static final String ROLE_DOCTOR        = "DOCTOR";
    public static final String ROLE_ADMIN         = "ADMIN";
    public static final String ROLE_RECEPTION     = "RECEPTION";
    public static final String ROLE_DOCTOR_ADMIN  = "DOCTOR_ADMIN";

    private AuthUtils() {}

    /**
     * Returns the doctorId that should be used to scope a query.
     * <ul>
     *   <li>DOCTOR → always own userId (request ignored)</li>
     *   <li>DOCTOR_ADMIN → requested if provided, else own (default own, allow override)</li>
     *   <li>ADMIN/RECEPTION → requestedDoctorId passthrough (null = all)</li>
     * </ul>
     */
    public static Long getEffectiveDoctorId(UserPrincipal principal, Long requestedDoctorId) {
        if (principal == null) return requestedDoctorId;
        String role = principal.getRole();
        if (ROLE_DOCTOR.equals(role)) {
            return principal.getUserId();
        }
        if (ROLE_DOCTOR_ADMIN.equals(role)) {
            return requestedDoctorId != null ? requestedDoctorId : principal.getUserId();
        }
        return requestedDoctorId;
    }

    /** True for any clinical role (DOCTOR or DOCTOR_ADMIN). */
    public static boolean isDoctor(UserPrincipal principal) {
        if (principal == null) return false;
        String role = principal.getRole();
        return ROLE_DOCTOR.equals(role) || ROLE_DOCTOR_ADMIN.equals(role);
    }

    /** True only for the strict DOCTOR role (excludes DOCTOR_ADMIN). */
    public static boolean isStrictDoctor(UserPrincipal principal) {
        return principal != null && ROLE_DOCTOR.equals(principal.getRole());
    }

    /** True for any managerial role (ADMIN or DOCTOR_ADMIN). */
    public static boolean isAdmin(UserPrincipal principal) {
        if (principal == null) return false;
        String role = principal.getRole();
        return ROLE_ADMIN.equals(role) || ROLE_DOCTOR_ADMIN.equals(role);
    }

    /** True only for the strict ADMIN role (excludes DOCTOR_ADMIN). */
    public static boolean isStrictAdmin(UserPrincipal principal) {
        return principal != null && ROLE_ADMIN.equals(principal.getRole());
    }

    /** True only for the hybrid DOCTOR_ADMIN role. */
    public static boolean isDoctorAdmin(UserPrincipal principal) {
        return principal != null && ROLE_DOCTOR_ADMIN.equals(principal.getRole());
    }
}
