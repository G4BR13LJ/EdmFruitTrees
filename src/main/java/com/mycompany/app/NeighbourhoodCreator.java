package com.mycompany.app;

import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.geojson.Feature;
import org.geojson.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NeighbourhoodCreator {
    private static final List<Tree> trees;
    static {
        try {
            trees = ReadTreeCSV.readCSV("Trees (1).csv");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public NeighbourhoodCreator() throws IOException {
    }

    public static List<Neighbourhood> mapToObject(String file) throws Exception {
        ObjectMapper om = new ObjectMapper();
        Map<String, Object> resultMap = om.readValue(new File(file), new TypeReference<Map<String, Object>>() {});
        List<Feature> features = om.convertValue(resultMap.get("features"), new TypeReference<List<Feature>>() {});
        List<Neighbourhood> hoods = new ArrayList<>();
        for(Feature f : features) {
            Map<String, Object> properties = f.getProperties();
            String hoodName = (String) properties.get("name");
            List<Coordinate> coordinates = new ArrayList<>();
            List<Tree> trees = treeMatcher(hoodName);
            GeoJsonObject geometry = f.getGeometry();
            Polygon polygon = null; // only for keeping variable in scope
            PointCollection polygonPoints = new PointCollection(SpatialReferences.getWgs84());
            if (geometry instanceof MultiPolygon){
                MultiPolygon mp = (MultiPolygon) geometry;
                for (int i = 0 ; i < mp.getCoordinates().get(0).get(0).size() ; i++){
                    Coordinate coordinate = new Coordinate(
                            mp.getCoordinates().get(0).get(0).get(i).getLongitude(),
                            mp.getCoordinates().get(0).get(0).get(i).getLatitude());
                    polygonPoints.add(mp.getCoordinates().get(0).get(0).get(i).getLongitude(),
                            mp.getCoordinates().get(0).get(0).get(i).getLatitude());
                    coordinates.add(coordinate);
                }
                polygon = new Polygon(polygonPoints);
            }
            Neighbourhood hood = new Neighbourhood(hoodName, coordinates, trees, polygon);
            hoods.add(hood);
        }
        return hoods;
    }
    private static List<Tree> treeMatcher(String nameOfNeighbourhood){
        return trees.stream()
                .filter(t -> t.getHood().equals(nameOfNeighbourhood))
                .collect(Collectors.toList());
    }
}
