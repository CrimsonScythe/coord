package common.src.main;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.SequentialSpace;

import javax.swing.plaf.FileChooserUI;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.UUID;

import static common.src.main.Constants.LISTEN_SPACE;

public class User {
    public static void main(String[] args) {

//        SequentialSpace localSpace =  new SequentialSpace();
        UUID uuid = UUID.randomUUID();
        String dataFile = args[0];


        try {
            //create local directory
            File theDir = new File("/home/kamal/Downloads/data/"+uuid+"/");
            if (!theDir.exists()){
                theDir.mkdirs();
            }

            File newdataFile = new File("/home/kamal/Downloads/data/ks-projects.csv");

            // connect to global remotespace to create specific space
            RemoteSpace remoteSpace = new RemoteSpace("tcp://localhost:5000/"+LISTEN_SPACE+"?keep");
            // pass on unique id and data

//            byte[] fileContent = Files.readAllBytes(new File(dataFile).toPath());
//            remoteSpace.put(uuid.toString()+"-server", uuid.toString(), fileContent);

            remoteSpace.put(uuid.toString()+"-server", uuid.toString(), newdataFile);

            // wait for final response
            remoteSpace.get(new ActualField("server-"+uuid.toString()), new FormalField(Object.class));


        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}
