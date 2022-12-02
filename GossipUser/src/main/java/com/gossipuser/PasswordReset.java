package com.gossipuser;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.effects.DepthLevel;
import io.github.palexdev.materialfx.enums.ButtonType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class PasswordReset {

    @FXML
    private MFXTextField email;

    @FXML
    private MFXPasswordField oldpass;

    @FXML
    private MFXPasswordField newpass;

    @FXML
    private MFXButton resetBtn;

    @FXML
    private AnchorPane verdict;

    @FXML
    void ResetOnClick(ActionEvent event) {
        String mail = dollarFilter(email.getText());
        String oldpassword = dollarFilter(oldpass.getText());
        String newpassword = dollarFilter(newpass.getText());

        MFXButton prompt = new MFXButton();
        prompt.setDepthLevel(DepthLevel.LEVEL3);
        prompt.setButtonType(ButtonType.RAISED);
        prompt.setWrapText(true);

        if (!validEmail(mail)) {
            prompt.setText("Email must be valid");
            verdict.getChildren().clear();
            verdict.getChildren().add(prompt);
        }
        else if (oldpassword.isEmpty() || newpassword.isEmpty()) {
            prompt.setText("Passwordfield is blank");
            verdict.getChildren().clear();
            verdict.getChildren().add(prompt);
        }
        else if (oldpassword.equals(newpassword)) {
            prompt.setText("Passwords must be different");
            verdict.getChildren().clear();
            verdict.getChildren().add(prompt);
        }
        else {
            try {
                Client client = new Client();
                client.sendMessage("resetpassword" + "$" + mail + "$" + oldpassword + "$" + newpassword);

                String result = client.readMessage();

                if (result.equals("true")) {
                    getBackToSignInPage(event);
                }
                else {
                    prompt.setText("Do you really have an account?");
                    verdict.getChildren().clear();
                    verdict.getChildren().add(prompt);
                }
            }
            catch (Exception e) {
                prompt.setText("Facing issues while resetting password");
                verdict.getChildren().clear();
                verdict.getChildren().add(prompt);
            }
        }
    }
    void getBackToSignInPage(ActionEvent event) throws IOException {
        Parent account_window = FXMLLoader.load(getClass().getResource("signIn.fxml"));
        Scene scene = new Scene(account_window);
        Stage signInStage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        signInStage.setScene(scene);
        signInStage.show();
    }

    boolean validEmail(String mail) {
        String[] tokens = mail.strip().split("@");
        return (tokens.length == 2);
    }
    String dollarFilter(String token) {
        String res = "";
        for (int i = 0; i < token.length(); i++) {
            if (token.charAt(i) == '$') res += ' ';
            else res += token.charAt(i);
        }
        return res;
    }
}
