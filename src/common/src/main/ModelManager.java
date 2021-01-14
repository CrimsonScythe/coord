package common.src.main;
import org.jspace.*;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import static common.src.main.Constants.LISTEN_SPACE;
import static common.src.main.Constants.UPDATES_SPACE;

public class ModelManager {
    public static void main(String[] args) {

        SequentialSpace listenSpace = new SequentialSpace();
        SpaceRepository spaceRepository = new SpaceRepository();

        // for listening to all incoming connections
        spaceRepository.add(LISTEN_SPACE, listenSpace);
        spaceRepository.addGate("tcp://localhost:5000/?keep");

        while (true) {
            try {

                // make a new space for this specific user
                Object[] datas = listenSpace.get(new FormalField(String.class), new FormalField(String.class), new FormalField(Object.class));

                // create new thread to avoid blocking the main thread
                new Thread(new createPrivateSpace(spaceRepository, listenSpace, datas)).start();

            } catch (InterruptedException e) {

                e.printStackTrace();
            }
        }

    }
}

class createPrivateSpace implements Runnable{

    SpaceRepository spaceRepository;
    Object[] datas;
    SequentialSpace listenSpace;
    String uuid;
    File data;
    String[] modelPaths;

    public createPrivateSpace(SpaceRepository spaceRepository, SequentialSpace listenSpace, Object[] datas) {
        this.spaceRepository=spaceRepository;
        this.datas=datas;
        this.listenSpace=listenSpace;
        this.data= (File) datas[2];
        this.uuid = (String) datas[1];
        this.modelPaths = new String[] {
                "/home/kamal/Documents/forest.py",
                "/home/kamal/Documents/boost.py"
//                "/home/kamal/Documents/bag.py"
        };
    }

    @Override
    public void run() {

        System.out.println(this.data);

        try {

            // Create own private space for model executor to write to
            SequentialSpace updatesSpace = new SequentialSpace();
//            SpaceRepository spaceRepository = new SpaceRepository();
            /**
             * put global lock
             * put empty updates list
             */
            updatesSpace.put("lock");
            updatesSpace.put("updates", new String[]{"0", "0"});

            spaceRepository.add(UPDATES_SPACE+uuid, updatesSpace);
            spaceRepository.addGate("tcp://localhost:8000/?keep");

            // Connect to ModelExecuter
            RemoteSpace serverSpace = new RemoteSpace("tcp://localhost:8080/"+LISTEN_SPACE+"?keep");

            // model manager puts the script and data
            serverSpace.put(uuid+"-exec", uuid ,modelPaths, data.getPath());

            // waits for updates from model executor
            while (true){

//                Object[] responses = updatesSpace.get(new FormalField(String.class), new FormalField(String.class));

                Object[] updates = updatesSpace.getp(new ActualField("updates"), new FormalField(String[].class));

                if (updates!=null) {

                    System.out.println(((String[]) updates[1])[0] + " " + ((String[]) updates[1])[1]);
                }
//                System.out.println(responses[0] +" "+ responses[1]);

            }

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

    }
}
