package org.test.tweetgraph;

/**
 * Created by luigidellaquila on 15/11/16.
 */
public class Main {

  public static void main(String[] args) {
    //    Vertx.vertx().createHttpServer().requestHandler(req -> req.response().end("Hello World!")).listen(8080);
    TwitterStreamReader reader = new TwitterStreamReader(new String[] { "codemotion" });
    reader.startReading(new TwitterProfileTracker(), new TweetTracker());
  }
}
