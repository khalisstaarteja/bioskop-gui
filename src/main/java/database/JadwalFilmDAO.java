package main.java.database;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import main.java.model.Bioskop;
import main.java.model.Film;
import main.java.model.JadwalFilm;
import main.java.model.Studio;

public class JadwalFilmDAO {
    // Ambil semua data jadwal film beserta data relasi Studio, Bioskop, dan Film
    public List<JadwalFilm> getAllJadwalFilm() throws SQLException {
        List<JadwalFilm> jadwalList = new ArrayList<>();
        String sql = "SELECT j.id AS jadwal_id, j.kode_jadwal, j.tanggal, j.jam, j.kapasitas, j.kursi_terisi, " +
                     "s.id AS studio_id, s.kode_studio, s.nomor_studio, s.jenis_studio, s.harga, " +
                     "b.id AS bioskop_id, b.kode_bioskop, b.nama_bioskop, b.lokasi, " +
                     "f.id AS film_id, f.kode_film, f.judul, f.genre, f.durasi, f.poster " +
                     "FROM jadwal_film j " +
                     "JOIN studio s ON j.studio_id = s.id " +
                     "JOIN bioskop b ON s.bioskop_id = b.id " +
                     "JOIN film f ON j.film_id = f.id";

        try (Connection conn = PostgreSQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("jadwal_id");
                String kodeJadwal = rs.getString("kode_jadwal");
                LocalDate tanggal = rs.getDate("tanggal").toLocalDate();
                LocalTime jam = rs.getTime("jam").toLocalTime();
                int kapasitas = rs.getInt("kapasitas");
                int kursiTerisi = rs.getInt("kursi_terisi");

                // Rekonstruksi objek Bioskop
                Bioskop bioskop = new Bioskop(
                    rs.getInt("bioskop_id"),
                    rs.getString("kode_bioskop"),
                    rs.getString("nama_bioskop"),
                    rs.getString("lokasi")
                );

                // Rekonstruksi objek Studio
                Studio studio = new Studio(
                    rs.getInt("studio_id"),
                    rs.getString("kode_studio"),
                    rs.getInt("nomor_studio"),
                    rs.getString("jenis_studio"),
                    bioskop,
                    rs.getFloat("harga")
                );

                // Rekonstruksi objek Film
                Film film = new Film(
                    rs.getInt("film_id"),
                    rs.getString("kode_film"),
                    rs.getString("judul"),
                    rs.getString("genre"),
                    rs.getInt("durasi"),
                    rs.getBytes("poster")
                );

                jadwalList.add(new JadwalFilm(id, kodeJadwal, studio, film, tanggal, jam, kapasitas, kursiTerisi));
            }
        }
        return jadwalList;
    }

    public void saveJadwalFilm(JadwalFilm jadwal) throws SQLException {
        String sql = "INSERT INTO jadwal_film (kode_jadwal, studio_id, film_id, tanggal, jam, kapasitas, kursi_terisi) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, jadwal.getKodeJadwal());
            pstmt.setInt(2, jadwal.getStudio().getId());
            pstmt.setInt(3, jadwal.getFilm().getId());
            pstmt.setDate(4, Date.valueOf(jadwal.getTanggal()));
            pstmt.setTime(5, Time.valueOf(jadwal.getJam()));
            pstmt.setInt(6, jadwal.getKapasitas());
            pstmt.setInt(7, jadwal.getKursiTerisi());
            pstmt.executeUpdate();
    
            syncListJadwal(conn, jadwal.getStudio().getId());
            syncListJadwalFilm(conn, jadwal.getFilm().getId());
        }
    }
    
    public void updateJadwalFilm(JadwalFilm jadwal, int id) throws SQLException {
        String sql = "UPDATE jadwal_film SET kode_jadwal = ?, studio_id = ?, film_id = ?, tanggal = ?, jam = ?, kapasitas = ?, kursi_terisi = ? WHERE id = ?";
        try (Connection conn = PostgreSQLConnection.getConnection()) {
            int studioLama = getStudioIdOfJadwal(conn, id);
            int filmLama = getFilmIdOfJadwal(conn, id);
    
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, jadwal.getKodeJadwal());
                pstmt.setInt(2, jadwal.getStudio().getId());
                pstmt.setInt(3, jadwal.getFilm().getId());
                pstmt.setDate(4, Date.valueOf(jadwal.getTanggal()));
                pstmt.setTime(5, Time.valueOf(jadwal.getJam()));
                pstmt.setInt(6, jadwal.getKapasitas());
                pstmt.setInt(7, jadwal.getKursiTerisi());
                pstmt.setInt(8, id);
                pstmt.executeUpdate();
            }
    
            syncListJadwal(conn, jadwal.getStudio().getId());
            if (studioLama != -1 && studioLama != jadwal.getStudio().getId()) {
                syncListJadwal(conn, studioLama);
            }

            syncListJadwalFilm(conn, jadwal.getFilm().getId());
            if (filmLama != -1 && filmLama != jadwal.getFilm().getId()) {
                syncListJadwalFilm(conn, filmLama);
            }
        }
    }
    
    public void deleteJadwalFilm(int id) throws SQLException {
        try (Connection conn = PostgreSQLConnection.getConnection()) {
            int studioId = getStudioIdOfJadwal(conn, id);
            int filmId = getFilmIdOfJadwal(conn, id);
    
            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM jadwal_film WHERE id = ?")) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }
    
            if (studioId != -1) syncListJadwal(conn, studioId);
        }
    }

    // Ambil jadwal aktif (belum lewat) sesuai film
    public List<JadwalFilm> getJadwalByFilmId(int filmId) throws SQLException {
        List<JadwalFilm> list = new ArrayList<>();
        
        String sql = "SELECT j.id AS jadwal_id, j.kode_jadwal, j.tanggal, j.jam, j.kapasitas, j.kursi_terisi, " +
                     "f.id AS film_id, f.kode_film, f.judul, f.genre, f.durasi, f.poster, " +
                     "s.id AS studio_id, s.kode_studio, s.nomor_studio, s.jenis_studio, s.harga, " +
                     "b.id AS bioskop_id, b.kode_bioskop, b.nama_bioskop, b.lokasi " +
                     "FROM jadwal_film j " +
                     "JOIN film f ON j.film_id = f.id " +
                     "JOIN studio s ON j.studio_id = s.id " +
                     "JOIN bioskop b ON s.bioskop_id = b.id " +
                     "WHERE j.film_id = ? " +
                     "AND (j.tanggal + j.jam) >= CURRENT_TIMESTAMP " +
                     "ORDER BY j.tanggal ASC, j.jam ASC";
    
        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, filmId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Bioskop bioskop = new Bioskop(
                        rs.getInt("bioskop_id"),
                        rs.getString("kode_bioskop"),
                        rs.getString("nama_bioskop"),
                        rs.getString("lokasi")
                    );

                    Film film = new Film(
                        rs.getInt("film_id"),
                        rs.getString("kode_film"),
                        rs.getString("judul"),
                        rs.getString("genre"),
                        rs.getInt("durasi"),
                        rs.getBytes("poster")
                    );
    
                    Studio studio = new Studio(
                        rs.getInt("studio_id"),
                        rs.getString("kode_studio"),
                        rs.getInt("nomor_studio"),
                        rs.getString("jenis_studio"),
                        bioskop,
                        rs.getFloat("harga")
                    );
    
                    JadwalFilm jadwal = new JadwalFilm(
                        rs.getInt("jadwal_id"),
                        rs.getString("kode_jadwal"),
                        studio,
                        film,
                        rs.getDate("tanggal").toLocalDate(),
                        rs.getTime("jam").toLocalTime(), 
                        rs.getInt("kapasitas"),
                        rs.getInt("kursi_terisi")
                    );
    
                    list.add(jadwal);
                }
            }
        }
        return list;
    }

    // Mengambil jadwal difilter berdasarkan Bioskop ID DAN Film ID
    public List<JadwalFilm> getJadwalByBioskopAndFilm(int bioskopId, int filmId) throws SQLException {
        List<JadwalFilm> list = new ArrayList<>();
        String sql = "SELECT j.id AS jadwal_id, j.kode_jadwal, j.tanggal, j.jam, j.kapasitas, j.kursi_terisi, " +
             "f.id AS film_id, f.kode_film, f.judul, f.genre, f.durasi, f.poster, " +
             "s.id AS studio_id, s.kode_studio, s.nomor_studio, s.jenis_studio, s.harga, " +
             "b.id AS bioskop_id, b.kode_bioskop, b.nama_bioskop, b.lokasi " +
             "FROM jadwal_film j " +
             "JOIN film f ON j.film_id = f.id " +
             "JOIN studio s ON j.studio_id = s.id " +
             "JOIN bioskop b ON s.bioskop_id = b.id " +
             "WHERE b.id = ? AND f.id = ? " +
             "AND (j.tanggal + j.jam) >= CURRENT_TIMESTAMP " +
             "AND j.kursi_terisi < j.kapasitas " +
             "ORDER BY j.tanggal, j.jam";

        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, bioskopId);
            pstmt.setInt(2, filmId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Bioskop bioskop = new Bioskop(
                        rs.getInt("bioskop_id"),
                        rs.getString("kode_bioskop"),
                        rs.getString("nama_bioskop"),
                        rs.getString("lokasi")
                    );
                   
                    Film film = new Film(
                        rs.getInt("film_id"),
                        rs.getString("kode_film"),
                        rs.getString("judul"),
                        rs.getString("genre"),
                        rs.getInt("durasi"),
                        rs.getBytes("poster")
                    );
    
                    Studio studio = new Studio(
                        rs.getInt("studio_id"),
                        rs.getString("kode_studio"),
                        rs.getInt("nomor_studio"),
                        rs.getString("jenis_studio"),
                        bioskop,
                        rs.getFloat("harga")
                    );
    
                    JadwalFilm jadwal = new JadwalFilm(
                        rs.getInt("jadwal_id"),
                        rs.getString("kode_jadwal"),
                        studio,
                        film,
                        rs.getDate("tanggal").toLocalDate(),
                        rs.getTime("jam").toLocalTime(), 
                        rs.getInt("kapasitas"),
                        rs.getInt("kursi_terisi")
                    );
    
                    list.add(jadwal);
                }
            }
        }
        return list;
    }

    // Sync ke studio
    private void syncListJadwal(Connection conn, int studioId) throws SQLException {
        String sql = "UPDATE studio SET list_jadwal = COALESCE(" +
                     "(SELECT array_agg(id ORDER BY tanggal, jam) FROM jadwal_film WHERE studio_id = ?), '{}') " +
                     "WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studioId);
            ps.setInt(2, studioId);
            ps.executeUpdate();
        }
    }

    // Sync ke film
    private void syncListJadwalFilm(Connection conn, int filmId) throws SQLException {
        String sql = "UPDATE film SET list_jadwal = COALESCE(" +
                     "(SELECT array_agg(id ORDER BY tanggal, jam) FROM jadwal_film WHERE film_id = ?), '{}') " +
                     "WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, filmId);
            ps.setInt(2, filmId);
            ps.executeUpdate();
        }
    }
    
    private int getFilmIdOfJadwal(Connection conn, int jadwalId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT film_id FROM jadwal_film WHERE id = ?")) {
            ps.setInt(1, jadwalId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }
    
    private int getStudioIdOfJadwal(Connection conn, int jadwalId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT studio_id FROM jadwal_film WHERE id = ?")) {
            ps.setInt(1, jadwalId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }
}