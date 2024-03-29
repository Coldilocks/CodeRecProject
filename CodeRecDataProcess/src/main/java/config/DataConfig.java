package config;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class DataConfig {

    // 输入的Java文件路径
    public static String JAVA_FILE_PATH;

    // 输出文件
    public static String OUTPUT_PATH;

    // JDK Class Vocab
    public static String JDKCLASS_VOCAB_FILE_PATH;

    // glove词表
    public static String GLOVE_VOCAB_PATH;

    // 常用停用词表
    public static String STOP_WORDS_PATH;

    // class_name_map配置文件
    public static String CLASS_NAME_MAP_CONFIG_FILE_PATH;

    public static String ANDROID_CLASS_FILE_PATH;

    // type_cast配置文件
    public static String TYPE_CAST_CONFIG_FILE_PATH;

    // 输出的graph.txt路径
    public static String OUTPUT_GRAPH_PATH;

    // 进行预测的python代码路径
    public static String GGNN_CLIENT_PYTHON_FILE_PATH;

    // 分词服务访问路径
    public static String URL;

    // 输入的Java文件（测试用）
    public static String TEST_INPUT_JAVA_FILE = "/Users/coldilock/Downloads/CodeRec Project/input/api2.java";

    // 输出的图（测试用）
    public static String TEST_OUTPUT_GRAPH_PATH = "/Users/coldilock/Downloads/CodeRec Project/Test/2.dot";

    // 调试用的路径
    public static String FINE_TEST_GRAPH_DATA_CONSTRUCT_PATH = "/Users/wangxin/Workspace/04-DataSpace/CodeRecommendation/GraphDataConstruct/graph/graph_";

    // 调试用的路径
    public static String PRE_TEST_OR_TRAIN_GRAPH_DATA_CONSTRUCT_PATH = "/Users/wangxin/Workspace/01-CodeSpace/02-TempDir/GraphDataConstruct/graph/graph_";

    private static final Properties properties = new Properties();

    /**
     * 加载配置
     */
    public static void loadConfig(String configFile){
        try{
            properties.load(new FileReader(configFile));
            JAVA_FILE_PATH = properties.getProperty("javaFilePath");
            OUTPUT_PATH = properties.getProperty("outputPath");
            JDKCLASS_VOCAB_FILE_PATH = properties.getProperty("JDKClassVocab");
            GLOVE_VOCAB_PATH = properties.getProperty("gloveVocab");
            STOP_WORDS_PATH = properties.getProperty("stopWords");
            CLASS_NAME_MAP_CONFIG_FILE_PATH = properties.getProperty("classNameMap");
            ANDROID_CLASS_FILE_PATH = properties.getProperty("AndroidClass");
            TYPE_CAST_CONFIG_FILE_PATH = properties.getProperty("typeCast");
            OUTPUT_GRAPH_PATH = properties.getProperty("outputGraphPath");
            GGNN_CLIENT_PYTHON_FILE_PATH = properties.getProperty("ggnnClient");
            URL = properties.getProperty("url");
        } catch (IOException e){
            System.exit(0);
        }
    }


}
