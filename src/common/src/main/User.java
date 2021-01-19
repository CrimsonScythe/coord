package common.src.main;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
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
            File theDir = new File("/home/kamal/Downloads/data/"+uuid+"/");
            if (!theDir.exists()){
                theDir.mkdirs();
            }

            File newdataFile = new File("/home/kamal/Downloads/data/ks-projects.csv");
            File testdataFile = new File("/home/kamal/Downloads/data/ks-projects.csv");

            // connect to global remotespace to create specific space
            RemoteSpace remoteSpace = new RemoteSpace("tcp://localhost:5000/"+LISTEN_SPACE+"?keep");

            // send data file path to model manager
            remoteSpace.put(uuid.toString()+"-server", uuid.toString(), newdataFile, testdataFile, "train", column);

            // TODO: FOR REPORT: Race condition if we connect to remotespace here without making the check
            //  as it might not have been created yet

            // if we get "ok" then private user space has been created and we can connect
            // TODO: we are not explciitl checking the return value here but it serves as a way to block until the
            //  other remotespace is created
            remoteSpace.get(new ActualField("server-"+uuid.toString()), new FormalField(String.class));

            // Now we connect to the space where the progress updates are
            RemoteSpace privateUserSpace = new RemoteSpace("tcp://localhost:8000/"+USER_UPDATES_SPACE+uuid+"?keep");

            // get prettified updates from manager
            // TODO: race condition here when using getp()
            List<String> lst = new ArrayList<String>();
            while (true) {
                //TODO: make a sketch in report of the loop being broken by third party
                // break loop when other party is done and use of getp to prevent blocking
                Object[] response = privateUserSpace.get(new ActualField("updates"), new FormalField(String.class), new FormalField(String.class), new FormalField(String.class));

                if (((String) response[1]).contains("done")) {
                    lst.add((String) response[1]);
                } else {
                    System.out.println((String) response[1]);

                }

                if (response[3].equals("break")) {
                    break;
                }

//                Object[] loop = privateUserSpace.getp(new ActualField("loop"), new FormalField(String.class));
//                if (loop!=null && loop[1].equals("break")) {
//                    break;
//                }
//                Object[] response = privateUserSpace.get(new ActualField("updates"), new FormalField(String.class));
//                System.out.println((String) response[1]);
            }

            if (lst.size()==1){
                System.out.println(lst.get(0));
            } else {
                System.out.println(lst.get(0)+" "+lst.get(1));
            }

            System.out.println("finished training - awaiting testing");
            System.out.println("Please choose desired model: 0 or 1 to download predictions");

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String selection = br.readLine();

            System.out.println("sending download request");
            String dataSavePath = "/home/kamal/Downloads/data/"+uuid+"/";
            privateUserSpace.put("selection", Integer.parseInt(selection), dataSavePath);

            Object[] res = privateUserSpace.get(new ActualField("downloaded"), new FormalField(String.class));
            System.out.println("download complete "+"file location is: "+res[1]);

//            System.out.println("Sending testing request");
//
//
//            String pathToTestData = "/home/kamal/Downloads/data/ks-projects.csv";
//            privateUserSpace.put("path", pathToTestData);
//
//            // await testing
//            privateUserSpace.get();

//                        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//            String pathToTestData = br.readLine();

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