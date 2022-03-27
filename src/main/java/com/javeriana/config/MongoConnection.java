package com.javeriana.config;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

public class MongoConnection {
    private static MongoClient mongoClient;
    private static final String DB = "Distribuidos";

    public MongoConnection() {
        MongoClientURI uri = new MongoClientURI(
                "mongodb+srv://admindb:admin123@cluster0-eayyw.mongodb.net/test?retryWrites=true&w=majority");
        mongoClient = new MongoClient(uri);
    }

    public MongoCollection<Document> findCollection(String nameCollection) {
        MongoDatabase mongoBD = mongoClient.getDatabase(DB);
        return mongoBD.getCollection(nameCollection);
    }

    public void insertObject(String nameCollection, Document nDoc) {
        MongoDatabase mongoBD = mongoClient.getDatabase(DB);
        MongoCollection<Document> collection = mongoBD.getCollection(nameCollection);

        collection.insertOne(nDoc);
    }

    public void updateObject(String nameCollection, String _id, Document nDoc) {
        MongoDatabase mongoBD = mongoClient.getDatabase(DB);
        MongoCollection<Document> collection = mongoBD.getCollection(nameCollection);
        BasicDBObject query = new BasicDBObject();
        query.put("_id", new ObjectId(_id));

        collection.replaceOne(query, nDoc);
    }

    public Document searchByID(String nameCollection, String _id) {
        MongoDatabase mongoBD = mongoClient.getDatabase(DB);
        MongoCollection<Document> collection = mongoBD.getCollection(nameCollection);

        BasicDBObject query = new BasicDBObject();
        query.put("_id", new ObjectId(_id));

        return collection.find(query).first();
    }

    public void deleteByID(String nameCollection, String _id) {
        MongoDatabase mongoBD = mongoClient.getDatabase(DB);
        MongoCollection<Document> collection = mongoBD.getCollection(nameCollection);

        BasicDBObject query = new BasicDBObject();
        query.put("_id", new ObjectId(_id));

        collection.deleteOne(query);
    }

    public void closeMongoDB() {
        mongoClient.close();
    }
}
