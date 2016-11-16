package org.test.tweetgraph;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.TwitterFactory;
import twitter4j.api.SearchResource;

import java.util.List;

/**
 * Created by luigidellaquila on 15/11/16.
 */
public class TwitterStreamReader {

  private final String[] keywords;

  public TwitterStreamReader(String[] keywords) {
    this.keywords = keywords;
  }

  public void startReading(TwitterProfileTracker userTracker, TweetTracker tweetTracker) {

    SearchResource search = TwitterFactory.getSingleton().search();
    Query query = new Query("codemotion");
    query.setCount(100);

    new Thread(() -> {
      while (true) {
        try {
          QueryResult result = search.search(query);
          List<Status> tweets = result.getTweets();
          for (Status status : tweets) {
            if (!userTracker.alreadyExists(status.getUser())) {
              userTracker.track(status, tweetTracker);
            } else {
              if (!tweetTracker.existsTweet(status)) {
                tweetTracker.track(status);
              }
            }
          }
          Thread.sleep(5000);
        } catch (InterruptedException ie) {
          break;
        } catch (Exception e) {

        }
      }
    }).start();

  }

}
