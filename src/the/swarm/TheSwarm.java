package the.swarm;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
 
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
 
import com.jogamp.opengl.util.Animator;
 
public class TheSwarm implements GLEventListener, KeyListener, MouseListener {
    float rotateT = 0.0f;
    static GLU glu = new GLU();
    static GLCanvas canvas = new GLCanvas();
    static Frame frame = new Frame("Swarm Game");
    
    // config properties
    static int FOLLOWERS = 200;
    
    // FIXME: just slapping these guys here? 
    private Swarmer leader;
    private Swarmer leaderTarget;
    private Swarmer modifiedTarget;
    private Swarmer[] followers;
    
    private StaticPoint[] leaderTrace = new StaticPoint[1000];
    private int leaderTraceIndex = 0;
    private StaticPoint[] followersTrace = new StaticPoint[30000];
    private int followersTraceIndex = 0;
    
    private boolean tracing = false; // FIXME: move this state somewhere else
    
    private int frameCounter = 0;
    
    static Animator animator = new Animator(canvas);
 
    public void display(GLAutoDrawable gLDrawable) {
        final GL2 gl = gLDrawable.getGL().getGL2();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();
		gl.glTranslatef(0.0f, 0.0f, -10.0f); // move camera back by 5

		// rotate on the three axis
		//gl.glRotatef(rotateT, 1.0f, 0.0f, 0.0f);
		//gl.glRotatef(rotateT, 0.0f, 1.0f, 0.0f);
		//gl.glRotatef(rotateT, 0.0f, 0.0f, 1.0f);

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
		if(followers != null) {
			for(Swarmer s : followers) {
				s.draw(gl);
			}
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

		// increasing rotation for the next iteration                                 
        //rotateT += 0.2f; 
		
		// RECALC POSITIONS
		// Check if leader is near target, within some threshold of distance
		float targetThreshold = 0.1f;
		Vector3f dist = leader.pos.multiply(-1).add(leaderTarget.pos);
		if(Math.abs(dist.x) <= targetThreshold && Math.abs(dist.y) <= targetThreshold && Math.abs(dist.z) <= targetThreshold)
			this.leaderTarget = new Swarmer(Vector3f.random(), 0.0f);
		
		// Accelerate leader towards target
		//System.out.println("leader.vec: " + leader.vec);
		modifiedTarget.pos = leaderTarget.pos.add(leader.vec.multiply(-200));
		Vector3f accelDelta = leader.pos.accelerate(modifiedTarget.pos, leader.accel);
		//System.out.println("accelDelta: " + accelDelta);
		leader.vec = leader.vec.add(accelDelta);
		leader.pos = leader.pos.add(leader.vec);
		if(tracing && frameCounter % 10 == 0) {
			if(leaderTraceIndex < leaderTrace.length) {
				leaderTrace[leaderTraceIndex] = leader.pos.toStaticPoint();
				leaderTraceIndex++;
			}
		}
		
		// Accelerate the followers towards the leader
		if(followers != null) {
			for(Swarmer s : followers) {
				accelDelta = s.pos.accelerate(leader.pos, s.accel);
				s.vec = s.vec.add(accelDelta);
				s.pos = s.pos.add(s.vec);
				if(tracing && frameCounter % 10 == 0) {
					if(followersTraceIndex < followersTrace.length) {
						followersTrace[followersTraceIndex] = s.pos.toStaticPoint();
						followersTraceIndex++;
					}
				}
			}
		}
		
		frameCounter++;
    }
 
    public void displayChanged(GLAutoDrawable gLDrawable, boolean modeChanged, boolean deviceChanged) {
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
        
        // Create initial vectors and stuff
        this.leader = new Swarmer(Vector3f.random(), 0.00001f);
        this.leaderTarget = new Swarmer(Vector3f.random(), 0.0f);
        this.modifiedTarget = new Swarmer(leaderTarget.pos.multiply(1), 0.0f);
        this.followers = new Swarmer[FOLLOWERS];
        for(int i = 0; i < FOLLOWERS; i++) {
        	this.followers[i] = new Swarmer(Vector3f.random(), 0.000008f);
        }
    }
 
    public void reshape(GLAutoDrawable gLDrawable, int x, int y, int width, int height) {
        GL2 gl = gLDrawable.getGL().getGL2();
        if (height <= 0) {
            height = 1;
        }
        float h = (float) width / (float) height;
        gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(50.0f, h, 1.0, 1000.0);
        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
        gl.glLoadIdentity();
    }
 
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            exit();
        }
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            this.tracing = true;
        }
    }
 
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            this.tracing = false;
            this.leaderTraceIndex = 0;
            this.followersTraceIndex = 0;
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
        // do nothing
    }

    
    
    
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
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
}