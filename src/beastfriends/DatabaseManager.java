package beastfriends;

import static beastfriends.BeastFriends.beastfriendsDataBaseAccess1;
import static beastfriends.BeastFriends.plugin;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.risingworld.api.Plugin;
import net.risingworld.api.World;

public class DatabaseManager extends BeastFriends{

  

    public DatabaseManager(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Initializes the database structure for managing NPCs.
     * Creates tables `beastfriends` and `DataBaseVersion` if they don't exist.
     */
    public void createDatabaseStructure() {
        System.out.println("-- Notes Database Loaded --");
        String WorldName = World.getName();
        
        beastfriendsDataBaseAccess1 = getSQLiteConnection(plugin.getPath() + "/"+WorldName+"/database.db");
        beastfriendsDataBaseAccess2 = getSQLiteConnection(plugin.getPath() + "/"+WorldName+"/database.db");
        beastfriendsDataBaseAccess3 = getSQLiteConnection(plugin.getPath() + "/"+WorldName+"/database.db");
        
        if (beastfriendsDataBaseAccess1 == null) {
            System.out.println("Failed to initialize database connection!");
            return;
        }
        
        // Create `beastfriends` table to store NPC data
        // TypeName: Type of NPC (e.g., pig, cow), NpcVariation: Variant (e.g., 1 or 2),
        // GroupId: Group affiliation, OwnerUID: Player UID, OwnerName: Player name,
        // health: NPC health, posx/posy/posz: Position, rotx/roty/rotz/rotw: Rotation,
        // Age: NPC Age, OwnerDate: Ownership timestamp
        beastfriendsDataBaseAccess1.execute("CREATE TABLE IF NOT EXISTS `beastfriends` (" +
                "`ID` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`NpcName` TEXT, " +
                "`TypeName` TEXT, " +
                "`NpcVariation` INTEGER, " +
                "`GroupId` INTEGER, " +
                "`GlobalID` LONG, " +
                "`OwnerUID` LONG, " +
                "`OwnerName` VARCHAR(64), " +
                "`health` INTEGER, " +
                "`posx` FLOAT, " +
                "`posy` FLOAT, " +
                "`posz` FLOAT, " +
                "`rotx` FLOAT, " +
                "`roty` FLOAT, " +
                "`rotz` FLOAT, " +
                "`rotw` FLOAT, " +
                "`Age` FLOAT, " +
                "`Guard` BOOLEAN, " +
                "`OwnerDate` TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL)");

        // Create `DataBaseVersion` table to track database schema version
        beastfriendsDataBaseAccess1.execute("CREATE TABLE IF NOT EXISTS `DataBaseVersion` (" +
                "`ID` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`Version` INTEGER)");

        // Initialize version if not exists
        try (ResultSet result = beastfriendsDataBaseAccess1.executeQuery("SELECT * FROM `DataBaseVersion` WHERE ID = '1'")) {
           if (!result.next())
        beastfriendsDataBaseAccess1.executeUpdate("INSERT INTO `DataBaseVersion` (Version) VALUES ('0');"); 
        } catch (SQLException ex) {Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, (String)null, ex);} 
    }
}

