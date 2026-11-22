package main.java.controller;

import java.sql.SQLException;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import main.java.database.PemesananDAO;
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

    private final PemesananDAO pemesananDAO = new PemesananDAO();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(c ->
            new SimpleStringProperty(String.valueOf(c.getValue().getNomorPemesanan())));
        pelangganColumn.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getUser().getNama()));
        filmColumn.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getJadwal().getFilm().getJudul()));
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
}