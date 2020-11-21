package assignement;

public class OrderConstraint extends Constraint{
	PTask t1;
	PTask t2;
	
	public OrderConstraint(PTask one, PTask two) {
		this.t1 = one;
		this.t2 = two;
	}
	
	boolean compatible(Solution solution) {
		boolean res = true;
		if (solution.getTime().get(this.t1) > solution.getTime().get(this.t2)) {
			return false;
			//System.out.println("OderConstraint");
		}
		return res;
	}
	
	public String toString() {
		return "OrderConstrain : [" + this.t1.toString() + ", " + this.t2.toString() + "]";
	}
}