package pg.project.bsk.client;

import javafx.fxml.FXMLLoader;
import pg.project.bsk.Controller.Controller;
import pg.project.bsk.Main;

import java.net.*;
import java.io.*;

public class Client extends Thread {
    private Controller controller;

    Socket client;

    InputStream inFromServer;
    DataInputStream input;

    OutputStream outToServer;
    DataOutputStream output;

    private static Client instance;

    public static synchronized  Client getInstance(Controller controller) throws Exception {
        if (instance==null)
            instance = new Client(controller,"127.0.0.1",1234);
        return instance;
    }

    public Client(Controller _controller, String ip, int port) throws Exception{
        client = null;
        client = new Socket(ip, port);

        inFromServer = client.getInputStream();
        input = new DataInputStream(inFromServer);
        outToServer = client.getOutputStream();
        output = new DataOutputStream(outToServer);

        controller = _controller;
    }

    public static void startClient(Controller controller) throws Exception{
        // ? do this in a seperate process?
        Thread t = getInstance(controller);
        t.start();

    }

    public void run() {
        try{
            output.writeUTF("Client connected!");
            do{
                String message = input.readUTF();
                if(!message.isEmpty()) getMessage(message);
            }while (true);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void getMessage(String message) {
        controller.updateMainTextArea(message);
    }

    public void sendMessage(String message) throws IOException {
        output.writeUTF(message);
    }

}
