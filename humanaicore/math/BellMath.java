/** Ben F Rayfield offers HumanAiCore opensource GNU LGPL */
package humanaicore.math;
import static humanaicore.common.CommonFuncs.*;

public class BellMath{
	private BellMath(){}
	
	static double stdDevSqCache[];
	
	/** Multiply this by zbell for each dimension of bell curve,
	or use functions which take a dimension parameter.
	Zbell is height 1 and has area bellMult.
	*/
	public static final double bellMult = 1 / Math.sqrt(2 * Math.PI);
	
	/** A zero dimensional bell curve is height 1 at position 0. Each unit of dimension,
	and it need not be in whole numbers, past that, multiplies by 1/sqrt(2*pi).
	*/
	public static double zbellHeight(double stdDev){
		return Math.exp(-.5*stdDev*stdDev);
	}
	
	public static double bellHeight(double stdDev){
		return zbellHeight(stdDev)*bellMult;
	}
	
	public static double bellHeight(double stdDev, double dimensions){
		return zbellHeight(stdDev)*Math.pow(bellMult, dimensions);
	}
	
	/** To avoid rolling over to infinity */
	public static final double MAX_DISTANCE = 1e100;
	
	/** From dot product between unit vectors (from hypersphere center to surface)
	to distance in bell curve coordinates in flat space tangent to that part of hypersphere
	<br><br>
	TODO This is the calculation needing the most research, in how the Riemann Hypersphere
	overlaps itself as the tangent plane views different vectors as bell curves
	seeing eachother in flat space. This may need to include Amount Of Dimension of both vectors.
	*/
	public static double dotToDistance(double dot){
		dot = Math.abs(dot);
		if(dot == 0) return MAX_DISTANCE;
		if(dot < -1 || dot > 1) throw new RuntimeException("Dot out of range: "+dot);
		double tangentInHypersphere = Math.sqrt(1 - dot*dot); //also happens to be the inverse Lorentz Factor
		double tangentToSurface = tangentInHypersphere/dot; //scale the triangle
		return Math.min(MAX_DISTANCE, tangentToSurface); //TODO Is this the right thing to return? See TODO comment of this function.
	}
	
	/** A "bifraction" is a number in range -1 to 1. "fraction" is 0 to 1.
	<br><br>
	bifractionToStdDev(-1) is negative and very big.
	bifractionToStdDev(0) is 0. Negative is symmetric to positive.
	bifractionToStdDev(.68) is near 1
	because around 68% of things are within 1 standard deviation.
	bifractionToStdDev(.95) is near 2.
	bifractionToStdDev(.997) is near 3.
	bifractionToStdDev(1) is very big.
	<br><br>
	Technicly, the ends -1 and 1 should be infinite, but using -Infinity
	and Infinity makes the code more complex than its worth.
	I'm using very accurate linear interpolations. Its approximate.
	*/
	public static double bifractionToStdDev(double bifraction){
		//TODO optimize by not calling synchronized initBellCurveInterpolaters here, even though it only does something the first time
		initBellCurveInterpolaters(); //only the first time
		boolean wasNeg = bifraction < 0;
		if(wasNeg) bifraction = -bifraction;
		double stdDev = bifractionToStdDev_interpolater.interpolate(bifraction);
		if(wasNeg) stdDev = -stdDev;
		return stdDev;
	};

	public static double stdDevToBifraction(double stdDev){
		//TODO optimize by not calling synchronized initBellCurveInterpolaters here, even though it only does something the first time
		initBellCurveInterpolaters(); //only the first time
		boolean wasNeg = stdDev < 0;
		if(wasNeg) stdDev = -stdDev;
		double bifraction = stdDevToBifraction_interpolater.interpolate(stdDev);
		if(wasNeg) bifraction = -bifraction;
		return bifraction;
	};
	
	/** Given the chance you are somewhere on a bell curve, what part are you on?
	zbellHeight ranges 1 (bell center) to 0 (infinite distance, excluded from parameter range).
	If you want it for a 1d or some other dimension of bell curve, use bellHeightToStdDev.
	<br><br>
	TODO Use cached linear interpolation in an approximation function
	*/
	public static double zbellHeightToStdDev(double zbellHeight){
		if(zbellHeight <= 0 || zbellHeight > 1) throw new ArithmeticException("zbellHeight="+zbellHeight);
		//zbellHeight is e^(-.5*stdDev*stdDev);
		//log(zbellHeight) = -.5*stdDev*stdDev
		return Math.sqrt(-2*Math.log(zbellHeight));
	}
	
