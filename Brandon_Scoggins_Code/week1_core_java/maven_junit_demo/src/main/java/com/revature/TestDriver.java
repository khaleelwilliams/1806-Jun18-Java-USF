package com.revature;

public class TestDriver {

	public int multiply(int x, int y) {
		if(x > 999) {
			throw new IllegalArgumentException();
		}
//		return x / y; 		// uncomment to see JUnit test fail
		return x * y;
	}
}