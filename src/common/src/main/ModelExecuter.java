package common.src.main;
import org.jspace.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.EmptyStackException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static common.src.main.Constants.*;

public class ModelExecuter {
    public static void main(String[] args) throws InterruptedException {

        SequentialSpace listenSpace = new SequentialSpace();
        SequentialSpace resourceSpace = new SequentialSpace();
        SpaceRepository spaceRepository = new SpaceRepository();

        spaceRepository.add(RESOURCES_SPACE, resourceSpace);

        // set initial resources
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
                        new FormalField(Object.class),
                        new FormalField(String.class),
                        new FormalField(String.class)
                );

                // start new thread for specific model manager
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
    private String datapath;
    private String mode;
    private final String column;
    private final SpaceRepository spaceRepository;
    private final SequentialSpace resourceSpace;
    private String testpath;

    public createPrivateServer(SpaceRepository spaceRepository, Object[] datas, SequentialSpace listenSpace, SequentialSpace resourceSpace){
        this.uuid = (String) datas[1];
        this.listenSpace = listenSpace;
        this.scriptPaths = (String[]) datas[2];
        this.datapath = (String) datas[3];
        this.testpath = (String) datas[4];
        this.mode = (String) datas[5];
        this.column = (String) datas[6];
        this.spaceRepository=spaceRepository;
        this.resourceSpace = resourceSpace;
    }

    @Override
    public void run() {

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

            // TODO: expain get query things here in the report?
            int resourcesQ = (int) resourceSpace.query(new ActualField("resources"), new FormalField(Integer.class))[1];
            System.out.println(resourcesQ);

            if (resourcesQ >= scriptPaths.length){
                int resources = (int) resourceSpace.get(new ActualField("resources"), new FormalField(Integer.class))[1];
                // indicate to manager that execution is parallel
                managerSpace.put("mode", "parallel");
                // reduce resources to indicate that resources are in use
                resources -= scriptPaths.length;
                // update resources in tuple space
                resourceSpace.put("resources", resources);
                // parallel execution
                executeParallel(managerSpace);


            } else {
                // sequential
                // indicate to manager that execution is sequential
                managerSpace.put("mode", "sequential");

                for (int i=0; i < scriptPaths.length; i++){
                    int resources = (int) resourceSpace.get(new ActualField("resources"), new FormalField(Integer.class))[1];
                    // reduce resrouces to indicate in use
                    resources -= 1;
                    // update resources space
                    resourceSpace.put("resources", resources);
                    // seq execution
                    executeSequential(managerSpace, scriptPaths[i], i);
                    int re = (int) resourceSpace.query(new ActualField("resources"), new FormalField(Integer.class))[1];

                    if (re==0){
                        resourceSpace.put("resources", 1);
                    } else {
                        int re1 = (int) resourceSpace.get(new ActualField("resources"), new FormalField(Integer.class))[1];
                        re1+= 1;
                        resourceSpace.put("resources", re1);
                    }
                }

            }


        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }


    public void executeSequential(RemoteSpace managerSpace, String scriptPath, int index) {

                try {

                    String s = null;
                    String args = new String(datapath+" "+testpath+" "+mode+" "+column+" "+uuid);
                    System.out.println("source /home/kamal/projects/quickml/env/bin/activate; python3 " + scriptPath + " " + args);
                    Process process = Runtime.getRuntime().exec(new String[]{"/bin/bash", "-c", "source /home/kamal/projects/quickml/env/bin/activate; python3 " + scriptPath + " " + args});
                    BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

                    while ((s = stdInput.readLine()) != null) {

                        if (s.contains("done")){

                            String name = scriptPath.split("/")[scriptPath.split("/").length-1];
                            // indicate to manager that execution is done
                            managerSpace.put("updates", ("Model " + name + " " + s), index);

                            break;
                        }

                        Pattern pattern;
                        String out = null;

                        if (scriptPath.contains("forest")) {
                            pattern = Pattern.compile("[0-9]+\\s\\w+\\s[0-9]+");
                        } else {
                            pattern = Pattern.compile("\\s+[0-9]+");
                        }

                        Matcher matcher = pattern.matcher(s);

                        if (matcher.find()) {
                            out = matcher.group(0);

                            String name = scriptPath.split("/")[scriptPath.split("/").length-1];
                            // send progress updates to manager
                            managerSpace.put("updates", ("Model " + name + " " + out), index);

                        }

                    }

                    System.out.println("finished");

                } catch (IOException | InterruptedException e) {
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
                        String args = new String(datapath+" "+testpath+" "+mode+" "+column+" "+uuid);
                        System.out.println("yolo");
                        System.out.println("source /home/kamal/projects/quickml/env/bin/activate; python3 " + scriptPaths[finalI] + " " + args);
                        Process process = Runtime.getRuntime().exec(new String[]{"/bin/bash", "-c", "source /home/kamal/projects/quickml/env/bin/activate; python3 " + scriptPaths[finalI] + " " + args});
                        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

                        System.out.println("Here is the standard output of the command:\n");
                        while ((s = stdInput.readLine()) != null) {

                            if (s.contains("done")){
                                // indicate to manager that execution is done
                                managerSpace.put("updates" + finalI, ("Model " + finalI + " " + s));
                                break;
                            }

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

                                // send progress updates to manager
                                managerSpace.put("updates" + finalI, ("Model " + finalI + " " + out));

                            }

                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }
    }


}


