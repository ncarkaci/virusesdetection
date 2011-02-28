
public interface Constants {
	// The path to the metamorphic worm files 
	public static final String PATH_NAME = "C:/Users/Annie/Desktop/School Work/Reverse Engineering/Da Lin's Data/junk35_function30/"; //DataSetOut0s0";
	// The path to the input files for HMM.exe
	public static final String OUTPUT_PATH = "C:/Users/Annie/Desktop/School Work/Reverse Engineering/Input Data/";
	// The path to the alphabet files for HMM.exe
	public static final String TRAIN_FILES_PATH = "C:/Users/Annie/Desktop/School Work/Reverse Engineering/TrainFiles/";
	// The path to the testing files for HMM.exe
	public static final String MODEL_PATH = "C:/Users/Annie/Desktop/School Work/Reverse Engineering/Model Files/";
	// The path to the testing files for HMM.exe
	public static final String SCORE_PATH = "C:/Users/Annie/Desktop/School Work/Reverse Engineering/Scores/";
	// The location for the HMM.exe
	public static final String HMM_EXE = "C:/Users/Annie/Documents/Visual Studio 2010/Projects/hmm_modified/Release/hmm_modified.exe";
	// The location to the compare set used for testing (non-family virus and normal files)
	public static final String TESTING_SETS = "C:/Users/Annie/Desktop/School Work/Reverse Engineering/Da Lin's Data/junk35_function30/CompareSet/";

	// Max number of hidden states for this test
	public static final int N_HIDDEN_STATES = 6;
	// The max percentage of subroutine and junk code for the metamorphic worm
	public static final int PERCENT = 40;
	// the random SEED
	public final int SEED = 888;
	// max iterations to be performed on the HMM
	public final int MAX_ITERS = 800; 
	// Number of normal files available (IDAR0 to IDAR 40)
	public static final int NORMAL_FILE_SIZE = 41;
	// Number of virus files (IDAN0 to IDAN199)
	public static final int VIRUS_FILE_SIZE = 200;
	// Number of non-family virus files (IDAV0 to 24)
	public static final int OTHER_VIRUS_SIZE = 25;
	
}
