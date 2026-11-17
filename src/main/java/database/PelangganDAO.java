package main.java.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import main.java.model.Bioskop;
import main.java.model.Film;
import main.java.model.JadwalFilm;
import main.java.model.Pelanggan;
import main.java.model.Pemesanan;
import main.java.model.Studio;

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

    // Ambil semua pelanggan beserta list pemesanannya (dari kolom pelanggan.list_pemesanan)
    public List<Pelanggan> getAllPelangganWithPemesanan() throws SQLException {
        List<Pelanggan> list = new ArrayList<>();
        String sql = "SELECT p.pelanggan_id, p.email, p.list_pemesanan, " +
                     "u.id AS user_id, u.nama, u.password, u.role " +
                     "FROM pelanggan p JOIN users u ON p.pelanggan_id = u.id " +
                     "ORDER BY u.nama";
        String sqlPemesanan = "SELECT pm.id, pm.nomor_pemesanan, pm.jumlah_pemesanan, pm.metode_pembayaran, pm.total_bayar, " +
                              "j.id AS jadwal_id, j.kode_jadwal, j.tanggal, j.jam, j.kapasitas, j.kursi_terisi, " +
                              "s.id AS studio_id, s.kode_studio, s.nomor_studio, s.jenis_studio, s.harga, " +
                              "b.id AS bioskop_id, b.kode_bioskop, b.nama_bioskop, b.lokasi, " +
                              "f.id AS film_id, f.kode_film, f.judul, f.genre, f.durasi, f.poster " +
                              "FROM pemesanan pm " +
                              "JOIN jadwal_film j ON pm.jadwal_id = j.id " +
                              "JOIN studio s ON j.studio_id = s.id " +
                              "JOIN bioskop b ON s.bioskop_id = b.id " +
                              "JOIN film f ON j.film_id = f.id " +
                              "WHERE pm.id = ANY(?) ORDER BY pm.id DESC";

        try (Connection conn = PostgreSQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
             PreparedStatement ps = conn.prepareStatement(sqlPemesanan)) {

            while (rs.next()) {
                Pelanggan pelanggan = new Pelanggan(
                    rs.getInt("user_id"),
                    rs.getString("nama"),
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getInt("pelanggan_id"),
                    rs.getString("email"));

                Array arr = rs.getArray("list_pemesanan");
                if (arr != null) {
                    Integer[] ids = (Integer[]) arr.getArray();
                    ps.setArray(1, conn.createArrayOf("integer", ids));
                    try (ResultSet rs2 = ps.executeQuery()) {
                        while (rs2.next()) {
                            Bioskop bioskop = new Bioskop(
                                rs2.getInt("bioskop_id"),
                                rs2.getString("kode_bioskop"),
                                rs2.getString("nama_bioskop"),
                                rs2.getString("lokasi"));

                            Studio studio = new Studio(
                                rs2.getInt("studio_id"),
                                rs2.getString("kode_studio"),
                                rs2.getInt("nomor_studio"),
                                rs2.getString("jenis_studio"),
                                bioskop,
                                rs2.getFloat("harga"));

                            Film film = new Film(
                                rs2.getInt("film_id"),
                                rs2.getString("kode_film"),
                                rs2.getString("judul"),
                                rs2.getString("genre"),
                                rs2.getInt("durasi"),
                                rs2.getBytes("poster"));

                            JadwalFilm jadwal = new JadwalFilm(
                                rs2.getInt("jadwal_id"),
                                rs2.getString("kode_jadwal"),
                                studio,
                                film,
                                rs2.getDate("tanggal").toLocalDate(),
                                rs2.getTime("jam").toLocalTime(),
                                rs2.getInt("kapasitas"),
                                rs2.getInt("kursi_terisi"));

                            pelanggan.addPemesanan(new Pemesanan(
                                rs2.getInt("id"),
                                rs2.getInt("nomor_pemesanan"),
                                jadwal,
                                rs2.getInt("jumlah_pemesanan"),
                                rs2.getString("metode_pembayaran"),
                                rs2.getFloat("total_bayar"),
                                pelanggan));
                        }
                    }
                }
                list.add(pelanggan);
            }
        }
        return list;
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