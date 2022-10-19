/*
Name: Alex James Waddell
Student Number: C3330987
Description: the A2 class is a driver program for the scanner class prompting it to grab and print a new token
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class A3 {

    public static void main(String[] args) {

        Scanner sc;
        Parser parser;
        BufferedReader input;
        ArrayList<String> listing = new ArrayList<>();
        ArrayList<Token> tokenStream = new ArrayList<>();
        ArrayList<Character> program = new ArrayList<>();
        Token tempToken;

        try {

            // creates a scanner to real in a file line by line
            File file = new File(args[0]);
            FileReader fr = new FileReader(file);
            input = new BufferedReader(fr);
            String nextLine;

            // converts the line into a char array, and adds \n at the end of every line to mark where it is
            while ((nextLine = input.readLine()) != null) {
                listing.add(nextLine);
                for (int i = 0; i < nextLine.length(); i++) {
                    program.add(nextLine.charAt(i));
                }
                program.add('\n');
            }

            sc = new Scanner(program);

            // main while loop that grabs and prints tokens
            while (sc.eof()) {
                tempToken = sc.gettoken();


                if (tempToken!= null) {
                    tokenStream.add(tempToken);
                }
                // prints the end of file
                if (!sc.eof()) {
                    tokenStream.add(sc.endFile());
                    if (sc.errorsDetected()) {
                        // make an error listing here
                        sc.printErrors();
                    } else {
                        parser = new Parser(tokenStream);
                        parser.parse();
                        parser.traverseTree();
                        parser.errors(listing);


                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

