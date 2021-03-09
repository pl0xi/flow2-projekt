package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable {
    protected Socket clientSocket;
    protected PrintWriter out;
    protected BufferedReader in;
    protected boolean runStatus;
    protected String user;
    protected boolean connected = false;
    public static ConcurrentHashMap<String, PrintWriter> clients = new ConcurrentHashMap<>();

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.runStatus = true;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(runStatus) {
            try {
                String tmpInput = in.readLine();
                if(tmpInput != null) {
                    int letterPos = tmpInput.indexOf("#");
                    if(letterPos != -1) {
                        String command = tmpInput.substring(0, letterPos);

                        ArrayList args = new ArrayList<String>();
                        int indexStart = letterPos;
                        while(indexStart != -1) {
                            if(tmpInput.indexOf("#", indexStart + 1) != -1) {
                                args.add(tmpInput.substring(indexStart + 1, tmpInput.indexOf("#", indexStart + 1)));
                            } else {
                                args.add(tmpInput.substring(indexStart + 1));
                            }

                            indexStart = tmpInput.indexOf("#", indexStart + 1);
                        }

                        commandController(command, args);
                    }
                } else if (tmpInput == null) {
                    connected = false;
                    clients.remove(user);
                    clientSocket.close();
                    runStatus = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void commandController(String command, ArrayList<String> args) {
        switch (command.toUpperCase()) {
            case "CONNECT":
                if(!connected) {
                    if(args.get(0) != null) {
                        user = args.get(0);
                        connected = true;
                        clients.put(user, out);
                        onlineCommand();
                        out.println("You are now connected");
                    } else {
                        out.println("CLOSE#2");
                    }
                } else {
                    out.println("You are already connected");
                }
                break;
            default:
                break;
        }
    }

    public boolean isConnected() {
        return connected;
    }

    protected void onlineCommand() {
      clients.forEach((k, v) -> {
          v.println("ONLINE:");
          clients.forEach((k_, v_) -> {
              v.println(k_);
          });
      });
    }
}
