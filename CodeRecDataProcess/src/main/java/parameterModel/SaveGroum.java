package parameterModel;

import java.io.FileWriter;
import java.io.ObjectOutputStream;

/**
 * Created by chenchi on 18/1/30.
 */
public class SaveGroum {
    public void save(Groum groum, String trace, ObjectOutputStream groumWriter, FileWriter traceWriter){
        try {
            groumWriter.writeObject(groum);
            traceWriter.write(trace + "\r\n");
            traceWriter.flush();
        }catch(Exception e){
            e.printStackTrace();
        }catch(Error e){

        }
    }

}
