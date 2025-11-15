package vn.edu.uth.quanlidaythem.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthTokenFilter extends OncePerRequestFilter {
    
    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthTokenFilter.class);
    
    public JwtAuthTokenFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);

            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                Collection<? extends GrantedAuthority> authorities = getAuthoritiesFromJwtToken(jwt);
                
                logger.debug("JWT Authentication for user: {} with authorities: {}", username, authorities);

                UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token expired: {}", e.getMessage());
            // Không set authentication, request sẽ bị từ chối
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
    
    // ✅ PHƯƠNG THỨC QUAN TRỌNG: Lấy authorities từ JWT token
    private Collection<? extends GrantedAuthority> getAuthoritiesFromJwtToken(String token) {
        try {
            // Parse token để lấy claims
            Claims claims = jwtUtils.getAllClaimsFromToken(token);
            
            List<GrantedAuthority> authorities = new ArrayList<>();
            
            // Lấy roles từ claim "roles" trong token
            @SuppressWarnings("unchecked")
            List<String> roles = claims.get("roles", List.class);
            
            if (roles != null) {
                for (String role : roles) {
                    // Đảm bảo role có prefix ROLE_ 
                    String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                    authorities.add(new SimpleGrantedAuthority(authority));
                    System.out.println("✅ Added authority: " + authority);
                }
            } else {
                System.err.println("⚠️ No roles found in JWT token");
            }
            
            return authorities;
            
        } catch (Exception e) {
            System.err.println("❌ Error extracting authorities from JWT: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}