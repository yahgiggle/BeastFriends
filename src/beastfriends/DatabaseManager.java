package beastfriends;

import net.risingworld.api.database.Database;
import net.risingworld.api.objects.Player;
import net.risingworld.api.objects.Npc;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DatabaseManager {

    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());

    private final Database petDatabase;

    public DatabaseManager(Database petDatabase) {
        this.petDatabase = petDatabase;
    }

    public void initializeDatabase() {
        petDatabase.executeUpdate("CREATE TABLE IF NOT EXISTS FoxOwnerShip " +
                "(PlayerUID TEXT, UserName TEXT, FoxID TEXT PRIMARY KEY, FoxName TEXT, SetDistance INTEGER)");
        petDatabase.executeUpdate("CREATE TABLE IF NOT EXISTS PigOwnerShip " +
                "(PlayerUID TEXT, UserName TEXT, PigID TEXT PRIMARY KEY, PigName TEXT, SetDistance INTEGER)");
        petDatabase.executeUpdate("CREATE TABLE IF NOT EXISTS ChickenOwnerShip " +
                "(PlayerUID TEXT, UserName TEXT, ChickenID TEXT PRIMARY KEY, ChickenName TEXT, SetDistance INTEGER)");
        petDatabase.executeUpdate("CREATE TABLE IF NOT EXISTS CowOwnerShip " +
                "(PlayerUID TEXT, UserName TEXT, CowID TEXT PRIMARY KEY, CowName TEXT, SetDistance INTEGER)");
        petDatabase.executeUpdate("CREATE TABLE IF NOT EXISTS SheepOwnerShip " +
                "(PlayerUID TEXT, UserName TEXT, SheepID TEXT PRIMARY KEY, SheepName TEXT, SetDistance INTEGER)");
        petDatabase.executeUpdate("CREATE TABLE IF NOT EXISTS BearOwnerShip " +
                "(PlayerUID TEXT, UserName TEXT, BearID TEXT PRIMARY KEY, BearName TEXT, SetDistance INTEGER)");
        petDatabase.executeUpdate("CREATE TABLE IF NOT EXISTS LionOwnerShip " +
                "(PlayerUID TEXT, UserName TEXT, LionID TEXT PRIMARY KEY, LionName TEXT, SetDistance INTEGER)");
        petDatabase.executeUpdate("CREATE TABLE IF NOT EXISTS GoatOwnerShip " +
                "(PlayerUID TEXT, UserName TEXT, GoatID TEXT PRIMARY KEY, GoatName TEXT, SetDistance INTEGER)");
        petDatabase.executeUpdate("CREATE TABLE IF NOT EXISTS DummyOwnerShip " +
                "(PlayerUID TEXT, UserName TEXT, DummyID TEXT PRIMARY KEY, DummyName TEXT, SetDistance INTEGER)");
        petDatabase.executeUpdate("CREATE TABLE IF NOT EXISTS HareOwnerShip " +
                "(PlayerUID TEXT, UserName TEXT, HareID TEXT PRIMARY KEY, HareName TEXT, SetDistance INTEGER)");
        petDatabase.executeUpdate("CREATE TABLE IF NOT EXISTS EarthwormOwnerShip " +
                "(PlayerUID TEXT, UserName TEXT, EarthwormID TEXT PRIMARY KEY, EarthwormName TEXT, SetDistance INTEGER)");
        petDatabase.executeUpdate("CREATE TABLE IF NOT EXISTS SnakeOwnerShip " +
                "(PlayerUID TEXT, UserName TEXT, SnakeID TEXT PRIMARY KEY, SnakeName TEXT, SetDistance INTEGER)");
        petDatabase.executeUpdate("CREATE TABLE IF NOT EXISTS ScorpionOwnerShip " +
                "(PlayerUID TEXT, UserName TEXT, ScorpionID TEXT PRIMARY KEY, ScorpionName TEXT, SetDistance INTEGER)");
        petDatabase.executeUpdate("CREATE TABLE IF NOT EXISTS SpiderOwnerShip " +
                "(PlayerUID TEXT, UserName TEXT, SpiderID TEXT PRIMARY KEY, SpiderName TEXT, SetDistance INTEGER)");
        petDatabase.executeUpdate("CREATE TABLE IF NOT EXISTS DeerOwnerShip " +
                "(PlayerUID TEXT, UserName TEXT, DeerID TEXT PRIMARY KEY, DeerName TEXT, SetDistance INTEGER)");
        petDatabase.executeUpdate("CREATE TABLE IF NOT EXISTS ArcticFoxOwnerShip " +
                "(PlayerUID TEXT, UserName TEXT, ArcticFoxID TEXT PRIMARY KEY, ArcticFoxName TEXT, SetDistance INTEGER)");
        petDatabase.executeUpdate("CREATE TABLE IF NOT EXISTS MooseOwnerShip " +
                "(PlayerUID TEXT, UserName TEXT, MooseID TEXT PRIMARY KEY, MooseName TEXT, SetDistance INTEGER)");
        petDatabase.executeUpdate("CREATE TABLE IF NOT EXISTS WildSowOwnerShip " +
                "(PlayerUID TEXT, UserName TEXT, WildSowID TEXT PRIMARY KEY, WildSowName TEXT, SetDistance INTEGER)");
        petDatabase.executeUpdate("CREATE TABLE IF NOT EXISTS WildBoarOwnerShip " +
                "(PlayerUID TEXT, UserName TEXT, WildBoarID TEXT PRIMARY KEY, WildBoarName TEXT, SetDistance INTEGER)");
        petDatabase.executeUpdate("CREATE TABLE IF NOT EXISTS PolarBearOwnerShip " +
                "(PlayerUID TEXT, UserName TEXT, PolarBearID TEXT PRIMARY KEY, PolarBearName TEXT, SetDistance INTEGER)");
        petDatabase.executeUpdate("CREATE TABLE IF NOT EXISTS PenguinOwnerShip " +
                "(PlayerUID TEXT, UserName TEXT, PenguinID TEXT PRIMARY KEY, PenguinName TEXT, SetDistance INTEGER)");
        petDatabase.executeUpdate("CREATE TABLE IF NOT EXISTS HorseOwnerShip " +
                "(PlayerUID TEXT, UserName TEXT, HorseID TEXT PRIMARY KEY, HorseName TEXT, SetDistance INTEGER)");
        petDatabase.executeUpdate("CREATE TABLE IF NOT EXISTS ZebraOwnerShip " +
                "(PlayerUID TEXT, UserName TEXT, ZebraID TEXT PRIMARY KEY, ZebraName TEXT, SetDistance INTEGER)");
        petDatabase.executeUpdate("CREATE TABLE IF NOT EXISTS ElephantOwnerShip " +
                "(PlayerUID TEXT, UserName TEXT, ElephantID TEXT PRIMARY KEY, ElephantName TEXT, SetDistance INTEGER)");
        petDatabase.executeUpdate("CREATE TABLE IF NOT EXISTS RhinocerosOwnerShip " +
                "(PlayerUID TEXT, UserName TEXT, RhinocerosID TEXT PRIMARY KEY, RhinocerosName TEXT, SetDistance INTEGER)");
        petDatabase.executeUpdate("CREATE TABLE IF NOT EXISTS WolfOwnerShip " +
                "(PlayerUID TEXT, UserName TEXT, WolfID TEXT PRIMARY KEY, WolfName TEXT, SetDistance INTEGER)");
        petDatabase.executeUpdate("CREATE TABLE IF NOT EXISTS ArcticWolfOwnerShip " +
                "(PlayerUID TEXT, UserName TEXT, ArcticWolfID TEXT PRIMARY KEY, ArcticWolfName TEXT, SetDistance INTEGER)");
        petDatabase.executeUpdate("CREATE TABLE IF NOT EXISTS FireWolfOwnerShip " +
                "(PlayerUID TEXT, UserName TEXT, FireWolfID TEXT PRIMARY KEY, FireWolfName TEXT, SetDistance INTEGER)");
        petDatabase.executeUpdate("CREATE TABLE IF NOT EXISTS BanditOwnerShip " +
                "(PlayerUID TEXT, UserName TEXT, BanditID TEXT PRIMARY KEY, BanditName TEXT, SetDistance INTEGER)");
        petDatabase.executeUpdate("CREATE TABLE IF NOT EXISTS BarbarianOwnerShip " +
                "(PlayerUID TEXT, UserName TEXT, BarbarianID TEXT PRIMARY KEY, BarbarianName TEXT, SetDistance INTEGER)");
        petDatabase.executeUpdate("CREATE TABLE IF NOT EXISTS SkeletonOwnerShip " +
                "(PlayerUID TEXT, UserName TEXT, SkeletonID TEXT PRIMARY KEY, SkeletonName TEXT, SetDistance INTEGER)");
        petDatabase.executeUpdate("CREATE TABLE IF NOT EXISTS GhoulOwnerShip " +
                "(PlayerUID TEXT, UserName TEXT, GhoulID TEXT PRIMARY KEY, GhoulName TEXT, SetDistance INTEGER)");

        LOGGER.info("[BeastFriends] Database tables initialized.");
    }

    public void registerPet(Player player, Npc npc, String npcName) {
        String tableName = getOwnershipTable(npcName);
        String idColumn = getIdColumn(npcName);
        String petName = getDefaultPetName(npcName);
        petDatabase.executeUpdate("INSERT INTO `" + tableName + "` (PlayerUID, UserName, " + idColumn + ", " + npcName + "Name, SetDistance) " +
                "VALUES ('" + player.getUID() + "', '" + player.getName() + "', '" + npc.getGlobalID() + "', '" + petName + "', '0')");
        npc.setName(petName);
        player.sendTextMessage("Pet named " + petName + " registered!");
        LOGGER.info("[BeastFriends] Registered pet " + petName + " for " + player.getName());
    }

    public boolean isOwner(Player player, Npc npc) {
        String tableName = getOwnershipTable(npc.getDefinition().name.toLowerCase());
        if (tableName == null) return false;

        try (ResultSet rs = petDatabase.executeQuery("SELECT PlayerUID FROM `" + tableName + "` WHERE " +
                getIdColumn(npc.getDefinition().name.toLowerCase()) + " = '" + npc.getGlobalID() + "'")) {
            return rs.next() && rs.getString("PlayerUID").equals(player.getUID());
        } catch (SQLException e) {
            LOGGER.severe("Error verifying NPC ownership: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Added methods to match expected calls
    public ResultSet executeQuery(String query) throws SQLException {
        return petDatabase.executeQuery(query);
    }

    public void executeUpdate(String query) {
        petDatabase.executeUpdate(query);
    }

    public String getOwnershipTable(String npcName) {
        switch (npcName.toLowerCase()) {
            case "fox": return "FoxOwnerShip";
            case "foxcub": return "FoxOwnerShip";
            case "pig": return "PigOwnerShip";
            case "piglet": return "PigOwnerShip";
            case "chicken": return "ChickenOwnerShip";
            case "chick": return "ChickenOwnerShip";
            case "cow": return "CowOwnerShip";
            case "bull": return "CowOwnerShip";
            case "calf": return "CowOwnerShip";
            case "sheep": return "SheepOwnerShip";
            case "ram": return "SheepOwnerShip";
            case "lamb": return "SheepOwnerShip";
            case "sheepshorn": return "SheepOwnerShip";
            case "ramshorn": return "SheepOwnerShip";
            case "bear": return "BearOwnerShip";
            case "bearmale": return "BearOwnerShip";
            case "bearcub": return "BearOwnerShip";
            case "lion": return "LionOwnerShip";
            case "lioness": return "LionOwnerShip";
            case "goat": return "GoatOwnerShip";
            case "billygoat": return "GoatOwnerShip";
            case "goatling": return "GoatOwnerShip";
            case "dummy": return "DummyOwnerShip";
            case "hare": return "HareOwnerShip";
            case "earthworm": return "EarthwormOwnerShip";
            case "snake": return "SnakeOwnerShip";
            case "scorpion": return "ScorpionOwnerShip";
            case "spider": return "SpiderOwnerShip";
            case "deer": return "DeerOwnerShip";
            case "deerstag": return "DeerOwnerShip";
            case "deercalf": return "DeerOwnerShip";
            case "deerred": return "DeerOwnerShip";
            case "arcticfox": return "ArcticFoxOwnerShip";
            case "arcticfoxcub": return "ArcticFoxOwnerShip";
            case "moose": return "MooseOwnerShip";
            case "moosebull": return "MooseOwnerShip";
            case "moosecalf": return "MooseOwnerShip";
            case "wildsow": return "WildSowOwnerShip";
            case "wildboar": return "WildBoarOwnerShip";
            case "wildpiglet": return "WildSowOwnerShip";
            case "polarbear": return "PolarBearOwnerShip";
            case "penguin": return "PenguinOwnerShip";
            case "horse": return "HorseOwnerShip";
            case "foal": return "HorseOwnerShip";
            case "zebra": return "ZebraOwnerShip";
            case "elephant": return "ElephantOwnerShip";
            case "rhinoceros": return "RhinocerosOwnerShip";
            case "wolf": return "WolfOwnerShip";
            case "shewolf": return "WolfOwnerShip";
            case "wolfcub": return "WolfOwnerShip";
            case "arcticwolf": return "ArcticWolfOwnerShip";
            case "arcticshewolf": return "ArcticWolfOwnerShip";
            case "firewolf": return "FireWolfOwnerShip";
            case "bandit": return "BanditOwnerShip";
            case "barbarian": return "BarbarianOwnerShip";
            case "skeleton": return "SkeletonOwnerShip";
            case "ghoul": return "GhoulOwnerShip";
            default: return null;
        }
    }

    private String getIdColumn(String npcName) {
        return npcName.toLowerCase() + "id";
    }

    private String getDefaultPetName(String npcName) {
        switch (npcName.toLowerCase()) {
            case "fox": return "Lassie";
            case "pig": return "Casber";
            case "chicken": return "Peek";
            case "cow": return "Chops";
            case "sheep": return "LambChops";
            case "bear": return "Poo";
            case "lion": return "Leo";
            case "goat": return "Billy";
            case "dummy": return "John";
            case "piglet": return "Piggy";
            case "bull": return "Bully";
            case "calf": return "Calfy";
            case "ram": return "Rammy";
            case "lamb": return "Lamby";
            case "sheepshorn": return "Shornie";
            case "ramshorn": return "Ramshornie";
            case "billygoat": return "Billygoat";
            case "goatling": return "Goatling";
            case "chick": return "Chicky";
            case "hare": return "Harey";
            case "earthworm": return "Wormy";
            case "snake": return "Snakey";
            case "scorpion": return "Scorp";
            case "spider": return "Spidey";
            case "deer": return "Deery";
            case "deerstag": return "Staggy";
            case "deercalf": return "Calfy";
            case "deerred": return "Reddy";
            case "foxcub": return "Cubby";
            case "arcticfox": return "Arctic";
            case "arcticfoxcub": return "ArcticCub";
            case "moose": return "Moosey";
            case "moosebull": return "BullMoose";
            case "moosecalf": return "MooseCalf";
            case "wildsow": return "Sowwy";
            case "wildboar": return "Boary";
            case "wildpiglet": return "Piglet";
            case "bearmale": return "BearMan";
            case "bearcub": return "BearCub";
            case "polarbear": return "Polar";
            case "penguin": return "Pengy";
            case "horse": return "Horsey";
            case "foal": return "Foaly";
            case "zebra": return "Zebra";
            case "elephant": return "Ellie";
            case "rhinoceros": return "Rhino";
            case "lioness": return "Leona";
            case "wolf": return "Wolfy";
            case "shewolf": return "SheWolf";
            case "wolfcub": return "WolfCub";
            case "arcticwolf": return "ArcticWolf";
            case "arcticshewolf": return "ArcticShe";
            case "firewolf": return "FireWolf";
            case "bandit": return "Bandit";
            case "barbarian": return "Barb";
            case "skeleton": return "Skelly";
            case "ghoul": return "Ghoulie";
            default: return npcName.substring(0, 1).toUpperCase() + npcName.substring(1);
        }
    }

    public void closeDatabase() {
        if (petDatabase != null) {
            petDatabase.close();
        }
    }
}