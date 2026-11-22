package main.java.controller;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import main.java.database.*;
import main.java.model.*;

public class JadwalFilmTableController {
    @FXML private TextField kodeJadwalField;
    @FXML private ComboBox<Bioskop> bioskopComboBox;
    @FXML private ComboBox<Studio> studioComboBox;
    @FXML private ComboBox<Film> filmComboBox;
    @FXML private DatePicker tanggalPicker;
    @FXML private TextField jamField;
    @FXML private TextField kapasitasField;
    @FXML private TableView<JadwalFilm> jadwalTable;
    
    @FXML private TableColumn<JadwalFilm, String> bioskopColumn;
    @FXML private TableColumn<JadwalFilm, String> studioColumn;
    @FXML private TableColumn<JadwalFilm, String> filmColumn;

    private final JadwalFilmDAO jadwalDAO = new JadwalFilmDAO();
    private final BioskopDAO bioskopDAO = new BioskopDAO();
    private final StudioDAO studioDAO = new StudioDAO();
    private final FilmDAO filmDAO = new FilmDAO();

    private List<Studio> allStudios;

    public void refreshTable() {
        try {
            jadwalTable.setItems(FXCollections.observableArrayList(jadwalDAO.getAllJadwalFilm()));
            bioskopComboBox.setItems(FXCollections.observableArrayList(bioskopDAO.getBioskopWithStudio()));
            filmComboBox.setItems(FXCollections.observableArrayList(filmDAO.getAllFilm()));
            allStudios = studioDAO.getAllStudio();
        } catch (SQLException e) {
            showError("Database Error", e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        try {
            List<Bioskop> listBioskop = bioskopDAO.getBioskopWithStudio();

            if (listBioskop == null || listBioskop.isEmpty()) {
                showAlert("Perhatian", "Belum ada Bioskop atau Studio yang terdaftar! Silakan input data Bioskop dan Studio terlebih dahulu.");
            }
            
            bioskopComboBox.setItems(FXCollections.observableArrayList(listBioskop));

            // Listener: Jika bioskop dipilih, otomatis load studio yang ada di bioskop itu
            bioskopComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    studioComboBox.setItems(FXCollections.observableArrayList(newVal.getListStudio()));
                } else {
                    studioComboBox.getItems().clear();
                }
            });

        } catch (SQLException e) {
            showError("Database Error", e.getMessage());
            return;
        }

