package it.polito.symnet.converter;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.polito.symnet.model.Dpi;
import it.polito.symnet.model.Elements;
import it.polito.symnet.model.Endhost;
import it.polito.symnet.model.Endpoint;
import it.polito.symnet.model.Firewall;
import it.polito.symnet.model.Generator;
import it.polito.symnet.model.Mailserver;
import it.polito.symnet.model.Nat;
import it.polito.symnet.model.ProtocolTypes;
import it.polito.symnet.model.Webserver;
import it.polito.verigraph.exception.DataNotFoundException;
import it.polito.verigraph.model.*;

public class Model {
	//graph elements
	Graph graph;
	List<Node> nodes=new ArrayList<Node>();	
	
	//Network elements
	List<Network> networks=new ArrayList<Network>();	
	Map<String, String> addresses=new HashMap<String, String>();
	
	//configuration nodes
	Map<String, String> protocols=new HashMap<String, String>();
	Map<String, String> ids=new HashMap<String, String>();
	Map<String, String> email_from=new HashMap<String, String>();	
	
	
	List<Map<String, Map<String, String>>> tmp_elements=new ArrayList<Map<String, Map<String, String>>>();
	
	List<String> host=new ArrayList<String>();
	Map<String, List<String>> neighbours=new HashMap<String, List<String>>();
	

	

	public List<Network> getNetworks(){
		return networks;
	}
	
	public Model(Graph graph) {
		this.graph=graph;
		Map<Long, Node> n=graph.getNodes();
		for(Map.Entry<Long, Node> s : n.entrySet()){
			nodes.add(s.getValue());
		}		
				
		protocols.put("HTTP_REQUEST", "1");
		protocols.put("HTTP_RESPONSE", "2");
		protocols.put("POP3_REQUEST", "3");
		protocols.put("POP3_RESPONSE", "4");
		
		Network net=new Network();

		createAddresses();
		
		List<String> ad=new ArrayList<String>();
		for(Map.Entry<String, String> a : addresses.entrySet()){
			ad.add(a.getKey());		
		}
		generateRouting(ad, net);
		createElements(ad, net);		
		generateNetwork();
		
		
		
	}

	private void createElements(List<String> ad, Network net) {		
		for(Node n : nodes){
			if(n.getFunctional_type().equals("endhost")){
				String body=new String();
				String protocol=new String();
				String email=new String();
				String name=n.getName();
				Map<String, String> filter=getConfigurations(n);
				for(Map.Entry<String, String> c : filter.entrySet()){
					if(c.getKey().equals("body"))
						body=c.getValue();
					else if(c.getKey().equals("protocol"))
						protocol=c.getValue();
					else if(c.getKey().equals("email_from"))
						email=c.getValue();
					else
						System.out.println("No configuation in "+n.getName()+" endhost");
				}
			
				Endhost endhost=new Endhost(addresses.get(name), addresses, protocol, body, email, name, neighbours.get(name), protocols, ids, email_from);
				net.setObject(name, endhost);
				net.setPorts(endhost.getPorts(), name);			
				host.add(name);
				
			}
		}
		
		//crea generator
		Generator generator=new Generator(host, graph);
		net.setGenerator(generator);
		net.setObject("generator", generator);
		net.setPorts(generator.getPorts(), "generator");
		
	}

	private void createAddresses() {
		Set<String> a=new HashSet<String>();
		List<String> notAddressing=new ArrayList<String>();
		notAddressing.add("firewall");
		notAddressing.add("dpi");		
		while(a.size()!=nodes.size())
			a.add(Integer.toString((int)(Math.random()*255)));		
		String prefix="192.168.1.";

		for(Node node : nodes){

			String type=node.getFunctional_type();
			if(notAddressing.contains(type)){
				continue;
			}else{
				String number=a.iterator().next();
				a.remove(number);
				String address=new StringBuilder().append(prefix).append(number).toString();
				addresses.put(node.getName(), address);

			}

		}


	}

