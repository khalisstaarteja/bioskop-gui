package main.java.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import main.java.model.User;
import main.java.model.Admin;
import main.java.model.Pelanggan;

public class UserDAO {
    // Autentikasi khusus berdasarkan role (Admin via Username, Pelanggan via Email)
    public User authenticateByRole(String role, String identifier, String password) throws SQLException {
        String sql = "";
        
        if ("Admin".equalsIgnoreCase(role)) {
            sql = "SELECT u.id, u.nama, u.password, u.role, a.username " +
                  "FROM users u " +
                  "JOIN admin a ON u.id = a.admin_id " +
                  "WHERE a.username = ? AND u.password = ? AND u.role = 'Admin'";
        } else if ("Pelanggan".equalsIgnoreCase(role)) {
            sql = "SELECT u.id, u.nama, u.password, u.role, p.email " +
                  "FROM users u " +
                  "JOIN pelanggan p ON u.id = p.pelanggan_id " +
                  "WHERE p.email = ? AND u.password = ? AND u.role = 'Pelanggan'";
        } else {
            return null;
        }

        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, identifier);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String nama = rs.getString("nama");
                    String pass = rs.getString("password");
                    String userRole = rs.getString("role");

                    if ("Admin".equalsIgnoreCase(userRole)) {
                        String username = rs.getString("username");
                        Admin admin = new Admin(nama, pass, username);
                        admin.setId(id);
                        return admin;
                    } else if ("Pelanggan".equalsIgnoreCase(userRole)) {
                        String email = rs.getString("email");
                        Pelanggan pelanggan = new Pelanggan(nama, email, pass, userRole);
                        pelanggan.setId(id);
                        return pelanggan;
                    }
                }
            }
        }
        return null;
    }

    // Ambil semua data user dari database
    public List<User> getAllUser() throws SQLException {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Connection conn = PostgreSQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String nama = rs.getString("nama");
                String password = rs.getString("password");
                String role = rs.getString("role");
                
                User user;

                if ("Admin".equalsIgnoreCase(role)) {
                    String username = "";
                    String sqlAdmin = "SELECT username from admin WHERE admin_id = ?";

                    try (PreparedStatement ps = conn.prepareStatement(sqlAdmin)) {
                        ps.setInt(1, id);

                        try (ResultSet rsAdmin = ps.executeQuery()) {
                            if (rsAdmin.next()) {
                                username = rsAdmin.getString("username");
                            }
                        }
                    }
                    user = new Admin(nama, password, username);
                }
                else if ("Pelanggan".equalsIgnoreCase(role)) {
                    String email = "";
                    String sqlPelanggan = "SELECT email FROM pelanggan WHERE pelanggan_id = ?";

                    try (PreparedStatement ps = conn.prepareStatement(sqlPelanggan)) {
                        ps.setInt(1, id);

                        try (ResultSet rsPelanggan = ps.executeQuery()) {
                            if (rsPelanggan.next()) {
                                email = rsPelanggan.getString("email");
                            }
                        }
                    }
                    user = new Pelanggan(nama, email, password, role);
                }
                else {
                    continue;
                }
                user.setId(id);
                userList.add(user);
            }
        }
        return userList;
    }

    // Cek apakah email yang mau diregister udah ada yang pake
    public boolean isEmailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM pelanggan WHERE email = ?";
        try (Connection conn = PostgreSQLConnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0; // True jika email udah ada
                }
            }
        }
        return false;
    }

    // Simpan data user baru
    public void saveUser(User user) throws SQLException {
        String sqlUser = "INSERT INTO users (nama, password, role) VALUES (?, ?, ?)";
        String sqlAdmin = "INSERT INTO admin (admin_id, username) VALUES (?, ?)";
        String sqlPelanggan = "INSERT INTO pelanggan (pelanggan_id, email) VALUES (?, ?)";
        try (Connection conn = PostgreSQLConnection.getConnection()) {
            conn.setAutoCommit(false);
            int generatedId = -1;
            // Simpan ke tabel users
            try (PreparedStatement pstmtUser = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                pstmtUser.setString(1, user.getNama());
                pstmtUser.setString(2, user.getPassword());
                pstmtUser.setString(3, user.getRole());
                pstmtUser.executeUpdate();

                // Ambil id auto increment yang di generate database
                try (ResultSet rs = pstmtUser.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getInt(1);
                    }
                }
            }
            // Validasi jika id gagal diambil
            if (generatedId == -1) {
                conn.rollback();
                throw new SQLException("Gagal mengambil auto-increment ID dari table users.");
            }

            // Jika role adalah Admin, simpan juga secara otomatis ke tabel admin
            if ("Admin".equalsIgnoreCase(user.getRole())) {
                try (PreparedStatement pstmtAdmin = conn.prepareStatement(sqlAdmin)) {
                    pstmtAdmin.setInt(1, generatedId);
                    pstmtAdmin.setString(2, ((Admin)user).getUsername());
                    pstmtAdmin.executeUpdate();
                }
            }
            // Jika role adalah Pelanggan, simpan ke tabel pelanggan
            else if ("Pelanggan".equalsIgnoreCase(user.getRole()) && user instanceof Pelanggan) {
                try (PreparedStatement pstmtPelanggan = conn.prepareStatement(sqlPelanggan)) {
                    pstmtPelanggan.setInt(1, generatedId);
                    pstmtPelanggan.setString(2, ((Pelanggan) user).getEmail());
                    pstmtPelanggan.executeUpdate();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    // Update data user berdasarkan ID
    public void updateUser(User user, int id) throws SQLException {
        try (Connection conn = PostgreSQLConnection.getConnection()) {
            conn.setAutoCommit(false);
            try{
                try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE users SET nama = ?, password = ?, role = ? WHERE id = ?")) {
                    ps.setString(1, user.getNama());
                    ps.setString(2, user.getPassword());
                    ps.setString(3, user.getRole());
                    ps.setInt(4, id);
                    ps.executeUpdate();
                }

                if (user instanceof Admin) {
                    try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE admin SET username = ? WHERE admin_id = ?")) {
                        ps.setString(1, ((Admin) user).getUsername());
                        ps.setInt(2, id);
                        ps.executeUpdate();
                    }
                }

                else if (user instanceof Pelanggan) {
                    try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE pelanggan SET email = ? WHERE pelanggan_id = ?")) {
                        ps.setString(1, ((Pelanggan) user).getEmail());
                        ps.setInt(2, id);
                        ps.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // Hapus data user berdasarkan ID
    public void deleteUser(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
}