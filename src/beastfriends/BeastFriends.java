package beastfriends;

import net.risingworld.api.Plugin;
import net.risingworld.api.events.Listener;
import net.risingworld.api.events.EventMethod;
import net.risingworld.api.events.player.PlayerMouseButtonEvent;
import net.risingworld.api.events.player.PlayerSpawnEvent;
import net.risingworld.api.objects.Player;
import net.risingworld.api.objects.Npc;
import net.risingworld.api.definitions.Npcs;
import net.risingworld.api.database.Database;
import net.risingworld.api.utils.MouseButton;
import java.util.logging.Logger;
import net.risingworld.api.events.player.PlayerConnectEvent;
import java.util.HashMap;

public class BeastFriends extends Plugin implements Listener {

    private static final Logger LOGGER = Logger.getLogger(BeastFriends.class.getName());
    private Database petDatabase;
    private DatabaseManager dbManager;
    private HashMap<Player, BeastFriendsUI> playerUIs = new HashMap<>(); // Store UI instances per player

    @Override
    public void onEnable() {
        System.out.println("[BeastFriends] Entering onEnable...");
        try {
            petDatabase = getSQLiteConnection(getPath() + "/pets.db");
            if (petDatabase == null) {
                LOGGER.severe("Failed to initialize database connection!");
                return;
            }
            dbManager = new DatabaseManager(petDatabase);
            System.out.println("[BeastFriends] Database connection established.");
            dbManager.initializeDatabase();
            System.out.println("[BeastFriends] Initializing event listeners...");
            registerEventListener(this);
            System.out.println("[BeastFriends] Event listeners registered.");
        } catch (Exception e) {
            LOGGER.severe("Failed to enable BeastFriends plugin: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        System.out.println("-- BeastFriends PLUGIN DISABLED --");
        if (dbManager != null) {
            dbManager.closeDatabase();
        }
        for (BeastFriendsUI ui : playerUIs.values()) {
            ui.closeAllMenus();
        }
        playerUIs.clear();
    }

    @EventMethod
    public void onPlayerConnect(PlayerConnectEvent event) {
        Player player = event.getPlayer();
        // No need to set thisfeedcount here; handled in TamingManager
    }

    @EventMethod
    public void onPlayerSpawn(PlayerSpawnEvent event) {
        Player player = event.getPlayer();
        player.setListenForMouseInput(true);
        System.out.println("[BeastFriends] Enabled mouse input for player: " + player.getName());
    }

    @EventMethod
    public void onPlayerMouseButtonEvent(PlayerMouseButtonEvent event) {
        Player player = event.getPlayer();
        if (event.isPressed() && event.getButton() == MouseButton.Right) {
            player.getNpcInLineOfSight(100, (npc) -> {
                if (npc != null && npc.getDefinition().type == Npcs.Type.Animal) {
                    BeastFriendsUI ui = playerUIs.get(player);
                    if (ui == null) {
                        ui = new BeastFriendsUI(this, player);
                        playerUIs.put(player, ui);
                    }
                    TamingManager taming = ui.getTaming();
                    if (dbManager.isOwner(player, npc)) {
                        ui.showPetManagementMenu(npc);
                    } else {
                        ui.showTameUI(npc); // Will check NPC match internally
                        taming.attemptTame(player, npc); // Start or continue taming if same NPC
                    }
                }
            });
        }
    }

    public void makePetFollow(Player player, Npc npc) {
        PetManager pets = new PetManager(this, dbManager);
        pets.makePetFollow(player, npc);
    }

    public void makePetStandStill(Player player, Npc npc) {
        PetManager pets = new PetManager(this, dbManager);
        pets.makePetStandStill(player, npc);
    }

    public void makePetRoam(Player player, Npc npc) {
        PetManager pets = new PetManager(this, dbManager);
        pets.makePetRoam(player, npc);
    }
}