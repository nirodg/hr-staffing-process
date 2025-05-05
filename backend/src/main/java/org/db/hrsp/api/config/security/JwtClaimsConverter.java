package org.db.hrsp.api.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtClaimsConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String CONFIDENTIAL_CLIENT_NAME;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        var authorities = extractRealmRoles(jwt);
        return new JwtAuthenticationToken(jwt, authorities, getPrincipalFromClaim(jwt));
    }

    private String getPrincipalFromClaim(Jwt jwt) {
        var claimName = "preferred_username";
        return jwt.getClaim(claimName);
    }

    // Should we keep this?
    private Collection<GrantedAuthority> extractRealmRoles(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Extract client roles
        Map<String, Object> resourceAccess = (Map<String, Object>) jwt.getClaims().get("resource_access");
        if (resourceAccess != null) {
            Map<String, Object> client = (Map<String, Object>) resourceAccess.get(CONFIDENTIAL_CLIENT_NAME);
            if (client != null && client.containsKey("roles")) {
                List<String> clientRoles = (List<String>) client.get("roles");
                authorities.addAll(clientRoles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        .collect(Collectors.toList()));
            }
        }

        return authorities;
    }
}