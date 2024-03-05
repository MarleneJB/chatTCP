
package com.mycompany.chatttcp;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TCPServer {
    private ServerSocket serverSocket;
    private HashMap<String, Socket> clients = new HashMap<>();
    private ArrayList<String> users = new ArrayList<>();
    
    public TCPServer(String ipAddress,int port, int backlog) throws IOException {
        serverSocket = new ServerSocket(port,backlog, InetAddress.getByName(ipAddress));
    }

    private void handleConnection(Socket clientSocket, String username) throws IOException {
        String clientKey = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
        clients.put(clientKey, clientSocket);
        users.add(username);

        System.out.println("Cliente conectado: " + clientKey);
        broadcast("Cliente " + clientKey + " se ha conectado");
    }

    private void handleDisconnection(Socket clientSocket) throws IOException {
        String clientKey = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
        clients.remove(clientKey);
        users.remove(users.indexOf(clientKey));

        System.out.println("Cliente desconectado: " + clientKey);
        broadcast("Cliente " + clientKey + " se ha desconectado");
    }


    public void sendData(String message, Socket clientSocket) throws IOException {
        PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
        writer.println(message);
    }

    public void processData(Socket clientSocket) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String received = reader.readLine();

        String[] lstEntradaServidor = received.split(",");
        String message = lstEntradaServidor[0];
        String username = lstEntradaServidor[1];

        String clientKey = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();

        if (message.startsWith("CONNECT")) {
            handleConnection(clientSocket, username);
        } else if (message.startsWith("DISCONNECT")) {
            handleDisconnection(clientSocket);
        } else if(message.startsWith("PRIVADO")) {
            String recipientKey = message.split(" ")[1];
            String mensaje = message.split(recipientKey)[1];
            String send = username + ": " + mensaje;
            sendPrivateMessage(send, recipientKey);
        } else {
            broadcast(clientKey + ": " + message);
        }
    }

    public void broadcast(String message) throws IOException {
        for (Map.Entry<String, Socket> entry : clients.entrySet()) {
            Socket clientSocket = entry.getValue();
            sendData(message, clientSocket);
        }
    }

    public void sendPrivateMessage(String message, String recipientKey) throws IOException {
        int index = users.indexOf(recipientKey);
        Socket recipientSocket = clients.get(users.get(index));

        if (recipientSocket != null) {
            sendData(message, recipientSocket);
        }
    }

    public void listen() {
        System.out.println("Servidor iniciado. Escuchando en el puerto: " + serverSocket.getLocalPort());

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                processData(clientSocket);
            } catch (IOException e) {
                System.err.println("Error en la conexión: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        int port = 12345; 
        String ipAddress = "192.168.137.226";
        int backlog = 40;
        try {
            TCPServer server = new TCPServer(ipAddress, port,backlog);
            server.listen();
        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }
}
