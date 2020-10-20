package codeAnalysis.processData;

import codeAnalysis.textTokenProcess.GloveVocab;
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
        String globalPath = System.getProperty("user.dir");
        String outputPath = "D:\\多行代码推荐\\GGNNData";
        ConstructGraph test = new ConstructGraph();
        GloveVocab gloveVocab = new GloveVocab();
        List<String> gloveVocabList = gloveVocab.getGloveList();
        List<String> stopWordsList = new ArrayList<>();
        // read jdk class name
        List<String> jdkList = new ArrayList<>();
        try {
            File fileClassNameMap = new File(globalPath + "/src/main/resources/vocab/JDKCLASS.txt");
            FileInputStream fileInputStream = new FileInputStream(fileClassNameMap);
            Scanner scanner2 = new Scanner(fileInputStream);
            while (scanner2.hasNextLine()) {
                String line = scanner2.nextLine();
                jdkList.add(line);
            }
            scanner2.close();
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Create FileWriters for this file
        ObjectOutputStream graphWriter = new ObjectOutputStream(new FileOutputStream(outputPath + "graph.txt"));
        // 以下为训练图结构必须的数据
        FileWriter traceWriter = new FileWriter(outputPath + "trace.txt", true);
        FileWriter predictionWriter = new FileWriter(outputPath + "prediction.txt", true);
        FileWriter graphSentenceWriter = new FileWriter(outputPath + "graph_represent.txt", true);
        FileWriter vocabWriter = new FileWriter(outputPath + "graph_vocab.txt",true);
        // 以下为附加的原有数据
        FileWriter classWriter = new FileWriter(outputPath+"class.txt",true);
        FileWriter generationNodeWriter = new FileWriter(outputPath+"generation_node.txt",true);
        FileWriter holeSizeWriter = new FileWriter(outputPath+"hole_size.txt",true);
        FileWriter blockpredictionWriter = new FileWriter(outputPath+"block_prediction.txt",true); // block of predictions (more lines)
        FileWriter originalStatementsWriter = new FileWriter(outputPath+"original_statement.txt",true); // original statements
        FileWriter variableNamesWriter = new FileWriter(outputPath+"variable_names.txt",true);// variable names
        FileWriter linesWriter = new FileWriter(outputPath+"lines.txt",true);// lines writer
        FileWriter methodNamesWriter = new FileWriter(outputPath+"method_names.txt",true);// lines writer
        while (true) {
            if (graphWriter != null && traceWriter != null) {
                break;
            } else {
                graphWriter = new ObjectOutputStream(new FileOutputStream(outputPath + "graph.txt"));
                traceWriter = new FileWriter(outputPath + "trace.txt", true);
            }
        }
        String path  = "C:\\Users\\Zero\\IdeaProjects\\HelloTest\\javaFilePath.txt";
        FileInputStream fileInputStream = new FileInputStream(path);
        Scanner scanner2 = new Scanner(fileInputStream);
        int index = 0;
        while (scanner2.hasNextLine()) {
            String filePath = scanner2.nextLine();
            System.out.println(++index +": " + filePath);
            // Construct Training data
//            if(index <= 70){
//                continue;
//            }
            try {
                File file = new File(filePath);
                if (file.length() / 1024 <= 200) {
                    test.constructGraph((int) 0, filePath, true, jdkList,
                            graphWriter,
                            traceWriter,
                            false, globalPath,
                            gloveVocabList, stopWordsList,
                            predictionWriter, graphSentenceWriter, vocabWriter,/*必须数据*/
                            classWriter, generationNodeWriter, holeSizeWriter, blockpredictionWriter,
                            originalStatementsWriter, variableNamesWriter, linesWriter /*原有数据*/,methodNamesWriter
                    );
                }
            } catch (Exception e) {
                continue;
                //e.printStackTrace();
            } catch(Error e){
                continue;
            }
        }
        graphWriter.writeObject(null);

        // close the writers
        graphWriter.close();
        traceWriter.close();
        predictionWriter.close();
        graphSentenceWriter.close();
        vocabWriter.close();
        classWriter.close();
        generationNodeWriter.close();
        holeSizeWriter.close();
        blockpredictionWriter.close();
        originalStatementsWriter.close();
        variableNamesWriter.close();
        linesWriter.close();
        methodNamesWriter.close();

        System.out.println("---");
        System.out.println("End of construct main.");
        long endTime = System.currentTimeMillis();
        String time = formatTime(endTime - startTime);
        System.out.println("total time: " + time);
        System.err.println(test.linesCount);
        System.err.println(test.test);
    }

    public static String formatTime(Long ms) {
        Integer ss = 1000;
        Integer mi = ss * 60;
        Integer hh = mi * 60;
        Integer dd = hh * 24;
        Long day = ms / dd;
        Long hour = (ms - day * dd) / hh;
        Long minute = (ms - day * dd - hour * hh) / mi;
        Long second = (ms - day * dd - hour * hh - minute * mi) / ss;
        Long milliSecond = ms - day * dd - hour * hh - minute * mi - second * ss;
        StringBuffer sb = new StringBuffer();
        if (day > 0) {
            sb.append(day + "d");
        }
        if (hour > 0) {
            sb.append(hour + "h");
        }
        if (minute > 0) {
            sb.append(minute + "m");
        }
        if (second > 0) {
            sb.append(second + "s");
        }
        if (milliSecond > 0) {
            sb.append(milliSecond + "ms");
        }
        return sb.toString();
    }
}
