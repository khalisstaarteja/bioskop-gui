package main.java.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import main.java.model.Admin;

public class AdminDAO {
    // Ambil semua data admin beserta informasi akun usernya menggunakan JOIN
    public List<Admin> getAllAdmin() throws SQLException {
        List<Admin> adminList = new ArrayList<>();
        String sql = "SELECT a.admin_id, a.username, u.id AS user_id, u.nama, u.password, u.role " +
                     "FROM admin a " +
                     "JOIN users u ON a.username = u.nama";

        try (Connection conn = PostgreSQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int adminId = rs.getInt("admin_id");
                String username = rs.getString("username");
                int userId = rs.getInt("user_id");
                String nama = rs.getString("nama");
                String password = rs.getString("password");
                String role = rs.getString("role");

                adminList.add(new Admin(userId, nama, password, role, adminId, username));
            }
        }
        return adminList;
    }

    // Simpan data ke tabel admin
    public void saveAdmin(Admin admin) throws SQLException {
        String sql = "INSERT INTO admin (username) VALUES (?)";

        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, admin.getUsername());
            pstmt.executeUpdate();
        }
    }

    // Update data admin berdasarkan admin_id
    public void updateAdmin(Admin admin, int adminId) throws SQLException {
        String sql = "UPDATE admin SET username = ? WHERE admin_id = ?";

        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, admin.getUsername());
            pstmt.setInt(2, adminId);
            pstmt.executeUpdate();
        }
    }

    // Hapus data admin berdasarkan admin_id
    public void deleteAdmin(int adminId) throws SQLException {
        String sql = "DELETE FROM admin WHERE admin_id = ?";

        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, adminId);
            pstmt.executeUpdate();
        }
    }
}