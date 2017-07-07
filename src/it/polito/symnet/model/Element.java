package it.polito.symnet.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public class Element {

	private String name;
	private String address;
	private String functional_type;
	private List<String> neighbours=new ArrayList<String>();
	private JsonNode configuration;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFunctional_type() {
		return functional_type;
	}
	public void setFunctional_type(String functional_type) {
		this.functional_type = functional_type;
	}
	public JsonNode getConfiguration() {
		return configuration;
	}
	public void setConfiguration(JsonNode configuration) {
		this.configuration = configuration;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Element other = (Element) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
}
