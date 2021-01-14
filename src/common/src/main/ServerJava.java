package common.src.main;
import com.opencsv.CSVWriter;
import okio.BufferedSink;
import okio.Okio;
import org.jspace.*;
import okhttp3.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static common.src.main.Constants.LISTEN_SPACE;

public class ServerJava {
    public static void main(String[] args){

        SequentialSpace listenSpace = new SequentialSpace();
        SpaceRepository spaceRepository = new SpaceRepository();

        spaceRepository.add(LISTEN_SPACE, listenSpace);
        spaceRepository.addGate("tcp://localhost:8080/?keep");

        while (true) {
            try {

                Object[] datas = listenSpace.get(new FormalField(String.class), new FormalField(Object.class), new FormalField(Object.class));
                new Thread(new CreatePrivateServer(datas, listenSpace)).start();

            } catch (InterruptedException e) {

            }
        }
    }
}

class CreatePrivateServer implements Runnable {

    private String uuid;
    private SequentialSpace listenSpace;
    private Object filepaths;

    public CreatePrivateServer(Object[] datas, SequentialSpace listenSpace){
        this.uuid = (String) datas[1];
        this.listenSpace = listenSpace;
        this.filepaths = datas[2];
    }

    @Override
    public void run() {
        ExecuteRequest();
    }


    public void ExecuteRequest(){

        try {

            // train

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("file","BankChurners.csv",
                            RequestBody.create(MediaType.parse("application/octet-stream"),
                                    new File("/home/kamal/projects/quickml/BankChurners.csv")))
                    .build();
            Request request = new Request.Builder()
                    .url("http://localhost:5555/coord/train/Dependent_count")
                    .method("POST", body)
                    .build();

            Response response = client.newCall(request).execute();

            String uuid = response.body().string();

            // test

            OkHttpClient client1 = new OkHttpClient().newBuilder()
                    .build();
            RequestBody body1 = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("file","BankChurners.csv",
                            RequestBody.create(MediaType.parse("application/octet-stream"),
                                    new File("/home/kamal/projects/quickml/BankChurners.csv")))
                    .addFormDataPart("id",uuid)
                    .build();
            Request request1 = new Request.Builder()
                    .url("http://localhost:5555/coord/test/")
                    .method("POST", body1)
                    .build();
            Response response1 = client1.newCall(request1).execute();

            createOutputCSV(response1);


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


