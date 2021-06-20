package pg.project.bsk.server;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import pg.project.bsk.Controller.Controller;
import pg.project.bsk.Decryptor.AES;

import java.net.*;
import java.io.*;
import java.util.Objects;

public class Server extends Thread {

    private Controller controller;
    private ServerSocket serverSocket;

    DataInputStream input;
    DataOutputStream output;
    Socket server;

    private static Server instance;

    public static synchronized Server getInstance(Controller controller) throws Exception {
        if(instance == null)
            instance = new Server(controller, 1234);
        return instance;
    }

    public Server(Controller _controller, int port) throws Exception {
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(1000000);

        System.out.println("Waiting for client on port " +
                serverSocket.getLocalPort());

        controller = _controller;
    }

    public static void startServer(Controller controller) {
        try {
            Thread t = getInstance(controller);
            t.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while(true) {
            try {
                server = serverSocket.accept();
                System.out.println("Just connected to " + server.getRemoteSocketAddress());

                input = new DataInputStream(server.getInputStream());
                output = new DataOutputStream(server.getOutputStream());
                sendMessage("Thank you for connecting to " + server.getLocalSocketAddress());
                do {
                    String message = input.readUTF();
                    if(!message.isEmpty())
                        getMessage(message);
                } while (true);
            } catch (SocketTimeoutException s) {

                System.out.println("Socket timed out");
                break;

            } catch (IOException e) {

                e.printStackTrace();
                break;

            }
        }
    }

    public void sendMessage(String message) throws IOException {
        String tmp = AES.encrypt(message.getBytes("UTF-8"), controller.getCurrentDecryptionType());
        System.out.println(tmp);
        output.writeUTF(tmp);
    }

    public void sendMessage(byte[] message) throws IOException {
        String tmp = AES.encrypt(message, controller.getCurrentDecryptionType());
        System.out.println(tmp);
        output.writeUTF(tmp);
    }

    public void getMessage(String message) throws IOException {
        byte[] tmp = AES.decrypt(message.getBytes("UTF-8"), controller.getCurrentDecryptionType());
        controller.updateMainTextArea(new String(tmp));
        //TODO:: logic to receiving files needs to be implemented here!
    }
}

