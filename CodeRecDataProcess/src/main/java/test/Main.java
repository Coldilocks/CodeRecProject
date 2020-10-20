package test;

import codeAnalysis.codeRepresentation.Graph;
import codeAnalysis.textTokenProcess.GloveVocab;
import utils.GraphWriteUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception{
        String globalPath = System.getProperty("user.dir");
        GloveVocab gloveVocab = new GloveVocab();
        List<String> gloveVocabList = gloveVocab.getGloveList();
        //StopWords stopWords = new StopWords();
        //List<String> stopWordsList = stopWords.getStopWordsList();
        List<String> stopWordsList = new ArrayList<>();

        // read jdk class name
        List<String> jdkList = new ArrayList<>();
        try {
            File fileClassNameMap = new File("C:\\Users\\zero\\IdeaProjects\\CodeRecProject\\CodeRecDataProcess\\src\\main\\resources\\vocab\\JDKCLASS.txt");
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
        String filePath = "C:\\Users\\zero\\Desktop\\yyd-temp\\CodeRec Project\\CodeRec Project\\input\\api1.java";
        // String filePath = "/Users/lingxiaoxia/Desktop/graphtestset/emailTestCaseSourceCode/_Users_lingxiaoxia_Desktop_ICSE2018相关_ICSE related file and data_test set_Froyo_Email-master_src_com_android_email_activity_AccountFolderList.java_292to336_4_FineGrain.java";
        Predict g = new Predict();
        long l = System.currentTimeMillis();
        Graph graph = g.getCodeGraph(filePath, true, false, globalPath, jdkList, gloveVocabList, stopWordsList).get(0);
        long l2 = System.currentTimeMillis();
        System.err.println(l2 - l);
        //GraphCheckerStopInHole graphChecker = new GraphCheckerStopInHole();

        //Graph graph = graphChecker.constructGraph(filePath,globalPath,true,true,jdkList,gloveVocabList,stopWordsList);

        try {
            graph.setSerialNumberofNode(graph.getRoot(),new ArrayList<>());
            GraphWriteUtil.show(graph.getRoot(), "C:\\Users\\zero\\Desktop\\yyd-temp\\CodeRec Project\\CodeRec Project\\new-test\\2.dot");
            graph.printEdgeType(graph.getRoot(), new ArrayList<>());
            //graph.print(graph.getRoot(), new ArrayList<>());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
