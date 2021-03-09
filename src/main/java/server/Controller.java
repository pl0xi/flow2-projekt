package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Controller implements Runnable{
    protected ServerSocket serverSocket;

    public Controller(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        while(true) {
            Thread tmpThread = null;
            try {
                Socket tmpSocket = serverSocket.accept();
                if(tmpSocket != null) {
                    tmpThread = new Thread(new ClientHandler(tmpSocket));
                    tmpThread.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
