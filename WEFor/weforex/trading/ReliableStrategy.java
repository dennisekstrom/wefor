package trading;

/**
 * Let subclasses implement abstract methods in IStrategy
 * 
 * One method for each characteristic a graph can have, should be implemented
 * here, simply returning Reliability.NONE
 * 
 * Explinations of characteristics: Fundamental times: TODO Default technical
 * analysis: TODO Consolidation: TODO High volatility: TODO
 * 
 * @author Dennis
 */

public abstract class ReliableStrategy implements Strategy {

	/**
	 * This strategy's reliability for default technical analysis
	 */
	public Reliability getDefaultReliability() {
		return Reliability.NONE;
	}

	/**
	 * This strategy's reliability in fundamental times
	 */
	public Reliability getFundamentalReliability() {
		return Reliability.NONE;
	}

	/**
	 * This strategy's reliability during consolidation
	 */
	public Reliability getConsolidationReliability() {
		return Reliability.NONE;
	}

	/**
	 * This strategy's reliability during high volatility
	 */
	public Reliability getHighVolatilityReliability() {
		return Reliability.NONE;
	}

	// TODO add other characteristics
}