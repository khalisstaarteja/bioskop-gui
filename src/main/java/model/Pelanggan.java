package main.java.model;

import java.util.ArrayList;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Pelanggan extends User implements Login {
    
    private final IntegerProperty pelangganId = new SimpleIntegerProperty();
    private final StringProperty email = new SimpleStringProperty();
    private ArrayList<Pemesanan> listPemesanan = new ArrayList<>();

    public Pelanggan(int id, String nama, String password, String role, int pelangganId, String email) {
        super(id, nama, password, role);
        this.pelangganId.set(pelangganId);
        this.email.set(email);
    }
    
    @Override
    public String showMenu() {
        return "/main/resources/pelanggan_dashboard_content.fxml";
    }


    @Override
    public void signUp() {
       
    }

    @Override
    public void signIn() {
        
    }

    // Memasukkan satu object Pemesanan ke dalam daftar pemesanan milik object ini
    public void addPemesanan(Pemesanan p) {
        this.listPemesanan.add(p);
    }

    // Property Methods
    public IntegerProperty pelangganIdProperty() { return pelangganId; }
    public StringProperty emailProperty() { return email; }

    // Setter Getter 
    public int getPelangganId() { return pelangganId.get(); }
    public void setPelangganId(int pelangganId) { this.pelangganId.set(pelangganId); }

    public String getEmail() { return email.get(); }
    public void setEmail(String email) { this.email.set(email); }

    public ArrayList<Pemesanan> getListPemesanan() {
        return listPemesanan;
    }
    public void setListPemesanan(ArrayList<Pemesanan> listPemesanan) {
        this.listPemesanan = listPemesanan;
    }
}