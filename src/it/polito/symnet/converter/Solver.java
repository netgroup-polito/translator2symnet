package it.polito.symnet.converter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Solver {

	
	protected static void run() {
		try {

			String s=null;
			Process p = Runtime.getRuntime().exec("sbt compile network");

			BufferedReader stdInput = new BufferedReader(new 
					InputStreamReader(p.getInputStream()));

			BufferedReader stdError = new BufferedReader(new 
					InputStreamReader(p.getErrorStream()));

			// read the output from the command
			System.out.println("Here is the standard output of the command:\n");
			while ((s = stdInput.readLine()) != null) {
				System.out.println(s);
			}

			// read any errors from the attempted command
			System.out.println("Here is the standard error of the command (if any):\n");
			while ((s = stdError.readLine()) != null) {
				System.out.println(s);
			}


		}
		catch (IOException e) {
			System.out.println("exception happened - here's what I know: ");
			e.printStackTrace();
			System.exit(-1);
		}

	}

	protected static void runTest(String start_point, int iteration) {
		List<String> time=new ArrayList<String>();
		int index_dim=0;
		
		if(start_point!=null){
			index_dim=iteration+2;
			time.add("symnet");
			time.add(start_point);
			
		}else{
			index_dim=iteration+1;
			time.add("symnet");
		
		}
		
		for(int i=0; i<iteration; i++){
			try {
				String s=null;
				System.out.println(i);

				Process p = Runtime.getRuntime().exec("sbt compile network");
				BufferedReader stdInput = new BufferedReader(new 
						InputStreamReader(p.getInputStream()));

				BufferedReader stdError = new BufferedReader(new 
						InputStreamReader(p.getErrorStream()));

				// read the output from the command
				System.out.println("Here is the standard output of the command:\n");
				while ((s = stdInput.readLine()) != null) {
					String[] temp=s.split(" "); 
					if(temp.length>1){
						String t=temp[1];
						String[] ms_temp=null;
						if(t.length()>11){		        		
							if(t.substring(4, 8).equals("Time")){
								ms_temp=t.split(":");													
								String[]ms_t=ms_temp[1].split("\\[");					
											
								if(ms_t!=null){
									time.add(ms_t[0]);
								}else{
									System.out.println("non c'è [");
									time.add(ms_temp[0]);	
								}						
									
							}
						}
					}
					System.out.println(s);
				}

				// read any errors from the attempted command
				System.out.println("Here is the standard error of the command (if any):\n");
				while ((s = stdError.readLine()) != null) {
					System.out.println(s);
				}




			}
			catch (IOException e) {
				System.out.println("exception happened - here's what I know: ");
				e.printStackTrace();
				System.exit(-1);
			}
		}
		//write result.csv
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new File("result.csv"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		StringBuilder builder = new StringBuilder();
		for(int j=0; j<index_dim; j++){
			if(j==index_dim-1){				
					builder.append(time.get(j));

			}else{				
				builder.append(time.get(j));		
				builder.append(",");
			
			}

		}
		pw.write(builder.toString());
		pw.close();


	}				
	
}
