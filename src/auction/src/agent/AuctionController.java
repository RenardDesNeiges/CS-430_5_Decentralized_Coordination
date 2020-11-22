package agent;

// Generic imports
import java.util.Random;
import javax.swing.text.Style;

import org.apache.commons.math.stat.descriptive.moment.Mean;

import java.util.Collections;
import java.util.stream.DoubleStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.lang.Math;
// Logist imports
import logist.Measures;
import logist.simulation.Vehicle;
import logist.topology.Topology;
import logist.topology.Topology.City;
import logist.task.Task;

import agent.Tuple;




public class AuctionController {

    private int round = 0;
    private int competitors;
    
    private double sd_weight = 0.4; //tIme To GEt HeURisTIc
    private double pB = 0.7;

    private Topology graphTopology;
    private Integer agentID;
    private List<Long[]> bidHistory = new ArrayList<Long[]>();
    private List<Double[]> exCostHistory = new ArrayList<Double[]>();
    private List<Integer> winHistory = new ArrayList<Integer>();
    private List<Task> taskHistory = new ArrayList<Task>();
    private HashMap<Integer,HashSet<City>> agentCities = new HashMap<Integer,HashSet<City>>();

    private ArrayList<ArrayList<Tuple<Double,Double>>> estimatorHistory = new ArrayList<ArrayList<Tuple<Double,Double>>>();


    public AuctionController(Topology graphTopology,int agentID){
        this.graphTopology = graphTopology;
        this.agentID = agentID;
    }

    public void printBidHistory(Boolean verbose){
        for(int i = 0; i<this.bidHistory.size(); i++){
            Long[] bids = this.bidHistory.get(i);
            Double[] exCosts = this.exCostHistory.get(i);
            if(verbose){
                if(i < 10){
                    System.out.print("Bid#0"+i+"; ");
                }
                else{
                    System.out.print("Bid#"+i+"; ");
                }      
            }
            for(Long bid : bids){
                System.out.print(bid+", ");
            }
            System.out.print("\n");
            if(exCosts != null){
                if(verbose){
                    if(i < 10){
                        System.out.print("eC0#0"+i+"; ");
                    }
                    else{
                        System.out.print("eC0#"+i+"; ");
                    }      
                }
                for(Double exCo : exCosts){
                    System.out.print(exCo+", ");
                }
                System.out.print("\n");
            }
            else{
                if(verbose){
                    System.out.println("est#"+i+":null");
                }
            }
            if(this.estimatorHistory.get(i) != null){
                if(verbose){
                    if(i < 10){
                        System.out.print("var#0"+i+"; ");
                    }
                    else{
                        System.out.print("var#"+i+"; ");
                    }      
                }
                for(Tuple<Double,Double> est : this.estimatorHistory.get(i)){
                    System.out.print(est.right+", ");
                }
                System.out.print("\n");
            }
            else{
                if(verbose) System.out.print("var#"+i+":");
                System.out.println("null");
            }
            if(this.estimatorHistory.get(i) != null){
                if(verbose){
                    if(i < 10){
                        System.out.print("est#0"+i+"; ");
                    }
                    else{
                        System.out.print("est#"+i+"; ");
                    }      
                }
                for(Tuple<Double,Double> est : this.estimatorHistory.get(i)){
                    System.out.print(est.left+";"+this.cBound(this.pB, est.left, est.right, i)+", ");
                }
                
                System.out.print("\n");
            }
            else{
                if(verbose) System.out.print("est#"+i+":");
                System.out.println("null");
            }
            if(verbose){
                if(this.agentID == this.winHistory.get(i)){
                    System.out.println("WON!");
                }
                else{
                    System.out.println("Lost..");
                }
            }
            
        }
        System.out.println("Cities, set :");
        System.out.println(this.agentCities);
    }

    private Double[] computeExCost(Task newTask){
        Double[] ret = new Double[this.competitors];
        for(int i = 0; i < this.competitors; i+=1){
            // Loop over all agents
            if(this.agentCities.containsKey(i)){  // If the agent has taken up some contracts we can use that to estimate it's marginal cost
                Double sumDist = 0.0;
                List<Double> distances = new ArrayList<Double>();
                for(City city : this.agentCities.get(i)){
                    distances.add(city.distanceTo(newTask.pickupCity));
                    distances.add(city.distanceTo(newTask.deliveryCity));
                    sumDist += city.distanceTo(newTask.pickupCity)+city.distanceTo(newTask.deliveryCity);
                }
                ret[i] = this.sd_weight*Collections.min(distances) + (1-this.sd_weight) * sumDist/distances.size();
            }
            else{ //If the agent hasn't taken a contract yet, nothing to compute
                ret[i] = Double.POSITIVE_INFINITY; //I use infinity to denote that no meanifull value can be infered (the opponent hasn't taken a task yet)
            }
        }
        return ret;
    }

