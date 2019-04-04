package com.example.scriptengine.security;

import com.example.scriptengine.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFacade implements IAuthenticationFacade {

    @Override
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public User getUser() {
        Authentication authentication = getAuthentication();
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(authority -> new User(authentication.getName(), authority.getAuthority()))
                .orElseGet(() -> new User(authentication.getName(), "ROLE_UNKNOWN"));
    }
}

