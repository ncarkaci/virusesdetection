import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class CmdExec {
	private int size_per_set;
	private int cross_validation_folds;
	private String score_filename;

	public CmdExec(int size, int folds) {
		size_per_set = size;
		cross_validation_folds = folds;
	}

	private void executeHMM(int n, int m, int t, int MAX_ITERS, 
			String in_file, String alphabet_file, String model_file, 
			int SEED, int indicator) {

		try {
			//String arguments = N + " " + M + " " + T + " " + 
			//MAX_ITERS + " \"" + in_file + "\" \"" + alphabet_file + "\" \"" +
			//model_file + "\" " + SEED + " " + indicator;	
			//String cmd_arguments = Constants.HMM_EXE + " " + arguments;
			String[] cmd_args = new String[10];
			cmd_args[0] = Constants.HMM_EXE;
			cmd_args[1] = Integer.toString(n);
			cmd_args[2] = Integer.toString(m);
			cmd_args[3] = Integer.toString(t);
			cmd_args[4] = Integer.toString(MAX_ITERS);
			cmd_args[5] = in_file;
			cmd_args[6] = alphabet_file;
			cmd_args[7] = model_file;
			cmd_args[8] = Integer.toString(SEED);
			cmd_args[9] = Integer.toString(indicator);			

			Runtime rr = Runtime.getRuntime();
			Process pp = rr.exec(cmd_args);
			BufferedReader in = new BufferedReader(new InputStreamReader(pp.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				//System.out.println(line);
				if (indicator == 0 && line.startsWith("logProb")) {
					//System.out.println(line);
					String[] probability = line.split(" ");
					//System.out.println(probability[1]);  // only want the probability from the testing phase
					writeScore(in_file, probability[1]);
				}
			}
			int exitVal = pp.waitFor();
			System.out.println("Process exitValue: " + exitVal);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

	private void writeScore(String filename, String probability) {
		// need the filename (without the file extension), then append 
		// to the file with file identifier and prob 
		String[] name_of_prob = filename.split("/");
		String[] name_only = name_of_prob[8].split("\\.");
		try {
			// If the file does not already exist, it is automatically created.
			FileWriter fw = new FileWriter(score_filename, true); // to append
			BufferedWriter out = new BufferedWriter(fw);
			out.write(name_only[0] + "\t" + probability);
			out.newLine();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	/*
	 * @param folder the folder which contain the training files (.in & .alphabet)
	 * @param file_set to indicate the combination of morphed percentage
	 */
	public void performTraining(String folder, String file_set, int size_used_4_training) {
		int n, m, t;
		int indicator = 1; // 1 is for training
		// in_file is for the .in file, alphabet_file is for .alphabet file
		// model_file is the name of the .model file
		String in_file, alphabet_file, model_file, current_file, temp;

		// perform the HMM from 2 hidden states to 6 hidden states
		for (n = 2; n <= Constants.N_HIDDEN_STATES; n++) {
			for (int cv = 0; cv < cross_validation_folds; cv++) { //cv is the number of cross-validation
				current_file = "f" + cv + "_" + size_used_4_training; //training file only
				in_file = folder + current_file + ".in";
				// T will be read from the .in file
				temp = readFirstLine(in_file);
				if ( temp == null) {
					System.err.println("Error! The first line of data is null!!!");
					return;
				}
				t = Integer.parseInt(temp);

				alphabet_file = folder + current_file + ".alphabet";
				//M will be read from the alphabet file
				if ((temp = readFirstLine(alphabet_file))== null) {
					System.err.println("Error! The first line of data is null!!!");
					return;
				}
				m = Integer.parseInt(temp);

				System.out.println("Start of HMM hidden state N: " + n + " fold# " + cv);
				model_file = Constants.MODEL_PATH + "IDAN" + file_set + current_file  + "_N" + n + ".model";
				executeHMM(n, m, t, Constants.MAX_ITERS, in_file, alphabet_file, model_file, Constants.SEED, indicator);
			}	
			System.out.println("End of one hidden state: " + n);
		}
	}

	public void performTesting(String file_type, String file_set, int size_used_4_training, int size_of_testing_set) {
		int n, m, t;
		int indicator = 0; //0 is for testing
		// folder which contains all the .in files to be tested
		String testing_path = Constants.OUTPUT_PATH + "DataSetOut" + file_set + "/";
		// in_file is for the .in file, alphabet_file is for .alphabet file
		// model_file is the name of the .model file
		String in_file, alphabet_file, model_file, current_file, temp;	

		for (n = 2; n <= Constants.N_HIDDEN_STATES; n++) { //number of hidden states
			for (int cv = 0; cv < cross_validation_folds; cv++) { // number of cross-validation set
				score_filename = Constants.SCORE_PATH + "HmmScores" + file_set 
			    + "f" + cv + "_" + size_used_4_training + "_N" + n + ".score";

				current_file = "f" + cv + "_" + size_used_4_training; //training file only
				alphabet_file =  Constants.TRAIN_FILES_PATH + "DataSetOut" + file_set + "/IDAN" + file_set + current_file + ".alphabet";
				model_file = Constants.MODEL_PATH + "IDAN" + file_set + current_file + "_N" + n + ".model";
				//M will be read from the alphabet file
				if ((temp = readFirstLine(alphabet_file))== null) {
					System.err.println("Error! The first line of data is null!!!");
					return;
				}
				m = Integer.parseInt(temp);

				//size_of_testing_set
				for (int index = 0; index < size_of_testing_set; index++) { //perform testing from file 0 to 39
					// test all family virus files
					in_file = testing_path + file_type + file_set + "f" + cv + "_" + index + ".in";
					// "IDAR"  : normal file 0 to 40
					// IDAV :non-family virus 0 to 24
					// T will be read from the .in file
					temp = readFirstLine(in_file);
					if (temp == null) {
						System.err.println("Error! The first line of data is null!!!");
						return;
					}
					t = Integer.parseInt(temp);

					executeHMM(n, m, t, Constants.MAX_ITERS, in_file, alphabet_file, model_file, Constants.SEED, indicator);
				}			
			}
		}
	}

	private String readFirstLine(String name) {
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

	/*
	 * Open all the folders to train the files.
	 */
	public void trainAllFolders() {
		String folder, subset;
		int size_used_4_training = 200 - size_per_set;
		// Create a pool of 4 threads. Each thread will execute exactly one worker at a time until it
		// finishes (i.e., until it exits the run() function).
		ExecutorService execSvc = Executors.newFixedThreadPool(Constants.MAX_THREADS);

		for (int i = 1; i <= Constants.PERCENT; i += 2) {
			for (int j = 0; j <= Constants.PERCENT; j += 2) {
				subset = i + "s" + j;
				System.out.println("Training set" + i + "s" + j);
				// pth to the training files
				folder = Constants.TRAIN_FILES_PATH + "DataSetOut" + subset + "/IDAN" + subset;
				//TrainThread thread = new TrainThread(this, folder, subset, size_used_4_training);
				// If there are already 4 threads running, this code will pause until there is a slot
				// available.
				execSvc.execute(new TrainThread(this, folder, subset, size_used_4_training));
			}
		}
		execSvc.shutdown();
	}

	public void testAllFolders() {
		String subset;
		int size_used_4_training = 200 - size_per_set;

		// test the virus - same family files
		for (int i = 1; i <= 0; i += 2) { //Constants.PERCENT
			for (int j = 0; j <= 16; j += 2) {
				subset = i + "s" + j;
				System.out.println("Testing set" + i + "s" + j);
				// open each folder and then train the 5 files in each folder
				performTesting("IDAN", subset, size_used_4_training, size_per_set);
				performTesting("IDAR", subset, size_used_4_training, Constants.NORMAL_FILE_SIZE);
				performTesting("IDAV", subset, size_used_4_training, Constants.OTHER_VIRUS_SIZE);
			}
		}
	}


}
