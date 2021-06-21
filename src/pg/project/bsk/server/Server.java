package pg.project.bsk.server;

import pg.project.bsk.Controller.Controller;
import pg.project.bsk.Decryptor.AES;
import pg.project.bsk.Decryptor.RSA;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Server extends Thread {

    private static final String SESSION_KEY = "[SESSION_KEY]";
    private static final String PUBLIC_KEY = "[PUBLIC_KEY]";

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

                do {
                    String message = input.readUTF();
                    if(!message.isEmpty()){
                        if(messageContainsPublicKey(message)) getPublicKey(message);
                        else getMessage(AES.decrypt(message.getBytes(StandardCharsets.UTF_8), controller.getCurrentDecryptionType()));
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

    public void sendMessage(String message) throws IOException {
        assert message != null;
        output.writeUTF(message);
    }

    public void getPublicKey(String message) throws IOException {
        byte[] sessionKey = AES.generateSessionKey();
        message = popPrefix(message);
        sendSessionKey(message, sessionKey);
        AES.setKey(Base64.getEncoder().encodeToString(sessionKey));
        controller.setVisibility();
    }
    public void getMessage(byte[] message) {
        String messageAsString;
        if(message != null) messageAsString = new String(message);
        else messageAsString = "";
        assert message != null;
        controller.updateMainTextArea(messageAsString);
        //TODO:: logic to receiving files needs to be implemented here!
    }

    private String popPrefix(String tmpString) {
        if(messageContainsSessionKey(tmpString))
            return tmpString.substring(SESSION_KEY.length());
        else if(messageContainsPublicKey(tmpString))
            return tmpString.substring(PUBLIC_KEY.length());
        return tmpString;
    }

    private boolean messageContainsSessionKey(String s) {
        return s.contains(SESSION_KEY);
    }

    private void sendSessionKey(String publicKey, byte[] sessionKey) throws IOException {
        String tmp = RSA.encryptWithKey(sessionKey,publicKey);
        sendMessage(tmp);
    }

    private boolean messageContainsPublicKey(String tmp) {
        return tmp.contains(PUBLIC_KEY);
    }
}

