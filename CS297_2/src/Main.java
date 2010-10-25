import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;


public class Main {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("Missing asm file argument!");
			return;
		}
		
		// The files to compute D square
		ArrayList<String> dataFiles = new ArrayList<String>();		
		for (int i = 0; i < args.length; i++) {
			dataFiles.add(args[i]);
		}
		
		// Read the raw instructions into a formatted form
		ReadInstructions readData = new ReadInstructions();
		// This contains the processed assembly codes from data files
		ArrayList<String> asmData = new ArrayList<String>();		
		ArrayList<String> asmData2 = new ArrayList<String>();
		HashMap<String, Double> frequency = new HashMap<String, Double>();
		HashMap<String, Double> frequency2 = new HashMap<String, Double>();		
		Double dSquare = 0.0, chiSquaredD = 0.0;
		int i = 0;
		//for (i = 0; i < dataFiles.size(); i++) {
			try {
				asmData = readData.readAssemblyFile(dataFiles.get(i));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			frequency = readData.countInstruction(asmData);			
			
			for (int j = 1; j < dataFiles.size(); j++) {
				// Processing read in assembly files
				try {									
					asmData2 = readData.readAssemblyFile(dataFiles.get(j));
					frequency2 = readData.countInstruction(asmData2);
					
					//System.out.println("Distinct keys: " + frequency.size() + " " + frequency2.size());
					dSquare = ChiSquaredDistance.computeDistance(frequency2, frequency);
					chiSquaredD = ChiSquaredDistance.getD4Error(5, frequency2.size()); 
					System.out.println(" j " + j + " " + dSquare + " chi-squared: " + chiSquaredD);	
										
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		//}
	}

}