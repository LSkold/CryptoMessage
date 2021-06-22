package pg.project.bsk.Controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import javafx.stage.Stage;
import pg.project.bsk.Decryptor.AES;
import pg.project.bsk.Decryptor.RSA;
import pg.project.bsk.appinfo.AppInfo;
import pg.project.bsk.client.Client;
import pg.project.bsk.server.Server;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Controller implements Initializable {


    public static final String appDataDirectory = System.getenv("APPDATA");
    @FXML
    Button submit;
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
    Button chooseFile;
    @FXML
    TextField filePath;
    @FXML
    Button submitFile;

    private AES.AesType currentDecryptionType = AES.AesType.AES_ECB;

    public void setTransferPercent(Integer transferPercent) {
        this.transferPercent = transferPercent;
    }

    Integer transferPercent;
    private String fileFullPath;

    private static String getKeysDirectory(KeyType keyType) {
        String path = appDataDirectory + "/CryptoMessage/";
        if (keyType == KeyType.Public) path += "/Public/";
        else path += "/Private/";
        return path;
    }

    public static String getPrivateKeyDirectory() {
        return getKeysDirectory(KeyType.Private);
    }

    public static String getPublicKeyDirectory() {
        return getKeysDirectory(KeyType.Public);
    }

    public void chooseFileClicked(ActionEvent actionEvent) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload File Path");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("ALL FILES", "*.*"),
                new FileChooser.ExtensionFilter("ZIP", "*.zip"),
                new FileChooser.ExtensionFilter("PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("TEXT", "*.txt"),
                new FileChooser.ExtensionFilter("IMAGE FILES", "*.jpg", "*.png", "*.gif")
        );

        File file = fileChooser.showOpenDialog(chooseFile.getScene().getWindow());

        if (file != null) {
            fileFullPath = file.getAbsolutePath();
            filePath.setText(file.getName());
            submitFile.setDisable(false);

        } else {
            System.out.println("error");
        }
    }


    public void startSendingFile(ActionEvent actionEvent) throws Exception {
        if (AppInfo.getInstance(this).getVersion() == AppInfo.AppVersion.Server) {
            if (!filePath.getText().isEmpty()) {
                File sendingFile = new File(fileFullPath);
                String stringMessageToClient = "[FILE]" + filePath.getText();
                String stringMessage = "[Server] sent file " + filePath.getText();
                updateMainTextArea(stringMessage);

                byte[] message = Files.readAllBytes(sendingFile.toPath());
                Server.getInstance(this).sendMessage(
                        sendingFile.toPath().toString(), true); // musi isc zwykly, paczki beda szyfrowane, odbior bez odszyfrowania i na koncu decrypt
                Server.getInstance(this).sendMessage(
                        AES.encrypt(stringMessageToClient.getBytes(StandardCharsets.UTF_8), getCurrentDecryptionType()), false);
                filePath.setText("");
            }

        }
        else
            System.out.println("tylko dla serwera narazie byq");
    }

    @FXML
    public void submitMessage(ActionEvent event) {

        try {
            if (AppInfo.getInstance(this).getVersion() == AppInfo.AppVersion.Server) {

                if (!mainTextField.getText().isEmpty()) {
                    String message = "[SERVER] " + mainTextField.getText();
                    updateMainTextArea(message);
                    Server.getInstance(this).sendMessage(
                            AES.encrypt(message.getBytes(StandardCharsets.UTF_8), getCurrentDecryptionType()), false);
                }
            }
            else {
                String message = "[CLIENT] " + mainTextField.getText();
                updateMainTextArea(message);
                Client.getInstance(this).sendMessage(
                        AES.encrypt(message.getBytes(StandardCharsets.UTF_8), getCurrentDecryptionType())
                );
            }
            mainTextField.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("progress.fxml"));
            /*
             * if "fx:controller" is not set in fxml
             * fxmlLoader.setController(NewWindowController);
             */
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.setTitle("Sending");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            Logger logger = Logger.getLogger(getClass().getName());
            logger.log(Level.SEVERE, "Failed to create new Window.", e);
        }
    }

    public void updateMainTextArea(String text) {
        mainTextArea.insertText(0, text + "\n");
        System.out.println("OUTPUT:\n" + mainTextArea.getText());
    }

    @FXML
    public void onEnter(ActionEvent event) {
        submitMessage(event);
    }

    @FXML
    public void encryptionChanged(ActionEvent actionEvent) {
        switch (chooseCryptType.getValue()) {
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

    public AES.AesType getCurrentDecryptionType() {
        return currentDecryptionType;
    }

    public void generateKeys(ActionEvent actionEvent) {
        try {
            RSA.generateRSAKeys();
            generateKeysButton.setDisable(areKeysGenerated());
            if (AES.isKeyEmpty()) {
                Client.getInstance(this).sendMessage(
                        "[PUBLIC_KEY]" + Base64.getEncoder().encodeToString(RSA.getPublicKey().getEncoded()));
                Client.setWaitingForSessionKey(true);
            }


        } catch (Exception e) {
            statusLabel.setText("");
            AES.setKey("");
            e.printStackTrace();
        }
    }

    private boolean areKeysGenerated() {
        File privateKey = new File(getPrivateKeyDirectory() + "rsaPrivateKey");
        File publicKey = new File(getPublicKeyDirectory() + "rsaPublicKey");

        if (privateKey.exists() && publicKey.exists()) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    statusLabel.setText("Keys are generated!");
                }
            });
            return true;
        }
        return false;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setVisibility();
    }

    public void setVisibility() {
        generateKeysButton.setDisable(areKeysGenerated());
        submit.setDisable(AES.isKeyEmpty());
        chooseFile.setDisable(AES.isKeyEmpty());
    }

    private enum KeyType {
        Private,
        Public,
    }

}
