import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/*
 * This program calculates the Detector score. It will take two set of assembly 
 * instructions, and then calculate the D square on each instruction.
 * D square is the sum of all instructions.
 * 
 * Gamma function is obtained from 
 * http://www.cs.princeton.edu/introcs/91float/Gamma.java.html
 */

public class ChiSquaredDistance {

	/*
	 * Calculate the D square distance.
	 */
	public static double computeDistance(HashMap<String, Double> nHat, HashMap<String, Double> n) {
		Iterator<Map.Entry<String, Double>> iterN = n.entrySet().iterator();
		double dSquare = 0, value = 0;

		while (iterN.hasNext()) {
			Map.Entry<String, Double> keySet = iterN.next();
			String key = keySet.getKey();
			Double nValue = keySet.getValue();
			Double nHatValue = nHat.get(key);

			//System.out.println(nValue + " " + nHatValue);

			if (nValue != null) {
				if (nHatValue == null) 
					nHatValue = 0.0;
				value = Math.pow((nHatValue - nValue), 2) / nValue;
				dSquare += value;
			}
			//System.out.println("keyset " + dSquare );
		}
		return dSquare;
	}
	
	/* 
	 * Gamma function is obtained from 
	 * http://www.cs.princeton.edu/introcs/91float/Gamma.java.html
	 */
	private static double logGamma(double x) {
		double tmp = (x - 0.5) * Math.log(x + 4.5) - (x + 4.5);
		double ser = 1.0 + 76.18009173    / (x + 0)   - 86.50532033    / (x + 1)
		+ 24.01409822    / (x + 2)   -  1.231739516   / (x + 3)
		+  0.00120858003 / (x + 4)   -  0.00000536382 / (x + 5);
		return tmp + Math.log(ser * Math.sqrt(2 * Math.PI));
	}
	private static double gamma(double x) { return Math.exp(logGamma(x)); }

	/*
	 * @param x is correspond to the desired D squared 
	 * @param k is the degrees of freedom (# of distinct instructions - 1)
	 */
	private static double ChiSquared(double x, int k) {
		double chi = 0;

		chi = (Math.pow(x, ((double)k/2)-1) * Math.exp(-x/2)) / (Math.pow(2, (double)k/2) * gamma((double)k/2));

		return chi;
	}
	
	public static double getD4Error(double alpha, int k) {
		double sum = 0, x = 0, delta = 0.01;
		
		while (sum < alpha) {
			sum += ChiSquared(x, k);
			x += delta;
		}
		return x - delta;
	}
	/*
	public static void main(String[] argv) {
		System.out.println("5th percentile and k = 100");
		double rate = getD4Error(5, 100);
		System.out.println(rate);
	} */
}
