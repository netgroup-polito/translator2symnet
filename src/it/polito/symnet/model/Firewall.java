package it.polito.symnet.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import it.polito.symnet.converter.Network;
import it.polito.verigraph.model.Graph;

public class Firewall {
	Map<String, String> ports=new HashMap<String, String>();
	Map<String, List<String>> declaration=new HashMap<String, List<String>>();
	Map<String, String> link=new HashMap<String, String>();
	List<String> filters=new ArrayList<String>();
	Map<String, String> addresses;
	String name;
	List<String> router=new ArrayList<String>();

	
	
	
	public Firewall(List<String> n, Map<String, String> addresses, List<String> filter, Graph graph, String name) {
		this.filters=filter;
		this.name=name;
		this.addresses=addresses;
		List<String> from=new ArrayList<String>();
		from.add("FromDevice");
		List<String> to=new ArrayList<String>();
		to.add("ToDevice");
		
		for(int i=0; i<n.size(); i++){
			//System.out.println("tipo porta: " + graph.searchNodeByName(n.get(i)).getFunctional_type());
			/*if((graph.searchNodeByName(n.get(i)).getFunctional_type()).equals("firewall") || 
					(graph.searchNodeByName(n.get(i)).getFunctional_type()).equals("webserver") ||
					(graph.searchNodeByName(n.get(i)).getFunctional_type()).equals("mailserver") ||
					(graph.searchNodeByName(n.get(i)).getFunctional_type()).equals("endpoint")){
				ports.put(n.get(i), "b");				
				declaration.put("b_in", from);				
				declaration.put("b_out", to);
				link.put("b_in", "fw");
				
			
			}else{
				ports.put(n.get(i), "a");				
				declaration.put("a_in", from);				
				declaration.put("a_out", to);
				link.put("a_in", "fw");
			}*/
			
			//foreach neighbour
			//ai_in :: FromDevice
			//ai_out :: ToDevice
			ports.put(n.get(i), "a"+i);				
			declaration.put("a"+i+"_in", from);				
			declaration.put("a"+i+"_out", to);
			link.put("a"+i+"_in", "fw");
			
		}		
		
		//general input and output
		declaration.put("a_in", from);				
		declaration.put("a_out", to);
		link.put("a_in", "fw");
		declaration.put("b_in", from);				
		declaration.put("b_out", to);
		link.put("b_in", "fw");
		
		List<String> tmp_router=new ArrayList<String>();
		
		//foreach address create arguments of IPClassifier
		//router :: IPClassifier(dst host address_1, ... , dst host address_n, -) 
		for(Map.Entry<String, String> ad : addresses.entrySet()){						
			tmp_router.add("dst host "+ad.getValue());
			router.add(ad.getKey());			
		}
		tmp_router.add("-");
		router.add("-");
		
		declaration.put("router", tmp_router);
		
		//foreach fw configuration create arguments for IPClassifier
		//fw :: IPClassifier(src host && dst host_1, ... , src host && dst host_n, -)
		filter.add("-");
		declaration.put("fw", filter);
		
		for(int i=0; i<filter.size(); i++){
			if(i==filter.size()-1)
				link.put("fw["+i+"]", "router");
			else
				link.put("fw["+i+"]", "Discard");
			
		}
		
		
		
		
	}
	
	
	public Map<String, String> getPorts(){
		return ports;
	}
	
	public List<String> generateDeclaration() {
		final String symbol=" :: ";
		String tmp=new String();
		List<String> declarations=new ArrayList<String>();		
		for(Entry<String, List<String>> decl : declaration.entrySet()){		
		//	System.out.println("decl: "+decl);
			
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
				
			}else if(decl.getKey().compareTo("fw")==0){
				
				StringBuilder gen=new StringBuilder().append("IPClassifier(");				
				for(int i=0; i<decl.getValue().size(); i++){					
					gen.append(decl.getValue().get(i));
					if(i==decl.getValue().size()-1)
						gen.append(")");
					else
						gen.append(", ");
				}
				tmp=new StringBuilder().append(decl.getKey()).append(symbol).append(gen.toString()).toString();
			}else{
				tmp=new StringBuilder().append(decl.getKey()).append(symbol).append(decl.getValue().get(0)).toString();
				//System.out.println("tmp: " + tmp);
			}
			
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
		
		//System.out.println("porte: " +ports);
		for(int i=0; i<router.size(); i++){
			if(i==router.size()-1){
				StringBuilder tmp=new StringBuilder().append("router["+i+"]").append(symbol).append("Discard");
				links.add(tmp.toString());
				
			}else{
				Map<String, String> tab=new HashMap<String, String>();
				tab=n.getElements().get(name);
				
				String adr=addresses.get(router.get(i));
			
								
				StringBuilder tmp=new StringBuilder().append("router["+i+"]").append(symbol).append(tab.get(adr)+"_out");
				links.add(tmp.toString());
			}
		
		}
		return links;
		
	}
	
	
	
	
}
