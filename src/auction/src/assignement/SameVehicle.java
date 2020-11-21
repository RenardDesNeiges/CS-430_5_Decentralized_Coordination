package assignement;

public class SameVehicle extends Constraint {
	PTask t1;
	PTask t2;
	
	public SameVehicle(PTask one, PTask two) {
		this.t1 = one;
		this.t2 = two;
	}
	
	boolean compatible(Solution solution) {
		boolean res = true;
		if (solution.getVehicle().get(this.t1) != solution.getVehicle().get(this.t2)) {
			return false;
			//System.out.println("SameVehicle Constraint");
			//System.out.println(solution.getVehicle().get(this.t1).toString() + " != " + solution.getVehicle().get(this.t2).toString());
		}
		return res;
	}
	
	@Override
	public String toString() {
		return "SameVehicle = [" + this.t1.toString() + ", " + this.t2.toString() + "]";
	}
}
