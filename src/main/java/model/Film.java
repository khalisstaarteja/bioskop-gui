package main.java.model;

import java.util.ArrayList;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Film {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty kodeFilm = new SimpleStringProperty();
    private final StringProperty judul = new SimpleStringProperty();
    private final StringProperty genre = new SimpleStringProperty();
    private final IntegerProperty durasi = new SimpleIntegerProperty();
    
    // Untuk menyimpan poster dalam bentuk byte array (sesuai tipe bytea di PostgreSQL)
    private byte[] poster; 
    private ArrayList<JadwalFilm> listJadwal;

    public Film(int id, String kodeFilm, String judul, String genre, int durasi, byte[] poster) {
        this.id.set(id);
        this.kodeFilm.set(kodeFilm);
        this.judul.set(judul);
        this.genre.set(genre);
        this.durasi.set(durasi);
        this.poster = poster;
        this.listJadwal = new ArrayList<>();
    }
    
    // Getter, Setter, and Property methods
    // ID
    public IntegerProperty idProperty() { return id; }
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }

    // Kode Film
    public StringProperty kodeFilmProperty() { return kodeFilm; }
    public String getKodeFilm() { return kodeFilm.get(); }
    public void setKodeFilm(String kodeFilm) { this.kodeFilm.set(kodeFilm); }

    // Judul
    public StringProperty judulProperty() { return judul; }
    public String getJudul() { return judul.get(); }
    public void setJudul(String judul) { this.judul.set(judul); }

    // Genre
    public StringProperty genreProperty() { return genre; }
    public String getGenre() { return genre.get(); }
    public void setGenre(String genre) { this.genre.set(genre); }

    // Durasi
    public IntegerProperty durasiProperty() { return durasi; }
    public int getDurasi() { return durasi.get(); }
    public void setDurasi(int durasi) { this.durasi.set(durasi); }

    // Poster
    public byte[] getPoster() { return poster; }
    public void setPoster(byte[] poster) { this.poster = poster; }

    // Relasi dengan JadwalFilm
    public ArrayList<JadwalFilm> getListJadwal() { return listJadwal; }
    public void setListJadwal(ArrayList<JadwalFilm> listJadwal) { this.listJadwal = listJadwal; }

    // Method pendukung relasi jadwal film
    public void addJadwal(JadwalFilm jadwal) {
        this.listJadwal.add(jadwal);
    }

    @Override
    public String toString() { return getJudul() + " (" + getDurasi() + " mnt)"; }
}