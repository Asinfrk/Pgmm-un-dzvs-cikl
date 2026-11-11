package application;

public class Statistics {

    public static class PlayerStats {

        private String name;
        private double ppg;
        private double apg;
        private double rpg;

        public PlayerStats(String name, double ppg, double apg, double rpg) {
            this.name = name;
            this.ppg = ppg;
            this.apg = apg;
            this.rpg = rpg;
        }

        public String getName() {
        	return name; 
        }
        public void setName(String name) {
        	this.name = name; 
        }

        public double getPpg() {
        	return ppg; 
        }
        public void setPpg(double ppg) {
        	this.ppg = ppg; 
        }

        public double getApg() {
        	return apg; 
        }
        public void setApg(double apg) {
        	this.apg = apg; 
        }

        public double getRpg() {
        	return rpg; 
        }
        public void setRpg(double rpg) {
        	this.rpg = rpg; 
        }
    }

    public static class TeamStats {

        private String team;
        private double ppg;
        private double apg;
        private double rpg;

        public TeamStats(String team, double ppg, double apg, double rpg) {
            this.team = team;
            this.ppg = ppg;
            this.apg = apg;
            this.rpg = rpg;
        }

        public String getTeam() {
        	return team; 
        }
        public void setTeam(String team) {
        	this.team = team; 
        }

        public double getPpg() {
        	return ppg; 
        }
        public void setPpg(double ppg) {
        	this.ppg = ppg; 
        }

        public double getApg() {
        	return apg; 
        }
        public void setApg(double apg) {
        	this.apg = apg; 
        }

        public double getRpg() {
        	return rpg; 
        }
        public void setRpg(double rpg) {
        	this.rpg = rpg; 
        }
    }
}
