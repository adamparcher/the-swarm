package the.swarm.gfx;

import javax.media.opengl.GL2;

import the.swarm.Vector3f;

public class StaticPoint extends Vector3f implements Drawable {

	public StaticPoint(float _x, float _y, float _z) {
		super(_x, _y, _z);
	}

	@Override
	public void draw(GL2 gl) {
		if(gl != null)
			gl.glVertex3f(this.x, this.y, this.z);
	}

}
