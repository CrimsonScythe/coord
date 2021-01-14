package common.src.main;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static common.src.main.Constants.LISTEN_SPACE;

public class User {
    public static void main(String[] args) {

//        SequentialSpace localSpace =  new SequentialSpace();
        UUID uuid = UUID.randomUUID();
//        String dataFile = args[0];

        try {
            //create local directory
            File theDir = new File("/home/kamal/Downloads/data/"+uuid+"/");
            if (!theDir.exists()){
                theDir.mkdirs();
            }

            File newdataFile = new File("/home/kamal/Downloads/data/ks-projects.csv");

            // connect to global remotespace to create specific space
            RemoteSpace remoteSpace = new RemoteSpace("tcp://localhost:5000/"+LISTEN_SPACE+"?keep");

            // send data file path to model manager
            remoteSpace.put(uuid.toString()+"-server", uuid.toString(), newdataFile);

            // get updates from manager


            // wait for final response
            remoteSpace.get(new ActualField("server-"+uuid.toString()), new FormalField(Object.class));


        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}

//TODO: change http to put and send the python script in a tuple space
//TODO: the python script will be executed on the serverJava itself
//TODO: maybe something that tells how many computers are available for crunching the data, if for example
//TODO: there are 3 available and 4 files then what?
//TODO: store training model parameters in a separate server and have user choose which one he wants
//TODO: for testing. Sometimes for example users want complex models for testing with lower loss.
