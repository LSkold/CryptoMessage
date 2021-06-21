package pg.project.bsk.client;

import javafx.fxml.FXMLLoader;
import pg.project.bsk.Controller.Controller;
import pg.project.bsk.Decryptor.AES;
import pg.project.bsk.Decryptor.RSA;
import pg.project.bsk.Main;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Client extends Thread {

    private static final String SESSION_KEY = "[SESSION_KEY]";

    private static boolean waitingForSessionKey = false;
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
        Thread t = getInstance(controller);
        t.start();
    }

    public static void setWaitingForSessionKey(boolean value) {
        waitingForSessionKey = value;
    }

    public void run() {
        try{
            do{
                String message = input.readUTF();
                if(!message.isEmpty()) {
                    if(waitingForSessionKey) getSessionKey(message);
                    else getMessage(AES.decrypt(message.getBytes(StandardCharsets.UTF_8), controller.getCurrentDecryptionType()));
                }
            }while (true);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) throws IOException{
        System.out.println(message);
        output.writeUTF(message);
    }

    public void getSessionKey(String message) {
        setWaitingForSessionKey(false);
        AES.setKey(RSA.decryptWithPrivateKey(message));
        controller.setVisibility();
    }

    public void getMessage(byte[] message) {
        String messageAsString = new String(message);
        try{
            Path path = Paths.get("D:\\Studia\\sem6\\bsk\\CryptoMessage\\src\\pg\\project\\bsk\\newfile.txt");
            throw new Exception("not implemented yet");
            //Files.write(path,message);
        }catch (Exception e){
            //TODO:: this catch doesnt work yet. Have to be changed, but anyway Files.write() is working pretty good!
            controller.updateMainTextArea(new String(message));
        }
    }
    private boolean messageContainsSessionKey(String s) {
        return s.contains(SESSION_KEY);
    }

    private String popPrefix(String tmpString) {
        if(messageContainsSessionKey(tmpString))
            return tmpString.substring(SESSION_KEY.length());
        return tmpString;
    }

}
