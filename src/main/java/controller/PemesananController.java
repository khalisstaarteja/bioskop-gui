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
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import main.java.database.BioskopDAO;
import main.java.database.FilmDAO;
import main.java.database.JadwalFilmDAO;
import main.java.database.PemesananDAO;
import main.java.model.Bioskop;
import main.java.model.Film;
import main.java.model.JadwalFilm;
import main.java.model.Pemesanan;
import main.java.model.User;

public class PemesananController {
    // Pemesanan
    @FXML private ComboBox<Bioskop> bioskopComboBox;
    @FXML private ComboBox<Film> filmComboBox;
    @FXML private ComboBox<JadwalFilm> jadwalComboBox;
    @FXML private TextField jumlahTiketField;
    @FXML private ComboBox<String> metodePembayaranComboBox;
    @FXML private Label totalHargaLabel;

    // Riwayat
    @FXML private TableView<Pemesanan> riwayatTable;
    @FXML private TableColumn<Pemesanan, String> filmColumn;
    @FXML private TableColumn<Pemesanan, String> jadwalColumn;
    @FXML private TableColumn<Pemesanan, Integer> jumlahColumn;
    @FXML private TableColumn<Pemesanan, String> totalColumn;

    private final PemesananDAO pemesananDAO = new PemesananDAO();
    private final JadwalFilmDAO jadwalDAO = new JadwalFilmDAO();
    private final FilmDAO filmDAO = new FilmDAO();
    private final BioskopDAO bioskopDAO = new BioskopDAO();
    private User currentUser;

    public void initUser(User user) {
        this.currentUser = user;
        loadRiwayat();
    }

