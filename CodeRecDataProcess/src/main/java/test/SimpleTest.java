//package test;
//
//import codeAnalysis.codeRepresentation.Graph;
//import codeAnalysis.textTokenProcess.GloveVocab;
//import config.DataConfig;
//import utils.GraphWriteUtil;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Scanner;
//
//public class SimpleTest {
//    public static void main(String[] args) throws Exception{
//
//        if(args.length == 0){
//            System.out.println("请指定配置文件路径");
//            System.exit(0);
//        }
//        if(!new File(args[0]).exists()){
//            System.out.printf("%s 不存在", args[0]);
//            System.exit(0);
//        } else {
//            DataConfig.loadConfig(args[0]);
//        }
//
//        String globalPath = System.getProperty("user.dir");
//        GloveVocab gloveVocab = new GloveVocab();
//        List<String> gloveVocabList = gloveVocab.getGloveList();
//        //StopWords stopWords = new StopWords();
//        //List<String> stopWordsList = stopWords.getStopWordsList();
//        List<String> stopWordsList = new ArrayList<>();
//
//        // read jdk class name
//        List<String> jdkList = new ArrayList<>();
//        try {
//            File fileClassNameMap = new File(DataConfig.JDKCLASS_VOCAB_FILE_PATH);
//            FileInputStream fileInputStream = new FileInputStream(fileClassNameMap);
//            Scanner scanner2 = new Scanner(fileInputStream);
//            while (scanner2.hasNextLine()) {
//                String line = scanner2.nextLine();
//                jdkList.add(line);
//            }
//            scanner2.close();
//            fileInputStream.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        String filePath = DataConfig.TEST_INPUT_JAVA_FILE;
//        // String filePath = "/Users/lingxiaoxia/Desktop/graphtestset/emailTestCaseSourceCode/_Users_lingxiaoxia_Desktop_ICSE2018相关_ICSE related file and data_test set_Froyo_Email-master_src_com_android_email_activity_AccountFolderList.java_292to336_4_FineGrain.java";
//        Predict g = new Predict();
//        long l = System.currentTimeMillis();
//        Graph graph = g.getCodeGraph(filePath, true, false, globalPath, jdkList, gloveVocabList, stopWordsList).get(0);
//        long l2 = System.currentTimeMillis();
//        System.err.println(l2 - l);
//        //GraphCheckerStopInHole graphChecker = new GraphCheckerStopInHole();
//
//        //Graph graph = graphChecker.constructGraph(filePath,globalPath,true,true,jdkList,gloveVocabList,stopWordsList);
//
//        try {
//            graph.setSerialNumberofNode(graph.getRoot(),new ArrayList<>());
//            GraphWriteUtil.show(graph.getRoot(), DataConfig.TEST_OUTPUT_GRAPH_PATH);
//            graph.printEdgeType(graph.getRoot(), new ArrayList<>());
//            //graph.print(graph.getRoot(), new ArrayList<>());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
