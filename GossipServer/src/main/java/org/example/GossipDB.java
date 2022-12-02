package org.example;

import com.mongodb.client.*;
import org.bson.Document;
import java.util.ArrayDeque;

public class GossipDB {
    String uri;
    GossipDB() {
        uri = new SecurityKey().get();
    }
    boolean createUser(String email, String password, String username) {
        try (MongoClient mongoClient = MongoClients.create(uri)){
            MongoDatabase logindb = mongoClient.getDatabase("logindb");
            Document doc = new Document("password", password);
            logindb.createCollection(email);
            MongoCollection<Document> collection = logindb.getCollection(email);
            collection.insertOne(doc);

            MongoDatabase usernamedb = mongoClient.getDatabase("usernamedb");
            doc = new Document("username", username);
            usernamedb.createCollection(email);
            collection = usernamedb.getCollection(email);
            collection.insertOne(doc);
            return true;
        }
        catch (Exception e) {
            System.out.println("Server Problem Creating user for " + email);
            return false;
        }
    }
    boolean logIn(String email, String password) {
        try (MongoClient mongoClient = MongoClients.create(uri)){
            MongoDatabase logindb = mongoClient.getDatabase("logindb");
            MongoCollection<Document> collection = logindb.getCollection(email);
            MongoCursor cursor = collection.find().iterator();
            while (cursor.hasNext()) {
                Document doc = (Document) cursor.next();
                String msg = doc.getString("password");
                if (msg.equals(password)) {
                    return true;
                }
            }
            return false;
        }
        catch (Exception e) {
            System.out.println("Server Problem logging in user for " + email);
            return false;
        }
    }

    boolean UpdateUserPassword(String email, String oldpass, String newpass) {
        try (MongoClient mongoClient = MongoClients.create(uri)){
            MongoDatabase logindb = mongoClient.getDatabase("logindb");
            MongoCollection<Document> collection = logindb.getCollection(email);
            System.out.println("collection Success");
            MongoCursor cursor = collection.find().iterator();
            Document mydoc = new Document();
            boolean found = false;
            while (cursor.hasNext()) {
                Document doc = (Document) cursor.next();
                if (doc != null) {
                    String pass = (String) doc.get("password");
                    if (pass.equals(oldpass)) {
                        mydoc = doc;
                        found = true;
                        break;
                    }
                }
            }
            if (found) {
                collection.deleteOne(mydoc);
                mydoc = new Document("password", newpass);
                collection.insertOne(mydoc);
                System.out.println("Password updated successfully");
                return true;
            }
            return false;
        }
        catch (Exception e) {
            System.out.println("Server error.. can't update password for " + email);
            return false;
        }
    }

    ArrayDeque<String> readAllMessage(String user1, String user2) {
        try (MongoClient mongoClient = MongoClients.create(uri)){
            MongoDatabase msgdb = mongoClient.getDatabase("messagedb");
            MongoCollection<Document> collection = msgdb.getCollection(user1 + user2);
            System.out.println("collection Success");

            MongoCursor cursor = collection.find().iterator();
            ArrayDeque<String> messages = new ArrayDeque<String>();

            while (cursor.hasNext()) {
                Document doc = (Document) cursor.next();
                if (doc != null) {
                    String person1 = (String) doc.get("from");
                    String person2 = (String) doc.get("to");
                    String msg = (String) doc.get("message");
                    String msgTime = (String) doc.get("time");
                    messages.offerLast(person1 + "$" + msg + "$" + person2 + "$" + msgTime);
                }
            }
            return messages;
        }
        catch (Exception e) {
            System.out.println("Can't load all message for " + user1 + ", " + user2);
            return new ArrayDeque<String>();
        }
    }

