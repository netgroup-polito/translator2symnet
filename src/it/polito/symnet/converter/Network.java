package it.polito.symnet.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.polito.symnet.model.Dpi;
import it.polito.symnet.model.Endhost;
import it.polito.symnet.model.Endpoint;
import it.polito.symnet.model.Firewall;
import it.polito.symnet.model.Generator;
import it.polito.symnet.model.Mailserver;
import it.polito.symnet.model.Nat;
import it.polito.symnet.model.Webserver;

public class Network {
	//routing table
	Map<String, Map<String, String>> elements_routing=new HashMap<String, Map<String, String>>();
	
	//ports of objects model
	Map<String, Map<String, String>> ports=new HashMap<String, Map<String, String>>();
	
	//objects model
	Map<String, Object> obj=new HashMap<String, Object>();
	
	//generator
	Generator generator;
	
	public void setGenerator(Generator generator){
		this.generator=generator;
	}
	
	public Generator getGenerator(){
		if(generator!=null)
			return generator;
		else
			return null;
	}
	public Map<String, Map<String, String>> getElements(){
		if(!elements_routing.isEmpty()){
			return elements_routing;
		}
		else
			return null;
	}
	
	public void setElements(Map<String, Map<String, String>> element){
		if(element!=null){
			for(Map.Entry<String, Map<String, String>> e : element.entrySet()){
				elements_routing.put(e.getKey(), new HashMap<String, String>(e.getValue()));
				
			}
		}
	}
	
	public void setSingleElement(String name, String dest, String next){
		elements_routing.get(name).put(dest, next);
		
	}
	
	
	
	public void setObject(String name, Object o){
		if(obj!=null)
			obj.put(name, o);
	}
	
	public void setObject(Map<String, Object> obj){
		if(obj!=null)
			obj.putAll(obj);
	}
	
	
	public Object getSingleObject(String name){
		if(name!=null){
			return obj.get(name);
		}else
			return null;
	}
	
	public Map<String, Object> getAllObject(){
		if(!obj.isEmpty())
			return obj;
		else
			return null;
	}
	
	public void setPorts(Map<String, String> port, String name){
		if(ports!=null){
			ports.put(name, port);
		}
	}
	
	public void setPorts(Map<String, Map<String, String>> port){
		if(ports!=null){
			for(Map.Entry<String, Map<String, String>> e : port.entrySet()){
				ports.put(e.getKey(), new HashMap<String, String>(e.getValue()));
				
			}
		}
	}
	
	public Map<String, String> getSinglePort(String name){
		if(name!=null){
			return ports.get(name);			
		}else
			return null;
		
	}
	public Map<String, Map<String, String>> getAllPort(){
		if(ports!=null){
			return ports;			
		}else
			return null;
		
	}
	
	public Map<String, String> getElement(String name){
		return elements_routing.get(name);
	}

