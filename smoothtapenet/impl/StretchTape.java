/** Ben F Rayfield offsers this software opensource GNU GPL 2+ */
package smoothtapenet.impl;
import static humanaicore.common.CommonFuncs.*;
import java.util.Arrays;
import humanaicore.err.Err;
import humanaicore.err.Todo;
import humanaicore.math.BellMath;
import smoothtapenet.Tape;

/** Ranges -1 to 1 and stretches tape while moving so you're always at 0.
Stores positions in arbitrary range and scales them when observed.
From relPos[0] to center is scaled to range -1 to 0.
From center to relPos[relPos.length-1] is scaled to range 0 to 1.
This way we dont have to move the points
while scaling them all to keep position always at 0.
TODO lossyCompress a side into fewer linearInterpolated points when too many points.
*/
public class StretchTape implements Tape{
	
	public float[] relPos, val;
	
	/** position in relPos[] coordinates */
	public float center;
	
	public float dev;
	
	public StretchTape(float firstVal, float lastVal, float dev){
		relPos = new float[]{-1, 1};
		val = new float[]{firstVal, lastVal};
		this.dev = dev;
		adjustResolutionOfSparsePoints();
	}

	/** read weightedSum at bellcurve whose areaUnderTheCurve is 1 */
	public float read(){
		//throw new Todo("read bellcurve. Could inefficiently use readPoint for now and optimize later");
		//FIXME TODO optimize. This is very wasteful. Use relPos[] and val[] directly for near points around a binary search for closest index to center.
		float densitySum = 0;
		float weightedValSum = 0;
		for(int i=-25; i<=25; i++){
			float pos = center+dev*i/10;
			float dens = bellDensity(pos);
			densitySum += dens;
			float val = readPoint(pos);
			weightedValSum += dens*val;
		}
		return weightedValSum/densitySum;
	}

	/** TODO Decay by decayRate=decay*bellHeight, for a bell with 1.0 area under curve (or is it a bell with height 1 at its center?) toward value */
	public Tape write(float value, float decay){
		if(decay != 0){
			//FIXME optimize this is very inefficient to check all the points
			for(int i=0; i<relPos.length; i++){
				float pos = relPosToAbsolutePos(relPos[i]);
				float dens = bellDensity(pos);
				float decayHere = decay*dens;
				//lg("decayHere="+decayHere);
				//if(decayHere < 0 || 1 < decayHere) throw new Err("decayHere="+decayHere);
				decayHere = Math.max(0, Math.min(decayHere, 1));
				val[i] = val[i]*(1-decayHere) + decayHere*value;
				if(val != val){
					throw new Err("nan");
				}
			}
		}
		return this;
	}
	
	protected float relPosToAbsolutePos(float r){
		return r<center ? (r-relPos[0])/(center-relPos[0])-1 : (r-center)/(relPos[relPos.length-1]-center);
	}

	public float dev(){ return dev; }

	public Tape dev(float newDev){ dev = newDev; return this; }

	/** WARNING: Because of scaling, like interest rates, move(x).move(-x) leaves you at slightly different position */
	public Tape move(float offset){
		if(offset < -.5 || .5 < offset) throw new Err("offset magnitude too big "+offset);
		center += offset*(offset<0 ? center-relPos[0] : relPos[relPos.length-1]-center);
		if(center != center){
			throw new Err("nan");
		}
		//lg("center="+center);
		
		float fraction = (center-relPos[0])/(relPos[relPos.length-1]-relPos[0]);
		if(fraction < .3f || .7f < fraction){
			moveCenterTo0ByStretch();
			adjustResolutionOfSparsePoints();
		}
		
		return this;
	}
	
	protected void moveCenterTo0ByStretch(){
		float lowRange = center-relPos[0], highRange = relPos[relPos.length-1]-center;
		for(int i=0; i<relPos.length; i++){
			if(relPos[i] < center){
				relPos[i] = -1 + (relPos[i]-relPos[0])/lowRange; //put into range -1 to 0
			}else{
				relPos[i] = (relPos[i]-center)/highRange; //put into range 0 to 1
			}
		}
		center = 0;
	}
	
	/** removes sparse points that are too dense, and adds points between those too far apart. */ 
	protected void adjustResolutionOfSparsePoints(){
		//FIXME dont use readPoint for this cuz it blurs the data by nonaligned linear interpolation
		int points = 100;
		float[] newRelPos = new float[points], newVal = new float[points];
		for(int i=0; i<points; i++){
			float pos = (float)i/(points-1)*2f-1;
			newRelPos[i] = pos;
			newVal[i] = readPoint(pos);
		}
		relPos = newRelPos;
		val = newVal;
	}
	
	public float readPoint(float position){
		float rpos = center+position*(position<0 ? center-relPos[0] : relPos[relPos.length-1]-center); 
		if(position <= relPos[0]) return val[0];
		if(relPos[relPos.length-1] <= position) return val[relPos.length-1];
		int i = Arrays.binarySearch(relPos, rpos);
		if(0 <= i) return val[i];
		int highIndex = -i-1;
		int lowIndex = highIndex-1;
		float between = relPos[highIndex]-relPos[lowIndex]; //relBetween2AdjacentPositions
		float fractionLow = (relPos[highIndex]-rpos)/between;
		return val[lowIndex]*fractionLow + (1-fractionLow)*val[highIndex];
	}
	
	/** Area under the curve is 1, so the bigger the dev the smaller the density at each point */
	protected float bellDensity(float position){
		return (float)BellMath.bellHeight(position/dev)/dev;
	}
	
	public float[] sparsePositions(){
		float[] f = new float[relPos.length];
		float lowRange = center-relPos[0], highRange = relPos[relPos.length-1]-center;
		for(int i=0; i<relPos.length; i++){
			f[i] = relPos[i]<center ? (relPos[i]-relPos[0])/lowRange-1 : (relPos[i]-center)/highRange;
		}
		return f;
	}

}
