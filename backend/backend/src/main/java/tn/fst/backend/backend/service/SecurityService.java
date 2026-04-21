package tn.fst.backend.backend.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tn.fst.backend.backend.entity.User;

@Service("securityService")
public class SecurityService {

    /**
     * Returns true if the current authenticated user's ID matches the given id.
     * Used for @PreAuthorize so users can access their own profile.
     */
    public boolean isCurrentUser(Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            return false;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof User user) {
            return user.getIdUser() != null && user.getIdUser().equals(id);
        }
        return false;
    }
}
