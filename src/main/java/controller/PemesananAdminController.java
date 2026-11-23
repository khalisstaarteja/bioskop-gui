package main.java.controller;

import java.sql.SQLException;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import main.java.database.PemesananDAO;
import main.java.model.Bioskop;
import main.java.model.Film;
import main.java.model.JadwalFilm;
import main.java.model.Pemesanan;

public class PemesananAdminController {
    @FXML private TableView<Pemesanan> pemesananTable;
    @FXML private TableColumn<Pemesanan, String> idColumn;
    @FXML private TableColumn<Pemesanan, String> pelangganColumn;
    @FXML private TableColumn<Pemesanan, String> filmColumn;
    @FXML private TableColumn<Pemesanan, String> jadwalColumn;
    @FXML private TableColumn<Pemesanan, Integer> jumlahColumn;
    @FXML private TableColumn<Pemesanan, String> totalColumn;
    @FXML private TableColumn<Pemesanan, String> metodeColumn;
    @FXML private TableColumn<Pemesanan, String> bioskopColumn;
    @FXML private TableColumn<Pemesanan, String> studioColumn;

    @FXML private ComboBox<Bioskop> bioskopComboBox;
    @FXML private ComboBox<Film> filmComboBox;
    @FXML private ComboBox<JadwalFilm> jadwalComboBox;
    @FXML private TextField jumlahTiketField;
    @FXML private ComboBox<String> metodePembayaranComboBox;
    @FXML private Label totalHargaLabel;

    private final PemesananDAO pemesananDAO = new PemesananDAO();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(c ->
            new SimpleStringProperty(String.valueOf(c.getValue().getNomorPemesanan())));
        pelangganColumn.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getUser().getNama()));
        filmColumn.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getJadwal().getFilm().getJudul()));
        bioskopColumn.setCellValueFactory(c ->
            new SimpleStringProperty(
                c.getValue().getJadwal().getStudio().getBioskop().getNamaBioskop()));
        studioColumn.setCellValueFactory(c ->
            new SimpleStringProperty(
            "Studio " + c.getValue().getJadwal().getStudio().getNomorStudio()));
        jadwalColumn.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getJadwal().getTanggal() + " | " + c.getValue().getJadwal().getJam()));
        jumlahColumn.setCellValueFactory(c ->
            c.getValue().jumlahPemesananProperty().asObject());
        totalColumn.setCellValueFactory(c ->
            new SimpleStringProperty("Rp " + String.format("%,.0f", c.getValue().getTotalBayar())));
        metodeColumn.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getMetodePembayaran()));

        loadData();
    }

    private void loadData() {
        try {
            List<Pemesanan> list = pemesananDAO.getAllPemesanan();
            pemesananTable.setItems(FXCollections.observableArrayList(list));
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Gagal memuat data: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void clearForm() {
        if (bioskopComboBox != null) bioskopComboBox.getSelectionModel().clearSelection();
        if (filmComboBox != null) filmComboBox.getSelectionModel().clearSelection();
        if (jadwalComboBox != null) jadwalComboBox.getSelectionModel().clearSelection();
        if (metodePembayaranComboBox != null) metodePembayaranComboBox.getSelectionModel().clearSelection();
        if (jumlahTiketField != null) jumlahTiketField.clear();
        if (totalHargaLabel != null) totalHargaLabel.setText("Rp 0");
    }

    // Delete
    @FXML
    private void handleDelete() {
        Pemesanan selected = pemesananTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Pilih data pemesanan yang ingin dihapus.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Yakin ingin menghapus data?",
                ButtonType.YES, ButtonType.NO);
        confirm.getDialogPane().getStylesheets()
                .add(getClass().getResource("/main/resources/css/style.css").toExternalForm());
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            try {
                pemesananDAO.deletePemesanan(selected.getId());
                loadData();
                clearForm();
                showAlert("Sukses", "Pemesanan berhasil dihapus!");
            } catch (Exception e) {
                e.printStackTrace();
                showError("Delete Error", "Pemesanan gagal dihapus: " + e.getMessage());
            }
        }
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