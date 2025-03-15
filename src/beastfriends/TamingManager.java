package beastfriends;

import net.risingworld.api.Timer;
import net.risingworld.api.objects.Player;
import net.risingworld.api.objects.Npc;
import net.risingworld.api.objects.Item;
import net.risingworld.api.objects.Inventory;
import net.risingworld.api.definitions.Npcs;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Logger;

public class TamingManager {

    private static final Logger LOGGER = Logger.getLogger(TamingManager.class.getName());

    private final BeastFriends plugin;
    private final BeastFriendsUI ui;
    private final DatabaseManager dbManager;
    private final HashMap<Player, Timer> playerTameTimers = new HashMap<>(); // Timer for each player's taming
    private final HashMap<Player, Integer> playerFeedCount = new HashMap<>(); // Feed count per player
    private final HashMap<Player, Integer> playerTameCountdowns = new HashMap<>(); // Countdown seconds per player
    private final HashMap<Player, Npc> playerTamingNpcs = new HashMap<>(); // NPC being tamed per player
    private final HashMap<Player, String> playerTamingNpcNames = new HashMap<>(); // NPC name for UI updates

    public TamingManager(BeastFriends plugin, BeastFriendsUI ui, DatabaseManager dbManager) {
        this.plugin = plugin;
        this.ui = ui;
        this.dbManager = dbManager;
    }

    public void attemptTame(Player player, Npc npc) {
        String npcName = npc.getDefinition().name.toLowerCase();
        int slot = player.getInventory().getQuickslotFocus();
        Item slotItem = player.getInventory().getItem(slot, Inventory.SlotType.Quickslot);
        if (slotItem == null) {
            ui.updateInfoLabel("You need to hold an item to tame!");
            return;
        }

        String foodItem = getRequiredFood(npcName);
        if (!slotItem.getName().toLowerCase().matches(foodItem)) {
            ui.updateInfoLabel("You need " + foodItem + " to tame a " + npcName + "!");
            return;
            
        }

        // Check if already taming
        if (playerTamingNpcs.containsKey(player)) {
            Npc tamingNpc = playerTamingNpcs.get(player);
            if (tamingNpc.getGlobalID() != npc.getGlobalID()) {
                // Let showTameUI handle the "not same NPC" message
                return;
            }
            // If same NPC, proceed to feed
            feedNpc(player, npc, slotItem, slot);
            return;
        }

        // Start new taming process
        LOGGER.info("[BeastFriends] " + player.getName() + " starting taming " + npcName);
        checkOwnership(player, npc, () -> {
            playerFeedCount.put(player, 0); // Initialize feed count
            playerTamingNpcs.put(player, npc); // Track the NPC
            playerTamingNpcNames.put(player, npcName); // Store NPC name
            ui.showTameUI(npc); // Show UI
        });
    }

