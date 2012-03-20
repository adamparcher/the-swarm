package the.swarm.test;

import static org.junit.Assert.*;

import org.junit.Test;

import the.swarm.Vector3f;

public class Vector3fTest {

	@Test
	public void testEquals() {
		Vector3f v1 = new Vector3f(1.0f, 2.0f, 3.0f);
		Vector3f v2 = new Vector3f(1.0f, 2.0f, 3.0f);
		Vector3f v3 = new Vector3f(1.0f, 1.0f, 1.0f);
		assertEquals(v1, v2);
		assertFalse("v1 should not equal v3", v1 == v3);
		assertFalse("v2 should not equal v3", v2 == v3);
	}
	
	@Test
	public void testAdd() {
		final Vector3f v1 = new Vector3f(1.0f, 2.0f, 3.0f);
		final Vector3f v2 = new Vector3f(-1.0f, -0.5f, 1.5f);
		
		assertEquals(new Vector3f(2.0f, 4.0f, 6.0f), v1.add(v1));
		assertEquals(new Vector3f(0.0f, 1.5f, 4.5f), v1.add(v2));
	}
	
	@Test
	public void testMultiplyScalar() {
		final Vector3f v1 = new Vector3f(1.0f, 2.0f, 3.0f);
		
		assertEquals(v1, v1.multiply(1.0f));
		assertEquals(new Vector3f(2.0f, 4.0f, 6.0f), v1.multiply(2.0f));
		assertEquals(new Vector3f(-.5f, -1.0f, -1.5f), v1.multiply(-.5f));
	}
	
	/**
	 * For a vector to accelerate toward a point, what i want is:
	 * given the position vector, a target, and acceleration value,
	 * give me a vector representing the change in velocity toward the target.
	 * 
	 * The returned vector can be added to an initial velocity vector to come up
	 * with a final velocity.
	 */
	@Test
	public void testAccelerateTowardPoint() {
		float accel = 1.67f;
		final Vector3f v1 = new Vector3f(0,0,0);
		
		Vector3f p1 = new Vector3f(100,0,0);
		assertEquals(new Vector3f(1.67f, 0, 0), v1.accelerate(p1, accel));
		
		p1 = new Vector3f(0,100,0);
		assertEquals( new Vector3f(0, 1.67f, 0), v1.accelerate(p1, accel));
		
		p1 = new Vector3f(0,0,100);
		assertEquals(new Vector3f(0, 0, 1.67f), v1.accelerate(p1, accel));
		
		p1 = new Vector3f(100,100,100);
		Vector3f vu = v1.accelerate(p1, accel).multiply(1000);
		assertEquals(new Vector3f(964.0f, 964.0f, 964.0f), new Vector3f(Math.round(vu.x), Math.round(vu.y), Math.round(vu.z)));
		
		
		p1 = new Vector3f(-50,-50,-50);
		vu = v1.accelerate(p1, accel).multiply(1000);
		assertEquals(new Vector3f(-964f, -964f, -964f), new Vector3f(Math.round(vu.x), Math.round(vu.y), Math.round(vu.z)));
	}
	
	/**
	 * String, what could be simpler?
	 */
	@Test
	public void testToString() {
		final Vector3f v1 = new Vector3f(1.2f, 3.4f, 5.6f);
		assertEquals("v: [1.2, 3.4, 5.6]", v1.toString());
	}

	
	/**
	 * Returns a unit vector in the same direction as the provided vector
	 */
	@Test
	public void testUnitVector() {
		final Vector3f v0 = new Vector3f(0,0,0);
		final Vector3f v1 = new Vector3f(1.0f, 0.0f, 0.0f);
		final Vector3f v2 = new Vector3f(0.0f, 1.0f, 0.0f);
		final Vector3f v3 = new Vector3f(0.0f, 0.0f, 1.0f);
		final Vector3f v4 = new Vector3f(4.0f, -7.0f, 9.0f);

		assertEquals(v0.unit(), new Vector3f(0,0,0));
		assertEquals(v1.unit(), v1);
		assertEquals(v2.unit(), v2);
		assertEquals(v3.unit(), v3);
		Vector3f vu = v4.unit().multiply(1000);
		assertEquals(new Vector3f(331f, -579f, 745f), new Vector3f(Math.round(vu.x), Math.round(vu.y), Math.round(vu.z)));
	}
	
	
}
