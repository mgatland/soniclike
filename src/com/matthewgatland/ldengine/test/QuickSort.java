package com.matthewgatland.ldengine.test;

//not part of my game engine. Just programming practice.

import java.util.Random;

public class QuickSort {

	//has a bug :/

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		for (int i=0; i < 20; i++) {
			new QuickSort().start();
		}
	}

	private void start() {
		final int[] values = {smallInt(),smallInt(),smallInt(),smallInt(),smallInt()};
		System.out.println(arrayString(values, 0, values.length));
		quickSort(values, 0, values.length);
		System.out.println("Sorted: " + arrayString(values, 0, values.length));
	}

	//low is inclusive, high is exclusive.
	private void quickSort(final int[] values, final int low, final int high) {
		debug("Sorting sublist " + arrayString(values, low, high));
		if (high - 1 <= low) { //+1 because high is exclusive.
			debug("Nothing to sort.");
			return;
		}
		//debug("from " + low + " to " + high);
		//choose random pivot, move to slot zero.
		swap(values, 0, rand.nextInt(high-low)+low);
		debug("Pivot is " + values[0]);
		int left = 0;
		int moveCount = 0; //not part of the algorithm, just for interest.
		for (int i = low; i < high; i++) {
			if (values[i] < values[0]) {
				//swap into a low position.
				swap(values, i, ++left);
				moveCount++;
			}
		}

		//restore pivot to be above the elements smaller than it.
		swap(values, 0, left);
		debug("Moves: " + moveCount);
		debug("Sorted -- " + arrayString(values, low, high, left));

		//sort below the pivot, and above the pivot.
		//the pivot itself is correctly placed so is never sorted again.
		quickSort(values, low, low+left);
		quickSort(values, low+left+1, high);
	}

	private void debug(final String string) {
		//System.out.println(string);
		string.toString(); //noop
	}

	private String arrayString(final int[] values, final int low, final int high) {
		return arrayString(values, low, high, -1);
	}

	private String arrayString(final int[] values, final int low, final int high, final int pivot) {
		String s = "";
		for (int i = 0; i < values.length; i++) {
			if (i == low) {
				s += "[";
			}
			if (i == pivot) {
				s += "(";
			}
			s += values[i];
			if (i == pivot) {
				s += ")";
			}
			if (i==high - 1) {
				s += "]";
			}
			s += " ";
		}
		return s;
	}

	private void swap(final int[] values, final int i, final int j) {
		final int temp = values[i];
		values[i] = values[j];
		values[j] = temp;
	}

	Random rand = new Random();

	private int smallInt() {
		return Math.abs(rand.nextInt()) % 20;
	}
}
