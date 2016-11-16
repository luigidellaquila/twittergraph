package org.test.tweetgraph;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.*;
import twitter4j.Status;

import java.util.Iterator;

/**
 * Created by luigidellaquila on 15/11/16.
 */
public class OrientService {

  static String CLASS_ACCOUNT = "Account";
  static String ID            = "tid";
  static String NAME          = "name";
  static String IMG           = "img";

  static String CLASS_FOLLOWS = "Follows";

  static String CLASS_TWEET = "Tweet";

  static String CLASS_AUTHOR = "Author";

  private static OrientService instance = new OrientService();

  //  private OrientGraphFactory factory = new OrientGraphFactory("plocal:/Users/luigidellaquila/temp/codemotiondb");
  private OrientGraphFactory factory = new OrientGraphFactory("remote:localhost/codemotiondb");

  /**
   * Returns and instance of OrientDB graph service
   *
   * @return
   */
  public static OrientService getInstance() {
    return instance;
  }

  private OrientService() {
    OrientGraphNoTx db = factory.getNoTx();
    initSchema(db);
    db.shutdown();
  }

  private void initSchema(OrientGraphNoTx db) {
    OrientVertexType accountClass = db.getVertexType(CLASS_ACCOUNT);
    if (accountClass == null) {
      accountClass = db.createVertexType(CLASS_ACCOUNT);
      accountClass.createProperty(ID, OType.LONG).createIndex(OClass.INDEX_TYPE.UNIQUE);
    }

    OrientEdgeType followsClass = db.getEdgeType(CLASS_FOLLOWS);
    if (followsClass == null) {
      followsClass = db.createEdgeType(CLASS_FOLLOWS);
      followsClass.createProperty("out", OType.LINK);
      followsClass.createProperty("in", OType.LINK);
      followsClass.createIndex(CLASS_FOLLOWS + ".out_in", OClass.INDEX_TYPE.UNIQUE, "out", "in");
    }

    OrientVertexType tweetClass = db.getVertexType(CLASS_TWEET);
    if (tweetClass == null) {
      tweetClass = db.createVertexType(CLASS_TWEET);
    }

    OrientEdgeType authorClass = db.getEdgeType(CLASS_AUTHOR);
    if (authorClass == null) {
      authorClass = db.createEdgeType(CLASS_AUTHOR);
    }

  }

  /**
   * Creates a user with a unique ID. If the user already exists, does nothing
   *
   * @param id     the user id
   * @param name   the user name
   * @param imgURI the image URI of the user profile
   */
  public void createUser(long id, String name, String imgURI) {
    OrientGraph graph = factory.getTx();
    try {
      Vertex existing = getUser(id, graph);
      if (existing == null) {
        Vertex v = graph.addVertex("class:" + CLASS_ACCOUNT);
        v.setProperty(ID, id);
        v.setProperty(NAME, name);
        v.setProperty(IMG, imgURI);
      }
      graph.commit();
    } catch (Exception e) {
      graph.rollback();
    } finally {
      graph.shutdown();
    }
  }

  public boolean existsUser(long id) {
    OrientGraph graph = factory.getTx();
    try {
      Vertex existing = getUser(id, graph);
      return existing != null;
    } finally {
      graph.shutdown();
    }
  }

  public Vertex getUser(long id, OrientGraph graph) {
    Iterator<Vertex> existing = graph.getVertices(CLASS_ACCOUNT + "." + ID, id).iterator();
    if (existing.hasNext()) {
      return existing.next();
    }
    return null;
  }

  public Vertex getTweet(long id, OrientGraph graph) {
    Iterator<Vertex> existing = graph.getVertices(CLASS_TWEET + "." + ID, id).iterator();
    if (existing.hasNext()) {
      return existing.next();
    }
    return null;
  }

  /**
   * Creates a Follows edge between two users
   *
   * @param from the user that follows (id)
   * @param to   the user that is followed (id)
   */
  public void addFollow(long from, long to) {
    OrientGraph graph = factory.getTx();
    try {
      Vertex fromV = getUser(from, graph);
      Vertex toV = getUser(to, graph);
      if (fromV == null || toV == null) {
        return;
      }
      fromV.addEdge(CLASS_FOLLOWS, toV);
      graph.commit();
    } catch (Exception e) {
      graph.rollback();
    } finally {
      graph.shutdown();
    }
  }

  public void addTweet(Status tweet) {
    OrientGraph graph = factory.getTx();
    try {
      OrientVertex tweetVertex = graph.addVertex("class:" + CLASS_TWEET);
      tweetVertex.setProperty(ID, tweet.getId());
      tweetVertex.setProperty("text", tweet.getText());
      Vertex author = getUser(tweet.getUser().getId(), graph);
      if (author != null) {
        tweetVertex.addEdge(CLASS_AUTHOR, author);
      }
      graph.commit();
    } catch (Exception e) {
      graph.rollback();
    } finally {
      graph.shutdown();
    }
  }

  public boolean existsTweet(long id) {
    OrientGraph graph = factory.getTx();
    try {
      Vertex existing = getTweet(id, graph);
      return existing != null;
    } finally {
      graph.shutdown();
    }
  }
}