	public static double bellHeightToStdDev(double bellHeight, double dimensions){
		return zbellHeightToStdDev(bellHeight*Math.pow(bellMult, dimensions));
	}
	
	/** This is the simple way to normalize to a bell-curve.
	The nlmi.math.bifractionToStdDev and nlmi.math.stdDevToBifraction functions allow extremely more detailed control.
	<br><br>
	Modifies the array.
	If all numbers in the array equal, does nothing.
	*/
	public static void bellCurveNormalize(double array[], double targetAve, double targetStdDev){
		double sum = 0;
		for(int i=0; i<array.length; i++) sum += array[i];
		double ave = sum / array.length;
		double sumOfSquares = 0;
		for(int i=0; i<array.length; i++){
			double diff = array[i] - ave;
			sumOfSquares += diff*diff;
		}
		double stdDev = Math.sqrt(sumOfSquares/array.length);
		//alert('ave='+ave+' stdDev='+stdDev+' targetAve='+targetAve+' targetStdDev='+targetStdDev+' array='+array);
		if(stdDev == 0) return; //all numbers are equal. stdDev does not make sense.
		for(int i=0; i<array.length; i++){
			double normed = (array[i] - ave) / stdDev; //average 0, stdDev 1
			array[i] = normed*targetStdDev + targetAve;
			//TODO fix code in nlmi with similar bug that I just fixed here: (normed + targetAve) * targetStdDev.
		}
	}

	private static void testInterpolate_interpObject_x_ymin_ymax(
			LinearInterpolate1Var interpObject, double x, double ymin, double ymax){
		String s = "testing interpolate. x="+x+" and y must be in range "+ymin+" to "+ymax;
		System.out.println(s);
		double y = interpObject.interpolate(x);
		s += ". y="+y;
		System.out.println(s);
		if(y < ymin || ymax < y) throw new RuntimeException(s+" interpObject="+interpObject);
	};

	private static void testInterpolate(){
		double x[] = new double[]{ //must be sorted.
			-5.75, //0
			-2, //1
			-.000025, //2
			0, //3
			0, //4
			7, //5
			7.00125, //6
			7.0015, //7
			1000000000, //8
			1000000000.5 //9
		};
		double y[] = new double[]{ //does not have to be sorted.
			1, //0
			2, //1
			4, //2
			8, //3
			16, //4
			32, //5
			64000, //6
			128, //7
			256, //8
			512000 //9
		};
		LinearInterpolate1Var interp = new LinearInterpolate1Var(x, y);
		double ep = .000001; //epsilon
		testInterpolate_interpObject_x_ymin_ymax(interp, -5.75, 1, 1);
		testInterpolate_interpObject_x_ymin_ymax(interp, 1000000000.5, 512000, 512000);
		testInterpolate_interpObject_x_ymin_ymax(interp, 2000000000.19, 512000, 512000);
		testInterpolate_interpObject_x_ymin_ymax(interp, -2, 2, 2);
		testInterpolate_interpObject_x_ymin_ymax(interp, 7.00125, 64000, 64000);
		testInterpolate_interpObject_x_ymin_ymax(interp, 7.0015, 128, 128);
		testInterpolate_interpObject_x_ymin_ymax(interp, 1000000000, 256, 256);
		double yy = 1 + 1.2/3.75*(2-1);
		testInterpolate_interpObject_x_ymin_ymax(interp, -5.75+1.2, yy-ep, yy+ep);
		yy = (256+512000)/2;
		testInterpolate_interpObject_x_ymin_ymax(interp, 1000000000.25, yy-ep, yy+ep);
		ep = .2; //roundoff error is more when multiplying big numbers.
		yy = 256./3 + 512000.*2/3;
		testInterpolate_interpObject_x_ymin_ymax(interp, 1000000000 + 1./3, yy-ep, yy+ep);
		ep = .01;
		yy = 64000*.04 + 128*.96; //7.00125 (.00024) 7.00149 (.00001) 7.0015
		testInterpolate_interpObject_x_ymin_ymax(interp, 7.00149, yy-ep, yy+ep);
		//TODO choose what to do when there are 2 equal numbers, like 0-->8 then 0-->16.
		testInterpolate_interpObject_x_ymin_ymax(interp, 0, 8, 16); //TODO
		yy = 16.*6/7 + 32./7;
		testInterpolate_interpObject_x_ymin_ymax(interp, 1, yy-ep, yy+ep);
		ep = .00001;
		yy = 16.*6999./7000 + 32./7000;
		testInterpolate_interpObject_x_ymin_ymax(interp, .001, yy-ep, yy+ep);
		yy = 16.*(7000-2092)/7000 + 32.*2092./7000;
		testInterpolate_interpObject_x_ymin_ymax(interp, 2.092, yy-ep, yy+ep);
		//TODO test input 0 and similar bugs. There was something that failed with that.
	};

