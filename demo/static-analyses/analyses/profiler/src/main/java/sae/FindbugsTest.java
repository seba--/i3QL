package sae;

import edu.umd.cs.findbugs.DetectorFactory;
import edu.umd.cs.findbugs.FindBugs2;
import edu.umd.cs.findbugs.Plugin;
import edu.umd.cs.findbugs.Project;
import edu.umd.cs.findbugs.config.UserPreferences;

import java.io.IOException;
import java.util.Iterator;

/**
 * @author Mirko Köhler
 */
public class FindbugsTest {

    public static void main(String[] args) {
        Project p = new Project();
        p.addFile("C:\\Users\\Mirko\\Documents\\GitHub\\sae\\test-data\\src\\main\\resources\\scala-compiler-2.8.1.jar");

        FindBugs2 bugfinder = new FindBugs2();


        UserPreferences pref = UserPreferences.createDefaultUserPreferences();
        pref.enableAllDetectors(false);

        Iterator<Plugin> it = Plugin.getAllPlugins().iterator();
        while (it.hasNext()) {
            Plugin	plugin = it.next();
            Iterator<DetectorFactory> it2 = plugin.getDetectorFactories().iterator();
            while (it2.hasNext()) {
                DetectorFactory factory = it2.next();
                if (factory.getShortName().equals("FindPuzzlers"))
                    pref.enableDetector(factory, true);
            }
        }

        bugfinder.setUserPreferences(pref);
        bugfinder.setProject(p);



        try {
            bugfinder.execute();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