    @FXML
    public void initialize() {
        loadBioskopList();
        
        // Isi pilihan metode pembayaran
        if (metodePembayaranComboBox != null) {
            metodePembayaranComboBox.setItems(FXCollections.observableArrayList("Transfer Bank", "E-Wallet", "Cash"));
        }

        // Listener saat Bioskop dipilih -> Filter Film yang ada di bioskop tersebut
        if (bioskopComboBox != null) {
            bioskopComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    loadFilmByBioskop(newVal.getId());
                    if (jadwalComboBox != null) jadwalComboBox.getItems().clear();
                } else {
                    if (filmComboBox != null) filmComboBox.getItems().clear();
                    if (jadwalComboBox != null) jadwalComboBox.getItems().clear();
                }
            });
        }

        // Listener saat Film dipilih -> Filter Jadwal berdasarkan Bioskop & Film
        if (filmComboBox != null) {
            filmComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && bioskopComboBox.getValue() != null) {
                    loadJadwalByBioskopAndFilm(bioskopComboBox.getValue().getId(), newVal.getId());
                } else {
                    if (jadwalComboBox != null) jadwalComboBox.getItems().clear();
                }
            });
        }

        // Setup Kolom Tabel Riwayat
        if (filmColumn != null) {
            filmColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getJadwal().getFilm().getJudul()));
            jadwalColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getJadwal().getTanggal() + " | " + cell.getValue().getJadwal().getJam()));
            jumlahColumn.setCellValueFactory(cell -> cell.getValue().jumlahPemesananProperty().asObject());
            totalColumn.setCellValueFactory(cell -> new SimpleStringProperty("Rp " + String.format("%,.0f", cell.getValue().getTotalBayar())));
        }

        refreshDataJadwal();
    }

    private void loadBioskopList() {
        try {
            List<Bioskop> listBioskop = bioskopDAO.getBioskopWithJadwal();
            if (bioskopComboBox != null) {
                bioskopComboBox.setItems(FXCollections.observableArrayList(listBioskop));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadFilmByBioskop(int bioskopId) {
        try {
            List<Film> listFilm = filmDAO.getFilmByBioskopId(bioskopId);
            if (filmComboBox != null) {
                filmComboBox.setItems(FXCollections.observableArrayList(listFilm));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadJadwalByBioskopAndFilm(int bioskopId, int filmId) {
        try {
            List<JadwalFilm> listJadwal = jadwalDAO.getJadwalByBioskopAndFilm(bioskopId, filmId);
            if (jadwalComboBox != null) {
                jadwalComboBox.setItems(FXCollections.observableArrayList(listJadwal));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void refreshDataJadwal() {
        if (currentUser != null) {
            loadRiwayat();
        }
    }

    private void loadRiwayat() {
        if (currentUser == null) return;
        try {
            List<Pemesanan> list = new main.java.database.PelangganDAO().getAllPelangganWithPemesanan().stream()
                    .filter(p -> p.getId() == currentUser.getId())
                    .findFirst()
                    .map(p -> (List<Pemesanan>) p.getListPemesanan())
                    .orElse(List.of());
    
            if (riwayatTable != null) {
                riwayatTable.setItems(FXCollections.observableArrayList(list));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    

    // Aksi saat pelanggan menekan tombol "Pesan Tiket"
    @FXML
    private void handlePesanTiket() {
        Bioskop bioskop = bioskopComboBox != null ? bioskopComboBox.getValue() : null;
        Film film = filmComboBox != null ? filmComboBox.getValue() : null;
        JadwalFilm jadwal = jadwalComboBox != null ? jadwalComboBox.getValue() : null;
        String jumlahStr = jumlahTiketField != null ? jumlahTiketField.getText().trim() : "";
        String metode = metodePembayaranComboBox != null ? metodePembayaranComboBox.getValue() : null;

        if (bioskop == null || film == null || jadwal == null || jumlahStr.isEmpty() || metode == null) {
            showAlert("Peringatan", "Lengkapi semua data pemesanan (Bioskop, Film, Jadwal, Jumlah, & Metode Pembayaran)!");
            return;
        }

        try {
            int jumlah = Integer.parseInt(jumlahStr);
            int sisaKursi = jadwal.getKapasitas() - jadwal.getKursiTerisi();
            if (jumlah > sisaKursi) {
                showAlert("Kursi Tidak Cukup",
                    "Sisa kursi hanya " + sisaKursi + ". Kurangi jumlah tiket yang dipesan.");
                return;
            }
            if (jumlah <= 0) {
                showAlert("Peringatan", "Jumlah tiket minimal 1!");
                return;
            }

            // Hitung total bayar (Harga Studio * Jumlah Tiket)
            float hargaSatuan = jadwal.getStudio() != null ? jadwal.getStudio().getHarga() : 0;
            float totalBayar = hargaSatuan * jumlah;
            int nomorRandom = pemesananDAO.generateNomorPemesanan();

            // Buat objek Pemesanan baru
            Pemesanan pemesananBaru = new Pemesanan(
                -1, nomorRandom, jadwal, jumlah, metode, totalBayar, currentUser
            );

            // Simpan ke database via DAO
            pemesananDAO.savePemesanan(pemesananBaru);
            loadRiwayat(); 
            clearForm();

            Alert tanya = new Alert(Alert.AlertType.CONFIRMATION,
                    "Tiket berhasil dipesan! Cetak tiket sekarang?", ButtonType.YES, ButtonType.NO);
            tanya.setTitle("Sukses");
            tanya.getDialogPane().getStylesheets().add(getClass().getResource("/main/resources/css/style.css").toExternalForm());
            tanya.setHeaderText(null);
            tanya.showAndWait();
            if (tanya.getResult() == ButtonType.YES) {
                tampilkanTiket(pemesananBaru);
            }

        } catch (NumberFormatException e) {
            showAlert("Format Salah", "Jumlah tiket harus berupa angka!");
        } catch (SQLException e) {
            showAlert("Database Error", e.getMessage());
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

    @FXML
    private void clearForm() {
        if (bioskopComboBox != null) bioskopComboBox.setValue(null);
        if (filmComboBox != null) filmComboBox.setValue(null);
        if (jadwalComboBox != null) jadwalComboBox.setValue(null);
        if (jumlahTiketField != null) jumlahTiketField.clear();
        if (metodePembayaranComboBox != null) metodePembayaranComboBox.setValue(null);
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

    public void setPreselectedJadwal(JadwalFilm jadwal) {
        if (jadwal == null) return;

        Bioskop targetBioskop = jadwal.getStudio().getBioskop();
        Film targetFilm = jadwal.getFilm();

        // pilih bioskop yg cocok berdasarkan id
        if (bioskopComboBox != null) {
            for (Bioskop b : bioskopComboBox.getItems()) {
                if (b.getId() == targetBioskop.getId()) {
                    bioskopComboBox.setValue(b);
                    break;
                }
            }
        }

        // pilih film yg cocok berdasarkan id
        if (filmComboBox != null) {
            for (Film f : filmComboBox.getItems()) {
                if (f.getId() == targetFilm.getId()) {
                    filmComboBox.setValue(f);
                    break;
                }
            }
        }

        // pilih jadwal yg cocok berdasarkan id
        if (jadwalComboBox != null) {
            for (JadwalFilm j : jadwalComboBox.getItems()) {
                if (j.getId() == jadwal.getId()) {
                    jadwalComboBox.setValue(j);
                    break;
                }
            }
        }

        if (jumlahTiketField != null) {
            jumlahTiketField.requestFocus();
        }
    }
}