    ArrayDeque<String> readNewMessages(String user1, String user2) {
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase msgdb = mongoClient.getDatabase("newmessagedb");
            MongoCollection<Document> collection = msgdb.getCollection(user1 + user2);
            System.out.println("collection Success");

            MongoCursor cursor = collection.find().iterator();
            ArrayDeque<String> messages = new ArrayDeque<String>();

            while (cursor.hasNext()) {
                Document doc = (Document) cursor.next();
                if (doc != null) {
                    String person1 = (String) doc.get("from");
                    String person2 = (String) doc.get("to");
                    String msg = (String) doc.get("message");
                    String msgTime = (String) doc.get("time");
                    messages.offerLast(person1 + "$" + msg + "$" + person2 + "$" + msgTime);
                }
            }
            return messages;
        } catch (Exception e) {
            System.out.println("Can't load new messages for " + user1 + ", " + user2);
            return new ArrayDeque<String>();
        }
    }
    boolean validUser(String email) {
        try (MongoClient mongoClient = MongoClients.create(uri)){
            MongoDatabase logindb = mongoClient.getDatabase("logindb");
            MongoCollection<Document> collection = logindb.getCollection(email);
            return true;
        }
        catch (Exception e) {
            System.out.println("Server Problem Creating user for " + email);
            return false;
        }
    }
    ArrayDeque<String> findMyFriends(String user1) {
        ArrayDeque<String> userList = new ArrayDeque<String>();
        try (MongoClient mongoClient = MongoClients.create(uri)){
            MongoDatabase msgdb = mongoClient.getDatabase("conversationlistdb");
            MongoCollection<Document> collection = msgdb.getCollection(user1);
            System.out.println("collection Success");

            MongoCursor cursor = collection.find().cursor();

            while (cursor.hasNext()) {
                Document doc = (Document) cursor.next();
                if (doc != null) {
                    String person1 = (String) doc.get("email");
                    userList.offerLast(person1);
                }
            }
            return userList;
        }
        catch (Exception e) {
            System.out.println("Can't find friends for user " + user1);
            return userList;
        }
    }
    boolean writeMessage(String user1, String user2, Document msgToken) {
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase msgdb = mongoClient.getDatabase("messagedb");
            try {
                msgdb.createCollection(user1 + user2);
            }
            catch (Exception e) {
                System.out.println("user already exists..");
            }
            MongoCollection<Document> collection = msgdb.getCollection(user1 + user2);
            System.out.println("collection Success");

            collection.insertOne(msgToken);
            return true;
        }
        catch (Exception e) {
            System.out.println("Can't write message to server..");
            return false;
        }
    }

    boolean deleleAMessageFromNewMessageDB(String user1, String user2, Document msgToken) {
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase newmessagedb = mongoClient.getDatabase("newmessagedb");
            MongoCollection<Document> collection = newmessagedb.getCollection(user1 + user2);
            System.out.println("collection Success");
            collection.deleteOne(msgToken);
            return true;
        }
        catch (Exception e) {
            System.out.println("Can't delete message in the Mongodb server..");
            return false;
        }
    }
    boolean writeANewMessageToNewMessageDB(String user1, String user2, Document msgToken) {
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase msgdb = mongoClient.getDatabase("newmessagedb");
            PleaseAddUser(user1, user2);
            PleaseAddUser(user2, user1);
            try {
                msgdb.createCollection(user1 + user2);
            }
            catch (Exception e) {
                System.out.println("already exists..");
            }
            MongoCollection<Document> collection = msgdb.getCollection(user1 + user2);
            System.out.println("collection Success");
            collection.insertOne(msgToken);
            return true;
        }
        catch (Exception e) {
            System.out.println("Can't write message newdb to the Mongodb server..");
            return false;
        }
    }
    String lastMessage(String user1, String user2) {
        try (MongoClient mongoClient = MongoClients.create(uri)){
            MongoDatabase msgdb = mongoClient.getDatabase("messagedb");
            MongoCollection<Document> collection = msgdb.getCollection(user1 + user2);
            System.out.println("collection Success");

            MongoCursor cursor = collection.find().iterator();
            ArrayDeque<String> messages = new ArrayDeque<String>();
            String lastMessage = "";
            while (cursor.hasNext()) {
                Document doc = (Document) cursor.next();
                if (doc != null) {
                    String person1 = (String) doc.get("from");
                    String person2 = (String) doc.get("to");
                    String msg = (String) doc.get("message");
                    String msgTime = (String) doc.get("time");
                    lastMessage = person1 + "$" + msg + "$" + person2 + "$" + msgTime;
                }
            }
            return lastMessage;
        }
        catch (Exception e) {
            System.out.println("Can't load all message for " + user1 + ", " + user2);
            return  "";
        }
    }
    String getActiveStatus(String user1) {
        try (MongoClient mongoClient = MongoClients.create(uri)){
            MongoDatabase activestatusdb = mongoClient.getDatabase("activestatusdb");
            MongoCollection<Document> collection = activestatusdb.getCollection(user1);
            MongoCursor cursor = collection.find().iterator();
            String status = "";
            while (cursor.hasNext()) {
                Document doc = (Document) cursor.next();
                if (doc != null) {
                    status = doc.getString("activestatus");
                    break;
                }
            }
            return status;
        }
        catch (Exception e) {
            System.out.println("Can't get active status for " + user1);
            return "";
        }
    }
    boolean updateActiveStatus(String user1, String loginTime) {
        try (MongoClient mongoClient = MongoClients.create(uri)){
            MongoDatabase activestatusdb = mongoClient.getDatabase("activestatusdb");
            try {
                activestatusdb.createCollection(user1);
                MongoCollection<Document> collection = activestatusdb.getCollection(user1);
                collection.insertOne(new Document("activestatus", loginTime));
            }
            catch (Exception e) {
                activestatusdb.getCollection(user1).drop();
                activestatusdb.createCollection(user1);
                MongoCollection<Document> collection = activestatusdb.getCollection(user1);
                collection.insertOne(new Document("activestatus", loginTime));
            }
            return true;
        }
        catch (Exception e) {
            System.out.println("Can't update active status for " + user1);
            return false;
        }
    }

    void PleaseAddUser(String user1, String user2) {
        try (MongoClient mongoClient = MongoClients.create(uri)){
            MongoDatabase conversationlistdb = mongoClient.getDatabase("conversationlistdb");
            try {
                conversationlistdb.createCollection(user1);
            }
            catch (Exception ignored) {}
            MongoCollection<Document> collection = conversationlistdb.getCollection(user1);
            MongoCursor cursor = collection.find().iterator();

            while (cursor.hasNext()) {
                Document doc = (Document) cursor.next();
                if (doc != null) {
                    String name = doc.getString("email");
                    if (name.equals(user2)) {
                        return;
                    }
                }
            }
            collection.insertOne(new Document("email", user2));
        }
        catch (Exception e) {
            System.out.println("Server Problem finding username for " + user1);
        }
    }
    String findThisUserName(String user1) {
        try (MongoClient mongoClient = MongoClients.create(uri)){
            MongoDatabase usernamedb = mongoClient.getDatabase("usernamedb");
            MongoCollection<Document> collection = usernamedb.getCollection(user1);
            MongoCursor cursor = collection.find().iterator();
            String name = "";
            while (cursor.hasNext()) {
                Document doc = (Document) cursor.next();
                if (doc != null) {
                    name = doc.getString("username");
                    break;
                }
            }
            return name;
        }
        catch (Exception e) {
            System.out.println("Server Problem finding username for " + user1);
            return "";
        }
    }
}
