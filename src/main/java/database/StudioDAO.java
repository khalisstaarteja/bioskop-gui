package main.java.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import main.java.model.Bioskop;
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

    // Simpan data studio baru ke database
    public void saveStudio(Studio studio) throws SQLException {
        String sql = "INSERT INTO studio (kode_studio, nomor_studio, jenis_studio, harga, bioskop_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studio.getKodeStudio());
            pstmt.setInt(2, studio.getNomorStudio());
            pstmt.setString(3, studio.getJenisStudio());
            pstmt.setFloat(4, studio.getHarga());
            pstmt.setInt(5, studio.getBioskop().getId()); // Mengambil ID dari objek Bioskop yang berelasi
            
            pstmt.executeUpdate();
        }
    }

    // Update data studio berdasarkan ID
    public void updateStudio(Studio studio, int id) throws SQLException {
        String sql = "UPDATE studio SET kode_studio = ?, nomor_studio = ?, jenis_studio = ?, harga = ?, bioskop_id = ? WHERE id = ?";

        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studio.getKodeStudio());
            pstmt.setInt(2, studio.getNomorStudio());
            pstmt.setString(3, studio.getJenisStudio());
            pstmt.setFloat(4, studio.getHarga());
            pstmt.setInt(5, studio.getBioskop().getId());
            pstmt.setInt(6, id);

            pstmt.executeUpdate();
        }
    }

    // Hapus data studio berdasarkan ID
    public void deleteStudio(int id) throws SQLException {
        String sql = "DELETE FROM studio WHERE id = ?";

        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
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
}