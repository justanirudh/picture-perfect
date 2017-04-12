package cop5556sp17;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class Name implements Runnable {
	int glob_int0, glob_int1;
	boolean glob_bool0;
	File f;
	URL url;
	// int sw;

	public Name(String[] args) throws MalformedURLException{
		// initialize instance variables with values from args.
		 glob_int0 = Integer.parseInt(args[0]);
		 glob_bool0 = Boolean.parseBoolean(args[1]);
		f =  new File(args[0]);
		url = new URL(args[1]);
	}

	public void run(){
		// declarations and statements from block
		int loc_int0, loc_int1;// , loc_int2;
		boolean loc_bool0, loc_bool1;// , loc_bool2;
		// int sh;
		loc_int0 = 100000;
		glob_int0 = 43;
		// BufferedImage bi
	}

	public static void main(String[] args) throws MalformedURLException{
		(new Name(args)).run();
	}
}
