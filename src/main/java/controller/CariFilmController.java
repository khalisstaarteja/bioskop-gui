package main.java.controller;

import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import main.java.database.FilmDAO;
import main.java.database.JadwalFilmDAO;
import main.java.model.Film;
import main.java.model.JadwalFilm;
import main.java.model.User;

public class CariFilmController {

    @FXML private VBox listFilmView;
    @FXML private TextField searchField;
    @FXML private VBox filmListContainer;

    @FXML private VBox detailJadwalView;
    @FXML private HBox selectedFilmHeader;
    @FXML private VBox jadwalListContainer;

    private final FilmDAO filmDAO = new FilmDAO();
    private final JadwalFilmDAO jadwalDAO = new JadwalFilmDAO();

    private List<Film> allFilms = new ArrayList<>();
    private User currentUser;
    private DashboardController dashboardController;

    public void initData(User user, DashboardController dashboardController) {
        this.currentUser = user;
        this.dashboardController = dashboardController;
    }

    @FXML
    public void initialize() {
        loadAllFilms();

        // Filter otomatis saat user mengetik di searchField
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> filterFilms(newVal));
        }
    }

    private void loadAllFilms() {
        try {
            allFilms = filmDAO.getAllFilm();
            renderFilmList(allFilms);
        } catch (SQLException e) {
            showError("Database Error", "Gagal memuat daftar film: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        filterFilms(searchField.getText());
    }

    @FXML
    private void handleResetSearch() {
        searchField.clear();
        renderFilmList(allFilms);
    }

    private void filterFilms(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            renderFilmList(allFilms);
            return;
        }
        String lower = keyword.trim().toLowerCase();
        List<Film> filtered = allFilms.stream()
                .filter(f -> f.getJudul().toLowerCase().contains(lower)
                          || f.getGenre().toLowerCase().contains(lower)
                          || f.getKodeFilm().toLowerCase().contains(lower))
                .collect(Collectors.toList());
        renderFilmList(filtered);
    }

    private void renderFilmList(List<Film> films) {
        filmListContainer.getChildren().clear();

        if (films == null || films.isEmpty()) {
            Label emptyLabel = new Label("Film tidak ditemukan.");
            emptyLabel.getStyleClass().add("film-card-meta");
            filmListContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Film film : films) {
            HBox card = createFilmCard(film, true);
            // Klik kartu juga bisa langsung buka detail jadwal
            card.setOnMouseClicked(e -> openFilmDetail(film));
            filmListContainer.getChildren().add(card);
        }
    }

    // Membuat tampilan kartu film: Poster di Kiri, Tulisan di Kanan
    private HBox createFilmCard(Film film, boolean showDetailButton) {
        HBox card = new HBox(18);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("film-card");

        // Kiri: Poster
        ImageView posterView = new ImageView();
        posterView.setFitWidth(95);
        posterView.setFitHeight(140);
        posterView.setPreserveRatio(true);
        if (film.getPoster() != null && film.getPoster().length > 0) {
            posterView.setImage(new Image(new ByteArrayInputStream(film.getPoster())));
        }

        // Kanan: Informasi Film
        VBox infoBox = new VBox(6);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label titleLabel = new Label(film.getJudul());
        titleLabel.getStyleClass().add("film-card-title");

        Label kodeLabel = new Label("Kode Film: " + film.getKodeFilm());
        kodeLabel.getStyleClass().add("film-card-meta");

        Label genreLabel = new Label("Genre: " + film.getGenre());
        genreLabel.getStyleClass().add("film-card-meta");

        Label durasiLabel = new Label("Durasi: " + film.getDurasi() + " Menit");
        durasiLabel.getStyleClass().add("film-card-meta");

        infoBox.getChildren().addAll(titleLabel, kodeLabel, genreLabel, durasiLabel);
        card.getChildren().addAll(posterView, infoBox);

        if (showDetailButton) {
            Button btnJadwal = new Button("Lihat Jadwal");
            btnJadwal.getStyleClass().add("btn-pesan-jadwal");
            btnJadwal.setOnAction(e -> openFilmDetail(film));
            card.getChildren().add(btnJadwal);
        }

        return card;
    }

    // Pindah ke tampilan detail film & daftar jadwalnya
    private void openFilmDetail(Film film) {
        try {
            // Ambil jadwal aktif dari DB lalu simpan ke listJadwal milik model Film
            List<JadwalFilm> jadwalAktif = jadwalDAO.getJadwalByFilmId(film.getId());
            film.setListJadwal(new ArrayList<>(jadwalAktif));

            // Tampilkan header film (poster kiri, tulisan kanan)
            selectedFilmHeader.getChildren().clear();
            HBox headerContent = createFilmCard(film, false);
            headerContent.setStyle("-fx-border-color: transparent; -fx-padding: 0; -fx-effect: none;");
            selectedFilmHeader.getChildren().add(headerContent);

            // Render list jadwal dari film.getListJadwal()
            renderJadwalList(film.getListJadwal());

            listFilmView.setVisible(false);
            listFilmView.setManaged(false);
            detailJadwalView.setVisible(true);
            detailJadwalView.setManaged(true);

        } catch (SQLException e) {
            showError("Database Error", "Gagal memuat jadwal film: " + e.getMessage());
        }
    }

    private void renderJadwalList(List<JadwalFilm> listJadwal) {
        jadwalListContainer.getChildren().clear();

        if (listJadwal == null || listJadwal.isEmpty()) {
            Label noJadwal = new Label("Belum ada jadwal tayang untuk film ini.");
            noJadwal.setStyle("-fx-font-size: 14px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            jadwalListContainer.getChildren().add(noJadwal);
            return;
        }

        for (JadwalFilm j : listJadwal) {
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("film-card");

            int sisaKursi = j.sisaKursi(); // Menggunakan method model JadwalFilm.sisaKursi()
            boolean isAvailable = sisaKursi > 0;

            VBox infoJadwal = new VBox(6);
            infoJadwal.setAlignment(Pos.CENTER_LEFT);

            Label bioskopLabel = new Label(j.getStudio().getBioskop().getNamaBioskop()
                    + " (" + j.getStudio().getBioskop().getLokasi() + ")");
            bioskopLabel.getStyleClass().add("bioskop-card-title");

            Label studioLabel = new Label("Studio " + j.getStudio().getNomorStudio()
                    + " (" + j.getStudio().getJenisStudio() + ")  •  Rp "
                    + String.format("%,.0f", j.getStudio().getHarga()));
            studioLabel.getStyleClass().add("studio-badge");

            Label waktuLabel = new Label("Tanggal: " + j.getTanggal() + "  |  Jam: " + j.getJam()
                    + "  |  Sisa Kursi: " + sisaKursi + " / " + j.getKapasitas());
            waktuLabel.getStyleClass().add("film-card-meta");

            infoJadwal.getChildren().addAll(bioskopLabel, studioLabel, waktuLabel);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // Badge Status Hijau / Merah
            Label statusBadge = new Label(isAvailable ? "Available (" + sisaKursi + " kursi)" : "Habis (0 kursi)");
            statusBadge.getStyleClass().add(isAvailable ? "status-available" : "status-full");

            // Tombol Pesan
            Button btnPesan = new Button("Pesan");
            btnPesan.getStyleClass().add("btn-pesan-jadwal");
            btnPesan.setDisable(!isAvailable); // Tidak bisa diklik jika kursi 0
            btnPesan.setOnAction(e -> {
                if (dashboardController != null) {
                    dashboardController.openPemesananWithJadwal(j);
                }
            });

            row.getChildren().addAll(infoJadwal, spacer, statusBadge, btnPesan);
            jadwalListContainer.getChildren().add(row);
        }
    }

    @FXML
    private void handleBackToList() {
        detailJadwalView.setVisible(false);
        detailJadwalView.setManaged(false);
        listFilmView.setVisible(true);
        listFilmView.setManaged(true);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}