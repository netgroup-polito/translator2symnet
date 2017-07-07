package it.polito.verigraph.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//import javax.xml.bind.annotation.XmlRootElement;
//import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

//import io.swagger.annotations.ApiModel;
//import io.swagger.annotations.ApiModelProperty;
import it.polito.verigraph.deserializer.NodeCustomDeserializer;
import it.polito.verigraph.serializer.CustomMapSerializer;


@JsonDeserialize(using = NodeCustomDeserializer.class)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Node {

	
	private long					id;


	private String					name;


	private String					functional_type;

	private Configuration			configuration	= new Configuration();


	private Map<Long, Neighbour>	neighbours		= new HashMap<Long, Neighbour>();


	public Node() {

	}

	public Node(long id, String name, String functional_type, Configuration configuration) {
		this.id = id;
		this.name = name;
		this.functional_type = functional_type;
		this.configuration = configuration;
	}

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


	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	@JsonSerialize(using = CustomMapSerializer.class)
	public Map<Long, Neighbour> getNeighbours() {
		return neighbours;
	}

	public void setNeighbours(Map<Long, Neighbour> neighbours) {
		this.neighbours = neighbours;
	}

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
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
		else
			return false;
	}

	public Neighbour searchNeighbourByName(String name) {
		for (Neighbour neighbour : this.neighbours.values()) {
			if (neighbour.getName().equals(name))
				return neighbour;
		}
		return null;
	}

}
