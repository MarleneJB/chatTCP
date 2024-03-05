package com.mycompany.chatttcp;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TCPClient {
    private Socket socket;
    private String username;
    private PrintWriter writer;
    private BufferedReader reader;

    public TCPClient(String serverIP, int serverPort, String username) throws IOException {
        socket = new Socket(serverIP, serverPort);
        this.username = username;

        writer = new PrintWriter(socket.getOutputStream(), true);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Enviar mensaje de conexión
        sendMessage("CONNECT");
    }

    public void sendMessage(String message) {
        writer.println(message + "," + username);
    }

    public void receiveMessages() {
        new Thread(() -> {
            try {
                while (true) {
                    String receivedMessage = reader.readLine();
                    System.out.println(receivedMessage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void disconnect() {
        // Enviar mensaje de desconexión
        sendMessage("DISCONNECT");

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Dirección IP del servidor: ");
        String serverIP = scanner.nextLine();

        
        int serverPort = 12345;

        System.out.print("Ingresa tu nombre de usuario: ");
        scanner.nextLine();
        String username = scanner.nextLine();

        TCPClient client = new TCPClient(serverIP, serverPort, username);
        client.receiveMessages();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            client.disconnect();
        }));
        while (true) {
            System.out.print("Mensaje: ");
            String message = scanner.nextLine();
            client.sendMessage(message);
        }
    }
}
