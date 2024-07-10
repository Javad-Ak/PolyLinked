import org.aut.controllers.*;
import org.aut.dataAccessors.*;
import org.aut.models.*;
import org.aut.utils.exceptions.NotAcceptableException;
import org.aut.utils.exceptions.NotFoundException;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.SQLException;

@DisplayName("------ testing DataBase...")
public class DBTest {
    @Test
    @DisplayName("---- full test")
    public void fullDBTest() throws Exception {
        DataBaseAccessor.create();
        User user1 = new User("kasra@gmail.com", "kasra123456", "Kasra", "Rezai", "D");
        User user2 = new User("javad@gmail.com", "javad123456", "Javad", "Akbari", "H");
        User user3 = new User("ali@gmail.com", "ali123456", "Ali", "Athari", "K");
        User[] users = {user1, user2, user3};

        Post post = new Post(user1.getUserId(), "hey #Woww .");
        Post post2 = new Post(user2.getUserId(), "#good luck boy");
        Post post3 = new Post(user3.getUserId(), "good luck #boy");
        for (User user : users) {
            try {
                UserAccessor.addUser(user);
            } catch (SQLException ignored) {
            }
        }
        try {
            PostAccessor.addPost(post);
            PostAccessor.addPost(post2);
            PostAccessor.addPost(post3);
        } catch (NotAcceptableException ignored) {
        }
        Like like1 = new Like(post2.getPostId(), user1.getUserId());
        Like like2 = new Like(post.getPostId(), user2.getUserId());
        Like like3 = new Like(post2.getPostId(), user3.getUserId());
        try {
            LikeAccessor.addLike(like1);
            LikeAccessor.addLike(like2);
            LikeAccessor.addLike(like3);
            LikeAccessor.deleteLike(like1.getPostId(), user1.getUserId());
            System.out.println("2 likes added and 1 deleted.");
        } catch (Exception ignored) {
        }

        Comment comment1 = new Comment(user2.getUserId(), post.getPostId(), "hey");
        Comment comment2 = new Comment(user3.getUserId(), post.getPostId(), "hey");
        try {
            CommentAccessor.addComment(comment1);
            CommentAccessor.addComment(comment2);
            System.out.println("2 Comments added.\n ####");
        } catch (Exception ignored) {
        }

        System.out.println("likes: " + LikeAccessor.countPostLikes(post.getPostId()));
        System.out.println(LikeAccessor.getLikersOfPost(post.getPostId()));

        System.out.println("comments: " + CommentAccessor.countPostComments(post.getPostId()));
        System.out.println(CommentAccessor.getCommentsOfPost(post.getPostId()));

        try {
            CommentAccessor.deleteComment(comment1.getId());
            System.out.println("1 comment deleted.");
        } catch (Exception ignored) {
        }

        Follow follow1 = new Follow(user1.getUserId(), user2.getUserId());
        Follow follow2 = new Follow(user2.getUserId(), user3.getUserId());
        Connect connect = new Connect(user1.getUserId(), user2.getUserId(), "Hey there!");
        Message message = new Message(user1.getUserId(), user2.getUserId(), post.getPostId(), "wow");
        try {
            FollowAccessor.addFollow(follow1);
            FollowAccessor.addFollow(follow2);
            ConnectAccessor.addConnect(connect);
            MessageAccessor.addMessage(message);
            System.out.println("2 follows and a connect added.");
        } catch (SQLException ignored) {
        }

        Education edu1 = new Education(user1.getUserId(), "art", "ddd", new Date(22222222), new java.util.Date(33333333), 75, "aa", "bbb");
        Education edu2 = new Education(user1.getUserId(), "math", "ddd", new Date(22222222), new java.util.Date(33333333), 75, "aa", "bbb");
        Education edu3 = new Education(user2.getUserId(), "game", "ddd", new Date(22222222), new java.util.Date(33333333), 75, "aa", "bbb");
        try {
            EducationAccessor.addEducation(edu1);
            EducationAccessor.addEducation(edu2);
            EducationAccessor.addEducation(edu3);
            EducationAccessor.deleteEducation(edu2.getEducationId());
            System.out.println("3 Educations added and 1 deleted.");
        } catch (Exception ignored) {
        }

        Skill skill1 = new Skill(edu1.getUserId(), edu1.getEducationId(), "1");
        Skill skill2 = new Skill(edu1.getUserId(), edu1.getEducationId(), "2");
        Skill skill3 = new Skill(edu1.getUserId(), edu1.getEducationId(), "3");
        try {
            SkillsAccessor.addSkill(skill1);
            SkillsAccessor.addSkill(skill2);
            SkillsAccessor.addSkill(skill3);
            SkillsAccessor.deleteSkill(skill1.getSkillId());
            System.out.println("3 Skills added and 1 deleted.");
        } catch (Exception ignored) {
        }

        CallInfo callInfo1 = new CallInfo(user1.getUserId(), user1.getEmail(), "222", "333", "444", "555", new Date(555555), CallInfo.PrivacyPolitics.EVERYONE, "tel");
        CallInfo callInfo12 = new CallInfo(user2.getUserId(), user2.getEmail(), "222", "333", "444", "555", new Date(555555), CallInfo.PrivacyPolitics.EVERYONE, "tel");
        try {
            CallInfoAccessor.addCallInfo(callInfo1);
            CallInfoAccessor.addCallInfo(callInfo12);
            CallInfoAccessor.deleteCallInfo(callInfo1.getUserId());
            System.out.println("2 CallInfos added and 1 deleted.");
        } catch (Exception ignored) {
        }

        System.out.println("---- user1 Feed: \n" + NewsFeedController.fetchFeed(user1.getUserId()));
    }

