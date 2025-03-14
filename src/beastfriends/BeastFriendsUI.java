package beastfriends;

import net.risingworld.api.objects.Player;
import net.risingworld.api.objects.Npc;
import net.risingworld.api.ui.UIElement;
import net.risingworld.api.ui.UILabel;
import net.risingworld.api.ui.style.Font;
import net.risingworld.api.ui.style.TextAnchor;
import net.risingworld.api.events.Listener;
import net.risingworld.api.events.EventMethod;
import net.risingworld.api.events.player.ui.PlayerUIElementClickEvent;

public class BeastFriendsUI implements Listener {

    static {
        System.out.println("[BeastFriends] BeastFriendsUI class loaded by JVM.");
    }

    private final BeastFriends plugin;
    private final Player player;
    private final TamingManager taming;
    private final BreedingManager breeding;
    private final PetManager pets;
    private UIElement tameMenu;
    private UIElement myPetsMenu;
    private UIElement tamingInfo;
    private UIElement petManagementMenu;
    private UILabel tameStatusLabel;
    private UILabel tamingInfoLabel;
    private UILabel feedButton;
    private UILabel closeButton;
    private UILabel followButton;
    private UILabel standStillButton;
    private UILabel roamButton;
    private UILabel breedButton;
    private Npc currentTamingNpc;
    private Npc currentManagedNpc;

    public BeastFriendsUI(BeastFriends plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.taming = new TamingManager(plugin, this, new DatabaseManager(plugin.getSQLiteConnection(plugin.getPath() + "/pets.db")));
        this.breeding = new BreedingManager(plugin, this, new DatabaseManager(plugin.getSQLiteConnection(plugin.getPath() + "/pets.db")));
        this.pets = new PetManager(plugin, new DatabaseManager(plugin.getSQLiteConnection(plugin.getPath() + "/pets.db")));
        plugin.registerEventListener(this);
    }

    public void showTameMenu(Npc npc) {
        if (tameMenu != null) tameMenu.setVisible(false);

        tameMenu = new UIElement();
        player.addUIElement(tameMenu);
        tameMenu.setSize(300, 200, false);
        tameMenu.setPosition(50, 50, true);
        tameMenu.setBorder(2);
        tameMenu.setBackgroundColor(0.1f, 0.1f, 0.1f, 0.9f);

        createLabel(tameMenu, 10, 10, "Tame " + npc.getDefinition().name, 280, 40);
        tameStatusLabel = createLabel(tameMenu, 10, 60, "Click 'Feed' to tame!", 280, 40);
        feedButton = createButton(tameMenu, 100, 120, "Feed", 100, 40);
        closeButton = createButton(tameMenu, 100, 160, "Close", 100, 40);
        player.setAttribute("tameFeedButton", feedButton);
        this.currentTamingNpc = npc;

        tameMenu.setVisible(true);
        player.setMouseCursorVisible(true);
    }

    public void closeTameMenu() {
        if (tameMenu != null) {
            tameMenu.setVisible(false);
            player.setMouseCursorVisible(false);
        }
    }

    public void showTamingInfo(String npcName, int feedCount) {
        if (tamingInfo != null) tamingInfo.setVisible(false);

        tamingInfo = new UIElement();
        player.addUIElement(tamingInfo);
        tamingInfo.setSize(300, 100, false);
        tamingInfo.setPosition(50, 10, true);
        tamingInfo.setBorder(2);
        tamingInfo.setBackgroundColor(0.1f, 0.1f, 0.1f, 0.9f);

        tamingInfoLabel = createLabel(tamingInfo, 10, 10, npcName.substring(0, 1).toUpperCase() + npcName.substring(1) + 
                " Called Feed " + feedCount + "/5 times\nFeed again within 60s!", 280, 80);
        tamingInfo.setVisible(true);
    }

    public void updateTamingInfo(String npcName, int feedCount, int secondsLeft) {
        updateTamingInfo(npcName, feedCount, secondsLeft, "Feed again within " + secondsLeft + "s!");
    }

    public void updateTamingInfo(String npcName, int feedCount, int secondsLeft, String message) {
        if (tamingInfoLabel != null) {
            tamingInfoLabel.setText(npcName.substring(0, 1).toUpperCase() + npcName.substring(1) + 
                    " Called Feed " + feedCount + "/5 times\n" + message);
        }
    }

    public void closeTamingInfo() {
        if (tamingInfo != null) {
            tamingInfo.setVisible(false);
        }
    }

