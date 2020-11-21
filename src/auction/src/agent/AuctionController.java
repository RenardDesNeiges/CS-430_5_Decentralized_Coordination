package agent;

// Generic imports
import java.util.Random;

// Logist imports
import logist.Measures;
import logist.simulation.Vehicle;
import logist.topology.Topology.City;
import logist.task.Task;

public class AuctionController {

    public Long givePrice(Task task, Vehicle vehicle, City currentCity, Random random) {
        if (vehicle.capacity() < task.weight)
        return null;

        long distanceTask = task.pickupCity.distanceUnitsTo(task.deliveryCity);
        long distanceSum = distanceTask
                + currentCity.distanceUnitsTo(task.pickupCity);
        double marginalCost = Measures.unitsToKM(distanceSum
                * vehicle.costPerKm());

        double ratio = 1.0 + (random.nextDouble() * 0.05 * task.id);
        double bid = ratio * marginalCost;

        return (long) Math.round(bid);
    }
}
