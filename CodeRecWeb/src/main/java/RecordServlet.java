//import javax.servlet.ServletException;
//import javax.servlet.ServletInputStream;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.*;
//import java.net.InetAddress;
//import java.net.UnknownHostException;
//
///**
// * Created by zero on 2019/5/5.
// */
//public class RecordServlet extends HttpServlet {
//
//    public static String getIp(HttpServletRequest request)
//    {
//        String ipAddress = request.getHeader("x-forwarded-for");
//        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
//            ipAddress = request.getHeader("Proxy-Client-IP");
//        }
//        if (ipAddress == null || ipAddress.length() == 0|| "unknown".equalsIgnoreCase(ipAddress)) {
//            ipAddress = request.getHeader("WL-Proxy-Client-IP");
//        }
//        if (ipAddress == null || ipAddress.length() == 0|| "unknown".equalsIgnoreCase(ipAddress)) {
//            ipAddress = request.getRemoteAddr();
//            if (ipAddress.equals("127.0.0.1")) {
//                // 根据网卡取本机配置的IP
//                InetAddress inet = null;
//                try {
//                    inet = InetAddress.getLocalHost();
//                    ipAddress = inet.getHostAddress();
//                } catch (UnknownHostException e) {
//                    //System.out.println("出现异常:"+e.toString());
//                }
//            }
//        }
//        if (ipAddress != null && ipAddress.length() > 15) {
//            if (ipAddress.indexOf(",") > 0) {
//                ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
//            }
//        }
//        if("0:0:0:0:0:0:0:1".equals(ipAddress)){
//            ipAddress="127.0.0.1";
//        }
//        return ipAddress;
//    }
//
//    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        try
//        {
//            BufferedReader br = new BufferedReader(new InputStreamReader((ServletInputStream)request.getInputStream(),"utf-8"));
//            StringBuffer sb = new StringBuffer("");
//            String temp;
//            String lineSeparator = str2HexStr(System.getProperty("line.separator"));
//            while((temp = br.readLine())!=null)
//            {
//                sb.append(temp + lineSeparator);
//            }
//            br.close();
//            String acceptjson = sb.toString();
//            acceptjson = hexStr2Str(acceptjson);
//            String ip = getIp(request);
//            String ipFile = "/mydisk/fudan_se_dl/code_recommendation/" + ip + ".txt";
//            //String ipFile = "/home/x/mydisk/TestCaseRecord/" + ip + ".txt";
//            FileWriter fw = new FileWriter(ipFile,true);
//            acceptjson = acceptjson.trim();
//            fw.write(acceptjson + "\r\n");
//            fw.close();
//        }catch (Exception e){
//
//        }catch(Error e){
//
//        }
//    }
//
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//
//    }
//
//    public  String str2HexStr(String str) {
//        char[] chars = "0123456789ABCDEF".toCharArray();
//        StringBuilder sb = new StringBuilder("");
//        byte[] bs = str.getBytes();
//        int bit;
//        for (int i = 0; i < bs.length; i++) {
//            bit = (bs[i] & 0x0f0) >> 4;
//            sb.append(chars[bit]);
//            bit = bs[i] & 0x0f;
//            sb.append(chars[bit]);
//        }
//        return sb.toString();
//    }
//
//    public  String hexStr2Str(String hexStr) {
//        String str = "0123456789ABCDEF";
//        char[] hexs = hexStr.toCharArray();
//        byte[] bytes = new byte[hexStr.length() / 2];
//        int n;
//        for (int i = 0; i < bytes.length; i++) {
//            n = str.indexOf(hexs[2 * i]) * 16;
//            n += str.indexOf(hexs[2 * i + 1]);
//            bytes[i] = (byte) (n & 0xff);
//        }
//        return new String(bytes);
//    }
//}
