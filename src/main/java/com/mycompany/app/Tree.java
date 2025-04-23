package com.mycompany.app;

public class Tree {
    private String name;
    private Coordinate coOrd;
    private String hood;
    public Tree(String name, Coordinate coOrd, String hood) {
        this.name = name;
        this.coOrd = coOrd;
        this.hood = hood;
    }
    public String getName() {
        return name;
    }
    public Coordinate getCoOrd() {
        return coOrd;
    }
    public String getHood() {
        return hood;
    }
    @Override
    public String toString() {
        return "Tree{" +
                "name='" + name + '\'' +
                ", coOrd=" + coOrd +
                ", hood='" + hood + '\'' +
                '}';
    }
}
