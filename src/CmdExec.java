import java.io.*;


public class CmdExec {
	
	private static void executeHMM(int N, int M, int T, int max_iters, 
			String in_file, String alphabet_file, String model_file, 
			int seed, int indicator) {


		try {
			String arguments = N + " " + M + " " + T + " " + 
			max_iters + " \"" + in_file + "\" \"" + alphabet_file + "\" \"" +
			model_file + "\" " + seed + " " + indicator;	
			
			StringBuffer ss = new StringBuffer();
			ss.append(" "); ss.append(N);ss.append(" ");ss.append(M);ss.append(" ");ss.append(T);ss.append(" ");
			ss.append(max_iters); ss.append(" "); ss.append(in_file); ss.append(" "); ss.append(alphabet_file);
			ss.append(" ");ss.append(model_file);ss.append(" ");ss.append(seed);ss.append(" ");ss.append(indicator);
			ss.toString();

			String cmd_arguments = Constants.HMM_EXE + " " + arguments;
			Runtime rr = Runtime.getRuntime();
			Process pp = rr.exec(cmd_arguments);

			BufferedReader in = new BufferedReader(new InputStreamReader(pp.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				System.out.println(line);
			}
			int exitVal = pp.waitFor();
			System.out.println("Process exitValue: " + exitVal);


		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}
	
	private static void performTraining(String folder) {
		int N, M, T;
		int indicator = 1; // 1 is for training
		int seed = 888; //the random seed
		int max_iters = 800; //max iterations to be performed on the HMM
		double train =  Math.ceil(198.0/5);
		// in_file is for the .in file, alphabet_file is for .alphabet file
		// model_file is the name of the .model file
		String in_file, alphabet_file, model_file, current_file, temp;
					
		// perform the HMM from 2 hidden states to 6 hidden states
		for (N = 2; N <= 2; N++) {
			for (int cv = 0; cv < 1; cv++) {
				current_file =  "IDAN" + cv + "_" + (int)train;
				in_file = Constants.OUTPUT_PATH + folder + current_file + ".in";
				// T will be read from the .in file
				temp = readFirstLine(in_file);
				if ( temp == null) {
					System.err.println("Error! The first line of data is null!!!");
					return;
				}
				T = Integer.parseInt(temp);
				
				alphabet_file = Constants.OUTPUT_PATH + folder + current_file + ".alphabet";
				//M will be read from the alphabet file
				if ((temp = readFirstLine(alphabet_file))== null) {
					System.err.println("Error! The first line of data is null!!!");
					return;
				}
				M = Integer.parseInt(temp);
				
				model_file = Constants.OUTPUT_PATH + folder + current_file + ".model";
				executeHMM(N, M, T, max_iters, in_file, alphabet_file, model_file, seed, indicator);
			}	
		}
	}
	
	private static String readFirstLine(String name) {
		String line = null;
		try {
			BufferedReader in = new BufferedReader(new FileReader(name));
			// read the first line only
			line = in.readLine().trim();
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace(); System.out.println(e);
		} catch (IOException e) {
			e.printStackTrace(); System.out.println(e);
		}
		return line;
	}
	
	private static void trainAllFolders() {
		String folder;
		for (int i = 0; i <= Constants.PERCENT; i++) {
			for (int j = 0; j <= Constants.PERCENT; j++) {
				folder = "DataSetOut" + i + "s" + j + "/";
				// open each folder and then train the 5 files in each folder
				performTraining(folder);
				
			}
		}
	}
	
	public static void main(String[] args) {		
		trainAllFolders();

		

	}

}
