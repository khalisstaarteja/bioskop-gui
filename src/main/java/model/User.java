package main.java.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public abstract class User {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty nama = new SimpleStringProperty();
    private final StringProperty password = new SimpleStringProperty();
    private final StringProperty role = new SimpleStringProperty();

    // Constructor 1: Lengkap dari Database / TableView
    public User(int id, String nama, String password, String role) {
        this.id.set(id);
        this.nama.set(nama);
        this.password.set(password);
        this.role.set(role);
    }

    // Constructor 2: Tanpa ID (untuk data baru sebelum disimpan ke DB)
    public User(String nama, String password, String role) {
        this.nama.set(nama);
        this.password.set(password);
        this.role.set(role);
    }

    public abstract String showMenu();

    // Property Methods
    public IntegerProperty idProperty() { return id; }
    public StringProperty namaProperty() { return nama; }
    public StringProperty passwordProperty() { return password; }
    public StringProperty roleProperty() { return role; }

    // Getters & Setters
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }

    public String getNama() { return nama.get(); }
    public void setNama(String nama) { this.nama.set(nama); }

    public String getPassword() { return password.get(); }
    public void setPassword(String password) { this.password.set(password); }

    public String getRole() { return role.get(); }
    public void setRole(String role) { this.role.set(role); }
}