package c195.controller;


import c195.dao.UserDAO;
import c195.util.Logger;
import c195.util.NavigationHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;

/**
 * View Controller for Login View
 * @author Jonathan Dowdell
 */
public class LoginViewController implements Initializable {

    @FXML
    public Label titleLabel;

    @FXML
    public TextField userIDTextField;

    @FXML
    public TextField passwordTextField;

    @FXML
    public Label zoneIDLabel;

    @FXML
    public Button signInButton;

    private String alertTitle;
    private String alertHeader;
    private String alertContext;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Locale locale = Locale.getDefault();
        resourceBundle = ResourceBundle.getBundle("Languages", locale);
        String username = resourceBundle.getString("username");
        String password = resourceBundle.getString("password");
        titleLabel.setText(resourceBundle.getString("title"));
        final String displayName = TimeZone.getDefault().getDisplayName();
        zoneIDLabel.setText(displayName);
        userIDTextField.setPromptText(username);
        passwordTextField.setPromptText(password);
        signInButton.setText(resourceBundle.getString("login"));
        alertTitle = resourceBundle.getString("alertTitle");
        alertHeader = resourceBundle.getString("alertHeader");
        alertContext = resourceBundle.getString("alertContext");
    }

    /**
     * Login Using User ID and Password
     * @author Jonathan Dowdell
     */
    @FXML
    public void login(ActionEvent event) throws IOException {
        String userID = userIDTextField.getText();
        String password = passwordTextField.getText();
        Boolean validUser = UserDAO.login(userID, password);
        Logger.log(userID, validUser, "Login Attempt");
        if (validUser) {
            System.out.println("Logged In");
            NavigationHelper.mainView(event);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(alertTitle);
            alert.setHeaderText(alertHeader);
            alert.setContentText(alertContext);
            alert.showAndWait();
        }
    }

}
