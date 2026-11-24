package main.java.controller;

import java.util.List;
import java.sql.SQLException;
import java.time.LocalDate;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import main.java.database.AdminDAO;
import main.java.database.PelangganDAO;
import main.java.database.UserDAO;
import main.java.database.PemesananDAO;
import main.java.model.Admin;
import main.java.model.Pelanggan;
import main.java.model.Pemesanan;
import main.java.model.User;

public class UserTableController {
    @FXML
    private TextField namaField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private VBox dynamicFormContainer;
    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, String> extraColumn;

    // Field dinamis yang dimasukkan ke dalam dynamicFormContainer
    private TextField usernameField;
    private TextField emailField;
    private final Label dynamicLabel = new Label();
    private final TextField dynamicTextField = new TextField();

    private final UserDAO userDAO = new UserDAO();
    private final AdminDAO adminDAO = new AdminDAO();
    private final PelangganDAO pelangganDAO = new PelangganDAO();
    private final PemesananDAO pemesananDAO = new PemesananDAO();
    private User currentUser;

    public void initUser(User user) {
        this.currentUser = user;
    }

    private void refreshTable() {
        try {
            userTable.setItems(FXCollections.observableArrayList(userDAO.getAllUser()));
        } catch (SQLException e) {
            showError("Database Error", e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList("Admin", "Pelanggan"));
        roleComboBox.setValue("Pelanggan");

        if (extraColumn != null) {
            extraColumn.setCellValueFactory(cellData -> {
                User u = cellData.getValue();
                if (u instanceof Admin) {
                    return new SimpleStringProperty("Username: " + ((Admin) u).getUsername());
                } else if (u instanceof Pelanggan) {
                    return new SimpleStringProperty("Email: " + ((Pelanggan) u).getEmail());
                }
                return new SimpleStringProperty("-");
            });
        }

        refreshTable();
        createExtraField(roleComboBox.getValue(), null);

        roleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            createExtraField(newVal, null);
        });

       userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                namaField.setText(newVal.getNama());
                passwordField.clear(); // kosongkan, jangan tampilkan hash
                passwordField.setPromptText("Kosongkan jika tidak ingin mengubah password");
                roleComboBox.setValue(newVal.getRole());
                createExtraField(newVal.getRole(), newVal);
            } else {
                clearForm();
            }
        });
    }

    // Extra field sesuai role (Admin = username, Pelanggan = email)
    private void createExtraField(String selectedRole, User u) {
        dynamicFormContainer.getChildren().clear();

        if (selectedRole == null) {
            return;
        }

        selectedRole = selectedRole.trim();

        if ("Admin".equals(selectedRole)) {
            Label label = new Label("Username Admin: ");
            usernameField = new TextField();
            if (u instanceof Admin) {
                usernameField.setText(((Admin) u).getUsername());
            }
            dynamicFormContainer.getChildren().add(new HBox(10, label, usernameField));
        } else if ("Pelanggan".equals(selectedRole)) {
            Label label = new Label("Email Pelanggan: ");
            emailField = new TextField();
            if (u instanceof Pelanggan) {
                emailField.setText(((Pelanggan) u).getEmail());
            }
            dynamicFormContainer.getChildren().add(new HBox(10, label, emailField));
        }

        dynamicFormContainer.setVisible(true);
        dynamicFormContainer.setManaged(true);
    }

    private String pesanValidasi(String role) {
        String detail = "Admin".equals(role)
                ? "Username 4-20 karakter, diawali huruf, hanya huruf/angka/titik/underscore."
                : "Email harus berformat benar (contoh: nama@email.com).";
        return detail + "\n\nPassword 8-20 karakter, mengandung angka, huruf kecil, huruf besar, "
                + "1 karakter spesial (!@#$%^&-+=()), dan tanpa spasi.";
    }

    @FXML
    private void handleSubmit() {
        String role = roleComboBox.getValue();
        String detail = "Admin".equals(role)
                ? usernameField.getText().trim()
                : emailField.getText().trim();
        if (namaField.getText().trim().isEmpty() || passwordField.getText().trim().isEmpty() || detail.isEmpty()) {
            showAlert("Warning", "Nama, Password dan Email/Username tidak boleh kosong!");
            return;
        }

        try {
            String nama = namaField.getText().trim();
            String pass = passwordField.getText().trim();

            User baru = "Admin".equals(role)
                    ? new Admin(-1, nama, pass, "Admin", -1, detail)
                    : new Pelanggan(-1, nama, pass, "Pelanggan", -1, detail);

            if (!baru.validate()) {
                showAlert("Data Tidak Valid", pesanValidasi(role));
                return;
            }

            if ("Admin".equals(role) && userDAO.isUsernameExists(detail, -1)) {
                showAlert("Username Sudah Dipakai",
                        "Username \"" + detail + "\" sudah digunakan. Pilih username lain.");
                return;
            }
            if ("Pelanggan".equals(role) && userDAO.isEmailExists(detail)) {
                showAlert("Email Sudah Dipakai", "Email \"" + detail + "\" sudah terdaftar.");
                return;
            }

            userDAO.saveUser(baru);
            refreshTable();
            clearForm();
            showAlert("Sukses", "User berhasil ditambahkan!");
        } catch (Exception e) {
            showError("Submit Error", e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Pilih data user yang ingin di-update.");
            return;
        }

        String selectedRole = roleComboBox.getValue();
        String detail = "Admin".equals(selectedRole)
                ? usernameField.getText().trim()
                : emailField.getText().trim();
        String nama = namaField.getText().trim();
        String passBaru = passwordField.getText().trim();
        boolean passwordDiubah = !passBaru.isEmpty();

        // Password tidak wajib saat update
        if (nama.isEmpty() || detail.isEmpty()) {
            showAlert("Warning", "Nama dan Email/Username tidak boleh kosong!");
            return;
        }

        // Kalau password tidak diubah, pakai hash lama
        String passUntukValidasi = passwordDiubah ? passBaru : selected.getPassword();
        String passFinal = passUntukValidasi;

        User temp = "Admin".equals(selectedRole)
                ? new Admin(selected.getId(), nama, passUntukValidasi, "Admin", -1, detail)
                : new Pelanggan(selected.getId(), nama, passUntukValidasi, "Pelanggan", -1, detail);

        // Validasi hanya kalau password diubah
        if (passwordDiubah) {
            if (!temp.validate()) {
                showAlert("Data Tidak Valid", pesanValidasi(selectedRole));
                return;
            }
        } else {
            if (!validasiDetailSaja(selectedRole, detail)) {
                showAlert("Data Tidak Valid", pesanDetail(selectedRole));
                return;
            }
        }

        try {
            if ("Admin".equals(selectedRole) && userDAO.isUsernameExists(detail, selected.getId())) {
                showAlert("Username Sudah Dipakai", "Username \"" + detail + "\" sudah digunakan admin lain.");
                return;
            }
            if ("Pelanggan".equals(selectedRole) && !detail.equalsIgnoreCase(((Pelanggan) selected).getEmail())
                    && userDAO.isEmailExists(detail)) {
                showAlert("Email Sudah Dipakai", "Email \"" + detail + "\" sudah terdaftar.");
                return;
            }
        } catch (SQLException e) {
            showError("Database Error", e.getMessage());
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Yakin ingin mengupdate user?",
                ButtonType.YES, ButtonType.NO);
        confirm.getDialogPane().getStylesheets()
                .add(getClass().getResource("/main/resources/css/style.css").toExternalForm());
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            try {
                selected.setNama(nama);
                selected.setPassword(passFinal);
                selected.setRole(selectedRole);

                if (selected instanceof Admin && "Admin".equalsIgnoreCase(selectedRole)) {
                    ((Admin) selected).setUsername(detail);
                } else if (selected instanceof Pelanggan && "Pelanggan".equalsIgnoreCase(selectedRole)) {
                    ((Pelanggan) selected).setEmail(detail);
                }

                userDAO.updateUser(selected, selected.getId());
                refreshTable();
                clearForm();
                showAlert("Sukses", "User berhasil diupdate!");
            } catch (Exception e) {
                showError("Update Error", e.getMessage());
            }
        }
    }

    private boolean validasiDetailSaja(String role, String detail) {
        if ("Admin".equals(role)) {
            return detail.matches("^[A-Za-z][A-Za-z0-9._]{3,19}$");
        }
        return detail.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    }

    private String pesanDetail(String role) {
        return "Admin".equals(role)
                ? "Username 4-20 karakter, diawali huruf, hanya huruf/angka/titik/underscore."
                : "Email harus berformat benar (contoh: nama@email.com).";
    }
    @FXML
    private void handleDelete() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Pilih data user yang ingin dihapus.");
            return;
        }
        if (currentUser != null && selected.getId() == currentUser.getId()) {
            showAlert("Tidak Bisa", "Kamu tidak bisa menghapus akun yang sedang kamu pakai.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Yakin ingin menghapus user?", ButtonType.YES,
                ButtonType.NO);
        confirm.getDialogPane().getStylesheets()
                .add(getClass().getResource("/main/resources/css/style.css").toExternalForm());
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            try {
                userDAO.deleteUser(selected.getId());
                refreshTable();
                clearForm();
                showAlert("Sukses", "User berhasil dihapus!");
            } catch (Exception e) {
                showError("Delete Error", "User ini memiliki pesanan!");
            }
        }
    }

    @FXML
    private void handleViewDetails() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Pilih data pelanggan yang ingin dilihat detail pesanannya.");
            return;
        }
        if (!(selected instanceof Pelanggan)) {
            showAlert("Info", "Admin tidak memiliki data pesanan. Pilih user dengan role Pelanggan.");
            return;
        }

        List<Pemesanan> pesanan;
        try {
            pesanan = pelangganDAO.getAllPelangganWithPemesanan().stream()
                    .filter(p -> p.getId() == selected.getId())
                    .findFirst()
                    .map(p -> (List<Pemesanan>) p.getListPemesanan())
                    .orElse(List.of());
        } catch (SQLException e) {
            showError("Database Error", e.getMessage());
            return;
        }

        TableView<Pemesanan> detailTable = new TableView<>();

        TableColumn<Pemesanan, String> colNo = new TableColumn<>("No. Pesanan");
        colNo.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getNomorPemesanan())));

        TableColumn<Pemesanan, String> colFilm = new TableColumn<>("Film");
        colFilm.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getJadwal().getFilm().getJudul()));

        TableColumn<Pemesanan, String> colBioskop = new TableColumn<>("Bioskop");
        colBioskop.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().getJadwal().getStudio().getBioskop().getNamaBioskop()));

        TableColumn<Pemesanan, String> colJadwal = new TableColumn<>("Jadwal Tayang");
        colJadwal.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getJadwal().getTanggal()
                + " | " + c.getValue().getJadwal().getJam()));

        TableColumn<Pemesanan, String> colJumlah = new TableColumn<>("Jumlah");
        colJumlah.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getJumlahPemesanan())));

        TableColumn<Pemesanan, String> colTotal = new TableColumn<>("Total Bayar");
        colTotal.setCellValueFactory(
                c -> new SimpleStringProperty("Rp " + String.format("%,.0f", c.getValue().getTotalBayar())));

        TableColumn<Pemesanan, String> colMetode = new TableColumn<>("Pembayaran");
        colMetode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMetodePembayaran()));

        detailTable.getColumns().addAll(colNo, colFilm, colBioskop, colJadwal, colJumlah, colTotal, colMetode);
        detailTable.setPlaceholder(new Label("Pelanggan ini belum memiliki pesanan."));
        detailTable.setPrefSize(700, 250);
        detailTable.setItems(FXCollections.observableArrayList(pesanan));

        Alert detail = new Alert(Alert.AlertType.INFORMATION);
        detail.setTitle("Detail Pesanan");
        detail.setHeaderText("Pelanggan: " + selected.getNama()
                + " \nEmail: " + ((Pelanggan) selected).getEmail());

        Label teksAtas = new Label("Daftar pesanan pelanggan ini:");
        teksAtas.setStyle("-fx-font-weight: bold;");

        detail.getDialogPane().setContent(new VBox(10, teksAtas, detailTable));
        detail.getDialogPane().getStylesheets()
                .add(getClass().getResource("/main/resources/css/style.css").toExternalForm());
        detail.showAndWait();
    }

    @FXML
    private void clearForm() {
        namaField.clear();
        passwordField.clear();
        roleComboBox.setValue("Pelanggan");
        createExtraField("Pelanggan", null);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/main/resources/css/style.css").toExternalForm());
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/main/resources/css/style.css").toExternalForm());
        alert.showAndWait();
    }
}