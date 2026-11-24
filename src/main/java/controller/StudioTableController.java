package main.java.controller;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import main.java.database.BioskopDAO;
import main.java.database.StudioDAO;
import main.java.model.Bioskop;
import main.java.model.Studio;
import main.java.model.JadwalFilm;

public class StudioTableController {
    @FXML private TextField kodeStudioField;
    @FXML private TextField nomorStudioField;
    @FXML private ComboBox<String> jenisStudioComboBox;
    @FXML private TextField hargaField;
    @FXML private ComboBox<Bioskop> bioskopComboBox;
    @FXML private TableView<Studio> studioTable;
    @FXML private TableColumn<Studio, String> bioskopColumn;

    private final StudioDAO studioDAO = new StudioDAO();
    private final BioskopDAO bioskopDAO = new BioskopDAO();

    public void refreshTable() {
        try {
            studioTable.setItems(FXCollections.observableArrayList(studioDAO.getAllStudio()));
            bioskopComboBox.setItems(FXCollections.observableArrayList(bioskopDAO.getAllBioskop()));
        } catch (SQLException e) {
            showError("Database Error", e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        try {
            List<Bioskop> listBioskop = bioskopDAO.getAllBioskop();

            if (listBioskop == null || listBioskop.isEmpty()) {
                // Beritahu admin agar input data dasar terlebih dahulu
                showAlert("Perhatian",
                        "Belum ada Bioskop yang terdaftar! Silakan input data Bioskop dan Studio terlebih dahulu.");
            }

            bioskopComboBox.setItems(FXCollections.observableArrayList(listBioskop));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        jenisStudioComboBox.setItems(FXCollections.observableArrayList("Regular", "IMAX", "Premiere"));
        hargaField.setEditable(false); // Harga otomatis mengikuti jenis studio

        // Listener otomatis set harga sesuai jenis studio
        jenisStudioComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("Regular".equals(newVal)) {
                hargaField.setText("50000");
            } else if ("IMAX".equals(newVal)) {
                hargaField.setText("70000");
            } else if ("Premiere".equals(newVal)) {
                hargaField.setText("90000");
            }
        });

        // Menampilkan nama bioskop pada kolom TableView
        if (bioskopColumn != null) {
            bioskopColumn.setCellValueFactory(
                    cellData -> new SimpleStringProperty(cellData.getValue().getBioskop().getNamaBioskop()));
        }

        refreshTable();

        studioTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                kodeStudioField.setText(newVal.getKodeStudio());
                nomorStudioField.setText(String.valueOf(newVal.getNomorStudio()));
                jenisStudioComboBox.setValue(newVal.getJenisStudio());
                hargaField.setText(String.format("%.0f", newVal.getHarga()));
                bioskopComboBox.setValue(newVal.getBioskop());
            } else {
                clearForm();
            }
        });
    }

    private Studio readFormFields() {
        String kode = kodeStudioField.getText().trim();
        int nomor = Integer.parseInt(nomorStudioField.getText().trim());
        String jenis = jenisStudioComboBox.getValue();
        float harga = Float.parseFloat(hargaField.getText().trim());
        Bioskop bioskop = bioskopComboBox.getValue();

        return new Studio(-1, kode, nomor, jenis, bioskop, harga);
    }

    @FXML
    private void handleSubmit() {
        if (kodeStudioField.getText().trim().isEmpty() || bioskopComboBox.getValue() == null
                || jenisStudioComboBox.getValue() == null) {
            showAlert("Warning", "Lengkapi semua data studio dan pilih bioskop!");
            return;
        }

        boolean kodeAda = studioTable.getItems().stream()
                .anyMatch(s -> s.getKodeStudio().equalsIgnoreCase(kodeStudioField.getText().trim()));
        if (kodeAda) {
            showAlert("Warning", "Kode studio sudah dipakai!");
            return;
        }

        try {
            studioDAO.saveStudio(readFormFields());
            refreshTable();
            clearForm();
            showAlert("Sukses", "Studio berhasil ditambahkan!");
        } catch (Exception e) {
            showError("Submit Error", e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        Studio selected = studioTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Pilih data studio yang ingin di-update.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Yakin ingin mengupdate data?", ButtonType.YES,
                ButtonType.NO);
        confirm.getDialogPane().getStylesheets().add(getClass().getResource("/main/resources/css/style.css").toExternalForm());
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            try {
                studioDAO.updateStudio(readFormFields(), selected.getId());
                refreshTable();
                clearForm();
                showAlert("Sukses", "Studio berhasil diupdate!");
            } catch (Exception e) {
                showError("Update Error", "Kode sudah dipakai / format angka salah!");
            }
        }
    }

    @FXML
    private void handleDelete() {
        Studio selected = studioTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Pilih data studio yang ingin dihapus.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Yakin ingin menghapus data?", ButtonType.YES,
                ButtonType.NO);
                confirm.getDialogPane().getStylesheets().add(getClass().getResource("/main/resources/css/style.css").toExternalForm());
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            try {
                studioDAO.deleteStudio(selected.getId());
                refreshTable();
                clearForm();
                showAlert("Sukses", "Studio berhasil dihapus!");
            } catch (Exception e) {
                showError("Delete Error", "Jadwal film terkait harap dihapus terlebih dahulu!");
            }
        }
    }

    @FXML
    private void handleViewDetails() {
        Studio selected = studioTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Pilih data studio yang ingin dilihat detail jadwal studionya.");
            return;
        }

        TableView<JadwalFilm> jadwalTable = new TableView<>();

        TableColumn<JadwalFilm, String> colKodeJadwal = new TableColumn<>("Kode Jadwal");
        colKodeJadwal.setCellValueFactory(new PropertyValueFactory<>("kodeJadwal"));

        TableColumn<JadwalFilm, String> colFilm = new TableColumn<>("Film");
        colFilm.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFilm().getJudul()));

        TableColumn<JadwalFilm, LocalDate> colTanggal = new TableColumn<>("Tanggal");
        colTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggal"));

        TableColumn<JadwalFilm, LocalTime> colJam = new TableColumn<>("Jam");
        colJam.setCellValueFactory(new PropertyValueFactory<>("jam"));

        TableColumn<JadwalFilm, Integer> colKapasitas = new TableColumn<>("Kapasitas");
        colKapasitas.setCellValueFactory(new PropertyValueFactory<>("kapasitas"));

        TableColumn<JadwalFilm, String> colTerisi = new TableColumn<>("Terisi");
        colTerisi.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getKursiTerisi())));

        jadwalTable.getColumns().addAll(colKodeJadwal, colFilm, colTanggal, colJam, colKapasitas, colTerisi);
        jadwalTable.setPlaceholder(new Label("Studio ini belum dijadwalkan."));
        jadwalTable.setPrefSize(550, 250);

        try {
            List<JadwalFilm> jadwal = studioDAO.getStudioWithJadwal().stream()
                    .filter(s -> s.getId() == selected.getId())
                    .flatMap(s -> s.getListJadwal().stream())
                    .collect(Collectors.toList());

            jadwalTable.setItems(FXCollections.observableArrayList(jadwal));
        } catch (SQLException e) {
            showError("Database Error", e.getMessage());
            return;
        }

        Alert detail = new Alert(Alert.AlertType.INFORMATION);
        detail.setTitle("Studio Details");
        detail.setHeaderText(selected.getKodeStudio() + " - Studio " + selected.getNomorStudio()
                + "\n" + selected.getJenisStudio() + "\nBioskop: "
                + selected.getBioskop().getKodeBioskop()
                + " - " + selected.getBioskop().getNamaBioskop()
                + "\nHarga: " + (int) selected.getHarga());

        Label teksAtas = new Label("Jadwal di studio ini:");
        teksAtas.setStyle("-fx-font-weight: bold;");

        VBox content = new VBox(10, teksAtas, jadwalTable);
        detail.getDialogPane().setContent(content);
        detail.getDialogPane().getStylesheets().add(getClass().getResource("/main/resources/css/style.css").toExternalForm());
        detail.showAndWait();
    }

    @FXML
    private void clearForm() {
        kodeStudioField.clear();
        nomorStudioField.clear();
        jenisStudioComboBox.setValue(null);
        hargaField.clear();
        bioskopComboBox.setValue(null);
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