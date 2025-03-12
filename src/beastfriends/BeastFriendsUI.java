package beastfriends;


import net.risingworld.api.Plugin;
import net.risingworld.api.objects.Player;
import net.risingworld.api.objects.Npc;
import net.risingworld.api.ui.UIElement;
import net.risingworld.api.ui.UILabel;
import net.risingworld.api.ui.style.Font;
import net.risingworld.api.ui.style.TextAnchor;
import net.risingworld.api.events.Listener;
import net.risingworld.api.events.EventMethod;
import net.risingworld.api.events.player.ui.PlayerUIElementClickEvent;

public class BeastFriendsUI  extends Plugin implements Listener{

    static {
        System.out.println("[BeastFriends] BeastFriendsUI class loaded by JVM.");
    }
    
    
    private final BeastFriends plugin;
    private final Player player;
    private UIElement tameMenu;
    private UIElement myPetsMenu;
    private UILabel tameStatusLabel;
    private UILabel feedButton;
    private UILabel closeButton;
    private Npc currentTamingNpc;

    public BeastFriendsUI(BeastFriends plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
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
        player.setAttribute("tameFeedButton", feedButton);
        this.currentTamingNpc = npc;

        tameMenu.setVisible(true);
        player.setMouseCursorVisible(true);
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

    public void closeAllMenus() {
        if (tameMenu != null) tameMenu.setVisible(false);
        if (myPetsMenu != null) myPetsMenu.setVisible(false);
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
            plugin.attemptTame(player, currentTamingNpc, this);
        } else if (clickedLabel == closeButton) {
            myPetsMenu.setVisible(false);
            player.setMouseCursorVisible(false);
        }
    }

    @Override
    public void onEnable() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void onDisable() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}