import java.util.Scanner;
import java.io.FileWriter;
import java.io.FileReader;
import java.util.ArrayList;
import java.io.FileNotFoundException;

public class ProcessAsmFiles {
	
	public static ArrayList<String> readAsmFile(String name) {
		FileReader reader;
		ArrayList<String> data = new ArrayList<String>();
		
		try {
			reader = new FileReader(name);
			Scanner in = new Scanner(reader);
			String line;
			
			while (in.hasNextLine()) {
				line = in.nextLine();
				// Only works with the text section
				if (line.startsWith(".text")) {
					data.add(line);					
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}
	
	public static void processedAsmFile(ArrayList<String> data) {
		FileWriter writer;

		try {
			writer = new FileWriter("output.asm");
			for (int i = 0; i < data.size(); i++) {
				String[] tokens = data.get(i).split("\\s+");
				int size = tokens.length;
				if (size >= 2 && tokens[1].compareTo(";") != 0) {
					String line = "";
					//System.out.println(tokens[1]);
					for (int j = 1; j < size; j++) {
						if (tokens[j].equals(";")) 
							break;
						line = line.concat(tokens[j] + " ");
					}
					System.out.println(line);
					writer.write(line);
					writer.write("\r\n");  // Needs \r for Windows
				}
			} 
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] argv) {
		ArrayList<String> data = new ArrayList<String>();
		data = readAsmFile("C:/Users/Annie/Desktop/School Work/Project/IdaPro Output/example_ida.asm");
		processedAsmFile(data);
	}

	
}

