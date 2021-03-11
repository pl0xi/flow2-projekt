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
                        clients.put(user, out);
                        onlineCommand();
                        connected = true;
                        out.println("You are now connected");
                    } else {
                        out.println("CLOSE#2");
                    }
                } else {
                    out.println("You are already connected");
                }
                break;
            case "SEND":
                if(connected) {
                    if(args.get(0) != null && args.get(1) != null) {
                        String usersRecevingMsg = args.get(0);
                        String message = args.get(1);
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
        int indexStart = 0;
        ArrayList<String> receiversProccesed = new ArrayList<>();
        boolean sendToEveryone = false;

        if(receivers.equals("*")) {
            sendToEveryone = true;
        } else {
            while(receivers.indexOf(",", indexStart + 1 ) != -1) {
                if(indexStart == 0) {
                    receiversProccesed.add(receivers.substring(indexStart, receivers.indexOf(",", indexStart)));
                } else {
                    receiversProccesed.add(receivers.substring(indexStart + 1, receivers.indexOf(",", indexStart + 1)));
                }

                indexStart = receivers.indexOf(",", indexStart + 1);
            }
        }

        if(indexStart == 0) {
            receiversProccesed.add(receivers);
        } else {
            receiversProccesed.add(receivers.substring(indexStart + 1));
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
