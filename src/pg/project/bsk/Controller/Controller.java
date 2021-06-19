package pg.project.bsk.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javafx.scene.control.TextArea;
import pg.project.bsk.Decryptor.AES;
import pg.project.bsk.appinfo.AppInfo;
import pg.project.bsk.client.Client;
import pg.project.bsk.server.Server;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Controller {

    private AES.AesType currentDecryptionType = AES.AesType.AES_ECB;

    @FXML
    Button submitTextMessage;
    @FXML
    TextArea mainTextArea;
    @FXML
    TextField mainTextField;
    @FXML
    ComboBox<String> chooseCryptType;


    @FXML
    public void submitMessage(ActionEvent event) {
        try {
            if(AppInfo.getInstance(this).getVersion() == AppInfo.AppVersion.Server){

                String tmp = "[SERVER] " + mainTextField.getText();
                byte[] tmpBytes = tmp.getBytes();
                byte[] message = new byte[1024];

                for (int i=0; i < tmpBytes.length; i++) {
                    message[i] = tmpBytes[i];
                }

                //byte[] en = AES.encrypt(message, this.getCurrentDecryptionType());
                //byte[] de = AES.decrypt(en, this.getCurrentDecryptionType());
                //System.out.println(new String(de));
                Server.getInstance(this).sendMessage(message);
                updateMainTextArea(message);

            }
            else{
                byte[] message = ("[CLIENT] " + mainTextField.getText()).getBytes(StandardCharsets.UTF_8);
                updateMainTextArea(message);
                Client.getInstance(this).sendMessage(message);
            }
            mainTextField.clear();
        }catch (Exception e){
            e.printStackTrace();
        }

//        try {
//            FXMLLoader fxmlLoader = new FXMLLoader();
//            fxmlLoader.setLocation(getClass().getResource("progress.fxml"));
//            /*
//             * if "fx:controller" is not set in fxml
//             * fxmlLoader.setController(NewWindowController);
//             */
//            Scene scene = new Scene(fxmlLoader.load());
//            Stage stage = new Stage();
//            stage.setTitle("Sending");
//            stage.setScene(scene);
//            stage.show();
//        } catch (IOException e) {
//            Logger logger = Logger.getLogger(getClass().getName());
//            logger.log(Level.SEVERE, "Failed to create new Window.", e);
//        }
    }

    public void updateMainTextArea(byte[] byteText){
        mainTextArea.insertText(0, new String(byteText)+"\n");
    }

    @FXML
    public void onEnter(ActionEvent event){
        submitMessage(event) ;
    }

    @FXML
    public void encryptionChanged(ActionEvent actionEvent) {
        switch(chooseCryptType.getValue()){
            case "ECB":
                currentDecryptionType = AES.AesType.AES_ECB;
                break;
            case "CBC":
                currentDecryptionType = AES.AesType.AES_CBC;
                break;
            case "OFB":
                currentDecryptionType = AES.AesType.AES_OFB2;
                break;
        }
    }

    public AES.AesType getCurrentDecryptionType(){
        return currentDecryptionType;
    }
}
