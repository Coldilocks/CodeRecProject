package codeAnalysis.textTokenProcess;

import config.DataConfig;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GloveVocab {
    private List<String> gloveList = new ArrayList<>();

    public List<String> getGloveList() {
        return gloveList;
    }

    public void setGloveList(List<String> gloveList) {
        this.gloveList = gloveList;
    }

    public GloveVocab(){
        try{
            String path = DataConfig.GLOVE_VOCAB_PATH;
            Scanner scanner = new Scanner(new FileReader(path));
            while (scanner.hasNextLine()) {
                String stopWord = scanner.nextLine();
                gloveList.add(stopWord);
            }
            scanner.close();
        }catch(Exception e){
            e.printStackTrace();
        }catch(Error e){
            e.printStackTrace();
        }
    }

}
