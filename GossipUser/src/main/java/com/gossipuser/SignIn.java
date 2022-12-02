package com.gossipuser;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXProgressBar;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.effects.DepthLevel;
import io.github.palexdev.materialfx.enums.ButtonType;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.Stack;

public class SignIn implements Initializable{
    @FXML
    private ImageView gossipLogo;
    @FXML
    private MFXTextField emailField;

    @FXML
    private MFXPasswordField passwordField;

    @FXML
    private MFXButton signInBtn;

    @FXML
    private MFXButton createBtn;

    @FXML
    private MFXButton reset;

    @FXML
    private AnchorPane result;
    @FXML
    private AnchorPane result1;

    @FXML
    void createNewAccountOnClick(ActionEvent event) throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Parent create_account_window = null;
                        try {
                            create_account_window = FXMLLoader.load(getClass().getResource("signup.fxml"));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        Scene scene = new Scene(create_account_window);
                        Stage signupStage = (Stage) ((Node)event.getSource()).getScene().getWindow();
                        signupStage.setScene(scene);

                        signupStage.show();
                    }
                });
            }
        }).start();
    }

    @FXML
    void resetPasswordOnClick(ActionEvent event) throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Parent create_account_window = null;
                        try {
                            create_account_window = FXMLLoader.load(getClass().getResource("passwordReset.fxml"));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        Scene scene = new Scene(create_account_window);
                        Stage passwordStage = (Stage) ((Node)event.getSource()).getScene().getWindow();
                        passwordStage.setScene(scene);
                        passwordStage.show();
                    }
                });
            }
        }).start();
    }

    @FXML
    void signInOnClick(ActionEvent event) {
        String email = dollarFilter(emailField.getText().toString());
        String password = dollarFilter(passwordField.getText().toString());

        MFXProgressBar progressBar = new MFXProgressBar();

        MFXButton prompt = new MFXButton();
        prompt.setDepthLevel(DepthLevel.LEVEL3);
        prompt.setButtonType(ButtonType.RAISED);
        prompt.setWrapText(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        result.getChildren().clear();
                        result.getChildren().add(progressBar);
                    }
                });

                if (!validEmail(email)) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            result1.getChildren().clear();
                            result1.getChildren().add(prompt);
                            prompt.setText("Email must be valid");
                        }
                    });
                }
                else if (password.isEmpty()) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            result1.getChildren().clear();
                            result1.getChildren().add(prompt);
                            prompt.setText("PasswordField is blank");
                        }
                    });
                }
                else {
                    try {
                        Client client = new Client();
                        client.sendMessage("signin" + "$" + email + "$" + password);

                        String rMsg = client.readMessage();
                        client.close();
                        if (rMsg.equals("true")) {
                            FileWriter file = new FileWriter("usermail.txt");
                            file.write(email);
                            file.close();

                            Client client1 = new Client();
                            client1.sendMessage("updateactivestatus$" + email + "$" + timeStringNow());
                            client1.close();

                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        getBackToMessengerPage(event);
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
                                    result1.getChildren().clear();
                                    prompt.setText("Can't log in, Try again");
                                    result1.getChildren().add(prompt);
                                }
                            });
                        }
                    }
                    catch (Exception e) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                result1.getChildren().clear();
                                result1.getChildren().add(prompt);
                                prompt.setText("Client request error");
                            }
                        });
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        gossipLogo.setImage(new Image(new File("src/icons/icons8-facebook-messenger-100.png").toURI().toString()));
    }

    boolean validEmail(String mail) {
        String[] tokens = mail.strip().split("@");
        return (tokens.length == 2);
    }
    void getBackToMessengerPage(ActionEvent event) throws IOException {
        Parent account_window = FXMLLoader.load(getClass().getResource("MessengerUI.fxml"));
        Scene scene = new Scene(account_window);
        Stage signInStage = (Stage) ((Node)event.getSource()).getScene().getWindow();
        signInStage.setScene(scene);
        signInStage.show();
    }
    String timeStringNow() {
        String res = String.valueOf(LocalDateTime.now());
        return res.replace('T', ' ');
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