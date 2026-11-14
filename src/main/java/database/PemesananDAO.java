package main.java.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import main.java.model.Bioskop;
import main.java.model.Film;
import main.java.model.JadwalFilm;
import main.java.model.Pemesanan;
import main.java.model.Studio;
import main.java.model.User;

public class PemesananDAO {
    // Ambil semua data pemesanan beserta relasi User dan JadwalFilm (Khusus Admin)
    public List<Pemesanan> getAllPemesanan() throws SQLException {
        List<Pemesanan> pemesananList = new ArrayList<>();
        String sql = "SELECT p.id AS pemesanan_id, p.nomor_pemesanan, p.jumlah_pemesanan, p.metode_pembayaran, p.total_bayar, " +
                     "u.id AS user_id, u.nama, u.password, u.role, " +
                     "j.id AS jadwal_id, j.kode_jadwal, j.tanggal, j.jam, j.kapasitas, j.kursi_terisi, " +
                     "s.id AS studio_id, s.kode_studio, s.nomor_studio, s.jenis_studio, s.harga, " +
                     "b.id AS bioskop_id, b.kode_bioskop, b.nama_bioskop, b.lokasi, " +
                     "f.id AS film_id, f.kode_film, f.judul, f.genre, f.durasi, f.poster " +
                     "FROM pemesanan p " +
                     "JOIN users u ON p.user_id = u.id " +
                     "JOIN jadwal_film j ON p.jadwal_id = j.id " +
                     "JOIN studio s ON j.studio_id = s.id " +
                     "JOIN bioskop b ON s.bioskop_id = b.id " +
                     "JOIN film f ON j.film_id = f.id " + // Spasi aman di sini
                     "ORDER BY p.id DESC";

        try (Connection conn = PostgreSQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("pemesanan_id");
                int nomorPemesanan = rs.getInt("nomor_pemesanan");
                int jumlahPemesanan = rs.getInt("jumlah_pemesanan");
                String metodePembayaran = rs.getString("metode_pembayaran");
                float totalBayar = rs.getFloat("total_bayar");

                // Rekonstruksi objek User
                User user = new User(
                    rs.getInt("user_id"),
                    rs.getString("nama"),
                    rs.getString("password"),
                    rs.getString("role")
                ) {
                    @Override
                    public String showMenu() { return null; }
                };

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

                // Rekonstruksi objek JadwalFilm
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

                pemesananList.add(new Pemesanan(id, nomorPemesanan, jadwal, jumlahPemesanan, metodePembayaran, totalBayar, user));
            }
        }
        return pemesananList;
    }

    // Ambil data pemesanan khsusu untuk Pelanggan tertentu berdasarkan user_id (Riwayat)
    public List<Pemesanan> getPemesananByUserId(int userId) throws SQLException {
        List<Pemesanan> pemesananList = new ArrayList<>();
        String sql = "SELECT p.id AS pemesanan_id, p.nomor_pemesanan, p.jumlah_pemesanan, p.metode_pembayaran, p.total_bayar, " +
                     "u.id AS user_id, u.nama, u.password, u.role, " +
                     "j.id AS jadwal_id, j.kode_jadwal, j.tanggal, j.jam, j.kapasitas, j.kursi_terisi, " +
                     "s.id AS studio_id, s.kode_studio, s.nomor_studio, s.jenis_studio, s.harga, " +
                     "b.id AS bioskop_id, b.kode_bioskop, b.nama_bioskop, b.lokasi, " +
                     "f.id AS film_id, f.kode_film, f.judul, f.genre, f.durasi, f.poster " +
                     "FROM pemesanan p " +
                     "JOIN users u ON p.user_id = u.id " +
                     "JOIN jadwal_film j ON p.jadwal_id = j.id " +
                     "JOIN studio s ON j.studio_id = s.id " +
                     "JOIN bioskop b ON s.bioskop_id = b.id " +
                     "JOIN film f ON j.film_id = f.id " +
                     "WHERE p.user_id = ? " +
                     "ORDER BY p.id DESC";

        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("pemesanan_id");
                    int nomorPemesanan = rs.getInt("nomor_pemesanan");
                    int jumlahPemesanan = rs.getInt("jumlah_pemesanan");
                    String metodePembayaran = rs.getString("metode_pembayaran");
                    float totalBayar = rs.getFloat("total_bayar");

                    User user = new User(
                        rs.getInt("user_id"),
                        rs.getString("nama"),
                        rs.getString("password"),
                        rs.getString("role")
                    ) {
                        @Override
                        public String showMenu() { return null; }
                    };

                    Bioskop bioskop = new Bioskop(
                        rs.getInt("bioskop_id"),
                        rs.getString("kode_bioskop"),
                        rs.getString("nama_bioskop"),
                        rs.getString("lokasi")
                    );

                    Studio studio = new Studio(
                        rs.getInt("studio_id"),
                        rs.getString("kode_studio"),
                        rs.getInt("nomor_studio"),
                        rs.getString("jenis_studio"),
                        bioskop,
                        rs.getFloat("harga")
                    );

                    Film film = new Film(
                        rs.getInt("film_id"),
                        rs.getString("kode_film"),
                        rs.getString("judul"),
                        rs.getString("genre"),
                        rs.getInt("durasi"),
                        rs.getBytes("poster")
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

                    pemesananList.add(new Pemesanan(id, nomorPemesanan, jadwal, jumlahPemesanan, metodePembayaran, totalBayar, user));
                }
            }
        }
        return pemesananList;
    }

    // Simpan data pemesanan baru ke database
    public void savePemesanan(Pemesanan p) throws SQLException {
        String updateKursi = "UPDATE bioskop.jadwal_film SET kursi_terisi = kursi_terisi + ? " +
                             "WHERE id = ? AND kursi_terisi + ? <= kapasitas";
        String insert = "INSERT INTO bioskop.pemesanan " +
                        "(nomor_pemesanan, user_id, jadwal_id, jumlah_pemesanan, metode_pembayaran, total_bayar, waktu) " +
                        "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
    
        try (Connection conn = PostgreSQLConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement upd = conn.prepareStatement(updateKursi)) {
                upd.setInt(1, p.getJumlahPemesanan());
                upd.setInt(2, p.getJadwal().getId());
                upd.setInt(3, p.getJumlahPemesanan());
    
                if (upd.executeUpdate() == 0) {
                    conn.rollback();
                    throw new SQLException("Kursi tidak mencukupi atau jadwal sudah penuh.");
                }
    
                try (PreparedStatement ins = conn.prepareStatement(insert)) {
                    ins.setInt(1, p.getNomorPemesanan());
                    ins.setInt(2, p.getUser().getId());
                    ins.setInt(3, p.getJadwal().getId());
                    ins.setInt(4, p.getJumlahPemesanan());
                    ins.setString(5, p.getMetodePembayaran());
                    ins.setInt(6, (int) p.getTotalBayar());
                    ins.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
    // Update data pemesanan berdasarkan ID
    public void updatePemesanan(Pemesanan pemesanan, int id) throws SQLException {
        String sql = "UPDATE pemesanan SET nomor_pemesanan = ?, user_id = ?, jadwal_id = ?, jumlah_pemesanan = ?, metode_pembayaran = ?, total_bayar = ? WHERE id = ?";

        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, pemesanan.getNomorPemesanan());
            pstmt.setInt(2, pemesanan.getUser().getId());
            pstmt.setInt(3, pemesanan.getJadwal().getId());
            pstmt.setInt(4, pemesanan.getJumlahPemesanan());
            pstmt.setString(5, pemesanan.getMetodePembayaran());
            pstmt.setFloat(6, pemesanan.getTotalBayar());
            pstmt.setInt(7, id);

            pstmt.executeUpdate();
        }
    }

    // Hapus data pemesanan berdasarkan ID
    public void deletePemesanan(int id) throws SQLException {
        String sql = "WITH del AS (DELETE FROM pemesanan WHERE id = ? RETURNING jadwal_id, jumlah_pemesanan) " +
                     "UPDATE jadwal_film j SET kursi_terisi = j.kursi_terisi - del.jumlah_pemesanan " +
                     "FROM del WHERE j.id = del.jadwal_id";
    
        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    // Ambil total pendapatan
    public double getTotalPendapatan() throws SQLException {
        String sql = "SELECT SUM(total_bayar) AS total FROM pemesanan";
        try (Connection conn = PostgreSQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble("total");
            }
        }
        return 0.0;
    }

    // Ambil total tiket yang berhasil terjual
    public int getTotalTiketTerjual() throws SQLException {
        String sql = "SELECT SUM(jumlah_pemesanan) AS total_tiket FROM pemesanan";
        try (Connection conn = PostgreSQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("total_tiket");
            }
        }
        return 0;
    }

    public int generateNomorPemesanan() throws SQLException {
        String sql = "SELECT 1 FROM pemesanan WHERE nomor_pemesanan = ?";
        try (Connection conn = PostgreSQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            while (true) {
                int nomor = (int) (Math.random() * 90000) + 10000;
                pstmt.setInt(1, nomor);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next()) return nomor;
                }
            }
        }
    }
}