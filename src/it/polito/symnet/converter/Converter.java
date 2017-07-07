package it.polito.symnet.converter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.gson.JsonArray;

import it.polito.symnet.model.*;
import it.polito.verigraph.model.*;

public class Converter {




	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException, InterruptedException {

		String input=new String();
		String policy=null;
		String source=null;
		String dest=null;
		String box=null;
		boolean test=false;
		String start_point=null;

		if(args.length==1)
			input=args[0];
		else if(args.length==2){
			input=args[0];
			if(args[1].equals("-test"))
				test=true;
			else{
				System.err.println("Command not valid");
				System.exit(-1);
			}
				
		}
		else if(args.length==3){
			if(args[1].equals("-start")){
				input=args[0];
				start_point=args[2];
			}else{
				System.err.println("Command not valid");
				System.exit(-1);
			}
				
			
		}
		else if(args.length==4){
			if(args[1].equals("-r")){
				input=args[0];
				policy=args[1];
				source=args[2];
				dest=args[3];
			}else if(args[1].equals("-test")){
				test=true;
				if(args[2].equals("-start")){
					input=args[0];					
					start_point=args[3];				
				}else{
					System.err.println("Command not valid");
					System.exit(-1);
				}
					
			}else{
				System.err.println("Command not valid");
				System.exit(-1);
			}
		}else if(args.length==5){
			input=args[0];
			policy=args[1];
			source=args[2];
			dest=args[3];
			box=args[4];
			if(policy.equals("-r")){
				System.err.println("Policy argument (2) is not valid. It can be <-i> or <-t>");
				System.exit(-1);
			}else if(!policy.equals("-i")){
				System.err.println("Policy argument (2) is not valid. It can be <-i> or <-t>");
				System.exit(-1);
			}else if(!policy.equals("-t")){
				System.err.println("Policy argument (2) is not valid. It can be <-i> or <-t>");
				System.exit(-1);	
			}
		}else{
			System.err.println("Error in arguments\n");
			System.exit(-1);
		}

		
		Parser parser=new Parser();
		Graph graph=Parser.retrieveGraph(new File(input));
		Map<Long, Node> nodes=graph.getNodes();

		if(policy!=null){

			List<String> checkNode=new ArrayList<String>();		
			for(Map.Entry<Long, Node> s : nodes.entrySet()){
				Node nodo=s.getValue();
				checkNode.add(nodo.getName());
			}
			if(source!=null && !checkNode.contains(source)){
				System.err.println("\"source node\" is not a node of graph \""+input +"\"\nPlease, check the arguments.");
				System.exit(-1);
			}else if(dest!=null && !checkNode.contains(dest)){						
				System.err.println("\"destination node\" is not a node of graph \""+input +"\"\nPlease, check the arguments.");
				System.exit(-1);

			}else if(box!=null && !checkNode.contains(box)){
				System.err.println("\"middlebox node\" is not a node of graph \""+input +"\"\nPlease, check the arguments.");
				System.exit(-1);
			}else if(source.equals(dest)){
				System.err.println("\"destination node\" is equal to \"source node\"\nPlease, check the arguments.");
				System.exit(-1);
			}
		}
		parser.validateGraph();
		Model model=new Model(graph);
		//System.out.println(model.addresses);

		List<Network> nets=model.getNetworks();

		Map<String, List<List<String>>>outputs=new HashMap<String, List<List<String>>>();
		Map<String, List<List<String>>>json_ok=new HashMap<String, List<List<String>>>();
		Map<String, List<List<String>>>json_fail=new HashMap<String, List<List<String>>>();
		String path=createDirectory("network");
		String path_out=createDirectory("outputs");

		for(Network n : nets){

			//file components
			Map<String, Map<String, List<String>>> click=new HashMap<String, Map<String, List<String>>>();
			List<String> links=new ArrayList<String>();
			String start=null;
			if(start_point!=null){
				start=new StringBuilder().append(start_point).append(":").append(start_point+"_in").append(":0").toString();
			}else
				start="generator:a_in:0";
			List<String> args_net=new ArrayList<String>();

			Map<String, Object> node=new HashMap<String, Object>();
			node=n.getAllObject();
			
		
			generateNetworkModel(node, click, links, n);		
			createFiles(path, click, links, start, args_net);
		}
	
			//run solver				
			if(test==true){
				runTest(start_point);
	
			}else{
				run();				
			}	
			
			readOutputJson(outputs, json_ok, json_fail);	
			createOutputs(path_out, outputs);
			createJson(path_out,json_ok, json_fail);
			if(policy!=null)
				checkPolicy(policy, source, dest, box, path_out);

}

	

