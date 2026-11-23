package main.java.model;

import java.util.regex.Pattern;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Admin extends User implements Login {

    private final IntegerProperty adminId = new SimpleIntegerProperty();
    private final StringProperty username = new SimpleStringProperty();

    public Admin(int id, String nama, String password, String role, int adminId, String username) {
        super(id, nama, password, role);
        this.adminId.set(adminId);
        this.username.set(username);
    }

    @Override
    public String showMenu() {
        return "/main/resources/admin_dashboard_content.fxml";
    }


    @Override
    public void signUp() {

    }

    @Override
    public void signIn() {
       
    }

    public boolean validate() {
        final String USERNAME_REGEX =
            "^[a-zA-Z][a-zA-Z0-9._]{3,19}$";
        final String PASSWORD_REGEX = 
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&\\-+=()])(?=\\S+$).{8,20}$";

        final Pattern USERNAME_PATTERN = Pattern.compile(USERNAME_REGEX);
        final Pattern PATTERN = Pattern.compile(PASSWORD_REGEX);
        if (getUsername() == null || getPassword() == null) {
            return false;
        }
        return USERNAME_PATTERN.matcher(getUsername()).matches() && 
                PATTERN.matcher(getPassword()).matches();
    }

    // Property Methods
    public IntegerProperty adminIdProperty() { return adminId; }
    public StringProperty usernameProperty() { return username; }

    // Setter Getter 
    public int getAdminId() { return adminId.get(); }
    public void setAdminId(int adminId) { this.adminId.set(adminId); }

    public String getUsername() { return username.get(); }
    public void setUsername(String username) { this.username.set(username); }
}