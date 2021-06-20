package pg.project.bsk.client;

import javafx.fxml.FXMLLoader;
import pg.project.bsk.Controller.Controller;
import pg.project.bsk.Decryptor.AES;
import pg.project.bsk.Main;

import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
            sendMessage("Client connected!");
            do{
                String message = input.readUTF();
                if(!message.isEmpty()) getMessage(message);
            }while (true);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) throws IOException{
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
        Path path = Paths.get("D:\\Studia\\sem6\\bsk\\CryptoMessage\\src\\pg\\project\\bsk\\newfile.txt");
        try{
            Files.write(path,tmp);
        }catch (Exception e){
            //TODO:: this catch doesnt work yet. Have to be changed, but anyway Files.write() is working pretty good!
            controller.updateMainTextArea(new String(tmp));
        }
    }

}
