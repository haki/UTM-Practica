package md.utm.universty.model;

import org.springframework.security.core.GrantedAuthority;

public enum UserRole implements GrantedAuthority {
    Admin, Professor, Student, User;

    @Override
    public String getAuthority() {
        return name();
    }
}
