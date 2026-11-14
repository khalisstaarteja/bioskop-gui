package main.java.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import main.java.model.Bioskop;

public class BioskopDAO {
    public List<Bioskop> getAllBioskop() throws SQLException {
        List<Bioskop> bioskopList = new ArrayList<>();
        String sql = "SELECT id, kode_bioskop, nama_bioskop, lokasi FROM bioskop";

        try (Connection conn = PostgreSQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String kodeBioskop = rs.getString("kode_bioskop");
                String namaBioskop = rs.getString("nama_bioskop");
                String lokasi = rs.getString("lokasi");

                bioskopList.add(new Bioskop(id, kodeBioskop, namaBioskop, lokasi));
            }
        }
        return bioskopList;
    }

    public void saveBioskop(Bioskop bioskop) throws SQLException {
        String sql = "INSERT INTO bioskop (kode_bioskop, nama_bioskop, lokasi) VALUES (?, ?, ?)";
        
        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, bioskop.getKodeBioskop());
            pstmt.setString(2, bioskop.getNamaBioskop());
            pstmt.setString(3, bioskop.getLokasi());
            pstmt.executeUpdate();
        }
    }

    public void updateBioskop(Bioskop bioskop, int id) throws SQLException {
        String sql = "UPDATE bioskop SET kode_bioskop = ?, nama_bioskop = ?, lokasi = ? WHERE id = ?";
        
        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, bioskop.getKodeBioskop());
            pstmt.setString(2, bioskop.getNamaBioskop());
            pstmt.setString(3, bioskop.getLokasi());
            pstmt.setInt(4, id);
            pstmt.executeUpdate();
        }
    }

    public void deleteBioskop(int id) throws SQLException {
        String sql = "DELETE FROM bioskop WHERE id = ?";
        
        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public List<Bioskop> getBioskopWithStudio() throws SQLException {
        List<Bioskop> list = new ArrayList<>();
        String sql = "SELECT DISTINCT b.* FROM bioskop b JOIN studio s ON b.id = s.bioskop_id";
    
        try (Connection conn = PostgreSQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
    
            while (rs.next()) {
                Bioskop bioskop = new Bioskop(
                    rs.getInt("id"),
                    rs.getString("kode_bioskop"),
                    rs.getString("nama_bioskop"),
                    rs.getString("lokasi")
                );
                list.add(bioskop);
            }
        }
        return list;
    }

    // Hanya bioskop yang punya minimal 1 jadwal tayang dan jadwalnya masih aktif
    public List<Bioskop> getBioskopWithJadwal() throws SQLException {
        List<Bioskop> list = new ArrayList<>();
        String sql = "SELECT DISTINCT b.id, b.kode_bioskop, b.nama_bioskop, b.lokasi " +
            "FROM bioskop b " +
            "JOIN studio s ON s.bioskop_id = b.id " +
            "JOIN jadwal_film j ON j.studio_id = s.id " +
            "WHERE (j.tanggal + j.jam) >= CURRENT_TIMESTAMP " +
            "AND j.kursi_terisi < j.kapasitas " +
            "ORDER BY b.nama_bioskop";

        try (Connection conn = PostgreSQLConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Bioskop(
                    rs.getInt("id"),
                    rs.getString("kode_bioskop"),
                    rs.getString("nama_bioskop"),
                    rs.getString("lokasi")
                ));
            }
        }
        return list;
    }
}