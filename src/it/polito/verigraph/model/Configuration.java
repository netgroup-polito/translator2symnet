package it.polito.verigraph.model;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


import it.polito.verigraph.deserializer.ConfigurationCustomDeserializer;
import it.polito.verigraph.serializer.CustomConfigurationSerializer;

public class Configuration {

	
	private String		id;


	private String		description	= "";


	private JsonNode	configuration;

	public Configuration() {

	}

	public Configuration(String id, String description, JsonNode configuration) {
		this.id = id;
		this.description = description;
		this.configuration = configuration;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public JsonNode getConfiguration() {
		return configuration;
	}

	public void setConfiguration(JsonNode configuration) {
		this.configuration = configuration;
	}

}
