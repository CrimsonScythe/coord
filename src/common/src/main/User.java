package common.src.main;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.SequentialSpace;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static common.src.main.Constants.LISTEN_SPACE;

public class User {
    public static void main(String[] args) {

//        SequentialSpace localSpace =  new SequentialSpace();
        UUID uuid = UUID.randomUUID();
        String dataFile = args[0];
//        File dataFile = new File(args[0]);

        try {
            //create local directory
            File theDir = new File("/home/kamal/Downloads/data/"+uuid+"/");
            if (!theDir.exists()){
                theDir.mkdirs();
            }

            // connect to global remotespace to create specific space
            RemoteSpace remoteSpace = new RemoteSpace("tcp://localhost:5000/"+LISTEN_SPACE+"?keep");
            // pass on unique id
            remoteSpace.put(uuid.toString());
            // wait for space to be created
            String response = (String) remoteSpace.get(new ActualField(uuid+"response"), new FormalField(String.class))[1];
            System.out.println("res");
            System.out.println(response);
            // space created
            if (response.equals("ok")){
                // connect to private space
                RemoteSpace privateSpace = new RemoteSpace("tcp://localhost:5000/"+uuid+"?keep");
                // send data to private space
                privateSpace.put("data", dataFile);

            } else {

            }


        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}
