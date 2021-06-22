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
    private static final int PACKAGE_SIZE = 1024;
    private static Server instance;
    DataInputStream input;
    DataOutputStream output;
    Socket server;
    private Controller controller;
    private ServerSocket serverSocket;


    public Server(Controller _controller, int port) throws Exception {
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(1000000);

        System.out.println("Waiting for client on port " +
                serverSocket.getLocalPort());

        controller = _controller;
    }

    public static synchronized Server getInstance(Controller controller) throws Exception {
        if (instance == null)
            instance = new Server(controller, 1234);
        return instance;
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
        while (true) {
            try {
                server = serverSocket.accept();
                System.out.println("Just connected to " + server.getRemoteSocketAddress());

                input = new DataInputStream(server.getInputStream());
                output = new DataOutputStream(server.getOutputStream());

                do {
                    String message = input.readUTF();
                    if (!message.isEmpty()) {
                        if (messageContainsPublicKey(message)) getPublicKey(message);
                        else
                            getMessage(AES.decrypt(message.getBytes(StandardCharsets.UTF_8), controller.getCurrentDecryptionType()));
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

    public void sendMessage(String message, boolean isFile) throws IOException {
        assert message != null;

        int size =  message.length();

        if (!isFile)
            output.writeUTF(message);
        else {

            int allPackages = message.length() / PACKAGE_SIZE;

//            new Runnable(){
//                @Override
//                public void run(){
                    try{
                        SendingThread f = new SendingThread("First part", 7777, message.substring(0, (int) (size * 0.25)));
                        f.run();
                        SendingThread s = new SendingThread("Second part", 7778, message.substring((int) (size * 0.25), (int) (size * 0.5)));
                        s.run();
                        SendingThread t = new SendingThread("Third part", 7779, message.substring((int) (size * 0.5), (int) (size * 0.75)));
                        t.run();
                        SendingThread o = new SendingThread("Fourth part", 7780, message.substring((int) (size * 0.75), size));
                        o.run();
                    }
                    catch(Exception e)
                    {
                        System.out.println(e.getMessage());
                    }
//                }
//            };


        }
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
        if (message != null) messageAsString = new String(message);
        else messageAsString = "";
        assert message != null;
        controller.updateMainTextArea(messageAsString);
        //TODO:: logic to receiving files needs to be implemented here!
    }

    private String popPrefix(String tmpString) {
        if (messageContainsSessionKey(tmpString))
            return tmpString.substring(SESSION_KEY.length());
        else if (messageContainsPublicKey(tmpString))
            return tmpString.substring(PUBLIC_KEY.length());
        return tmpString;
    }

    private boolean messageContainsSessionKey(String s) {
        return s.contains(SESSION_KEY);
    }

    private void sendSessionKey(String publicKey, byte[] sessionKey) throws IOException {
        String tmp = RSA.encryptWithKey(sessionKey, publicKey);
        sendMessage(tmp, false);
    }

    private boolean messageContainsPublicKey(String tmp) {
        return tmp.contains(PUBLIC_KEY);
    }

    private class SendingThread extends Thread {

        DataOutputStream outputToClient;
        ServerSocket threadSocket;
        Socket thread;
        String partOfFileToSend;

        public SendingThread(String name, int port, String message) throws IOException {

            super(name);
            this.threadSocket = new ServerSocket(port);
            this.threadSocket.setSoTimeout(100000);
            this.partOfFileToSend = message;

        }

        @Override
        public void run() {

            // do przesylania
            int amountOfPackages = this.partOfFileToSend.length() / PACKAGE_SIZE;
            int totalSize = this.partOfFileToSend.length();
            int numberOfPackage = 0;

            try {
                thread = this.threadSocket.accept();
                System.out.println(this.getName() + " just started. ");

                this.outputToClient = new DataOutputStream(thread.getOutputStream());

                do {
                    if (this.partOfFileToSend.length() > PACKAGE_SIZE) {
                        outputToClient.writeUTF(this.partOfFileToSend.substring(0, PACKAGE_SIZE));
                        this.partOfFileToSend = this.partOfFileToSend.substring(0, PACKAGE_SIZE);
                    } else {
                        outputToClient.writeUTF(partOfFileToSend);
                        outputToClient.writeUTF("END");
                        break;
                    }
                    numberOfPackage++;

                } while (true);

            } catch (SocketTimeoutException s) {
                System.out.println("Socket timed out");

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}