	public List<String> generateLinks() {
		final String separator=":";
		final String arrow="->";
		List<String> links=new ArrayList<String>();
		String tmp=new String();
		boolean dpi_extern=false;
		
		//generator
		
		for(Map.Entry<String, Object> object : obj.entrySet()){
			String name=object.getKey();
			Object o=object.getValue();
			if(o instanceof Generator){
				Map<String, String> g=generator.getPorts();
				for(Map.Entry<String, String> t : g.entrySet()){
					tmp=new StringBuilder().append("generator").append(separator).append(t.getKey()).append(separator).append("0").append(arrow).append(t.getKey()).append(separator).append(t.getValue()).append(separator).append(0).toString();
					links.add(tmp);
				}
			}else if(o instanceof Endhost){
				Map<String, String> g=ports.get(name);
				for(Map.Entry<String, String> t : g.entrySet()){
					String neigh=t.getKey();	
					tmp=new StringBuilder().append(name).append(separator).append(t.getKey()+"_out").append(separator).append("0").append(arrow).append(t.getValue()).append(separator).append("a_in").append(separator).append(0).toString();
					links.add(tmp);
				}
			}else if(o instanceof Webserver){
				Map<String, String> g=ports.get(name);
				for(Map.Entry<String, String> t : g.entrySet()){
					String neigh=t.getKey();		
					String arg=new String();	
					if(obj.get(neigh) instanceof Dpi){
						dpi_extern=true;
					}
					if(obj.get(neigh) instanceof Endhost){
						arg="b_in";
					}else if(obj.get(neigh) instanceof Nat){						
							arg="b_in";						
					}else if((ports.get(neigh)).get(name)==null){						
							arg="a_in";						
					}else
						arg=(ports.get(neigh)).get(name)+"_in";
					
					tmp=new StringBuilder().append(name).append(separator).append(t.getValue()+"_out").append(separator).append("0").append(arrow).append(t.getKey()).append(separator).append(arg).append(separator).append(0).toString();
					links.add(tmp);
				}
			}else if(o instanceof Firewall){
				Map<String, String> g=ports.get(name);
				for(Map.Entry<String, String> t : g.entrySet()){
					String neigh=t.getKey();
					String arg=new String();
					if(obj.get(neigh) instanceof Dpi){
						dpi_extern=true;
					}
					if(obj.get(neigh) instanceof Endhost)
						arg="b_in";
					else if(obj.get(neigh) instanceof Nat){
						arg="b_in";
					}else if((ports.get(neigh)).get(name)==null){						
						arg="a_in";						
					}else
						arg=(ports.get(neigh)).get(name)+"_in";
					
					tmp=new StringBuilder().append(name).append(separator).append(t.getValue()+"_out").append(separator).append("0").append(arrow).append(t.getKey()).append(separator).append(arg).append(separator).append(0).toString();
					links.add(tmp);
				}
				
			}else if(o instanceof Dpi){
				Map<String, String> g=ports.get(name);
				for(Map.Entry<String, String> t : g.entrySet()){
					String neigh=t.getKey();
					String arg=new String();
					if(obj.get(neigh) instanceof Endhost)
						arg="b_in";
					else if(obj.get(neigh) instanceof Nat){
						if(dpi_extern)
							arg="b_in";
						else
							arg="a_in";
					}else if((ports.get(neigh)).get(name)==null){						
						arg="a_in";			
					}else
						arg=(ports.get(neigh)).get(name)+"_in";
					tmp=new StringBuilder().append(name).append(separator).append(t.getValue()+"_out").append(separator).append("0").append(arrow).append(t.getKey()).append(separator).append(arg).append(separator).append(0).toString();
					links.add(tmp);
			}
				
			}else if(o instanceof Endpoint){
				Map<String, String> g=ports.get(name);
				for(Map.Entry<String, String> t : g.entrySet()){
					String neigh=t.getKey();
					String arg=new String();
					if(obj.get(neigh) instanceof Dpi){
						dpi_extern=true;
					}
					if(obj.get(neigh) instanceof Endhost){
						arg="b_in";
					}else if(obj.get(neigh) instanceof Nat){						
							arg="b_in";						
					}else if((ports.get(neigh)).get(name)==null){						
							arg="a_in";						
					}else
						arg=(ports.get(neigh)).get(name)+"_in";
					tmp=new StringBuilder().append(name).append(separator).append(t.getValue()+"_out").append(separator).append("0").append(arrow).append(t.getKey()).append(separator).append(arg).append(separator).append(0).toString();
					links.add(tmp);
				}
				
			}else if(o instanceof Mailserver){
				Map<String, String> g=ports.get(name);
				for(Map.Entry<String, String> t : g.entrySet()){
					String neigh=t.getKey();	
					String arg=new String();
					if(obj.get(neigh) instanceof Dpi){
						dpi_extern=true;
					}
					if(obj.get(neigh) instanceof Endhost){
						arg="b_in";
					}else if(obj.get(neigh) instanceof Nat){						
							arg="b_in";						
					}else if((ports.get(neigh)).get(name)==null){						
							arg="a_in";						
					}else
						arg=(ports.get(neigh)).get(name)+"_in";
					tmp=new StringBuilder().append(name).append(separator).append(t.getValue()+"_out").append(separator).append("0").append(arrow).append(t.getKey()).append(separator).append(arg).append(separator).append(0).toString();
					links.add(tmp);
				}
		}else if(o instanceof Nat){
			Map<String, String> g=ports.get(name);
			for(Map.Entry<String, String> t : g.entrySet()){
				String neigh=t.getKey();
				String arg=new String();
				if(obj.get(neigh) instanceof Endhost)
					arg="b_in";
				else if((ports.get(neigh)).get(name)==null){						
					arg="a_in";						
				}else
					arg=(ports.get(neigh)).get(name)+"_in";
				tmp=new StringBuilder().append(name).append(separator).append(t.getValue()+"_out").append(separator).append("0").append(arrow).append(t.getKey()).append(separator).append(arg).append(separator).append(0).toString();
				links.add(tmp);
			}
			
		}		
		
		
		}
			return links;
		
		
	}
}