    private Double cBound(Double p, Double mu, Double var, double n){    
        return mu - p*Math.sqrt(var/n);
    }


    //Inch'allah this works
    private Tuple<Double,Double> LinearRegression(ArrayList<Double> x, ArrayList<Double> y) {
        if (x.size() != y.size()) {
            throw new IllegalArgumentException("array lengths are not equal");
        }
        int n = x.size();

        // first pass
        Double sumx = 0.0, sumy = 0.0, sumx2 = 0.0;
        for (int i = 0; i < n; i++) {
            sumx  += x.get(i);
            sumx2 += x.get(i)*x.get(i);
            sumy  += y.get(i);
        }
        Double xbar = sumx / n;
        Double ybar = sumy / n;

        // second pass: compute summary statistics
        Double xxbar = 0.0, yybar = 0.0, xybar = 0.0;
        for (int i = 0; i < n; i++) {
            xxbar += (x.get(i) - xbar) * (x.get(i) - xbar);
            yybar += (y.get(i) - ybar) * (y.get(i) - ybar);
            xybar += (x.get(i) - xbar) * (y.get(i) - ybar);
        }
        Double slope  = xybar / xxbar;
        Double intercept = ybar - slope * xbar;
        return new Tuple<Double,Double>(slope,intercept);
    }

    //Inch'allah this works
    private Tuple<Double,Double> LinearRegressionVariance(ArrayList<Double> x, ArrayList<Double> y,Double slope, Double intercept) {
        int n = x.size();
        Double sumx = 0.0, sumy = 0.0, sumx2 = 0.0;
        for (int i = 0; i < n; i++) {
            sumx  += x.get(i);
            sumx2 += x.get(i)*x.get(i);
            sumy  += y.get(i);
        }
        Double xbar = sumx / n;
        Double ybar = sumy / n;

        Double xxbar = 0.0, yybar = 0.0, xybar = 0.0;
        for (int i = 0; i < n; i++) {
            xxbar += (x.get(i) - xbar) * (x.get(i) - xbar);
            yybar += (y.get(i) - ybar) * (y.get(i) - ybar);
            xybar += (x.get(i) - xbar) * (y.get(i) - ybar);
        }

        // more statistical analysis
        Double rss = 0.0;      // residual sum of squares
        Double ssr = 0.0;      // regression sum of squares
        for (int i = 0; i < n; i++) {
            Double fit = slope*x.get(i) + intercept;
            rss += (fit - y.get(i)) * (fit - y.get(i));
            ssr += (fit - ybar) * (fit - ybar);
        }

        Double degreesOfFreedom = (double) n-2;
        Double r2    = ssr / yybar;
        Double svar  = rss / degreesOfFreedom;
        Double svar1 = svar / xxbar;
        Double svar0 = svar/n + xbar*xbar*svar1;

        return new Tuple<Double,Double>(svar, svar0);
    }

    // returns a list of all errors in the past
    private ArrayList<Double> errors(){
        ArrayList<Double> err = new ArrayList<Double>();
        for(int i = 0; i<bidHistory.size(); i+=1){
            Long minBid = Long.MAX_VALUE;
            for(int j = 0; j<bidHistory.get(i).length; j+=1){
                if(j!= this.agentID){
                    if(bidHistory.get(i)[j] < minBid){
                        minBid = bidHistory.get(i)[j];
                    }
                }
                
            }
            err.add((double) minBid-bidHistory.get(i)[this.agentID]);

        }
        return err;
    }

