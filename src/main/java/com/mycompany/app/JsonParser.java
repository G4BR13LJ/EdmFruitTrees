package com.mycompany.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class JsonParser {
    private static ObjectMapper objMapper = getDefault();

    private static ObjectMapper getDefault(){
        ObjectMapper defObj = new ObjectMapper();
        return defObj;
    }

    public static JsonNode parse(String src) throws IOException{
        return objMapper.readTree(src);
    }

    public static void tester(){
        try {
            Neighbourhood obj = objMapper.readValue(
                    new File("c:\\Users\\Mauro\\Desktop\\example_arcgis\\java-maven-starter-project-main\\Test_File.geojson"),
                    Neighbourhood.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