    private void feedNpc(Player player, Npc npc, Item foodItem, int slot) {
        String npcName = playerTamingNpcNames.get(player);
        int currentFeedCount = playerFeedCount.getOrDefault(player, 0);

        // Start or continue timer
        Timer oldTimer = playerTameTimers.get(player);
        if (oldTimer != null) oldTimer.kill();
        playerTameCountdowns.put(player, 60); // Reset countdown to 60 seconds
        final Timer newTimer = new Timer(1.0f, 0.0f, 60, () -> {
            int secondsLeft = playerTameCountdowns.getOrDefault(player, 60);
            int feedCount = playerFeedCount.getOrDefault(player, 0);
            ui.updateInfoLabel(npcName.substring(0, 1).toUpperCase() + npcName.substring(1) + 
                    " Called Feed " + feedCount + "/5 times\nFeed again within " + secondsLeft + "s!");
            secondsLeft--;
            playerTameCountdowns.put(player, secondsLeft);
            if (secondsLeft <= 0) {
                ui.updateInfoLabel(npcName.substring(0, 1).toUpperCase() + npcName.substring(1) + 
                        " Called Feed " + feedCount + "/5 times\nTaming failed - time expired!");
                resetTaming(player, npc);
            }
        });
        playerTameTimers.put(player, newTimer);
        newTimer.start();

        npc.setLocked(false);
        npc.moveTo(player.getPosition());

        Random rand = new Random();
        if (rand.nextInt(20) == 5 && currentFeedCount < 5) {
            npc.setAlerted(true, 5);
            float offsetX = rand.nextFloat() * 10 - 5;
            float offsetZ = rand.nextFloat() * 10 - 5;
            npc.moveTo(player.getPosition().x + offsetX, player.getPosition().y, player.getPosition().z + offsetZ);
            ui.updateInfoLabel(npcName.substring(0, 1).toUpperCase() + npcName.substring(1) + 
                    " Called Feed " + currentFeedCount + "/5 times\nThe " + npcName + " ran away!");
            resetTaming(player, npc);
            return;
        }

        npc.setLocked(true);

        // Consume the food item and increment feed count
        if (foodItem.getStack() > 1) {
            foodItem.setStack(foodItem.getStack() - 1);
        } else {
            player.getInventory().removeItem(slot, Inventory.SlotType.Quickslot);
        }
        int newFeedCount = currentFeedCount + 1;
        
        playerFeedCount.put(player, newFeedCount);
        player.sendTextMessage("feedcount=" + newFeedCount); // Log feed amount
        LOGGER.info("[BeastFriends] " + player.getName() + " fed " + npcName + ", new feed count: " + newFeedCount);

        npc.setLocked(false);

        if (rand.nextInt(8) == 2) {
            npc.setAlerted(true, 5 - newFeedCount);
        }

        ui.updateInfoLabel(npcName.substring(0, 1).toUpperCase() + npcName.substring(1) + 
                " Called Feed " + newFeedCount + "/5 times\nFeed again within 60s!");
        npc.moveTo(player.getPosition().x + 2, player.getPosition().y, player.getPosition().z);

        if (newFeedCount >= 5) {
            LOGGER.info("[BeastFriends] Taming complete for " + npcName + " by " + player.getName());
            playerFeedCount.put(player, 0);
            ui.updateInfoLabel(npcName.substring(0, 1).toUpperCase() + npcName.substring(1) + 
                    " Called Feed 5/5 times\nThis " + npcName + " is now your pet!");
            npc.setLocked(true);
            npc.setAlerted(false);
            npc.moveTo(player.getPosition().x, player.getPosition().y, player.getPosition().z);
            String tableName = getOwnershipTable(npcName);
            String defaultPetName = getDefaultPetName(npcName);
            dbManager.executeUpdate("INSERT INTO `" + tableName + "` (PlayerUID, UserName, " + npcName + "ID, " + npcName + "Name, SetDistance) " +
                    "VALUES ('" + player.getUID() + "', '" + player.getName() + "', '" + npc.getGlobalID() + "', '" + defaultPetName + "', '0')");
            npc.setName(defaultPetName);
            ui.showMyPetsMenu(player);
            ui.closeTameUI();
            playerTameTimers.remove(player);
            playerTamingNpcs.remove(player);
            playerTamingNpcNames.remove(player);
        }
    }

    private void resetTaming(Player player, Npc npc) {
        npc.setLocked(false);
        ui.closeTameUI();
        Timer timer = playerTameTimers.get(player);
        if (timer != null) timer.kill();
        playerTameTimers.remove(player);
        playerTameCountdowns.remove(player);
        playerTamingNpcs.remove(player);
        playerTamingNpcNames.remove(player);
        playerFeedCount.remove(player);
        LOGGER.info("[BeastFriends] Taming reset for " + player.getName());
    }

    private String getRequiredFood(String npcName) {
        switch (npcName) {
            case "fox": case "lion": case "lioness": case "wolf": case "shewolf": case "arcticwolf": case "arcticshewolf": case "firewolf": return "steak";
            case "pig": case "piglet": case "goat": case "billygoat": case "goatling": return "apple";
            case "chicken": case "chick": return "earthworm";
            case "cow": case "bull": case "calf": return "grass";
            case "sheep": case "ram": case "lamb": case "sheepshorn": case "ramshorn": return "lettuceleaves";
            case "bear": case "bearmale": case "bearcub": case "polarbear": return "corncob";
            case "hare": case "foxcub": case "arcticfox": case "arcticfoxcub": return "carrot";
            case "deer": case "deerstag": case "deercalf": case "deerred": return "berries";
            case "moose": case "moosebull": case "moosecalf": return "twigs";
            case "wildsow": case "wildboar": case "wildpiglet": return "mushroom";
            case "penguin": return "fish";
            case "horse": case "foal": case "zebra": return "hay";
            case "elephant": case "rhinoceros": return "leaves";
            case "snake": case "scorpion": case "spider": return "insect";
            case "earthworm": return "dirt";
            case "bandit": case "barbarian": case "dummy": return "bread";
            case "skeleton": case "ghoul": return "bone";
            default: return "";
        }
    }

    private void checkOwnership(Player player, Npc npc, Runnable onSuccess) {
        String npcName = npc.getDefinition().name.toLowerCase();
        String tableName = getOwnershipTable(npcName);
        if (tableName == null) {
            onSuccess.run();
            return;
        }

        try (ResultSet rs = dbManager.executeQuery("SELECT UserName FROM `" + tableName + "` WHERE " +
                npcName + "ID = '" + npc.getGlobalID() + "' AND PlayerUID = '" + player.getUID() + "'")) {
            if (rs.next()) {
                ui.updateInfoLabel(rs.getString("UserName") + " already owns this " + npc.getDefinition().name);
            } else {
                onSuccess.run();
            }
        } catch (SQLException e) {
            LOGGER.severe("Database error during ownership check: " + e.getMessage());
        }
    }

    private String getOwnershipTable(String npcName) {
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
}