package com.gossipuser;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
    ObjectInputStream ois;
    ObjectOutputStream oos;
    Socket client;
    Client() throws IOException {
        client = new Socket("localhost", 22222);
        oos = new ObjectOutputStream(client.getOutputStream());
        ois = new ObjectInputStream(client.getInputStream());
    }
    public void sendMessage(String msg) throws IOException {
        oos.writeObject(msg);
    }
    public String readMessage() throws IOException, ClassNotFoundException {
        return ois.readObject().toString();
    }
    public void close() throws IOException {
        client.close();
    }
}
