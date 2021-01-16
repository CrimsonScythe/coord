package common.src.main;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static common.src.main.Constants.LISTEN_SPACE;
import static common.src.main.Constants.USER_UPDATES_SPACE;

public class User {
    public static void main(String[] args) {

        String column = args[0];

//        SequentialSpace localSpace =  new SequentialSpace();
        UUID uuid = UUID.randomUUID();
//        String dataFile = args[0];

        try {
            //create local directory
//            File theDir = new File("/home/kamal/Downloads/data/"+uuid+"/");
//            if (!theDir.exists()){
//                theDir.mkdirs();
//            }

            File newdataFile = new File("/home/kamal/Downloads/data/ks-projects.csv");

            // connect to global remotespace to create specific space
            RemoteSpace remoteSpace = new RemoteSpace("tcp://localhost:5000/"+LISTEN_SPACE+"?keep");

            // send data file path to model manager
            remoteSpace.put(uuid.toString()+"-server", uuid.toString(), newdataFile, "train", column);

            // TODO: FOR REPORT: Race condition if we connect to remotespace here without making the check
            //  as it might not have been created yet

            // if we get "ok" then private user space has been created and we can connect
            // TODO: we are not explciitl checking the return value here but it serves as a way to block until the
            //  other remotespace is created
            remoteSpace.get(new ActualField("server-"+uuid.toString()), new FormalField(String.class));

            // Now we connect to the space where the progress updates are
            RemoteSpace privateUserSpace = new RemoteSpace("tcp://localhost:8000/"+USER_UPDATES_SPACE+uuid+"?keep");

            // get prettified updates from manager
            while (true) {
                //TODO: make a sketch in report of the loop being broken by third party
                // break loop when other party is done and use of getp to prevent blocking
                Object[] loop = privateUserSpace.getp(new ActualField("loop"), new FormalField(String.class));
                if (loop!=null && loop[1].equals("break")) {
//                    Object[] response = privateUserSpace.get(new ActualField("updates"), new FormalField(String.class));
//                    System.out.println((String) response[1]);
                    break;
                }
                Object[] response = privateUserSpace.get(new ActualField("updates"), new FormalField(String.class));
                System.out.println((String) response[1]);
            }

            System.out.println("finished training - awaiting testing");

            // send data for testing
//            privateUserSpace.put("user"+uuid, uuid.toString(), newdataFile, "test", column);

            // wait for final response
            // remoteSpace.get(new ActualField("server-"+uuid.toString()), new FormalField(Object.class));


        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}

//TODO: user puts model arguments and can also specify the frequency of updates desired.