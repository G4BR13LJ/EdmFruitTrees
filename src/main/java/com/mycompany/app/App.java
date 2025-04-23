package com.mycompany.app;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geometry.*;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
//import javafx.scene.chart.LogarithmicAxis;

import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;

import javax.xml.crypto.Data;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Stream;

public class App extends Application {

    private MapView mapView;
    private final GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
    private static List<Neighbourhood> hoods = new ArrayList<>();

    private List<Graphic> prevPoints = new ArrayList<>();
    private final TextField neighbourhoodField = new TextField();
    private ComboBox<String> treeTypesBox;

    public static void main(String[] args) {
        try {
            hoods = NeighbourhoodCreator.mapToObject(
                    "City of Edmonton-Neighbourhoods.geojson");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Application.launch(args);
    }
    @Override
    public void start(Stage stage) {
        //--------------------------Initial setup-----------------------------------------------------------------------
        // set the title and size of the stage and show it
        String yourApiKey =
                "AAPKea3e4f073cca4ac8935e354c6206d005JxEUCbiMFwvtMrxlniCEojH5fihHAHpfX81BCwwbR6uiTxmAHMQiFcmkPNVXbVtn";
        ArcGISRuntimeEnvironment.setApiKey(yourApiKey);
        int WIDTH = 350;
        stage.setTitle("Edmonton Edible Fruit Trees");
        VBox vBox = new VBox(10); vBox.setPrefHeight(900); vBox.setPrefWidth(WIDTH);
        VBox vBox1 = new VBox();
        HBox hBoxGraph = new HBox(10); hBoxGraph.setPrefWidth(WIDTH);
        HBox hBoxLeftFields = new HBox(10); hBoxLeftFields.setPrefWidth(WIDTH); hBoxLeftFields.setPrefWidth(WIDTH);
        ObservableList<String> treeTypeList = FXCollections.observableArrayList();
        treeTypesBox = new ComboBox<>(treeTypeList); treeTypesBox.setPrefWidth(WIDTH);
        treeTypesBox.setItems(FXCollections.observableArrayList(treeHashMap().keySet()));
        treeTypesBox.setPromptText("Select Tree Type");
        Button searchBtn = new Button("Search"); searchBtn.setPrefWidth(WIDTH);
        Button findMost = new Button("Find Hood With Most"); findMost.setPrefWidth(WIDTH);
        Button resetBtn = new Button("Reset Map"); resetBtn.setPrefWidth(WIDTH);
        final Label hoodLabel = new Label("Neighbourhood to Search");
        hoodLabel.setFont(new Font("Arial", 15));
        BorderPane borderPane = new BorderPane(); StackPane stackPane = new StackPane();
        mapView = new MapView();
        Scene scene = new Scene(borderPane);
        stage.setScene(scene);
        ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_MODERN_ANTIQUE); //ARCGIS_STREETS
        mapView.setMap(map);
        mapView.setViewpoint(new Viewpoint(53.55, -113.42, 290000));
        mapView.getGraphicsOverlays().add(graphicsOverlay);
        Separator line = new Separator(); Separator line1 = new Separator();

        for (Neighbourhood hood : hoods) {
            drawHood(hood);
        }
        //--------------ON CLICK EVENT LOGIC----------------------------------------------------------------------------
        Callout callout = mapView.getCallout();
        mapView.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.isStillSincePress()) {
                treeTypesBox.setValue("");
                treeTypesBox.setPromptText("Select Tree Type");
                Point2D point = new Point2D(e.getX(), e.getY());
                Point mapPoint = mapView.screenToLocation(point);
                Point projectedPoint = (Point) GeometryEngine.project(mapPoint, SpatialReferences.getWgs84());
                String hoodName = "";
                for (Neighbourhood hood : hoods){
                    if (GeometryEngine.contains(hood.getMp(), projectedPoint)){
                        hBoxGraph.getChildren().clear();
                        hoodName = hood.getName();
                        mapView.setViewpoint(new Viewpoint(
                                hood.getCenter().getLatitude(), hood.getCenter().getLongitude(), 40000));
                        unDrawTrees(prevPoints);
                        prevPoints.addAll(drawTrees(hood.getTrees()));
                        BarChart<String,Number> barChart = barChartCreator(hood);
                        hBoxGraph.getChildren().add(barChart);
                    }
                }
                callout.setTitle("Neighbourhood Name");
                callout.setDetail(String.format(hoodName));
                callout.showCalloutAt(mapPoint);
            } else if (e.getButton() == MouseButton.SECONDARY && e.isStillSincePress()) {
                callout.dismiss();
                treeTypesBox.setPromptText("Select Tree Type");
            }
        });
        //----------RESET BTN ACTION------------------------------------------------------------------------------------
        resetBtn.setOnAction(event -> {
            hBoxGraph.getChildren().clear();
            neighbourhoodField.clear();
            unDrawTrees(prevPoints);
            callout.dismiss();
            mapView.setViewpoint(new Viewpoint(53.55, -113.42, 290000));
            treeTypesBox.setValue(null);
        });
        //----------SEARCH BTN ACTION-----------------------------------------------------------------------------------
        searchBtn.setOnAction(event -> {
            String hoodName = neighbourhoodField.getText();
            String comboBoxString = treeTypesBox.getValue();
            if (treeTypesBox.getValue() == null) comboBoxString = "";
            List<Tree> filteredTreeList = new ArrayList<>();
            if (!hoodName.equals("") && !comboBoxString.equals("")){
                unDrawTrees(prevPoints); hBoxGraph.getChildren().clear();
                for (Neighbourhood hood : hoods) {
                    if (hood.getName().equals(hoodName.toUpperCase())) {
                        for (Tree tree : hood.getTrees()) {
                            if (tree.getName().equalsIgnoreCase(treeTypesBox.getValue())) {
                                filteredTreeList.add(tree);
                            }
                        }
                        BarChart<String, Number> barChart = barChartCreator(hood);
                        hBoxGraph.getChildren().add(barChart);
                        mapView.setViewpoint(new Viewpoint(
                                hood.getCenter().getLatitude(), hood.getCenter().getLongitude(), 40000));
                    }
                }
                prevPoints.addAll(drawTrees(filteredTreeList));
            }
            else if (!hoodName.equals("")){
                unDrawTrees(prevPoints); hBoxGraph.getChildren().clear();
                for (Neighbourhood hood : hoods) {
                    if (hood.getName().equals(hoodName.toUpperCase())) {
                        System.out.println("hood list of trees size " + hood.getTrees().size());
                        prevPoints.addAll(drawTrees(hood.getTrees()));
                        BarChart<String, Number> barChart = barChartCreator(hood);
                        hBoxGraph.getChildren().add(barChart);
                        mapView.setViewpoint(new Viewpoint(
                                hood.getCenter().getLatitude(), hood.getCenter().getLongitude(), 40000));
                    }
                }
            }
            else if (!comboBoxString.equals("")){
                unDrawTrees(prevPoints); hBoxGraph.getChildren().clear();
                mapView.setViewpoint(new Viewpoint(53.55, -113.42, 290000));
                for (Neighbourhood hood : hoods) {
                    for (Tree tree : hood.getTrees()) {
                        if (tree.getName().equalsIgnoreCase(treeTypesBox.getValue())) {
                            filteredTreeList.add(tree);
                        }
                    }
                }
                prevPoints.addAll(drawTrees(filteredTreeList));
            }
            neighbourhoodField.clear();
            treeTypesBox.setValue(null);
            treeTypesBox.setPromptText("Select Tree Type");
            callout.dismiss();
        });
        //----------FIND MOST BTN---------------------------------------------------------------------------------------
        findMost.setOnAction(event -> {
            String comboBoxString = treeTypesBox.getValue();
            if (treeTypesBox.getValue() == null) comboBoxString = "";
            if (!comboBoxString.equals("")) {
                List<Tree> filteredTreeList = new ArrayList<>();
                Neighbourhood hood = findMostTreeType(comboBoxString);
                unDrawTrees(prevPoints); hBoxGraph.getChildren().clear();
                for (Tree tree : hood.getTrees()) {
                    if (tree.getName().equalsIgnoreCase(treeTypesBox.getValue())) {
                        filteredTreeList.add(tree);
                    }
                }
                BarChart<String, Number> barChart = barChartCreator(hood);
                hBoxGraph.getChildren().add(barChart);
                mapView.setViewpoint(new Viewpoint(
                        hood.getCenter().getLatitude(), hood.getCenter().getLongitude(), 45000));
                prevPoints.addAll(drawTrees(filteredTreeList));
            }
            neighbourhoodField.clear();
            treeTypesBox.setValue(null);
            treeTypesBox.setPromptText("Select Tree Type");
            callout.dismiss();
        });
        //--------------------------------------------------------------------------------------------------------------
        String cssLayout =
                "-fx-border-color: lightgrey;\n-fx-border-insets: 5;\n-fx-border-width: 1;\n-fxf-border-style: solid;\n";
        vBox.setStyle(cssLayout);
        vBox.getChildren().addAll(hoodLabel, neighbourhoodField,
                treeTypesBox, searchBtn, findMost, line, resetBtn, line1);
        stackPane.getChildren().add(mapView);
        try {
            Image image1 = new Image(new FileInputStream("legend.jpg"));
            ImageView iv = new ImageView(image1);
            stackPane.getChildren().add(iv);
            StackPane.setAlignment(iv, Pos.BOTTOM_LEFT);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        vBox.getChildren().add(hBoxGraph);
        borderPane.setLeft(vBox);
        stage.setMaximized(true);
        borderPane.setCenter(stackPane);
        treeTypesBox.setPromptText("Select Tree Type");
        stage.show();
    }
    private void drawHood(Neighbourhood hood){
        int numOfTrees = hood.getTrees().size();
        List<Coordinate> coordinates = hood.getCoordinates();
        SimpleLineSymbol blueOutlineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.GRAY, 1);
        PointCollection polygonPoints = new PointCollection(SpatialReferences.getWgs84());
        for (Coordinate coordinate: coordinates){
            polygonPoints.add(coordinate.getLongitude(), coordinate.getLatitude());
        }
        SimpleFillSymbol polygonFillSymbol = null;
        Polygon polygon = new Polygon(polygonPoints);
        if(numOfTrees >= 100) {
            polygonFillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID,
                            Color.web("#90EE90", .25), blueOutlineSymbol);
        }
        else if(numOfTrees > 25){
            polygonFillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID,
                            Color.web("#FFC300", .30), blueOutlineSymbol);
        }
        else if(numOfTrees > 0){
            polygonFillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID,
                            Color.web("#FFA500", .50), blueOutlineSymbol);
        }
        else{
            polygonFillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID,
                            Color.web("#FFCCCB", .60), blueOutlineSymbol);
        }
        Graphic polygonGraphic = new Graphic(polygon, polygonFillSymbol);
        graphicsOverlay.getGraphics().add(polygonGraphic);
    }
    private List<Graphic> drawTrees(List<Tree> trees){
        System.out.println("success");
        HashMap<String, Color> colorMap = treeHashMap();
        List<Graphic> prevPoints = new ArrayList<>();
        for (Tree tree : trees){
            Point point = new Point(tree.getCoOrd().getLongitude(), tree.getCoOrd().getLatitude(),
                    SpatialReferences.getWgs84());
            Color color = colorMap.get(tree.getName());
            SimpleMarkerSymbol simpleMarkerSymbol =
                    new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, color, 10);
            SimpleLineSymbol blueOutlineSymbol =
                    new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLACK, 2);
            simpleMarkerSymbol.setOutline(blueOutlineSymbol);
            Graphic pointGraphic = new Graphic(point, simpleMarkerSymbol);
            prevPoints.add(pointGraphic);
            graphicsOverlay.getGraphics().add(pointGraphic);
        }
        return prevPoints;
    }
    private void unDrawTrees(List<Graphic> prevPoints){
        for (Graphic point : prevPoints){
            graphicsOverlay.getGraphics().remove(point);
        }
    }
    private BarChart<String,Number> barChartCreator(Neighbourhood hood){
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        final BarChart<String,Number> bc =
                new BarChart<String,Number>(xAxis,yAxis);
        bc.setTitle("Trees by Neighbourhood"); xAxis.setLabel("Neighbourhood"); yAxis.setLabel("# of Trees");
        XYChart.Series series1 = null;
        HashMap<String, Integer> treeMap = hood.treesAndNumbers();
        HashMap<String, String> colorMap = cssMap();
        for (String treeName: treeMap.keySet()){
            series1 = new XYChart.Series();
            series1.setName(treeName);
            series1.getData().add(new XYChart.Data(hood.getName(), treeMap.get(treeName)));
            bc.getData().add(series1);
        }
        Integer index = 0;
        for (String treeName: treeMap.keySet()) {
            bc.lookupAll(".default-color"+index.toString()+".chart-bar")
                    .forEach(n -> n.setStyle("-fx-bar-fill: " + colorMap.get(treeName)));
            index += 1;
        }
        for(Node node : bc.lookupAll("Label.chart-legend-item")) {
            Label tempLabel = (Label)node;
            for (String treeName: treeMap.keySet()) {
                if (tempLabel.getText().equals(treeName)) {
                    tempLabel.getGraphic().setStyle("-fx-bar-fill: "+ colorMap.get(treeName)+";");
                }
            }
        }
        bc.autosize();
        String legend = bc.getLegendSide().toString();
        System.out.println(legend);
        bc.setLegendVisible(true);
        bc.setLegendSide(Side.TOP);
        return bc;
    }
    private Neighbourhood findMostTreeType(String treeName){
        int most = -1;
        Neighbourhood target = null;
        for (Neighbourhood hood : hoods){
            HashMap<String, Integer> treeList = hood.treesAndNumbers();
            if (treeList.get(treeName)!=null && treeList.get(treeName) > most){
                most = treeList.get(treeName);
                target = hood;
            }
        }
        return target;
    }
    private HashMap<String, Color> treeHashMap(){
        HashMap<String, Color> map = new HashMap<>();
        map.put("Hackberry", Color.DARKVIOLET);
        map.put("Apple", Color.LIGHTPINK);
        map.put("Plum", Color.PLUM);
        map.put("Pear", Color.LIGHTGREEN);
        map.put("Russian Olive", Color.GRAY);
        map.put("Cherry", Color.RED);
        map.put("Chokecherry", Color.CRIMSON);
        map.put("Butternut", Color.YELLOW);
        map.put("Crabapple", Color.GREEN);
        map.put("Acorn", Color.LIGHTGREY);
        map.put("Juniper", Color.BLUE);
        map.put("Saskatoon", Color.BLUEVIOLET);
        map.put("Hawthorn", Color.CYAN);
        map.put("Coffeetree pod", Color.BROWN);
        map.put("Walnut", Color.BEIGE);
        map.put("Caragana flower/pod", Color.TEAL);
        return map;
    }
    private HashMap<String, String> cssMap(){
        HashMap<String, String> map = new HashMap<>();
        map.put("Hackberry", "darkviolet");
        map.put("Apple", "lightpink");
        map.put("Plum", "plum");
        map.put("Pear", "lightgreen");
        map.put("Russian Olive", "grey");
        map.put("Cherry", "red");
        map.put("Chokecherry", "crimson");
        map.put("Butternut", "yellow");
        map.put("Crabapple", "green");
        map.put("Acorn", "lightgrey");
        map.put("Juniper", "blue");
        map.put("Saskatoon", "blueviolet");
        map.put("Hawthorn", "cyan");
        map.put("Coffeetree pod", "brown");
        map.put("Walnut", "beige");
        map.put("Caragana flower/pod", "teal");
        return map;
    }
    @Override
    public void stop() {
        if (mapView != null) {
            mapView.dispose();
        }
    }
}
