package pg.project.bsk.client;

import pg.project.bsk.Controller.Controller;
import pg.project.bsk.Decryptor.AES;
import pg.project.bsk.Decryptor.RSA;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;


public class Client extends Thread {

    public static final String FILENAME_PREFIX = "[FILE]";
    private static final String SESSION_KEY = "[SESSION_KEY]";
    private static boolean waitingForSessionKey = false;
    private static Client instance;
    Socket client;

    InputStream inFromServer;
    DataInputStream input;

    OutputStream outToServer;
    DataOutputStream output;
    String fileName; // to trzeba by odebrac z poczatku pliku, dokladnie part 1


    private Controller controller;
    private Object lock;
    private int finishedThreads;


    public Client(Controller _controller, String ip, int port) throws Exception {
        client = null;
        client = new Socket(ip, port);

        inFromServer = client.getInputStream();
        input = new DataInputStream(inFromServer);
        outToServer = client.getOutputStream();
        output = new DataOutputStream(outToServer);

        controller = _controller;
    }

    // Zrobimy tak, że porty jednoznacznie beda okreslac jaka czesc pliku dostaje klient, w sensie takim, że jesli bedzie port np. 2888 to dostanie zawsze 0.0 - 0.25 pliku, wtedy latwiej zlozyc

    public static synchronized Client getInstance(Controller controller) throws Exception {
        if (instance == null)
            instance = new Client(controller, "127.0.0.1", 1234);
        return instance;
    }

    public static void startClient(Controller controller) throws Exception {
        Thread t = getInstance(controller);
        t.start();
    }

    public static void setWaitingForSessionKey(boolean value) {
        waitingForSessionKey = value;
    }

    public void run() {
        try {
            int fileStart;
            boolean fileIsReady = false;
            boolean isFile = false;
            do {
                String message = input.readUTF();
                if (!message.isEmpty()) {
                    if (waitingForSessionKey)
                        getSessionKey(message);
                    else {

                        //file start przechowuje poczatek pliku, bo tam przyjdzie tez nazwa
                        if (!getFileNameIsAvailable(message).equals("")) {
                            fileIsReady = true;
                        }
                        if (fileIsReady) {

                            // PSEUDO CZUWACZ NA ZJEDNOCZENIE PLIKÓW, NIE SPRAWDZALEM CZY DZIALA
                            new Runnable() {
                                @Override
                                public void run() {
                                    while (true) {

                                        synchronized (lock) {
                                            try {
                                                Thread.sleep(1000);
                                                if (finishedThreads == 4) {

                                                    PrintWriter pw = new PrintWriter(fileName);
                                                    BufferedReader br1 = new BufferedReader(new FileReader("1_part"));
                                                    BufferedReader br2 = new BufferedReader(new FileReader("2_part"));
                                                    BufferedReader br3 = new BufferedReader(new FileReader("3_part"));
                                                    BufferedReader br4 = new BufferedReader(new FileReader("4_part"));

                                                    String line1 = br1.readLine();
                                                    String line2 = br2.readLine();
                                                    String line3 = br3.readLine();
                                                    String line4 = br4.readLine();
                                                    do {

                                                        if (line1 != null) {
                                                            pw.println(line1);
                                                            line1 = br1.readLine();
                                                        } else if (line2 != null) {
                                                            pw.println(line2);
                                                            line2 = br2.readLine();
                                                        } else if (line3 != null) {
                                                            pw.println(line3);
                                                            line3 = br3.readLine();

                                                        } else if (line4 != null) {
                                                            pw.println(line4);
                                                            line4 = br4.readLine();
                                                        } else
                                                            break;
                                                    }
                                                    while (true);

                                                }
                                            } catch (InterruptedException | FileNotFoundException e) {
                                                e.printStackTrace();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }

                                }
                            };

                            try {
                                System.out.println("Starting threads. ");
                                ReceivingThread f = new ReceivingThread("First part", 7777, "127.0.0.1", 1);
                                f.start();
                                ReceivingThread s = new ReceivingThread("Second part", 7778, "127.0.0.1", 2);
                                s.start();
                                ReceivingThread t = new ReceivingThread("Third part", 7779, "127.0.0.1", 3);
                                t.start();
                                ReceivingThread o = new ReceivingThread("Fourth part", 7780, "127.0.0.1", 4);
                                o.start();
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }
//                                }
//                            };

                        } else
                            getMessage(AES.decrypt(message.getBytes(StandardCharsets.UTF_8), controller.getCurrentDecryptionType()));
                    }
                }
            } while (true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getFileNameIsAvailable(String message) {

        String tmp = new String(AES.decrypt(message.getBytes(StandardCharsets.UTF_8), controller.getCurrentDecryptionType()));
        if (tmp.contains(FILENAME_PREFIX))
            return tmp.substring(FILENAME_PREFIX.length());
        else
            return "";
    }

    public void sendMessage(String message) throws IOException {
        System.out.println(message);
        output.writeUTF(message);
    }

    public void getSessionKey(String message) {
        setWaitingForSessionKey(false);
        AES.setKey(RSA.decryptWithPrivateKey(message));
        controller.setVisibility();
    }

    public void getMessage(byte[] message) {

        String messageString = new String(message);
//        if (isFile) {
//            try {
//                Path path = Paths.get(fileName);
//                Files.write(path, Collections.singleton(messageString));
//                controller.updateMainTextArea("File named " + fileName + " successfully downloaded.");
//
//            } catch (Exception e) {
//               //TODO:: this catch doesnt work yet. Have to be changed, but anyway Files.write() is working pretty good!
//            }
//        }
//        else {
        controller.updateMainTextArea(messageString);
        //}
    }

    private boolean messageContainsSessionKey(String s) {
        return s.contains(SESSION_KEY);
    }

    private String popPrefix(String tmpString) {
        if (messageContainsSessionKey(tmpString))
            return tmpString.substring(SESSION_KEY.length());
        return tmpString;
    }

    private class ReceivingThread extends Thread {

        InputStream inFromThread;
        DataInputStream inputThread;
        FileOutputStream outputFilePart;
        int numberOfPack;
        String fileName;
        Socket socketThread;

        public ReceivingThread(String name, int port, String ip, int numberOfPack) throws IOException {

            super(name);
            this.numberOfPack = numberOfPack;
            this.socketThread = new Socket(ip, port);
            this.fileName = numberOfPack + "_part";

        }

        @Override
        public void run() {
            System.out.println(this.getName() + " just started. ");

            try {
                this.inFromThread = this.socketThread.getInputStream();
                this.inputThread = new DataInputStream(this.inFromThread);
                this.outputFilePart = new FileOutputStream(numberOfPack + "_part");
                System.out.println(this.getName() + " initialized correctly. ");
            } catch (IOException e) {
                e.printStackTrace();
            }

            Path path = Paths.get(this.fileName);
            String message;

            try {

                do {
                    message = inputThread.readUTF();
                    Files.write(path, AES.decrypt(message.getBytes(StandardCharsets.UTF_8), controller.getCurrentDecryptionType()));

                } while (!message.equals("END_OF_UPLOADING."));

                synchronized (lock) {
                    finishedThreads++;
                }

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

}
