package it.polito.symnet.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import it.polito.symnet.converter.Network;

public class Endpoint {

	Map<String, String> ports=new HashMap<String, String>();
	Map<String, List<String>> declaration=new HashMap<String, List<String>>();
	Map<String, String> link=new HashMap<String, String>();
	String address;
	String name;
	Map<String, String> addresses;
	List<String>router=new ArrayList<String>();


	public Endpoint(String address, List<String> n, Map<String, String> addresses, String name, Map<String, String> protocols) {
		this.address=address;
		this.name=name;
		this.addresses=addresses;
		
		List<String> from=new ArrayList<String>();
		from.add("FromDevice");
		List<String> to=new ArrayList<String>();
		to.add("ToDevice");
		/*
		for(int i=0; i<n.size(); i++){
			//foreach neighbour
			//ai_in :: FromDevice
			//ai_out :: ToDevice
			ports.put(n.get(i), "a"+i);
			declaration.put("a"+i+"_in", from);				
			link.put("a"+i+"_in", "a_in");
			
			
		}*/
		
		//general input and output:
	
		declaration.put("a_in", from);
		link.put("a_in", "router");
		
		declaration.put(name+"_in", from);
		link.put(name+"_in", "router");
		declaration.put(name+"_out", to);
		
		
		
		List<String> args=new ArrayList<String>();	
		int index=1;
		List<String>tmp_router=new ArrayList<String>();
		tmp_router.add(0, "dst host "+addresses.get(name));
		router.add(0, name);
		
		//foreach address create arguments of IPClassifier
		//router :: IPClassifier(dst host address_1, ... , dst host address_n, -) 
		
		tmp_router.add(index, "-");
		router.add(index, "-");		
		declaration.put("router", tmp_router);	
		link.put("router[0]", name+"_out");
		link.put("router[1]", "Discard");
	
		
	}
	

	public Map<String, String> getPorts(){
		return ports;
	}


	public List<String> generateDeclaration() {
		final String symbol=" :: ";
		String tmp=new String();
		List<String> declarations=new ArrayList<String>();
		for(Entry<String, List<String>> decl : declaration.entrySet()){		
			if(decl.getKey().compareTo("router")==0){
				
				StringBuilder gen=new StringBuilder().append("IPClassifier(");
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


	public List<String> generateLink(Network n) {
		final String symbol=" -> ";
		List<String> links=new ArrayList<String>();
		for(Map.Entry<String, String> l : link.entrySet()){
			StringBuilder tmp=new StringBuilder().append(l.getKey()).append(symbol).append(l.getValue());
			links.add(tmp.toString());
		}
	
	
		
		return links;
	}

}
