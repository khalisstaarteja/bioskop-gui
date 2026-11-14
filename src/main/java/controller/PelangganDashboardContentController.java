package main.java.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.ByteArrayInputStream;
import main.java.database.FilmDAO;
import main.java.database.PemesananDAO;
import main.java.model.Film;
import main.java.model.Pemesanan;
import main.java.model.User;

import java.sql.SQLException;
import java.util.List;

public class PelangganDashboardContentController {

    @FXML private Label totalTiketSayaLabel;
    @FXML private Label totalFilmDitontonLabel;

    @FXML private ImageView posterImageView;
    @FXML private Label movieTitleLabel;
    @FXML private Label movieGenreLabel;

    private List<Film> latestFilms;
    private int currentIndex = 0;
    private User currentUser;

    private final FilmDAO filmDAO = new FilmDAO();
    private final PemesananDAO pemesananDAO = new PemesananDAO();

    @FXML
    public void initialize() {
        loadUserStatistics();
        loadRecentMovies();
    }

    public void initUser(User user) {
        this.currentUser = user;
        loadUserStatistics();
        loadRecentMovies();
    }

    private void loadUserStatistics() {
        if (currentUser == null) return;
        try {
            List<Pemesanan> riwayat = pemesananDAO.getPemesananByUserId(currentUser.getId());
            int totalTiket = 0;
            for (Pemesanan p : riwayat) {
                totalTiket += p.getJumlahPemesanan();
            }

            if (totalTiketSayaLabel != null) {
                totalTiketSayaLabel.setText(totalTiket + " Tiket");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadRecentMovies() {
        try {
            latestFilms = filmDAO.getFilmWithJadwal(); 
            if (latestFilms != null && !latestFilms.isEmpty()) {
                displayMovie(currentIndex);
            } else {
                if (movieTitleLabel != null) movieTitleLabel.setText("Belum ada data film");
                if (movieGenreLabel != null) movieGenreLabel.setText("-");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayMovie(int index) {
        if (latestFilms == null || latestFilms.isEmpty()) return;
        
        Film film = latestFilms.get(index);
        if (movieTitleLabel != null) movieTitleLabel.setText(film.getJudul());
        if (movieGenreLabel != null) {
            movieGenreLabel.setText("Genre: " + film.getGenre() + " | Durasi: " + film.getDurasi() + " Menit");
        }
    
        try {
            byte[] posterBytes = film.getPoster();
            if (posterBytes != null && posterBytes.length > 0 && posterImageView != null) {
                ByteArrayInputStream bis = new ByteArrayInputStream(posterBytes);
                Image image = new Image(bis);
                posterImageView.setImage(image);
            } else if (posterImageView != null) {
                posterImageView.setImage(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (posterImageView != null) posterImageView.setImage(null);
        }
    }

    @FXML
    private void handleNextMovie() {
        if (latestFilms == null || latestFilms.isEmpty()) return;
        currentIndex = (currentIndex + 1) % latestFilms.size();
        displayMovie(currentIndex);
    }

    @FXML
    private void handlePrevMovie() {
        if (latestFilms == null || latestFilms.isEmpty()) return;
        currentIndex = (currentIndex - 1 + latestFilms.size()) % latestFilms.size();
        displayMovie(currentIndex);
    }
}