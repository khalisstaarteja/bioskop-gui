package main.java.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import main.java.model.Film;

public class FilmDAO {
    // Ambil semua data film dari database
    public List<Film> getAllFilm() throws SQLException {
        List<Film> filmList = new ArrayList<>();
        String sql = "SELECT id, kode_film, judul, genre, durasi, poster FROM film";

        try (Connection conn = PostgreSQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String kodeFilm = rs.getString("kode_film");
                String judul = rs.getString("judul");
                String genre = rs.getString("genre");
                int durasi = rs.getInt("durasi");
                byte[] poster = rs.getBytes("poster");

                filmList.add(new Film(id, kodeFilm, judul, genre, durasi, poster));
            }
        }
        return filmList;
    }

    // Simpan data film baru
    public void saveFilm(Film film) throws SQLException {
        String sql = "INSERT INTO film (kode_film, judul, genre, durasi, poster) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, film.getKodeFilm());
            pstmt.setString(2, film.getJudul());
            pstmt.setString(3, film.getGenre());
            pstmt.setInt(4, film.getDurasi());
            pstmt.setBytes(5, film.getPoster()); // Menyimpan byte array untuk kolom bytea
            
            pstmt.executeUpdate();
        }
    }

    // Update data film berdasarkan ID
    public void updateFilm(Film film, int id) throws SQLException {
        String sql = "UPDATE film SET kode_film = ?, judul = ?, genre = ?, durasi = ?, poster = ? WHERE id = ?";

        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, film.getKodeFilm());
            pstmt.setString(2, film.getJudul());
            pstmt.setString(3, film.getGenre());
            pstmt.setInt(4, film.getDurasi());
            pstmt.setBytes(5, film.getPoster());
            pstmt.setInt(6, id);

            pstmt.executeUpdate();
        }
    }

    // Hapus data film berdasarkan ID
    public void deleteFilm(int id) throws SQLException {
        String sql = "DELETE FROM film WHERE id = ?";

        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    // Mengambil daftar film terbaru untuk carousel/slider di dashboard admin
    public List<Film> getRecentFilms() throws SQLException {
        List<Film> list = new ArrayList<>();
        // Ambil 5 film terbaru berdasarkan ID terbesar
        String sql = "SELECT * FROM film ORDER BY id DESC LIMIT 5"; 

        try (Connection conn = PostgreSQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String kodeFilm = rs.getString("kode_film");
                String judul = rs.getString("judul");
                String genre = rs.getString("genre");
                int durasi = rs.getInt("durasi");
                byte[] poster = rs.getBytes("poster");

                Film film = new Film(id, kodeFilm, judul, genre, durasi, poster);
                list.add(film);
            }
        }
        return list;
    }

    // Mengambil daftar film yang memiliki jadwal aktif di bioskop tertentu
    public List<Film> getFilmByBioskopId(int bioskopId) throws SQLException {
        List<Film> list = new ArrayList<>();
        String sql = "SELECT DISTINCT f.id, f.kode_film, f.judul, f.genre, f.durasi, f.poster " +
             "FROM film f " +
             "JOIN jadwal_film j ON f.id = j.film_id " +
             "JOIN studio s ON j.studio_id = s.id " +
             "WHERE s.bioskop_id = ? " +
             "AND (j.tanggal + j.jam) >= CURRENT_TIMESTAMP " +
             "AND j.kursi_terisi < j.kapasitas";

        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, bioskopId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Film film = new Film(
                        rs.getInt("id"),
                        rs.getString("kode_film"),
                        rs.getString("judul"),
                        rs.getString("genre"),
                        rs.getInt("durasi"),
                        rs.getBytes("poster")
                    );
                    list.add(film);
                }
            }
        }
        return list;
    }

    // Mengambil hanya film yang memiliki jadwal tayang aktif
    public List<Film> getFilmWithJadwal() throws SQLException {
        List<Film> list = new ArrayList<>();
        String sql = "SELECT DISTINCT f.* FROM film f JOIN jadwal_film j ON f.id = j.film_id";

        try (Connection conn = PostgreSQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String kodeFilm = rs.getString("kode_film");
                String judul = rs.getString("judul");
                String genre = rs.getString("genre");
                int durasi = rs.getInt("durasi");
                byte[] poster = rs.getBytes("poster");

                Film film = new Film(id, kodeFilm, judul, genre, durasi, poster);
                list.add(film);
            }
        }
        return list;
    }
}