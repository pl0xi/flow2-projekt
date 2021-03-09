package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.UnknownHostException;

public class ChatServer {
    public static void main(String[] args) throws UnknownHostException {
        try {
            if (args.length == 1) {
                int port = Integer.parseInt(args[0]);
                ServerSocket serverSocket = new ServerSocket(port);

                Thread controller = new Thread(new Controller(serverSocket));
                controller.start();
            }
            else {
                throw new IllegalArgumentException("Server not provided with the right arguments");
            }
        } catch (NumberFormatException ne) {
            System.out.println("Illegal inputs provided when starting the server!");
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}