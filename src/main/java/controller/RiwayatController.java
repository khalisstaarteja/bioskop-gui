package main.java.controller;

import java.sql.SQLException;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import main.java.database.PemesananDAO;
import main.java.model.Pemesanan;
import main.java.model.User;

public class RiwayatController {

    @FXML private TableView<Pemesanan> riwayatTable;
    @FXML private TableColumn<Pemesanan, String> nomorColumn;
    @FXML private TableColumn<Pemesanan, String> filmColumn;
    @FXML private TableColumn<Pemesanan, String> jadwalColumn;
    @FXML private TableColumn<Pemesanan, Integer> jumlahColumn;
    @FXML private TableColumn<Pemesanan, String> totalColumn;
    
    private final PemesananDAO pemesananDAO = new PemesananDAO();
    private User currentUser;

    @FXML
    public void initialize() {
        if (nomorColumn != null) {
            nomorColumn.setCellValueFactory(c ->
                new SimpleStringProperty("INV-" + c.getValue().getNomorPemesanan()));
        }
        filmColumn.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getJadwal().getFilm().getJudul()));
        jadwalColumn.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getJadwal().getTanggal() + " | " + c.getValue().getJadwal().getJam()));
        jumlahColumn.setCellValueFactory(c ->
            c.getValue().jumlahPemesananProperty().asObject());
        totalColumn.setCellValueFactory(c ->
            new SimpleStringProperty("Rp " + String.format("%,.0f", c.getValue().getTotalBayar())));
    }

    // Dipanggil dari DashboardController.openTab setelah FXML dimuat
    public void initUser(User user) {
        this.currentUser = user;
        loadRiwayat();
    }

    private void loadRiwayat() {
        try {
            List<Pemesanan> list = pemesananDAO.getPemesananByUserId(currentUser.getId());
            riwayatTable.setItems(FXCollections.observableArrayList(list));
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Gagal memuat riwayat: " + e.getMessage()).showAndWait();
        }
    }
}