package com.journaldev.mongodb.model;

public class Person {

	// id will be used for primary key in MongoDB
	// We could use ObjectId, but I am keeping it
	// independent of MongoDB API classes
	private String id;

	private String name;

	private String country;
	private OPERATION operation;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the operation
	 */
	public OPERATION getOperation() {
		return operation;
	}

	/**
	 * @param operation the operation to set
	 */
	public void setOperation(OPERATION operation) {
		this.operation = operation;
	}

	public enum OPERATION {
		SAVE,
		EDIT,
		DELETE;
	}
}
