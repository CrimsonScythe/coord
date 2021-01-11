package common.src.main;

import org.jspace.*;

import java.io.*;
import java.util.List;

import static common.src.main.Constants.LISTEN_SPACE;

public class LoadDistributor {
    public static void main(String[] args) {

        SequentialSpace listenSpace = new SequentialSpace();
        SpaceRepository spaceRepository = new SpaceRepository();

        // for listening to all incoming connections
        spaceRepository.add(LISTEN_SPACE, listenSpace);
        spaceRepository.addGate("tcp://localhost:5000/?keep");

        while (true) {
            try {

                // make a new space for this specific user
                String uuid = (String) listenSpace.get(new FormalField(String.class))[0];

                // create new thread to avoid blocking the main thread
                new Thread(new CreatePrivateSpace(spaceRepository, listenSpace, uuid)).start();


            } catch (InterruptedException e) {

                e.printStackTrace();
            }
        }

    }
}

class CreatePrivateSpace implements Runnable{

    SpaceRepository spaceRepository;
    String uuid;
    SequentialSpace listenSpace;

    public CreatePrivateSpace(SpaceRepository spaceRepository, SequentialSpace listenSpace, String uuid) {
        this.spaceRepository=spaceRepository;
        this.uuid=uuid;
        this.listenSpace=listenSpace;
    }

    @Override
    public void run() {
        try {
            SequentialSpace dataSpace = new SequentialSpace();
            spaceRepository.add(uuid, dataSpace);
            spaceRepository.addGate("tcp://localhost:5000/?keep");

            // communicate back to user that space has been created
            listenSpace.put(uuid+"response", "ok");

            while (true) {
                // get data from private user
                Object[] datas = dataSpace.get(new ActualField("data"), new FormalField(Object.class));
                // once data has been received split it up and send it to serverJava
                List<ByteArrayOutputStream> outputStreamList = split.split((String) datas[1], uuid);
                // send data to serverJava
                if (outputStreamList!=null) {
                    RemoteSpace serverSpace = new RemoteSpace("tcp://localhost:8080/"+LISTEN_SPACE+"?keep");
                    serverSpace.put(uuid);
                    // server created
                    String response = (String) serverSpace.get(new ActualField("response"), new FormalField(String.class))[1];
                    if (response.equals("ok")) {

                    }
//                    for (int i = 0; i < outputStreamList.size(); i++) {
//
//                        OutputStream outputStream = new FileOutputStream("/home/kamal/Downloads/data/yolo"+i+".csv");
//                        outputStreamList.get(i).writeTo(outputStream);
//                    }
                } else {
                    System.out.println("NULL");
                }

            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
