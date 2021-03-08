package server;


import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

public class ChatServer {


    //Call server with arguments like this: 8088
    public static void main(String[] args) throws UnknownHostException {
        int port = 8088;
        try {
            if (args.length == 1) {
                port = Integer.parseInt(args[0]);
            }
            else {
                throw new IllegalArgumentException("Server not provided with the right arguments");
            }
        } catch (NumberFormatException ne) {
            System.out.println("Illegal inputs provided when starting the server!");
            return;
        }

    }


}