package main.java.controller;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.java.model.Admin;
import main.java.model.User;

public class DashboardController {
    @FXML private BorderPane rootPane;
    @FXML private TabPane mainTabPane;
    @FXML private Label loggedUserLabel;
    @FXML private VBox adminMenu;
    @FXML private VBox pelangganMenu;

    private User currentUser;

    public void setLoggedUser(User user) {
        this.currentUser = user;
        loadSidebar();

        if (mainTabPane != null) {
            mainTabPane.getTabs().clear();

            mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                if (newTab != null) {
                    syncActiveMenu(newTab);

                    Object ctrl = newTab.getUserData();
                    if (ctrl instanceof JadwalFilmTableController) {
                        ((JadwalFilmTableController) ctrl).refreshTable();
                    } else if (ctrl instanceof StudioTableController) {
                        ((StudioTableController) ctrl).refreshTable();
                    } else if (ctrl instanceof RiwayatController) {
                        ((RiwayatController) ctrl).initUser(currentUser);
                    } else if (ctrl instanceof PemesananController) {
                        ((PemesananController) ctrl).initUser(currentUser);
                    }
                }
            });

            loadDefaultTab();
        }
    }

    private String menuTextForTab(String tabTitle) {
        return switch (tabTitle) {
            case "Beranda Admin", "Beranda Pelanggan" -> "Beranda";
            case "Data Bioskop" -> "Kelola Bioskop";
            case "Data Studio" -> "Kelola Studio";
            case "Data Film" -> "Kelola Film";
            case "Data Jadwal Tayang" -> "Kelola Jadwal Tayang";
            case "Data Users" -> "Kelola User";
            case "Kelola Pemesanan" -> "Lihat Pemesanan";
            case "Cari Film" -> "Cari Film";
            case "Form Pemesanan Tiket" -> "Form Pemesanan";
            case "Riwayat Pemesanan" -> "Riwayat Pemesanan";
            default -> "";
        };
    }

    // Untuk tau tab yang lagi aktif yang mana
    private void syncActiveMenu(Tab tab) {
        VBox menu = (currentUser instanceof Admin) ? adminMenu : pelangganMenu;
        if (menu == null) return;

        String target = menuTextForTab(tab.getText());
        for (Node n : menu.getChildren()) {
            if (n instanceof Button) {
                Button b = (Button) n;
                b.getStyleClass().remove("active");
                if (b.getText().equals(target)) {
                    b.getStyleClass().add("active");
                }
            }
        }
    }

    private void loadSidebar() {
        try {
            // Tetap memuat sidebar.fxml yang menggunakan controller ini
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/sidebar.fxml"));
            loader.setController(this);
            Parent sidebarView = loader.load();

            if (rootPane != null) {
                rootPane.setLeft(sidebarView);
            }

            boolean isAdmin = currentUser instanceof Admin;
            adminMenu.setVisible(isAdmin);
            adminMenu.setManaged(isAdmin);
            pelangganMenu.setVisible(!isAdmin);
            pelangganMenu.setManaged(!isAdmin);

            loggedUserLabel.setText("Login sebagai:\n" + currentUser.getNama() + " (" + currentUser.getRole() + ")");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Object openTab(String title, String fxmlPath) {
        if (mainTabPane == null) return null;

        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getText().equals(title)) {
                mainTabPane.getSelectionModel().select(tab);
                return tab.getUserData();
            }
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof PemesananController) {
                ((PemesananController) controller).initUser(currentUser);
            } else if (controller instanceof RiwayatController) {
                ((RiwayatController) controller).initUser(currentUser);
            } else if (controller instanceof CariFilmController) {
                ((CariFilmController) controller).initData(currentUser, this);
            }

            Tab newTab = new Tab(title, view);
            newTab.setUserData(controller);
            mainTabPane.getTabs().add(newTab);
            mainTabPane.getSelectionModel().select(newTab);
            return controller;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @FXML 
    private void handleShowBeranda() {
        if (mainTabPane != null) {
            mainTabPane.getTabs().clear();
            loadDefaultTab();
        }
    }

    // Abstract method currentUser.showMenu() dipanggil
    private void loadDefaultTab() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(currentUser.showMenu()));
            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof AdminDashboardContentController) {
                ((AdminDashboardContentController) controller).initUser(currentUser);
            } else if (controller instanceof PelangganDashboardContentController) {
                ((PelangganDashboardContentController) controller).initUser(currentUser);
            }

            String title = (currentUser instanceof Admin) ? "Beranda Admin" : "Beranda Pelanggan";
            Tab defaultTab = new Tab(title, view);
            defaultTab.setClosable(false);
            mainTabPane.getTabs().add(defaultTab);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Navigasi Menu Admin
    @FXML private void showBioskopView() { openTab("Data Bioskop", "/main/resources/bioskop_table.fxml"); }
    @FXML private void showStudioView() { openTab("Data Studio", "/main/resources/studio_table.fxml"); }
    @FXML private void showFilmView() { openTab("Data Film", "/main/resources/film_table.fxml"); }
    @FXML private void showJadwalView() { openTab("Data Jadwal Tayang", "/main/resources/jadwalFilm_table.fxml"); }
    @FXML private void showUserView() { openTab("Data Users", "/main/resources/user_table.fxml"); }
    @FXML private void showPemesananAdminView() { openTab("Kelola Pemesanan", "/main/resources/pemesanan_table.fxml"); }

    // Navigasi Menu Pelanggan
    @FXML private void showCariFilmView() { openTab("Cari Film", "/main/resources/cari_film.fxml"); }
    @FXML private void showPemesananView() { openTab("Form Pemesanan Tiket", "/main/resources/pemesanan_view.fxml"); }
    @FXML private void showRiwayatView() { openTab("Riwayat Pemesanan", "/main/resources/riwayat_table.fxml"); }

    // Shortcut buat buka form pemesanan pas kita klik film card biar dia bisa autofill
    public void openPemesananWithJadwal(main.java.model.JadwalFilm jadwal) {
        Object ctrl = openTab("Form Pemesanan Tiket", "/main/resources/pemesanan_view.fxml");
        if (ctrl instanceof PemesananController) {
            ((PemesananController) ctrl).setPreselectedJadwal(jadwal);
        }
    }

    @FXML 
    private void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/main/resources/login_view.fxml"));
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login Sistem Bioskop");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExit() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Yakin ingin keluar dari aplikasi?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Konfirmasi Keluar");
        confirm.setHeaderText(null);
        confirm.getDialogPane().getStylesheets().add(getClass().getResource("/main/resources/css/style.css").toExternalForm());

        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            javafx.application.Platform.exit();
            System.exit(0);
        }
    }
}
