package beastfriends;

import net.risingworld.api.Timer;
import net.risingworld.api.objects.Player;
import net.risingworld.api.objects.Npc;
import net.risingworld.api.objects.Item;
import net.risingworld.api.objects.Inventory;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Logger;
import net.risingworld.api.definitions.Npcs;




public class TamingManager {

    private static final Logger LOGGER = Logger.getLogger(TamingManager.class.getName());
    static int thisfeedcount = 0;
    private final BeastFriends plugin;
    private final BeastFriendsUI ui;
    private final DatabaseManager dbManager;
    private final HashMap<Player, Integer> playerPetFeed = new HashMap<>();
    private final HashMap<Player, Boolean> playerPetCalled = new HashMap<>();
    private final HashMap<Player, Timer> playerTameTimers = new HashMap<>();
    private final HashMap<Player, Integer> playerTameCountdowns = new HashMap<>();
    
    
    
    
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
            ui.closeTameMenu();
            startTamingProcess(player, npc, slotItem, slot);
        });
    }

    private void startTamingProcess(Player player, Npc npc, Item foodItem, int slot) {
        
        
        String npcName = npc.getDefinition().name.toLowerCase();
        int[] feedCount = {playerPetFeed.getOrDefault(player, 0)};
        ui.showTamingInfo(npcName, feedCount[0]);

        playerTameCountdowns.put(player, 60);
        final Timer tameTimer = new Timer(1.0f, 0.0f, 60, () -> {
            int secondsLeft = playerTameCountdowns.getOrDefault(player, 0);
            ui.updateTamingInfo(npcName, feedCount[0], secondsLeft);
            secondsLeft--;
            playerTameCountdowns.put(player, secondsLeft);
            if (secondsLeft <= 0) {
                ui.updateTamingInfo(npcName, 0, 0, "Taming failed - time expired!");
                resetTaming(player, npc);
            }
        });
        playerTameTimers.put(player, tameTimer);
        tameTimer.start();

        Runnable tameUpdate = () -> {
            npc.setLocked(false);
            npc.moveTo(player.getPosition());

            Random rand = new Random();
            if (rand.nextInt(20) == 5) {
                npc.setAlerted(true, 5);
                float offsetX = rand.nextFloat() * 10 - 5;
                float offsetZ = rand.nextFloat() * 10 - 5;
                npc.moveTo(player.getPosition().x + offsetX, player.getPosition().y, player.getPosition().z + offsetZ);
                ui.updateTamingInfo(npcName, feedCount[0], 0, "The " + npcName + " ran away!");
                resetTaming(player, npc);
                return;
            }

            player.getNpcInLineOfSight(4, (closeNpc) -> {
                if (closeNpc != null && closeNpc.getGlobalID() == npc.getGlobalID()) {
                    closeNpc.setLocked(true);

                    if (foodItem.getStack() > 1) {
                        foodItem.setStack(foodItem.getStack() - 1);
                        
                        thisfeedcount = thisfeedcount +1;
                        player.sendTextMessage("feedcount="+thisfeedcount);
                    } else {
                        player.getInventory().removeItem(slot, Inventory.SlotType.Quickslot);
                    }

                    feedCount[0]++;
                    playerPetFeed.put(player, feedCount[0]);
                    closeNpc.setLocked(false);

                    if (rand.nextInt(8) == 2) {
                        closeNpc.setAlerted(true, 5 - feedCount[0]);
                    }

                    ui.updateTamingInfo(npcName, feedCount[0], 60);
                    Timer oldTimer = playerTameTimers.get(player);
                    if (oldTimer != null) oldTimer.kill();
                    playerTameCountdowns.put(player, 60);
                    final Timer newTimer = new Timer(1.0f, 0.0f, 60, () -> {
                        int secondsLeft = playerTameCountdowns.getOrDefault(player, 0);
                        ui.updateTamingInfo(npcName, feedCount[0], secondsLeft);
                        secondsLeft--;
                        playerTameCountdowns.put(player, secondsLeft);
                        if (secondsLeft <= 0) {
                            ui.updateTamingInfo(npcName, 0, 0, "Taming failed - time expired!");
                            resetTaming(player, npc);
                        }
                    });
                    playerTameTimers.put(player, newTimer);
                    newTimer.start();

                    closeNpc.moveTo(player.getPosition().x + 2, player.getPosition().y, player.getPosition().z);
                    playerPetCalled.put(player, false);

                    if (thisfeedcount >= 5) {
                        thisfeedcount = 0;
                        LOGGER.info("[BeastFriends] Taming complete for " + npcName + " by " + player.getName());
                        playerPetFeed.put(player, 0);
                        ui.updateTamingInfo(npcName, 5, 0, "This " + npcName + " is now your pet!");
                        closeNpc.setLocked(true);
                        closeNpc.setBehaviour(Npcs.Behaviour.Default);
                        closeNpc.moveTo(player.getPosition().x + 1, player.getPosition().y, player.getPosition().z);
                        dbManager.registerPet(player, closeNpc, npcName);
                        ui.showMyPetsMenu(player);
                        Timer finalTimer = playerTameTimers.get(player);
                        if (finalTimer != null) finalTimer.kill();
                        playerTameTimers.remove(player);
                        playerTameCountdowns.remove(player);
                    }
                }
            });
        };
        tameTimer.setTask(tameUpdate);
    }

    private void resetTaming(Player player, Npc npc) {
        playerPetFeed.put(player, 0);
        playerPetCalled.put(player, false);
        npc.setLocked(false);
        ui.closeTamingInfo();
        Timer timer = playerTameTimers.get(player);
        if (timer != null) timer.kill();
        playerTameTimers.remove(player);
        playerTameCountdowns.remove(player);
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
        if (!dbManager.isOwner(player, npc)) {
            onSuccess.run();
        } else {
            ui.updateTameMenuText("This " + npc.getDefinition().name + " is already owned!");
        }
    }
}

