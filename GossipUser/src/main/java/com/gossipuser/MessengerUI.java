package com.gossipuser;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.effects.DepthLevel;
import io.github.palexdev.materialfx.enums.ButtonType;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

public class MessengerUI implements Initializable {

    @FXML
    private MFXTextField NewMessageField;

    @FXML
    private MFXButton SendBtn;

    @FXML
    private Label acitve_status;

    @FXML
    private ImageView gossipLogo;

    @FXML
    private MFXButton searchBtn;

    @FXML
    private MFXTextField searchField;

    @FXML
    private Label user_Label;
    @FXML
    private MFXScrollPane userScroller;
    @FXML
    private MFXScrollPane messageScroller;
    @FXML
    private VBox vbox_message;

    @FXML
    private VBox vbox_userList;

    ArrayDeque<String> userList = new ArrayDeque<String>();
    String searchedUser = "";
    String chatNow = "";
    Thread ReaderThread = null;
    Thread ActiveStatusThread = null;
    @FXML
    void searchBtnOnClick(ActionEvent event) {
        String searchItem = dollarFilter(searchField.getText().toString());
        searchField.clear();

        if (!searchItem.isEmpty()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    check_valid_user(searchItem);
                    allUserList();
                    try {
                        update_vbox_userList();
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }
    }

    @FXML
    void sendBtnOnClick(ActionEvent event) throws IOException {
        String message = dollarFilter(NewMessageField.getText().toString());
        NewMessageField.clear();

        if (!message.isEmpty()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String From = myEmail();
                        String To = chatNow;
                        if (!To.isEmpty()) {
                            Client client = new Client();
                            client.sendMessage("newmessage" + "$" + From + "$" + message + "$" + To + "$" + timeStringNow());
                            client.close();
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    addPrompt(message, true);
                                }
                            });
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    allUserList();
                    try {
                        update_vbox_userList();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        gossipLogo.setImage(new Image(new File("src/icons/icons8-facebook-messenger-100.png").toURI().toString()));
        userScroller.setFitToWidth(true);
        messageScroller.setVvalue(1.0);
        messageScroller.setFitToWidth(true);
        vbox_message.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                messageScroller.setVvalue((double) t1);
            }
        });

        Thread userListThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        allUserList();
                        update_vbox_userList();
                        Thread.sleep(60000);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        userListThread.setDaemon(true);
        userListThread.start();

        ActiveStatusThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Client client = new Client();
                        client.sendMessage("updateactivestatus" + "$" + myEmail() + "$" + timeStringNow());
                        client.close();

                        if (!chatNow.isBlank()) updateUserActiveStatus(chatNow);
                        Thread.sleep(60000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        ActiveStatusThread.setDaemon(true);
        ActiveStatusThread.start();
    }

    void allUserList() {
        try {
            Client client = new Client();
            client.sendMessage("findallfriends" + "$" + myEmail());
            userList.clear();

            Set<String> set = new TreeSet<String>();

            while (true) {
                String request = client.readMessage();
                if (request.equals("end")) break;
                set.add(request);
            }

            if (!searchedUser.isEmpty()) {
                set.add(searchedUser);
            }

            for (String user : set) {
                userList.offerLast(user);
            }
            client.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    void update_vbox_userList() throws ParseException {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                vbox_userList.getChildren().clear();
            }
        });

        for (String user:userList) {
            showAnUserToVBoxUserList(user);
        }
    }
    void check_valid_user(String usermail) {
        try {
            if (!usermail.isEmpty()) {
                Client client = new Client();
                client.sendMessage("validuser" + "$" + usermail);

                String msg = client.readMessage();
                client.close();
                if (msg.equals("true")) {
                    searchedUser = usermail;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    void showAnUserToVBoxUserList(String user1) throws ParseException {
        VBox userdata = new VBox();

        userdata.setAlignment(Pos.TOP_CENTER);
        userdata.setSpacing(10);
        userdata.setPadding(new Insets(7, 7, 7, 7));

        MFXButton nameFlow = new MFXButton();

        nameFlow.setPadding(new Insets(5,5,5,5));
        nameFlow.setStyle("-fx-background-color: #d3d3d3; -fx-background-radius: 18px; -fx-font-weight: bold");
        nameFlow.setAlignment(Pos.CENTER);
        nameFlow.setDepthLevel(DepthLevel.LEVEL0);
        nameFlow.setButtonType(ButtonType.FLAT);
        nameFlow.setWrapText(true);

        nameFlow.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (ReaderThread != null) {
                    ReaderThread.interrupt();
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            showAConversation(user1);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                chatNow = user1;
                ReaderThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            try {
                                Client client = new Client();
                                client.sendMessage("findnewmessage" + "$" + myEmail() + "$" + user1);

                                while (true) {
                                    String request = client.readMessage();
                                    if (request.equals("end")) break;
                                    String[] messageToken = Splitter(request);
                                    addPrompt(messageToken[1], messageToken[0].equals(myEmail()));
                                    Notify();
                                    Thread.sleep(100);
                                }
                                Thread.sleep(2000);
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                ReaderThread.setDaemon(true);
                ReaderThread.start();
            }
        });
        String request = "";
        try {
            Client client = new Client();
            client.sendMessage("lastmessage$" + myEmail() + "$" + user1);
            request = client.readMessage();
            client.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        String Lastmessage = "No message";
        String[] tokens = Splitter(request);
        String seenTime = CalculateDiffTime(createDate(timeStringNow()));
        if (tokens.length > 1) {
            Lastmessage = tokens[1];
            seenTime = CalculateDiffTime(createDate(tokens[3]));
        }

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.TOP_CENTER);
        hBox.setPadding(new Insets(7, 7, 7,7));

        Text LastMsg = new Text(Lastmessage);
        LastMsg.setFill(Color.color(0.502, 0.502, 0.502));
        LastMsg.setStyle("-fx-color:d3d3d3; -fx-font-size: 18px");
        TextFlow LastMsgFlow = new TextFlow(LastMsg);
        LastMsgFlow.setPadding(new Insets(3, 3, 3, 3));
        LastMsgFlow.setStyle("-fx-font-weight: bold; -fx-background-radius: 20px;");

        if (seenTime.equals(" now")) seenTime = ": just now";
        else seenTime = ":" + seenTime + " ago";

        Text seenText = new Text(seenTime);
        seenText.setFill(Color.color(0.502, 0.502, 0.502));
        TextFlow seenFlow = new TextFlow(seenText);
        seenFlow.setStyle("-fx-font-weight: bold; -fx-background-radius: 20px;");
        seenFlow.setPadding(new Insets(3 ,3 ,3, 3));

        Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setPrefWidth(205);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                nameFlow.setText(user1);
                userdata.getChildren().add(nameFlow);
                hBox.getChildren().add(LastMsgFlow);
                hBox.getChildren().add(seenFlow);
                userdata.getChildren().add(hBox);

                vbox_userList.getChildren().add(userdata);
                vbox_userList.getChildren().add(separator);
            }
        });
    }

    void showAConversation(String usermail) throws IOException, ClassNotFoundException, ParseException {
        chatNow = usermail;
        Client client = new Client();
        client.sendMessage("getusername$" + usermail);
        String username = client.readMessage();
        client.close();

        updateUserActiveStatus(usermail);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                user_Label.setText(username);
                vbox_message.getChildren().clear();
            }
        });
        ArrayDeque<String > messageQ = FindAConversation(usermail);
        for (String msg : messageQ) {
            String[] messageToken = Splitter(msg);
            try {
                addPrompt(messageToken[1], messageToken[0].equals(myEmail()));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    ArrayDeque<String> FindAConversation(String usermail) {
        try {
            Client client = new Client();
            client.sendMessage("readallmessage" + "$" + myEmail() + "$" + usermail);
            ArrayDeque<String > msg = new ArrayDeque<String>();

            while (true) {
                String request = client.readMessage();
                if (request.equals("end")) break;
                msg.offerLast(request);
            }
            client.close();
            return msg;
        }
        catch (Exception e) {
            e.printStackTrace();
            return  new ArrayDeque<String>();
        }
    }
    void addPrompt(String message, boolean From) {
        Text text = new Text(message);
        TextFlow textFlow = new TextFlow();

        textFlow.setPadding(new Insets(10, 10, 10, 10));
        text.setStyle("-fx-font-size: 18");

        HBox hBox = new HBox();

        if (From) {
            hBox.setAlignment(Pos.CENTER_RIGHT);
            textFlow.setStyle("-fx-background-color: rgb(0, 132, 255);; -fx-background-radius: 18px");
            text.setFill(Color.color(0.99, 0.99, 0.99));
        }
        else {
            hBox.setAlignment(Pos.CENTER_LEFT);
            textFlow.setStyle("-fx-background-color: #d3d3d3; -fx-background-radius: 18px");
        }

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                textFlow.getChildren().add(text);
                hBox.getChildren().add(textFlow);
                vbox_message.getChildren().add(hBox);
            }
        });
    }

    void updateUserActiveStatus(String user) throws IOException, ClassNotFoundException, ParseException {
        Client client = new Client();
        client.sendMessage("activestatus$" + user);
        String msg = client.readMessage();
        client.close();
        if (msg.isBlank()) return;
        Date date = createDate(msg);

        String timeres = CalculateDiffTime(date);
        if (timeres.equals(" now")) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    acitve_status.setText("Active now");
                }
            });
        }
        else {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    acitve_status.setText("Active" + timeres + " ago");
                }
            });
        }
    }
    String[] Splitter(String res) {
        String word = "";

        int cnt = 1;
        for (int i = 0; i < res.length(); i++) {
            if (res.charAt(i) == '$') cnt++;
        }
        String[] tokens = new String[cnt];

        for (int i = 0, j = 0; i < res.length(); i++) {
            if (res.charAt(i) == '$') {
                tokens[j++] = word;
                word = "";
            }
            else {
                word += res.charAt(i);
            }
        }
        tokens[cnt - 1] = word;
        return tokens;
    }
    String myEmail() throws IOException {
        FileReader reader = new FileReader("usermail.txt");
        String tmp = "";
        int ch;
        while((ch = reader.read()) != -1) {
            tmp += (char)ch;
        }
        reader.close();
        return tmp;
    }
    String CalculateDiffTime(Date date1) throws ParseException {
        long sec = date1.getTime() / 1000;
        Date date = createDate(timeStringNow());
        long current = date.getTime() / 1000;

        long dif = current - sec;

        if (dif < 60) {
            return " now";
        }
        else if (dif < 3600) {
            return " " + dif / 60 + " min";
        }
        else if (dif < 86400) {
            return " " + dif / 3600 + " hour";
        }
        else if (dif < 2592000) {
            return " " + dif / 86400 + " day";
        }
        else if (dif < 31104000) {
            return " " + dif / 2592000 + " mon";
        }
        else {
            return " " + dif / 31104000 + " year";
        }
    }
    Date createDate(String time) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = sdf.parse(time);
        return date;
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

    void Notify() {
        Media media = new Media(new File("src/Sound/Iphone - Notification Sound.mp3").toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setAutoPlay(true);
    }
}
