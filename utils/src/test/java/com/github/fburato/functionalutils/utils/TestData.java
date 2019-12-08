package com.github.fburato.functionalutils.utils;

public class TestData {
    private String a;
    private String a1;
    private Integer b;
    private double c;

    public TestData() {
    }

    public TestData(String a, String a1, Integer b, double c) {
        this.a = a;
        this.a1 = a1;
        this.b = b;
        this.c = c;
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public String getA1() {
        return a1;
    }

    public void setA1(String a1) {
        this.a1 = a1;
    }

    public Integer getB() {
        return b;
    }

    public void setB(Integer b) {
        this.b = b;
    }

    public double getC() {
        return c;
    }

    public void setC(double c) {
        this.c = c;
    }
}
