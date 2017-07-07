package it.polito.symnet.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Endhost {	

	Map<String, String> ports=new HashMap<String, String>();
	Map<String, List<String>> declaration=new HashMap<String, List<String>>();
	Map<String, String> link=new HashMap<String, String>();
	String address;
	String body=new String();
	String protocol=new String();
	String email=new String();	
	String name;

	

	public Endhost(String address, Map<String, String> addresses, String proto, String body, String email, String name, List<String> list, Map<String, String> protocols, Map<String, String> ids, Map<String, String> email_from) {

		this.body=body;
		this.protocol=proto;
		this.email=email;
		this.name=name;
		


		List<String> from=new ArrayList<String>();
		from.add("FromDevice");
		List<String> to=new ArrayList<String>();
		to.add("ToDevice");

		
		//fill ports	
		for(String l : list){			
			ports.put(name, l);
		}		
	
		
		//foreach port:
		//a_in :: FromDevice
		//a_out :: ToDevice
		declaration.put("a_in", from);
		declaration.put("b_in", from);
		
		declaration.put(name+"_out", to);
		declaration.put(name, to);	
		
		declaration.put(name+"_in", from);
		link.put(name+"_in", "generator");

		//ports link
		//a_in -> generator
		//b_in -> response
		link.put("a_in", "generator");
		link.put("b_in", "response");


		//create generator generator :: Generator(web ...., web ....)		
		String http_req=protocols.get("HTTP_REQUEST");
		String pop_req=protocols.get("POP3_REQUEST");
		
		if(proto.equals("HTTP_REQUEST") || proto.equals("POP3_REQUEST")){
			List<String> args=new ArrayList<String>();
			String b=new String();
			if(!body.isEmpty()){
				if(ids.get(body)==null){
					int val;
					if(ids.size()==0){
						val=100;
					}else
						val=ids.size()+1+100;					
					String value=String.valueOf(val).toString();
					ids.put(body, value);
					b=value;
				}else
					b=ids.get(body);
					
				
			}
			else{
				b="50";
			}

			for(Map.Entry<String, String> ad : addresses.entrySet()){			
				if(ad.getKey().compareTo(name)==0)
					continue;
			
				String arg=new StringBuilder().append("web ").append(address+" ").append(ad.getValue()+" ").append(b+" ").append(http_req).toString();
				args.add(arg);
				
				
			}
			declaration.put("generator", args);
			for(int i=0; i<addresses.size(); i++){
				link.put("generator["+i+"]", name+"_out");
			}
		}else{
			String e=new String();
			if(!email.isEmpty()){
				if(email_from.get(email)==null){
					int val;
					if(email_from.size()==0){
						val=100;
					}else
						val=email_from.size()+1+1000;					
					String value=String.valueOf(val).toString();
					email_from.put(email, value);
					e=value;
				}else
					e=email_from.get(email);
			}
			else{
				e="50";
			}

			List<String> args=new ArrayList<String>();
			for(Map.Entry<String, String> ad : addresses.entrySet()){
				if(ad.getKey().compareTo(name)==0)
					continue;
				String arg=new StringBuilder().append("mail ").append(address+" ").append(ad.getValue()+" ").append(e+" ").append(pop_req).toString();
				args.add(arg);
				
			}
			declaration.put("generator", args);
			
			//create links
			//generator[n] -> endhost_out
			for(int i=0; i<addresses.size(); i++){
				link.put("generator["+i+"]", name+"_out");
			}
		}

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

		String http=protocols.get("HTTP_RESPONSE");
		String pop=protocols.get("POP3_RESPONSE");


		String res_http=new StringBuilder().append("Response() -> Generator(proto ").append(http).append(")").append(" -> "+name+"_out").toString();
		String res_pop=new StringBuilder().append("Response() -> Generator(proto ").append(pop).append(")").append(" -> "+name+"_out").toString();

		for(int j=0; j<filter.size(); j++){
			if(j==0)
				link.put("response["+j+"]", res_http);
			else if(j==2)
				link.put("response["+j+"]", res_pop);
			else 
				link.put("response["+j+"]", name);



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
				
			}else
				tmp=new StringBuilder().append(decl.getKey()).append(symbol).append(decl.getValue().get(0)).toString();
			
			declarations.add(tmp);
		}
		
		return declarations;
	}


	public List<String> generateLink() {
		final String symbol=" -> ";
		List<String> links=new ArrayList<String>();
		for(Map.Entry<String, String> l : link.entrySet()){
			StringBuilder tmp=new StringBuilder().append(l.getKey()).append(symbol).append(l.getValue());
			links.add(tmp.toString());
		}
		
		return links;
		
	}


	




}
