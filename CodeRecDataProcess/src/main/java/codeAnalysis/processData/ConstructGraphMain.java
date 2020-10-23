package codeAnalysis.processData;

import codeAnalysis.textTokenProcess.GloveVocab;
import config.DataConfig;
import utils.FileWriterUtil;
import utils.TimeUtil;

import javax.xml.crypto.Data;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * For Testing Construct Graph Data
 */
public class ConstructGraphMain {

    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();

        // Read the configuration file
        if(args.length == 0){
            System.out.println("请指定配置文件路径");
            System.exit(0);
        }
        if(!new File(args[0]).exists()){
            System.out.printf("%s 不存在", args[0]);
            System.exit(0);
        } else {
            DataConfig.loadConfig(args[0]);
        }

        String globalPath = System.getProperty("user.dir");

        String outputPath = DataConfig.OUTPUT_PATH;

        ConstructGraph graphConstructor = new ConstructGraph();
        GloveVocab gloveVocab = new GloveVocab();
        List<String> gloveVocabList = gloveVocab.getGloveList();
        List<String> stopWordsList = new ArrayList<>();

        // Read JDKClassName from file
        List<String> jdkList = new ArrayList<>();
        try {
            File fileClassNameMap = new File(DataConfig.JDKCLASS_VOCAB_FILE_PATH);
            FileInputStream fileInputStream = new FileInputStream(fileClassNameMap);
            Scanner scanner = new Scanner(fileInputStream);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                jdkList.add(line);
            }
            scanner.close();
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create FileWriters for output
        FileWriterUtil.createWriters(outputPath);

        // Read txt file to get java file paths
        String javaFilePath  = DataConfig.JAVA_FILE_PATH;
        FileInputStream fileInputStream = new FileInputStream(javaFilePath);
        Scanner scanner = new Scanner(fileInputStream);
        int index = 0;
        while (scanner.hasNextLine()) {
            String singleJavaFilePath = scanner.nextLine();
            System.out.println(++index +": " + singleJavaFilePath);
            // Construct Training data
            File file = new File(singleJavaFilePath);
            if (file.length() / 1024 <= 200) {
                graphConstructor.constructGraph(
                        0, singleJavaFilePath, true,
                        jdkList, false, globalPath,
                        gloveVocabList, stopWordsList
                );
            }
        }

        FileWriterUtil.graphWriter.writeObject(null);

        // close the writers
        FileWriterUtil.closeWriters();

        System.out.println("---");
        System.out.println("End of construct main.");
        long endTime = System.currentTimeMillis();
        String time = TimeUtil.formatTime(endTime - startTime);
        System.out.println("total time: " + time);
        System.err.println(graphConstructor.linesCount);
        System.err.println(graphConstructor.test);
    }

}
