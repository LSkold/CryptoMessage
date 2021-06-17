package pg.project.bsk;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pg.project.bsk.Controller.Controller;
import pg.project.bsk.appinfo.AppInfo;
import pg.project.bsk.server.Server;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        String title = "Crypto Message - ";

        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("Controller/main.fxml").openStream());
        if(AppInfo.getInstance(fxmlLoader.getController()).getVersion() == AppInfo.AppVersion.Server){
            Server.startServer(fxmlLoader.getController());
            title += "Server";
        }
        else{
            title += "Client";
        }
        primaryStage.setTitle(title);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();


    }
    public static void main(String[] args) {
        launch(args);
    }
}
