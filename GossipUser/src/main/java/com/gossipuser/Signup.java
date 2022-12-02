package com.gossipuser;

import io.github.palexdev.materialfx.controls.*;
import io.github.palexdev.materialfx.effects.DepthLevel;
import io.github.palexdev.materialfx.enums.ButtonType;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Border;
import javafx.stage.Stage;

import java.io.IOException;

public class Signup {

    @FXML
    private MFXTextField email;

    @FXML
    private MFXPasswordField password;

    @FXML
    private MFXPasswordField repeatPassword;

    @FXML
    private MFXButton signupBtn;

    @FXML
    private AnchorPane verdict;
    @FXML
    private AnchorPane progress;
    @FXML
    private MFXTextField NameField;
    @FXML
    void signupOnClick(ActionEvent event) {
        String mail = dollarFilter(email.getText().toString());
        String npass = dollarFilter(password.getText().toString());
        String rpass = dollarFilter(repeatPassword.getText().toString());
        String name = dollarFilter(NameField.getText().toString());

        MFXButton prompt = new MFXButton();
        prompt.setDepthLevel(DepthLevel.LEVEL3);
        prompt.setButtonType(ButtonType.RAISED);
        prompt.setWrapText(true);

        MFXProgressSpinner progressBar = new MFXProgressSpinner();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        progress.getChildren().clear();
                        progress.getChildren().add(progressBar);
                    }
                });
                if (!validEmail(mail)) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            verdict.getChildren().clear();
                            prompt.setText("Email must be valid");
                            verdict.getChildren().add(prompt);
                        }
                    });
                }
                else if (name.isEmpty()) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            verdict.getChildren().clear();
                            prompt.setText("Name field is blank");
                            verdict.getChildren().add(prompt);
                        }
                    });
                }
                else if (npass.isEmpty() || rpass.isEmpty()) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            verdict.getChildren().clear();
                            prompt.setText("Password field is blank");
                            verdict.getChildren().add(prompt);
                        }
                    });
                }
                else if (npass.equals(rpass)) {
                    try {
                        Client client = new Client();
                        client.sendMessage("signup" + "$" + mail + "$" + npass + "$" + name);

                        String rMsg = client.readMessage();

                        if (rMsg.equals("true")) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        getBackToSignInPage(event);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            });
                        }
                        else {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    verdict.getChildren().clear();
                                    prompt.setText("Facing error while Creating account");
                                    verdict.getChildren().add(prompt);
                                }
                            });
                        }
                    }
                    catch (Exception e) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                verdict.getChildren().clear();
                                prompt.setText("Email exists or server problem");
                                verdict.getChildren().add(prompt);
                            }
                        });
                    }
                }
                else {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            verdict.getChildren().clear();
                            prompt.setText("Password doesn't match");
                            verdict.getChildren().add(prompt);
                        }
                    });
                }
            }
        }).start();
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