	/** interpolates from range [0 to 1] to range [0 to very large number] */
	private static volatile LinearInterpolate1Var bifractionToStdDev_interpolater = null;

	/** interpolates from range [0 to very large number] to range [0 to 1] */
	private static volatile LinearInterpolate1Var stdDevToBifraction_interpolater = null;
	

	private synchronized static void initBellCurveInterpolaters(){
		if(bifractionToStdDev_interpolater == null){
			
			

			
			
			//Test the interpolator code, independent of bell curves or statistics.
			testInterpolate();
		
			
			
			//Create it only once (and test it), until you restart this software.
			//int accuracy = 20000; //TODO how many should this be? Some browsers kill slow scripts.
			int accuracy = 0x10000; //TODO how many should this be? Some browsers kill slow scripts.
			double randomStdDevs[] = new double[accuracy];
			for(int i=0; i<accuracy; i++){
				//TODO adjust this a little so half of them are less than 3 std devs
				double fraction = 1 - (double)i/(accuracy-1); //decreasing
				double epsilon = 1. / 100000;
				//fraction^2 works a little better than ^1 or ^3.5. Its approximate.
				randomStdDevs[i] = (1+epsilon)/(fraction*fraction+epsilon) - 1;
			}
			//randomStdDevs is sorted ascending, but is not aligned to any statistics.
			//It will be used to generate statistics at those specific standard-deviations.
			double bellCurveHeights[] = new double[accuracy];
			for(int i=0; i<accuracy; i++){
				double stdDev = randomStdDevs[i];
				//alert('stdDevCreating  1.1');
				bellCurveHeights[i] = bellMult*zbellHeight(stdDev);
				//bellCurveHeights[i] = bellCurveHeightAt(stdDev);
			}
			//Area under half a bell curve should be approximately the sum
			//of all randomStdDevs[i] * bellCurveHeights[i]. It should be 1/2.
			double approximateHalfBellCurveArea = 0;
			for(int i=1; i<accuracy; i++){ //TODO start at 0 or 1?
				double widthOf_stdDev = randomStdDevs[i] - randomStdDevs[i-1];
				approximateHalfBellCurveArea += widthOf_stdDev * bellCurveHeights[i];
			}
			if(Math.abs(approximateHalfBellCurveArea - .5) > 1./5000){ //very accurate
				throw new RuntimeException("approximateHalfBellCurveArea should be near 1/2 but is "+approximateHalfBellCurveArea);
			}
			double mult = .5 / approximateHalfBellCurveArea;
			for(int i=0; i<accuracy; i++){ //normalize the approximations to 1/2, a small change.
				bellCurveHeights[i] *= mult;
			}
			double sum = 0;
			//var fractions = [sum];
			double fractions[] = new double[accuracy];
			fractions[0] = sum;
			//range 0 to 1 (maybe 1.00001? If so, fix it.).
			//Its cumulative sums of half bell curve area. The last one should be 1/2.
			for(int i=1; i<accuracy; i++){ //TODO does it ever exceed 1.0 (Or should I have written 1/2?)?
				double width = randomStdDevs[i] - randomStdDevs[i-1];
				double nextPartOfArea = width * bellCurveHeights[i];
				sum += nextPartOfArea;
				fractions[i] = sum;
			}
			if(Math.abs(sum - .5) > 1./5000){ //very accurate
				throw new RuntimeException("bell curve cumulative sum should be near 1/2 but is "+sum);
			}
			//Normalize those cumulative sums to range exactly 0 to 1. A small change.
			for(int i=0; i<accuracy; i++) fractions[i] /= sum;
			//Interpolater object for fractions[] to randomStdDevs[], a half bell curve.
			bifractionToStdDev_interpolater = new LinearInterpolate1Var(fractions, randomStdDevs);
			//Interpolater for the opposite direction. Both are fast to use after create them here.
			stdDevToBifraction_interpolater = new LinearInterpolate1Var(randomStdDevs, fractions);
			//Test those 2 interpolaters:
			test_bifractionStdDevInterpolators();
			
			
			testBifractionsAndBellHeightsFormCircle();
		}
	}

