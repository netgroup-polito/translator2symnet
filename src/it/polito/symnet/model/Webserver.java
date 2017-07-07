package it.polito.symnet.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import it.polito.symnet.converter.Network;

public class Webserver {
	
	Map<String, String> ports=new HashMap<String, String>();
	Map<String, List<String>> declaration=new HashMap<String, List<String>>();
	Map<String, String> link=new HashMap<String, String>();
	String address;
	String name;
	Map<String, String> addresses;
	List<String>router=new ArrayList<String>();
	
	public Webserver(String address, List<String> n, Map<String, String> addresses, String name, Map<String, String> protocols) {
		this.address=address;
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
			link.put("a"+i+"_in", "a_in");
			
			
		}
		
		//general input and output
		declaration.put("a_in", from);
		link.put("a_in", "ws");
		
		declaration.put(name+"_in", from);
		declaration.put(name+"_out", to);
		
		//create response
		//response :: ApplicationProto(app proto http_request, app proto http_response, app proto pop3_request, app proto pop3_response, -)
		String application="app proto ";		
		List<String> filter=new ArrayList<String>();
		filter.add(new StringBuilder().append(application).append(protocols.get("HTTP_REQUEST")).toString());
		filter.add(new StringBuilder().append(application).append(protocols.get("HTTP_RESPONSE")).toString());
		filter.add(new StringBuilder().append(application).append(protocols.get("POP3_REQUEST")).toString());
		filter.add(new StringBuilder().append(application).append(protocols.get("POP3_RESPONSE")).toString());
		filter.add("-");
		
		declaration.put("response", filter);
		
		
		
		String http_req=protocols.get("HTTP_REQUEST");
		List<String> args=new ArrayList<String>();	
		int index=1;
		List<String>tmp_router=new ArrayList<String>();
		
		
		tmp_router.add(0, "dst host "+addresses.get(name));
		router.add(0, name);
		
		//foreach address create arguments of IPClassifier
		//router :: IPClassifier(dst host address_1, ... , dst host address_n, -) 
		for(Map.Entry<String, String> ad : addresses.entrySet()){		
			
			if(ad.getKey().compareTo(name)==0){				
				//tmp_router.add(0, "dst host "+ad.getValue());
				//router.add(0, ad.getKey());
				continue;
			}
		
			String arg=new StringBuilder().append("web ").append(address+" ").append(ad.getValue()+" ").append("50 ").append(http_req).toString();
			args.add(arg);
			tmp_router.add(index, "dst host "+ad.getValue());
			router.add(index, ad.getKey());
			index++;
			
			
		}
		tmp_router.add(index, "-");
		router.add(index, "-");
		declaration.put("generator", args);
		declaration.put("router", tmp_router);
		List<String> ws=new ArrayList<String>();
		ws.add(0,new StringBuilder().append("dst host ").append(address).toString());
		ws.add(1, "-");
		declaration.put("ws", ws);
		
		link.put(name+"_in", "generator");
		for(int k=0; k<addresses.size(); k++){
			link.put("generator["+k+"]", "router");
		}
		
		
		
		String http=protocols.get("HTTP_RESPONSE");
		String pop=protocols.get("POP3_RESPONSE");
	
		String res_http=new StringBuilder().append("Response() -> Generator(proto ").append(http).append(")").append(" -> router").toString();
		String res_pop=new StringBuilder().append("Response() -> Generator(proto ").append(pop).append(")").append(" -> router").toString();
		
		for(int j=0; j<filter.size(); j++){
			if(j==0)
				link.put("response["+j+"]", res_http);
			else if(j==2)
				link.put("response["+j+"]", res_pop);
			else 
				link.put("response["+j+"]", name+"_out");
				
				
	
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
			if(decl.getKey().compareTo("generator")==0){				
				
				StringBuilder gen=new StringBuilder().append("Generator(");				
				for(int i=0; i<decl.getValue().size(); i++){					
					gen.append(decl.getValue().get(i));
					if(i==decl.getValue().size()-1)
						gen.append(")");
					else
						gen.append(", ");
				}
				tmp=new StringBuilder().append(decl.getKey()).append(symbol).append(gen.toString()).toString();
			}else if(decl.getKey().compareTo("response")==0){
				
				StringBuilder gen=new StringBuilder().append("ApplicationClassifier(");
				for(int i=0; i<decl.getValue().size(); i++){					
					gen.append(decl.getValue().get(i));
					if(i==decl.getValue().size()-1)
						gen.append(")");
					else
						gen.append(", ");
			}
				tmp=new StringBuilder().append(decl.getKey()).append(symbol).append(gen.toString()).toString();
				
			}else if(decl.getKey().compareTo("router")==0){
				
				StringBuilder gen=new StringBuilder().append("IPClassifier(");
				for(int i=0; i<decl.getValue().size(); i++){					
					gen.append(decl.getValue().get(i));
					if(i==decl.getValue().size()-1)
						gen.append(")");
					else
						gen.append(", ");
			}
				tmp=new StringBuilder().append(decl.getKey()).append(symbol).append(gen.toString()).toString();
				
			}else if(decl.getKey().compareTo("ws")==0){
				
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
		

		for(int i=0; i<router.size(); i++){
			if(i==0){
				StringBuilder tmp=new StringBuilder().append("router[0]").append(symbol).append("response");
				links.add(tmp.toString());
			}else if(i==router.size()-1){
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

		//StringBuilder tmp=new StringBuilder().append("ws[0]").append(symbol).append("router");
		StringBuilder tmp=new StringBuilder().append("ws[0]").append(symbol).append("response");
		links.add(tmp.toString());
	
		StringBuilder tmp2=new StringBuilder().append("ws[1]").append(symbol).append("Discard");
		links.add(tmp2.toString());	
		
		
		return links;
	}

}
