
public class TrainThread implements Runnable {

	private String folder;
	private String subset;
	private int size_used_4_training;
	private CmdExec cmd;
	
	public TrainThread(CmdExec c, String curr_folder, String curr_subset, int size) {
		cmd = c;
		folder = curr_folder;
		subset = curr_subset;
		size_used_4_training = size;
	}
	

	public void run() {
		// open each folder and then train the 5 files in each folder
		cmd.performTraining(folder, subset, size_used_4_training);
	}

}
