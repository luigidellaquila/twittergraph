package org.test.tweetgraph;

import twitter4j.Status;

/**
 * Created by luigidellaquila on 15/11/16.
 */
public class TweetTracker {

  public void track(Status status) {
    System.out.println("NEW TWEET:" + status.getText());
    OrientService.getInstance().addTweet(status);
  }

  public boolean existsTweet(Status status) {
    OrientService orient = OrientService.getInstance();
    return orient.existsTweet(status.getId());

  }
}
