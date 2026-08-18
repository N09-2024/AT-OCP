package com.ocp.at.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public class SecurityUtils {

    /**
     * Get the ID (UUID) of the current user.
     *
     * @return the UUID of the current user.
     */
    public static Optional<String> getCurrentUtilisateurId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) principal;
            return Optional.ofNullable(userDetails.getId());
        } else if (principal instanceof UserDetails) {
            UserDetails springSecurityUser = (UserDetails) principal;
            return Optional.ofNullable(springSecurityUser.getUsername());
        } else if (principal instanceof String) {
            return Optional.of((String) principal);
        }
        return Optional.empty();
    }

    /**
     * Get the email/login of the current user.
     *
     * @return the email of the current user.
     */
    public static Optional<String> getCurrentUserLogin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            UserDetails springSecurityUser = (UserDetails) principal;
            return Optional.ofNullable(springSecurityUser.getUsername());
        } else if (principal instanceof String) {
            return Optional.of((String) principal);
        }
        return Optional.empty();
    }
}
