package the.swarm;

import java.awt.Canvas;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL2;

public class MouseHandler implements MouseListener {

	
	TheSwarm theSwarm;
	public int x;
	public int y;
	
	private static MouseHandler theHandler;
	
	private MouseHandler(TheSwarm s) {
		this.theSwarm = s;
	}
	
	public static void init(TheSwarm s) {
		MouseHandler.theHandler = new MouseHandler(s);
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		Point p = e.getPoint();
		

		// All following code is to unproject the mouse click coordinates back into the 3d coordinates
		// position_near is a point at z = 0
		// position_far is a point at z = 1
		// Together they can be used as a vector representing the ray traced by the mouse click, if needed.
		float win_x = p.x;
		float win_y = p.y;
		
		this.updatePosition(p);
		
		Canvas canvas = this.theSwarm.getCanvas();

		FloatBuffer position_near = FloatBuffer.allocate(3);
		FloatBuffer position_far = FloatBuffer.allocate(3);
		FloatBuffer modelview = theSwarm.getModelview();
		FloatBuffer projection = theSwarm.getProjection();
		IntBuffer viewport = theSwarm.getViewport();

		GL2 gl = theSwarm.getGL2();
		boolean result1 = this.theSwarm.getGLU().gluUnProject(win_x, canvas.getHeight()-win_y, 0f, modelview, projection, viewport, position_near);
		boolean result2 = this.theSwarm.getGLU().gluUnProject(win_x, canvas.getHeight()-win_y, 1f, modelview, projection, viewport, position_far);

		float dx = position_far.get(0)-position_near.get(0);
		float dy = position_far.get(1)-position_near.get(1);
		float dz = position_far.get(2)-position_near.get(2);
		float multiplier = (0-position_near.get(2))/dz;
		float new_x = (dx * multiplier) + position_near.get(0);
		float new_y = (dy * multiplier) + position_near.get(1);
		// Reset the leader target to the place the mouse was clicked, assuming z = 0
		theSwarm.setLeaderTarget(new Swarmer(new Vector3f(new_x, new_y, 0), 0));
		
		
		
//		// This is just junk leftover below here
//		
//		if ((e.getModifiers() & shoot) != 0)
//
//			;// game.setShoot();
//
//		if ((e.getModifiers() & use) != 0)
//
//			;// game.setUse();
	}

	private void updatePosition(Point p) {
		this.x = p.x;
		this.y = p.y;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public static MouseHandler getHandler() {
		return MouseHandler.theHandler;
	}
}
