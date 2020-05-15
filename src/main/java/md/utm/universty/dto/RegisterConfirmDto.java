package md.utm.universty.dto;

public class RegisterConfirmDto {
    private String token;
    private String name;
    private String surname;
    private String password;

    public String getSurname() {
        return surname;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public String getPassword() {
        return password;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
