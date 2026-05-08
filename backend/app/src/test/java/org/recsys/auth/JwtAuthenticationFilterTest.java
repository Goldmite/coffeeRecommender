package org.recsys.auth;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.recsys.config.jwt.JwtAuthenticationFilter;
import org.recsys.config.jwt.JwtUtils;
import org.recsys.repository.UserRepository;
import org.recsys.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private FilterChain filterChain;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private JwtAuthenticationFilter jwtFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSetAuthContext_WhenTokenIsValid() throws ServletException, IOException {
        // given
        String token = "valid.jwt.token";
        String email = "test@example.com";

        UserDetails mockUserDetails = org.springframework.security.core.userdetails.User.builder()
                .username(email)
                .password("protected")
                .authorities("ROLE_USER")
                .build();

        // 1. Setup Header
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        // 2. Setup JWT Utils
        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(jwtUtils.getEmailFromToken(token)).thenReturn(email);

        // 3. Setup Service - THIS must match the field name in JwtAuthenticationFilter
        when(userDetailsService.loadUserByUsername(email)).thenReturn(mockUserDetails);

        // when
        jwtFilter.doFilter(request, response, filterChain);

        // then
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth, "Authentication should not be null");
        assertEquals(email, auth.getName());
        verify(filterChain).doFilter(request, response);
    }
}