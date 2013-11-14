package the.swarm;

import the.swarm.gfx.StaticPoint;

public class Vector3f {
    static public final float MAX_XYZ = 3;
    
    // Some useful Vector constants
    public static Vector3f ZERO = new Vector3f(0,0,0);
    public static Vector3f UNIT_X = new Vector3f(1,0,0);
    public static Vector3f UNIT_Y = new Vector3f(0,1,0);
    public static Vector3f UNIT_Z = new Vector3f(0,0,1);

	
	public Vector3f(float _x, float _y, float _z) {
		x = _x;
		y = _y; 
		z = _z;
	}
	public float x;
	public float y;
	public float z;
	
	static public Vector3f random() {
		return new Vector3f(MAX_XYZ-(float)Math.random()*(MAX_XYZ*2.0f),MAX_XYZ-(float)Math.random()*(MAX_XYZ*2.0f),0);
	}

	
	public Vector3f add(final Vector3f _v) {
		return new Vector3f(this.x+_v.x, this.y+_v.y, this.z+_v.z);
	}

	public Vector3f multiply(float f) {
		return new Vector3f(this.x*f, this.y*f, this.z*f);
	}



	/**
	 * Find a unit vector for this vector
	 * @return
	 */
	public Vector3f unit() {
		float norm = this.length();
		
		if(norm != 0)
			return new Vector3f(this.x/norm, this.y/norm, this.z/norm);
		else
			return Vector3f.ZERO;
	}

	private float length(){
		double normSq = this.x*this.x + this.y*this.y + this.z*this.z;
		return (float)Math.sqrt(normSq);
	}

	

	public Vector3f accelerate(final Vector3f target, float accel) {
		if(target == null || target == this)
			return Vector3f.ZERO;
		
		Vector3f delta = this.multiply(-1).add(target).unit();
		//System.out.println("delta: " + delta);
		
		return delta.multiply(accel);
	}
	

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "" + this.x + ", " + this.y + ", " + this.z + "";
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object _v) {
		Vector3f v = (Vector3f) _v;
		return this.x == v.x && this.y == v.y && this.z == v.z;
	}


	public StaticPoint toStaticPoint() {
		return new StaticPoint(this.x, this.y, this.z);
	}
}
