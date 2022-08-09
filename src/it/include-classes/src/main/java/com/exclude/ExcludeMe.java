package com.exclude;

public class ExcludeMe {

	public void excluded() {
		System.out.println("this method must not be invoked from tests");//
	}

}
