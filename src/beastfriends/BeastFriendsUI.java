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
    private UIElement infoContainer; // Separate container for info label
    private UIElement buttonContainer; // Separate container for button label
    private UILabel infoLabel; // Top label for info
    private UILabel buttonLabel; // Bottom label for Feed and Close buttons
    private Npc currentTamingNpc;

    public BeastFriendsUI(BeastFriends plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.taming = new TamingManager(plugin, this, new DatabaseManager(plugin.getSQLiteConnection(plugin.getPath() + "/pets.db")));
        this.breeding = new BreedingManager(plugin, this, new DatabaseManager(plugin.getSQLiteConnection(plugin.getPath() + "/pets.db")));
        this.pets = new PetManager(plugin, new DatabaseManager(plugin.getSQLiteConnection(plugin.getPath() + "/pets.db")));
        plugin.registerEventListener(this);
    }

    // Getter for taming
    public TamingManager getTaming() {
        return taming;
    }

    // Getter for currentTamingNpc
    public Npc getCurrentTamingNpc() {
        return currentTamingNpc;
    }

    public void showTameUI(Npc npc) {
        // If taming another NPC and this is a different NPC, show message
        if (currentTamingNpc != null && currentTamingNpc.getGlobalID() != npc.getGlobalID()) {
            if (infoLabel != null) {
                infoLabel.setText("This is not the same NPC!");
            }
            return;
        }

        // Create info container if it doesn't exist
        if (infoContainer == null) {
            infoContainer = new UIElement();
            player.addUIElement(infoContainer);
            infoContainer.setSize(300, 80, false);
            infoContainer.setPosition(50, 10, true);
            infoContainer.setBorder(2);
            infoContainer.setBackgroundColor(0.1f, 0.1f, 0.1f, 0.9f);

            infoLabel = new UILabel();
            infoLabel.setText("Click 'Feed' to tame " + npc.getDefinition().name + "!");
            infoLabel.setFont(Font.Medieval);
            infoLabel.style.textAlign.set(TextAnchor.MiddleCenter);
            infoLabel.setFontColor(1.0f, 1.0f, 1.0f, 1.0f);
            infoLabel.setFontSize(16);
            infoLabel.setSize(280, 60, false);
            infoLabel.setPosition(10, 10, false);
            infoContainer.addChild(infoLabel);
        }

        // Create button container if it doesn't exist
        if (buttonContainer == null) {
            buttonContainer = new UIElement();
            player.addUIElement(buttonContainer);
            buttonContainer.setSize(300, 40, false);
            buttonContainer.setPosition(50, 20, true);
            buttonContainer.setBorder(2);
            buttonContainer.setBackgroundColor(0.1f, 0.1f, 0.1f, 0.9f);

            buttonLabel = new UILabel();
            buttonLabel.setText("<b>Feed</b>          <b>Close</b>");
            buttonLabel.setFont(Font.Medieval);
            buttonLabel.style.textAlign.set(TextAnchor.MiddleCenter);
            buttonLabel.setFontColor(0.0f, 0.8f, 0.0f, 1.0f);
            buttonLabel.setFontSize(16);
            buttonLabel.setSize(280, 20, false);
            buttonLabel.setPosition(10, 10, false);
            buttonLabel.setClickable(true);
            buttonContainer.addChild(buttonLabel);
        }

        infoContainer.setVisible(true);
        buttonContainer.setVisible(true);
        player.setMouseCursorVisible(true);
        this.currentTamingNpc = npc;
    }

    // Update the top info label text
    public void updateInfoLabel(String text) {
        if (infoLabel != null) {
            infoLabel.setText(text);
        }
    }

    // Close the entire taming UI
    public void closeTameUI() {
        if (infoContainer != null) {
            infoContainer.setVisible(false);
            infoContainer = null;
            infoLabel = null;
        }
        if (buttonContainer != null) {
            buttonContainer.setVisible(false);
            buttonContainer = null;
            buttonLabel = null;
        }
        player.setMouseCursorVisible(false);
        currentTamingNpc = null; // Reset taming NPC
    }

    // Hide the button container and mouse, keep info container visible
    public void hideButtonUI() {
        if (buttonContainer != null) {
            buttonContainer.setVisible(false);
            player.setMouseCursorVisible(false);
        }
    }

    public void showMyPetsMenu(Player player) {
        // Existing method unchanged
        UIElement myPetsMenu = new UIElement();
        player.addUIElement(myPetsMenu);
        myPetsMenu.setSize(300, 200, false);
        myPetsMenu.setPosition(50, 50, true);
        myPetsMenu.setBorder(2);
        myPetsMenu.setBackgroundColor(0.1f, 0.1f, 0.1f, 0.9f);

        UILabel titleLabel = new UILabel();
        titleLabel.setText("My Pets");
        titleLabel.setFont(Font.Medieval);
        titleLabel.style.textAlign.set(TextAnchor.MiddleCenter);
        titleLabel.setFontColor(1.0f, 1.0f, 1.0f, 1.0f);
        titleLabel.setFontSize(16);
        titleLabel.setSize(280, 40, false);
        titleLabel.setPosition(10, 10, false);
        myPetsMenu.addChild(titleLabel);

        UILabel petsLabel = new UILabel();
        petsLabel.setText("No pets yet...");
        petsLabel.setFont(Font.Medieval);
        petsLabel.style.textAlign.set(TextAnchor.MiddleCenter);
        petsLabel.setFontColor(1.0f, 1.0f, 1.0f, 1.0f);
        petsLabel.setFontSize(16);
        petsLabel.setSize(280, 100, false);
        petsLabel.setPosition(10, 60, false);
        myPetsMenu.addChild(petsLabel);

        UILabel closeButton = new UILabel();
        closeButton.setText("<b>Close</b>");
        closeButton.setFont(Font.Medieval);
        closeButton.style.textAlign.set(TextAnchor.MiddleCenter);
        closeButton.setFontColor(0.0f, 0.8f, 0.0f, 1.0f);
        closeButton.setFontSize(16);
        closeButton.setSize(100, 40, false);
        closeButton.setPosition(100, 170, false);
        closeButton.setClickable(true);
        closeButton.setBorder(2);
        closeButton.setBackgroundColor(0.2f, 0.2f, 0.2f, 0.9f);
        myPetsMenu.addChild(closeButton);

        myPetsMenu.setVisible(true);
        player.setMouseCursorVisible(true);
    }

    public void showPetManagementMenu(Npc npc) {
        // Existing method unchanged
        UIElement petManagementMenu = new UIElement();
        player.addUIElement(petManagementMenu);
        petManagementMenu.setSize(300, 250, false);
        petManagementMenu.setPosition(50, 50, true);
        petManagementMenu.setBorder(2);
        petManagementMenu.setBackgroundColor(0.1f, 0.1f, 0.1f, 0.9f);

        UILabel titleLabel = new UILabel();
        titleLabel.setText("Manage " + npc.getName());
        titleLabel.setFont(Font.Medieval);
        titleLabel.style.textAlign.set(TextAnchor.MiddleCenter);
        titleLabel.setFontColor(1.0f, 1.0f, 1.0f, 1.0f);
        titleLabel.setFontSize(16);
        titleLabel.setSize(280, 40, false);
        titleLabel.setPosition(10, 10, false);
        petManagementMenu.addChild(titleLabel);

        UILabel followButton = new UILabel();
        followButton.setText("<b>Follow Me</b>");
        followButton.setFont(Font.Medieval);
        followButton.style.textAlign.set(TextAnchor.MiddleCenter);
        followButton.setFontColor(0.0f, 0.8f, 0.0f, 1.0f);
        followButton.setFontSize(16);
        followButton.setSize(130, 40, false);
        followButton.setPosition(10, 60, false);
        followButton.setClickable(true);
        followButton.setBorder(2);
        followButton.setBackgroundColor(0.2f, 0.2f, 0.2f, 0.9f);
        petManagementMenu.addChild(followButton);

        UILabel standStillButton = new UILabel();
        standStillButton.setText("<b>Stand Still</b>");
        standStillButton.setFont(Font.Medieval);
        standStillButton.style.textAlign.set(TextAnchor.MiddleCenter);
        standStillButton.setFontColor(0.0f, 0.8f, 0.0f, 1.0f);
        standStillButton.setFontSize(16);
        standStillButton.setSize(130, 40, false);
        standStillButton.setPosition(160, 60, false);
        standStillButton.setClickable(true);
        standStillButton.setBorder(2);
        standStillButton.setBackgroundColor(0.2f, 0.2f, 0.2f, 0.9f);
        petManagementMenu.addChild(standStillButton);

        UILabel roamButton = new UILabel();
        roamButton.setText("<b>Roam</b>");
        roamButton.setFont(Font.Medieval);
        roamButton.style.textAlign.set(TextAnchor.MiddleCenter);
        roamButton.setFontColor(0.0f, 0.8f, 0.0f, 1.0f);
        roamButton.setFontSize(16);
        roamButton.setSize(130, 40, false);
        roamButton.setPosition(10, 110, false);
        roamButton.setClickable(true);
        roamButton.setBorder(2);
        roamButton.setBackgroundColor(0.2f, 0.2f, 0.2f, 0.9f);
        petManagementMenu.addChild(roamButton);

        UILabel breedButton = new UILabel();
        breedButton.setText("<b>Breed</b>");
        breedButton.setFont(Font.Medieval);
        breedButton.style.textAlign.set(TextAnchor.MiddleCenter);
        breedButton.setFontColor(0.0f, 0.8f, 0.0f, 1.0f);
        breedButton.setFontSize(16);
        breedButton.setSize(130, 40, false);
        breedButton.setPosition(160, 110, false);
        breedButton.setClickable(true);
        breedButton.setBorder(2);
        breedButton.setBackgroundColor(0.2f, 0.2f, 0.2f, 0.9f);
        petManagementMenu.addChild(breedButton);

        UILabel closeButton = new UILabel();
        closeButton.setText("<b>Close</b>");
        closeButton.setFont(Font.Medieval);
        closeButton.style.textAlign.set(TextAnchor.MiddleCenter);
        closeButton.setFontColor(0.0f, 0.8f, 0.0f, 1.0f);
        closeButton.setFontSize(16);
        closeButton.setSize(100, 40, false);
        closeButton.setPosition(100, 160, false);
        closeButton.setClickable(true);
        closeButton.setBorder(2);
        closeButton.setBackgroundColor(0.2f, 0.2f, 0.2f, 0.9f);
        petManagementMenu.addChild(closeButton);

        this.currentTamingNpc = npc;
        petManagementMenu.setVisible(true);
        player.setMouseCursorVisible(true);
    }

    public void closeAllMenus() {
        if (infoContainer != null) {
            infoContainer.setVisible(false);
            infoContainer = null;
            infoLabel = null;
        }
        if (buttonContainer != null) {
            buttonContainer.setVisible(false);
            buttonContainer = null;
            buttonLabel = null;
        }
        player.setMouseCursorVisible(false);
        currentTamingNpc = null;
    }

    @EventMethod
    public void onPlayerUIElementClickEvent(PlayerUIElementClickEvent event) {
        if (event.getPlayer() != player) return;
        UILabel clickedLabel = (UILabel) event.getUIElement();

        if (clickedLabel == buttonLabel) {
            String text = clickedLabel.getText().toLowerCase();
            if (text.contains("feed") && currentTamingNpc != null) {
                taming.attemptTame(player, currentTamingNpc);
                hideButtonUI(); // Hide button container and mouse on feed click
            } else if (text.contains("close")) {
                closeTameUI();
            }
        }
    }
}