package codeAnalysis.textTokenProcess;

import edu.stanford.nlp.simple.Sentence;

public class Stemming {

    /**
     * 词形还原
     * @param word
     * @return
     */
    public static String getLemma(String word) {
        Sentence sentence = new Sentence(word);
        if(sentence.lemmas().size() > 0){
            return sentence.lemmas().get(0);
        } else {
            return word;
        }
    }

}
