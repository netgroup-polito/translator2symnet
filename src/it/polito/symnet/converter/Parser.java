package it.polito.symnet.converter;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import it.polito.verigraph.exception.*;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;


import it.polito.verigraph.exception.ForbiddenException;
import it.polito.verigraph.model.*;


public class Parser {

	static Graph graph;
	
	public Parser() {
	
	}

	static Graph retrieveGraph(File file) {
		ObjectMapper mapper = new ObjectMapper();
		graph=new Graph();
		try { 
			File json = file;
			graph = mapper.readValue(json, Graph.class); 
		
		
		} catch (JsonGenerationException ex){
			ex.printStackTrace(); 
		}catch (JsonMappingException ex){
			ex.printStackTrace();
		} catch (IOException ex){
			ex.printStackTrace();
		}
		
		return graph;
	}

	
	public static void validateGraph() throws JsonProcessingException {
			for (Node node : graph.getNodes().values()) {
				validateNode(graph, node);
			}
		}
	
	public static void validateNode(Graph graph, Node node) throws JsonProcessingException {
		if (graph == null)
			throw new InternalServerErrorException("Node validation failed: cannot validate null graph");
		if (node == null)
			throw new InternalServerErrorException("Node validation failed: cannot validate null node");

		if (node.getName() == null)
			throw new InternalServerErrorException("Node validation failed: node 'name' field cannot be null");
		if (node.getFunctional_type() == null)
			throw new InternalServerErrorException("Node validation failed: node 'functional_type' field cannot be null");

		if (node.getName().equals(""))
			throw new InternalServerErrorException("Node validation failed: node 'name' field cannot be an empty string");
		if (node.getFunctional_type().equals(""))
			throw new InternalServerErrorException("Node validation failed: node 'functional_type' field cannot be an empty string");

		Node nodeFound =graph.searchNodeByName(node.getName());
		if ((nodeFound != null) && (nodeFound.equals(node) == false))
			throw new InternalServerErrorException("Node validation failed: graph already has a node named '"	+ node.getName()
											+ "'");
		Configuration configuration = node.getConfiguration();
		if (configuration != null) {
			JsonNode configurationJsonNode = configuration.getConfiguration();
			// validate configuration against schema file
			validateNodeConfigurationAgainstSchemaFile(node, configurationJsonNode);
			
		}

		// validate neighbours
		Map<Long, Neighbour> nodeNeighboursMap = node.getNeighbours();
		if (nodeNeighboursMap == null)
			throw new InternalServerErrorException("Node validation failed: node 'neighbours' cannot be null");
		for (Neighbour neighbour : nodeNeighboursMap.values()) {
			validateNeighbour(graph, node, neighbour);
		}
	}

	public static void validateNeighbour(Graph graph, Node node, Neighbour neighbour) throws JsonProcessingException {
		if (graph == null)
			throw new InternalServerErrorException("Neighbour validation failed: cannot validate null graph");
		if (node == null)
			throw new InternalServerErrorException("Neighbour validation failed: cannot validate null node");
		if (neighbour == null)
			throw new InternalServerErrorException("Neighbour validation failed: cannot validate null neighbour");

		if (neighbour.getName() == null)
			throw new InternalServerErrorException("Neighbour validation failed: neighbour 'name' field cannot be null");
		if (neighbour.getName().equals(""))
			throw new InternalServerErrorException("Neighbour validation failed: neighbour 'name' field cannot be an empty string");

		//Node nodeFound = graph.searchNodeByName(neighbour.getName());
		
		Node nodeFound=graph.searchNodeByName(neighbour.getName());
		if ((nodeFound == null) || (nodeFound.getName().equals(node.getName())))
			throw new InternalServerErrorException("Neighbour validation failed: '"	+ neighbour.getName()
											+ "' is not a valid name for a neighbour of node '" + node.getName() + "'");

		Neighbour neighbourFound = node.searchNeighbourByName(neighbour.getName());
		if ((neighbourFound != null) && (neighbourFound.equals(neighbour) == false))
			throw new InternalServerErrorException("Neighbour validation failed: node '"	+ node.getName()
											+ "' already has a neighbour named '" + neighbour.getName() + "'");
	}
	public static void validateNodeConfigurationAgainstSchemaFile(Node node, JsonNode configurationJson) {
		String schemaFileName = node.getFunctional_type() + ".json";
			
		File schemaFile = new File(System.getProperty("user.dir")+ File.separator+ "json" + File.separator+schemaFileName);

		if (!schemaFile.exists()) {
			throw new ForbiddenException("Functional type '"	+ node.getFunctional_type()
											+ "' is not supported! Please edit 'functional_type' field of node '"
											+ node.getName() + "'");
		}

		JsonSchema schemaNode = null;
		try {
			schemaNode = ValidationUtils.getSchemaNode(schemaFile);
		}
		catch (IOException e) {
			throw new InternalServerErrorException("Unable to load '" + schemaFileName + "' schema file");
		}
		catch (ProcessingException e) {
			throw new InternalServerErrorException("Unable to resolve '"	+ schemaFileName
													+ "' schema file as a schema node");
		}

		try {
			ValidationUtils.validateJson(schemaNode, configurationJson);
		}
		catch (ProcessingException e) {
			throw new InternalServerErrorException("Something went wrong trying to validate node '"	+ node.getName()
											+ "' with the following configuration: '" + configurationJson.toString()
											+ "' against the json schema '" + schemaFile.getName() + "': "
											+ e.getMessage());

		}

	}
		
	

}
