package it.polito.symnet.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import it.polito.symnet.converter.Network;
import it.polito.verigraph.model.*;

public class Nat {
	Map<String, String> ports=new HashMap<String, String>();
	Map<String, List<String>> declaration=new HashMap<String, List<String>>();
	Map<String, String> link=new HashMap<String, String>();
	String address;
	Map<String, String> addresses=new HashMap<String, String>();
	List<String>router=new ArrayList<String>();
	String name;

	
	public Nat(String address, List<String> n, Map<String, String> addresses, Graph graph, String name) {
		this.address=address;
		this.addresses=addresses;
		this.name=name;
		
		List<String> from=new ArrayList<String>();
		from.add("FromDevice");
		List<String> to=new ArrayList<String>();
		to.add("ToDevice");
		
		for(int i=0; i<n.size(); i++){
			
			if((graph.searchNodeByName(n.get(i)).getFunctional_type()).equals("firewall")|| 
					(graph.searchNodeByName(n.get(i)).getFunctional_type()).equals("webserver") ||
					(graph.searchNodeByName(n.get(i)).getFunctional_type()).equals("mailserver") ||
					(graph.searchNodeByName(n.get(i)).getFunctional_type()).equals("endpoint")){ 
				//port b, port toward extern
				ports.put(n.get(i), "b");
				declaration.put("b_in", from);				
				declaration.put("b_out", to);	
				link.put("b_in", "[1]nat");
			}else{			
				ports.put(n.get(i), "a"+i);
				declaration.put("a"+i+"_in", from);		
				link.put("a"+i+"_in", "router");
				declaration.put("a"+i+"_out", to);	
				link.put("a"+i+"_in", "a_in");
			}
		}
		
		declaration.put("a_in", from);				
		link.put("a_in", "router");
		
		
		
		
		
		List<String> tmp_router =new ArrayList<String>();
		for(Map.Entry<String, String> ad : addresses.entrySet()){
			
			if(ad.getKey().compareTo(name)==0){				
				continue;
			}
			tmp_router.add("dst host "+ad.getValue());
			
			router.add(ad.getKey());
			
			
			
		}
		tmp_router.add("-");
		router.add("-");		
		declaration.put("router", tmp_router);
		
		List<String> args=new ArrayList<String>();
		if(address!=null){		
			args.add("pattern "+address+ " - - - 0 1");
			args.add("keep 2 0");			
			declaration.put("nat", args);
		}
		
		link.put("nat[0]", "b_out");
		link.put("nat[1]", "router");
		link.put("nat[2]", "Discard");
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
				
			}else if(decl.getKey().compareTo("nat")==0){
				
				StringBuilder gen=new StringBuilder().append("IPRewriter(");				
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
		
		for(int i=0; i<router.size(); i++){
			if(i==router.size()-1){
				StringBuilder tmp=new StringBuilder().append("router["+i+"]").append(symbol).append("Discard");
				links.add(tmp.toString());
				
			}else{
				Map<String, String> tab=new HashMap<String, String>();
				tab=n.getElements().get(name);
				String out=new String();
				String adr=addresses.get(router.get(i));
			
				if((tab.get(adr)).equals("b"))
					out="nat";
				else
					out=tab.get(adr)+"_out";
								
				StringBuilder tmp=new StringBuilder().append("router["+i+"]").append(symbol).append(out);
				links.add(tmp.toString());
			}
		
		}
		return links;
		
	}
	
	
}
