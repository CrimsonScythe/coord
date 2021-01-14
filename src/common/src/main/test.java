package common.src.main;

import org.jspace.RemoteSpace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class test {
    public static void main(String[] args) {

        try {

            new Thread(new pipeToStdout2()).start();

            RemoteSpace remoteSpace = new RemoteSpace("tcp://localhost:3690/space?keep");


            Process process = Runtime.getRuntime().exec("python3 "+"/home/kamal/Downloads/tst.py");

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));



            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(process.getErrorStream()));

            // read the output from the command
//            System.out.println("Here is the standard output of the command:\n");
//            while ((s = stdInput.readLine()) != null) {
//                try {
//                    remoteSpace.put(s);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
////                System.out.println(s);
//            }

            // read any errors from the attempted command
//            System.out.println("Here is the standard error of the command (if any):\n");
//            while ((s = stdError.readLine()) != null) {
//                System.out.println(s);
//            }

            System.exit(0);


        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

class pipeToStdout2 implements Runnable {

    @Override
    public void run() {

    }
}