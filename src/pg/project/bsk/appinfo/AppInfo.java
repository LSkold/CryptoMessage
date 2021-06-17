package pg.project.bsk.appinfo;

import pg.project.bsk.Controller.Controller;
import pg.project.bsk.client.Client;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;

public class AppInfo {
    public enum AppVersion {
        Server("Server"),
        Client("Client");

        private String type;

        AppVersion(String _type){
            this.type = _type;
        }

        public String getType() {return type;}
    }

    public AppInfo(Controller controller){
        this.version = checkAppVersion(controller);
    }

    private static AppInfo instance;

    public static synchronized AppInfo getInstance(Controller controller) {
        if(instance == null)
            instance = new AppInfo(controller);
        return instance;
    }

    private AppVersion version;

    public AppVersion getVersion(){
        return version;
    }

    static AppVersion checkAppVersion(Controller controller){
        try {
            Client.startClient(controller);
        } catch (Exception e) {
            return AppVersion.Server;
        }
        return AppVersion.Client;
    }

}
