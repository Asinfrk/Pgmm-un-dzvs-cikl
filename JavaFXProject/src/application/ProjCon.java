package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ProjCon {

    @FXML private Button btn;
    @FXML private PasswordField password;
    @FXML private TextField user_name;
    @FXML private Label errorLabel;

    public void initialize() {

        btn.setOnAction(event -> handleLogin());

        // Allow pressing Enter in the password field to trigger login
        password.setOnAction(event -> handleLogin());
        user_name.setOnAction(event -> password.requestFocus());

        // Hover effect
        String baseStyle =
            "-fx-background-color:#c9a84c;-fx-text-fill:#0d0d1a;-fx-font-weight:bold;" +
            "-fx-font-size:13;-fx-background-radius:6;-fx-padding:11 0;-fx-cursor:hand;";
        String hoverStyle =
            "-fx-background-color:#e0bf6e;-fx-text-fill:#0d0d1a;-fx-font-weight:bold;" +
            "-fx-font-size:13;-fx-background-radius:6;-fx-padding:11 0;-fx-cursor:hand;";
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
    }

    private void handleLogin() {

        String username = user_name.getText().trim();
        String pwd = password.getText();

        if (username.isEmpty() || pwd.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        if (authenticateWithDatabase(username, pwd)) {
            openMainWindow();
        } else {
            showError("Invalid username or password.");
            password.clear();
        }
    }

    /**
     * Checks the users table in MySQL.
     */
     
    private boolean authenticateWithDatabase(String username, String password) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT id FROM users WHERE username = ? AND password = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);   
            ResultSet rs = ps.executeQuery();
            boolean found = rs.next();
            rs.close();
            ps.close();
            return found;
        } catch (Exception e) {
            // DB unavailable – fall back to hardcoded credentials for development
            System.err.println("DB login failed, using fallback: " + e.getMessage());
            return username.equals("admin") && password.equals("admin");
        }
    }

    private void openMainWindow() {
        try {
            Stage newStage = new Stage();
            Parent root = FXMLLoader.load(getClass().getResource("Projmainlay.fxml"));
            Scene scene = new Scene(root, 1100, 820);
            newStage.setScene(scene);
            newStage.setTitle("Basketball League Manager");
            newStage.show();
            Main.globalStage.close();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to open application.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

}