	public void generateRouting(List<String> ad, Network net) {
		

		//routing_element contains name of node for create routing table
		List<String> routing_element=new ArrayList<String>();
		routing_element.add("firewall");
		routing_element.add("nat");
		routing_element.add("webserver");
		routing_element.add("mailserver");
		routing_element.add("dpi");
		routing_element.add("endpoint");

		List<String> route=new ArrayList<String>();

		for(int i=0; i<nodes.size(); i++){

			//create neighbours list
			Map<Long, Neighbour> neighbour=nodes.get(i).getNeighbours();
			List<String> n=new ArrayList<String>();
			for(Map.Entry<Long, Neighbour> d : neighbour.entrySet()){
				n.add(d.getValue().getName());
			}
			neighbours.put(nodes.get(i).getName(), n);			
		
			//insert name routing
			String type=nodes.get(i).getFunctional_type();
			if(routing_element.contains(type)){
				route.add(nodes.get(i).getName());

			}
			
		}


		Map<String, Map<String, String>> elements=new HashMap<String, Map<String, String>>();

		for(int i=0; i<route.size(); i++){	

			String name=route.get(i);
			String type=graph.searchNodeByName(name).getFunctional_type();
			Node node=graph.searchNodeByName(name);
			
			//System.out.println("TIPO: " + type);
			switch(type){			
			
			case "firewall" : {
				List<String> n=neighbours.get(name);		
				List<String> filter=new ArrayList<String>();
				filter=getConfiguration(node);				
				Firewall f=new Firewall(n, addresses, filter, graph, name);
				//System.out.println("porte fiewall: " + f.getPorts());
				Map<String, String> map=new HashMap<String, String>();

				//search path foreach address
				for(String adr : ad){
					if(adr.equals(name))
						continue;
				//	System.out.println("address: " + adr);
					int found=0;
					if(n.contains(adr)){
						
						map.put(addresses.get(adr), f.getPorts().get(adr));						
					}
					else{
						for(int j=0; j<n.size(); j++){
							String tmp=n.get(j);
							//System.out.println("j :" + j + "name: " + n.get(j));
							Set<String>visited=new HashSet<String>();
							visited.add(name);
							researchRoute(visited, map, adr, tmp, name, n.get(j), f.getPorts(), found, neighbours);
						}
					}

				}
				elements.put(name, map);
				net.setElements(elements);
				net.setObject(name, f);
				net.setPorts(f.getPorts(), name);
			//	System.out.println(map);

				break;

			}

			case "nat" : {
			List<String> n=neighbours.get(name);							
			Nat nat=new Nat(addresses.get(name), n, addresses, graph, name);		
			Map<String, String> map=new HashMap<String, String>();

			//search path foreach address
			for(String adr : ad){
				if(adr.equals(name))
					continue;
				//System.out.println("address: " + adr);
				int found=0;
				if(n.contains(adr)){
					
					map.put(addresses.get(adr), nat.getPorts().get(adr));						
				}
				else{
					for(int j=0; j<n.size(); j++){
						String tmp=n.get(j);
						Set<String>visited=new HashSet<String>();
						visited.add(name);
						researchRoute(visited, map, adr, tmp, name, n.get(j), nat.getPorts(), found, neighbours);
					}
				}

			}
			elements.put(name, map);
			net.setElements(elements);
			net.setObject(name, nat);
			net.setPorts(nat.getPorts(), name);
			//System.out.println(map);

			break;

		}
		case "webserver" : {
			List<String> n=neighbours.get(name);							
			Webserver ws=new Webserver(addresses.get(name), n, addresses, name, protocols);			
			Map<String, String> map=new HashMap<String, String>();

			//search path foreach address
			for(String adr : ad){
				if(adr.equals(name))
					continue;
				//System.out.println("address: " + adr);
				int found=0;
				if(n.contains(adr)){
					
					map.put(addresses.get(adr), ws.getPorts().get(adr));						
				}
				else{
					for(int j=0; j<n.size(); j++){
						String tmp=n.get(j);
						//System.out.println("j :" + j + "name: " + n.get(j));
						Set<String>visited=new HashSet<String>();
						visited.add(name);
						researchRoute(visited, map, adr, tmp, name, n.get(j), ws.getPorts(), found, neighbours);
					}
				}

			}
			elements.put(name, map);
			net.setElements(elements);
			net.setObject(name, ws);
			net.setPorts(ws.getPorts(), name);
			host.add(name);
		//	System.out.println(map);

			break;

		}
		
		case "mailserver" : {
			List<String> n=neighbours.get(name);							
			Mailserver ms=new Mailserver(addresses.get(name), n, addresses, name, protocols);
			Map<String, String> map=new HashMap<String, String>();

			//search path foreach address
			for(String adr : ad){
				if(adr.equals(name))
					continue;
				//System.out.println("address: " + adr);
				int found=0;
				if(n.contains(adr)){
					
					map.put(addresses.get(adr), ms.getPorts().get(adr));						
				}
				else{
					for(int j=0; j<n.size(); j++){
						String tmp=n.get(j);
						//System.out.println("j :" + j + "name: " + n.get(j));
						Set<String>visited=new HashSet<String>();
						visited.add(name);
						researchRoute(visited, map, adr, tmp, name, n.get(j), ms.getPorts(), found, neighbours);
					}
				}

			}
			elements.put(name, map);
			net.setElements(elements);
			net.setObject(name, ms);
			net.setPorts(ms.getPorts(), name);
			host.add(name);
			//System.out.println(map);

			break;

		}
		
		case "dpi" : {
			List<String> n=neighbours.get(name);	
			List<String> filter=new ArrayList<String>();
			filter=getConfiguration(node);
			//System.out.println("dpi: " +n);
			Dpi dpi=new Dpi(n, addresses, filter, graph, name, ids);			
			Map<String, String> map=new HashMap<String, String>();

			//search path foreach address
			for(String adr : ad){
				if(adr.equals(name))
					continue;
				//System.out.println("address: " + adr);
				int found=0;
				//System.out.println(dpi.getPorts());
				if(n.contains(adr)){
					
					map.put(addresses.get(adr), dpi.getPorts().get(adr));						
				}
				else{
					for(int j=0; j<n.size(); j++){
						String tmp=n.get(j);
						//System.out.println("j :" + j + "name: " + n.get(j));
						Set<String>visited=new HashSet<String>();
						visited.add(name);
						researchRoute(visited, map, adr, tmp, name, n.get(j), dpi.getPorts(), found, neighbours);
					}
				}

			}
			elements.put(name, map);
			net.setElements(elements);
			net.setObject(name, dpi);
			net.setPorts(dpi.getPorts(), name);
		//	System.out.println(map);

			break;

		}
		
		case "endpoint" : {
			//System.out.println("endpoin entrato");
			List<String> n=neighbours.get(name);							
			Endpoint ep=new Endpoint(addresses.get(name), n, addresses, name, protocols);			
			Map<String, String> map=new HashMap<String, String>();

			//search path foreach address
			for(String adr : ad){
				if(adr.equals(name))
					continue;
				//System.out.println("address: " + adr);
				int found=0;
				if(n.contains(adr)){
					
					map.put(addresses.get(adr), ep.getPorts().get(adr));						
				}
				else{
					for(int j=0; j<n.size(); j++){
						String tmp=n.get(j);
						//System.out.println("j :" + j + "name: " + n.get(j));
						Set<String>visited=new HashSet<String>();
						visited.add(name);
						researchRoute(visited, map, adr, tmp, name, n.get(j), ep.getPorts(), found, neighbours);
					}
				}

			}
			elements.put(name, map);
			net.setElements(elements);
			net.setObject(name, ep);
			//System.out.println(net.getAllObject());
			net.setPorts(ep.getPorts(), name);
			//host.add(name);
			break;

		}
		
		case "endhost":{			
			break;
		}
		
		default:{
			System.out.println("Element "+name+"not valid.");
			break;
		}
		
		}

		}
		networks.add(net);
		Network e=networks.get(0);
		Map<String, Map<String, String>> v=e.getElements();
		
		

	}


