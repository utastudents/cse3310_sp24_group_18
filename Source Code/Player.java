import java.awt.Color;

public class Player {
    private String username;
    private Color color;
    private int score;
    private PlayerStatus status;

    // Getters for username, color, score, and status
    // getters
    public String getUsername() {
        return username;
    }

    public Color getColor() {
        return color;
    }

    public int getScore() {
        return score;
    }

    public PlayerStatus getStatus() {
        return status;
    }
}