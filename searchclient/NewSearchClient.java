package searchclient;

import searchclient.cbs.algriothem.CBSRunner;
import searchclient.cbs.model.Environment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class NewSearchClient {

    public static void main(String[] args) throws IOException {
        // Use stderr to print to the console.
        System.err.println("SearchClient initializing. I am sending this using the error output stream.");

        // Send client name to server.
        System.out.println("SearchClient");

        // We can also print comments to stdout by prefixing with a #.
        System.out.println("#This is a comment.");

//        String levelFile = "/Users/blackbear/Desktop/dtu/semester1/course/Mas/searchclient/cbslevel/MAPF03C.lvl";
//        String levelFile = "/Users/blackbear/Desktop/dtu/semester1/course/Mas/searchclient/levels/SAD1.lvl";
        String levelFile = "/Users/blackbear/Desktop/dtu/semester1/course/Mas/searchclient/cbslevel/MAsimple1-Design-3.lvl";
        // Parse the level.
        BufferedReader serverMessages = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.US_ASCII));
//        BufferedReader serverMessages = new BufferedReader(new FileReader(levelFile));
        Environment environment = Environment.parseLevel(serverMessages);

        // Search for a plan.
        Action[][] plan = null;
        try {
            //        System.err.format("Starting %s.\n");
            CBSRunner cbsRunner = new CBSRunner();
            plan = cbsRunner.findSolution(environment);
        } catch (OutOfMemoryError ex) {
            System.err.println("Maximum memory usage exceeded.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Print plan to server.
        if (plan == null) {
            System.err.println("Unable to solve level.");
            System.exit(0);
        } else {
            System.err.format("Found solution of length %,d.\n", plan.length);
            for (int i = 0; i < plan.length; i++) {
                System.err.format("Step Num %d: ", i);
                for (int j = 0; j < plan[i].length; j++) {
                    System.err.format("%s|", plan[i][j].name);
                }
                System.err.println();
            }

            for (Action[] jointAction : plan) {
                System.out.print(jointAction[0].name + "@" + jointAction[0].name);
                for (int action = 1; action < jointAction.length; ++action) {
                    System.out.print("|");
                    System.out.print(jointAction[action].name);
                }
                System.out.println();
                // We must read the server's response to not fill up the stdin buffer and block the server.
                serverMessages.readLine();
            }
        }
    }
}
