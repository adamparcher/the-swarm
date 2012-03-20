package the.swarm;

import javax.media.opengl.GL2;

import the.swarm.gfx.Drawable;

public class Swarmer implements Drawable {
    	public Swarmer(Vector3f _pos, float _accel){
    		this.accel = _accel;
    		this.pos = _pos;
    		this.vec = new Vector3f(0, 0, 0);
    	}
    	public Vector3f pos;
    	public Vector3f vec;
    	public float accel;
    	
		@Override
		public void draw(GL2 gl) {
			if(gl != null)
				gl.glVertex3f(this.pos.x, this.pos.y, this.pos.z);
		}
}
