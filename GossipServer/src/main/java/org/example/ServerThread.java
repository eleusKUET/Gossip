package org.example;

import org.bson.Document;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayDeque;
public class ServerThread implements Runnable{
    Thread serverThread;
    Socket response;
    ObjectOutputStream oos;
    ObjectInputStream ois;
    String usermail;
    GossipDB db;
    ServerThread(Socket response) throws IOException {
        serverThread = new Thread(this);
        System.out.println("Server Thread Started..");
        this.response = response;
        ois = new ObjectInputStream(response.getInputStream());
        oos = new ObjectOutputStream(response.getOutputStream());
        db = new GossipDB();
        serverThread.start();
    }

    @Override
    public void run() {
        System.out.println("Running started..");
        try {
            Object messageObj = ois.readObject();
            String request = messageObj.toString();
            String[] tokens = Splitter(request);

            System.out.println(tokens[0] + " request..");

            if (tokens[0].equals("signup")) {
                if (accountCreationRequest(tokens)) {
                    oos.writeObject("true");
                }
                else {
                    oos.writeObject("false");
                }
                response.close();
                return;
            }
            if (tokens[0].equals("resetpassword")) {
                if (passwordResetRequest(tokens)) {
                    oos.writeObject("true");
                }
                else {
                    oos.writeObject("false");
                }
                response.close();
                return;
            }

            if (tokens[0].equals("signin")) {
                if (accountLogInRequest(tokens)) {
                    oos.writeObject("true");
                } else {
                    oos.writeObject("false");
                }
                response.close();
                return;
            }

            if (tokens[0].equals("findallfriends")) {
                ArrayDeque<String > users = db.findMyFriends(tokens[1]);
                for (String user : users) {
                    oos.writeObject(user);
                }
                oos.writeObject("end");
                response.close();
                return;
            }
            if (tokens[0].equals("readallmessage")) {
                ArrayDeque<String> messages = db.readAllMessage(tokens[1], tokens[2]);
                for (String message : messages) {
                    oos.writeObject(message);
                }
                oos.writeObject("end");
                response.close();
                return;
            }
            if (tokens[0].equals("validuser")) {
                if (db.validUser(tokens[1])) oos.writeObject("true");
                else oos.writeObject("false");
                response.close();
                return;
            }
            if (tokens[0].equals("newmessage")) {
                Document doc = new Document("from", tokens[1]).append("message", tokens[2]).append("to", tokens[3]).append("time", tokens[4]);
                if (db.writeANewMessageToNewMessageDB(tokens[1], tokens[3], doc)) {
                    db.writeMessage(tokens[1], tokens[3], doc);
                    System.out.println("successfully received new message " + request);
                }
                else {
                    System.out.println("newmessage writing failed..");
                }
                response.close();
                return;
            }
            if (tokens[0].equals("findnewmessage")) {
                ArrayDeque<String> newmsg = db.readNewMessages(tokens[2], tokens[1]);
                while (!newmsg.isEmpty()) {
                    String msg = newmsg.peekFirst();
                    newmsg.pollFirst();
                    String[] tok = Splitter(msg);
                    Document doc = new Document("from", tok[0]).append("message", tok[1]).append("to", tok[2]).append("time", tok[3]);
                    db.deleleAMessageFromNewMessageDB(tokens[2], tokens[1], doc);
                    db.writeMessage(tokens[1], tokens[2], doc);
                    oos.writeObject(msg);
                }
                oos.writeObject("end");
                response.close();
                return;
            }
            if (tokens[0].equals("lastmessage")) {
                String lastMsg = db.lastMessage(tokens[1], tokens[2]);
                if (!lastMsg.isEmpty()) oos.writeObject(lastMsg);
                oos.writeObject("end");
                response.close();
                return;
            }
            if (tokens[0].equals("activestatus")) {
                String status = db.getActiveStatus(tokens[1]);
                oos.writeObject(status);
                response.close();
                return;
            }
            if (tokens[0].equals("updateactivestatus")) {
                db.updateActiveStatus(tokens[1], tokens[2]);
                response.close();
                return;
            }
            if (tokens[0].equals("getusername")) {
                String username = db.findThisUserName(tokens[1]);
                if (!username.isEmpty()) oos.writeObject(username);
                else oos.writeObject("false");
                response.close();
                return;
            }
            if (tokens[0].equals("unsend")) {
                if (db.unsendMessage("messagedb", tokens[1], tokens[3], tokens[1], tokens[3], tokens[2], tokens[4])) {
                    if (db.unsendMessage("messagedb", tokens[3], tokens[1], tokens[1], tokens[3], tokens[2], tokens[4])) {
                        if (db.unsendMessage("newmessagedb", tokens[1], tokens[3], tokens[1], tokens[3], tokens[2], tokens[4])) {
                            if (db.unsendMessage("newmessagedb", tokens[3], tokens[1], tokens[1], tokens[3], tokens[2], tokens[4])) {
                                oos.writeObject("true");
                            }
                            else {
                                oos.writeObject("false");
                            }
                        }
                        else  {
                            oos.writeObject("false");
                        }
                    }
                    else  {
                        oos.writeObject("false");
                    }
                }
                else {
                    oos.writeObject("false");
                }
                response.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    boolean accountCreationRequest(String[] request) {
        String email = request[1];
        String password = request[2];
        String username = request[3];
        return db.createUser(email, password, username);
    }
    boolean passwordResetRequest(String[] request) {
        String email = request[1];
        String oldpass = request[2];
        String newpass = request[3];
        return db.UpdateUserPassword(email, oldpass, newpass);
    }
    boolean accountLogInRequest(String[] request) {
        String email = usermail = request[1];
        String password = request[2];

        return db.logIn(email, password);
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
}
