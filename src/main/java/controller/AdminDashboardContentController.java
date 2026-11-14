package main.java.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.ByteArrayInputStream;
import main.java.database.FilmDAO;
import main.java.database.PemesananDAO;
import main.java.model.Film;
import main.java.model.User;

import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AdminDashboardContentController {

    @FXML private Label totalPendapatanLabel;
    @FXML private Label totalTiketLabel;
    @FXML private Label totalFilmLabel;

    @FXML private ImageView posterImageView;
    @FXML private Label movieTitleLabel;
    @FXML private Label movieGenreLabel;

    private List<Film> latestFilms;
    private int currentIndex = 0;
    private User currentUser; // Variabel untuk menyimpan user yang login

    private final FilmDAO filmDAO = new FilmDAO();
    private final PemesananDAO pemesananDAO = new PemesananDAO();

    @FXML
    public void initialize() {
        loadStatistics();
        loadRecentMovies();
    }

    public void initUser(User user) {
        this.currentUser = user;
        loadStatistics();
        loadRecentMovies();
    }

    private void loadStatistics() {
        try {
            double pendapatan = pemesananDAO.getTotalPendapatan();
            int totalTiket = pemesananDAO.getTotalTiketTerjual();
            int totalFilm = filmDAO.getAllFilm().size();

            Locale indoLocale = new Locale("in", "ID");
            NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(indoLocale);
            String formattedPendapatan = rupiahFormat.format(pendapatan);

            if (totalPendapatanLabel != null) {
                totalPendapatanLabel.setText(formattedPendapatan);
            }
            if (totalTiketLabel != null) {
                totalTiketLabel.setText(totalTiket + " Tiket");
            }
            if (totalFilmLabel != null) {
                totalFilmLabel.setText(totalFilm + " Film");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadRecentMovies() {
        try {
            // Hanya ambil film yang punya jadwal aktif
            latestFilms = filmDAO.getFilmWithJadwal(); 
            if (latestFilms != null && !latestFilms.isEmpty()) {
                currentIndex = 0;
                displayMovie(currentIndex);
            } else {
                if (movieTitleLabel != null) movieTitleLabel.setText("Belum ada film dengan jadwal aktif");
                if (movieGenreLabel != null) movieGenreLabel.setText("-");
                if (posterImageView != null) posterImageView.setImage(null);
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