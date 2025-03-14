package beastfriends;

import net.risingworld.api.Timer;
import net.risingworld.api.objects.Player;
import net.risingworld.api.objects.Npc;
import net.risingworld.api.ui.UIElement;
import net.risingworld.api.ui.UILabel;
import net.risingworld.api.ui.style.Font;
import net.risingworld.api.ui.style.TextAnchor;
import net.risingworld.api.utils.Vector3f;
import net.risingworld.api.utils.Quaternion;
import net.risingworld.api.World;
import net.risingworld.api.definitions.Npcs;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Logger;

public class BreedingManager {

    private static final Logger LOGGER = Logger.getLogger(BreedingManager.class.getName());

    private final BeastFriends plugin;
    private final BeastFriendsUI ui;
    private final DatabaseManager dbManager;
    private final HashMap<Player, Npc> playerBreedingSelection = new HashMap<>();
    private final HashMap<Player, UIElement> playerBreedingPopups = new HashMap<>();
    private final HashMap<Player, UILabel> playerBreedingLabels = new HashMap<>();
    private final HashMap<Player, Boolean> playerBreedingInProgress = new HashMap<>();
    private final HashMap<Player, Timer> playerBreedTimers = new HashMap<>();
    private static Timer breedTimer = null;
    
    
    
    public BreedingManager(BeastFriends plugin, BeastFriendsUI ui, DatabaseManager dbManager) {
        this.plugin = plugin;
        this.ui = ui;
        this.dbManager = dbManager;
    }

    public void attemptBreed(Player player, Npc firstNpc) {
        String npcType = firstNpc.getDefinition().name.toLowerCase();
        String npcName = firstNpc.getDefinition().name;

        if (playerBreedingInProgress.getOrDefault(player, false)) {
            player.sendTextMessage("Please wait until the current breeding attempt finishes!");
            return;
        }

        UIElement breedingPopup = playerBreedingPopups.computeIfAbsent(player, k -> {
            UIElement newPopup = new UIElement();
            player.addUIElement(newPopup);
            newPopup.setSize(300, 100, false);
            newPopup.setPosition(50, 10, true);
            newPopup.setBorder(2);
            newPopup.setBackgroundColor(0.1f, 0.1f, 0.1f, 0.9f);
            newPopup.setVisible(true);
            return newPopup;
        });

        UILabel breedingLabel = playerBreedingLabels.computeIfAbsent(player, k -> {
            UILabel newLabel = new UILabel();
            newLabel.setFont(Font.Medieval);
            newLabel.style.textAlign.set(TextAnchor.MiddleCenter);
            newLabel.setFontColor(1.0f, 1.0f, 1.0f, 1.0f);
            newLabel.setFontSize(16);
            newLabel.setSize(280, 80, false);
            newLabel.setPosition(10, 10, false);
            breedingPopup.addChild(newLabel);
            return newLabel;
        });

        if (!dbManager.isOwner(player, firstNpc)) {
            breedingLabel.setText("You don’t own this pet!");
            closePopupAfterDelay(player, breedingPopup);
            return;
        }

        if (playerBreedingSelection.containsKey(player)) {
            Npc secondNpc = firstNpc;
            Npc selectedFirstNpc = playerBreedingSelection.get(player);
            playerBreedingSelection.remove(player);

            if (!dbManager.isOwner(player, secondNpc)) {
                breedingLabel.setText("You don’t own the second pet!");
                closePopupAfterDelay(player, breedingPopup);
                return;
            }

            if (!secondNpc.getDefinition().name.toLowerCase().equals(npcType)) {
                breedingLabel.setText("Both pets must be the same type to breed!");
                closePopupAfterDelay(player, breedingPopup);
                return;
            }

            startBreeding(player, selectedFirstNpc, secondNpc, npcType, npcName, breedingPopup, breedingLabel);
        } else {
            playerBreedingSelection.put(player, firstNpc);
            breedingLabel.setText("Select another " + npcName + " to breed with!");
        }
    }

