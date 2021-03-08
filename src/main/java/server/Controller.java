package server;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Controller implements Runnable{
    ServerSocket serverSocket;
    Socket clientSocket;
    PrintWriter outM;
    BufferedReader in;

    public Controller(ServerSocket serverSocket, Socket clientSocket, PrintWriter outM, BufferedReader in) {
        this.serverSocket = serverSocket;
        this.clientSocket = clientSocket;
        this.outM = outM;
        this.in = in;
    }

    @Override
    public void run() {

    }
}
