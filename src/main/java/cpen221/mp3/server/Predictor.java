package cpen221.mp3.server;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class Predictor {
    private int n;
    private List<Double> doubleList = new ArrayList<>();
    private List<Boolean> boolList = new ArrayList<>();

    /*
    Abstraction Function:
    Represents the list of predictions

    Representation Invariant:
    - n > 0
    - doubleList and boolList are not null
     */

    /**
     * Construct new predictor object
     * @param objList the List of values or timestamps from previous events, elements are either Double or Boolean, is not null
     * @param isDouble confirms whether elements of objList are Double or Boolean
     * @param n the number of predictions to make
     */
    public Predictor(List<Object> objList, boolean isDouble, int n){
        this.n = n;
        if(isDouble) {
            for(Object obj : objList){
                this.doubleList.add((Double)obj);
            }
        }
        else {
            for(Object obj : objList){
                this.boolList.add((Boolean)obj);
            }
        }
    }

    /**
     * Predicts double values
     * @return the list of Doubles representing the predictions
     */
    public List<Double> predictDouble (){
        List<Double> predictions = new ArrayList<>();
        if(this.doubleList.isEmpty()) return predictions;
        if(detectPattern()){
            for(int i = 0; i < this.n; i++){
                int index = this.doubleList.size()-(i+1)%2;
                predictions.add(this.doubleList.get(index-1));
            }
        }
        else{
            for(int i = this.doubleList.size(); i < this.doubleList.size()+this.n; i++){
                predictions.add(LagrangeInterpolation(i));
            }
        }
        return predictions;
    }

    /**
     * Uses Lagrange interpolation to predict a double value given previous double values
     * @param x the index in the list of predictions
     * @return the value to be predicted
     */
    private double LagrangeInterpolation (int x){
        int[] xArr = IntStream.range(0, this.doubleList.size()).toArray();
        double result = 0;

        for(int i = 0; i < this.doubleList.size(); i++){
            double products = this.doubleList.get(i);
            for(int j = 0; j < this.doubleList.size(); j++){
                if(j!=i){
                    products*=((x-xArr[j])/(double)(xArr[i]-xArr[j]));
                }
            }
            result+=products;
        }
        return result;
    }

    /**
     * Detect whether the 6 most recent values has alternating values
     * @return true if the 6 most recent values has alternating values, false otherwise
     */
    private boolean detectPattern (){
        int mostRecentInd = this.doubleList.size();
        if(mostRecentInd>=6){
            boolean doub1 = this.doubleList.get(mostRecentInd-1).equals(this.doubleList.get(mostRecentInd-3))&&this.doubleList.get(mostRecentInd-1).equals(this.doubleList.get(mostRecentInd-5));
            boolean doub2 = this.doubleList.get(mostRecentInd-2).equals(this.doubleList.get(mostRecentInd-4))&&this.doubleList.get(mostRecentInd-2).equals(this.doubleList.get(mostRecentInd-6));
            return doub1 && doub2;
        }
        else{
            return false;
        }
    }

    /**
     * Predicts boolean values
     * @return the list of Booleans representing the predictions
     */
    public List<Boolean> predictBool (){

        Map<Boolean, Map<Boolean, Double>> transitionMatrix = new HashMap<>();
        for (int i = 0; i < this.boolList.size()-1; i++){
            boolean currState = this.boolList.get(i);
            boolean nextState = this.boolList.get(i+1);

            transitionMatrix.putIfAbsent(currState, new HashMap<>());
            transitionMatrix.get(currState).merge(nextState, 1.0, Double::sum);
        }
        List<Boolean> predictions = new ArrayList<>();
        boolean currentState = this.boolList.get(this.boolList.size()-1);
        for (int i = 0; i < this.n; i++) {

            Map<Boolean, Double> nextStateProbabilities = transitionMatrix.get(currentState);

            boolean nextState = nextStateProbabilities.entrySet().stream()//make random
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(currentState);

            predictions.add(nextState);
            currentState = nextState;
        }
        return predictions;
    }

}
