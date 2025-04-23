package com.mycompany.app;

import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ReadTreeCSV {
    public static List<Tree> readCSV(String fileName) throws IOException {
        BufferedReader reader = Files.newBufferedReader(Paths.get(fileName));
        reader.readLine();
        String line;
        List<Tree> trees = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            String[] values = line.split(",");
            String hood = values[1];
            String name = "";
            double longitude;
            double latitude;
            if (values.length == 21) {
                 name = values[14];
                longitude = Double.parseDouble(values[17]);
                latitude = Double.parseDouble(values[16]);
            }
            else {
                name = values[13];
                longitude = Double.parseDouble(values[16]);
                latitude = Double.parseDouble(values[15]);
            }
            Coordinate coOrd = new Coordinate(longitude, latitude);
            Tree tree = new Tree(name, coOrd, hood);
            trees.add(tree);
        }
        return trees;
    }
}
