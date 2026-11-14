package main.java.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import main.java.database.UserDAO;
import main.java.model.Pelanggan;
import main.java.model.User;

public class LoginController {

    // Komponen Form Login
    @FXML private GridPane loginPane;
    @FXML private ComboBox<String> loginRoleComboBox; 
    @FXML private Label identifierLabel;              
    @FXML private TextField usernameOrEmailField;     
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    // Komponen Form Register Pelanggan
    @FXML private GridPane registerPane;
    @FXML private TextField regNamaField;
    @FXML private TextField regEmailField;
    @FXML private PasswordField regPasswordField;
    @FXML private Label regErrorLabel;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        // 1. Masukkan pilihan role ke ComboBox Login
        if (loginRoleComboBox != null) {
            loginRoleComboBox.setItems(FXCollections.observableArrayList("Admin", "Pelanggan"));
        }

        // 2. Ubah label dan prompt text secara dinamis saat role dipilih
        if (loginRoleComboBox != null) {
            loginRoleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if ("Admin".equals(newVal)) {
                    identifierLabel.setText("Username:");
                    usernameOrEmailField.setPromptText("Masukkan username admin");
                } else if ("Pelanggan".equals(newVal)) {
                    identifierLabel.setText("Email:");
                    usernameOrEmailField.setPromptText("Masukkan email pelanggan");
                }
            });
        }
    }

    // Pindah ke form register
    @FXML
    private void showRegisterForm(ActionEvent event) {
        loginPane.setVisible(false);
        registerPane.setVisible(true);
        if (regErrorLabel != null) regErrorLabel.setText("");
    }

    // Kembali ke form login (dengan ActionEvent)
    @FXML
    private void showLoginForm(ActionEvent event) {
        showLoginForm();
    }

    // Kembali ke form login (tanpa parameter, agar bisa dipanggil otomatis)
    private void showLoginForm() {
        registerPane.setVisible(false);
        loginPane.setVisible(true);
        if (errorLabel != null) errorLabel.setText("");
    }

    // Logika Login (Admin & Pelanggan berdasarkan role yang dipilih)
    @FXML
    private void handleLogin(ActionEvent event) {
        String role = loginRoleComboBox.getValue();
        String identifier = usernameOrEmailField.getText().trim(); 
        String password = passwordField.getText().trim();

        if (role == null || identifier.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Silakan pilih role, isi data, dan password terlebih dahulu!");
            return;
        }

        try {
            // Panggil method di UserDAO untuk autentikasi spesifik berdasarkan role
            User user = userDAO.authenticateByRole(role, identifier, password);

            if (user != null) {
                // Login Berhasil -> Buka Dashboard
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/main_layout.fxml"));
                Parent root = loader.load();

                // Kirim data user yang sedang login ke DashboardController
                DashboardController dashboardController = loader.getController();
                dashboardController.setLoggedUser(user); 

                Stage stage = (Stage) usernameOrEmailField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Dashboard Sistem Bioskop");
                stage.show();
            } else {
                errorLabel.setText("Gagal! " + role + " atau Password salah.");
            }
        } catch (SQLException | IOException e) {
            errorLabel.setText("Terjadi kesalahan sistem: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Logika Registrasi Pelanggan Baru
    @FXML
    private void handleRegister(ActionEvent event) {
        String nama = regNamaField.getText().trim();
        String email = regEmailField.getText().trim();
        String pass = regPasswordField.getText().trim();

        if (nama.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            regErrorLabel.setText("Semua field pendaftaran wajib diisi!");
            return;
        }

        try {
            // Cek apakah email sudah terdaftar di sistem
            if (userDAO.isEmailExists(email)) {
                regErrorLabel.setText("Email ini sudah terdaftar! Silahkan gunakan email lain.");
                return;
            }

            // Buat objek Pelanggan baru dengan role "Pelanggan"
            Pelanggan pelangganBaru = new Pelanggan(nama, email, pass, "Pelanggan");
            
            // Simpan ke database melalui UserDAO
            userDAO.saveUser(pelangganBaru);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Registrasi Berhasil");
            alert.setHeaderText(null);
            alert.setContentText("Akun pelanggan berhasil dibuat! Silakan login menggunakan email Anda.");
            alert.showAndWait();

            // Bersihkan form & kembali ke form login
            regNamaField.clear();
            regEmailField.clear();
            regPasswordField.clear();
            showLoginForm();

        } catch (Exception e) {
            regErrorLabel.setText("Gagal mendaftar: " + e.getMessage());
            e.printStackTrace();
        }
    }
}