	private Map<String, String> getConfigurations(Node node) {
		
		Map<String, String> config=new HashMap<String, String>();
		String funcType=node.getFunctional_type();
		switch(funcType.toUpperCase()){		
	
		case "ENDHOST" : {
		
		String body=new String();			
		String protocol=new String();
		String email=new String();
		
		String empty="[]";
		JsonNode conf=node.getConfiguration().getConfiguration();
		if(!conf.toString().equals(empty)){
			ObjectMapper mapper=new ObjectMapper();		
			java.util.Map<String, String> map=new LinkedHashMap();			
			String input;
			Matcher matcher = Pattern.compile("\\[([^\\]]*)\\]").matcher(conf.toString());
			if(matcher.find()){
				input=matcher.group(1);
			}else
				input=conf.toString();
			
			try{
			map=mapper.readValue(input, java.util.LinkedHashMap.class);
			for(java.util.Map.Entry<String, String> m : map.entrySet()){				
				switch(m.getKey()){
				
				case "body":{
					body=m.getValue();
					break;
				}
				
				case "protocol":{					
					ProtocolTypes proto=ProtocolTypes.fromValue(m.getValue());
					protocol=proto.value();
					break;
				}
				case "email_from":{
					email=m.getValue();
					break;
				}
									
				
				default:
					break;
				}
				
				if(!body.isEmpty())
					config.put("body", body);
				if(!protocol.isEmpty())
					config.put("protocol", protocol);
				if(!email.isEmpty())
					config.put("email_from", email);
				
			}
			
			}catch(JsonGenerationException ex) {
	
		        ex.printStackTrace();
	
		    } catch (JsonMappingException ex) {
	
		        ex.printStackTrace();
	
		    } catch (IOException ex) {
	
		        ex.printStackTrace();
	
		    }catch(IllegalArgumentException ia){
				System.out.println("Error in json conversion: ");
				ia.printStackTrace();
			}
		
			
		}
		else{
			System.out.println("endhost empty");
		}			
	
		break;
	
		
		}		
	
		}
		return config;
		
	}

