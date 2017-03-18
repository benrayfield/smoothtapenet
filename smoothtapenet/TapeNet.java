/** Ben F Rayfield offsers this software opensource GNU GPL 2+ */
package smoothtapenet;

import java.util.Arrays;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

import humanaicore.common.MathUtil;
import humanaicore.err.Err;
import humanaicore.err.Todo;
import smoothtapenet.impl.StretchTape;

/** A fullyConnected directed network between Tapes (nodes are each a smooth turingTape)
with 4 float32 edge types (move, writeVal, writeDecay, stdDev) between each ordered pair of nodes.
Some of those edge types may be held to symmetric.
*/
public class TapeNet{
	
	//TODO considerScalarBifractionNodesWithOnlyNegativeWeightsToAvoidOverfittingSinceItsEqxorlike
	
	public final Tape[] nodes;
	
	public float[][] nodeVars;
	
	/** edges[EdgeType][toNode][fromNode] */
	public final float[][][] edges;
	
	/** For each EdgeType, you start with a fraction, then multiply it, then add to it *
	public final float[] mult, add;
	*/
	public final Map<EdgeType,DoubleUnaryOperator> weightedSumToNodeVar;
	
	public TapeNet(int size, Map<EdgeType,DoubleUnaryOperator> weightedSumToNodeVar){
		this(newTapes(size), weightedSumToNodeVar);
	}
	
	/** weightedSumToNodeVar DoubleUnaryOperator should be
	some scaling (possibly nonlinear) of sigmoid of weightedSum param
	*/
	public TapeNet(Tape[] nodes, Map<EdgeType,DoubleUnaryOperator> weightedSumToNodeVar){
		this.nodes = nodes;
		edges = new float[EdgeType.values().length][nodes.length][nodes.length];
		nodeVars = new float[edges.length][nodes.length];
		//FIXME what mults and adds? Example, move needs to be small and half the time negative
		/*mult = new float[edges.length];
		Arrays.fill(mult, 1);
		add = new float[edges.length];
		*/
		this.weightedSumToNodeVar = weightedSumToNodeVar;
	}
	
	/** input and output of visibleNodes.
	FIXME Maybe this isnt the right API to describe visibleNodes, but the bigger problem is how the tapenet should
	know the other nodes are meant for learning these more important nodes?
	FIXME TODO And we should probably do aidreamApi which means multiple cycles (ended by some var being negative)
	with max possible cycles per call, so the turingCompleteness has time to think,
	or maybe like I found in oppvec mouseai those multiple cycles happen on their own while mouse changes
	only a little at a time and it builds up learning. In tapenet it will also need to build up thinking/predicting.
	*/
	public void io(float[] visibleNodes, float cycles){
		nextState(cycles);
		throw new Todo();
	}
	
	/** Time is continuous, so cycles is normally far less than 1.
	writeDecay and move amount are multiplied by cycles, but
	TODO will that cause problems with going back and forth many small times
	having a different bellcurve of where position ends than fewer big times?
	It probably would, but I'm not sure what to do about it other than
	to try to use approx the same change in time each call
	like a timer that activates approximately on an interval.
	*/
	public void nextState(float cycles){
		//TODO use ForkJoinPool to thread this
		for(int edgeType=0; edgeType<edges.length; edgeType++){
			Arrays.fill(nodeVars[edgeType], 0f);
		}
		float[] read = new float[nodes.length];
		for(int i=0; i<nodes.length; i++){
			read[i] = nodes[i].read();
		}
		for(int edgeType=0; edgeType<edges.length; edgeType++){
			for(int i=0; i<nodes.length; i++){
				for(int j=0; j<nodes.length; j++){
					nodeVars[edgeType][i] += read[j]*edges[edgeType][i][j];
					if(nodeVars[edgeType][i] != nodeVars[edgeType][i]){
						throw new Err("nan");
					}
				}
			}
		}
		EdgeType[] types = EdgeType.values();
		for(int edgeType=0; edgeType<edges.length; edgeType++){
			DoubleUnaryOperator op = weightedSumToNodeVar.get(types[edgeType]);
			for(int i=0; i<nodes.length; i++){
				//nodeVars[edgeType][i] = add[edgeType] + mult[edgeType]*(float)MathUtil.sigmoid(nodeVars[edgeType][i]);
				nodeVars[edgeType][i] = (float) op.applyAsDouble(nodeVars[edgeType][i]);
				if(nodeVars[edgeType][i] != nodeVars[edgeType][i]){
					throw new Err("nan");
				}
			}
		}
		for(int i=0; i<nodes.length; i++){
			float move = nodeVars[EdgeType.move.ordinal()][i]*cycles;
			float writeVal = nodeVars[EdgeType.writeVal.ordinal()][i];
			float writeDecay = nodeVars[EdgeType.writeDecay.ordinal()][i]*cycles;
			float stdDev = nodeVars[EdgeType.stdDev.ordinal()][i];
			nodes[i].dev(stdDev).write(writeVal,writeDecay).move(move);
		}
	}
	
	public void learn(float learnRate){
		throw new Todo("TODO contrastiveDivergence oppvec? Probably learnrandboltz was overfitting cuz of lack of oppvec, not cuz of fullyConnected.");
	}
	
	static Tape[] newTapes(int size){
		//TODO do this with Stream and lambda
		Tape[] t = new Tape[size];
		for(int i=0; i<size; i++) t[i] = new StretchTape(.5f, .5f, .03f);
		return t;
	}

}
