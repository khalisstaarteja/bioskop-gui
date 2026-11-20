package main.java.controller;

import java.io.IOException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import main.java.model.Admin;
import main.java.model.User;

public class DashboardController {
    @FXML private BorderPane rootPane;
    @FXML private TabPane mainTabPane;

    private User currentUser;

    public void setLoggedUser(User user) {
        this.currentUser = user;
        loadSidebar();

        if (mainTabPane != null) {
            mainTabPane.getTabs().clear();

            mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                if (newTab != null) {
                    Object ctrl = newTab.getUserData();
                    if (ctrl instanceof JadwalFilmTableController) {
                        ((JadwalFilmTableController) ctrl).refreshTable();
                    } else if (ctrl instanceof StudioTableController) {
                        ((StudioTableController) ctrl).refreshTable();
                    }
                }
            });

            if (user instanceof Admin) {
                loadDefaultAdminTab();
            } else {
                loadDefaultPelangganTab();
            }
        }
    }

    private void loadSidebar() {
        try {
            String sidebarFxml = currentUser.showMenu();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(sidebarFxml));
            loader.setController(this);
            Parent sidebarView = loader.load();

            if (rootPane != null) {
                rootPane.setLeft(sidebarView);
            }

            Label userLabel = (Label) sidebarView.lookup("#loggedUserLabel");
            if (userLabel != null) {
                userLabel.setText("Login sebagai:\n" + currentUser.getNama() + " (" + currentUser.getRole() + ")");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private Object openTab(String title, String fxmlPath) {
        if (mainTabPane == null) return null;
        
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getText().equals(title)) {
                mainTabPane.getSelectionModel().select(tab);
                return tab.getUserData(); // Ambil controller yg ada di tab
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

    @FXML private void handleShowBeranda() {
        if (mainTabPane != null) {
            mainTabPane.getTabs().clear();
            if ("Admin".equalsIgnoreCase(currentUser.getRole())) {
                loadDefaultAdminTab();
            } else {
                loadDefaultPelangganTab(); 
            }
        }
    }

    private void loadDefaultAdminTab() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/admin_dashboard_content.fxml"));
            Parent adminView = loader.load();
            AdminDashboardContentController controller = loader.getController();
            if (controller != null) controller.initUser(currentUser);
            
            Tab adminTab = new Tab("Beranda Admin", adminView);
            adminTab.setClosable(false);
            mainTabPane.getTabs().add(adminTab);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadDefaultPelangganTab() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/pelanggan_dashboard_content.fxml"));
            Parent pelangganView = loader.load();
            PelangganDashboardContentController controller = loader.getController();
            if (controller != null) controller.initUser(currentUser);
            
            Tab pelangganTab = new Tab("Beranda Pelanggan", pelangganView);
            pelangganTab.setClosable(false);
            mainTabPane.getTabs().add(pelangganTab);
        } catch (Exception e) { e.printStackTrace(); }
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

    public void openPemesananWithJadwal(main.java.model.JadwalFilm jadwal) {
        Object ctrl = openTab("Form Pemesanan Tiket", "/main/resources/pemesanan_view.fxml");
        if (ctrl instanceof PemesananController) {
            ((PemesananController) ctrl).setPreselectedJadwal(jadwal);
        }
    }

    @FXML private void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/main/resources/login_view.fxml"));
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login Sistem Bioskop");
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void handleExit() { Platform.exit(); }
}