    private void startBreeding(Player player, Npc firstNpc, Npc secondNpc, String npcType, String npcName, UIElement breedingPopup, UILabel breedingLabel) {
        float firstStartX = firstNpc.getPosition().x;
        float firstStartY = firstNpc.getPosition().y;
        float firstStartZ = firstNpc.getPosition().z;
        Vector3f targetPosition = secondNpc.getPosition();
       
        firstNpc.setLocked(false);
        secondNpc.setBehaviour(Npcs.Behaviour.Default);
        secondNpc.setLocked(true);
        firstNpc.moveTo(targetPosition);
    
        breedingLabel.setText(npcName + " selected\nMoving to partner...");
        LOGGER.info("[BeastFriends] Moving first " + npcType + " to second " + npcType + " for " + player.getName());

        playerBreedingInProgress.put(player, true);

        final int[] tickCount = {0};
        final boolean[] breedingStarted = {false};
        final boolean[] breedingFailed = {false};
        breedTimer = new Timer(0.5f, 0.0f, -1, new Runnable() {
            @Override
            public void run() {
                tickCount[0]++;
                
                // Phase 1: Moving (0-30s)
                if (tickCount[0] <= 60 && !breedingStarted[0]) { // 30 seconds at 0.5s ticks = 60
                    Vector3f currentPosition = firstNpc.getPosition();
                    float distance = currentPosition.distance(targetPosition);
                    if (distance < 4.0f) {
                        firstNpc.setBehaviour(Npcs.Behaviour.Default);
                        firstNpc.setLocked(true);
                        breedingLabel.setText(npcName + "s now breeding...");
                        LOGGER.info("[BeastFriends] First " + npcType + " arrived at distance: " + distance);
                        breedingStarted[0] = true;
                    } else if (tickCount[0] >= 60) {
                        breedingLabel.setText("Failed to reach partner!\nBreeding canceled.");
                        LOGGER.warning("[BeastFriends] First " + npcType + " failed to reach second " + npcType + " within 30s");
                        firstNpc.setBehaviour(Npcs.Behaviour.Default);
                        firstNpc.setLocked(true);
                        secondNpc.setBehaviour(Npcs.Behaviour.Default);
                        secondNpc.setLocked(true);
                        cleanupBreeding(player, firstNpc, secondNpc, breedingPopup, breedTimer);
                    }
                }
                // Phase 2: Breeding (30-60s)
                else if (tickCount[0] > 60 && tickCount[0] <= 120 && breedingStarted[0]) { // 60s = 120 ticks
                    if (tickCount[0] >= 120) {
                        Random rand = new Random();
                        if (rand.nextBoolean()) {
                            short npcID = getNpcID(npcType);
                            Vector3f spawnPosition = new Vector3f(firstNpc.getPosition().x, firstNpc.getPosition().y, firstNpc.getPosition().z);
                            Npc babyNpc = World.spawnNpc(npcID, spawnPosition, Quaternion.IDENTITY);
                            if (babyNpc != null) {
                                LOGGER.info("[BeastFriends] Baby " + npcType + " spawned with ID: " + babyNpc.getGlobalID());
                                dbManager.registerPet(player, babyNpc, npcType);
                                breedingLabel.setText("Breeding successful!\nA new " + npcType + " has been born!");
                            } else {
                                LOGGER.severe("Failed to spawn baby " + npcType);
                                breedingLabel.setText("Breeding failed!\nUnable to spawn baby!");
                            }
                        } else {
                            breedingLabel.setText("Breeding failed!\nThe first pet returns to its spot.");
                            firstNpc.setLocked(false);
                            firstNpc.moveTo(firstStartX, firstStartY, firstStartZ);
                            breedingFailed[0] = true;
                        }
                    }
                }
                // Phase 3: Returning (60-90s, failure only)
                else if (tickCount[0] > 120 && tickCount[0] <= 180 && breedingFailed[0]) { // 90s = 180 ticks
                    float returnDistance = firstNpc.getPosition().distance(firstStartX, firstStartY, firstStartZ);
                    if (returnDistance < 1.0f) {
                        firstNpc.setBehaviour(Npcs.Behaviour.Default);
                        firstNpc.setLocked(true);
                        secondNpc.setBehaviour(Npcs.Behaviour.Default);
                        secondNpc.setLocked(true);
                        cleanupBreeding(player, firstNpc, secondNpc, breedingPopup, breedTimer);
                    } else if (tickCount[0] >= 180) {
                        LOGGER.warning("[BeastFriends] First " + npcType + " failed to return within 30s. Forcing lock.");
                        firstNpc.setBehaviour(Npcs.Behaviour.Default);
                        firstNpc.setLocked(true);
                        secondNpc.setBehaviour(Npcs.Behaviour.Default);
                        secondNpc.setLocked(true);
                        cleanupBreeding(player, firstNpc, secondNpc, breedingPopup, breedTimer);
                    }
                }
                // Phase 4: Cleanup (after 5s delay)
                else if (tickCount[0] > (breedingFailed[0] ? 180 : 120) + 10) { // +5s = 10 ticks
                    cleanupBreeding(player, firstNpc, secondNpc, breedingPopup, breedTimer);
                }
            }
        });

        breedTimer.start();
        playerBreedTimers.put(player, breedTimer);
    }

