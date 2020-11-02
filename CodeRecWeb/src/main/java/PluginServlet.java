import codeAnalysis.textTokenProcess.GloveVocab;

//import variableprocessing.GloveVocab;
import codeAnalysis.textTokenProcess.GloveVocab;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import net.sf.json.JSONArray;
import static org.apache.commons.lang.StringEscapeUtils.unescapeHtml;

@WebServlet(name = "PluginServlet")
public class PluginServlet extends HttpServlet {

    String[] strs; // generate statements
    private Map<String, Map<String, List>> parameterList = new HashMap<>();
    private List<List<String>> importInfoList = new ArrayList<>();
    private List<String>  tipList = new ArrayList<>();

//    public static Map<String,Record> map;
//    public static Map<String,Boolean> castMap = new HashMap<>();
    public List<String> jdkList = new ArrayList<>();
    public List<String> gloveVocabList = new ArrayList<>();
    public List<String> stopWordsList = new ArrayList<>();

    @Override
    public void init(){
        try {
//            LoadSource.initMap();
            LoadSource.initCastMap();
            LoadSource.initTipMap();
//            ObjectInputStream in = new ObjectInputStream(new FileInputStream("/home/x/mydisk/groumMapNew7_1.txt"));
//            Object object = null;
//            object = in.readObject();
//            map = (Map<String,Record>)object;
//            in.close();
            //System.out.println("start cast read");
            //File fileTypeCast = new File( "/Users/lingxiaoxia/IdeaProjects/CodeRecommendation/Extractor/src/main/java/codetree/configs/type_cast.config");
//            File fileTypeCast = new File( "/home/x/mydisk/IdeaProjects/CodeRecommendation/Extractor/src/main/java/codetree/configs/type_cast.config");
//            FileInputStream fileInputStream = new FileInputStream(fileTypeCast);
//            Scanner scanner = new Scanner(fileInputStream);
//            while (scanner.hasNextLine()) {
//                castMap.put(scanner.nextLine(), true);
//            }
//            scanner.close();
//            fileInputStream.close();
            //System.out.println("finish cast read");
            //System.out.println("start jdkList");
//            String globalPath = "/Users/lingxiaoxia/IdeaProjects/CodeRecommendation";
            String globalPath = "/home/x/mydisk/IdeaProjects/CodeRecommendation";
            //File fileClassNameMap = new File(globalPath + "/Extractor/src/main/java/constructdata/configs/JDKCLASS.txt");
            //String globalPath = "/Users/lingxiaoxia/IdeaProjects/CodeRecommendation";
            File fileClassNameMap = new File(globalPath + "/Extractor/src/main/java/constructdata/configs/AndroidClass.txt");
//            String globalPath = "/mydisk/fudan_se_dl/code_recommendation";
//            File fileClassNameMap = new File(globalPath + "/JDKCLASS.txt");

            FileInputStream fileInputStream2 = new FileInputStream(fileClassNameMap);
            Scanner scanner2 = new Scanner(fileInputStream2);
            while (scanner2.hasNextLine()) {
                String line = scanner2.nextLine();
                jdkList.add(line);
            }
            scanner2.close();
            fileInputStream2.close();
            //System.out.println("finish jdkList");
            //System.out.println("start gloveVocabList");
            GloveVocab gloveVocab = new GloveVocab();
            gloveVocabList = gloveVocab.getGloveList();
            //System.out.println("finish gloveVocabList");
            stopWordsList = new ArrayList<>();
            //System.out.println("finish stopWordsList");
        }catch(Exception e){

        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String rawcode = "";
        String space = "";
        PrintWriter out = response.getWriter();
        try
        {

            BufferedReader br = new BufferedReader(new InputStreamReader((ServletInputStream)request.getInputStream(),"utf-8"));
            StringBuffer sb = new StringBuffer("");
            String temp;
            String lineSeparator = str2HexStr(System.getProperty("line.separator"));
            while((temp = br.readLine())!=null)
            {
                sb.append(temp + lineSeparator);
            }

            br.close();
            rawcode = sb.toString();
            rawcode = hexStr2Str(rawcode);
            rawcode = rawcode.replaceAll("woshidanyinhaochenchi","'");
            rawcode = rawcode.replaceAll("dotchenchi",".");
            rawcode = rawcode.replaceAll("\t \" \t","\"" );
            /*
            String ip = getIp(request);
            //String ipFile = "/home/x/mydisk/TestCaseRecord/" + ip + ".txt";
            String ipFile = "/mydisk/fudan_se_dl/code_recommendation/TestCaseRecord/" + ip + ".txt";
            FileWriter fw = new FileWriter(ipFile,true);
            fw.write("context: " + "\r\n" + rawcode);
            fw.close();
            */
            //writeFile(acceptjson);
        GGNNRawCodeHandler rawCodeHandler = new GGNNRawCodeHandler(jdkList,gloveVocabList,stopWordsList,space);
        List<String> completecode = rawCodeHandler.handleRawCode(rawcode);//handle rawcode
        importInfoList = rawCodeHandler.getImportInfoList();
        tipList = rawCodeHandler.getTipList();
        if(completecode == null){
            String s = "[[{\"index\":0,\"statement\":\"Sorry, CodeWisdom-aiAssistant cannot parse this piece of code.\",\"import\":\"[]\",\"tip\":\"No API Description\"}]]";
            s = str2HexStr(s);
            out.write(s);
        }
        else if(completecode.size() == 0){
            String s = "[[{\"index\":0,\"statement\":\"Sorry, there is no recommendation.\",\"import\":\"[]\",\"tip\":\"No API Description\"}]]";
            s = str2HexStr(s);
            out.write(s);
        }
        else {
            strs = new String[completecode.size()];
            for (int i = 0; i < completecode.size(); i++) {
                strs[i] = completecode.get(i).replace("\n", "");
            }

            StringBuilder statements = new StringBuilder("");
            for (int i = 0; i < strs.length; i ++) {
                statements.append("{\"index\":").append(i).append(",\"statement\":\"").append(strs[i]);
                statements.append("\",\"import\":\"").append(importInfoList.get(i).toString());
                statements.append("\",\"tip\":\"").append(tipList.get(i));
                if(i != 9){
                    statements.append("\"},");
                }else{
                    statements.append("\"}");
                }
            }
            String s = "[[" + statements.toString() + "]]";
            s = str2HexStr(s);
            out.write(s);
        }
        out.flush();
        out.close();
        }catch (Exception e){
            //e.printStackTrace();
        }catch(Error e){
            //e.printStackTrace();
        }
    }

    public static String getIp(HttpServletRequest request)
    {
        String ipAddress = request.getHeader("x-forwarded-for");
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0|| "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0|| "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
            if (ipAddress.equals("127.0.0.1")) {
                // 根据网卡取本机配置的IP
                InetAddress inet = null;
                try {
                    inet = InetAddress.getLocalHost();
                    ipAddress = inet.getHostAddress();
                } catch (UnknownHostException e) {
                    //System.out.println("出现异常:"+e.toString());
                }
            }
        }
        if (ipAddress != null && ipAddress.length() > 15) {
            if (ipAddress.indexOf(",") > 0) {
                ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
            }
        }
        if("0:0:0:0:0:0:0:1".equals(ipAddress)){
            ipAddress="127.0.0.1";
        }
        return ipAddress;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    public  String str2HexStr(String str) {
        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;
        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
        }
        return sb.toString();
    }

    public  String hexStr2Str(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;
        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }
}
