import java.io.IOException;

public class Main {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) {
	/*	if (args.length < 2) {
			System.out.println("Missing asm file argument!");
			return;
		}
	*/	 
		int size_of_input = 198; //Integer.parseInt(args[1]);
		int cross_validation_folds = 5; //Integer.parseInt(args[2]);
		int size_per_set = (int) Math.ceil((double)size_of_input/cross_validation_folds);
				
		//System.out.println(size_per_set);
		//startCrossValidation();
		startHMM(size_per_set, cross_validation_folds);
	  
	} // end of main	
	
	public static void startHMM(int size, int cross_validation_folds) {
		CmdExec cmd = new CmdExec(size, cross_validation_folds);
		cmd.trainAllFolders();
		//cmd.testAllFolders();
	}
	
	public static void startCrossValidation() {
		int size_of_input = 198; //Integer.parseInt(args[1]);
		int cross_validation_folds = 5; //Integer.parseInt(args[2]);
		
		int file_percent, subpercent; 
		String name, folder_name, file_index;
		
		// Read the raw instructions into a formatted form
		ReadInstructions read_data = new ReadInstructions();
		
		// Train all 40 x 40 folders
		for (file_percent = 0; file_percent <= Constants.PERCENT; file_percent++) {
			for (subpercent = 0; subpercent <= Constants.PERCENT; subpercent++) {
				file_index =  file_percent + "s" + subpercent;
				name = "DataSetOut" + file_index; //name of the input files - 0s0: 0% dead code & 0% subroutine
				folder_name = Constants.PATH_NAME + name; // full path name to the file - /junk35_function30/
				System.out.format("%s%n", folder_name);			

				// CrossValidation(200, 5, C:/DataSetOut0s0, DataSetOut0s0, 0s0)
				CrossValidation validation = new CrossValidation(size_of_input, cross_validation_folds, folder_name, name, file_index);
				// perform 5-fold validation
				for (int fold_index = 0; fold_index < cross_validation_folds; fold_index++) {
					
					validation.doCrossValidation(read_data, fold_index);
				} // end of nth fold validation	
			}// end of subroutine set 
		} // end of dead code insertion set
	}
}