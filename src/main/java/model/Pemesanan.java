package main.java.model;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Pemesanan implements Tiket {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty nomorPemesanan = new SimpleIntegerProperty();
    
    // Objek relasi
    private ObjectProperty<JadwalFilm> jadwal = new SimpleObjectProperty<>();
    private ObjectProperty<User> user = new SimpleObjectProperty<>();
    
    private final IntegerProperty jumlahPemesanan = new SimpleIntegerProperty();
    private final StringProperty metodePembayaran = new SimpleStringProperty();
    private final FloatProperty totalBayar = new SimpleFloatProperty();

    public Pemesanan(int id, int nomorPemesanan, JadwalFilm jadwal, int jumlahPemesanan, String metodePembayaran, float totalBayar, User user) {
        this.id.set(id);
        this.nomorPemesanan.set(nomorPemesanan);
        this.jadwal.set(jadwal);
        this.jumlahPemesanan.set(jumlahPemesanan);
        this.metodePembayaran.set(metodePembayaran);
        this.totalBayar.set(totalBayar);
        this.user.set(user);
    }
    
    // Property Methods
    public IntegerProperty idProperty() { return id; }
    public IntegerProperty nomorPemesananProperty() { return nomorPemesanan; }
    public ObjectProperty<JadwalFilm> jadwalProperty() { return jadwal; }
    public ObjectProperty<User> userProperty() { return user; }
    public IntegerProperty jumlahPemesananProperty() { return jumlahPemesanan; }
    public StringProperty metodePembayaranProperty() { return metodePembayaran; }
    public FloatProperty totalBayarProperty() { return totalBayar; }

    // Getters & Setters
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }

    public int getNomorPemesanan() { return nomorPemesanan.get(); }
    public void setNomorPemesanan(int nomorPemesanan) { this.nomorPemesanan.set(nomorPemesanan); }

    public JadwalFilm getJadwal() { return jadwal.get(); }
    public void setJadwal(JadwalFilm jadwal) { this.jadwal.set(jadwal); }

    public int getJumlahPemesanan() { return jumlahPemesanan.get(); }
    public void setJumlahPemesanan(int jumlahPemesanan) { this.jumlahPemesanan.set(jumlahPemesanan); }

    public String getMetodePembayaran() { return metodePembayaran.get(); }
    public void setMetodePembayaran(String metodePembayaran) { this.metodePembayaran.set(metodePembayaran); }

    public float getTotalBayar() { return totalBayar.get(); }
    public void setTotalBayar(float totalBayar) { this.totalBayar.set(totalBayar); }

    public User getUser() { return user.get(); }
    public void setUser(User user) { this.user.set(user); }

    @Override
    public void cetakTiket() {
        System.out.println("\n========== TIKET PEMESANAN ==========");
        System.out.println("Nomor Pemesanan     : " + getNomorPemesanan());
        System.out.println("Film                : " + getJadwal().getFilm().getJudul());
        System.out.println("Tanggal             : " + getJadwal().getTanggal());
        System.out.println("Jam Tayang          : " + getJadwal().getJam());
        System.out.println("Bioskop             : " + getJadwal().getStudio().getBioskop().getNamaBioskop());
        System.out.println("Studio              : " + getJadwal().getStudio().getNomorStudio());
        System.out.println("Jumlah Tiket        : " + getJumlahPemesanan());
        System.out.println("Metode Bayar        : " + getMetodePembayaran());
        System.out.println("Total Bayar         : Rp " + String.format("%.0f", getTotalBayar()));
        System.out.println("=====================================");
    }
}