    // returns an array of double tuples where left contains avg estimation and right contains variance
    private ArrayList<Tuple<Double,Double>> computeExPrice(Task newTask){ //fuck that language without tuples I created tuples myself
        Double[] newExCosts = this.exCostHistory.get(round);
        ArrayList<Tuple<Double,Double>> ret = new ArrayList<Tuple<Double,Double>>();
        // Loop over all agents 
        for(int i = 0; i < this.competitors; i+=1){
            Tuple<Double,Double> estimator = new Tuple<Double,Double>(Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY);
            // formating the data so the mean-square error fitting algorithm is somewhat more readable
            ArrayList<Double> exCo = new ArrayList<Double>();
            ArrayList<Double> bids = new ArrayList<Double>();
            Double sumBids = 0.0;
            for(int j = 0; j< this.bidHistory.size(); j+=1){
                sumBids += (double) this.bidHistory.get(j)[i];
                if(this.exCostHistory.get(j) != null){
                    if(this.exCostHistory.get(j)[i] != Double.POSITIVE_INFINITY){
                        exCo.add(this.exCostHistory.get(j)[i]);
                        bids.add((double) this.bidHistory.get(j)[i]);
                    }
                }
                
            }
            
            // should compute VARIANCE HERE

            // Checking if there we have exCostData
            if(exCo.size()>=2){
                //compute mean-square error for a line if we have at least two points
                Tuple<Double,Double> line = LinearRegression(exCo,bids);
                Double slope = line.left;
                Double intercept = line.right;
                Tuple<Double,Double> variance = LinearRegressionVariance(exCo,bids,slope,intercept);
                Double sVar = variance.right;
                estimator.left = intercept+slope*newExCosts[i];
                estimator.right = sVar;
            }
            else{
                //If we have a no exCo values we just try to find out the variance and the mean
                int n = this.bidHistory.size();
                estimator.left = sumBids/n;
                if(n > 1){
                    Double sumVar = 0.0;
                    for(int j = 0; j< n; j+=1){
                        sumVar += (this.bidHistory.get(j)[i]-estimator.left)*(this.bidHistory.get(j)[i]-estimator.left);
                    }
                    estimator.right = sumVar/(n-1);
                }
                else{
                    estimator.right = 0.0;
                }
            }
            ret.add(estimator);
        }
        return ret;
    }

    public void updateBidHistory(int winner, Long[] bids, Task task){
        //updating the histories of bids, winning agents and tasks auctioned
        bidHistory.add(bids);
        winHistory.add(winner);
        taskHistory.add(task);

        // updating the set of cities visited by each agent
        HashSet<City> newSet = new HashSet<City>();
        if (agentCities.containsKey(winner)){
            newSet = new HashSet<City>(agentCities.get(winner));
        }
        newSet.add(task.deliveryCity);
        newSet.add(task.pickupCity);
        if (agentCities.containsKey(winner)){
            agentCities.remove(winner);
        }
        agentCities.put((int) winner, newSet);
       
        // 

        //This is just debut shit
        if(winHistory.size() == 19) // kind'of arbitrary but ya know I wan't some example of data
            printBidHistory(true);
    }

    // computes an offer for a given task while learning from the history
    public Long returnPrice(Task task, Double marginalCost) {

        //Long marginalCost = (long)2000;// here I should call the marginal cost for us to perform the task
        Double price;
        if(round == 1){
            //cannot be found out at round 0
            this.competitors = this.bidHistory.get(0).length;   
        }

        if(round == 0){
            //strategy for round 0 (we have no information yet)
            this.exCostHistory.add(null); // we add a null element as the 0 of exCostHistory so that the ids line up
            this.estimatorHistory.add(null);

            price = 0.8*marginalCost;
            System.out.println("r1 : marg_cost = "+marginalCost + " ; price = " + Math.round(price));
        }  
        else{
            //strategy for the following rounds
            this.exCostHistory.add(this.computeExCost(task));
            ArrayList<Tuple<Double,Double>> est = this.computeExPrice(task);
            this.estimatorHistory.add(est);
            ArrayList<Double> lowBounds = new ArrayList<Double>();
            for(Tuple<Double,Double> e: est){
                lowBounds.add(this.cBound(this.pB, e.left, e.right, round));
            }
            Double lb = Collections.min(lowBounds);

            Double cumError = 0.0;
            for(Double e: this.errors()){
                cumError += e;
            }
            
            Double margin = 0.1*marginalCost + 0.3*(cumError/(round-1));
            if(margin < 0){
                margin = 10.0;
            }

            //this is where the strategy has to be implemented
            

            if(marginalCost < lb){
                price = lb;
                System.out.println("<lb => marg_cost = "+marginalCost + " ; price = " + Math.round(price) + " ; lb = " + lb+"; margin = "+(lb-marginalCost));
            }
            else{
                price = marginalCost + margin;
                //System.out.println(this.errors());
                System.out.println("margon : marg_cost = "+marginalCost + " ; price = " + Math.round(price) + " ; lb = " + lb+"; margin = "+margin);
            }

        }

        round += 1; // increment the round number (behavior changes depending on time)
        //System.out.println("marg_cost = "+marginalCost + " ; price = " + Math.round(price));
        return (long) Math.round(price);
    }
}
