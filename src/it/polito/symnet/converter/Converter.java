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

import it.polito.policy.*;

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
		int tests=1;

		if(args.length==1)
			input=args[0];		
		else if(args.length==3){			
			if(args[1].equals("-start")){
				input=args[0];
				start_point=args[2];
			}else if(args[1].equals("-test")){
				input=args[0];
				test=true;
				tests=Integer.parseInt(args[2]);				
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
			}else{
				System.err.println("Command not valid");
				System.exit(-1);
			}
		}else if(args.length==5){			
			input=args[0];			
			if(args[1].equals("-test")){
				test=true;
				if(args[2].equals("-start")){									
					start_point=args[3];
					tests=Integer.parseInt(args[4]);
					
				}else{
					System.err.println("Command not valid");
					System.exit(-1);
				}		
			}else if(args[1].equals("-r")){
				System.err.println("Policy argument (2) is not valid. It can be <-i> or <-t>");
				System.exit(-1);
			}else if(args[1].equals("-i") || args[1].equals("-t")){
				policy=args[1];
				source=args[2];
				dest=args[3];
				box=args[4];				
			}else{
				System.err.println("Command not valid");
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
		Outputs out=new Outputs();
	

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
			out.createFiles(click, links, start, args_net);
		}
	
			//run solver	
			Solver s=new Solver();
			if(test==true){
				s.runTest(start_point, tests);
	
			}else{
				s.run();				
			}	
		
			
			out.readOutputJson();	
			out.createOutputs();
			out.createJson();
			if(policy!=null)
				Policy.checkPolicy(policy, source, dest, box);

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


}
