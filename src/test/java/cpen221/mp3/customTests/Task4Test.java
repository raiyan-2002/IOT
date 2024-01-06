package cpen221.mp3.customTests;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.net.*;
import java.io.*;
import cpen221.mp3.client.*;
import cpen221.mp3.entity.*;
import cpen221.mp3.event.*;
import cpen221.mp3.handler.*;
import cpen221.mp3.server.*;

import static org.junit.jupiter.api.Assertions.*;

public class Task4Test {
    @Test
    public void testBooleanConst(){
        List<Boolean> booleans = new ArrayList<>();
        for(int i = 0; i < 10; i++){
            booleans.add(true);
        }

        Predictor predictor = new Predictor(new ArrayList<>(booleans), false, 5);
        List<Boolean> check = new ArrayList<>();

        for(int i = 0; i < 5; i++){
            check.add(true);
        }

        assertEquals(new ArrayList<Object>(predictor.predictBool()), new ArrayList<Object>(check));
    }
    @Test
    public void testBooleanAlternating(){
        List<Boolean> booleans = new ArrayList<>();
        for(int i = 0; i < 5; i++){
            booleans.add(true);
            booleans.add(false);
        }

        Predictor predictor = new Predictor(new ArrayList<>(booleans), false, 10);
        List<Boolean> check = new ArrayList<>();

        for(int i = 0; i < 5; i++){
            check.add(true);
            check.add(false);
        }

        assertEquals(new ArrayList<Object>(predictor.predictBool()), new ArrayList<Object>(check));
    }
    @Test
    public void testDoubleConst(){
        List<Double> doubs = new ArrayList<>();
        for(int i = 0; i < 10; i++){
            doubs.add(1.0);
        }

        Predictor predictor = new Predictor(new ArrayList<>(doubs), true, 5);
        List<Double> check = new ArrayList<>();

        for(int i = 0; i < 5; i++){
            check.add(1.0);
        }

        assertEquals(new ArrayList<Object>(predictor.predictDouble()), new ArrayList<Object>(check));
    }
    @Test
    public void testDoubleAlternating(){
        List<Double> doubs = new ArrayList<>();
        for(int i = 0; i < 10; i++){
            doubs.add(1.0);
            doubs.add(2.0);
        }

        Predictor predictor = new Predictor(new ArrayList<>(doubs), true, 10);
        List<Double> check = new ArrayList<>();

        for(int i = 0; i < 5; i++){
            check.add(1.0);
            check.add(2.0);
        }

        assertEquals(new ArrayList<Object>(predictor.predictDouble()), new ArrayList<Object>(check));
    }
    @Test
    public void testDoubleLinear(){
        List<Double> doubs = new ArrayList<>();
        for(int i = 0; i < 10; i++){
            doubs.add((double)i);
        }

        Predictor predictor = new Predictor(new ArrayList<>(doubs), true, 5);
        List<Double> check = new ArrayList<>();

        for(int i = 0; i < 5; i++){
            check.add(doubs.get(doubs.size()-1)+i+1);
            assertEquals(predictor.predictDouble().get(i), check.get(i), 0.000001);
        }

    }
    @Test
    public void testDoubleQuadratic(){
        List<Double> doubs = new ArrayList<>();
        for(int i = 0; i < 10; i++){
            doubs.add((double)i*i);
        }

        Predictor predictor = new Predictor(new ArrayList<>(doubs), true, 5);
        List<Double> check = new ArrayList<>();
        for(int i = 0; i < 5; i++){
            check.add((double)(doubs.size()+i)*(doubs.size()+i));
        }
        for(int i = 0; i < 5; i++){
            assertEquals(predictor.predictDouble().get(i), check.get(i), 0.000001);
        }

    }
    @Test
    public void testFromServer1(){
        List<Boolean> booleans = new ArrayList<>();
        for(int i = 0; i < 10; i++){
            booleans.add(true);
        }

        Predictor predictor = new Predictor(new ArrayList<>(booleans), false, 5);
        List<Boolean> check = new ArrayList<>();

        for(int i = 0; i < 5; i++){
            check.add(true);
        }

        assertEquals(new ArrayList<Object>(predictor.predictBool()), new ArrayList<Object>(check));
    }
}
