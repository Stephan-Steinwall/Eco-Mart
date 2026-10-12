package com.cybrixx.ecomartbackend;

import com.cybrixx.ecomartbackend.config.security.JwtAuthFilter;
import com.cybrixx.ecomartbackend.config.security.JwtUtils;
import com.cybrixx.ecomartbackend.service.security.CustomUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** A stale token must never turn a request (least of all the login request) into a 500. */
class JwtAuthFilterTests {

    private final JwtUtils jwtUtils = mock(JwtUtils.class);
    private final CustomUserDetailsService users = mock(CustomUserDetailsService.class);
    private final JwtAuthFilter filter = new JwtAuthFilter(jwtUtils, users);
    private final FilterChain chain = mock(FilterChain.class);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private MockHttpServletRequest requestWithToken(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }

    @Test
    void expiredTokenIsIgnoredAndTheRequestStillContinues() throws Exception {
        when(jwtUtils.extractUsername("old")).thenThrow(new ExpiredJwtException(null, null, "JWT expired"));
        MockHttpServletRequest request = requestWithToken("old");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertDoesNotThrow(() -> filter.doFilter(request, response, chain));

        verify(chain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication(), "stale token must not authenticate");
    }

    @Test
    void malformedTokenIsIgnoredToo() throws Exception {
        when(jwtUtils.extractUsername("junk")).thenThrow(new MalformedJwtException("Malformed JWT"));
        MockHttpServletRequest request = requestWithToken("junk");

        assertDoesNotThrow(() -> filter.doFilter(request, new MockHttpServletResponse(), chain));

        verify(chain).doFilter(eq(request), any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void tokenOfADeletedUserIsIgnored() throws Exception {
        when(jwtUtils.extractUsername("ghost")).thenReturn("gone@example.com");
        when(users.loadUserByUsername("gone@example.com")).thenThrow(new UsernameNotFoundException("gone"));
        MockHttpServletRequest request = requestWithToken("ghost");

        assertDoesNotThrow(() -> filter.doFilter(request, new MockHttpServletResponse(), chain));

        verify(chain).doFilter(eq(request), any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void validTokenStillAuthenticates() throws Exception {
        UserDetails details = new User("admin@example.com", "pw", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(jwtUtils.extractUsername("good")).thenReturn("admin@example.com");
        when(users.loadUserByUsername("admin@example.com")).thenReturn(details);
        when(jwtUtils.validateToken("good", details)).thenReturn(true);
        MockHttpServletRequest request = requestWithToken("good");

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        verify(chain).doFilter(eq(request), any());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("admin@example.com", SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
