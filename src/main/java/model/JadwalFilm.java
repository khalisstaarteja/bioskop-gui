package main.java.model;

import java.time.LocalDate;
import java.time.LocalTime;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class JadwalFilm {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty kodeJadwal = new SimpleStringProperty();
    
    // Objek relasi
    private ObjectProperty<Studio> studio = new SimpleObjectProperty<>();
    private ObjectProperty<Film> film = new SimpleObjectProperty<>();
    
    private final ObjectProperty<LocalDate> tanggal = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalTime> jam = new SimpleObjectProperty<>();
    private final IntegerProperty kapasitas = new SimpleIntegerProperty();
    private final IntegerProperty kursiTerisi = new SimpleIntegerProperty();

    public JadwalFilm(int id, String kodeJadwal, Studio studio, Film film, LocalDate tanggal, LocalTime jam, int kapasitas, int kursiTerisi) {
        this.id.set(id);
        this.kodeJadwal.set(kodeJadwal);
        this.studio.set(studio);
        this.film.set(film);
        this.tanggal.set(tanggal);
        this.jam.set(jam);
        this.kapasitas.set(kapasitas);
        this.kursiTerisi.set(kursiTerisi);
    }

    // Getter, Setter, Property methods
    // ID
    public IntegerProperty idProperty() { return id; }
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }

    // Kode Jadwal
    public StringProperty kodeJadwalProperty() { return kodeJadwal; }
    public String getKodeJadwal() { return kodeJadwal.get(); }
    public void setKodeJadwal(String kodeJadwal) { this.kodeJadwal.set(kodeJadwal); }

    // Studio
    public ObjectProperty<Studio> studioProperty() { return studio; }
    public Studio getStudio() { return studio.get(); }
    public void setStudio(Studio studio) { this.studio.set(studio); }

    // Film
    public ObjectProperty<Film> filmProperty() { return film; }
    public Film getFilm() { return film.get(); }
    public void setFilm(Film film) { this.film.set(film); }

    // Tanggal
    public ObjectProperty<LocalDate> tanggalProperty() { return tanggal; }
    public LocalDate getTanggal() { return tanggal.get(); }
    public void setTanggal(LocalDate tanggal) { this.tanggal.set(tanggal); }

    // Jam
    public ObjectProperty<LocalTime> jamProperty() { return jam; }
    public LocalTime getJam() { return jam.get(); }
    public void setJam(LocalTime jam) { this.jam.set(jam); }

    // Kapasitas
    public IntegerProperty kapasitasProperty() { return kapasitas; }
    public int getKapasitas() { return kapasitas.get(); }
    public void setKapasitas(int kapasitas) { this.kapasitas.set(kapasitas); }

    // Kursi Terisi
    public IntegerProperty kursiTerisiProperty() { return kursiTerisi; }
    public int getKursiTerisi() { return kursiTerisi.get(); }
    public void setKursiTerisi(int kursiTerisi) { this.kursiTerisi.set(kursiTerisi); }

    // Method kursi
    public int sisaKursi() {
        return getKapasitas() - getKursiTerisi();
    }

    public void tambahKursiTerisi(int jumlah) {
        this.kursiTerisi.set(this.kursiTerisi.get() + jumlah);
    }

    @Override
    public String toString() { return getTanggal() + " | " + getJam() + " - Studio " + getStudio().getNomorStudio(); }
}