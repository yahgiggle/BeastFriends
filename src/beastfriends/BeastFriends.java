package beastfriends;

import net.risingworld.api.Plugin;
import net.risingworld.api.events.Listener;
import net.risingworld.api.events.EventMethod;
import net.risingworld.api.events.player.PlayerMouseButtonEvent;
import net.risingworld.api.events.player.PlayerSpawnEvent;
import net.risingworld.api.objects.Player;
import net.risingworld.api.objects.Npc;
import net.risingworld.api.objects.Item;
import net.risingworld.api.objects.Inventory;
import net.risingworld.api.definitions.Npcs;
import net.risingworld.api.Timer;
import net.risingworld.api.database.Database;
import net.risingworld.api.ui.UIElement;
import net.risingworld.api.ui.UILabel;
import net.risingworld.api.utils.MouseButton;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BeastFriends extends Plugin implements Listener {

    static {
        System.out.println("[BeastFriends] BeastFriends class loaded by JVM.");
    }

    private Database petDatabase;
    private HashMap<Player, BeastFriendsUI> playerUIs = new HashMap<>();
    private HashMap<Player, Integer> playerPetFeed = new HashMap<>();
    private HashMap<Player, Boolean> playerPetCalled = new HashMap<>();

    @Override
    public void onEnable() {
        System.out.println("[BeastFriends] Entering onEnable...");
        try {
            petDatabase = getPetDatabaseConnection(getPath() + "/pets.db");
            if (petDatabase == null) {
                Logger.getLogger(BeastFriends.class.getName()).log(Level.SEVERE, "Failed to initialize database connection!");
                return;
            }
            System.out.println("[BeastFriends] Database connection established.");
            initializeDatabase();
            System.out.println("[BeastFriends] Initializing event listeners...");
            registerEventListener(this);
            System.out.println("[BeastFriends] Event listeners registered.");
        } catch (Exception e) {
            Logger.getLogger(BeastFriends.class.getName()).log(Level.SEVERE, "Failed to enable BeastFriends plugin", e);
        }
    }

    @Override
    public void onDisable() {
        System.out.println("-- BeastFriends PLUGIN DISABLED --");
        if (petDatabase != null) petDatabase.close();
        playerUIs.values().forEach(ui -> ui.closeAllMenus());
    }

    private void initializeDatabase() {
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
        System.out.println("[BeastFriends] Database tables initialized.");
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
                    showTameMenu(player, npc);
                }
            });
        }
    }

    private void showTameMenu(Player player, Npc npc) {
        System.out.println("[BeastFriends] Attempting to show tame menu for player: " + player.getName());
        BeastFriendsUI ui = playerUIs.computeIfAbsent(player, p -> {
            System.out.println("[BeastFriends] Creating new BeastFriendsUI for player: " + p.getName());
            try {
                BeastFriendsUI newUI = new BeastFriendsUI(this, p);
                System.out.println("[BeastFriends] BeastFriendsUI created successfully for player: " + p.getName());
                return newUI;
            } catch (Exception e) {
                System.out.println("[BeastFriends] Failed to create BeastFriendsUI for player: " + p.getName() + " - " + e.getMessage());
                return null;
            }
        });
        if (ui != null) {
            System.out.println("[BeastFriends] Showing tame menu for player: " + player.getName());
            ui.showTameMenu(npc);
        } else {
            System.out.println("[BeastFriends] Unable to show tame menu - UI is null for player: " + player.getName());
        }
    }

    public void attemptTame(Player player, Npc npc, BeastFriendsUI ui) {
        String npcName = npc.getDefinition().name.toLowerCase();
        int slot = player.getInventory().getQuickslotFocus();
        Item slotItem = player.getInventory().getItem(slot, Inventory.SlotType.Quickslot);
        if (slotItem == null) {
            ui.updateTameMenuText("You need to hold an item to tame!");
            return;
        }

        String foodItem = getRequiredFood(npcName);
        if (!slotItem.getName().toLowerCase().matches(foodItem)) {
            ui.updateTameMenuText("You need " + foodItem + " to tame a " + npcName + "!");
            return;
        }

        if (Boolean.TRUE.equals(playerPetCalled.get(player))) {
            ui.updateTameMenuText("You already called this " + npcName + "!");
            return;
        }

        checkOwnership(player, npc, () -> {
            playerPetCalled.put(player, true);
            ui.updateTameMenuText(npcName.substring(0, 1).toUpperCase() + npcName.substring(1) + " Called");
            startTamingProcess(player, npc, slotItem, ui, slot);
        }, ui);
    }

    private String getRequiredFood(String npcName) {
        switch (npcName) {
            case "fox": case "lion": return "steak";
            case "pig": case "goat": return "apple";
            case "chicken": return "earthworm";
            case "cow": return "grass";
            case "sheep": return "lettuceleaves";
            case "bear": return "corncob";
            default: return "";
        }
    }

    private void checkOwnership(Player player, Npc npc, Runnable onSuccess, BeastFriendsUI ui) {
        String tableName = getOwnershipTable(npc.getDefinition().name.toLowerCase());
        if (tableName == null) return;

        try (ResultSet rs = petDatabase.executeQuery("SELECT UserName FROM `" + tableName + "` WHERE " +
                getIdColumn(npc.getDefinition().name.toLowerCase()) + " = '" + npc.getGlobalID() + "'")) {
            if (rs.next()) {
                ui.updateTameMenuText(rs.getString("UserName") + " Already Owns This " + npc.getDefinition().name);
            } else {
                onSuccess.run();
            }
        } catch (SQLException e) {
            Logger.getLogger(BeastFriends.class.getName()).log(Level.SEVERE, null, e);
            ui.updateTameMenuText("Database error occurred!");
        }
    }

    private String getOwnershipTable(String npcName) {
        switch (npcName.toLowerCase()) {
            case "fox": return "FoxOwnerShip";
            case "pig": return "PigOwnerShip";
            case "chicken": return "ChickenOwnerShip";
            case "cow": return "CowOwnerShip";
            case "sheep": return "SheepOwnerShip";
            case "bear": return "BearOwnerShip";
            case "lion": return "LionOwnerShip";
            case "goat": return "GoatOwnerShip";
            case "dummy": return "DummyOwnerShip";
            default: return null;
        }
    }

    private String getIdColumn(String npcName) {
        return npcName.toLowerCase() + "id";
    }

    private void startTamingProcess(Player player, Npc npc, Item foodItem, BeastFriendsUI ui, int slot) {
        Timer tameTimer = new Timer(5.0f, 0.0f, -1, null);
        String npcName = npc.getDefinition().name.toLowerCase();
        final int[] feedCount = {playerPetFeed.getOrDefault(player, 0)};

        UILabel feedButton = (UILabel) player.getAttribute("tameFeedButton");
        if (feedButton != null) feedButton.setClickable(true);

        Runnable tameUpdate = () -> {
            npc.setLocked(false);
            npc.moveTo(player.getPosition());

            Random rand = new Random();
            if (rand.nextInt(10) == 5) {
                npc.setAlerted(true, 5);
                npc.moveTo(0, rand.nextInt(100), 0);
                ui.updateTameMenuText("The " + npcName + " ran away!");
                tameTimer.kill();
                playerPetCalled.put(player, false);
                return;
            }

            player.getNpcInLineOfSight(4, (closeNpc) -> {
                if (closeNpc != null && closeNpc.getGlobalID() == npc.getGlobalID()) {
                    closeNpc.setLocked(true);
                    tameTimer.kill();

                    if (foodItem.getStack() > 1) {
                        foodItem.setStack(foodItem.getStack() - 1);
                    } else {
                        player.getInventory().removeItem(slot, Inventory.SlotType.Quickslot);
                    }

                    feedCount[0]++;
                    playerPetFeed.put(player, feedCount[0]);
                    closeNpc.setLocked(false);

                    if (rand.nextInt(4) == 2) {
                        closeNpc.setAlerted(true, 5 - feedCount[0]);
                    }

                    ui.updateTameMenuText("Fed " + npcName + " (" + feedCount[0] + "/5 times)");
                    npc.moveTo(0, 100, 0);
                    npc.setAlerted(true, 5 - feedCount[0]);
                    playerPetCalled.put(player, false);

                    if (feedCount[0] == 5) {
                        playerPetFeed.put(player, 0);
                        ui.updateTameMenuText("This " + npcName + " is now your pet!");
                        closeNpc.setLocked(true);
                        registerPet(player, closeNpc, npcName);
                        playerUIs.get(player).showMyPetsMenu(player);
                    }
                }
            });
        };

        tameTimer.setTask(tameUpdate);
        tameTimer.start();
    }

    private void registerPet(Player player, Npc npc, String npcName) {
        String tableName = getOwnershipTable(npcName);
        String idColumn = getIdColumn(npcName);
        String petName = getDefaultPetName(npcName);
        petDatabase.executeUpdate("INSERT INTO `" + tableName + "` (PlayerUID, UserName, " + idColumn + ", " + npcName + "Name, SetDistance) " +
                "VALUES ('" + player.getUID() + "', '" + player.getName() + "', '" + npc.getGlobalID() + "', '" + petName + "', '0')");
        npc.setName(petName);
        player.sendTextMessage("Pet named " + petName + " registered!");
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
            default: return npcName.substring(0, 1).toUpperCase() + npcName.substring(1);
        }
    }

    private Database getPetDatabaseConnection(String dbPath) {
        try {
            return getSQLiteConnection(dbPath);
        } catch (Exception e) {
            Logger.getLogger(BeastFriends.class.getName()).log(Level.SEVERE, "Failed to connect to database: " + dbPath, e);
            return null;
        }
    }
}