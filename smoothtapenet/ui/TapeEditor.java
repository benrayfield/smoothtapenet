/** Ben F Rayfield offsers this software opensource GNU GPL 2+ */
package smoothtapenet.ui;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;

import humanaicore.common.ScreenUtil;
import humanaicore.common.Time;
import humanaicore.err.Todo;
import smoothtapenet.Tape;
import smoothtapenet.impl.StretchTape;

/** A horizontal or vertical view of a Tape.
For each scalar in range -1 to 1, an scalar value seen as height (if horizontal).
TODO editor, start with just display.
*/
public class TapeEditor extends JPanel implements MouseListener, MouseMotionListener{
	
	public final Tape tape;
	
	public final boolean vertical;
	
	protected float mouseA, mouseB;
	
	protected float maxMovePerSecond = 1.7f;
	
	protected float maxWritePerSecond = .8f;
	
	protected double lastEvent = Time.time();
	
	public final Set<Integer> mouseButtonsDown = new HashSet();
	
	public final Object lock;
	
	public TapeEditor(Tape tape, boolean vertical, Object lock){
		this.tape = tape;
		this.vertical = vertical;
		setBackground(Color.black);
		setForeground(Color.white);
		this.lock = lock;
		addMouseMotionListener(this);
		addMouseListener(this);
	}
	
	public void paint(Graphics g){
		synchronized(lock){
			int w = getWidth(), h = getHeight();
			g.setColor(getBackground());
			g.fillRect(0, 0, w, h);
			g.setColor(new Color(0,.5f,0));
			float[] pos = tape.sparsePositions();
			if(vertical){
				//TODO how to avoid duplicate code? I dont want to use an AffineTransform. I could swap x and y indexs.
				int devPixels = (int)(.5+tape.dev()*h/2); //round
				g.drawLine(0, h/2-devPixels, w, h/2-devPixels);
				g.drawLine(0, h/2+devPixels, w, h/2+devPixels);
				g.setColor(getForeground());
				for(int i=0; i<pos.length-1; i++){
					float val0 = tape.readPoint(pos[i]); //range -1 to 1
					float val1 = tape.readPoint(pos[i+1]);
					g.drawLine(
						(int)((.5f+.5f*val0)*w), //x0
						(int)((.5f+.5f*pos[i])*h), //y0
						(int)((.5f+.5f*val1)*w), //x1
						(int)((.5f+.5f*pos[i+1])*h) //y1
					);
				}
			}else{
				//TODO how to avoid duplicate code? I dont want to use an AffineTransform. I could swap x and y indexs.
				int devPixels = (int)(.5+tape.dev()*w/2); //round
				g.drawLine(w/2-devPixels, 0, w/2-devPixels, h);
				g.drawLine(w/2+devPixels, 0, w/2+devPixels, h);
				g.setColor(getForeground());
				for(int i=0; i<pos.length-1; i++){
					float val0 = tape.readPoint(pos[i]); //range -1 to 1
					float val1 = tape.readPoint(pos[i+1]);
					g.drawLine(
						(int)((.5f+.5f*pos[i])*w), //x0
						(int)((.5f+.5f*val0)*h), //y0
						(int)((.5f+.5f*pos[i+1])*w), //x1
						(int)((.5f+.5f*val1)*h) //y1
					);
				}
			}
		}
	}

	public void mouseDragged(MouseEvent e){
		mouseMoved(e);
	}

	public void mouseMoved(MouseEvent e){
		mouseA = 2f*e.getX()/getWidth()-1;
		mouseB = 2f*e.getY()/getHeight()-1;
		if(vertical){
			float temp = mouseA;
			mouseA = mouseB;
			mouseB = temp;
		}
		event();
	}
	
	protected void event(){
		synchronized(lock){
			double now = Time.time();
			double dt = Math.max(0, Math.min(now-lastEvent, .1));
			lastEvent = now;
			tape.move(-(float)dt*maxMovePerSecond*mouseA);
			int direction = 0;
			if(mouseButtonsDown.contains(MouseEvent.BUTTON1)) direction++;
			if(mouseButtonsDown.contains(MouseEvent.BUTTON3)) direction--;
			float writeValue = mouseB*direction;
			float writeDecay = Math.abs(direction)*(float)dt*maxWritePerSecond;
			tape.write(writeValue, writeDecay);
		}
		repaint();
	}

	public void mouseClicked(MouseEvent e){}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}

	public void mousePressed(MouseEvent e){
		mouseButtonsDown.add(e.getButton());
		event();
	}

	public void mouseReleased(MouseEvent e){
		mouseButtonsDown.remove(e.getButton());
		event();
	}
	
	public static void main(String[] args){
		JFrame window = new JFrame(TapeEditor.class.getName());
		boolean vertical = true;
		window.add(new TapeEditor(new StretchTape(-.5f, .5f, .03f),vertical, new Object()));
		if(vertical){
			window.setSize(200, 700);
		}else{
			window.setSize(700, 200);
		}
		ScreenUtil.moveToScreenCenter(window);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
	}	

}