	private static void generateNetworkModel(Map<String, Object> node, Map<String, Map<String, List<String>>> click, List<String> links, Network n) {
		
		//for each element of the network, create declarations and links
		for(Map.Entry<String, Object> tmp : node.entrySet()){


			if(tmp.getValue() instanceof Endhost){
				Map<String, List<String>> t=new HashMap<String, List<String>>();
				List<String> decl=((Endhost)tmp.getValue()).generateDeclaration();
				List<String> link=((Endhost)tmp.getValue()).generateLink();
				t.put("declarations", decl);
				t.put("links", link);					
				click.put(tmp.getKey(), t);


			}else if(tmp.getValue() instanceof Webserver){
				Map<String, List<String>> t=new HashMap<String, List<String>>();
				List<String> decl=((Webserver)tmp.getValue()).generateDeclaration();
				List<String> link=((Webserver)tmp.getValue()).generateLink(n);
				t.put("declarations", decl);
				t.put("links", link);					
				click.put(tmp.getKey(), t);


			}else if(tmp.getValue() instanceof Nat){
				Map<String, List<String>> t=new HashMap<String, List<String>>();
				List<String> decl=((Nat)tmp.getValue()).generateDeclaration();
				List<String> link=((Nat)tmp.getValue()).generateLink(n);
				t.put("declarations", decl);
				t.put("links", link);					
				click.put(tmp.getKey(), t);


			}else if(tmp.getValue() instanceof Firewall){
				Map<String, List<String>> t=new HashMap<String, List<String>>();
				List<String> decl=((Firewall)tmp.getValue()).generateDeclaration();
				List<String> link=((Firewall)tmp.getValue()).generateLink(n);
				t.put("declarations", decl);
				t.put("links", link);					
				click.put(tmp.getKey(), t);			

			}else if(tmp.getValue() instanceof Dpi){
				Map<String, List<String>> t=new HashMap<String, List<String>>();
				List<String> decl=((Dpi)tmp.getValue()).generateDeclaration();
				List<String> link=((Dpi)tmp.getValue()).generateLink(n);
				t.put("declarations", decl);
				t.put("links", link);					
				click.put(tmp.getKey(), t);					

			}else if(tmp.getValue() instanceof Endpoint){
				Map<String, List<String>> t=new HashMap<String, List<String>>();
				List<String> decl=((Endpoint)tmp.getValue()).generateDeclaration();
				List<String> link=((Endpoint)tmp.getValue()).generateLink(n);
				t.put("declarations", decl);
				t.put("links", link);					
				click.put(tmp.getKey(), t);
			}else if(tmp.getValue() instanceof Mailserver){
				Map<String, List<String>> t=new HashMap<String, List<String>>();
				List<String> decl=((Mailserver)tmp.getValue()).generateDeclaration();
				List<String> link=((Mailserver)tmp.getValue()).generateLink(n);
				t.put("declarations", decl);
				t.put("links", link);					
				click.put(tmp.getKey(), t);
			}
		}
		Map<String, List<String>> t=new HashMap<String, List<String>>();
		List<String> decl=n.getGenerator().generateDeclaration();
		List<String> link=n.getGenerator().generateLinks(n);
		t.put("declarations", decl);
		t.put("links", link);					
		click.put("generator", t);		

		links.addAll(n.generateLinks());	
	

	}

