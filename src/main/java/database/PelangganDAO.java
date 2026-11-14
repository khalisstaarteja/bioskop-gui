package main.java.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import main.java.model.Pelanggan;

public class PelangganDAO {
    // Ambil semua data pelanggan beserta informasi akun usernya menggunakan JOIN
    public List<Pelanggan> getAllPelanggan() throws SQLException {
        List<Pelanggan> pelangganList = new ArrayList<>();
        String sql = "SELECT p.pelanggan_id, p.email, u.id AS user_id, u.nama, u.password, u.role " +
                     "FROM pelanggan p " +
                     "JOIN users u ON p.pelanggan_id = u.id";

        try (Connection conn = PostgreSQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int pelangganId = rs.getInt("pelanggan_id");
                String email = rs.getString("email");
                int userId = rs.getInt("user_id");
                String nama = rs.getString("nama");
                String password = rs.getString("password");
                String role = rs.getString("role");

                pelangganList.add(new Pelanggan(userId, nama, password, role, pelangganId, email));
            }
        }
        return pelangganList;
    }

    // Simpan data ke tabel pelanggan
    public void savePelanggan(Pelanggan pelanggan, int userId) throws SQLException {
        String sql = "INSERT INTO pelanggan (pelanggan_id, email) VALUES (?, ?)";

        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, pelanggan.getEmail());
            pstmt.executeUpdate();
        }
    }

    // Update data pelanggan berdasarkan pelanggan_id
    public void updatePelanggan(Pelanggan pelanggan, int pelangganId) throws SQLException {
        String sql = "UPDATE pelanggan SET email = ? WHERE pelanggan_id = ?";

        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, pelanggan.getEmail());
            pstmt.setInt(2, pelangganId);
            pstmt.executeUpdate();
        }
    }

    // Hapus data pelanggan berdasarkan pelanggan_id
    public void deletePelanggan(int pelangganId) throws SQLException {
        String sql = "DELETE FROM pelanggan WHERE pelanggan_id = ?";

        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, pelangganId);
            pstmt.executeUpdate();
        }
    }
}