	private List<String> getConfiguration(Node node) {
		List<String> filter=new ArrayList<String>();
		String funcType=node.getFunctional_type();
		switch(funcType.toUpperCase()){		
	
		
		case "FIREWALL" : {
			
			String empty="[]";
		
			JsonNode conf=node.getConfiguration().getConfiguration();
			
			List<Elements> elements_list=new ArrayList<Elements>();
			
			if(!conf.toString().equals(empty)){	
				ObjectMapper mapper=new ObjectMapper();			
				java.util.Map<String, String> map=new LinkedHashMap();		
				String input;
				Matcher matcher = Pattern.compile("\\[([^\\]]*)\\]").matcher(conf.toString());
				if(matcher.find()){
					input=matcher.group(1);
				}else
					input=conf.toString();
				Pattern pattern=Pattern.compile("\\{([^\\}]*)\\}");
				List<String> list = new ArrayList<String>();
				Matcher match= pattern.matcher(input);
				while (match.find()) {
				    list.add(match.group());
				}	
							
				try{
					for(String string : list){
						map.putAll(mapper.readValue(string, LinkedHashMap.class));	
						for(java.util.Map.Entry<String, String> m : map.entrySet()){
							Elements elem=new Elements();
							Matcher dst_address=Pattern.compile("ip_").matcher(m.getKey());
							if(dst_address.find()){
								String[]values=m.getKey().split("ip_");
								elem.setDestination(values[1]);
							}else							
								elem.setDestination(m.getKey());
							Matcher src_address=Pattern.compile("ip_").matcher(m.getValue());
							if(src_address.find()){
								String[]values=m.getValue().split("ip_");
								elem.setSource(values[1]);
							}else		
								elem.setSource(m.getValue());	
							elements_list.add(elem);
						}
					}
				
			
				}catch(JsonGenerationException ex) {
		
			        ex.printStackTrace();
		
			    } catch (JsonMappingException ex) {
		
			        ex.printStackTrace();
			        
			    }catch(IllegalArgumentException ia){
					System.out.println("Error in json conversion: ");
					ia.printStackTrace();
						
			    }catch (IOException ex) {
		
			        ex.printStackTrace();
		
			    }
			
			
				
			}
				else{
					System.out.println("elements_list empty");
					
				}
			
			for(Elements e : elements_list){
				String src="src host ";
				String dst="dst host ";
				String and=" && ";
				if((addresses.get(e.getSource())!=null) && (addresses.get(e.getDestination())!=null)){		
					String a=new StringBuilder().append(src).append(addresses.get(e.getSource())).append(and).append(dst).append(addresses.get(e.getDestination())).toString();
					filter.add(a);			
				}else{
					System.err.println("Error in "+node.getName()+" configuration, Check and retry.");
					throw new DataNotFoundException("Error in "+node.getName()+" configuration, Check and retry.");
				}
					
			}
			
			
			break;
		}
		
		case "DPI" : {
			String empty="[]";
			
			JsonNode conf=node.getConfiguration().getConfiguration();
						
			List<String> notAllowed=new ArrayList<String>();
			
			if(!conf.toString().equals(empty)){
				ObjectMapper mapper=new ObjectMapper();
				
				List<String> list=new ArrayList<String>();
				 try {
		
				        list = mapper.readValue(conf.toString(), ArrayList.class);
				      
				        for(String s : list){			    	
				    		notAllowed.add(s);
				        }
				       
				      
		
				    } catch (JsonGenerationException ex) {
		
				        ex.printStackTrace();
		
				    } catch (JsonMappingException ex) {
		
				        ex.printStackTrace();
				    }catch(IllegalArgumentException ia){
						System.out.println("Error in json conversion: ");
						ia.printStackTrace();					
		
				    } catch (IOException ex) {
		
				        ex.printStackTrace();
		
				    }
				
				}
			else{
				System.out.println("dpi empty");
			}
			
			filter=notAllowed;
			break;
		}
		
	
			
		}
			
		
		return filter;
	}

