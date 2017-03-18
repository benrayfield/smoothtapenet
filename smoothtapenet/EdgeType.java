/** Ben F Rayfield offsers this software opensource GNU GPL 2+ */
package smoothtapenet;

/** These are ordered so different implementations will use the first n of them.
More advanced implementations will also do learning on the higher indexed EdgeTypes.
You need at least the first 2 (move and writeVal), holding the others constant.
*/
public enum EdgeType{
	
	/** How much to move in range -1 to 1. Continuous scaling keeps position at 0,
	so when move x distance, range -1 to x is scaled to -1 to 0, and x to 1 scaled to 0 to 1.
	*/
	move,
	
	/** weight in "sigmoid of weightedSum of Tape.read()" */
	writeVal,
	
	writeDecay,
	
	/** how big a bellcurve of position to read and write at */
	stdDev

}
