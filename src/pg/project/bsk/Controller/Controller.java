package pg.project.bsk.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import pg.project.bsk.Decryptor.AES;
import pg.project.bsk.Decryptor.RSA;
import pg.project.bsk.appinfo.AppInfo;
import pg.project.bsk.client.Client;
import pg.project.bsk.server.Server;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.PublicKey;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Controller implements Initializable {

    private enum  KeyType{
        Private,
        Public,
    }

    public static final String appDataDirectory = System.getenv("APPDATA");
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
    Button generateKeysButton;
    @FXML
    Label statusLabel;

    @FXML
    public void submitMessage(ActionEvent event) {
        try {
            if(AppInfo.getInstance(this).getVersion() == AppInfo.AppVersion.Server){
                String message = "[SERVER] " + mainTextField.getText();
                updateMainTextArea(message);
                Server.getInstance(this).sendMessage(message);
            }
            else{
                String message = "[CLIENT] " + mainTextField.getText();
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

    public void updateMainTextArea(String text){
        mainTextArea.insertText(0,text+"\n");
        System.out.println("OUTPUT:\n"+mainTextArea.getText());
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

    public void generateKeys(ActionEvent actionEvent) {
        try {
            RSA.generateRSAKeys();
            generateKeysButton.setDisable(areKeysGenerated());
        } catch (Exception e) {
            statusLabel.setText("");
            e.printStackTrace();
        }
    }

    private static String getKeysDirectory(KeyType keyType){
        String path = appDataDirectory + "/CryptoMessage/";
        if(keyType == KeyType.Public) path+="/Public/";
        else path += "/Private/";
        return path;
    }

    public static String getPrivateKeyDirectory(){
        return getKeysDirectory(KeyType.Private);
    }

    public static String getPublicKeyDirectory(){
        return getKeysDirectory(KeyType.Public);
    }

    private boolean areKeysGenerated(){
        File privateKey = new File(getPrivateKeyDirectory()+"rsaPrivateKey");
        File publicKey = new File(getPublicKeyDirectory()+"rsaPublicKey");

        if(privateKey.exists() && publicKey.exists()) {
            statusLabel.setText("Keys are generated!");
            return true;
        }
        return false;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        generateKeysButton.setDisable(areKeysGenerated());
    }
}
