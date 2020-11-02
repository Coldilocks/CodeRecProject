package parameterModel;

/**
 * Created by zhanghr on 2018/1/24 20:07.
 */
public interface GroumCounter {

    // the number of method where two groums co-appear
    int getCoAppearanceCount(Groum g1, Groum g2);

    // the number of method with groum
    int getGroumCount(Groum g);

    // the number of all graphs, one graph is responding for a method
    int getTotalGraphCount();
}