package uta.cse3310;

import java.util.ArrayList;
import java.util.List;

public class Chat {
    private List<String> messages = new ArrayList<>();

    // Method to add a message to the chat
    public void addMessage(String message) {
        messages.add(message);
    }

    // Method to get all messages
    public List<String> getMessages() {
        return messages;
    }
}
