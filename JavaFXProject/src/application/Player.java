package application;

public class Player {
    	private int salary;
	private String name;
	private int age;
	private String country;
	private String position;
	private String team;
	private double ppg;
	private double apg;
	private double rpg;

	
	public Player(String name, int age, String country, String position, int salary, String team,
            double ppg, double apg, double rpg) {
  this.name = name;
  this.age = age;
  this.country = country;
  this.position = position;
  this.salary = salary;
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



	public int getSalary() {
		return salary;
	}


	public void setSalary(int salary) {
		this.salary = salary;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public int getAge() {
		return age;
	}


	public void setAge(int age) {
		this.age = age;
	}


	public String getCountry() {
		return country;
	}


	public void setCountry(String country) {
		this.country = country;
	}


	public String getPosition() {
		return position;
	}


	public void setPosition(String position) {
		this.position = position;
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

