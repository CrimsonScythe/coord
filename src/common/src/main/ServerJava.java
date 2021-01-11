package common.src.main;

import org.jspace.*;

import static common.src.main.Constants.LISTEN_SPACE;

public class ServerJava {
    public static void main(String[] args){

        SequentialSpace listenSpace = new SequentialSpace();
        SpaceRepository spaceRepository = new SpaceRepository();

        spaceRepository.add(LISTEN_SPACE, listenSpace);
        spaceRepository.addGate("tcp://localhost:8080/?keep");

        while (true) {
            try {

                String uuid = (String) listenSpace.get(new FormalField(String.class))[0];
                new Thread(new CreatePrivateServer(uuid, listenSpace));

            } catch (InterruptedException e) {

            }
        }
    }
}

class CreatePrivateServer implements Runnable {

    private String uuid;
    private SequentialSpace listenSpace;

    public CreatePrivateServer(String uuid, SequentialSpace listenSpace){
        this.uuid = uuid;
        this.listenSpace = listenSpace;
    }

    @Override
    public void run() {
        try {
            listenSpace.put("response", "ok");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
