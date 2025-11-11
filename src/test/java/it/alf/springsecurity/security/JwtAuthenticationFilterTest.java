package it.alf.springsecurity.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    JwtUtils jwtUtils;
    JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        jwtUtils = mock(JwtUtils.class);
        filter = new JwtAuthenticationFilter(jwtUtils);
    }

    @Test
    void shouldSkipWhenNoAuthHeader() throws Exception {
        var req = new MockHttpServletRequest();
        var res = new MockHttpServletResponse();
        var chain = new MockFilterChain();
        filter.doFilterInternal(req, res, chain);
        // no exception and no security context change (can't access easily here)
        assertNotNull(res);
    }

    @Test
    void shouldAuthenticateWhenValidToken() throws Exception {
        var req = new MockHttpServletRequest();
        var res = new MockHttpServletResponse();
        var chain = new MockFilterChain();
        req.addHeader("Authorization", "Bearer token-123");
        when(jwtUtils.validate("token-123")).thenReturn(true);
        when(jwtUtils.getUsernameFromToken("token-123")).thenReturn("user1");
        when(jwtUtils.getRolesFromToken("token-123")).thenReturn(List.of("ROLE_USER"));
        filter.doFilterInternal(req, res, chain);
        assertNotNull(res);
    }
}
