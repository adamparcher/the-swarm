package the.swarm;

import java.awt.Canvas;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES1;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.glu.GLU;

import the.swarm.gfx.StaticPoint;
import the.swarm.util.SwarmTimer;

import com.jogamp.newt.event.InputEvent;
import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.awt.TextRenderer;

public class TheSwarm implements GLEventListener, KeyListener {
	float rotateT = 0.0f;
	static GLU glu = new GLU();
	static GLCanvas canvas = new GLCanvas();
	static Frame frame = new Frame("Swarm Game");
	TextRenderer renderer;

	// config properties
	static int FOLLOWERS = 6000;

	// FIXME: just slapping these guys here?
	private Swarmer leader;
	private Swarmer leaderTarget;

	private Swarmer modifiedTarget;
	private Swarmer[] followers;
	private float[] superVectors;

	private StaticPoint[] leaderTrace = new StaticPoint[1000];
	private int leaderTraceIndex = 0;
	private StaticPoint[] followersTrace = new StaticPoint[30000];
	private int followersTraceIndex = 0;

	private boolean tracing = false; // FIXME: move this state somewhere else
	private boolean zoomOut;
	private boolean zoomIn;
	

	float cameraZoom = -10.0f;

	private int frameCounter = 0;
	
	// Stuff for calculating framerate
	private long[] frameTimes = new long[30];
	private boolean frameTimesFilled = false;
	private int frameTimeCounter = 0;
	private long totalFrameTime = 0l;
	private float frameRate;
	private long lastFrameTime;

	static Animator animator = new Animator(canvas);
	
	// Allocate arrays to hold important matrices
	FloatBuffer projection = FloatBuffer.allocate(16);
	FloatBuffer modelview = FloatBuffer.allocate(16);
	IntBuffer viewport = IntBuffer.allocate(16);
	
	
	
	private boolean printedDebugTiming;
	


