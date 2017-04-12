package cop5556sp17;

public class Name implements Runnable {
	int glob_int0, glob_int1;
	boolean glob_bool0;
//	int sw;

	public Name(String[] args) {
		// initialize instance variables with values from args.
//		glob_int0 = Integer.parseInt(args[0]);
//		glob_bool0 = Boolean.parseBoolean(args[1]);
	}

	public void run() {	
		// declarations and statements from block
		int loc_int0, loc_int1;//, loc_int2;
		boolean loc_bool0, loc_bool1;//, loc_bool2;
//		int sh;
		loc_int0 = 100000;
		glob_int0 = 43;
		loc_int1 = loc_int0 % glob_int0;
		loc_bool0 = true;
		loc_bool1 = false;
		loc_bool0 = loc_bool0 | loc_bool1;
		
//		sw = PLPRuntimeFrame.getScreenWidth();
//		sh = PLPRuntimeFrame.getScreenHeight();
//		System.out.println(sw + "," + sh);

	}

	public static void main(String[] args) {
		(new Name(args)).run();
	}
}