	private void researchRoute(Set<String> visited, Map<String, String> map, String address, String name_tmp,
			String name_obj, String name, Map<String, String> ports, int found, Map<String, List<String>> neighbours) {

		//System.out.println("name_tmp: " + name_tmp.get(1));
	
		List<String> neigh_tmp=neighbours.get(name_tmp);		
		if(neigh_tmp.contains(address)){
			Map<String, String> tuple=new HashMap<String, String>();
			tuple.put(addresses.get(address), ports.get(name));
			if(found==0){				
				map.put(addresses.get(address), ports.get(name));
			//	System.out.println("Added: " + addresses.get(address));
			}else if(found>0){	
				//if that address is already in the map:
				if(!(map.get(addresses.get(address)).equals(tuple.get(addresses.get(address))))){
					Map<String, Map<String, String>> elem=new HashMap<String, Map<String, String>>();
					elem.put(name_obj, tuple);
					if(found>tmp_elements.size()){					
						tmp_elements.add(found-1, elem);
					}else{
						Map<String, Map<String, String>> elem_tmp=tmp_elements.get(found-1);
						elem_tmp.put(name_obj, tuple);					
					}
				//	System.out.println("Added: " + addresses.get(address));
				}
				else
					found=found-1;
				
			}
			found= found+1;

		}else{

			for(int k=0; k<neigh_tmp.size(); k++){	
				String tmp=neigh_tmp.get(k);
				
				if(visited.contains(tmp)){
			
					continue;
				}
				
				
				//System.out.println("name_tmp " + tmp);
				visited.add(tmp);
				researchRoute(visited, map, address, tmp, name_obj, name, ports, found, neighbours);


			}

		}
		
	}

	public void generateNetwork() {
		
		
		if(!tmp_elements.isEmpty()){
			//replace networks from the first
			
			
			Network network=networks.get(0);
			Map<String, Object> object=network.getAllObject();
			Map<String, Object> m=new HashMap<String, Object>();
			for(Map.Entry<String, Object> obj : object.entrySet()){
				m.put(obj.getKey(), obj.getValue());
			}			
			Map<String, Map<String, String>> ports=network.getAllPort();
			Map<String, Map<String, String>> p=new HashMap<String, Map<String, String>>();
			for(Map.Entry<String, Map<String, String>> port : ports.entrySet()){
				p.put(port.getKey(), new HashMap<String, String>(port.getValue()));				
			}	
			
			Map<String, Map<String, String>> elem=network.getElements();
			Map<String, Map<String, String>> e=new HashMap<String, Map<String, String>>();
			for(Map.Entry<String, Map<String, String>> el : elem.entrySet()){
				e.put(el.getKey(), new HashMap<String, String>(el.getValue()));				
			}		
			
			
			//a new network foreach element i of tmp_elements
			for(int i=0; i<tmp_elements.size(); i++){
				
				for(Map.Entry<String, Map<String, String>> tmp : tmp_elements.get(i).entrySet()){
				
				//new network, replace value of network[0]
				Network n=new Network();				
				n.setPorts(p);				
				n.setObject(m);
				n.setElements(e);
								
				
				//new element to insert
				String name=tmp.getKey();
				Map<String, String> element=tmp.getValue();
				
				for(Map.Entry<String, String> a : element.entrySet()){
					//replace the element to insert in the new network
					n.setSingleElement(name, a.getKey(), a.getValue());					
				}
				network.setGenerator(networks.get(0).getGenerator());
				networks.add(n);
				}
			}
			
			
		}
		
		/*for(Network nn : networks){
			System.out.println(nn.getElements());
		}*/
	}
	
}











