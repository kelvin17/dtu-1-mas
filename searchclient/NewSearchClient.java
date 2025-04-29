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

        String levelFile = "/Users/blackbear/Desktop/dtu/semester1/course/Mas/searchclient/searchclient_java/cbslevel/MAthomasAppartment_redblue.lvl";
//        BufferedReader serverMessages = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.US_ASCII));
        BufferedReader serverMessages = new BufferedReader(new FileReader(levelFile));
        Environment environment = Environment.parseLevel(serverMessages);

        int superB = -1;//watch dog for Max
        if (args.length > 0) {
            superB = Integer.parseInt(args[0]);
            System.err.printf("Parameter of B provided = %d. MA-CBS.\n", superB);
        } else {
            System.err.println("No parameter of B provided. Defaulting to Basic CBS.");
        }

        // Search for a plan.
        Action[][] plan = null;
        CBSRunner cbsRunner = new CBSRunner();
        boolean timeout = false;
        try {
            plan = cbsRunner.findSolution(environment, superB);
        } catch (OutOfMemoryError ex) {
            System.err.println("Maximum memory usage exceeded.");
        } catch (TimeoutException e) {
            timeout = true;
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }

        // Print plan to server.
        if (plan == null) {
            System.err.println("Unable to solve level.");
            System.err.printf("Aborting by time out: %s\n", timeout ? "yes" : "no");
            System.exit(0);
        } else {
//            System.err.format("Found solution of length %,d.\n", plan.length);
//            for (int i = 0; i < plan.length; i++) {
//                System.err.format("Step Num %d: ", i);
//                for (int j = 0; j < plan[i].length; j++) {
//                    System.err.format("%s|", plan[i][j].name);
//                }
//                System.err.println();
//            }

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
