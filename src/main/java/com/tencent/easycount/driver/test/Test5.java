package com.tencent.easycount.driver.test;

public class Test5 {
	public static void main(String[] args) {
		int last = 0;
		int dd = 6000;
		double fact = 0.9;
		for (int i = 0; i < 60; i++) {
			System.out.println(last);
			last += dd;
			dd = (int) (dd * fact);
		}
	}
}
