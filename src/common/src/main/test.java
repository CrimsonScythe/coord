package common.src.main;

import org.apache.commons.exec.*;
import org.jspace.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class test {
    public static void main(String[] args) {

        try {
//        String s = null;
//
//        String line = "python3 "+"/home/kamal/Downloads/tst.py";
//        CommandLine cmdLine = CommandLine.parse(line);
//        DefaultExecutor executor = new DefaultExecutor();
//        int exitValue = executor.execute(cmdLine);
//
//        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
//
//        ExecuteWatchdog watchdog = new ExecuteWatchdog(60*1000);
//        Executor executor = new DefaultExecutor();
////        executor.setExitValue(1);
////        executor.setWatchdog(watchdog);
//        executor.execute(cmdLine, resultHandler);

// some time later the result handler callback was invoked so we
// can safely request the exit value

//        int exitValue = resultHandler.waitFor();



//            RemoteSpace remoteSpace = new RemoteSpace("tcp://localhost:3690/space?keep");



            Process process = Runtime.getRuntime().exec("python3 "+"/home/kamal/Downloads/tst.py");

            String s=null;
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

            System.out.println("Here is the standard output of the command:\n");
            try {
                while ((s = stdInput.readLine()) != null) {
                    System.out.println(s);
//                remoteSpace.put(s);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }


//            new Thread(new pipeToStdout2(process, remoteSpace)).start();



        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

class pipeToStdout2 implements Runnable {
    Process process;
    Space remoteSpace;


    public pipeToStdout2(Process process, Space remoteSpace){
        this.process=process;
        this.remoteSpace=remoteSpace;
    }

    @Override
    public void run() {
        String s=null;
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

        System.out.println("Here is the standard output of the command:\n");
        try {
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
//                remoteSpace.put(s);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}