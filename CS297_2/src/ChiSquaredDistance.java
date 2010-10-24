import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/*
 * This program calculates the Detector score. It will take two set of assembly 
 * instructions, and then calculate the D square on each instruction.
 * D square is the sum of all instructions.
 */

public class ChiSquaredDistance {
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

}
