/** Ben F Rayfield offsers this software opensource GNU GPL 2+ */
package smoothtapenet.ui;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

import javax.swing.JFrame;
import javax.swing.JPanel;

import humanaicore.common.MathUtil;
import humanaicore.common.Rand;
import humanaicore.common.ScreenUtil;
import humanaicore.common.Time;
import humanaicore.err.Todo;
import smoothtapenet.EdgeType;
import smoothtapenet.Tape;
import smoothtapenet.TapeNet;
import smoothtapenet.impl.StretchTape;

/** OLD: Edits a stretchtape using mouse left/right for velocity
and mouse up/down for (TODO which is it?) value or decay.
<br><br>
UPDATE: tapenetGrid2dWithTapeWavesOnTopAndLeft
<br><br>
Try it at first with random weights, and adjust them manually, to get a feel
for how this kind of ai works. This intuition will later help me customize
oppvec andOr contrastiveDivergence andOr other learning algorithms for it.
<br><br>
and color the diagonal so can see where self to self is.
<br><br>
Each square will have 4 smaller squares as a 2x2, of different colors.
These are the edges for move, writeVal, writeDecay, and stdDev.
These will be editable with mouse to raise or lower them like a paint program,
and maybe other buttons to help choose which of the 4 edgeTypes you're
raising/lowering with 2 mouse buttons.
<br><br>
Will see the waves changing amplitude in middle (writeVal and writeDecay and stdDev)
and scaling (move), shrinking/enlarging their 2 sides,
*/
public class TapeNetEditor extends JPanel implements MouseMotionListener{
	
	public final TapeNet tapenet;
	
	public TapeNetEditor(TapeNet tapenet){
		setLayout(new GridLayout(0,1));
		for(int i=0; i<tapenet.nodes.length; i++){
			add(new TapeEditor(tapenet.nodes[i], false, this));
		}
		this.tapenet = tapenet;
	}

	public void mouseDragged(MouseEvent e){
		mouseMoved(e);
	}

	public void mouseMoved(MouseEvent e){
		throw new Todo();
	}
	
	public static void main(String[] args){
		JFrame window = new JFrame(TapeNetEditor.class.getName());
		boolean vertical = true;
		Map<EdgeType,DoubleUnaryOperator> ops = new HashMap();
		ops.put(EdgeType.move, (weightedSum)->(MathUtil.sigmoid(weightedSum)*2-1)*.7);
		ops.put(EdgeType.writeVal, (weightedSum)->MathUtil.sigmoid(weightedSum)*2-1);
		ops.put(EdgeType.writeDecay, (weightedSum)->MathUtil.sigmoid(weightedSum));
		ops.put(EdgeType.stdDev, (weightedSum)->MathUtil.sigmoid(weightedSum)*.1+.05);
		int size = 8;
		final TapeNet tapenet = new TapeNet(size, ops);
		float newWeightAve = 0;
		float newWeightDev = (float)Math.sqrt(size);
		for(int edgeType=0; edgeType<tapenet.edges.length; edgeType++){
			for(int i=0; i<size; i++){
				for(int j=0; j<size; j++){
					tapenet.edges[edgeType][i][j] = newWeightAve + newWeightDev*(float)Rand.strongRand.nextGaussian();
				}
			}
		}
		window.add(new TapeNetEditor(tapenet));
		if(vertical){
			window.setSize(200, 700);
		}else{
			window.setSize(700, 200);
		}
		ScreenUtil.moveToScreenCenter(window);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
		new Thread(){
			public void run(){
				while(true){
					float dt = .03f;
					tapenet.nextState(dt);
					Time.sleepNoThrow(dt);
					window.repaint();
				}
			}
		}.start();
	}

}
