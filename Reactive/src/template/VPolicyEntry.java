package template;

/*
 * entry of policy corresponding to a specific state;
 * entry contains two part: (1) the optimal action to the state
 * 							(2) the expected profit  
 */
public class VPolicyEntry {
	
	private VAction antion = null;
	private double profit = 0;
	
	public VPolicyEntry(VAction action, double profit){
		this.antion = action;
		this.profit = profit;
	}

	public VAction getAntion() {
		return antion;
	}

	public void setAntion(VAction antion) {
		this.antion = antion;
	}

	public double getProfit() {
		return profit;
	}

	public void setProfit(double profit) {
		this.profit = profit;
	}
	
	

}
