package com.driver;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class WhatsappService {
    WhatsappRepository repo = new WhatsappRepository();

    public String createUser(String name, String mobile) throws Exception {
        if(repo.getUserMobile().contains(mobile)){
            throw new Exception("User already exists");
        }
        repo.getUserMobile().add(mobile);
        User user = new User(name, mobile);
        repo.getUser().add(user);
        return  "SUCCESS";

    }

    public Group createGroup(List<User> users) {
        String groupName = "";
        HashMap<Group, List<User>> groupUserMap = repo.getGroupUserMap();
        if(users.size() == 2){
            groupName = users.get(1).getName();
        }
        else {
            List<String> groupNames = groupUserMap.keySet().stream().map(Group::getName).filter(x-> StringUtils.startsWithIgnoreCase(x, "Group")).collect(Collectors.toList());
            groupName = "Group"+(groupNames.size()+1);
        }

        Group grp = new Group(groupName, users.size());
        groupUserMap.put(grp,users);
        return grp;

    }

    public int createMessage(String content) {
        int count = repo.getMessageId() + 1;
        Message message = new Message(count, content, new Date());
        return count;
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception {
        if(!repo.getGroupUserMap().containsKey(group)) throw new Exception("Group does not exist");
        if(!this.userExistsInGroup(group, sender)) throw  new Exception("You are not allowed to send message");

        List<Message> messages = new ArrayList<>();
        if(repo.getGroupMessageMap().containsKey(group)) messages = repo.getGroupMessageMap().get(group);

        messages.add(message);
        repo.getGroupMessageMap().put(group, messages);
        return messages.size();
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception {
        if(!repo.getGroupUserMap().containsKey(group)) throw new Exception("Group does not exist");
        if(!repo.getAdminMap().get(group).equals(approver)) throw new Exception("Approver does not have rights");
        if(!this.userExistsInGroup(group, user)) throw  new Exception("User is not a participant");

        repo.getAdminMap().put(group, user);
        return "SUCCESS";
    }

    public boolean userExistsInGroup(Group group, User sender) {
        List<User> users = repo.getGroupUserMap().get(group);
        for(User user: users) {
            if(user.equals(sender)) return true;
        }

        return false;
    }

}