    private void cleanupBreeding(Player player, Npc firstNpc, Npc secondNpc, UIElement breedingPopup, Timer breedTimer) {
        firstNpc.setLocked(false);
        secondNpc.setLocked(false);
        breedingPopup.setVisible(false);
        player.removeUIElement(breedingPopup);
        playerBreedingPopups.remove(player);
        playerBreedingLabels.remove(player);
        playerBreedingInProgress.put(player, false);
        if (breedTimer != null) {
            breedTimer.kill();
            playerBreedTimers.remove(player);
        }
        LOGGER.info("[BeastFriends] Breeding process cleaned up for " + player.getName());
    }

    private void closePopupAfterDelay(Player player, UIElement breedingPopup) {
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                breedingPopup.setVisible(false);
                player.removeUIElement(breedingPopup);
                playerBreedingPopups.remove(player);
                playerBreedingLabels.remove(player);
                playerBreedingSelection.remove(player);
            } catch (InterruptedException e) {
                LOGGER.severe("Error popup interrupted during ownership check: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private short getNpcID(String npcType) {
        switch (npcType.toLowerCase()) {
            case "pig": return 1;
            case "piglet": return 3;
            case "cow": return 5;
            case "bull": return 6;
            case "calf": return 7;
            case "sheep": return 10;
            case "ram": return 11;
            case "lamb": return 12;
            case "sheepshorn": return 13;
            case "ramshorn": return 14;
            case "goat": return 20;
            case "billygoat": return 21;
            case "goatling": return 22;
            case "chicken": return 25;
            case "chick": return 27;
            case "hare": return 30;
            case "earthworm": return 35;
            case "snake": return 40;
            case "scorpion": return 43;
            case "spider": return 45;
            case "deer": return 50;
            case "deerstag": return 51;
            case "deercalf": return 52;
            case "deerred": return 55;
            case "fox": return 60;
            case "foxcub": return 62;
            case "arcticfox": return 65;
            case "arcticfoxcub": return 67;
            case "moose": return 70;
            case "moosebull": return 71;
            case "moosecalf": return 72;
            case "wildsow": return 75;
            case "wildboar": return 76;
            case "wildpiglet": return 77;
            case "bear": return 80;
            case "bearmale": return 81;
            case "bearcub": return 82;
            case "polarbear": return 85;
            case "penguin": return 90;
            case "horse": return 100;
            case "foal": return 102;
            case "zebra": return 120;
            case "elephant": return 125;
            case "rhinoceros": return 130;
            case "lion": return 135;
            case "lioness": return 136;
            case "wolf": return 150;
            case "shewolf": return 151;
            case "wolfcub": return 152;
            case "arcticwolf": return 155;
            case "arcticshewolf": return 156;
            case "firewolf": return 160;
            case "dummy": return 200;
            case "bandit": return 210;
            case "barbarian": return 215;
            case "skeleton": return 220;
            case "ghoul": return 230;
            default:
                LOGGER.warning("[BeastFriends] Unknown NPC type: " + npcType + ", defaulting to Pig (1)");
                return 1;
        }
    }
}