	private static void test_bifractionStdDevInterpolators(){
		System.out.println("Testing nlmi.math.bifractionToStdDev and its opposite function.");
		double testBifraction[] = new double[]{-0.9973002, -0.9544997, -0.6826894, 0, 0.6826894, 0.9544997, 0.9973002};
		double testStdDev[] = new double[]{-3, -2, -1, 0, 1, 2, 3};
		//Can test with any stdDev. Whole numbers were not how it was created, so they are ok test cases.
		for(int i=0; i<7; i++){
			double stdDevAnswered = bifractionToStdDev(testBifraction[i]);
			if(Math.abs(stdDevAnswered - testStdDev[i]) > .001){
				throw new RuntimeException("testBifraction["+i+"]="+testBifraction[i]+" answered "+stdDevAnswered+" but expected very close to "+testStdDev[i]);
			}
			double bifractionAnswered = stdDevToBifraction(testStdDev[i]);
			if(Math.abs(bifractionAnswered - testBifraction[i]) > .001){
				throw new RuntimeException("testStdDev["+i+"]="+testStdDev[i]+" answered "+bifractionAnswered+" but expected very close to "+testBifraction[i]);
			}
		}
		System.out.println("Bell Curve math tests pass in test_bifractionStdDevInterpolators");
	};
	
	/** FIXME check bounds */
	public static double estimateBellHeightGivenStdDevSquared(double stdDevSquared){
		if(stdDevSqCache == null){
			stdDevSqCache = new double[0x100000];
			//initBellCurveInterpolaters();
			for(int i=0; i<0x100000; i++){
				double xstdDevSquared = (double)i/0x10000;
				//stdDevSqCache[i] = bellCurveHeightAt(Math.sqrt(xstdDevSquared));
				stdDevSqCache[i] = bellMult*zbellHeight(Math.sqrt(xstdDevSquared));
			}
			double testStdDev = 1.34;
			double estBellHeight = estimateBellHeightGivenStdDevSquared(testStdDev*testStdDev);
			double bellHeight = bellMult*zbellHeight(testStdDev);
			//double bellHeight = bellCurveHeightAt(testStdDev);
			if(Math.abs(estBellHeight-bellHeight) > .000001) throw new RuntimeException("estBellHeight="+estBellHeight+" bell="+bellHeight);
		}
		//initBellCurveInterpolaters(); //only the first time
		double d = stdDevSquared*0x10000;
		int i = (int)d;
		//if(i+1 >= cache.length) return bellCurveHeightAt(Math.sqrt(stdDevSquared));
		double remainder = d-i;
		//return cache[i]*(1-remainder) + remainder*cache[i+1];
		return stdDevSqCache[i]*(1-remainder) + remainder*stdDevSqCache[i+1];
	}
	
	private static void testBifractionsAndBellHeightsFormCircle(){
		lg("Starting testBifractionsAndBellHeightsFormCircle tests.");
		for(double angle=0; angle<2*Math.PI; angle+=.001){
			double bifraction = Math.cos(angle);
			double stdDev = bifractionToStdDev(bifraction);
			double zBellHeight = zbellHeight(stdDev);
			lg("angle="+angle+" bifraction="+bifraction+" stdDev="+stdDev+" zbellHeight="+zBellHeight);
		}
		lg("half of bell curve between plus/minus stdDev "+bifractionToStdDev(.5));
		lg("(TODO finish tests) testBifractionsAndBellHeightsFormCircle tests pass");
	}

}
