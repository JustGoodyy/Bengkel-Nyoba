package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.User;
import model.UserSession;
import util.AnimationUtil;
import javafx.scene.image.ImageView;

public class MainLayoutController {

    @FXML private StackPane contentArea;
    @FXML private Label lblActiveUser;
    @FXML private VBox sidebarMenuBox;
    @FXML private Button btnLogout;
    @FXML private Button btnDashboard;
    @FXML private Button btnInventory;
    @FXML private Button btnCashier;
    @FXML private Button btnReport;
    @FXML private ImageView imgLogo;

    private static final String ROLE_ADMIN = "ADMIN";

    @FXML
    public void initialize() {
        if (UserSession.getInstance().isLoggedIn()) {
            User current = UserSession.getInstance().getCurrentUser();
            lblActiveUser.setText(current.getUsername() + " (" + current.getRole() + ")");
        }

        for (Node child : sidebarMenuBox.getChildren()) {
            if (child instanceof Button) {
                AnimationUtil.attachHoverScale(child, 1.04);
            }
        }
        AnimationUtil.attachHoverScale(btnLogout, 1.04);

        applyRoleRestrictions();

        loadView("/view/Dashboard.fxml"); // Dashboard greets the user first, per spec
    }
    

    private void applyRoleRestrictions() {
        if (!isAdmin()) {
            hide(btnInventory);
            hide(btnReport);
        }
    }

    private void hide(Button b) {
        if (b == null) return;
        b.setVisible(false);
        b.setManaged(false); 
    }

    private boolean isAdmin() {
        return UserSession.getInstance().isLoggedIn()
                && ROLE_ADMIN.equalsIgnoreCase(UserSession.getInstance().getCurrentUser().getRole());
    }

    @FXML public void goToDashboard() { loadView("/view/Dashboard.fxml"); }

    @FXML
    public void goToInventory() {
        if (!isAdmin()) { denyAccess(); return; }
        loadView("/view/Inventory.fxml");
    }

    @FXML public void goToCashier() { loadView("/view/Cashier.fxml"); }

    @FXML
    public void goToReport() {
        if (!isAdmin()) { denyAccess(); return; }
        loadView("/view/Report.fxml");
    }

    private void denyAccess() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Akses Ditolak");
        alert.setHeaderText(null);
        alert.setContentText("Menu ini hanya dapat diakses oleh Admin/Owner.");
        alert.showAndWait();
        loadView("/view/Dashboard.fxml");
    }

    @FXML
    public void handleLogout() {
        UserSession.getInstance().logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) contentArea.getScene().getWindow();
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setFullScreen(true);
            stage.setTitle("Bengkel Management System");
            AnimationUtil.fadeIn(root, 350);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlPath) {
        try {
            Node view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(view);
            AnimationUtil.fadeInSlideUp(view, 260);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}