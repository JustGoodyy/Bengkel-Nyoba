package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.User;
import model.UserSession;
import util.AnimationUtil;
import util.XmlDatabase;
import java.util.ArrayList;

public class LoginController {

    @FXML private VBox loginCard;
    @FXML private Button btnLogin;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    private final XmlDatabase<User> userDb = new XmlDatabase<>("data/users.xml", User.class);

    @FXML
    public void initialize() {
        AnimationUtil.fadeIn(loginCard, 450); 
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        AnimationUtil.pulse(btnLogin); 

        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Username dan password wajib diisi.");
            lblError.setVisible(true);
            AnimationUtil.shake(loginCard);
            return;
        }

        ArrayList<User> users = userDb.loadAll();
        User matched = null;
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username) && u.getPassword().equals(password)) {
                matched = u;
                break;
            }
        }

        if (matched == null) {
            lblError.setText("Username atau password salah.");
            lblError.setVisible(true);
            AnimationUtil.shake(loginCard);
            return;
        }

        UserSession.getInstance().setCurrentUser(matched);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainLayout.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) txtUsername.getScene().getWindow();
            Scene scene = new Scene(root, 1100, 700);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setFullScreen(true);
            stage.setTitle("Bengkel Management System - " + matched.getRole());
            AnimationUtil.fadeIn(root, 350);
        } catch (Exception e) {
            e.printStackTrace();
            lblError.setText("Gagal membuka aplikasi: " + e.getMessage());
            lblError.setVisible(true);
            AnimationUtil.shake(loginCard);
        }
    }
}