	private static void readOutputJson(Map<String, List<List<String>>> outputs, Map<String, List<List<String>>> json_ok, Map<String, List<List<String>>> json_fail) {
		ObjectMapper mapper = new ObjectMapper();

		try { 
			File json = new File(System.getProperty("user.dir")+ File.separator+ "out.json");
			JsonNode output = mapper.readTree(json); 
			JsonNode finalized = output.get("finalized");
			JsonNode ok=finalized.get("ok");	
			JsonNode ok_states=ok.get("states");
			JsonNode fail=finalized.get("fail");
			JsonNode fail_states=fail.get("states");

			Map<Integer, List<String>> result=new HashMap<Integer, List<String>>();
		

			//read stuck states
			for(int i=0; i<ok_states.size(); i++){					

				JsonNode port=(ok_states.get(i)).get("port_trace");				
				Map<String, String>l=new HashMap<String, String>();
				int index=0;
				List<String> tmp=new ArrayList<String>();


				for(int j=0; j<port.size(); j++){	

					
					l=mapper.convertValue(port.get(j), LinkedHashMap.class);


					String indice=Integer.toString(j);					
					String temp[]=(l.get(indice)).split("-");					
					String first=temp[0];

					if(first.equals("generator"))
						continue;						
					else{
						if(index!=0){

							if(!first.equals(tmp.get(index-1))){
								tmp.add(index, first);						
								index++;
							}else	
								continue;
						}else{
							tmp.add(index, first);						
							index++;
						}
					}		
				}
				result.put(i, tmp);
			

			}

			//insert stuck_states in data structure
			String sat="SAT:";
			String unsat="UNSAT:";
			String result_state="RESULTS";
		
			List<String> sat_result=new ArrayList<String>();
			List<String> unsat_result=new ArrayList<String>();			
			sat_result.add(result_state);
			sat_result.add(sat);			
			unsat_result.add(result_state);
			unsat_result.add(unsat);
			
			
			for(int i=0; i<ok_states.size(); i++){				
				List<String> single=new ArrayList<String>();
				String name=new String();

				for(int j=0; j<result.get(i).size(); j++){
					if(j==0){
						name=result.get(i).get(j);
					}
					single.add(result.get(i).get(j));
				}



				List<List<String>> add=outputs.get(name);
				List<List<String>> ad=json_ok.get(name);


				if(add!=null){

					if(!containsList(add, single)){						
						add.add(single);
					}

				}
				else if(add==null){
					List<List<String>> out=new ArrayList<List<String>>();
					out.add(sat_result);
					outputs.put(name, out);
					out.add(single);					
					outputs.put(name, out);

				}	
				if(ad!=null){
					if(!containsList(ad, single)){
						ad.add(single);
					}
				}
				else if(ad==null){
					List<List<String>> out=new ArrayList<List<String>>();
					out.add(single);					
					json_ok.put(name, out);
				}	

			}

			

			//read fail states
			Map<Integer, List<String>> result_fail=new HashMap<Integer, List<String>>();
			int s=0;
			for(int i=0; i<fail_states.size(); i++){	

				String status=fail_states.get(i).get("status").asText();

				if((status.split(" "))[0].equals("Unexpected")){
					s++;

					JsonNode port=(fail_states.get(i)).get("port_trace");
					Map<String, String>l=new HashMap<String, String>();
					int index=0;
					List<String> tmp=new ArrayList<String>();


					for(int j=0; j<port.size(); j++){						

					
						l=mapper.convertValue(port.get(j), LinkedHashMap.class);


						String indice=Integer.toString(j);					
						String temp[]=(l.get(indice)).split("-");					
						String first=temp[0];

						if(first.equals("generator"))
							continue;						
						else{
							if(index!=0){

								if(!first.equals(tmp.get(index-1))){
									tmp.add(index, first);						
									index++;
								}else	
									continue;
							}else{
								tmp.add(index, first);						
								index++;
							}

						}					



					}
					result_fail.put(s, tmp);
					
				}
			}

			boolean unsat_add=false;
			for(int i=1; i<=s; i++){
				if(i==1)
					unsat_add=true;
				List<String> single=new ArrayList<String>();
				List<String> text=new ArrayList<String>();
				String name=new String();		
			
				for(int j=0; j<result_fail.get(i).size(); j++){
					if(j==0){
						name=result_fail.get(i).get(j);							
					}

					single.add(result_fail.get(i).get(j));
				}		

				List<List<String>> add=outputs.get(name);
				List<List<String>> ad=json_fail.get(name);

				if(add!=null){						
						
					if(!containsList(add, unsat_result)){
						add.add(unsat_result);
					}
					if(!containsList(add, single)){	
						
						add.add(single);
					}

				}
				else if(add==null){
					List<List<String>> out=new ArrayList<List<String>>();			
						
					out.add(unsat_result);
					outputs.put(name, out);
					
					out.add(single);				
					outputs.put(name, out);


				}		
				if(ad!=null){
					if(!containsList(ad, single)){
						ad.add(single);
					}
				}else if(ad==null){
					List<List<String>> out=new ArrayList<List<String>>();
					out.add(single);				
					json_fail.put(name, out);
				}	

			}


		} catch (JsonGenerationException ex){
			ex.printStackTrace(); 
		}catch (JsonMappingException ex){
			ex.printStackTrace();
		}catch(IllegalArgumentException ia){
			System.out.println("Error in json conversion: ");
			ia.printStackTrace();		
		} catch (IOException ex){
			ex.printStackTrace();
		}


	}

