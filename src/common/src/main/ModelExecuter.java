package common.src.main;

import okhttp3.*;
import okio.Buffer;
import okio.BufferedSink;
import okio.Okio;
import org.apache.commons.io.IOUtils;
import org.jspace.*;

import javax.script.*;
import java.io.*;
import java.util.function.Consumer;

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




            new Thread(new pipeToStdout()).start();

            RemoteSpace remoteSpace = new RemoteSpace("tcp://localhost:3690/space?keep");

//            "source projects/quickml/env/bin/activate; python3 /home/kamal/Documents/forest.py";

            Process p = new ProcessBuilder(new String[]{"/bin/bash","-c","source /home/kamal/projects/quickml/env/bin/activate; python3 "+scriptpath}).start();
//            Logger logger = LoggerFactory.getLogger(getClass());

            StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), System.out::println);
//            StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), logger::error);

            new Thread(outputGobbler).start();
//            new Thread(errorGobbler).start();
            p.waitFor();

            /**

            Process process = Runtime.getRuntime().exec(new String[]{"/bin/bash","-c","source /home/kamal/projects/quickml/env/bin/activate; python3 "+scriptpath});


            //            Process process = Runtime.getRuntime().exec("python3 "+scriptpath);



            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));


//            BufferedReader stdError = new BufferedReader(new
//                    InputStreamReader(process.getErrorStream()));

            // read the output from the command
            System.out.println("Here is the standard output of the command:\n");
            while ((s = stdInput.readLine()) != null) {
                try {
                    remoteSpace.put(s);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                System.out.println(s);
            }

            // read any errors from the attempted command
//            System.out.println("Here is the standard error of the command (if any):\n");
//            while ((s = stdError.readLine()) != null) {
//                System.out.println(s);
//            }

//            System.exit(0);

             */

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
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

class StreamGobbler implements Runnable {
    private InputStream inputStream;
    private Consumer<String> consumeInputLine;

    public StreamGobbler(InputStream inputStream, Consumer<String> consumeInputLine) {
        this.inputStream = inputStream;
        this.consumeInputLine = consumeInputLine;
    }

    public void run() {
        new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumeInputLine);
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
