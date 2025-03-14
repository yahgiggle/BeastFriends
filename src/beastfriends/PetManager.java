package beastfriends;

import net.risingworld.api.Timer;
import net.risingworld.api.objects.Player;
import net.risingworld.api.objects.Npc;
import net.risingworld.api.definitions.Npcs; // Added this import
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Logger;

public class PetManager {

    private static final Logger LOGGER = Logger.getLogger(PetManager.class.getName());

    private final BeastFriends plugin;
    private final DatabaseManager dbManager;
    private final HashMap<Player, Timer> playerPetTimers = new HashMap<>();

    public PetManager(BeastFriends plugin, DatabaseManager dbManager) {
        this.plugin = plugin;
        this.dbManager = dbManager;
    }

    public void makePetFollow(Player player, Npc npc) {
        if (!dbManager.isOwner(player, npc)) {
            player.sendTextMessage("You don’t own this pet!");
            return;
        }
        npc.setLocked(false);
        Timer oldTimer = playerPetTimers.get(player);
        if (oldTimer != null) oldTimer.kill();
        
        final Timer followTimer = new Timer(1.0f, 0.0f, -1, () -> {
            npc.moveTo(player.getPosition().x + 1, player.getPosition().y, player.getPosition().z);
        });
        followTimer.start();
        playerPetTimers.put(player, followTimer);
        LOGGER.info("[BeastFriends] Pet " + npc.getName() + " following " + player.getName());
    }

    public void makePetStandStill(Player player, Npc npc) {
        if (!dbManager.isOwner(player, npc)) {
            player.sendTextMessage("You don’t own this pet!");
            return;
        }
        Timer oldTimer = playerPetTimers.get(player);
        if (oldTimer != null) oldTimer.kill();
        npc.setBehaviour(Npcs.Behaviour.Default);
        npc.setLocked(true);
        playerPetTimers.remove(player);
        LOGGER.info("[BeastFriends] Pet " + npc.getName() + " standing still for " + player.getName());
    }

    public void makePetRoam(Player player, Npc npc) {
        if (!dbManager.isOwner(player, npc)) {
            player.sendTextMessage("You don’t own this pet!");
            return;
        }
        Timer oldTimer = playerPetTimers.get(player);
        if (oldTimer != null) oldTimer.kill();
        npc.setLocked(false);
        
        final Timer roamTimer = new Timer(2.0f, 0.0f, -1, () -> {
            Random rand = new Random();
            float offsetX = rand.nextFloat() * 10 - 5;
            float offsetZ = rand.nextFloat() * 10 - 5;
            npc.moveTo(npc.getPosition().x + offsetX, npc.getPosition().y, npc.getPosition().z + offsetZ);
        });
        roamTimer.start();
        playerPetTimers.put(player, roamTimer);
        LOGGER.info("[BeastFriends] Pet " + npc.getName() + " roaming for " + player.getName());
    }

    public void stopPetTimers(Player player) {
        Timer timer = playerPetTimers.get(player);
        if (timer != null) {
            timer.kill();
            playerPetTimers.remove(player);
        }
    }
}