	private static void run() {
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

	private static void runTest(String start_point) {
		List<String> time=new ArrayList<String>();
		int index_dim=0;
		
		if(start_point!=null){
			index_dim=12;
			time.add("symnet");
			time.add(start_point);
			
		}else{
			index_dim=11;
			time.add("symnet");
		
		}
		
		for(int i=0; i<10; i++){
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


	private static boolean containsList(List<List<String>> input, List<String> temp){
		boolean found=false;
		int index=0;
		for(List<String> a : input){			
			if(a.size()!=temp.size())
				continue;
			else{

				for(int i=0; i<=a.size(); i++){

					if(i==a.size()){
						found=true;
						break;
					}
					if(a.get(i).equals(temp.get(i))){						
						continue;
					}							
					else
						break;

				}
			}
		}

		return found;
	}

	private static void checkPolicy(String policy, String source, String dest, String box, String path_out) throws JsonParseException, JsonMappingException, IOException {

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



	private static void createJson(String path, Map<String, List<List<String>>> json_ok, Map<String, List<List<String>>> json_fail) {


		ObjectMapper mapper = new ObjectMapper();


		for(Map.Entry<String, List<List<String>>> tmp : json_ok.entrySet()){
			String name=tmp.getKey();
			File file = new File(path+File.separator+name+"_ok.json");
			List<Map<String, String>> result=new ArrayList<Map<String, String>>();

			List<List<String>> args=tmp.getValue();
			for(int i=0; i<args.size(); i++){
				Map<String, String> res=new HashMap<String, String>();
				for(int j=0; j<args.get(i).size(); j++){
					res.put(Integer.toString(j), args.get(i).get(j));
				}
				result.add(res);
			}

			try {
				// Serialize Java object info JSON file.
				mapper.writerWithDefaultPrettyPrinter().writeValue(file, result);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		for(Map.Entry<String, List<List<String>>> tmp : json_fail.entrySet()){
			String name=tmp.getKey();
			File file = new File(path+File.separator+name+"_fail.json");
			List<Map<String, String>> result=new ArrayList<Map<String, String>>();

			List<List<String>> args=tmp.getValue();
			for(int i=0; i<args.size(); i++){
				Map<String, String> res=new HashMap<String, String>();
				for(int j=0; j<args.get(i).size(); j++){
					res.put(Integer.toString(j), args.get(i).get(j));
				}
				result.add(res);
			}

			try {
				// Serialize Java object info JSON file.
				mapper.writerWithDefaultPrettyPrinter().writeValue(file, result);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}











	private static void createOutputs(String path, Map<String, List<List<String>>> outputs){


		//write file
		FileWriter fout;
		BufferedWriter write;
		PrintWriter pwrite;
		String newline="\n";	
		try{
			for(Map.Entry<String, List<List<String>>> o : outputs.entrySet()){
				String name_out=o.getKey();
				List<List<String>> args=o.getValue();
				List<String> result=new ArrayList<String>();

				fout=new FileWriter(path+ File.separator+name_out+".output");
				write=new BufferedWriter(fout);
				pwrite=new PrintWriter(write);	
				

				for(int i=0; i<args.size(); i++){
					List<String> element=args.get(i);
					StringBuilder res=new StringBuilder();
					StringBuilder out=new StringBuilder();
					res.append("RESULTS").append("UNSAT:");
					if(element.equals(res)){
						pwrite.println("-------------------------");
					}
					for(int j=0; j<element.size(); j++){
						if(j<element.size()-1)
							out.append(element.get(j)+ " -> ");
						else
							out.append(element.get(j));
					}
					pwrite.println(out.toString() + newline);

				}

				pwrite.close();

			}
		}catch (IOException e) {
			System.out.println("Exception in create Outputs: ");
			e.printStackTrace();
			System.exit(-1);
		}

	}













	private static void createFiles(String path, Map<String, Map<String, List<String>>> click, List<String> links,
			String start, List<String> args_net){
		String newline="\n";

		//write file
		FileWriter fout;
		BufferedWriter write;
		PrintWriter pwrite;		
	
		try{
			//start.click
			fout=new FileWriter(path+ File.separator+"start.start");
			write=new BufferedWriter(fout);
			pwrite=new PrintWriter(write);
			pwrite.println(start + newline);			
			pwrite.close();		


			//links.links			
			fout=new FileWriter(path+ File.separator+"links.links");
			write=new BufferedWriter(fout);
			pwrite=new PrintWriter(write);
			for(String c : links){				
				pwrite.println(c + newline);					
			}			
			pwrite.close();		

			//others .click
			for(Map.Entry<String, Map<String, List<String>>> file : click.entrySet()){
			
				String name_file=file.getKey()+".click";				
				fout=new FileWriter(path+File.separator+name_file);
				write=new BufferedWriter(fout);
				pwrite=new PrintWriter(write);
				for(String c : file.getValue().get("declarations")){				
					pwrite.println(c + newline);							
				}
				for(String c : file.getValue().get("links")){
					pwrite.println(c + newline);				
				}
				pwrite.close();
				args_net.add(path+File.separator+name_file);
			}
		}catch (IOException e) {
			System.out.println("Exception in .click creation: ");
			e.printStackTrace();
			System.exit(-1);
		}

		try{
			//network.scala
			String indent="  ";
			fout=new FileWriter(System.getProperty("user.dir")+File.separator+"src"+File.separator+"main"+File.separator+"scala"+File.separator+"org"+File.separator+"change"+File.separator+"v2"+File.separator+"runners"+File.separator+"experiments"+File.separator+"NetworkRunner.scala");
			write=new BufferedWriter(fout);
			pwrite=new PrintWriter(write);
			pwrite.println("package org.change.v2.runners.experiments"+newline+
					"import java.io.{File, FileOutputStream, PrintStream}" + newline+
					"import sys.process._"+newline+newline+
					"import org.change.parser.clickfile.ClickToAbstractNetwork"+newline+
					"import org.change.v2.executor.clickabstractnetwork.ClickExecutionContext" + newline+
					"import org.change.v2.executor.clickabstractnetwork.executionlogging.{ModelValidation, JsonLogger, OldStringifier}" + newline+
					"import org.change.parser.startpoints.StartPointParser"+newline+newline+
					"import org.change.parser.interclicklinks.InterClickLinksParser"+newline+
					"import org.change.v2.abstractnet.generic.NetworkConfig"+newline+
					"import org.change.v2.analysis.expression.concrete.ConstantValue"+newline+
					"import org.change.v2.abstractnet.optimizedrouter"+newline+newline+newline				
					);
			pwrite.println("object NetworkRunner{"+newline+
					indent+"def main (args: Array[String]) {"+newline+	
					indent+indent+"val start = System.currentTimeMillis()"+newline+
					indent+indent+"val ctx = ClickExecutionContext.buildAggregated(" + newline+
					indent+indent+indent+"List("+newline);
			for(int i=0; i<args_net.size(); i++){
				if(i==args_net.size()-1)
					pwrite.println(indent+indent+indent+indent+"\""+args_net.get(i)+"\""+newline);
				else
					pwrite.println(indent+indent+indent+indent+"\""+args_net.get(i)+"\","+newline);
			}
			pwrite.println(indent+indent+indent+indent+").map(ClickToAbstractNetwork.buildConfig(_, prefixedElements = true)),"+newline+
					indent+indent+indent+"InterClickLinksParser.parseLinks(\""+path+File.separator+"links.links"+"\"),"+newline+
					indent+indent+indent+"startElems = Some(StartPointParser.parseStarts("+newline+
					indent+indent+indent+indent+" \""+path+File.separator+"start.start\"))" + newline+
					indent+indent+indent+indent+" ).setLogger(JsonLogger)"+newline+newline+				
					indent+indent+"var crtExecutor = ctx"+newline+
					indent+indent+indent+"while(! crtExecutor.isDone) {"+newline+
					indent+indent+indent+"crtExecutor = crtExecutor.execute()"+newline+				
					indent+indent+"}"+newline+newline+	
					indent+indent+" println(\"Time:\" + (System.currentTimeMillis() - start))"+newline+
					indent+indent+"}"+newline+
					"}");	

			pwrite.close();			
		}catch (IOException e) {
			System.out.println("Exception in network.scala creation: ");
			e.printStackTrace();
			System.exit(-1);
		}

	}

	private static String createDirectory(String name){
		//create dir "network"			
		File directory = new File(name);
		String path;
		try{
			if (directory.exists())
			{
				//System.out.println("The dir exists");
				File[] files = directory.listFiles();
				for (File f : files)
					if (!f.delete()) {
						System.err.println("Can't remove  " + f.getAbsolutePath());
					}
					else {
						//System.out.println("Removed file " + f.getAbsolutePath());
					}

			}
			else
			{
				if (!directory.exists())
				{

					boolean created = directory.mkdir();
					if (!created) {
						throw new IOException("Failed to create network directory");
					}

				}

			}
		}catch (IOException e) {
			System.out.println("Exception in create Directory "+name+": ");
			e.printStackTrace();
			System.exit(-1);
		}

		path = directory.getAbsolutePath();
		return path;

	}



}
