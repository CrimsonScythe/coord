package common.src.main;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.Space;
import org.jspace.SpaceRepository;

import java.io.*;

import static common.src.main.Constants.LISTEN_SPACE;

public class ModelExecuter {
    public static void main(String[] args) {

        SequentialSpace listenSpace = new SequentialSpace();
        SpaceRepository spaceRepository = new SpaceRepository();

        spaceRepository.add(LISTEN_SPACE, listenSpace);
        spaceRepository.addGate("tcp://localhost:8080/?keep");

        while (true) {
            try {

                Object[] datas = listenSpace.get(new FormalField(String.class), new FormalField(Object.class), new FormalField(String.class), new FormalField(Object.class));
                new Thread(new createPrivateServer(datas, listenSpace)).start();

            } catch (InterruptedException e) {

            }
        }

    }
}

class createPrivateServer implements Runnable {

    private String uuid;
    private SequentialSpace listenSpace;
//    private Object filepaths;
    private final String scriptpath;
    private final String datapath;

    public createPrivateServer(Object[] datas, SequentialSpace listenSpace){
        this.uuid = (String) datas[1];
        this.listenSpace = listenSpace;
        this.scriptpath = (String) datas[2];
        this.datapath = (String) datas[3];
    }

    @Override
    public void run() {

        String s = null;

        try {

            Process process = Runtime.getRuntime().exec(new String[]{"/bin/bash","-c","source /home/kamal/projects/quickml/env/bin/activate; python3 "+scriptpath});

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

            System.out.println("Here is the standard output of the command:\n");
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void createOutputCSV(Response response) {
        try {

            File downloadedFile = new File("/home/kamal/Downloads/data/out.csv");
            BufferedSink sink = Okio.buffer(Okio.sink(downloadedFile));
            sink.writeAll(response.body().source());
            sink.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}



class pipeToStdout implements Runnable {

    @Override
    public void run() {

        try {

            Space space = new SequentialSpace();
            SpaceRepository spaceRepository = new SpaceRepository();
            spaceRepository.add("space", space);
            spaceRepository.addGate("tcp://localhost:3690/?keep");

            while (true){

                String out = (String) space.get(new FormalField(String.class))[0];
                System.out.println(out);

            }


        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
