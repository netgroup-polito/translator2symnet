package it.polito.verigraph.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import it.polito.verigraph.deserializer.GraphCustomDeserializer;
import it.polito.verigraph.serializer.CustomMapSerializer;


@JsonDeserialize(using = GraphCustomDeserializer.class)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Graph {


	private long			id;


	private Map<Long, Node>	nodes	= new HashMap<Long, Node>();

	public Graph() {

	}

	public Graph(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@JsonSerialize(using = CustomMapSerializer.class)
	public Map<Long, Node> getNodes() {
		return nodes;
	}

	public void setNodes(Map<Long, Node> nodes) {
		this.nodes = nodes;
	}

	public Node searchNodeByName(String name) {
		for (Node node : this.nodes.values()) {
			if (node.getName().equals(name))
				return node;
		}
		return null;
	}

	public int nodesWithName(String name) {
		int occurrences = 0;
		for (Node node : this.nodes.values()) {
			if (node.getName().equals(name))
				occurrences++;

		}
		return occurrences;
	}

}
