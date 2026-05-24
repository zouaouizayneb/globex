package tn.fst.backend.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        
        // Log to identify why /api/orders might be failing
        if (request.getRequestURI().contains("/api/orders")) {
            System.out.println("DEBUG: JwtAuthenticationFilter - Request to " + request.getRequestURI());
            System.out.println("DEBUG: Authorization Header: " + (authHeader != null ? "Present" : "Missing"));
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            String username = jwtUtil.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtUtil.validateToken(jwt, userDetails)) {
                    if (request.getRequestURI().contains("/api/orders")) {
                        System.out.println("DEBUG: Token valid for user: " + username);
                        System.out.println("DEBUG: User enabled: " + userDetails.isEnabled());
                        System.out.println("DEBUG: User account non-locked: " + userDetails.isAccountNonLocked());
                    }

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    if (request.getRequestURI().contains("/api/orders")) {
                        System.out.println("DEBUG: SecurityContext populated for user: " + username);
                    }
                } else {
                    if (request.getRequestURI().contains("/api/orders")) {
                        System.out.println("DEBUG: Token validation failed (expired or mismatched) for user: " + username);
                    }
                }
            }
        } catch (Exception e) {
            if (request.getRequestURI().contains("/api/orders")) {
                System.out.println("DEBUG: Authentication error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                e.printStackTrace();
            }
            SecurityContextHolder.clearContext();
        }
        
        filterChain.doFilter(request, response);
    }
}
