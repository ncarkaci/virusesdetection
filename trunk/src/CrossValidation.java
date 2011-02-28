import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;


public class CrossValidation {
	private int size_of_input;
	private int num_folds; // Number of cross-validation folds (how many splits are there)
	private String folder_name; // Folder where the input data are located
	private String filename; //name for the folder for each set (DataSetOut0s0)
	private String file_index_name; //the current percents of metamorphic virus (0s0 - 40s40)
	
	/*
	 * @param input the total size of input files
	 * @param sets number of cross-validation folds
	 * @param name path to the input files folder - where the asm files are
	 * @param fname name for the folder for each set (DataSetOut0s0)
	 * @param findex the current percents of metamorphic virus (0s0 - 40s40)
	 */
	public CrossValidation(int input, int sets, String name, String fname, String findex) {
		size_of_input = input;
		num_folds = sets;
		folder_name = name;
		filename = fname;
		file_index_name = findex;
	}

	public void doCrossValidation(ReadInstructions read_data, int fold_index) {
		// Make sure to create a folder to store these input files for HMM
		makeDir();

		String output_file_folder = null;
		// Contains all the input assembly files
		ArrayList<String> data_files = generateDataFiles();
		// Contain the asm file(s) to create alphabet and in file
		int size_per_set = 200/num_folds; //size_of_input / num_folds;
		
		// First, create the training set [size_per_set...size_of_input-1], then extract the 
		// alphabet from it. The alphabet is shared by the entire fold (fold_index).
		ArrayList<String> input_asm_files = new ArrayList<String>();
		// Training file will be using the remaining file - ex: from index starting at 40 to 199
		for (int j = size_per_set; j < size_of_input; j++) {
			try { 
				input_asm_files.addAll(read_data.readAssemblyFile(data_files.get(j)));			
			} catch (IOException e) {
				e.printStackTrace();
			}
		} 
		// This contains all the unique symbols for the alphabet file in the training.
		ArrayList<String> alphabet_data = read_data.getAlphabet(input_asm_files);
		output_file_folder = Constants.TRAIN_FILES_PATH + filename + "/IDAN" + file_index_name + "f" + fold_index + "_";
		int size_used_4_training = 200 - size_per_set;
		// Write the alphabet file to the location indicated. ALWAYS use this alphabet.
		read_data.writeAlphabets(alphabet_data, output_file_folder + size_used_4_training);
		// Create the training .in file.
		createInputFiles(read_data, alphabet_data, input_asm_files, size_used_4_training, output_file_folder); 
		
		output_file_folder = Constants.OUTPUT_PATH + filename + "/IDAN" + file_index_name + "f" + fold_index + "_";
		// The testing set is [0...size_per_set-1] and for each input, output a .in file
		// which shares the alphabet with training.
		for (int i = 0; i < size_per_set; i++) {
			try { 
				ArrayList<String> current_file = read_data.readAssemblyFile(data_files.get(i));
				createInputFiles(read_data, alphabet_data, current_file, i, output_file_folder);
			} catch (IOException e) {
				e.printStackTrace();
			}	
		} 
		// Create .in files for the normal files
		String input_files_folder = Constants.TESTING_SETS + "IDAR";
		output_file_folder = Constants.OUTPUT_PATH + filename + "/IDAR" + file_index_name + "f" + fold_index + "_";
		createInputFiles4Testing(read_data, alphabet_data, input_files_folder, output_file_folder, Constants.NORMAL_FILE_SIZE);
		// Create .in files for the non-family virus files
		input_files_folder = Constants.TESTING_SETS + "IDAV";
		output_file_folder = Constants.OUTPUT_PATH + filename + "/IDAV" + file_index_name + "f" + fold_index + "_";
		createInputFiles4Testing(read_data, alphabet_data, input_files_folder, output_file_folder, Constants.OTHER_VIRUS_SIZE);		
	}

	public void createInputFiles4Testing(ReadInstructions read_data, ArrayList<String> alphabet_data, 
			String type_of_input, String path_name, int size) {
		String current_asm;
		// We have 41 normal files. They are named IDAR0 to IDAR40
		// The testing set is and for each input, output a .in file.
		for (int i = 0; i < size; i++) {
			try { 
				current_asm = type_of_input + i + ".asm";
				ArrayList<String> current_file = read_data.readAssemblyFile(current_asm);
				createInputFiles(read_data, alphabet_data, current_file, i, path_name);
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
	}
	
	/**
	 * 
	 * @param read_data the opcodes structure will be stored here
	 * @param current_file the current assembly file to be convert to alphabet and in files
	 * @param file_index the index to identify training or testing file (0 - 39: testing, 40 is training)
	 * @param fold_index the current cross-validation fold
	 */
	private void createInputFiles(ReadInstructions read_data, ArrayList<String> alphabet_data, 
			ArrayList<String> current_file, int file_index, String path_name) {
		// This contains the processed assembly codes from data files
		//ArrayList<String> asm_data = new ArrayList<String>();	
		//this is the .in file
		//asm_data.addAll(current_file); 
		
		// This contains all the index for the opcodes
		// get the index of all opcodes
		ArrayList<Integer> index_data = read_data.getInputFile(current_file, alphabet_data);
		// write the index into the .in file to the location indicated
		read_data.writeInputFile(index_data, path_name + file_index);	
	}

	private ArrayList<String> generateDataFiles() {
		// Number prefix of the input assembly files
		int[] file_num = new int[size_of_input];
		// Contains all the input assembly files
		ArrayList<String> data_files = new ArrayList<String>();	

		// perform file permutation for each set
		file_num = performPermutation();
		// add the files in the order that generated by the perm generator
		for (int k = 0; k < size_of_input; k++) {
			data_files.add(folder_name + "/IDAN" + file_num[k] + ".asm");
		}
		return data_files;
	}

	private void makeDir() {
		//check to see if the dir exist, if not create it
		File folder = new File(Constants.OUTPUT_PATH + filename);
		File folder2 = new File(Constants.TRAIN_FILES_PATH + filename);
		boolean done = true;
		boolean done2 = true;
		if (!folder.exists()) {
			done = folder.mkdir();
		} 
		if (!folder2.exists()) {
			done2 = folder2.mkdir();
		} 
		if (done == false || done2 == false){
			System.err.println("Error! Cannot make dir!");
			System.exit(1);
		}
	}

	/*
	 * Perform permutation on the files to make sure the cross-validation 
	 * sets are not the same.
	 */
	private int[] performPermutation() {
		int j = 0, k; 
		Random rand = new Random();
		// Need a permutation of files for cross-validation
		int[] file_num = new int[size_of_input];
		// initialize the file number in increase order
		for (int i = 0; i < size_of_input; i++) {
			file_num[i] = i;
		}

		// perform permutation
		for (int i = size_of_input - 1; i > 0; i--) {
			j = rand.nextInt(i+1); // random integer between 0 and i
			k = file_num[i];
			file_num[i] = file_num[j];
			file_num[j] = k;
		}
		return file_num;
	}
}
