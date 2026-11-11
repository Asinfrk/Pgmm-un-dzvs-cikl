package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import application.Statistics.PlayerStats;
import application.Statistics.TeamStats;

import java.sql.*;

public class PMainLayCon {

    // Observable lists
    ObservableList<Player> allPlayers = FXCollections.observableArrayList();
    FilteredList<Player> filteredPlayers = new FilteredList<>(allPlayers, p -> true);

    ObservableList<Team> allTeams = FXCollections.observableArrayList();
    FilteredList<Team> filteredTeams = new FilteredList<>(allTeams, t -> true);

    // Top bar
    @FXML private Button logoutBtn;
    @FXML private Label loggedInLabel;

    // Player tab
    @FXML private TextField txt_name, txt_age, txt_country, txt_position, txt_salary;
    @FXML private ComboBox<String> team_cmb;
    @FXML private Button addP, deleteP, updateP, clearP;
    @FXML private Label playerStatusLabel;

    @FXML private TableView<Player> tblviewPlayer;
    @FXML private TableColumn<Player, String> clm_name, clm_country, clm_position, clm_teamP;
    @FXML private TableColumn<Player, Integer> clm_age, clm_salary;
    @FXML private TableColumn<Player, Double> clm_ppg, clm_apg, clm_rpg;

    // Team tab
    @FXML private TextField txt_team, txt_wins, txt_loses, txt_WL, txt_streak;
    @FXML private TextField txt_conwins, txt_conloses, txt_conWL;
    @FXML private Button addT, deleteT, updateT, clearT, convert;
    @FXML private CheckBox checkbox;
    @FXML private Label teamStatusLabel;

    @FXML private TableView<Team> tblviewTeam;
    @FXML private TableColumn<Team, String> clm_team, clm_streak;
    @FXML private TableColumn<Team, Integer> clm_wins, clm_loses;
    @FXML private TableColumn<Team, Float> clm_WL;
    @FXML private TableColumn<Team, Double> clm_tppg, clm_tapg, clm_trpg;

    // Statistics tab
    @FXML private Button Sbtn_player, Sbtn_team;
    @FXML private ComboBox<String> Scmb_sort, Scmb_stat;
    @FXML private TableView<PlayerStats> StblviewPlayer;
    @FXML private TableView<TeamStats> StblviewTeam;
    @FXML private TableColumn<PlayerStats, String> Sclm_player;
    @FXML private TableColumn<PlayerStats, Double> Sclm_Pppg, Sclm_Papg, Sclm_Prpg;
    @FXML private TableColumn<TeamStats, String> Sclm_Team;
    @FXML private TableColumn<TeamStats, Double> Sclm_Tppg, Sclm_Tapg, Sclm_Trpg;


    private boolean suppressTeamFilter = false;

    @FXML
    public void initialize() {
        logoutBtn.setOnAction(e -> logout());
        setupPlayerTab();
        setupTeamTab();
        initializeStatisticsTab();
    }

    //Player tab setup

