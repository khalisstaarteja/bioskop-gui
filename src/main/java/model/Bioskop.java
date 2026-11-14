package main.java.model;

import java.util.ArrayList;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Bioskop {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty kodeBioskop = new SimpleStringProperty();
    private final StringProperty namaBioskop = new SimpleStringProperty();
    private final StringProperty lokasi = new SimpleStringProperty();
    private ArrayList<Studio> listStudio;

    public Bioskop(int id, String kodeBioskop, String namaBioskop, String lokasi) {
        this.id.set(id);
        this.kodeBioskop.set(kodeBioskop);
        this.namaBioskop.set(namaBioskop);
        this.lokasi.set(lokasi);
        this.listStudio = new ArrayList<>();
    }

    // Property
    public IntegerProperty idProperty() { return id; }
    public StringProperty kodeBioskopProperty() { return kodeBioskop; }
    public StringProperty namaBioskopProperty() { return namaBioskop; }
    public StringProperty lokasiProperty() { return lokasi; }

    // Getter & Setter 
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }

    public String getKodeBioskop() { return kodeBioskop.get(); }
    public void setKodeBioskop(String kodeBioskop) { this.kodeBioskop.set(kodeBioskop); }

    public String getNamaBioskop() { return namaBioskop.get(); }
    public void setNamaBioskop(String namaBioskop) { this.namaBioskop.set(namaBioskop); }

    public String getLokasi() { return lokasi.get(); }
    public void setLokasi(String lokasi) { this.lokasi.set(lokasi); }

    public ArrayList<Studio> getListStudio() { return listStudio; }
    public void setListStudio(ArrayList<Studio> listStudio) { this.listStudio = listStudio; }

    // Method pendukung
    public void addStudio(Studio studio) {
        this.listStudio.add(studio);
    }

    @Override
    public String toString() { return getKodeBioskop() + " - " + getNamaBioskop(); }
}