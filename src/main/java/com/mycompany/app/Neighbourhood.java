package com.mycompany.app;

import com.esri.arcgisruntime.geometry.Polygon;
import org.geojson.MultiPolygon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
public class Neighbourhood {
    private String name;
    private List<Coordinate> coordinates;
    private List<Tree> trees;
    private Polygon mp;
    public Neighbourhood(String name, List<Coordinate> coordinates, List<Tree> trees, Polygon mp) {
        this.name = name;
        this.coordinates = coordinates;
        this.trees = trees;
        this.mp = mp;
    }
    public void setTrees(List<Tree> trees) {
        this.trees = new ArrayList<>(trees);
    }
    public List<Coordinate> getCoordinates() {
        return new ArrayList<>(coordinates);
    }
    public String getName() {
        return name;
    }
    public List<Tree> getTrees() {
        return new ArrayList<>(trees);
    }
    public Polygon getMp() {
        return mp;
    }
    @Override
    public String toString() {
        return "Neighbourhood{" + name + " " + trees.toString() + "}";
    }
    public HashMap<String, Integer> treesAndNumbers(){
        HashMap<String, Integer> treesNumber = new HashMap<>();
        for (Tree tree : getTrees()){
            if (treesNumber.get(tree.getName()) != null){
                treesNumber.put(tree.getName(), treesNumber.get(tree.getName())+ 1);
            }
            else treesNumber.putIfAbsent(tree.getName(), 1);
        }
        return treesNumber;
    }
    public Coordinate getCenter(){
        double maxLong = coordinates.get(0).getLongitude();
        double maxLat = coordinates.get(0).getLatitude();
        double minLong = coordinates.get(0).getLongitude();
        double minLat= coordinates.get(0).getLatitude();
        for (Coordinate coord : coordinates){
            if (coord.getLatitude() > maxLat){
                maxLat = coord.getLatitude();
            }
            if (coord.getLatitude() < minLat){
                minLat = coord.getLatitude();
            }
            if (coord.getLongitude() > maxLong){
                maxLong = coord.getLongitude();
            }
            if (coord.getLongitude() < minLong){
                minLong = coord.getLongitude();
            }
        }
        double longitude = (minLong + maxLong)/2.0;
        double latitude = (minLat + maxLat)/2.0;
        return new Coordinate(longitude, latitude);
    }
}