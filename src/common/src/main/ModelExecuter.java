package common.src.main;

import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import org.jspace.*;

import java.io.*;
import java.rmi.Remote;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static common.src.main.Constants.*;

public class ModelExecuter {
    public static void main(String[] args) throws InterruptedException {

        SequentialSpace listenSpace = new SequentialSpace();
        SequentialSpace resourceSpace = new SequentialSpace();
        SpaceRepository spaceRepository = new SpaceRepository();

        spaceRepository.add(RESOURCES_SPACE, resourceSpace);
        // add initial resources
        resourceSpace.put("resources", 3);
        spaceRepository.add(LISTEN_SPACE, listenSpace);
        spaceRepository.addGate("tcp://localhost:8080/?keep");

        while (true) {
            try {

                Object[] datas = listenSpace.get(
                        new FormalField(String.class),
                        new FormalField(Object.class),
                        new FormalField(String[].class),
                        new FormalField(Object.class),
                        new FormalField(String.class),
                        new FormalField(String.class)
                );

                // start new thread
                new Thread(new createPrivateServer(spaceRepository, datas, listenSpace, resourceSpace)).start();

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
    private final String mode;
    private final String column;
    private final SpaceRepository spaceRepository;
    private final SequentialSpace resourceSpace;

    public createPrivateServer(SpaceRepository spaceRepository, Object[] datas, SequentialSpace listenSpace, SequentialSpace resourceSpace){
        this.uuid = (String) datas[1];
        this.listenSpace = listenSpace;
        this.scriptPaths = (String[]) datas[2];
        this.datapath = (String) datas[3];
        this.mode = (String) datas[4];
        this.column = (String) datas[5];
        this.spaceRepository=spaceRepository;
        this.resourceSpace = resourceSpace;
    }

    @Override
    public void run() {

//        System.out.println(this.uuid+" "+this.l);
        System.out.println(this.mode);
        System.out.println(this.column);

        try {

            // create local space so that the manager can connect and send test data
            SequentialSpace privateSpace = new SequentialSpace();
            spaceRepository.add(uuid, privateSpace);
            spaceRepository.addGate("tcp://localhost:8080/?keep");

            // connect to manager space
            RemoteSpace managerSpace = new RemoteSpace("tcp://localhost:8000/"+UPDATES_SPACE+uuid+"?keep");

            // resources is >= script length then we can execute in parallel
            // otherwise we cannot
            // TODO: mutex in code block below?

            int resources = (int) resourceSpace.get(new ActualField("resources"), new FormalField(Integer.class))[1];
            System.out.println(resources);

            if (resources >= scriptPaths.length){

                // indicate to manager that execution is parallel
                managerSpace.put("mode", "parallel");
                // reduce resources to indicate that resources are in use
                resources =-scriptPaths.length;
                resourceSpace.put("resources", resources);
                // parallel
                executeParallel(managerSpace);
                // free resources

                int re = (int) resourceSpace.query(new ActualField("resources"), new FormalField(int.class))[1];
                if (re==0){
                    resourceSpace.put("resources", scriptPaths.length);
                } else {
                    int re1 = (int) resourceSpace.get(new ActualField("resources"), new FormalField(int.class))[1];
                    re1+= scriptPaths.length;
                    resourceSpace.put("resources", re1);
                }

            } else {
                // sequential

                // indicate to manager that execution is sequential
            }




        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    public void executeParallel(RemoteSpace managerSpace) {
        for (int i=0; i < scriptPaths.length; i++){
            int finalI = i;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String s = null;
                        String args = new String(datapath+" "+mode+" "+column+" "+uuid);
                        System.out.println("yolo");
                        System.out.println("source /home/kamal/projects/quickml/env/bin/activate; python3 " + scriptPaths[finalI] + " " + args);
                        Process process = Runtime.getRuntime().exec(new String[]{"/bin/bash", "-c", "source /home/kamal/projects/quickml/env/bin/activate; python3 " + scriptPaths[finalI] + " " + args});
                        boolean startmatch=false;
                        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

                        System.out.println("Here is the standard output of the command:\n");
                        while ((s = stdInput.readLine()) != null) {
//                                System.out.println(s);
                            // put updates in manager space
                            Pattern pattern;
                            String out = null;
//                                System.out.println(s);
                            if (finalI==0) {
                                pattern = Pattern.compile("[0-9]+\\s\\w+\\s[0-9]+");
                            } else {
                                pattern = Pattern.compile("\\s+[0-9]+");
                            }
                            Matcher matcher = pattern.matcher(s);

                            if (matcher.find()) {
                                out = matcher.group(0);
//
//                                    if (finalI==1 && startmatch){
//                                        managerSpace.put("updates" + finalI, ("Model " + finalI + " " + out));
//
//                                    }else if(finalI==0) {
//                                        managerSpace.put("updates" + finalI, ("Model " + finalI + " " + out));
////
//                                    }
//
//                                    if (finalI==1 && s.contains("rows")){
//                                        startmatch = true;
//                                    }
                                managerSpace.put("updates" + finalI, ("Model " + finalI + " " + out));


                            }

                        }

                        /**
                         * Start testing
                         */

//                            privateSpace.get("data", new FormalField())

                        // model is trained
//                            System.out.println("done "+finalI);
//
//                            // start model testing
//                            Process process1 = Runtime.getRuntime().exec(new String[]{"/bin/bash", "-c", "source /home/kamal/projects/quickml/env/bin/activate; python3 " + scriptPaths[finalI]});
//                            BufferedReader stdInput1 = new BufferedReader(new InputStreamReader(process1.getInputStream()));
//                            String s1=null;
//
//                            while ((s1 = stdInput1.readLine()) != null){
//
//                            }

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

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


