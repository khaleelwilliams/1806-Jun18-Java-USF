package com.revature.util;

import java.io.FileNotFoundException;

import com.revature.models.Beta;
import com.revature.models.Circle;

public class Charlie extends Beta{

	@Override		// increases visibility
	public double protectedDoubleGetter() {
		return 5.5;
	}
	
	// cannot override because parent class version has package visibility from a separate package
	// Methods with package level visibility are not inherited by subclasses located in a different package.
//	@Override
//	String packageStringGetter() {
//		return "nope";
//	}
	
	/*
	 * This works! You can override a method by changing the return type of the method 
	 * signature, but only if the new return type is a subclass of the original return 
	 * type.
	 */
	@Override
	public Circle getShape() {
		return new Circle();
	}
	
	/*
	 * This does not work. We are trying to override the method by using a super
	 * class of the return type, instead of a subclass.
	 */
//	@Override
//	public Shape getCircle() {
//		return new Circle();
//	}
	
	
	// Can make thrown exceptions more specific but not more generic.
	// The throws portion can be left out and still successfully override
	@Override
	protected String protectedStringThrowsException() throws FileNotFoundException{
		return "Method overridden";
	}
	
//	@Override
//	public String publicStringThrowsIOException() throws Exception{
//		return "";
//	}
	
	
	
	
}
