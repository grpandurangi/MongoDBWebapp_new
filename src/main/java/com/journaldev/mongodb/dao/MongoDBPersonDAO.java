package com.journaldev.mongodb.dao;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.journaldev.mongodb.converter.PersonConverter;
import com.journaldev.mongodb.model.Person;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

//DAO class for different MongoDB CRUD operations
//take special note of "id" String to ObjectId conversion and vice versa
//also take note of "_id" key for primary key
public class MongoDBPersonDAO {

	private DBCollection col;

	@SuppressWarnings("deprecation")
	public MongoDBPersonDAO(MongoClient mongo) {
		this.col = mongo.getDB(System.getProperty("MONGODB_AUTHDB")).getCollection("Persons");
	}

	public Person createPerson(Person p) throws DAOException {
		try {
			DBObject doc = PersonConverter.toDBObject(p);
			this.col.insert(doc);
			ObjectId id = (ObjectId) doc.get("_id");
			p.setId(id.toString());
			return p;
		} catch (Exception e) {
			throw new DAOException(e.getLocalizedMessage(), e);
		}
	}

	public void updatePerson(Person p) throws DAOException {
		try {
			DBObject query = BasicDBObjectBuilder.start()
					.append("_id", new ObjectId(p.getId())).get();
			this.col.update(query, PersonConverter.toDBObject(p));
		} catch (Exception e) {
			throw new DAOException(e.getLocalizedMessage(), e);
		}
	}

	public List<Person> readAllPerson() throws DAOException {
		List<Person> data = new ArrayList<Person>();
		try {
			DBCursor cursor = col.find();
			while (cursor.hasNext()) {
				DBObject doc = cursor.next();
				Person p = PersonConverter.toPerson(doc);
				data.add(p);
			}
			return data;
		} catch (Exception e) {
			/*if(e instanceof MongoSocketReadException) {
				mongo.close();
			}*/
			throw new DAOException(e.getLocalizedMessage(), e);
		}
	}

	public void deletePerson(Person p) throws DAOException {
		try {
			DBObject query = BasicDBObjectBuilder.start()
					.append("_id", new ObjectId(p.getId())).get();
			this.col.remove(query);
		} catch (Exception e) {
			throw new DAOException(e.getLocalizedMessage(), e);
		}
	}

	public Person readPerson(Person p) throws DAOException {
		try {
			DBObject query = BasicDBObjectBuilder.start()
					.append("_id", new ObjectId(p.getId())).get();
			DBObject data = this.col.findOne(query);
			return PersonConverter.toPerson(data);
		} catch (Exception e) {
			throw new DAOException(e.getLocalizedMessage(), e);
		}
	}
	
	public Person searchPerson(Person p) throws DAOException {
		try {
			DBObject query = BasicDBObjectBuilder.start()
					.append("name", p.getName()).get();
			DBObject data = this.col.findOne(query);
			return PersonConverter.toPerson(data);
		} catch (Exception e) {
			 throw new DAOException(e.getLocalizedMessage(), e);
		}
	}

}
