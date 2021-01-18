package common.src.main;
import org.jspace.*;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import static common.src.main.Constants.*;
import static common.src.main.Constants.*;

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
                Object[] datas = listenSpace.get(
                        new FormalField(String.class),
                        new FormalField(String.class),
                        new FormalField(Object.class),
                        new FormalField(String.class),
                        new FormalField(String.class)
                );


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
    String column;
    String mode;

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
        this.mode = (String) datas[3];
        this.column = (String) datas[4];
    }

    @Override
    public void run() {

        System.out.println(this.data);

        try {

            // Create own private space for model executor to write to
            SequentialSpace userUpdatesSpace = new SequentialSpace();
            SequentialSpace updatesSpace = new SequentialSpace();

            /**
             * put global lock
             * put empty updates list
             */
            updatesSpace.put("lock");
            updatesSpace.put("updates0", "");
            updatesSpace.put("updates1", "");

            spaceRepository.add(UPDATES_SPACE+uuid, updatesSpace);
            spaceRepository.add(USER_UPDATES_SPACE+uuid, userUpdatesSpace);
            spaceRepository.addGate("tcp://localhost:8000/?keep");

            // put signal back to user that space has been created
            listenSpace.put("server-"+uuid.toString(), "ok");

            // Connect to ModelExecuter
            RemoteSpace serverSpace = new RemoteSpace("tcp://localhost:8080/"+LISTEN_SPACE+"?keep");

            // model manager puts the script and data
            serverSpace.put(uuid+"-exec", uuid ,modelPaths, data.getPath(), mode, column);


            // waits for updates from model executor

            // here we must wait and see if the execution is parallel or sequential
            // TODO:// branching good for report
            String mode = (String) updatesSpace.get(new ActualField("mode"), new FormalField(String.class))[1];
            if (mode.equals("parallel")) {

                while (true){

                    Object[] updates1 = updatesSpace.get(new ActualField("updates0"), new FormalField(String.class));
                    Object[] updates2 = updatesSpace.get(new ActualField("updates1"), new FormalField(String.class));

                    if (!updates1[1].toString().equals("") && !updates2[1].toString().equals("")) {

                        int a = Integer.parseInt(updates1[1].toString().split("\\s")[2]);
                        int b = Integer.parseInt(updates1[1].toString().split("\\s")[4]);

                        float c = ((float) a / (float) b) * 100.0f;

                        int d = Integer.parseInt(updates2[1].toString().split("\\s+")[2]);

                        System.out.println((int) c + "%" + " " + (int) d + "%");

                        String updates = (int) c + "%" + " " + (int) d + "%";

                        // put the prettied content to userupdatespace

                        if (((int) c)==100 && ((int) d)==100) {
                            // TODO: signal break out of loop to user
                            userUpdatesSpace.put("updates", updates, "loop", "break");
//                            userUpdatesSpace.put("loop", "break");
                            break;
                        } else {
                            // TODO: otherwise continue sending updates
                            userUpdatesSpace.put("updates", updates, "loop", "continue");
                        }


                    }


                }
            } else {
                // sequential mode
                while (true) {

                    Object[] updates1 = updatesSpace.get(new ActualField("updates"), new FormalField(String.class), new FormalField(Integer.class));

                      if (updates1[1].toString().contains("forest")) {

                        int a = Integer.parseInt(updates1[1].toString().split("\\s")[2]);
                        int b = Integer.parseInt(updates1[1].toString().split("\\s")[4]);

                        float c = ((float) a / (float) b) * 100.0f;

                        System.out.println((int) c + "%" );

                        String updates = (int) c + "%";

                        // put the prettied content to userupdatespace

                        if (((int) c)==100 ) {
                            // TODO: signal break out of loop to user
                            if ((int) updates1[2] == modelPaths.length-1){
                                userUpdatesSpace.put("updates", updates, "loop", "break");
                                break;
                            } else {
                                userUpdatesSpace.put("updates", updates, "loop", "continue");
                            }

                        } else {
                            // TODO: otherwise continue sending updates
                            userUpdatesSpace.put("updates", updates, "loop", "continue");
                        }


                    } else {


                          int d = Integer.parseInt(updates1[1].toString().split("\\s+")[2]);

                          System.out.println((int) d + "%");

                          String updates = (int) d + "%";

                          // put the prettied content to userupdatespace

                          if (((int) d)==100) {
                              // TODO: signal break out of loop to user
                              if ((int) updates1[2] == modelPaths.length-1){
                                  userUpdatesSpace.put("updates", updates, "loop", "break");
                                  break;
                              } else {
                                  userUpdatesSpace.put("updates", updates, "loop", "continue");
                              }
                          } else {
                              // TODO: otherwise continue sending updates
                              userUpdatesSpace.put("updates", updates, "loop", "continue");
                          }

                      }
                }
            }

            // training finished
            // start testing now
            // wait for updates from user
            Object[] testdata = userUpdatesSpace.get(new FormalField(String.class), new FormalField(String.class));
            String testpath = (String) testdata[1];
            // connect to private model executor space
            RemoteSpace privateexec = new RemoteSpace("tcp://localhost:8080/"+uuid+"?keep");
            // send data to private model executor space
            privateexec.put("testdata", testpath, modelPaths);

//            privateexec.put(uuid+"-exec", uuid ,modelPaths, data.getPath(), mode, column);

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }



    }
}
