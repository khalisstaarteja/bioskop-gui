package main.java.model;

import java.util.ArrayList;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Studio {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty kodeStudio = new SimpleStringProperty();
    private final IntegerProperty nomorStudio = new SimpleIntegerProperty();
    private final StringProperty jenisStudio = new SimpleStringProperty();
    private final FloatProperty harga = new SimpleFloatProperty();
    
    // Objek relasi ke Bioskop
    private ObjectProperty<Bioskop> bioskop = new SimpleObjectProperty<>();
    private ArrayList<JadwalFilm> listJadwal = new ArrayList<>();

    public Studio(int id, String kodeStudio, int nomorStudio, String jenisStudio, Bioskop bioskop, float harga) {
        this.id.set(id);
        this.kodeStudio.set(kodeStudio);
        this.nomorStudio.set(nomorStudio);
        this.jenisStudio.set(jenisStudio);
        this.bioskop.set(bioskop);
        this.harga.set(harga);
        this.listJadwal = new ArrayList<>();
    }

    // Property Methods
    public IntegerProperty idProperty() { return id; }
    public StringProperty kodeStudioProperty() { return kodeStudio; }
    public IntegerProperty nomorStudioProperty() { return nomorStudio; }
    public StringProperty jenisStudioProperty() { return jenisStudio; }
    public FloatProperty hargaProperty() { return harga; }
    public ObjectProperty<Bioskop> bioskopProperty() { return bioskop; }

    // Getters & Setters
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }

    public String getKodeStudio() { return kodeStudio.get(); }
    public void setKodeStudio(String kodeStudio) { this.kodeStudio.set(kodeStudio); }

    public int getNomorStudio() { return nomorStudio.get(); }
    public void setNomorStudio(int nomorStudio) { this.nomorStudio.set(nomorStudio); }

    public String getJenisStudio() { return jenisStudio.get(); }
    public void setJenisStudio(String jenisStudio) { this.jenisStudio.set(jenisStudio); }

    public Bioskop getBioskop() { return bioskop.get(); }
    public void setBioskop(Bioskop bioskop) { this.bioskop.set(bioskop); }

    public float getHarga() { return harga.get(); }
    public void setHarga(float harga) { this.harga.set(harga); }

    public ArrayList<JadwalFilm> getListJadwal() { return listJadwal; }
    public void setListJadwal(ArrayList<JadwalFilm> listJadwal) { this.listJadwal = listJadwal; }

    // Method pendukung relasi jadwal
    public void addJadwal(JadwalFilm jadwal) {
        this.listJadwal.add(jadwal);
    }

    @Override
    public String toString() { return "Studio " + getNomorStudio() + " (" + getJenisStudio() + ")";}
}