    public void showMyPetsMenu(Player player) {
        if (myPetsMenu != null) myPetsMenu.setVisible(false);

        myPetsMenu = new UIElement();
        player.addUIElement(myPetsMenu);
        myPetsMenu.setSize(300, 200, false);
        myPetsMenu.setPosition(50, 50, true);
        myPetsMenu.setBorder(2);
        myPetsMenu.setBackgroundColor(0.1f, 0.1f, 0.1f, 0.9f);

        createLabel(myPetsMenu, 10, 10, "My Pets", 280, 40);
        createLabel(myPetsMenu, 10, 60, "No pets yet...", 280, 100);
        closeButton = createButton(myPetsMenu, 100, 170, "Close", 100, 40);

        myPetsMenu.setVisible(true);
        player.setMouseCursorVisible(true);
    }

    public void showPetManagementMenu(Npc npc) {
        if (petManagementMenu != null) petManagementMenu.setVisible(false);

        petManagementMenu = new UIElement();
        player.addUIElement(petManagementMenu);
        petManagementMenu.setSize(300, 250, false);
        petManagementMenu.setPosition(50, 50, true);
        petManagementMenu.setBorder(2);
        petManagementMenu.setBackgroundColor(0.1f, 0.1f, 0.1f, 0.9f);

        createLabel(petManagementMenu, 10, 10, "Manage " + npc.getName(), 280, 40);
        followButton = createButton(petManagementMenu, 10, 60, "Follow Me", 130, 40);
        standStillButton = createButton(petManagementMenu, 160, 60, "Stand Still", 130, 40);
        roamButton = createButton(petManagementMenu, 10, 110, "Roam", 130, 40);
        breedButton = createButton(petManagementMenu, 160, 110, "Breed", 130, 40);
        closeButton = createButton(petManagementMenu, 100, 160, "Close", 100, 40);

        this.currentManagedNpc = npc;
        petManagementMenu.setVisible(true);
        player.setMouseCursorVisible(true);
    }

    public void closeAllMenus() {
        if (tameMenu != null) tameMenu.setVisible(false);
        if (myPetsMenu != null) myPetsMenu.setVisible(false);
        if (tamingInfo != null) tamingInfo.setVisible(false);
        if (petManagementMenu != null) petManagementMenu.setVisible(false);
        player.setMouseCursorVisible(false);
    }

    public void updateTameMenuText(String text) {
        if (tameStatusLabel != null) {
            tameStatusLabel.setText(text);
        }
    }

    private UILabel createLabel(UIElement parent, int x, int y, String text, int width, int height) {
        UILabel label = new UILabel();
        label.setText(text);
        label.setFont(Font.Medieval);
        label.style.textAlign.set(TextAnchor.MiddleCenter);
        label.setFontColor(1.0f, 1.0f, 1.0f, 1.0f);
        label.setFontSize(16);
        label.setSize(width, height, false);
        label.setPosition(x, y, false);
        parent.addChild(label);
        return label;
    }

    private UILabel createButton(UIElement parent, int x, int y, String text, int width, int height) {
        UILabel button = new UILabel();
        button.setClickable(true);
        button.setText("<b>" + text + "</b>");
        button.setFont(Font.Medieval);
        button.style.textAlign.set(TextAnchor.MiddleCenter);
        button.setFontColor(0.0f, 0.8f, 0.0f, 1.0f);
        button.setFontSize(16);
        button.setSize(width, height, false);
        button.setBorder(2);
        button.setBackgroundColor(0.2f, 0.2f, 0.2f, 0.9f);
        button.setPosition(x, y, false);
        parent.addChild(button);
        return button;
    }

    @EventMethod
    public void onPlayerUIElementClickEvent(PlayerUIElementClickEvent event) {
        if (event.getPlayer() != player) return;
        UILabel clickedLabel = (UILabel) event.getUIElement();

        if (clickedLabel == feedButton && currentTamingNpc != null) {
            taming.attemptTame(player, currentTamingNpc);
        } else if (clickedLabel == closeButton) {
            if (tameMenu != null) tameMenu.setVisible(false);
            if (myPetsMenu != null) myPetsMenu.setVisible(false);
            if (petManagementMenu != null) petManagementMenu.setVisible(false);
            player.setMouseCursorVisible(false);
        } else if (clickedLabel == followButton && currentManagedNpc != null) {
            plugin.makePetFollow(player, currentManagedNpc);
            petManagementMenu.setVisible(false);
            player.setMouseCursorVisible(false);
        } else if (clickedLabel == standStillButton && currentManagedNpc != null) {
            plugin.makePetStandStill(player, currentManagedNpc);
            petManagementMenu.setVisible(false);
            player.setMouseCursorVisible(false);
        } else if (clickedLabel == roamButton && currentManagedNpc != null) {
            plugin.makePetRoam(player, currentManagedNpc);
            petManagementMenu.setVisible(false);
            player.setMouseCursorVisible(false);
        } else if (clickedLabel == breedButton && currentManagedNpc != null) {
            breeding.attemptBreed(player, currentManagedNpc);
            petManagementMenu.setVisible(false);
            player.setMouseCursorVisible(false);
        }
    }
}