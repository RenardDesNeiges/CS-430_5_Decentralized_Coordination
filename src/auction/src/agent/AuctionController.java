package agent;

// Generic imports
import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Logist imports
import logist.Measures;
import logist.simulation.Vehicle;
import logist.topology.Topology.City;
import logist.task.Task;

public class AuctionController {


    private List<long[]> bidHistory = new ArrayList<long[]>();
    private List<Integer> winHistory = new ArrayList<Integer>();

    public void printBidHistory(){
        for(int i = 0; i<this.bidHistory.size(); i++){
            long[] bids = this.bidHistory.get(i);
            if(i < 10){
                System.out.print("Bid#0"+i+"; ");
            }
            else{
                System.out.print("Bid#"+i+"; ");
            }            
            for(Long bid : bids){
                System.out.print(bid+", ");
            }
            System.out.println("won by : " + winHistory.get(i));
        }
    }

    public void updateBidHistory(int winner, long[] bids){
        bidHistory.add(bids);
        winHistory.add(winner);
        if(winHistory.size() == 19) // kind'of arbitrary but ya know I wan't some example of data
            printBidHistory();
    }

    public Long returnPrice(Task task) {
        return (long) Math.round(0);
    }
}
