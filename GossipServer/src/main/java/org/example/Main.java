package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(22222);

        while (true) {
            Socket socket = server.accept();
            new ServerThread(socket);
        }
    }
}