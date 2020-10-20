package codeAnalysis.textTokenProcess;

import java.util.ArrayList;
import java.util.List;

public class StopWordsVocab {

    private List<String> stopWordsList = new ArrayList<>();

    public List<String> getStopWordsList() {
        return stopWordsList;
    }

    public void setStopWordsList(List<String> stopWordsList) {
        this.stopWordsList = stopWordsList;
    }

}
