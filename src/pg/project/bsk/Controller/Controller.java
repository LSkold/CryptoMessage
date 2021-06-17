package pg.project.bsk.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javafx.scene.control.TextArea;
import pg.project.bsk.appinfo.AppInfo;
import pg.project.bsk.client.Client;
import pg.project.bsk.server.Server;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Controller {
    @FXML
    Button submitTextMessage;
    @FXML
    TextArea mainTextArea;
    @FXML
    TextField mainTextField;


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
}
