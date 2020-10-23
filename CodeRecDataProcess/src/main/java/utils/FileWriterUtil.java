package utils;

import java.io.*;

/**
 * @author coldilock
 */
public class FileWriterUtil {
    private static String outputPath;
    // Create FileWriters for this file
    public static ObjectOutputStream graphWriter;
    // 以下为训练图结构必须的数据
    public static FileWriter traceWriter;
    public static FileWriter predictionWriter;
    public static FileWriter graphSentenceWriter;
    public static FileWriter vocabWriter;
    // 以下为附加的原有数据
    public static FileWriter classWriter;
    public static FileWriter generationNodeWriter;
    public static FileWriter holeSizeWriter;
    public static FileWriter blockpredictionWriter; // block of predictions (more lines)
    public static FileWriter originalStatementsWriter; // original statements
    public static FileWriter variableNamesWriter;// variable names
    public static FileWriter linesWriter;// lines writer
    public static FileWriter methodNamesWriter;// lines writer

    public static void createWriters(String path) throws IOException{
        outputPath = path;
        graphWriter = new ObjectOutputStream(new FileOutputStream(outputPath + "graph.txt"));
        // 以下为训练图结构必须的数据
        traceWriter = new FileWriter(outputPath + "trace.txt", true);
        predictionWriter = new FileWriter(outputPath + "prediction.txt", true);
        graphSentenceWriter = new FileWriter(outputPath + "graph_represent.txt", true);
        vocabWriter = new FileWriter(outputPath + "graph_vocab.txt",true);
        // 以下为附加的原有数据
        classWriter = new FileWriter(outputPath+"class.txt",true);
        generationNodeWriter = new FileWriter(outputPath+"generation_node.txt",true);
        holeSizeWriter = new FileWriter(outputPath+"hole_size.txt",true);
        // block of predictions (more lines)
        blockpredictionWriter = new FileWriter(outputPath+"block_prediction.txt",true);
        // original statements
        originalStatementsWriter = new FileWriter(outputPath+"original_statement.txt",true);
        // variable names
        variableNamesWriter = new FileWriter(outputPath+"variable_names.txt",true);
        // lines writer
        linesWriter = new FileWriter(outputPath+"lines.txt",true);
        // method names writer
        methodNamesWriter = new FileWriter(outputPath+"method_names.txt",true);
    }

    public static void closeWriters() throws IOException {
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
    }
}
