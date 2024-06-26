package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferReader;
    private BufferedWriter bufferWriter;
    private String clientUsername;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferReader.readLine();
            clientHandlers.add(this);
            broadcastMessage("Server: " + clientUsername + " has entered the chat!");
        } catch (IOException error) {
            closeEverything(socket, bufferReader, bufferWriter);
        }
    }

    @Override
    public void run() {
        String messageFromClient;
        while (socket.isConnected()) {
            try {
                messageFromClient = bufferReader.readLine();
                broadcastMessage(messageFromClient);
            } catch (IOException error) {
                closeEverything(socket, bufferReader, bufferWriter);
                break;
            }
        }
    }

    public void broadcastMessage(String messageToSend) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (!clientHandler.clientUsername.equals(clientUsername)) {
                    clientHandler.bufferWriter.write(messageToSend);
                    clientHandler.bufferWriter.newLine();
                    clientHandler.bufferWriter.flush();
                }
            } catch (IOException error) {
                closeEverything(socket, bufferReader, bufferWriter);
            }
        }
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage("Server: " + clientUsername + " has left the chat");
    }

    public void closeEverything(Socket socket, BufferedReader bufferReader, BufferedWriter bufferWriter) {
        removeClientHandler();
        try {
            if (bufferReader != null) {
                bufferReader.close();
            }
            if (bufferWriter != null) {
                bufferWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException error) {
            error.printStackTrace();
        }
    }
}
