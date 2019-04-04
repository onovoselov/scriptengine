package com.example.scriptengine.model;

import javax.security.auth.Subject;
import java.security.Principal;

public class User implements Principal {
    private String userName;
    private String roleName;

    public User(String userName, String roleName) {
        this.userName = userName;
        this.roleName = roleName;
    }

    public String getUserName() {
        return userName;
    }

    public String getRoleName() {
        return roleName;
    }

    public boolean isAdmin() {
        return "ROLE_ADMIN".equals(roleName);
    }

    @Override
    public String getName() {
        return userName;
    }

    @Override
    public boolean implies(Subject subject) {
        return false;
    }
}
