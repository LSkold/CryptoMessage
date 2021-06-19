package pg.project.bsk.client;

import javafx.fxml.FXMLLoader;
import pg.project.bsk.Controller.Controller;
import pg.project.bsk.Decryptor.AES;
import pg.project.bsk.Decryptor.RSA;
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

    private class FilePartSocket extends Thread
    {
        protected String ip;
        protected int port;
        protected String name;
        protected String storageName;
        private Object sync;


        Socket socket;
        InputStream inputStream;
        OutputStream outputStream;
        FileOutputStream outputFile;



        public FilePartSocket(String name, String ip, int port, String storageName, Object sync) throws IOException {
            super(name);
            this.ip = ip;
            this.port = port;
            this.storageName = storageName;
            this.sync = sync;

            socket = new Socket(ip, port);
            inputStream = client.getInputStream();
            outputStream = client.getOutputStream();
            outputFile = new FileOutputStream(storageName);

        }

        public void run() {

            byte[] buffer = new byte[1024];
            int len;

            try {
                while ((len = inputStream.read(buffer)) > 0) {

                    byte[] pack = getMessage(buffer, true);
                    outputFile.write(pack);
                }

                sync.notifyAll();
            }
            catch(Exception e)
            {
                System.out.println("Reading to buffer failure: " + e.getMessage());
            }

        }
    }


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
            byte[] buffer = new byte[1024];
            int len;

            do {
                while((len = input.read(buffer)) > 0)
                {
                    if(isString(buffer))
                    {
                        System.out.println(new String(buffer));
                        getMessage(buffer, false);
                    }
                    else { // FILE
                        //tworzy 4 watki
                        //trzeba pamietac o tym pierwszym bufferze złapanym, zeby go niepominac
                        //te 4 utworzone pliki pozniej trzeba bedzie zsumowac w dobrej kolejnosci
                        //ja skończę to dzielenie itd. <3
                    }

                }
            } while (true);
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    //zakladajac ze dziala dekrypcja
    private boolean isString(byte[] message) {

        String test = new String(AES.decrypt(message, controller.getCurrentDecryptionType()));
        if(test.contains("[CLIENT]") || test.contains("[SERVER]"))
            return true;
        return false;

    }

    private byte[] getMessage(byte[] message, boolean isFile) {
        byte[] tmp = AES.decrypt(message, controller.getCurrentDecryptionType());
        if(!isFile)
            controller.updateMainTextArea(tmp);

        return tmp;
    }

    public void sendMessage(byte[] message) throws IOException {


        byte[] tmp = AES.encrypt(message, controller.getCurrentDecryptionType());
        controller.updateMainTextArea(message);
        controller.updateMainTextArea(tmp);

        output.write(tmp); //writeUTF(tmp);
    }

}
