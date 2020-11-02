package utils;

public class Util {

    public static int getCharacterCnt(String s, char c, int i, int e) {
        int cnt = 0;
        if(i >= 0 && i < s.length() && i < e && e < s.length()) {
            for (; i < e; i++) {
                if (s.charAt(i) == c) {
                    cnt++;
                }
            }
        }
        return cnt;
    }

    public static int getCharacterCntSkipParenthesis(String s, char c, int i, int e) {
        int cnt = 0;
        int singleP = 0;
        int doubleP = 0;
        char lastChar = 'l';
        if(i >= 0 && i < s.length() && i < e && e < s.length()) {
            for (; i < e; i++) {
                if(lastChar != '\\') {
                    if (singleP == 0 && doubleP == 0) {
                        if (s.charAt(i) == c) {
                            cnt++;
                        } else if (s.charAt(i) == '\'') {
                            singleP++;
                        } else if (s.charAt(i) == '\"') {
                            doubleP++;
                        }
                    } else if (singleP > 0 && doubleP == 0) {
                        if (s.charAt(i) == '\'') {
                            singleP--;
                        }
                    } else if (singleP == 0 && doubleP > 0) {
                        if (s.charAt(i) == '\"') {
                            doubleP--;
                        }
                    }
                }
                lastChar = s.charAt(i);
            }
        }
        return cnt;
    }

    /**
     * 返回配对的下一个括号所在位置
     * */
    public static int findNextRightParenthesis(String line, int fromindex) {
        if(fromindex < 0){
            return -1;
        }
        int numOfDoubleQuotationMarks = 0;
        int numOfSingleOuatationMarks = 0;
        int numOfPreLeftParenthesis = 0;
        char lastChar = 'l';
        for (int j = fromindex; j < line.length(); j++) {
            char c = line.charAt(j);
            if(lastChar != '\\') {
                if (numOfDoubleQuotationMarks == 0 && numOfSingleOuatationMarks == 0) {
                    if (c == '(') {// 遇到左括号
                        numOfPreLeftParenthesis += 1;
                    } else if (c == ')') {
                        numOfPreLeftParenthesis--;
                        if (numOfPreLeftParenthesis == 0) {
                            return j;
                        }
                    } else if (c == '\'') {
                        numOfSingleOuatationMarks += 1;
                    } else if (c == '\"') {
                        numOfDoubleQuotationMarks += 1;
                    }
                } else if (numOfDoubleQuotationMarks > 0 && numOfSingleOuatationMarks == 0) {
                    if (c == '\"') {
                        numOfDoubleQuotationMarks -= 1;
                    }
                } else if (numOfDoubleQuotationMarks == 0 && numOfSingleOuatationMarks > 0) {
                    if (c == '\'') {
                        numOfSingleOuatationMarks -= 1;
                    }
                }
            }
            lastChar = c;
        }
        return -1;
    }
}
