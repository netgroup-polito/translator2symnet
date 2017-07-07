package it.polito.symnet.converter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Outputs {
	static Map<String, List<List<String>>>outputs=new HashMap<String, List<List<String>>>();
	static Map<String, List<List<String>>>json_ok=new HashMap<String, List<List<String>>>();
	static Map<String, List<List<String>>>json_fail=new HashMap<String, List<List<String>>>();
	static String path=null;
	static String path_out=null;
	
	public Outputs(){
		path=createDirectory("network");
		path_out=createDirectory("outputs");
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

	protected static void createFiles(Map<String, Map<String, List<String>>> click, List<String> links,
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
	
	
	protected static void readOutputJson() {
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
	
	protected static void createOutputs(){


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

				fout=new FileWriter(path_out+ File.separator+name_out+".output");
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
	
	protected static void createJson() {


		ObjectMapper mapper = new ObjectMapper();


		for(Map.Entry<String, List<List<String>>> tmp : json_ok.entrySet()){
			String name=tmp.getKey();
			File file = new File(path_out+File.separator+name+"_ok.json");
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
			File file = new File(path_out+File.separator+name+"_fail.json");
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


}