    private void setupPlayerTab() {

        // Team filter combo
        team_cmb.setItems(FXCollections.observableArrayList(
            "Show All",
            "Lakers", "Warriors", "Bulls", "Celtics", "Heat", "Nets", "Suns", "Bucks",
            "Nuggets", "76ers", "Knicks", "Clippers", "Mavericks", "Grizzlies", "Kings", "Timberwolves"
        ));
        team_cmb.setValue("Show All");

        // Table columns
        clm_name.setCellValueFactory(new PropertyValueFactory<>("name"));
        clm_age.setCellValueFactory(new PropertyValueFactory<>("age"));
        clm_country.setCellValueFactory(new PropertyValueFactory<>("country"));
        clm_position.setCellValueFactory(new PropertyValueFactory<>("position"));
        clm_salary.setCellValueFactory(new PropertyValueFactory<>("salary"));
        clm_teamP.setCellValueFactory(new PropertyValueFactory<>("team"));
        clm_ppg.setCellValueFactory(new PropertyValueFactory<>("ppg"));
        clm_apg.setCellValueFactory(new PropertyValueFactory<>("apg"));
        clm_rpg.setCellValueFactory(new PropertyValueFactory<>("rpg"));
        tblviewPlayer.setItems(filteredPlayers);

        loadPlayersFromDB();

        // Filter by team
        team_cmb.setOnAction(e -> {
            if (suppressTeamFilter) return;
            String selected = team_cmb.getValue();
            filteredPlayers.setPredicate(player -> {
                if (selected == null || selected.equals("Show All")) return true;
                return player.getTeam().equals(selected);
            });
        });

        tblviewPlayer.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, p) -> {
                if (p != null) {
                    txt_name.setText(p.getName());
                    txt_age.setText(String.valueOf(p.getAge()));
                    txt_country.setText(p.getCountry());
                    txt_position.setText(p.getPosition());
                    txt_salary.setText(String.valueOf(p.getSalary()));

                    suppressTeamFilter = true;
                    String playerTeam = p.getTeam();
                    if (playerTeam == null || playerTeam.isEmpty()) {
                        team_cmb.setValue("Show All");
                    } else {
                        team_cmb.setValue(playerTeam);
                    }
                    suppressTeamFilter = false;

                    clearPlayerStatus();
                }
            }
        );

        // Button actions
        addP.setOnAction(e -> addPlayer());
        deleteP.setOnAction(e -> deletePlayer());
        updateP.setOnAction(e -> updatePlayer());
        clearP.setOnAction(e -> clearPlayerFields());
    }

    private void addPlayer() {
        if (!playerFieldsAreValid()) return;

        try {
            String team = team_cmb.getValue();
            if (team == null || team.equals("Show All")) team = "";

            Player p = new Player(
                txt_name.getText().trim(),
                Integer.parseInt(txt_age.getText().trim()),
                txt_country.getText().trim(),
                txt_position.getText().trim(),
                Integer.parseInt(txt_salary.getText().trim()),
                team, 0.0, 0.0, 0.0
            );
            allPlayers.add(p);
            insertPlayerToDB(p);
            clearPlayerFields();
            showPlayerStatus("Player added successfully!", false);
        } catch (NumberFormatException ex) {
            showPlayerStatus("Age and Salary must be whole numbers.", true);
        }
    }

    private void deletePlayer() {
        Player p = tblviewPlayer.getSelectionModel().getSelectedItem();
        if (p == null) {
            showPlayerStatus("Please select a player to delete.", true);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Player");
        confirm.setHeaderText("Delete " + p.getName() + "?");
        confirm.setContentText("This action cannot be undone.");
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                allPlayers.remove(p);
                deletePlayerFromDB(p.getName());
                clearPlayerFields();
                showPlayerStatus("Player deleted.", false);
            }
        });
    }

    private void updatePlayer() {
        Player p = tblviewPlayer.getSelectionModel().getSelectedItem();
        if (p == null) {
            showPlayerStatus("Please select a player to update.", true);
            return;
        }
        if (!playerFieldsAreValid()) return;

        try {
            p.setName(txt_name.getText().trim());
            p.setAge(Integer.parseInt(txt_age.getText().trim()));
            p.setCountry(txt_country.getText().trim());
            p.setPosition(txt_position.getText().trim());
            p.setSalary(Integer.parseInt(txt_salary.getText().trim()));
            p.setTeam(team_cmb.getValue() == null ? "" : team_cmb.getValue());
            tblviewPlayer.refresh();
            updatePlayerInDB(p);
            showPlayerStatus("Player updated successfully!", false);
        } catch (NumberFormatException ex) {
            showPlayerStatus("Age and Salary must be whole numbers.", true);
        }
    }

    // Returns true only if all player fields have something in them
    private boolean playerFieldsAreValid() {
        if (txt_name.getText().trim().isEmpty() ||
            txt_age.getText().trim().isEmpty() ||
            txt_country.getText().trim().isEmpty() ||
            txt_position.getText().trim().isEmpty() ||
            txt_salary.getText().trim().isEmpty()) {
            showPlayerStatus("Please fill in all fields.", true);
            return false;
        }
        return true;
    }

    private void showPlayerStatus(String message, boolean isError) {
        if (playerStatusLabel == null) return;
        playerStatusLabel.setText(message);
        playerStatusLabel.setStyle(isError ? "-fx-text-fill: #e05555;" : "-fx-text-fill: #55cc55;");
        playerStatusLabel.setVisible(true);
    }

    private void clearPlayerStatus() {
        if (playerStatusLabel != null) playerStatusLabel.setVisible(false);
    }

    private void clearPlayerFields() {
        txt_name.clear();
        txt_age.clear();
        txt_country.clear();
        txt_position.clear();
        txt_salary.clear();
        team_cmb.setValue("Show All");
        tblviewPlayer.getSelectionModel().clearSelection();
        clearPlayerStatus();
    }

    //Team tab setup

    private void setupTeamTab() {

        // Table columns
        clm_team.setCellValueFactory(new PropertyValueFactory<>("teamName"));
        clm_wins.setCellValueFactory(new PropertyValueFactory<>("wins"));
        clm_loses.setCellValueFactory(new PropertyValueFactory<>("loses"));
        clm_WL.setCellValueFactory(new PropertyValueFactory<>("wl"));
        clm_streak.setCellValueFactory(new PropertyValueFactory<>("streak"));
        clm_tppg.setCellValueFactory(new PropertyValueFactory<>("ppg"));
        clm_tapg.setCellValueFactory(new PropertyValueFactory<>("apg"));
        clm_trpg.setCellValueFactory(new PropertyValueFactory<>("rpg"));
        tblviewTeam.setItems(filteredTeams);

        loadTeamsFromDB();

        // Auto-calculate W/L whenever wins or losses field changes
        txt_wins.textProperty().addListener((obs, old, val) -> autoUpdateWL());
        txt_loses.textProperty().addListener((obs, old, val) -> autoUpdateWL());

        // Click a row → fill in the form fields
        tblviewTeam.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, t) -> {
                if (t != null) {
                    txt_team.setText(t.getTeamName());
                    txt_wins.setText(String.valueOf(t.getWins()));
                    txt_loses.setText(String.valueOf(t.getLoses()));
                    txt_WL.setText(String.format("%.3f", t.getWl()));
                    txt_streak.setText(t.getStreak());
                    clearTeamStatus();
                }
            }
        );

        // Button actions
        addT.setOnAction(e -> addTeam());
        deleteT.setOnAction(e -> deleteTeam());
        updateT.setOnAction(e -> updateTeam());
        clearT.setOnAction(e -> clearTeamFields());

        // Checkbox: show only winning teams (more wins than losses)
        checkbox.setOnAction(e ->
            filteredTeams.setPredicate(team ->
                !checkbox.isSelected() || team.getWins() >= 41
            )
        );

        // W/L converter
        convert.setOnAction(e -> {
            try {
                int wins = Integer.parseInt(txt_conwins.getText().trim());
                int loses = Integer.parseInt(txt_conloses.getText().trim());
                txt_conWL.setText(String.format("%.3f", calculateWL(wins, loses)));
            } catch (NumberFormatException ex) {
                showAlert("Invalid Input", "Please enter valid whole numbers.");
            }
        });
    }

    // Live W/L update as user types wins/losses
    private void autoUpdateWL() {
        try {
            int wins = Integer.parseInt(txt_wins.getText().trim());
            int loses = Integer.parseInt(txt_loses.getText().trim());
            txt_WL.setText(String.format("%.3f", calculateWL(wins, loses)));
        } catch (NumberFormatException ex) {
          
        }
    }

    private void addTeam() {
        if (!teamFieldsAreValid()) return;

        try {
            int wins = Integer.parseInt(txt_wins.getText().trim());
            int loses = Integer.parseInt(txt_loses.getText().trim());
            float wl = calculateWL(wins, loses);
            txt_WL.setText(String.format("%.3f", wl));

            Team t = new Team(txt_team.getText().trim(), wins, loses, wl, txt_streak.getText().trim(), 0, 0, 0);
            allTeams.add(t);
            insertTeamToDB(t);
            clearTeamFields();
            showTeamStatus("Team added successfully!", false);
        } catch (NumberFormatException ex) {
            showTeamStatus("Wins and Losses must be whole numbers.", true);
        }
    }

    private void deleteTeam() {
        Team t = tblviewTeam.getSelectionModel().getSelectedItem();
        if (t == null) {
            showTeamStatus("Please select a team to delete.", true);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Team");
        confirm.setHeaderText("Delete " + t.getTeamName() + "?");
        confirm.setContentText("This action cannot be undone.");
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                allTeams.remove(t);
                deleteTeamFromDB(t.getTeamName());
                clearTeamFields();
                showTeamStatus("Team deleted.", false);
            }
        });
    }

    private void updateTeam() {
        Team t = tblviewTeam.getSelectionModel().getSelectedItem();
        if (t == null) {
            showTeamStatus("Please select a team to update.", true);
            return;
        }
        if (!teamFieldsAreValid()) return;

        try {
            int wins = Integer.parseInt(txt_wins.getText().trim());
            int loses = Integer.parseInt(txt_loses.getText().trim());
            float wl = calculateWL(wins, loses);
            txt_WL.setText(String.format("%.3f", wl));

            t.setTeamName(txt_team.getText().trim());
            t.setWins(wins);
            t.setLoses(loses);
            t.setWl(wl);
            t.setStreak(txt_streak.getText().trim());
            tblviewTeam.refresh();
            updateTeamInDB(t);
            showTeamStatus("Team updated successfully!", false);
        } catch (NumberFormatException ex) {
            showTeamStatus("Wins and Losses must be whole numbers.", true);
        }
    }

    // Returns true only if all required team fields are filled
    private boolean teamFieldsAreValid() {
        if (txt_team.getText().trim().isEmpty() ||
            txt_wins.getText().trim().isEmpty() ||
            txt_loses.getText().trim().isEmpty() ||
            txt_streak.getText().trim().isEmpty()) {
            showTeamStatus("Please fill in all fields.", true);
            return false;
        }
        return true;
    }

    private void showTeamStatus(String message, boolean isError) {
        if (teamStatusLabel == null) return;
        teamStatusLabel.setText(message);
        teamStatusLabel.setStyle(isError ? "-fx-text-fill: #e05555;" : "-fx-text-fill: #55cc55;");
        teamStatusLabel.setVisible(true);
    }

    private void clearTeamStatus() {
        if (teamStatusLabel != null) teamStatusLabel.setVisible(false);
    }

    private void clearTeamFields() {
        txt_team.clear();
        txt_wins.clear();
        txt_loses.clear();
        txt_WL.clear();
        txt_streak.clear();
        tblviewTeam.getSelectionModel().clearSelection();
        clearTeamStatus();
    }

    //Logout

    private void logout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    DatabaseConnection.closeConnection();
                    Stage newStage = new Stage();
                    Parent root = FXMLLoader.load(getClass().getResource("ProjMain.fxml"));
                    Scene scene = new Scene(root, 680, 480);
                    newStage.setTitle("Basketball League Manager – Login");
                    newStage.setScene(scene);
                    newStage.show();
                    logoutBtn.getScene().getWindow().hide();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //Statistics tab

    private void initializeStatisticsTab() {
        Scmb_stat.getItems().addAll("PPG", "APG", "RPG");
        Scmb_stat.getSelectionModel().selectFirst();
        Scmb_sort.getItems().addAll("Descending", "Ascending");
        Scmb_sort.getSelectionModel().selectFirst();

        Sclm_player.setCellValueFactory(new PropertyValueFactory<>("name"));
        Sclm_Pppg.setCellValueFactory(new PropertyValueFactory<>("ppg"));
        Sclm_Papg.setCellValueFactory(new PropertyValueFactory<>("apg"));
        Sclm_Prpg.setCellValueFactory(new PropertyValueFactory<>("rpg"));

        Sclm_Team.setCellValueFactory(new PropertyValueFactory<>("team"));
        Sclm_Tppg.setCellValueFactory(new PropertyValueFactory<>("ppg"));
        Sclm_Tapg.setCellValueFactory(new PropertyValueFactory<>("apg"));
        Sclm_Trpg.setCellValueFactory(new PropertyValueFactory<>("rpg"));

        loadPlayerStats();
        loadTeamStats();

        Sbtn_player.setOnAction(e -> showPlayerStats());
        Sbtn_team.setOnAction(e -> showTeamStats());
        Scmb_sort.setOnAction(e -> applySorting());
        Scmb_stat.setOnAction(e -> applySorting());

        showPlayerStats();
        applySorting();
    }

    private void applySorting() {
        String stat = Scmb_stat.getValue();
        String order = Scmb_sort.getValue();

        StblviewPlayer.getItems().sort((a, b) ->
            compareStats(stat, order, a.getPpg(), a.getApg(), a.getRpg(), b.getPpg(), b.getApg(), b.getRpg())
        );
        StblviewTeam.getItems().sort((a, b) ->
            compareStats(stat, order, a.getPpg(), a.getApg(), a.getRpg(), b.getPpg(), b.getApg(), b.getRpg())
        );
    }

    private int compareStats(String stat, String order,
                             double ap, double aa, double ar,
                             double bp, double ba, double br) {
        double v1 = switch (stat) { case "PPG" -> ap; case "APG" -> aa; default -> ar; };
        double v2 = switch (stat) { case "PPG" -> bp; case "APG" -> ba; default -> br; };
        return order.equals("Ascending") ? Double.compare(v1, v2) : Double.compare(v2, v1);
    }

    private void loadPlayerStats() {
        ObservableList<PlayerStats> stats = FXCollections.observableArrayList();
        for (Player p : allPlayers) {
            stats.add(new PlayerStats(p.getName(), p.getPpg(), p.getApg(), p.getRpg()));
        }
        StblviewPlayer.setItems(stats);
    }

    private void loadTeamStats() {
        ObservableList<TeamStats> stats = FXCollections.observableArrayList();
        for (Team t : allTeams) {
            stats.add(new TeamStats(t.getTeamName(), t.getPpg(), t.getApg(), t.getRpg()));
        }
        StblviewTeam.setItems(stats);
    }

    private void showPlayerStats() {
        StblviewPlayer.setVisible(true);
        StblviewTeam.setVisible(false);
    }

    private void showTeamStats() {
        StblviewPlayer.setVisible(false);
        StblviewTeam.setVisible(true);
    }

    // DB helpers

    private void loadPlayersFromDB() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM players");
            while (rs.next()) {
                allPlayers.add(new Player(
                    rs.getString("name"),
                    rs.getInt("age"),
                    rs.getString("country"),
                    rs.getString("position"),
                    rs.getInt("salary"),
                    rs.getString("team"),
                    rs.getDouble("ppg"),
                    rs.getDouble("apg"),
                    rs.getDouble("rpg")
                ));
            }
            rs.close();
        } catch (Exception e) {
            System.err.println("DB unavailable, loading sample players: " + e.getMessage());
            loadSamplePlayers();
        }
    }

    private void loadTeamsFromDB() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM teams");
            while (rs.next()) {
                allTeams.add(new Team(
                    rs.getString("team_name"),
                    rs.getInt("wins"),
                    rs.getInt("loses"),
                    rs.getFloat("wl"),
                    rs.getString("streak"),
                    rs.getDouble("ppg"),
                    rs.getDouble("apg"),
                    rs.getDouble("rpg")
                ));
            }
            rs.close();
        } catch (Exception e) {
            System.err.println("DB unavailable, loading sample teams: " + e.getMessage());
            loadSampleTeams();
        }
    }

    private void insertPlayerToDB(Player p) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "INSERT INTO players (name,age,country,position,salary,team,ppg,apg,rpg) VALUES (?,?,?,?,?,?,?,?,?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, p.getName());
            ps.setInt(2, p.getAge());
            ps.setString(3, p.getCountry());
            ps.setString(4, p.getPosition());
            ps.setInt(5, p.getSalary());
            ps.setString(6, p.getTeam());
            ps.setDouble(7, p.getPpg());
            ps.setDouble(8, p.getApg());
            ps.setDouble(9, p.getRpg());
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deletePlayerFromDB(String name) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM players WHERE name = ?");
            ps.setString(1, name);
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updatePlayerInDB(Player p) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "UPDATE players SET age=?,country=?,position=?,salary=?,team=? WHERE name=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, p.getAge());
            ps.setString(2, p.getCountry());
            ps.setString(3, p.getPosition());
            ps.setInt(4, p.getSalary());
            ps.setString(5, p.getTeam());
            ps.setString(6, p.getName());
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertTeamToDB(Team t) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "INSERT INTO teams (team_name,wins,loses,wl,streak,ppg,apg,rpg) VALUES (?,?,?,?,?,?,?,?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, t.getTeamName());
            ps.setInt(2, t.getWins());
            ps.setInt(3, t.getLoses());
            ps.setFloat(4, t.getWl());
            ps.setString(5, t.getStreak());
            ps.setDouble(6, t.getPpg());
            ps.setDouble(7, t.getApg());
            ps.setDouble(8, t.getRpg());
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteTeamFromDB(String name) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM teams WHERE team_name = ?");
            ps.setString(1, name);
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateTeamInDB(Team t) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "UPDATE teams SET wins=?,loses=?,wl=?,streak=? WHERE team_name=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, t.getWins());
            ps.setInt(2, t.getLoses());
            ps.setFloat(3, t.getWl());
            ps.setString(4, t.getStreak());
            ps.setString(5, t.getTeamName());
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Misc helpers

    private float calculateWL(int wins, int loses) {
        int total = wins + loses;
        return total == 0 ? 0f : (float) wins / total;
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    //Sample fallback data

    private void loadSamplePlayers() {
        allPlayers.addAll(
            new Player("LeBron James", 39, "USA", "SF", 47600000, "Lakers", 25.7, 8.3, 7.5),
            new Player("Anthony Davis", 31, "USA", "PF", 40000000, "Lakers", 24.3, 3.1, 12.6),
            new Player("Stephen Curry", 36, "USA", "PG", 51900000, "Warriors", 29.4, 6.1, 5.2),
            new Player("Klay Thompson", 34, "USA", "SG", 43200000, "Warriors", 17.9, 2.3, 3.3),
            new Player("Giannis Antetokounmpo", 29, "Greece", "PF", 48700000, "Bucks", 30.2, 5.8, 11.4),
            new Player("Nikola Jokic", 29, "Serbia", "C", 47600000, "Nuggets", 26.4, 9.0, 12.4),
            new Player("Jayson Tatum", 26, "USA", "SF", 32600000, "Celtics", 27.1, 4.8, 8.1),
            new Player("Luka Doncic", 25, "Slovenia", "PG", 43000000, "Mavericks", 33.9, 9.8, 9.2),
            new Player("Joel Embiid", 30, "Cameroon", "C", 47600000, "76ers", 34.7, 5.6, 11.0),
            new Player("Kevin Durant", 35, "USA", "SF", 47600000, "Suns", 27.3, 5.3, 6.7),
            new Player("Jimmy Butler", 34, "USA", "SF", 45100000, "Heat", 21.5, 5.4, 5.2),
            new Player("Damian Lillard", 34, "USA", "PG", 45600000, "Bucks", 24.7, 7.1, 4.4),
            new Player("Devin Booker", 27, "USA", "SG", 36000000, "Suns", 26.8, 6.5, 4.5),
            new Player("Jalen Brunson", 28, "USA", "PG", 26000000, "Knicks", 28.7, 6.7, 3.6),
            new Player("Anthony Edwards", 23, "USA", "SG", 40000000, "Timberwolves", 25.9, 5.1, 5.4)
        );
    }

    private void loadSampleTeams() {
        allTeams.addAll(
            new Team("Lakers", 46, 36, calculateWL(46, 36), "W2", 116.2, 25.1, 44.0),
            new Team("Warriors", 44, 38, calculateWL(44, 38), "L1", 118.9, 27.3, 45.1),
            new Team("Celtics", 57, 25, calculateWL(57, 25), "W4", 120.1, 25.5, 43.9),
            new Team("Bucks", 49, 33, calculateWL(49, 33), "W1", 119.3, 24.8, 44.4),
            new Team("Nuggets", 53, 29, calculateWL(53, 29), "W3", 117.8, 28.0, 45.0),
            new Team("Heat", 44, 38, calculateWL(44, 38), "L2", 112.0, 23.5, 43.5),
            new Team("Suns", 49, 33, calculateWL(49, 33), "W2", 115.5, 26.0, 43.5),
            new Team("76ers", 47, 35, calculateWL(47, 35), "L1", 114.8, 24.5, 44.2)
        );
    }
}