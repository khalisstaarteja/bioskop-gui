package main.java.controller;

import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import main.java.database.BioskopDAO;
import main.java.database.StudioDAO;
import main.java.model.Bioskop;
import main.java.model.Studio;

public class BioskopTableController {
    @FXML
    private TextField kodeBioskopField;
    @FXML
    private TextField namaBioskopField;
    @FXML
    private TextField lokasiField;
    @FXML
    private TableView<Bioskop> bioskopTable;

    private final BioskopDAO bioskopDAO = new BioskopDAO();
    private final StudioDAO studioDAO = new StudioDAO();

    private void refreshTable() {
        try {
            bioskopTable.setItems(FXCollections.observableArrayList(bioskopDAO.getAllBioskop()));
        } catch (SQLException e) {
            showError("Database Error", e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        refreshTable();

        bioskopTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                kodeBioskopField.setText(newVal.getKodeBioskop());
                namaBioskopField.setText(newVal.getNamaBioskop());
                lokasiField.setText(newVal.getLokasi());
            } else {
                clearForm();
            }
        });
    }

    private Bioskop readFormFields() {
        String kode = kodeBioskopField.getText().trim();
        String nama = namaBioskopField.getText().trim();
        String lokasi = lokasiField.getText().trim();
        return new Bioskop(0, kode, nama, lokasi);
    }

    @FXML
    private void handleSubmit() {
        if (kodeBioskopField.getText().trim().isEmpty() || namaBioskopField.getText().trim().isEmpty()) {
            showAlert("Warning", "Kode dan Nama Bioskop tidak boleh kosong!");
            return;
        }

        // Validasi Kode Bioskop unik
        boolean kodeAda = bioskopTable.getItems().stream()
                .anyMatch(b -> b.getKodeBioskop().equalsIgnoreCase(kodeBioskopField.getText().trim()));
        if (kodeAda) {
            showAlert("Warning", "Kode bioskop sudah dipakai! Gunakan kode lain.");
            return;
        }

        try {
            bioskopDAO.saveBioskop(readFormFields());
            refreshTable();
            clearForm();
        } catch (Exception e) {
            showError("Submit Error", e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        Bioskop selected = bioskopTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Pilih data bioskop yang ingin di-update.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Yakin ingin mengupdate data?", ButtonType.YES,
                ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            try {
                bioskopDAO.updateBioskop(readFormFields(), selected.getId());
                refreshTable();
                clearForm();
            } catch (Exception e) {
                showError("Update Error", e.getMessage());
            }
        }
    }

    @FXML
    private void handleDelete() {
        Bioskop selected = bioskopTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Pilih data bioskop yang ingin dihapus.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Yakin ingin menghapus data?", ButtonType.YES,
                ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            try {
                bioskopDAO.deleteBioskop(selected.getId());
                refreshTable();
                clearForm();
            } catch (Exception e) {
                showError("Delete Error", e.getMessage());
            }
        }
    }

    @FXML
    private void handleViewDetails() {
        Bioskop selected = bioskopTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Pilih data bioskop yang ingin dilihat detail studionya.");
            return;
        }

        TableView<Studio> studioTable = new TableView<>();

        TableColumn<Studio, String> colKodeStudio = new TableColumn<>("Kode Studio");
        colKodeStudio.setCellValueFactory(new PropertyValueFactory<>("kodeStudio"));

        TableColumn<Studio, Integer> colNomorStudio = new TableColumn<>("Nomor");
        colNomorStudio.setCellValueFactory(new PropertyValueFactory<>("nomorStudio"));

        TableColumn<Studio, String> colJenisStudio = new TableColumn<>("Jenis");
        colJenisStudio.setCellValueFactory(new PropertyValueFactory<>("jenisStudio"));

        TableColumn<Studio, Float> colHarga = new TableColumn<>("Harga");
        colHarga.setCellValueFactory(new PropertyValueFactory<>("harga"));

        studioTable.getColumns().addAll(colKodeStudio, colNomorStudio, colJenisStudio, colHarga);
        studioTable.setPlaceholder(new Label("Belum ada studio di bioskop ini."));
        studioTable.setPrefSize(450, 250);

        try {
            studioTable.setItems(FXCollections.observableArrayList(
                    studioDAO.getStudioByBioskopId(selected.getId())));
        } catch (SQLException e) {
            showError("Database Error", e.getMessage());
            return;
        }

        Alert detail = new Alert(Alert.AlertType.INFORMATION);
        detail.setTitle("Bioskop Details");
        detail.setHeaderText(selected.getKodeBioskop() + " - " + selected.getNamaBioskop()
                + "\n" + selected.getLokasi());
        Label teksAtas = new Label("Studio di bioskop ini:");
        teksAtas.setStyle("-fx-font-weight: bold;");
        VBox content = new VBox(10, teksAtas, studioTable);
        detail.getDialogPane().setContent(content);
        detail.showAndWait();
    }

    private void clearForm() {
        kodeBioskopField.clear();
        namaBioskopField.clear();
        lokasiField.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}