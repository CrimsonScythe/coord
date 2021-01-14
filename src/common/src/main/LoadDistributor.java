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
                Object[] datas = listenSpace.get(new FormalField(String.class), new FormalField(String.class), new FormalField(Object.class));
//                System.out.println(datas[2]);


                // create new thread to avoid blocking the main thread
                new Thread(new CreatePrivateSpace(spaceRepository, listenSpace, datas)).start();


            } catch (InterruptedException e) {

                e.printStackTrace();
            }
        }

    }
}

class CreatePrivateSpace implements Runnable{

    SpaceRepository spaceRepository;
    Object[] datas;
    SequentialSpace listenSpace;
    String uuid;
    File data;

    public CreatePrivateSpace(SpaceRepository spaceRepository, SequentialSpace listenSpace, Object[] datas) {
        this.spaceRepository=spaceRepository;
        this.datas=datas;
        this.listenSpace=listenSpace;
        this.data= (File) datas[2];
        this.uuid = (String) datas[1];
    }

    @Override
    public void run() {
        try {

            System.out.println(data.getPath());
            List<String> outputFilePaths = split.split(data.getPath(), uuid);
//                List<ByteArrayOutputStream> outputStreamList = split.split((String) data, uuid);
                // send data to serverJava
                if (outputFilePaths!=null) {
                    RemoteSpace serverSpace = new RemoteSpace("tcp://localhost:8080/"+LISTEN_SPACE+"?keep");
                    serverSpace.put(uuid+"-server",uuid, outputFilePaths);
//                    for (ByteArrayOutputStream byteArrayOutputStream : outputStreamList) {
//                        serverSpace.put(uuid, byteArrayOutputStream);
//                    }
//                    // send value to indicate end of transmission
//                    serverSpace.put(uuid, "end");
                    serverSpace.get(new ActualField("server-"+uuid), new FormalField(Object.class));

                } else {
                    System.out.println("NULL");
                }

//            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
