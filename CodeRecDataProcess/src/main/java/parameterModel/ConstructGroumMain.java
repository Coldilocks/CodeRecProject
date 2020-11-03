package parameterModel;

import codeAnalysis.textTokenProcess.GloveVocab;
import codeAnalysis.textTokenProcess.StopWordsVocab;
import config.DataConfig;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by chenchi on 18/1/30.
 * todo: replcace constant file path
 */
public class ConstructGroumMain {
    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        String globalPath = System.getProperty("user.dir");
        String filePaths = "/home/x/mydisk/filePath.txt";
        String outputPath = "/home/x/mydisk/Groum/";

        ConstructGroum test = new ConstructGroum();

        GloveVocab gloveVocab = new GloveVocab();
        List<String> gloveVocabList = gloveVocab.getGloveList();
        StopWordsVocab stopWords = new StopWordsVocab();
        List<String> stopWordsList = stopWords.getStopWordsList();

        Scanner scanner = new Scanner(new FileReader(filePaths));
        long cnt = 0;
        long cntpos = 1;
        long endpos = 472453;

        // read jdk class name
        List<String> jdkList = new ArrayList<>();
        try {
            File fileClassNameMap = new File(DataConfig.JDKCLASS_VOCAB_FILE_PATH);
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
        ObjectOutputStream groumWriter = new ObjectOutputStream(new FileOutputStream(outputPath + "groum.txt"));
        FileWriter traceWriter = new FileWriter(outputPath + "trace.txt", true);
        while (true) {
            if (groumWriter != null && traceWriter != null) {
                break;
            } else {
                groumWriter = new ObjectOutputStream(new FileOutputStream(outputPath + "groum.txt"));
                traceWriter = new FileWriter(outputPath + "trace.txt", true);
            }
        }
        // Construct data based on the files
        while (scanner.hasNextLine()) {
            cnt++;
            String filePath = scanner.nextLine();
            filePath = filePath.replace("/Volumes/WowZone","/Volumes/Sea");
            //filePath = filePath.replaceFirst("/Volumes/zzz","/Volumes/Sea");
            if (cnt < cntpos) {
                continue;
            }
            if (cnt >= endpos) {
                break;
            }
            System.out.println(cnt + ".: " + filePath);

            // Construct Training data
            try {
                File file = new File(filePath);
                if (file.length() / 1024 <= 200) {
                    test.constructGroum((int) cnt, filePath, true, jdkList,
                            groumWriter,
                            traceWriter,
                            false, globalPath,
                            gloveVocabList, stopWordsList);
                }
            } catch (Exception e) {
//                    e.printStackTrace();
            }
        }
        groumWriter.writeObject(null);
        if (groumWriter != null) {
            groumWriter.close();
            traceWriter.close();
        }
        FileWriter out = new FileWriter("/Users/lingxiaoxia/Desktop/Groum/lines.txt", true);
        out.write(test.linesCount + "\r\n");
        out.write(test.test + "\r\n");
        out.close();
        scanner.close();
        System.out.println("---");
        System.out.println("cnt:" + cnt);
        System.out.println("cntpos:" + cntpos);
        System.out.println("endpos:" + endpos);
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
