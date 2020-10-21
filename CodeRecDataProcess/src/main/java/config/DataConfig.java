package config;

public class DataConfig {
    // 输入的Java文件路径
    public static final String JAVA_FILE_PATH = "/Users/coldilock/Downloads/CodeRec Project/input/javaFilePath.txt";

    // 输出文件
    public static final String OUTPUT_PATH = "/Users/coldilock/Downloads/CodeRec Project/output/";

    // 输入的Java文件（测试用）
    public static final String TEST_INPUT_JAVA_FILE = "/Users/coldilock/Downloads/CodeRec Project/input/api1.java";

    // 输出的图（测试用）
    public static final String TEST_OUTPUT_GRAPH_PATH = "/Users/coldilock/Downloads/CodeRec Project/Test/2.dot";

    // JDK Class Vocab
    public static final String JDKCLASS_VOCAB_FILE_PATH = "/Users/coldilock/Documents/Code/Github/CodeRecProject/CodeRecDataProcess/src/main/resources/vocab/JDKCLASS.txt";

    // glove词表
    public static final String GLOVE_VOCAB_PATH = "/Users/coldilock/Documents/Code/Github/CodeRecProject/CodeRecDataProcess/src/main/resources/vocab/gloveVocab.txt";

    // 常用停用词表
    public static final String STOP_WORDS_PATH = "/Users/coldilock/Documents/Code/Github/CodeRecProject/CodeRecDataProcess/src/main/resources/vocab/stopWords.txt";

    // 输出的graph.txt路径
    public static final String OUTPUT_GRAPH_PATH = "/Users/coldilock/Documents/Code/Github/CodeRecProject/CodeRecDataProcess/src/main/resources/outputGraph/graph.txt";

    // class_name_map配置文件
    public static final String CLASS_NAME_MAP_CONFIG_FILE_PATH = "/Users/coldilock/Documents/Code/Github/CodeRecProject/CodeRecDataProcess/src/main/resources/config/class_name_map.config";

    // type_cast配置文件
    public static final String TYPE_CAST_CONFIG_FILE_PATH = "/Users/coldilock/Documents/Code/Github/CodeRecProject/CodeRecDataProcess/src/main/resources/config/type_cast.config";

    // 进行预测的python代码路径
    public static final String GGNN_CLIENT_PYTHON_FILE_PATH = "/ggnn/Client.py";

    public static final String URL = "http://localhost:9000/?properties={%22annotators%22%3A%22tokenize%2Cssplit%2Clemma%22%2C%22outputFormat%22%3A%22json%22}";

    // 调试用的路径
    public static final String FINE_TEST_GRAPH_DATA_CONSTRUCT_PATH = "/Users/wangxin/Workspace/04-DataSpace/CodeRecommendation/GraphDataConstruct/graph/graph_";

    // 调试用的路径
    public static final String PRE_TEST_OR_TRAIN_GRAPH_DATA_CONSTRUCT_PATH = "/Users/wangxin/Workspace/01-CodeSpace/02-TempDir/GraphDataConstruct/graph/graph_";




}
