import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/*
 * Read all the X86 instructions and assign an index to each instruction.
 */
public class ReadInstructions {
	private String inputfile = "All_X86_instructions2.txt";
	// This is where the list of instructions with their indexes
	private String outputfile = "All_instructions.txt";
	// Contain all the instructions with its index
	private HashMap<String, Integer> opcode = new HashMap<String, Integer>();

	/*
	 * Read the raw instruction list file, then save the opcode into 
	 * key & value pair in HashMap.
	 * This is going to be our look-up dictionary of opcode with its
	 * designated index.
	 */
	public ReadInstructions() {	
		FileReader reader;
		try {
			reader = new FileReader(inputfile);
			//FileWriter writer = new FileWriter(outputfile);
			Scanner in = new Scanner(reader);
			int index = 0;

			while (in.hasNextLine()) {
				String line = in.nextLine();
				String[] tokens = line.split("\t"); // Remove everything after the tab
				String[] finalTokens = tokens[0].split("/");  // Separate the /
				int length = finalTokens.length;

				if (length >= 1) {
					int num = 0;
					while (num < length) {
						//	System.out.println(index + " " + finalTokens[num]);
						//writer.write(index + ":" + finalTokens[num]);
						//writer.write("\n");  
						//writer.write("\r\n");  //In Windows, needs the \r

						// Add these instructions into the hashMap
						opcode.put(finalTokens[num], index);

						num++; 			
						index++;
					}
				} 	
			}
			//writer.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * Read the data from the assembly files given, and perform some
	 * minor processing to get rid of all the comment lines and blank
	 * lines. 
	 */
	public ArrayList<String> readAssemblyFile(String filename) throws IOException {
		//String filename = "IDAN0.asm";
		FileReader reader = new FileReader(filename);
		BufferedReader input = new BufferedReader(reader);
		String line = null;
		ArrayList<String> asm = new ArrayList<String>();

		/*
		 * readLine:
		 * it returns the content of a line MINUS the newline.
		 * it returns null only for the END of the stream.
		 * it returns an empty String if two newlines appear in a row.
		 */
		while ((line = input.readLine()) != null) {
			// Trim the leading and ending spaces
			line = line.trim();
			// Only want lines not started with ; or empty line
			if (!line.isEmpty() && !line.startsWith(";")) {
				// Skip the rest once we get to the DATA section
				if (line.startsWith("DATA")) {
					break;
				}			
				asm.add(line);
			}
		}
		return processedAsmList(asm);
	}

	/*
	 * Perform more clean-up process on the assembly code.
	 */
	private ArrayList<String> processedAsmList(ArrayList<String> data) {
		ArrayList<String> processedData = new ArrayList<String>();

		// The replaceFirst will replace the \t with space
		//asmData.get(30).replaceFirst("\t", " ");
		for (String s: data) {
			String[] tokens = s.replaceFirst("\t", " ").split(" ");
			//String[] tokens = data.get(index).replaceFirst("\t", " ").split(" ");
			//System.out.println(tokens[0]);
			// Opcode is always located at the first position
			processedData.add(tokens[0]);
		}
		return processedData;
	}
	
	public void opcodeIndex4HMM(ArrayList<String> data, String name) {
		int index;
		FileWriter file;
		String[] fname = name.split("\\.");
		//System.out.println("name is " + name + " fname is " +fname[0] );
		try {
			file = new FileWriter(fname[0] + ".seq");
			for (String key: data) {
				//System.out.println("Key is " + key);
				if (opcode.containsKey(key.toUpperCase())) {
					index = opcode.get(key.toUpperCase());
					//System.out.println("Inside writing index " + index);
					file.write("[ " + index + " ] ; ");
				}
			}
			file.write("\n");
			file.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void printAllKeyPairs() {
		Iterator<Map.Entry<String, Integer>> iter = opcode.entrySet().iterator();
		while (iter.hasNext()) {
			System.out.println(iter.next());
		}
	}
	

	public void printAllKeyPairs2(Map<String, Integer> data) {
		Iterator<Map.Entry<String, Integer>> iter = data.entrySet().iterator();
		while (iter.hasNext()) {
			System.out.println(iter.next());
		}
	}
	
	/*
	 * Locate the key in the Instructions set. 
	 */
	public Boolean ifContainKey(String key) {
		return opcode.containsKey(key);
	}

	public int getKeyIndex(String key) {
		return opcode.get(key);
	}
	
	/*
	 * Count the number of occurrence for each instruction.
	 */
	public HashMap<String, Double> countInstruction(ArrayList<String> data) {
		HashMap<String, Integer> frequency = new HashMap<String, Integer>();
		int totalFrequency = 0;
		
		// Keep count of each instruction
		for (String inst: data) {
			String foundOpcode = inst.toUpperCase();
			if (opcode.containsKey(foundOpcode)) {
				// It is a valid opcode, then keep count
				if (frequency.containsKey(foundOpcode)) {
					int currentValue = frequency.get(foundOpcode);
					frequency.put(foundOpcode, currentValue + 1);
				} else {
					frequency.put(foundOpcode, 1);  // This opcode is not in the dictionary, create a new one
				}
				totalFrequency++;
			}		
		}
		//printAllKeyPairs2(frequency);
		HashMap<String, Double> frequencyData = new HashMap<String, Double>();
		frequencyData = normalizeFrequency(frequency, totalFrequency);
		return frequencyData; //test on the normalized frequency count
	}
	
	private HashMap<String, Double> normalizeFrequency(HashMap<String, Integer> data, int total) {
		Iterator<Map.Entry<String, Integer>> iter = data.entrySet().iterator();
		HashMap<String, Double> frequencyData = new HashMap<String, Double>();
		
		double frequency = 0;
		
		while (iter.hasNext()) {
			Map.Entry<String, Integer> current = iter.next();
			frequency = (double) current.getValue() / total;
			frequencyData.put(current.getKey(), frequency);
		//	System.out.print(current.getKey() + " ");
		//	System.out.format("%.5f", frequency);
		//	System.out.println();
		}
		return frequencyData;
	}
}
