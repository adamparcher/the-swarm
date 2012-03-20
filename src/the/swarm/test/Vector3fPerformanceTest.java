package the.swarm.test;

import java.util.Date;

import org.junit.Test;

import the.swarm.Vector3f;

public class Vector3fPerformanceTest {


	@Test
	public void testLengthCalculation2() {
		System.out.println("#2");
		int vector_count = 100;
		int iterations = 10000;
		Vector3f[] vectors = new Vector3f[vector_count];
		// random vectors
		for(int i = 0; i < vector_count; i++) {
			vectors[i] = Vector3f.random();
		}
		
		long start = new Date().getTime();
		for(int i = 0; i < iterations; i++) {
			for(Vector3f v : vectors) {
				v.unit();
			}
		}
		long end = new Date().getTime();
		System.out.println("Start: " + start + "\nEnd: " + end + "\nTime: " + (end-start));
	}
}
