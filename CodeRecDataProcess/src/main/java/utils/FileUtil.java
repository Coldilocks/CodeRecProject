package utils;

import java.io.*;
import java.util.Collection;

public class FileUtil {


    public static void saveStringInFile(String str, FileWriter writer){
        try{
            writer.write(str + "\r\n");
            writer.flush();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static String getContent(String fileName)
    {
        File file = new File(fileName);
        BufferedReader reader = null;
        StringBuffer sb = new StringBuffer();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                sb.append(tempString+"\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return sb.toString();
    }

    /**
     * 转换文件编码
     * @param srcFileName
     * @param destFileName
     * @param srcEncoding
     * @param destEncoding
     * @throws IOException
     */
    public static void transformFileEncoding(String srcFileName, String destFileName, String srcEncoding, String destEncoding) throws IOException {
        String line_separator = System.getProperty("line.separator");
        FileInputStream fis = new FileInputStream(srcFileName);
        StringBuffer content = new StringBuffer();
        DataInputStream in = new DataInputStream(fis);
        BufferedReader d = new BufferedReader(new InputStreamReader(in, srcEncoding));
        String line = null;
        while ((line = d.readLine()) != null)
            content.append(line + line_separator);
        d.close();
        in.close();
        fis.close();

        Writer ow = new OutputStreamWriter(new FileOutputStream(destFileName), destEncoding);
        ow.write(content.toString());
        ow.close();
    }


    public static void delFSFiles(File destPath) {
        if (destPath.isFile()) {
            destPath.delete();
        }else {
            File[] children = destPath.listFiles();
            if (children!= null && children.length > 0) {
                for (File file : children) {
                    delFSFiles(file);
                }
            }
            destPath.delete();
        }
    }

    public static void travelFSFiles(File destPath, Collection<String> entries) {
        if (destPath.isFile()) {
            entries.add(destPath.getAbsolutePath());
        }else if (destPath.isDirectory()) {
            File[] children = destPath.listFiles();
            for (File file : children) {
                travelFSFiles(file, entries);
            }
        }
    }

    public static  void travelFSDirs(File destPath, Collection<String> entries) {
        if (destPath.isDirectory()) {
            entries.add(destPath.getAbsolutePath());
            File[] children = destPath.listFiles();
            for (File file : children) {
                travelFSFiles(file, entries);
            }
        }
    }

    public static void createFileParent(File file) {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
    }

    public static void saveFile(String fileName, String content){
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileName)),true);
            pw.println(content);
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
