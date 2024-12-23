package sdsdsd;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class Player extends Thread {
    private final String playerName;
    private final Scoreboard scoreboard;
    private boolean isPlaying = true;

    public Player(String playerName, Scoreboard scoreboard) {
        this.playerName = playerName;
        this.scoreboard = scoreboard;
    }

    @Override
    public void run() {
        Random random = new Random();

        while (isPlaying) {
            try {
                Thread.sleep(random.nextInt(2000) + 500); // Simulates time between actions

               
                if (random.nextBoolean()) {
                    int points = random.nextInt(10) + 1;
                    scoreboard.updateScore(playerName, points);
                    System.out.println(playerName + " attacked and scored " + points + " points!");
                } else {
                    System.out.println(playerName + " defended successfully!");
                }
            } catch (InterruptedException e) {
                System.out.println(playerName + " was interrupted.");
                Thread.currentThread().interrupt();
            }
        }
    }

    public void stopPlaying() {
        isPlaying = false;
    }
}

class Scoreboard {
    private final Object lock = new Object();
    private final List<PlayerScore> scores = new ArrayList<>();

    public void updateScore(String playerName, int points) {
        synchronized (lock) {
            PlayerScore playerScore = scores.stream()
                    .filter(score -> score.getPlayerName().equals(playerName))
                    .findFirst()
                    .orElse(null);

            if (playerScore == null) {
                playerScore = new PlayerScore(playerName);
                scores.add(playerScore);
            }
            playerScore.addPoints(points);
        }
    }

    public void displayScores() {
        synchronized (lock) {
            System.out.println("--- Current Scores ---");
            for (PlayerScore score : scores) {
                System.out.println(score.getPlayerName() + ": " + score.getPoints() + " points");
            }
            System.out.println("----------------------");
        }
    }
}

class PlayerScore {
    private final String playerName;
    private int points;

    public PlayerScore(String playerName) {
        this.playerName = playerName;
        this.points = 0;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getPoints() {
        return points;
    }

    public void addPoints(int points) {
        this.points += points;
    }
}

class GameMatch extends Thread {
    private final List<Player> players;
    private final Scoreboard scoreboard;
    private final int matchDuration; 

    public GameMatch(int matchDuration) {
        this.players = new ArrayList<>();
        this.scoreboard = new Scoreboard();
        this.matchDuration = matchDuration;
    }

    public void addPlayer(String playerName) {
        players.add(new Player(playerName, scoreboard));
    }

    @Override
    public void run() {
        System.out.println("Game started!");

        // Start all player threads
        for (Player player : players) {
            player.start();
        }

        try {
            Thread.sleep(matchDuration); // Simulates match duration
        } catch (InterruptedException e) {
            System.out.println("Match was interrupted.");
            Thread.currentThread().interrupt();
        }


        for (Player player : players) {
            player.stopPlaying();
        }

     
        for (Player player : players) {
            try {
                player.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("Game over!");
        scoreboard.displayScores();
    }
}

public class MultiplayerGameServer {
    public static void main(String[] args) {
        GameMatch match = new GameMatch(10000); // 10-second match duration

        // Add players
        match.addPlayer("Player1");
        match.addPlayer("Player2");
        match.addPlayer("Player3");

    
        match.start();

        try {
            match.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Thank you for playing!");
    }
}
