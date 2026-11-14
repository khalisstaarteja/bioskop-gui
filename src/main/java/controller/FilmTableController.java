package main.java.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.TableCell;
import javafx.stage.FileChooser;
import main.java.database.FilmDAO;
import main.java.model.Film;

public class FilmTableController {
    @FXML private TextField kodeFilmField;
    @FXML private TextField judulField;
    @FXML private TextField genreField;
    @FXML private TextField durasiField;
    @FXML private ImageView posterImageView;
    @FXML private TableView<Film> filmTable;
    @FXML private TableColumn<Film, byte[]> posterColumn;

    private byte[] selectedPosterBytes;
    private final FilmDAO filmDAO = new FilmDAO();

    private void refreshTable() {
        try {
            filmTable.setItems(FXCollections.observableArrayList(filmDAO.getAllFilm()));
        } catch (SQLException e) {
            showError("Database Error", e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        refreshTable();

        filmTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                kodeFilmField.setText(newVal.getKodeFilm());
                judulField.setText(newVal.getJudul());
                genreField.setText(newVal.getGenre());
                durasiField.setText(String.valueOf(newVal.getDurasi()));
                selectedPosterBytes = newVal.getPoster();
                if (selectedPosterBytes != null) {
                    posterImageView.setImage(new Image(new ByteArrayInputStream(selectedPosterBytes)));
                } else {
                    posterImageView.setImage(null);
                }
            } else {
                clearForm();
            }
        });

        posterColumn.setCellFactory(column -> new TableCell<Film, byte[]>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(byte[] imageBytes, boolean empty) {
                super.updateItem(imageBytes, empty);
                
                // Jika poster ga ada, kosongkan cell
                if (empty || imageBytes == null) {
                    setGraphic(null);
                }
                else {
                    // byte[] dari database diubah menjadi JavaFX
                    ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
                    Image image = new Image(bis);
                    // Atur ukuran poster
                    imageView.setImage(image);
                    imageView.setFitWidth(50);
                    imageView.setFitHeight(75);
                    imageView.setPreserveRatio(true);

                    // Tampilkan gambarnya di table
                    setGraphic(imageView);
                }
            }

        });
    }

    @FXML
    private void handleChoosePoster() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih Poster Film");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                selectedPosterBytes = Files.readAllBytes(file.toPath());
                posterImageView.setImage(new Image(file.toURI().toString()));
            } catch (IOException e) {
                showError("File Error", "Gagal membaca file gambar: " + e.getMessage());
            }
        }
    }

    private Film readFormFields() {
        String kode = kodeFilmField.getText().trim();
        String judul = judulField.getText().trim();
        String genre = genreField.getText().trim();
        int durasi = Integer.parseInt(durasiField.getText().trim());

        return new Film(-1, kode, judul, genre, durasi, selectedPosterBytes);
    }

    @FXML
    private void handleSubmit() {
        if (kodeFilmField.getText().trim().isEmpty() || judulField.getText().trim().isEmpty()) {
            showAlert("Warning", "Kode dan Judul Film tidak boleh kosong!");
            return;
        }

        boolean kodeAda = filmTable.getItems().stream()
                .anyMatch(f -> f.getKodeFilm().equalsIgnoreCase(kodeFilmField.getText().trim()));
        if (kodeAda) {
            showAlert("Warning", "Kode film sudah dipakai!");
            return;
        }

        try {
            filmDAO.saveFilm(readFormFields());
            refreshTable();
            clearForm();
        } catch (Exception e) {
            showError("Submit Error", e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        Film selected = filmTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Pilih data film yang ingin di-update.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Yakin ingin mengupdate data?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            try {
                filmDAO.updateFilm(readFormFields(), selected.getId());
                refreshTable();
                clearForm();
            } catch (Exception e) {
                showError("Update Error", e.getMessage());
            }
        }
    }

    @FXML
    private void handleDelete() {
        Film selected = filmTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Pilih data film yang ingin dihapus.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Yakin ingin menghapus data?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            try {
                filmDAO.deleteFilm(selected.getId());
                refreshTable();
                clearForm();
            } catch (Exception e) {
                showError("Delete Error", e.getMessage());
            }
        }
    }

    private void clearForm() {
        kodeFilmField.clear();
        judulField.clear();
        genreField.clear();
        durasiField.clear();
        selectedPosterBytes = null;
        posterImageView.setImage(null);
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