    @Test
    @DisplayName("---- testing comments table")
    public void HashtagTest() throws Exception {
        DataBaseAccessor.create();
        System.out.println(HashtagController.hashtagDetector("wow"));
    }

    @Test
    @DisplayName("---- testing comments table")
    public void CommentsTest() throws Exception {
        DataBaseAccessor.create();
        User user1 = new User("ali@gmail.com", "ali1222345", "Ali", "akbari", "ll");
        User user2 = new User("javad@gmail.com", "ali1222345", "Ali", "akbari", "ll");
        User user3 = new User("kasra@gmail.com", "ali1222345", "Ali", "akbari", "ll");
        User[] users = {user1, user2, user3};

        Post post = new Post(user1.getUserId(), "hey");
        for (User user : users) {
            try {
                UserAccessor.addUser(user);
            } catch (SQLException ignored) {
            }
        }
        try {
            PostAccessor.addPost(post);
        } catch (NotAcceptableException ignored) {
        }
        Like like1 = new Like(post.getPostId(), user1.getUserId());
        Like like2 = new Like(post.getPostId(), user2.getUserId());
        Like like3 = new Like(post.getPostId(), user3.getUserId());
        try {
            LikeAccessor.addLike(like1);
            LikeAccessor.addLike(like2);
            LikeAccessor.addLike(like3);
            LikeAccessor.deleteLike(like1.getPostId(), user1.getUserId());
            System.out.println("2 likes added and 1 deleted.");
        } catch (Exception ignored) {
        }

        Comment comment1 = new Comment(user2.getUserId(), post.getPostId(), "hey");
        Comment comment2 = new Comment(user3.getUserId(), post.getPostId(), "hey");
        try {
            CommentAccessor.addComment(comment1);
            CommentAccessor.addComment(comment2);
            System.out.println("2 Comments added.\n ####");
        } catch (Exception ignored) {
        }

        System.out.println("likes: " + LikeAccessor.countPostLikes(post.getPostId()));
        System.out.println(LikeAccessor.getLikersOfPost(post.getPostId()));

        System.out.println("comments: " + CommentAccessor.countPostComments(post.getPostId()));
        System.out.println(CommentAccessor.getCommentsOfPost(post.getPostId()));

        try {
            CommentAccessor.deleteComment(comment1.getId());
            System.out.println("1 comment deleted.");
        } catch (Exception ignored) {
        }
    }

    @Test
    @DisplayName("---- testing users table")
    public void LikeTest() throws Exception {
        DataBaseAccessor.create();
        User user1 = new User("ali@gmail.com", "ali1222345", "Ali", "akbari", "ll");
        User user2 = new User("javad@gmail.com", "ali1222345", "Ali", "akbari", "ll");
        User user3 = new User("kasra@gmail.com", "ali1222345", "Ali", "akbari", "ll");
        User[] users = {user1, user2, user3};

        Post post = new Post(user1.getUserId(), "hey");
        for (User user : users) {
            try {
                UserAccessor.addUser(user);
            } catch (SQLException ignored) {
            }
        }
        try {
            PostAccessor.addPost(post);
        } catch (NotAcceptableException ignored) {
        }
        Like like1 = new Like(post.getPostId(), user1.getUserId());
        Like like2 = new Like(post.getPostId(), user2.getUserId());
        try {
            LikeAccessor.addLike(like1);
            LikeAccessor.addLike(like2);
            LikeAccessor.deleteLike(like1.getPostId(), user1.getUserId());
            System.out.println(LikeAccessor.getLike(like2.getPostId(), user2.getUserId()));
        } catch (Exception ignored) {
        }
    }

