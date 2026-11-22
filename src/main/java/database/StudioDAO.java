package main.java.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import main.java.model.Bioskop;
import main.java.model.Film;
import main.java.model.JadwalFilm;
import main.java.model.Studio;

public class StudioDAO {
    // Ambil semua data studio beserta data bioskop induknya (menggunakan JOIN)
    public List<Studio> getAllStudio() throws SQLException {
        List<Studio> studioList = new ArrayList<>();
        String sql = "SELECT s.id AS studio_id, s.kode_studio, s.nomor_studio, s.jenis_studio, s.harga, " +
                     "b.id AS bioskop_id, b.kode_bioskop, b.nama_bioskop, b.lokasi " +
                     "FROM studio s " +
                     "JOIN bioskop b ON s.bioskop_id = b.id";

        try (Connection conn = PostgreSQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("studio_id");
                String kodeStudio = rs.getString("kode_studio");
                int nomorStudio = rs.getInt("nomor_studio");
                String jenisStudio = rs.getString("jenis_studio");
                float harga = rs.getFloat("harga");

                // Membuat objek Bioskop relasinya
                Bioskop bioskop = new Bioskop(
                    rs.getInt("bioskop_id"),
                    rs.getString("kode_bioskop"),
                    rs.getString("nama_bioskop"),
                    rs.getString("lokasi")
                );

                studioList.add(new Studio(id, kodeStudio, nomorStudio, jenisStudio, bioskop, harga));
            }
        }
        return studioList;
    }

    public void saveStudio(Studio studio) throws SQLException {
        String sql = "INSERT INTO studio (kode_studio, nomor_studio, jenis_studio, harga, bioskop_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studio.getKodeStudio());
            pstmt.setInt(2, studio.getNomorStudio());
            pstmt.setString(3, studio.getJenisStudio());
            pstmt.setFloat(4, studio.getHarga());
            pstmt.setInt(5, studio.getBioskop().getId());
            pstmt.executeUpdate();
    
            syncListStudio(conn, studio.getBioskop().getId());
        }
    }
    
    public void updateStudio(Studio studio, int id) throws SQLException {
        String sql = "UPDATE studio SET kode_studio = ?, nomor_studio = ?, jenis_studio = ?, harga = ?, bioskop_id = ? WHERE id = ?";
        try (Connection conn = PostgreSQLConnection.getConnection()) {
            int bioskopLama = getBioskopIdOfStudio(conn, id);
    
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, studio.getKodeStudio());
                pstmt.setInt(2, studio.getNomorStudio());
                pstmt.setString(3, studio.getJenisStudio());
                pstmt.setFloat(4, studio.getHarga());
                pstmt.setInt(5, studio.getBioskop().getId());
                pstmt.setInt(6, id);
                pstmt.executeUpdate();
            }
    
            syncListStudio(conn, studio.getBioskop().getId());
            if (bioskopLama != -1 && bioskopLama != studio.getBioskop().getId()) {
                syncListStudio(conn, bioskopLama);
            }
        }
    }
    
    public void deleteStudio(int id) throws SQLException {
        try (Connection conn = PostgreSQLConnection.getConnection()) {
            int bioskopId = getBioskopIdOfStudio(conn, id);
    
            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM studio WHERE id = ?")) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }
    
            if (bioskopId != -1) syncListStudio(conn, bioskopId);
        }
    }
    // Mengambil daftar studio berdasarkan ID Bioskop
    public List<Studio> getStudioByBioskopId(int bioskopId) throws SQLException {
        List<Studio> list = new ArrayList<>();
        String sql = "SELECT s.id AS studio_id, s.kode_studio, s.nomor_studio, s.jenis_studio, s.harga, " +
                    "b.id AS bioskop_id, b.kode_bioskop, b.nama_bioskop, b.lokasi " +
                    "FROM studio s " +
                    "JOIN bioskop b ON s.bioskop_id = b.id " +
                    "WHERE s.bioskop_id = ?";

        try (Connection conn = PostgreSQLConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, bioskopId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
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

                    list.add(studio);
                }
            }
        }
        return list;
    }

    // Ambil studio yang punya jadwal, lengkap dengan listJadwal terisi
    public List<Studio> getStudioWithJadwal() throws SQLException {
        List<Studio> list = new ArrayList<>();
        String sql = "SELECT s.id AS studio_id, s.kode_studio, s.nomor_studio, s.jenis_studio, s.harga, s.list_jadwal, " +
                    "b.id AS bioskop_id, b.kode_bioskop, b.nama_bioskop, b.lokasi " +
                    "FROM studio s JOIN bioskop b ON s.bioskop_id = b.id " +
                    "WHERE cardinality(s.list_jadwal) > 0 " +
                    "ORDER BY b.nama_bioskop, s.nomor_studio";
        String sqlJadwal = "SELECT j.id, j.kode_jadwal, j.tanggal, j.jam, j.kapasitas, j.kursi_terisi, " +
                        "f.id AS film_id, f.kode_film, f.judul, f.genre, f.durasi, f.poster " +
                        "FROM jadwal_film j JOIN film f ON j.film_id = f.id " +
                        "WHERE j.id = ANY(?) ORDER BY j.tanggal, j.jam";

        try (Connection conn = PostgreSQLConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            PreparedStatement ps = conn.prepareStatement(sqlJadwal)) {

            while (rs.next()) {
                Bioskop bioskop = new Bioskop(
                    rs.getInt("bioskop_id"),
                    rs.getString("kode_bioskop"),
                    rs.getString("nama_bioskop"),
                    rs.getString("lokasi"));

                Studio studio = new Studio(
                    rs.getInt("studio_id"),
                    rs.getString("kode_studio"),
                    rs.getInt("nomor_studio"),
                    rs.getString("jenis_studio"),
                    bioskop,
                    rs.getFloat("harga"));

                Array arr = rs.getArray("list_jadwal");
                if (arr != null) {
                    Integer[] ids = (Integer[]) arr.getArray();
                    ps.setArray(1, conn.createArrayOf("integer", ids));
                    try (ResultSet rs2 = ps.executeQuery()) {
                        while (rs2.next()) {
                            Film film = new Film(
                                rs2.getInt("film_id"),
                                rs2.getString("kode_film"),
                                rs2.getString("judul"),
                                rs2.getString("genre"),
                                rs2.getInt("durasi"),
                                rs2.getBytes("poster"));

                            studio.addJadwal(new JadwalFilm(
                                rs2.getInt("id"),
                                rs2.getString("kode_jadwal"),
                                studio,
                                film,
                                rs2.getDate("tanggal").toLocalDate(),
                                rs2.getTime("jam").toLocalTime(),
                                rs2.getInt("kapasitas"),
                                rs2.getInt("kursi_terisi")));
                        }
                    }
                }
                list.add(studio);
            }
        }
        return list;
    }
    
    // Sync ke bioskop
    private void syncListStudio(Connection conn, int bioskopId) throws SQLException {
        String sql = "UPDATE bioskop SET list_studio = COALESCE(" +
                     "(SELECT array_agg(id ORDER BY nomor_studio) FROM studio WHERE bioskop_id = ?), '{}') " +
                     "WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bioskopId);
            ps.setInt(2, bioskopId);
            ps.executeUpdate();
        }
    }
    
    private int getBioskopIdOfStudio(Connection conn, int studioId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT bioskop_id FROM studio WHERE id = ?")) {
            ps.setInt(1, studioId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }
}