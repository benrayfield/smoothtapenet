/** Ben F Rayfield offsers this software opensource GNU GPL 2+ */
package smoothtapenet;

/** A smooth turingTape of scalar value and scalar position.
All changes are in a bellcurve area and toward a target value at some decay rate.
You can only read and write at current position.
Moving may cause roundoff or scaling in all other parts,
since it may be held to always be at position 0 between tape ends -1 and 1,
scaling the 2 sides while moving so position stays at 0.
TODO choose a model of movement and scaling.
<br><br>
This is normally implemented as points linear interpolated between.
*/
public interface Tape{
	
	public float read();
	
	/** Returns this Tape Decay ranges 0 to 1 and is normally very near 0. Value can be anything.
	ForExample a neuralnet uses node values 0 to 1, or bifractionNodes use values -1 to 1.
	*/
	public Tape write(float value, float decay);
	
	/** returns standard deviation of position for reading and writing */
	public float dev();
	
	/** Sets dev(). Returns this Tape */
	public Tape dev(float newDev);
	
	/** offset ranges -.5 to .5 (since position ranges -1 to 1 and we need some room for scaling)
	and normally is very near 0. Returns this Tape.
	*/ 
	public Tape move(float offset);
	
	/** Normally used for debugging and display. Reads at point instead of bellcurve.
	Position ranges -1 to 1. Those ranges are stretched by move(double) to stay at 0.
	*/
	public float readPoint(float position);
	
	/** Tape linear interpolates between sparse points. These may have roundoff.
	For each of these you can readPoint(float) to get value, or readPoint anywhere in -1 to 1.
	*/
	public float[] sparsePositions();

}
