package common.src.main;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Utility class from here: https://stackoverflow.com/a/24903576/4070357
 */

public class split {
    public static List<String> split(String filepath, String uuid)
    {
        try{
            // Reading file and getting no. of files to be generated
//            filepath = "/home/kamal/Downloads/data/ks-projects.csv";
            String inputfile = filepath; //  Source File Name.
            double nol = 38000.0; //  No. of lines to be split and saved in each output file.
            File file = new File(inputfile);
            Scanner scanner = new Scanner(file);
            int count = 0;
            while (scanner.hasNextLine())
            {
                scanner.nextLine();
                count++;
            }
            System.out.println("Lines in the file: " + count);     // Displays no. of lines in the input file.

            double temp = (count/nol);
            int temp1=(int)temp;
            int nof=0;
            if(temp1==temp)
            {
                nof=temp1;
            }
            else
            {
                nof=temp1+1;
            }
            System.out.println("No. of files to be generated :"+nof); // Displays no. of files to be generated.

            //---------------------------------------------------------------------------------------------------------

            // Actual splitting of file into smaller files

            FileInputStream fstream = new FileInputStream(inputfile); DataInputStream in = new DataInputStream(fstream);

            BufferedReader br = new BufferedReader(new InputStreamReader(in)); String strLine;

//            List<ByteArrayOutputStream> outputStreams = new ArrayList<>();

            List<String> outputFilePaths = new ArrayList<>();

            for (int j=1;j<=nof;j++)
            {
//                new OutputStreamWriter()
//                new FileWriter(new File())
                outputFilePaths.add("/home/kamal/Downloads/data/"+uuid+"/File"+j+".csv");
                FileWriter fstream1 = new FileWriter("/home/kamal/Downloads/data/"+uuid+"/File"+j+".csv");     // Destination File Location
//                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//                OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream);
//                BufferedWriter out = new BufferedWriter(streamWriter);
                BufferedWriter out = new BufferedWriter(fstream1);


                for (int i=1;i<=nol;i++)
                {
                    strLine = br.readLine();
                    if (strLine!= null)
                    {
                        out.write(strLine);
                        if(i!=nol)
                        {
                            out.newLine();
                        }
                    }
                }

                out.close();
//                outputStreams.add(outputStream);
            }

            in.close();

            return outputFilePaths;

        } catch (Exception e)
        {
            System.err.println("Error: " + e.getMessage());
        }

        return null;
    }

} 