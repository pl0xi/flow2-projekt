package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable {
    protected Socket clientSocket;
    protected PrintWriter out;
    protected BufferedReader in;
    protected boolean runStatus;
    protected String user;
    protected boolean connected = false;
    protected int counter = 0;
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
                        String args [] = tmpInput.substring(letterPos + 1).split("#");
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

    protected void commandController(String command, String [] args) {
        switch (command.toUpperCase()) {
            case "CONNECT":
                if(!connected) {
                    if(args.length == 1) {
                        final boolean[] userFound = {false};
                        clients.forEach((k,v) -> {
                            if(args[0].toUpperCase().equals(k.toUpperCase())) {
                                userFound[0] = true;
                            }
                        });

                        if(!userFound[0]) {
                            user = args[0];
                            clients.put(user, out);
                            onlineCommand();
                            connected = true;
                            out.println("You are now connected");
                        } else {
                            out.println("Username is occupied");
                        }
                    } else {
                        out.println("CLOSE#2");
                    }
                } else {
                    out.println("You are already connected");
                }
                break;
            case "SEND":
                if(connected) {
                    if(args.length == 2) {
                        String usersRecevingMsg = args[0];
                        String message = args[1];
                        sendCommand(usersRecevingMsg, message);
                    } else {
                        out.println("Invalid arguments");
                    }
                }
                break;
            case "CLOSE":
                connected = false;
                clients.remove(user);
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                runStatus = false;
                break;
            default:
                break;
        }
    }


    protected void onlineCommand() {
        int maxClients = clients.size();
        clients.forEach((k, v) -> {
            counter = 0;
            v.print("ONLINE#");
            clients.forEach((k_, v_) -> {
                v.print(k_);
                counter++;
                if(counter != maxClients) {
                    v.print(",");
                } else {
                    v.println();
                }
            });
        });
    }

    protected void sendCommand(String receivers, String message) {
        String [] receiversProccesed = new String[0];
        boolean sendToEveryone = false;

        if(receivers.equals("*")) {
            sendToEveryone = true;
        } else {
            receiversProccesed = receivers.split(",");
        }
        

        if(sendToEveryone) {
            clients.forEach((k, v) -> {
                if(!k.toUpperCase().equals(user.toUpperCase())) {
                    v.println("MESSAGE#" + user + "#" + message);
                }
            });
        } else {
            for(String userReceiving_: receiversProccesed) {
                clients.forEach((k, v) -> {
                    if(userReceiving_.toUpperCase().equals(k.toUpperCase())) {
                        v.println("MESSAGE#" + user + "#" +  message);
                    }
                });
            }
        }
    }
}
