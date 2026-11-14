package main.java.controller;

import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import main.java.database.BioskopDAO;
import main.java.model.Bioskop;

public class BioskopTableController {
    @FXML private TextField kodeBioskopField;
    @FXML private TextField namaBioskopField;
    @FXML private TextField lokasiField;
    @FXML private TableView<Bioskop> bioskopTable;

    private final BioskopDAO bioskopDAO = new BioskopDAO();

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
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Yakin ingin mengupdate data?", ButtonType.YES, ButtonType.NO);
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
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Yakin ingin menghapus data?", ButtonType.YES, ButtonType.NO);
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