    @Test
    @DisplayName("---- testing users table")
    public void UserTest() throws Exception {
        DataBaseAccessor.create();
        String userId = null;
        try {
            User user = new User("reza@gmail.com", "ali1222345", "Ali", "akbari", "ll");
            UserAccessor.addUser(user);
            userId = user.getUserId();
            System.out.println("User added successfully");
        } catch (SQLException e) {
            System.out.println("User exists " + e.getMessage());
        }


        try {
            User user = UserAccessor.getUserById(userId != null ? userId : "user719066ad-4efe-8f14");
            UserAccessor.deleteUser(userId != null ? userId : "user719066ad-4efe-8f14");
            System.out.println("User " + user.getEmail() + " deleted successfully");
        } catch (SQLException e) {
            System.out.println("SQLException " + e.getMessage());
        } catch (NotFoundException e) {
            System.out.println("User does not exist " + e.getMessage());
        }

        try {
            User user = UserAccessor.getUserByEmail("reza2@gmail.com");
            UserController.deleteUser(user);
            System.out.println("User " + user.getEmail() + " deleted successfully");
        } catch (SQLException e) {
            System.out.println("SQLException " + e.getMessage());
        } catch (NotFoundException e) {
            System.out.println("User does not exist " + e.getMessage());
        }

        try {
            User newUser = new User("reza2@gmail.com", "ali1222345", "Ali", "akbari", "ll");

            UserAccessor.addUser(newUser);
            System.out.println("User added successfully");
            newUser.setFirstName("UpdatedAli");
            UserAccessor.updateUser(newUser);
            System.out.println("User " + newUser.getEmail() + " updated successfully");
        } catch (SQLException e) {
            System.out.println("SQLException " + e.getMessage());
        }

    }

    @Test
    @DisplayName("---- testing follows table")
    public void FollowTest() throws Exception {
        DataBaseAccessor.create();
        User user1, user2;
        UserAccessor.addUser(user1 = new User("ali3@gmail.com", "ali1222345", "Ali", "akbari", ""));
        UserAccessor.addUser(user2 = new User("ali4@gmail.com", "ali1222345", "Alireza", "athari", ""));
        FollowController.addFollow(new Follow(user1.getUserId(), user2.getUserId()));
        try {
            FollowController.addFollow(new Follow(user1.getUserId(), user2.getUserId()));
        } catch (NotAcceptableException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Follow added successfully");
    }

    @Test
    @DisplayName("---- testing profiles table")
    public void ProfileTest() throws Exception {
        DataBaseAccessor.create();

        User user1 = new User("reza@gmail.com", "ali1222345", "Ali", "akbari", "ll");
        User user2 = new User("kasra@gmail.com", "ali1222345", "Ali", "akbari", "ll");
        try {
            UserAccessor.addUser(user1);
            UserAccessor.addUser(user2);
        } catch (SQLException e) {
            System.out.println("User exists" + e.getMessage());
        }

        Profile prof = new Profile(user1.getUserId(), "aaa", "ddd", "jjj", Profile.Status.JOB_SEARCHER, Profile.Profession.ACTOR, 1);
        ProfileAccessor.addProfile(prof);
        System.out.println("profile added successfully");

        ProfileAccessor.updateProfile(new Profile(user1.getUserId(), "updated", "ddd", "jjj", Profile.Status.JOB_SEARCHER, Profile.Profession.ACTOR, 1));
        System.out.println("profile updated successfully");

        try {
            Profile p2 = ProfileAccessor.getProfile(user1.getUserId());
            System.out.println("profile fetched success fully: " + p2);
        } catch (NotFoundException e) {
            System.out.println("profile does not exist ");
        }
    }

    @Test
    @DisplayName("---- testing connect table")
    public void ConnectTest() throws Exception {
        DataBaseAccessor.create();
        try {
            ConnectController.addConnect(new Connect("user90059c8e-4be7-8764", "user241374d1-464e-863e", "this is connect note", Connect.AcceptState.WAITING));
        } catch (NotAcceptableException | NotFoundException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Connect added successfully");
    }
}
