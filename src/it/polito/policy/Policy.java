package it.polito.policy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Policy {

	public static void main(String[] args) throws JsonGenerationException, JsonMappingException, IOException {
		String source=null;
		String dest=null;
		String policy=null;
		String box=null;
		if(args.length==3){
			policy=args[0];
			source=args[1];
			dest=args[2];	
			if(!policy.equals("-r")){
					System.err.println("Policy is not valid");
					System.exit(1);
			}
			
		}
		else if(args.length==4){
			policy=args[0];
			source=args[1];
			dest=args[2];	
			box=args[3];
			if(policy.equals("-r")){
				System.err.println("Policy is not valid");
				System.exit(1);
			}
			
		}else{
			System.err.println("Command not valid");
			System.exit(1);
		
	
		}
		/*List<Map<String, String>> input_ok= new ArrayList<Map<String, String>>();
		List<Map<String, String>> input_fail= new ArrayList<Map<String, String>>();
		
		ObjectMapper mapper=new ObjectMapper();
		try{
			File json_ok=new File(System.getProperty("user.dir")+File.separator+"outputs"+File.separator+source+"_ok.json");
			File json_fail=new File(System.getProperty("user.dir")+File.separator+"outputs"+File.separator+source+"_fail.json");
		
			input_ok=mapper.readValue(json_ok, ArrayList.class);
			input_fail=mapper.readValue(json_fail, ArrayList.class);
			

			String result=null;	*/
			
			checkPolicy(policy, source, dest, box);
			
	/*switch(policy){
			
			case "-r":{
				boolean sat=false;
				boolean unsat=false;	
			
				for(int i=0; i<input_ok.size(); i++){
					if(sat==true){
						result="SAT";			
						break;
					}else if(input_ok.get(i).containsValue(dest)){					
									sat=true;
							
						}								
			
				}
				for(int i=0; i<input_fail.size(); i++){
					if(sat==true){
						result="SAT";			
						break;
					}else if(input_fail.get(i).containsValue(dest)){
						
						List<String> record=new ArrayList<String>();
						int d=0;						
						for(Map.Entry<String, String> line : input_fail.get(i).entrySet()){						
							record.add(Integer.parseInt(line.getKey()), line.getValue());	
							if(line.getValue().equals(dest))
								d=Integer.parseInt(line.getKey());
							}
						
						if(d<record.size()-1){					
								sat=true;
					}				
				}
				}
				
				if(sat==true)
					result="SAT";
				if(sat==false)
					result="UNSAT";			
				System.out.println("Reachability from \""+source+"\" to \""+dest+"\": "+result);
				break;
			}
			
			case "-i" :{			
				boolean sat=false;
				boolean unsat=false;
				boolean trovato=false;
				for(int i=0; i<input_ok.size(); i++){
					
					if(trovato==true){										
						break;
					}else if(input_ok.get(i).containsValue(dest)){
						int m=0;
						boolean mfound=false;
						int dst=0;
						List<String> record=new ArrayList<String>();
						for(Map.Entry<String, String> map : input_ok.get(i).entrySet()){
							record.add(Integer.parseInt(map.getKey()),map.getValue());
							if(map.getValue().equals(box)){
								if(mfound==false){
									m=Integer.parseInt(map.getKey());
									mfound=true;
								}
								else{
									if(Integer.parseInt(map.getKey())<m){
											m=Integer.parseInt(map.getKey());											
									}
								}
							}
							else if(map.getValue().equals(dest))
								dst=Integer.parseInt(map.getKey());						
							
						}
				
						if(dst>0 && m>0){
							if(dst<m){				
									
										sat=true;
										trovato=true;
									
								}
							
						}else if(dst>0 && m==0){							
								sat=true;
								trovato=true;
							
						}
						
						
					}				
						
				}
				for(int i=0; i<input_fail.size(); i++){
				
					if(trovato==true){
						break;
					}else if(input_fail.get(i).containsValue(dest)){
						int m=0;
						int dst=0;
						boolean mfound=false;
						List<String> record=new ArrayList<String>();
						for(Map.Entry<String, String> map : input_fail.get(i).entrySet()){
							
							record.add(Integer.parseInt(map.getKey()),map.getValue());
							if(map.getValue().equals(box)){
								if(mfound==false){
									m=Integer.parseInt(map.getKey());
									mfound=true;
								}
								else{
									if(Integer.parseInt(map.getKey())<m){
											m=Integer.parseInt(map.getKey());											
									}
								}
							}
							else if(map.getValue().equals(dest))
								dst=Integer.parseInt(map.getKey());								
							
						}
						
						if(dst>0 && m>0){
							if(dst<m){
								sat=true;
								trovato=true;
							}
						}else if(dst>0 && m==0){
							if(dst<record.size()-1){
								sat=true;
								trovato=true;
							}
						}
						
						
					}				
						
				}
				if(sat==true)
					result="SAT";
				if(sat==false)
					result="UNSAT";			
				System.out.println("Isolation from \""+source+"\" to \""+dest+ "\"(without middlebox "+box+"): "+result);
				
				
				break;
			}
			
			case "-t" :{			
				boolean sat=false;
				boolean unsat=false;
				boolean trovato=false;
				for(int i=0; i<input_ok.size(); i++){
					if(trovato==true){										
						break;
					}else if(input_ok.get(i).containsValue(dest)){
						int m=0;
						boolean mfound=false;
						int dst=0;
						List<String> record=new ArrayList<String>();
						for(Map.Entry<String, String> map : input_ok.get(i).entrySet()){
							record.add(Integer.parseInt(map.getKey()),map.getValue());
							if(map.getValue().equals(box)){
								if(mfound==false){
									m=Integer.parseInt(map.getKey());
									mfound=true;
								}
								else{
									if(Integer.parseInt(map.getKey())<m){
											m=Integer.parseInt(map.getKey());											
									}
								}
							}
							else if(map.getValue().equals(dest))
								dst=Integer.parseInt(map.getKey());						
							
						}
						if(dst>0 && m>0){
							if(dst>m){								
										sat=true;
										trovato=true;
									}
								}
						
						
						
						
					}				
						
				}
				for(int i=0; i<input_fail.size(); i++){
					if(trovato==true){
						break;
					}else if(input_fail.get(i).containsValue(dest)){
						int m=0;
						int dst=0;
						boolean mfound=false;
						List<String> record=new ArrayList<String>();
						for(Map.Entry<String, String> map : input_fail.get(i).entrySet()){
							
							record.add(Integer.parseInt(map.getKey()),map.getValue());
							if(map.getValue().equals(box)){
								if(mfound==false){
									m=Integer.parseInt(map.getKey());
									mfound=true;
								}
								else{
									if(Integer.parseInt(map.getKey())<m){
											m=Integer.parseInt(map.getKey());											
									}
								}
							}
							else if(map.getValue().equals(dest))
								dst=Integer.parseInt(map.getKey());								
							
						}
						if(dst>0 && m>0){
							if(dst>m){
								if(dst<record.size()-1){
									sat=true;
									trovato=true;
								}
							}
						}
						
						
					}				
						
				}
				if(sat==true)
					result="SAT";
				if(sat==false)
					result="UNSAT";			
				System.out.println("Traversal from \""+source+"\" to \""+dest+ "\"(without middlebox \""+box+"\"): "+result);
				
				
				break;
			}
			
			default:
				System.out.println("Policy not valid \nDigit: graph <-r ");
				break;
			}
		}catch(IOException e){
			System.out.println(source+".json" + "not present.\n The source doesn't reach no one");
			System.exit(1);
		}
		
		
	*/

		
	}

public static void checkPolicy(String policy, String source, String dest, String box) throws JsonParseException, JsonMappingException, IOException {

	List<Map<String, String>> input_ok= new ArrayList<Map<String, String>>();
	List<Map<String, String>> input_fail= new ArrayList<Map<String, String>>();

	ObjectMapper mapper=new ObjectMapper();
	try{
		File json_ok=new File(System.getProperty("user.dir")+File.separator+"outputs"+File.separator+source+"_ok.json");
		File json_fail=new File(System.getProperty("user.dir")+File.separator+"outputs"+File.separator+source+"_fail.json");			
		input_ok=mapper.readValue(json_ok, ArrayList.class);
		input_fail=mapper.readValue(json_fail, ArrayList.class);			

		String result=null;
		switch(policy){

		case "-r":{
			boolean sat=false;
			boolean unsat=false;		
			for(int i=0; i<input_ok.size(); i++){
				if(sat==true){
					result="SAT";			
					break;
				}else if(input_ok.get(i).containsValue(dest)){					
					sat=true;

				}								

			}
			for(int i=0; i<input_fail.size(); i++){
				if(sat==true){
					result="SAT";			
					break;
				}else if(input_fail.get(i).containsValue(dest)){

					List<String> record=new ArrayList<String>();
					int d=0;						
					for(Map.Entry<String, String> line : input_fail.get(i).entrySet()){						
						record.add(Integer.parseInt(line.getKey()), line.getValue());	
						if(line.getValue().equals(dest))
							d=Integer.parseInt(line.getKey());
					}

					if(d<record.size()-1){					
						sat=true;
					}				
				}
			}

			if(sat==true)
				result="SAT";
			if(sat==false)
				result="UNSAT";			
			System.out.println("Reachability from \""+source+"\" to \""+dest+"\": "+result);
			break;
		}

		case "-i" :{			
			boolean sat=false;
			boolean unsat=false;
			boolean trovato=false;
			for(int i=0; i<input_ok.size(); i++){

				if(trovato==true){										
					break;
				}else if(input_ok.get(i).containsValue(dest)){
					int m=0;
					boolean mfound=false;
					int dst=0;
					List<String> record=new ArrayList<String>();
					for(Map.Entry<String, String> map : input_ok.get(i).entrySet()){
						record.add(Integer.parseInt(map.getKey()),map.getValue());
						if(map.getValue().equals(box)){
							if(mfound==false){
								m=Integer.parseInt(map.getKey());
								mfound=true;
							}
							else{
								if(Integer.parseInt(map.getKey())<m){
									m=Integer.parseInt(map.getKey());											
								}
							}
						}
						else if(map.getValue().equals(dest))
							dst=Integer.parseInt(map.getKey());						

					}

					if(dst>0 && m>0){
						if(dst<m){				

							sat=true;
							trovato=true;

						}

					}else if(dst>0 && m==0){							
						sat=true;
						trovato=true;

					}


				}				

			}
			for(int i=0; i<input_fail.size(); i++){

				if(trovato==true){
					break;
				}else if(input_fail.get(i).containsValue(dest)){
					int m=0;
					int dst=0;
					boolean mfound=false;
					List<String> record=new ArrayList<String>();
					for(Map.Entry<String, String> map : input_fail.get(i).entrySet()){

						record.add(Integer.parseInt(map.getKey()),map.getValue());
						if(map.getValue().equals(box)){
							if(mfound==false){
								m=Integer.parseInt(map.getKey());
								mfound=true;
							}
							else{
								if(Integer.parseInt(map.getKey())<m){
									m=Integer.parseInt(map.getKey());											
								}
							}
						}
						else if(map.getValue().equals(dest))
							dst=Integer.parseInt(map.getKey());								

					}

					if(dst>0 && m>0){
						if(dst<m){
							sat=true;
							trovato=true;
						}
					}else if(dst>0 && m==0){
						if(dst<record.size()-1){
							sat=true;
							trovato=true;
						}
					}


				}				

			}
			if(sat==true)
				result="SAT";
			if(sat==false)
				result="UNSAT";			
			System.out.println("Isolation from \""+source+"\" to \""+dest+ "\"(without middlebox "+box+"): "+result);


			break;
		}

		case "-t" :{			
			boolean sat=false;
			boolean unsat=false;
			boolean trovato=false;
			for(int i=0; i<input_ok.size(); i++){
				if(trovato==true){										
					break;
				}else if(input_ok.get(i).containsValue(dest)){
					int m=0;
					boolean mfound=false;
					int dst=0;
					List<String> record=new ArrayList<String>();
					for(Map.Entry<String, String> map : input_ok.get(i).entrySet()){
						record.add(Integer.parseInt(map.getKey()),map.getValue());
						if(map.getValue().equals(box)){
							if(mfound==false){
								m=Integer.parseInt(map.getKey());
								mfound=true;
							}
							else{
								if(Integer.parseInt(map.getKey())<m){
									m=Integer.parseInt(map.getKey());											
								}
							}
						}
						else if(map.getValue().equals(dest))
							dst=Integer.parseInt(map.getKey());						

					}
					if(dst>0 && m>0){
						if(dst>m){								
							sat=true;
							trovato=true;
						}
					}




				}				

			}
			for(int i=0; i<input_fail.size(); i++){
				if(trovato==true){
					break;
				}else if(input_fail.get(i).containsValue(dest)){
					int m=0;
					int dst=0;
					boolean mfound=false;
					List<String> record=new ArrayList<String>();
					for(Map.Entry<String, String> map : input_fail.get(i).entrySet()){

						record.add(Integer.parseInt(map.getKey()),map.getValue());
						if(map.getValue().equals(box)){
							if(mfound==false){
								m=Integer.parseInt(map.getKey());
								mfound=true;
							}
							else{
								if(Integer.parseInt(map.getKey())<m){
									m=Integer.parseInt(map.getKey());											
								}
							}
						}
						else if(map.getValue().equals(dest))
							dst=Integer.parseInt(map.getKey());								

					}
					if(dst>0 && m>0){
						if(dst>m){
							if(dst<record.size()-1){
								sat=true;
								trovato=true;
							}
						}
					}


				}				

			}
			if(sat==true)
				result="SAT";
			if(sat==false)
				result="UNSAT";			
			System.out.println("Traversal from \""+source+"\" to \""+dest+ "\"(without middlebox \""+box+"\"): "+result);


			break;
		}

		default:
			System.out.println("Policy not valid \nDigit: graph <-r, -i, -t> src dst <box> ");
			break;
		}

	}catch(IOException e){
		System.out.println(source+".json" + "not present.\n The source doesn't reach no one");
		System.exit(1);
	}

}
}
