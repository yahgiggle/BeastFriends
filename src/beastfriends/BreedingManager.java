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
        Timer breedTimer = new Timer(0.5f, 0.0f, -1, () -> {
            tickCount[0]++;

            if (tickCount[0] <= 60 && !breedingStarted[0]) {
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
                    cleanupBreeding(player, firstNpc, secondNpc, breedingPopup);
                }
            } else if (tickCount[0] > 60 && tickCount[0] <= 120 && breedingStarted[0]) {
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
            } else if (tickCount[0] > 120 && tickCount[0] <= 180 && breedingFailed[0]) {
                float returnDistance = firstNpc.getPosition().distance(firstStartX, firstStartY, firstStartZ);
                if (returnDistance < 1.0f || tickCount[0] >= 180) {
                    cleanupBreeding(player, firstNpc, secondNpc, breedingPopup);
                }
            } else if (tickCount[0] > (breedingFailed[0] ? 180 : 120) + 10) {
                cleanupBreeding(player, firstNpc, secondNpc, breedingPopup);
            }
        });

        breedTimer.start();
        playerBreedTimers.put(player, breedTimer);
    }

    private void cleanupBreeding(Player player, Npc firstNpc, Npc secondNpc, UIElement breedingPopup) {
        firstNpc.setLocked(false);
        secondNpc.setLocked(false);
        breedingPopup.setVisible(false);
        player.removeUIElement(breedingPopup);
        playerBreedingPopups.remove(player);
        playerBreedingLabels.remove(player);
        playerBreedingInProgress.put(player, false);
        Timer timer = playerBreedTimers.remove(player);
        if (timer != null) timer.kill();
        LOGGER.info("[BeastFriends] Breeding process cleaned up for " + player.getName());
    }

    private void closePopupAfterDelay(Player player, UIElement breedingPopup) {
        new Timer(3.0f, 0.0f, 1, () -> {
            breedingPopup.setVisible(false);
            player.removeUIElement(breedingPopup);
            playerBreedingPopups.remove(player);
            playerBreedingLabels.remove(player);
            playerBreedingSelection.remove(player);
        }).start();
    }

    private short getNpcID(String npcType) {
        switch (npcType.toLowerCase()) {
            case "pig": return 1;
            case "cow": return 5;
            case "sheep": return 10;
            case "goat": return 20;
            case "chicken": return 25;
            case "hare": return 30;
            case "deer": return 50;
            case "fox": return 60;
            case "moose": return 70;
            case "wildsow": return 75;
            case "bear": return 80;
            case "polarbear": return 85;
            case "penguin": return 90;
            case "horse": return 100;
            case "zebra": return 120;
            case "elephant": return 125;
            case "rhinoceros": return 130;
            case "lion": return 135;
            case "wolf": return 150;
            default:
                LOGGER.warning("[BeastFriends] Unknown NPC type: " + npcType + ", defaulting to Pig (1)");
                return 1;
        }
    }
}