	public void display(GLAutoDrawable gLDrawable) {
		SwarmTimer.start("display-translate");
		
		final GL2 gl = gLDrawable.getGL().getGL2();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();
		gl.glTranslatef(0.0f, 0.0f, cameraZoom); // move camera back by 5
		gl.glGetFloatv(GLMatrixFunc.GL_MODELVIEW_MATRIX, modelview); // save off a copy of the modelview matrix
		
		// rotate on the three axis
		// gl.glRotatef(rotateT, 1.0f, 0.0f, 0.0f);
		// gl.glRotatef(rotateT, 0.0f, 1.0f, 0.0f);
		// gl.glRotatef(rotateT, 0.0f, 0.0f, 1.0f);

		SwarmTimer.end("display-translate");
		
		SwarmTimer.start("display-points");
		
		// DRAW
		gl.glBegin(GL2.GL_POINTS);
		gl.glColor3f(1.0f, 0.6f, 0.6f); // set leader color to red
		leader.draw(gl);
		if (this.tracing) {
			for (int i = 0; i < leaderTraceIndex; i++) {
				leaderTrace[i].draw(gl);
			}
		}
		gl.glColor3f(0.6f, 0.6f, 1.0f); // color = blue
		
		if (followers != null) {
			for(int i = 0; i < followers.length; i++) {
				this.superVectors[i*3] = followers[i].pos.x;
				this.superVectors[i*3+1] = followers[i].pos.y;
				this.superVectors[i*3+2] = followers[i].pos.z;
			}
			gl.glVertex3fv(this.superVectors, 0);
//			gl.glVertex3fv(FloatBuffer.wrap(this.superVectors));
//			for (Swarmer s : followers) {
//				s.draw(gl);
//				
//			}
		}
		if (this.tracing) {
			for (int i = 0; i < followersTraceIndex; i++) {
				followersTrace[i].draw(gl);
			}
		}
		gl.glColor3f(0.6f, 1.0f, 0.6f); // color = green
		leaderTarget.draw(gl);
		gl.glColor3f(1.0f, 1.0f, 1.0f); // color = white
		modifiedTarget.draw(gl);
		gl.glEnd();

		SwarmTimer.end("display-points");
		
		
		// increasing rotation for the next iteration
		// rotateT += 0.2f;

		
		SwarmTimer.start("display-recalculate-positions");
		// RECALC POSITIONS
		// Check if leader is near target, within some threshold of distance
		float targetThreshold = 0.1f;
		Vector3f dist = leader.pos.multiply(-1).add(leaderTarget.pos);
		// if(Math.abs(dist.x) <= targetThreshold && Math.abs(dist.y) <=
		// targetThreshold && Math.abs(dist.z) <= targetThreshold)
		// this.leaderTarget = new Swarmer(Vector3f.random(), 0.0f);

		// Accelerate leader towards target
		// System.out.println("leader.vec: " + leader.vec);
		modifiedTarget.pos = leaderTarget.pos.add(leader.vec.multiply(-200));
		Vector3f accelDelta = leader.pos.accelerate(modifiedTarget.pos,
				leader.accel);
		// System.out.println("accelDelta: " + accelDelta);
		leader.vec = leader.vec.add(accelDelta);
		leader.pos = leader.pos.add(leader.vec);

		SwarmTimer.end("display-recalculate-positions");
		

		SwarmTimer.start("display-recalculate-positions-swarm");
		// Accelerate the followers towards the leader
		if (followers != null) {
			for (Swarmer s : followers) {
				accelDelta = s.pos.accelerate(leader.pos, s.accel);
				s.vec = s.vec.add(accelDelta);
				s.pos = s.pos.add(s.vec);
				if (tracing && frameCounter % 10 == 0) {
					if (followersTraceIndex < followersTrace.length) {
						followersTrace[followersTraceIndex] = s.pos
								.toStaticPoint();
						followersTraceIndex++;
					}
				}
			}
		}

		SwarmTimer.end("display-recalculate-positions-swarm");

		// Make changes based on Input
		if (tracing && frameCounter % 10 == 0) {
			if (leaderTraceIndex < leaderTrace.length) {
				leaderTrace[leaderTraceIndex] = leader.pos.toStaticPoint();
				leaderTraceIndex++;
			}
		}
		if (zoomIn && cameraZoom < 0f) {
			cameraZoom = cameraZoom * 0.99f;
		} else if (zoomOut) {
			cameraZoom = cameraZoom * 1.01f;

		}


		SwarmTimer.start("display-hud");
	    // HUD Drawing
		renderer.beginRendering(gLDrawable.getWidth(), gLDrawable.getHeight());
		// optionally set the color
		renderer.setColor(1.0f, 0.2f, 0.2f, 0.8f);
		if (MouseHandler.getHandler() != null) {
			renderer.draw("Target: " + leaderTarget.pos, 5, 35);	
		}
		if (MouseHandler.getHandler() != null) {
			renderer.draw("Camera Zoom: " + cameraZoom, 5, 20);	
		}
		if (MouseHandler.getHandler() != null) {
			renderer.draw("Framerate: " + frameRate, 5, 5);	
		}
		
		SwarmTimer.end("display-hud");
		
		// ... more draw commands, color changes, etc.
		renderer.endRendering();

		frameCounter++;
		
		// Calculate Framerate
		totalFrameTime -= frameTimes[frameTimeCounter];
		long currentFrameTime = System.nanoTime();
		frameTimes[frameTimeCounter] = currentFrameTime - lastFrameTime;
		totalFrameTime += frameTimes[frameTimeCounter];
		lastFrameTime = currentFrameTime;
		frameTimeCounter++;
		frameTimeCounter = (frameTimeCounter >= frameTimes.length) ? 0 : frameTimeCounter;
		frameRate = totalFrameTime == 0 ? 0 : 1000000000 / (totalFrameTime / frameTimes.length);
	}

	public void displayChanged(GLAutoDrawable gLDrawable, boolean modeChanged,
			boolean deviceChanged) {
	}

