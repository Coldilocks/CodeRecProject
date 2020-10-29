package codeAnalysis.textTokenProcess;

import com.alibaba.fastjson.JSONObject;
import config.DataConfig;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Stemming {

    /**
     * 调用python代码
     * todo: 改成调用NLTK服务的形式
     * @param word
     * @return
     */
    public String getLemma(String word) {
        String lemma = null;
        try {
            String[] cmd = new String[] {"curl","--data",word,
                    DataConfig.URL,
                    "-o","-"};
            Runtime rt = Runtime.getRuntime();
            Process p = rt.exec(cmd);
            BufferedInputStream in = new BufferedInputStream(p.getInputStream());
            BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
            String s;
            String result = "";
            while ((s = inBr.readLine()) != null) {
                result += s;
            }
            inBr.close();
            JSONObject json = new JSONObject();
            JSONObject json1 = (JSONObject)json.getJSONArray("sentences").get(0);
            JSONObject json2 = (JSONObject) json1.getJSONArray("tokens").get(0);
            lemma = json2.getString("lemma");
            if(lemma != null){
                return lemma;
            }else{
                return word;
            }
        } catch (Exception ioe) {
            return word;
        }catch(Error e){
            return word;
        }
    }

}
