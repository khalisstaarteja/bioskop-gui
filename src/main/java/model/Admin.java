package main.java.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Admin extends User implements Login {
    
    private final IntegerProperty adminId = new SimpleIntegerProperty();
    private final StringProperty username = new SimpleStringProperty();
    private boolean loginStatus = false;

    public Admin(int id, String nama, String password, String role, int adminId, String username) {
        super(id, nama, password, role);
        this.adminId.set(adminId);
        this.username.set(username);
    }

    public Admin(String nama, String password, String username) {
        super(nama, password, "Admin"); 
        this.username.set(username);
    }
    
    public boolean isLoginBerhasil() {
        return loginStatus;
    }

    @Override
    public String showMenu() {
        return "/main/resources/sidebar_admin.fxml";
    }

    @Override
    public void signUp() {
        System.out.println("Admin berhasil mendaftar.");
    }

    @Override
    public void signIn() {
        System.out.println("\n[Admin berhasil masuk!]");
        this.loginStatus = true; 
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