	public void init(GLAutoDrawable gLDrawable) {
		GL2 gl = gLDrawable.getGL().getGL2();
		gl.glShadeModel(GLLightingFunc.GL_SMOOTH);
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glClearDepth(1.0f);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDepthFunc(GL.GL_LEQUAL);
		gl.glHint(GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
		((Component) gLDrawable).addKeyListener(this);
		MouseHandler.init(this);
		((Component) gLDrawable).addMouseListener(MouseHandler.getHandler());

		// Create initial vectors and stuff
		this.leader = new Swarmer(Vector3f.random(), 0.00001f);
		this.leaderTarget = new Swarmer(Vector3f.random(), 0.0f);
		this.modifiedTarget = new Swarmer(leaderTarget.pos.multiply(1), 0.0f);
		this.followers = new Swarmer[FOLLOWERS];
		this.superVectors = new float[FOLLOWERS*3];
		for (int i = 0; i < FOLLOWERS; i++) {
			this.followers[i] = new Swarmer(Vector3f.random(), 0.000008f);
		}
		
		// Set up TextRenderer
		renderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 16));

	}

	public void reshape(GLAutoDrawable gLDrawable, int x, int y, int width,
			int height) {
		SwarmTimer.start("reshape");
		
		GL2 gl = gLDrawable.getGL().getGL2();
		if (height <= 0) {
			height = 1;
		}

		viewport.put(0, width);
		viewport.put(1, height);
		
		float h = (float) width / (float) height;
		gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(50.0f, h, 1.0, 1000.0);
		gl.glGetFloatv(GLMatrixFunc.GL_PROJECTION_MATRIX, projection); // save off a copy of the projection matrix
		gl.glGetIntegerv(gl.GL_VIEWPORT, viewport);
		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		gl.glLoadIdentity();
		
		SwarmTimer.end("reshape");
	}

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			new Thread(new Runnable() {
				public void run() {
					System.exit(0);
				}
			}).start();
		}
		if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			this.tracing = true;
		}

		if (e.getKeyCode() == KeyEvent.VK_X) {
			this.zoomIn = true;
		}
		if (e.getKeyCode() == KeyEvent.VK_Z) {
			this.zoomOut = true;
		}
		

		if (e.getKeyCode() == KeyEvent.VK_0) {
			cameraZoom = -10.0f;
		}
		
		if (e.getKeyCode() == KeyEvent.VK_T) {
			if(!printedDebugTiming) {
				System.out.println(SwarmTimer.getResults());
				printedDebugTiming = true;
			}
			
		}
	}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			this.tracing = false;
			this.leaderTraceIndex = 0;
			this.followersTraceIndex = 0;
		}
		if (e.getKeyCode() == KeyEvent.VK_X) {
			this.zoomIn = false;
		}
		if (e.getKeyCode() == KeyEvent.VK_Z) {
			this.zoomOut = false;
		}

		if (e.getKeyCode() == KeyEvent.VK_T) {
			printedDebugTiming = false;
		}
	}

	public void keyTyped(KeyEvent e) {
	}

	public static void exit() {


		
		animator.stop();
		frame.dispose();
		System.exit(0);
	}

	public static void main(String[] args) {
		canvas.addGLEventListener(new TheSwarm());
		frame.add(canvas);
		frame.setSize(1280, 800);
		frame.setUndecorated(false);
		frame.setExtendedState(Frame.NORMAL);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				
				exit();
			}
		});
		frame.setVisible(true);
		animator.start();
		canvas.requestFocus();
	}

	public void dispose(GLAutoDrawable gLDrawable) {
		// Spit out timings
	}

	public GL2 getGL2() {
		return canvas.getGL().getGL2();
	}

	public GLU getGLU() {
		return glu;
	}

	public Canvas getCanvas() {
		return this.canvas;
	}


	public Swarmer getLeaderTarget() {
		return leaderTarget;
	}
	
	public void setLeaderTarget(Swarmer lt) {
		this.leaderTarget = lt;
	}

	public FloatBuffer getProjection() {
		return projection;
	}

	public FloatBuffer getModelview() {
		return modelview;
	}

	public IntBuffer getViewport() {
		return viewport;
	}

}