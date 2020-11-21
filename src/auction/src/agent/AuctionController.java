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


    private List<Long[]> bidHistory = new ArrayList<Long[]>();
    private List<Integer> winHistory = new ArrayList<Integer>();

    public void printBidHistory(){
        System.out.println(bidHistory);
        System.out.println(winHistory);
    }

    public void updateBidHistory(int winner, Long[] bids){
        bidHistory.add(bids);
        winHistory.add(winner);
        printBidHistory();
    }

    public Long returnPrice(Task task) {
        return (long) Math.round(0);
    }
}
