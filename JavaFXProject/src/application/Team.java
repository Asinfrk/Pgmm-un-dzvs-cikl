package application;

public class Team {

    private String teamName;
    private int wins;
    private int loses;
    private float wl;
    private String streak;
    private double ppg;
    private double apg;
    private double rpg;

    public Team(String teamName, int wins, int loses, float wl, String streak,
                double ppg, double apg, double rpg) {
        this.teamName = teamName;
        this.wins = wins;
        this.loses = loses;
        this.wl = wl;
        this.streak = streak;
        this.ppg = ppg;
        this.apg = apg;
        this.rpg = rpg;
    }

    public String getTeamName() {
    	return teamName; 
    }
    public void setTeamName(String teamName) {
    	this.teamName = teamName; 
    }

    public int getWins() {
    	return wins; 
    }
    public void setWins(int wins) {
    	this.wins = wins; 
    }

    public int getLoses() {
    	return loses; 
    }
    public void setLoses(int loses) {
    	this.loses = loses; 
    }

    public float getWl() {
    	return wl; 
    }
    public void setWl(float wl) {
    	this.wl = wl; 
    }

    public String getStreak() {
    	return streak; 
    }
    public void setStreak(String streak) {
    	this.streak = streak; 
    }

    public double getPpg() {
    	return ppg; 
    }

    public double getApg() {
    	return apg; 
    }

    public double getRpg() {
    	return rpg; 
    }

}
