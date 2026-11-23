package main.java.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
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
    @FXML private TableColumn<Pemesanan, String> bioskopColumn;
    @FXML private TableColumn<Pemesanan, String> studioColumn;
    
    private final PemesananDAO pemesananDAO = new PemesananDAO();
    private User currentUser;

    @FXML
    public void initialize() {
        if (nomorColumn != null) {
            nomorColumn.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getNomorPemesanan())));
        }
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
    }

    // Dipanggil dari DashboardController.openTab setelah FXML dimuat
    public void initUser(User user) {
        this.currentUser = user;
        loadRiwayat();
    }

    private void loadRiwayat() {
        try {
            List<Pemesanan> list = new main.java.database.PelangganDAO().getAllPelangganWithPemesanan().stream()
                    .filter(p -> p.getId() == currentUser.getId())
                    .findFirst()
                    .map(p -> (List<Pemesanan>) p.getListPemesanan())
                    .orElse(List.of());
    
            riwayatTable.setItems(FXCollections.observableArrayList(list));
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Gagal memuat riwayat: " + e.getMessage()).showAndWait();
        }
    }
    

    // Cetak tiket
    @FXML
    private void handleCetakTiket() {
         Pemesanan selected = riwayatTable != null ? riwayatTable.getSelectionModel().getSelectedItem() : null;
         if (selected == null) {
             showAlert("Peringatan", "Pilih dulu riwayat tiket pada tabel yang ingin dicetak!");
             return;
         }
         tampilkanTiket(selected);
     }

    private void tampilkanTiket(Pemesanan p) {
        try {
            p.cetakTiket();

            String htmlContent = bacaResource("/main/resources/tiket.html");
            String css = bacaResource("/main/resources/css/tiket.css");

            String tanggalSekarang = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd | HH:mm"));
            String totalFormatted = String.format("%,.0f", p.getTotalBayar());

            String itemRows =
                baris("Bioskop", p.getJadwal().getStudio().getBioskop().getNamaBioskop()) +
                baris("Film", p.getJadwal().getFilm().getJudul()) +
                baris("Studio", "Studio " + p.getJadwal().getStudio().getNomorStudio()) +
                baris("Jadwal", p.getJadwal().getTanggal() + " | " + p.getJadwal().getJam()) +
                baris("Jumlah", p.getJumlahPemesanan() + " Tiket") +
                baris("Pembayaran", p.getMetodePembayaran()) +
                "<tr class=\"total\"><td class=\"k\">Total</td><td class=\"v\">Rp " + totalFormatted + "</td></tr>";

            htmlContent = htmlContent.replace("<!--CSS-->", "<style>" + css + "</style>")
                .replace("{{tiketNumber}}", String.valueOf(p.getNomorPemesanan()))
                .replace("{{date}}", tanggalSekarang)
                .replace("{{customerName}}", currentUser.getNama())
                .replace("{{sellerName}}", "Bioskop Sistem Admin")
                .replace("{{itemRows}}", itemRows);

            WebView webView = new WebView();
            webView.setPrefSize(560, 560);
            webView.getEngine().loadContent(htmlContent);

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Tiket - " + p.getJadwal().getFilm().getJudul());

            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.getStylesheets().add(getClass().getResource("/main/resources/css/style.css").toExternalForm());
            dialogPane.setContent(webView);

            ButtonType btnPdfType = new ButtonType("Download / Print PDF", ButtonBar.ButtonData.LEFT);
            dialogPane.getButtonTypes().addAll(btnPdfType, ButtonType.CLOSE);

            Button btnPdf = (Button) dialogPane.lookupButton(btnPdfType);
            btnPdf.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
                e.consume();
                javafx.print.PrinterJob job = javafx.print.PrinterJob.createPrinterJob();
                if (job != null) {
                    for (javafx.print.Printer printer : javafx.print.Printer.getAllPrinters()) {
                        if (printer.getName().toLowerCase().contains("pdf")) {
                            job.setPrinter(printer);
                            break;
                        }
                    }
                    if (job.showPrintDialog(dialogPane.getScene().getWindow())) {
                        webView.getEngine().print(job);
                        job.endJob();
                        showAlert("Sukses", "Struk tiket berhasil diproses ke PDF!");
                    }
                } else {
                    showAlert("Error", "Layanan cetak PDF tidak ditemukan di sistem ini.");
                }
            });

            dialog.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal merender template tiket: " + e.getMessage());
        }
    }

    private String baris(String label, String isi) {
        return "<tr><td class=\"k\">" + label + "</td><td class=\"v\">" + isi + "</td></tr>";
    }
    
    private String bacaResource(String path) throws java.io.IOException {
        java.io.InputStream in = getClass().getResourceAsStream(path);
        if (in == null) {
            throw new java.io.FileNotFoundException("Resource tidak ditemukan: " + path);
        }
        try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            return r.lines().collect(Collectors.joining("\n"));
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}