package org.test.tweetgraph;

import twitter4j.*;
import twitter4j.api.FriendsFollowersResources;

/**
 * Created by luigidellaquila on 15/11/16.
 */
public class TwitterProfileTracker {

  FriendsFollowersResources ff = TwitterFactory.getSingleton().friendsFollowers();

  public void track(Status status, TweetTracker tweetTracker) {
    new Thread(() -> doTrack(status, tweetTracker)).start();
  }

  public void doTrack(Status status, TweetTracker tweetTracker) {
    OrientService orient = OrientService.getInstance();

    User user = status.getUser();

    System.out.println("USER: " + user.getScreenName());
    orient.createUser(user.getId(), user.getScreenName(), user.getProfileImageURL());

    tweetTracker.track(status);

    long cursor = -1;
    IDs followers = null;
    do {
      try {
        followers = ff.getFollowersIDs(user.getId(), cursor);
        for (long follower : followers.getIDs()) {
          System.out.println("  follower: " + follower);
          orient.addFollow(follower, user.getId());
        }
      } catch (TwitterException e) {
        try {
          Thread.sleep(5000);
        } catch (InterruptedException e1) {
        }
      }
    } while (followers == null || (cursor = followers.getNextCursor()) != 0);

    cursor = -1;
    IDs friends = null;
    do {
      try {
        friends = ff.getFriendsIDs(user.getId(), cursor);
        for (long friend : friends.getIDs()) {
          System.out.println("  friend: " + friend);
          orient.addFollow(user.getId(), friend);
        }
      } catch (TwitterException e) {
        try {
          Thread.sleep(5000);
        } catch (InterruptedException e1) {
        }
      }
    } while (friends == null || (cursor = friends.getNextCursor()) != 0);
  }

  public boolean alreadyExists(User user) {
    OrientService orient = OrientService.getInstance();
    return orient.existsUser(user.getId());
  }
}
