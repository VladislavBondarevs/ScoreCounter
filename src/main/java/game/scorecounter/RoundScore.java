package game.scorecounter;

public class RoundScore {
    private int round;
    private int team1Score;
    private int team2Score;

    public RoundScore() {}

    public RoundScore(int round) {
        this.round = round;
        this.team1Score = 0;
        this.team2Score = 0;
    }

    public RoundScore(int round, int team1Score, int team2Score) {
        this.round = round;
        this.team1Score = team1Score;
        this.team2Score = team2Score;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public int getTeam1Score() {
        return team1Score;
    }

    public void setTeam1Score(int team1Score) {
        this.team1Score = team1Score;
    }

    public int getTeam2Score() {
        return team2Score;
    }

    public void setTeam2Score(int team2Score) {
        this.team2Score = team2Score;
    }

    @Override
    public String toString() {
        return "RoundScore{" +
                "round=" + round +
                ", team1Score=" + team1Score +
                ", team2Score=" + team2Score +
                '}';
    }
}
