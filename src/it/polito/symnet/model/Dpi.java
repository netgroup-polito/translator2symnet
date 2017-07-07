package it.polito.symnet.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import it.polito.symnet.converter.Network;
import it.polito.verigraph.model.Graph;

public class Dpi {
	Map<String, String> ports=new HashMap<String, String>();
	Map<String, List<String>> declaration=new HashMap<String, List<String>>();
	Map<String, String> link=new HashMap<String, String>();
	List<String> filters=new ArrayList<String>();
	Map<String, String> addresses;
	String name;
	List<String> router=new ArrayList<String>();


	
	public Dpi(List<String> n, Map<String, String> addresses, List<String> filter, Graph graph, String name, Map<String, String> ids) {
		this.filters=filter;
		this.name=name;
		this.addresses=addresses;
		
		List<String> from=new ArrayList<String>();
		from.add("FromDevice");
		
		List<String> to=new ArrayList<String>();
		to.add("ToDevice");
		
		for(int i=0; i<n.size(); i++){
			
			//foreach neighbour
			//ai_in :: FromDevice
			//ai_out :: ToDevice
			ports.put(n.get(i), "a"+i);				
			declaration.put("a"+i+"_in", from);				
			declaration.put("a"+i+"_out", to);
			link.put("a"+i+"_in", "dpi");
			
		}
		
		//general input:
		//a_in :: FromDevice
		//a_out :: ToDevice
		declaration.put("a_in", from);				
		declaration.put("a_out", to);
		link.put("a_in", "dpi");
		declaration.put("b_in", from);				
		declaration.put("b_out", to);
		link.put("b_in", "dpi");
		
		//foreach address create arguments of IPClassifier
		//router :: IPClassifier(dst host address_1, ... , dst host address_n, -) 
		List<String> tmp_router=new ArrayList<String>();
		for(Map.Entry<String, String> ad : addresses.entrySet()){			
						
			tmp_router.add("dst host "+ad.getValue());
			router.add(ad.getKey());		
		}
		tmp_router.add("-");
		router.add("-");		
		declaration.put("router", tmp_router);
		
		
		//foreach dpi configuration create arguments for ApplicationClassifier
		//dpi :: ApplicationClassifier(body val_1, ... , body val_n, -)
		List<String> filt=new ArrayList();
		for(String f : filters){		
			String fil=new String();
			if(ids.get(f)==null){
				int val;
				if(ids.size()==0){
					val=100;
				}else
					val=ids.size()+1+100;					
				String value=String.valueOf(val).toString();
				ids.put(f, value);
				fil=value;
			}else
				fil=ids.get(f);						
				
			filt.add("body "+fil);
		}
		filt.add("-");
		declaration.put("dpi", filt);
		
		
		//create link of dpi
		//dpi[n] -> router
		for(int i=0; i<filt.size(); i++){
			if(i==filt.size()-1)
				link.put("dpi["+i+"]", "router");
			else
				link.put("dpi["+i+"]", "Discard");
			
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
				
			}else if(decl.getKey().compareTo("dpi")==0){
				
				StringBuilder gen=new StringBuilder().append("ApplicationClassifier(");				
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
