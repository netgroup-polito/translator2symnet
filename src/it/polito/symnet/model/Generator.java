package it.polito.symnet.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import it.polito.symnet.converter.Network;
import it.polito.verigraph.model.Graph;

public class Generator {
	
	Map<String, String> ports=new HashMap<String, String>();
	Map<String, List<String>> declaration=new HashMap<String, List<String>>();
	Map<String, String> link=new HashMap<String, String>();
	

	
	
	
	public Generator(List<String> host, Graph graph) {
		
		List<String> from=new ArrayList<String>();
		from.add("FromDevice");
		List<String> to=new ArrayList<String>();
		to.add("ToDevice");
		List<String> args=new ArrayList<String>();
			
		
		String arg="web 192.168.1.1 192.168.1.1 800 150";
		
		for(int i=0; i<host.size(); i++){
			String type=graph.searchNodeByName(host.get(i)).getFunctional_type();
			declaration.put(host.get(i), to);			
			if(type.equals("endhost")){
				ports.put(host.get(i), "a_in");
			}
			else{
				ports.put(host.get(i), host.get(i)+"_in");
			}	
			
			link.put("a_in["+i+"]", host.get(i));
			args.add(arg);
		}
		
		
		
		
		declaration.put("a_in", args);
			
				
			
	
		
		
		
		
	}
	
	
	public Map<String, String> getPorts(){
		return ports;
	}


	public List<String> generateDeclaration() {
		final String symbol=" :: ";
		String tmp=new String();
		List<String> declarations=new ArrayList<String>();
		for(Entry<String, List<String>> decl : declaration.entrySet()){		
			if(decl.getKey().compareTo("a_in")==0){				
				
				StringBuilder gen=new StringBuilder().append("Generator(");				
				for(int i=0; i<decl.getValue().size(); i++){					
					gen.append(decl.getValue().get(i));
					if(i==decl.getValue().size()-1)
						gen.append(")");
					else
						gen.append(", ");
				}
				tmp=new StringBuilder().append(decl.getKey()).append(symbol).append(gen.toString()).toString();
			}else
				tmp=new StringBuilder().append(decl.getKey()).append(symbol).append(decl.getValue().get(0)).toString();
			declarations.add(tmp);
		}
		
		return declarations;
	}


	public List<String> generateLinks(Network n) {
		final String symbol=" -> ";
		List<String> links=new ArrayList<String>();
		for(Map.Entry<String, String> l : link.entrySet()){
			StringBuilder tmp=new StringBuilder().append(l.getKey()).append(symbol).append(l.getValue());
			links.add(tmp.toString());
		}
		
		
		return links;
		
	}

}
