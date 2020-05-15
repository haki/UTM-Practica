package md.utm.universty.dto;

import md.utm.universty.model.UserRole;

public class SendRegisterMail {
    private String email;
    private int userRole;

    public String getEmail() {
        return email;
    }

    public int getUserRole() {
        return userRole;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUserRole(int userRole) {
        this.userRole = userRole;
    }
}
