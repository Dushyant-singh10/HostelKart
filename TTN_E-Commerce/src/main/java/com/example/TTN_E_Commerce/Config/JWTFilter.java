package com.example.TTN_E_Commerce.Config;

import com.example.TTN_E_Commerce.Entity.User;
import com.example.TTN_E_Commerce.Enum.RoleType;
import com.example.TTN_E_Commerce.Repository.UserRepository;
import com.example.TTN_E_Commerce.Service.Impl.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JWTFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if (path.startsWith("/customer/register") ||
                path.startsWith("/customer/login") ||
                path.startsWith("/seller/register") ||
                path.startsWith("/seller/login") ||
                path.startsWith("/admin/login") ||
                path.startsWith("/activate") ||
                path.startsWith("/resendToken") ||
                path.startsWith("/reset-passowrd") ||
                path.startsWith("/forgot-password") ||
                path.startsWith("/user/refreshAccessToken")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = jwtService.extractTokenFromRequest(request);

        if (token != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            try {

                Claims claims = jwtService.validateAccessToken(token);
                String email = claims.getSubject();

                List<String> roles = claims.get("roles", List.class);

                if (roles == null || roles.isEmpty()) {
                    writeError(response,
                            HttpServletResponse.SC_UNAUTHORIZED,
                            "No role found in token");
                    return;
                }

                String roleName = roles.get(0).replace("ROLE_", "");

                RoleType roleType;

                try {
                    roleType = RoleType.valueOf(roleName);
                } catch (IllegalArgumentException ex) {
                    writeError(response,
                            HttpServletResponse.SC_UNAUTHORIZED,
                            "Invalid role in token");
                    return;
                }

                User user = userRepository
                        .findByEmailAndRole(email, roleType)
                        .orElse(null);

                if (user == null) {
                    writeError(response,
                            HttpServletResponse.SC_UNAUTHORIZED,
                            "User not found");
                    return;
                }

                if (!user.isActive()) {
                    writeError(response,
                            HttpServletResponse.SC_UNAUTHORIZED,
                            "Account is not activated");
                    return;
                }

                if (user.isLocked()) {
                    writeError(response,
                            HttpServletResponse.SC_UNAUTHORIZED,
                            "Account is locked");
                    return;
                }

                if (user.getRefreshToken() == null) {
                    writeError(response,
                            HttpServletResponse.SC_UNAUTHORIZED,
                            "Session expired. Please login again");
                    return;
                }

                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                authorities
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder.getContext()
                        .setAuthentication(authToken);

            } catch (ExpiredJwtException e) {

                writeError(response,
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Access token expired");
                return;

            } catch (Exception e) {

                writeError(response,
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Invalid token");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void writeError(HttpServletResponse response,
                            int status,
                            String message) throws IOException {

        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"error\": \"" + message + "\"}"
        );
    }
}