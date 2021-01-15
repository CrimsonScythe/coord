package common.src.main;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import org.jspace.*;

import java.io.*;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static common.src.main.Constants.LISTEN_SPACE;
import static common.src.main.Constants.UPDATES_SPACE;

public class ModelExecuter {
    public static void main(String[] args) {

        SequentialSpace listenSpace = new SequentialSpace();
        SpaceRepository spaceRepository = new SpaceRepository();

        spaceRepository.add(LISTEN_SPACE, listenSpace);
        spaceRepository.addGate("tcp://localhost:8080/?keep");

        while (true) {
            try {

                Object[] datas = listenSpace.get(new FormalField(String.class), new FormalField(Object.class), new FormalField(String[].class), new FormalField(Object.class));

                // start new thread
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
    private final String[] scriptPaths;
    private final String datapath;


    public createPrivateServer(Object[] datas, SequentialSpace listenSpace){
        this.uuid = (String) datas[1];
        this.listenSpace = listenSpace;
        this.scriptPaths = (String[]) datas[2];
        this.datapath = (String) datas[3];

    }

    @Override
    public void run() {

        try {
            // connect to manager space
            RemoteSpace managerSpace = new RemoteSpace("tcp://localhost:8000/"+UPDATES_SPACE+uuid+"?keep");


//            String scriptpath="/home/kamal/Documents/boost.py";

            // IO operations so we must spawn new thread for each model in modellist

            for (int i=0; i<scriptPaths.length; i++){

                int finalI = i;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String s = null;
                            Process process = Runtime.getRuntime().exec(new String[]{"/bin/bash", "-c", "source /home/kamal/projects/quickml/env/bin/activate; python3 " + scriptPaths[finalI]});

                            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

                            System.out.println("Here is the standard output of the command:\n");
                            while ((s = stdInput.readLine()) != null) {
//                                System.out.println(s);
                                // put updates in manager space
                                Pattern pattern;
                                String out = null;

                                if (finalI==0) {
                                    pattern = Pattern.compile("[0-9]+\\s\\w+\\s[0-9]+");
                                } else {
                                    pattern = Pattern.compile("\\s+[0-9]+");
                                }
                                Matcher matcher = pattern.matcher(s);

                                if (matcher.find()) {
                                    out = matcher.group(0);
//                                    managerSpace.get(new ActualField("lock"));
//                                    System.out.println("got lock");

//                                    Object[] res=managerSpace.get(new ActualField("updates"), new FormalField(Object.class));
//                                    System.out.println("ssss");
//                                    ((String[]) res[1])[finalI] =  ("Model "+finalI+" " + out);

                                    managerSpace.put("updates"+finalI, ("Model "+finalI+" " + out));
//                                    managerSpace.put("updates", new String[] {("Model "+finalI+" " + out), "ss"});
//                                    managerSpace.put("Model "+finalI, out);
//                                    managerSpace.put("lock");
//                                    System.out.println("put lcok");
                                }


//                                managerSpace.put("Model "+finalI, s);
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

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
