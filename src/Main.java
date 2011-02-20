import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;


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
		
		int file_percent, subpercent; 
		String name, folder_name;
		
		// Read the raw instructions into a formatted form
		ReadInstructions read_data = new ReadInstructions();
		
		// Train all 40 x 40 folders
		for (file_percent = 0; file_percent <= Constants.PERCENT; file_percent++) {
			for (subpercent = 0; subpercent <= Constants.PERCENT; subpercent++) {
				name = "DataSetOut" + file_percent + 's' + subpercent; //name of the input files - 0s0: 0% dead code & 0% subroutine
				folder_name = Constants.PATH_NAME + name; // full path name to the file
				System.out.format("%s%n", folder_name);			

				CrossValidation validation = new CrossValidation(size_of_input, cross_validation_folds, folder_name, name);
				// perform 5-fold validation
				for (int fold_index = 0; fold_index < cross_validation_folds; fold_index++) {
					
					validation.doCrossValidation(read_data, fold_index);
				}
				
			}
		}
		

	}
	
	
}