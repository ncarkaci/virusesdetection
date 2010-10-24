import java.util.ArrayList;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

public class OpcodeIndex4Hmm {
	public static void OpcodeIndex4Hmm() {
		// Initialize the opcode indexes for hmm input
		try {
			writeIndexes();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void writeIndexes() throws IOException {
		int max = readIndex(); 
		System.out.println("mas is "+ max);
		String outputFile = "test.seq";
		FileWriter output;

		output = new FileWriter(outputFile);
		for (int i = 0; i < max; i++) {
			output.write("[ " + i + " ] ; ");
		}
		output.write("\n");
		output.close();
	}

	private static int readIndex() {
		RandomAccessFile raInput;
		FileReader input;
		String inputFile = "All_instructions.txt";
		int max = 0;

		try {
			input = new FileReader(inputFile);
			Scanner in = new Scanner(input);

			//raInput = new RandomAccessFile(inputFile, "r");
			//long len = raInput.length();
			//raInput.seek(len);
			while(in.hasNextLine()) {
				in.nextLine();
				max++;
			}
			input.close();
			return max;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public static void main(String[] args) {
		OpcodeIndex4Hmm();
	}

}
