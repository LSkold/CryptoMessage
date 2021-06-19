package pg.project.bsk.server;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import pg.project.bsk.Controller.Controller;
import pg.project.bsk.Decryptor.AES;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.net.*;
import java.io.*;
import java.security.NoSuchAlgorithmException;
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
            // ? do this in a seperate process?
            Thread t = getInstance(controller);
            t.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while(true) {
            try {
            /*
            System.out.println("service: " + service);
            System.out.println("input1: " + input1);
            System.out.println("input2: " + input2);
            */
                server = serverSocket.accept();
                System.out.println("Just connected to " + server.getRemoteSocketAddress());

                input = new DataInputStream(server.getInputStream());
                output = new DataOutputStream(server.getOutputStream());
                //output.writeUTF("Thank you for connecting to " + server.getLocalSocketAddress());

                byte[] buffer = new byte[1024];
                int len;

                do {
                    while((len = input.read(buffer)) > 0)
                    {
                        getMessage(buffer);
                    }
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
    private String menu() {
        return "\tMath Server\n***************************\nchoose a number for the coresponding service\nthen send response in this format\n\n\tservice: (int)\n\tinput1: (int)\n\tinput2: (int)\n\n 0. Quit\n 1. Print this help message\n 2. Addition\n 3. Subtraction\n 4. Multiplication\n 5. Division";
    }

    private int add(int a, int b) {
        return a + b;
    }

    private int diff(int a, int b) {
        return a - b;
    }

    private int mult(int a, int b) {
        return a*b;
    }

    private double qout(int a, int b) {
        return (double)a/b;
    }

    public void sendMessage(byte[] message) throws IOException {

        byte[] tmp = AES.encrypt(message, controller.getCurrentDecryptionType());
        if (tmp != null) {
            output.write(tmp);// .writeUTF(tmp);
        }
    }

    public void getMessage(byte[] message) {
       controller.updateMainTextArea(message);
       byte[] tmp = AES.decrypt(message, controller.getCurrentDecryptionType());
        //controller.updateMainTextArea(tmp);
    }
}

