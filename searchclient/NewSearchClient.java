package searchclient;

import searchclient.cbs.algriothem.CBSRunner;
import searchclient.cbs.model.LowLevelState;

import java.io.BufferedReader;
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

        // Parse the level.
        BufferedReader serverMessages = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.US_ASCII));
//        State initialState = SearchClient.parseLevel(serverMessages);
        LowLevelState lowLevelState = LowLevelState.parseLevel(serverMessages);

        // Search for a plan.
        Action[][] plan = null;
        try {
            //        System.err.format("Starting %s.\n");
            CBSRunner cbsRunner = new CBSRunner();
            plan = cbsRunner.findSolution(lowLevelState);
        } catch (OutOfMemoryError ex) {
            System.err.println("Maximum memory usage exceeded.");
        }

        // Print plan to server.
        if (plan == null) {
            System.err.println("Unable to solve level.");
            System.exit(0);
        } else {//err 会打印在控制台里
            System.err.format("Found solution of length %,d.\n", plan.length);

            for (Action[] jointAction : plan) {
                System.err.print(jointAction[0].name + "@" + jointAction[0].name);
                System.out.print(jointAction[0].name + "@" + jointAction[0].name);
                for (int action = 1; action < jointAction.length; ++action) {
                    System.out.print("|");
                    System.out.print(jointAction[action].name);

                    System.err.print("|");
                    System.err.println(jointAction[action].name);
                }
                System.out.println();
                System.err.println();
                // We must read the server's response to not fill up the stdin buffer and block the server.
                serverMessages.readLine();
            }
        }
    }
}
