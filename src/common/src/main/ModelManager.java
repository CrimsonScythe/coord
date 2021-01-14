package common.src.main;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.SequentialSpace;
import org.jspace.SpaceRepository;
import java.io.File;
import java.io.IOException;

import static common.src.main.Constants.LISTEN_SPACE;

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

    public createPrivateSpace(SpaceRepository spaceRepository, SequentialSpace listenSpace, Object[] datas) {
        this.spaceRepository=spaceRepository;
        this.datas=datas;
        this.listenSpace=listenSpace;
        this.data= (File) datas[2];
        this.uuid = (String) datas[1];
    }

    @Override
    public void run() {

        System.out.println(this.data);

        try {

            // Connect to ModelExecuter
            RemoteSpace serverSpace = new RemoteSpace("tcp://localhost:8080/"+LISTEN_SPACE+"?keep");

            // model manager puts the script and data
            serverSpace.put(uuid+"-exec", uuid ,"/home/kamal/Documents/forest.py", data.getPath());


        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

    }
}