        if (bioskopColumn != null) {
            bioskopColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStudio().getBioskop().getNamaBioskop()));
        }
        if (studioColumn != null) {
            studioColumn.setCellValueFactory(cell -> new SimpleStringProperty("Studio " + cell.getValue().getStudio().getNomorStudio() + " (" + cell.getValue().getStudio().getJenisStudio() + ")"));
        }
        if (filmColumn != null) {
            filmColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFilm().getJudul()));
        }

        // Batasi tanggal tidak boleh memilih tanggal sebelum hari ini
        tanggalPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        refreshTable();

        // Listener saat baris tabel diklik untuk Edit
        jadwalTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                kodeJadwalField.setText(newVal.getKodeJadwal());
                bioskopComboBox.setValue(newVal.getStudio().getBioskop());
                studioComboBox.setValue(newVal.getStudio());
                filmComboBox.setValue(newVal.getFilm());
                tanggalPicker.setValue(newVal.getTanggal());
                jamField.setText(newVal.getJam().toString());
                kapasitasField.setText(String.valueOf(newVal.getKapasitas()));
            } else {
                clearForm();
            }
        }); 
    }

    // Method untuk memfilter studio berdasarkan bioskop yang dipilih
    private void loadStudioByBioskop(int bioskopId) {
        try {
            List<Studio> listStudio = studioDAO.getStudioByBioskopId(bioskopId);
            if (studioComboBox != null) {
                studioComboBox.setItems(FXCollections.observableArrayList(listStudio));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method untuk mengecek apakah jadwal bentrok di studio dan tanggal yang sama
    private boolean isJadwalTaken(Studio newStudio, LocalDate newTanggal, LocalTime newJam, int newDurasiMenit, int excludeId) {
        try {
            // Ambil semua data jadwal terbaru dari database
            List<JadwalFilm> listJadwal = jadwalDAO.getAllJadwalFilm();
            
            LocalTime newJamSelesai = newJam.plusMinutes(newDurasiMenit);

            for (JadwalFilm j : listJadwal) {
                // Jika sedang mode edit, abaikan jadwal milik dirinya sendiri
                if (j.getId() == excludeId) continue;

                // Cek apakah studio dan tanggalnya sama
                if (j.getStudio().getId() == newStudio.getId() && j.getTanggal().equals(newTanggal)) {
                    LocalTime existingJamMulai = j.getJam();
                    int existingDurasi = j.getFilm().getDurasi();
                    LocalTime existingJamSelesai = existingJamMulai.plusMinutes(existingDurasi);

                    // Rumus irisan waktu (Overlap check):
                    // (MulaiBaru < SelesaiLama) DAN (SelesaiBaru > MulaiLama)
                    if (newJam.isBefore(existingJamSelesai) && newJamSelesai.isAfter(existingJamMulai)) {
                        return true; // Bentrok
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private JadwalFilm readFormFields(int existingKursiTerisi) {
        String kode = kodeJadwalField.getText().trim();
        Studio studio = studioComboBox.getValue();
        Film film = filmComboBox.getValue();
        LocalDate tanggal = tanggalPicker.getValue();
        LocalTime jam = LocalTime.parse(jamField.getText().trim());
        int kapasitas = Integer.parseInt(kapasitasField.getText().trim());

        return new JadwalFilm(-1, kode, studio, film, tanggal, jam, kapasitas, existingKursiTerisi);
    }

    @FXML
    private void handleSubmit() {
        if (kodeJadwalField.getText().trim().isEmpty() || studioComboBox.getValue() == null || filmComboBox.getValue() == null || tanggalPicker.getValue() == null || jamField.getText().trim().isEmpty()) {
            showAlert("Warning", "Lengkapi semua field jadwal film!");
            return;
        }

        LocalDate tanggalPilihan = tanggalPicker.getValue();
        // Pastikan tidak bisa input tanggal masa lalu lewat ketikan/bypass
        if (tanggalPilihan.isBefore(LocalDate.now())) {
            showAlert("Warning", "Tidak dapat memilih tanggal di masa lalu (sebelum hari ini)!");
            return;
        }

        boolean kodeAda = jadwalTable.getItems().stream()
                .anyMatch(j -> j.getKodeJadwal().equalsIgnoreCase(kodeJadwalField.getText().trim()));
        if (kodeAda) {
            showAlert("Warning", "Kode jadwal sudah dipakai!");
            return;
        }

        try {
            Studio studio = studioComboBox.getValue();
            Film film = filmComboBox.getValue();
            LocalTime jam = LocalTime.parse(jamField.getText().trim());

            // Cek Bentrok Jadwal (-1 artinya data baru, belum punya ID)
            if (isJadwalTaken(studio, tanggalPilihan, jam, film.getDurasi(), -1)) {
                showAlert("Jadwal Bentrok", "Studio ini sudah terisi pada rentang jam tersebut! Silakan pilih jam atau studio lain.");
                return;
            }

            JadwalFilm jadwalBaru = readFormFields(0);
            jadwalDAO.saveJadwalFilm(jadwalBaru);
            
            refreshTable();
            clearForm();
            showAlert("Sukses", "Jadwal film berhasil ditambahkan!");
        } catch (NumberFormatException nfe) {
            showAlert("Format Salah", "Kapasitas harus berupa angka!");
        } catch (DateTimeParseException dtpe) {
            showAlert("Format Salah", "Format jam harus HH:mm (Contoh: 14:30)");
        } catch (Exception e) {
            showError("Submit Error", e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        JadwalFilm selected = jadwalTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Pilih jadwal yang ingin di-update.");
            return;
        }

        LocalDate tanggalPilihan = tanggalPicker.getValue();
        // Pastikan tidak bisa input tanggal masa lalu lewat ketikan/bypass
        if (tanggalPilihan.isBefore(LocalDate.now())) {
            showAlert("Warning", "Tidak dapat memilih tanggal di masa lalu (sebelum hari ini)!");
            return;
        }

        int kapasitasBaru = Integer.parseInt(kapasitasField.getText().trim());
        if (kapasitasBaru < selected.getKursiTerisi()) {
            showAlert("Warning", "Kapasitas baru (" + kapasitasBaru + ") tidak boleh lebih kecil dari kursi yang sudah terisi (" + selected.getKursiTerisi() + ")!");
            return;
        }


        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Yakin ingin mengupdate jadwal?", ButtonType.YES, ButtonType.NO);
        confirm.getDialogPane().getStylesheets().add(getClass().getResource("/main/resources/css/style.css").toExternalForm());
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            try {
                Studio studio = studioComboBox.getValue();
                Film film = filmComboBox.getValue();
                LocalTime jam = LocalTime.parse(jamField.getText().trim());
                 // Cek Bentrok Jadwal, kirim ID jadwal yang diedit 
                if (isJadwalTaken(studio, tanggalPilihan, jam, film.getDurasi(), selected.getId())) {
                    showAlert("Jadwal Bentrok", "Studio ini sudah terisi pada rentang jam tersebut! Silakan pilih jam atau studio lain.");
                    return;
                }
                jadwalDAO.updateJadwalFilm(readFormFields(selected.getKursiTerisi()), selected.getId());
                refreshTable();
                clearForm();
                showAlert("Sukses", "Jadwal film berhasil diupdate!");
            } catch (Exception e) {
                showError("Update Error", e.getMessage());
            }
        }
    }

    @FXML
    private void handleDelete() {
        JadwalFilm selected = jadwalTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Pilih jadwal yang ingin dihapus.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Yakin ingin menghapus jadwal?", ButtonType.YES, ButtonType.NO);
        confirm.getDialogPane().getStylesheets().add(getClass().getResource("/main/resources/css/style.css").toExternalForm());
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            try {
                jadwalDAO.deleteJadwalFilm(selected.getId());
                refreshTable();
                clearForm();
                showAlert("Sukses", "Jadwal Film berhasil dihapus!");
            } catch (Exception e) {
                showError("Delete Error", "Jadwal ini sudah memiliki pesanan!");
            }
        }
    }

    @FXML
    private void clearForm() {
        kodeJadwalField.clear();
        bioskopComboBox.setValue(null);
        studioComboBox.getItems().clear();
        filmComboBox.setValue(null);
        tanggalPicker.setValue(null);
        jamField.clear();
        kapasitasField.clear();
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