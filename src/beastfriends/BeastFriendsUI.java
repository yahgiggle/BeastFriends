package beastfriends;

import java.sql.SQLException;
import net.risingworld.api.Plugin;
import net.risingworld.api.assets.TextureAsset;
import net.risingworld.api.database.Database;
import net.risingworld.api.events.player.PlayerConnectEvent;
import net.risingworld.api.objects.Player;
import net.risingworld.api.ui.UIElement;
import net.risingworld.api.ui.UILabel;
import net.risingworld.api.ui.UITarget;
import net.risingworld.api.ui.UITextField;
import net.risingworld.api.ui.style.Font;
import net.risingworld.api.ui.style.ScaleMode;
import net.risingworld.api.ui.style.TextAnchor;


public class BeastFriendsUI extends BeastFriends{
    
    
   public BeastFriendsUI(Plugin plugin) {
        this.plugin = plugin;
      
    }

   
   
    public void BeastHUD(PlayerConnectEvent event) throws SQLException {   
        Player player = event.getPlayer();
        
        UIElement petsHUD = new UIElement();
        player.addUIElement(petsHUD);
        petsHUD.setSize(500, 40, false); 
        petsHUD.setPosition(65, 5, true); 
        petsHUD.setBorderEdgeRadius(5.0f, false);
        petsHUD.setBorder(1);
        petsHUD.setBorderColor(888);
        petsHUD.setBackgroundColor(0.1f, 0.1f, 0.1f, 0.7f); // Semi-transparent
        petsHUD.setVisible(true);

        UILabel InfoLabel = new UILabel();
        InfoLabel.setText("BeastFriends");
        InfoLabel.setFont(Font.Medieval);
        InfoLabel.style.textAlign.set(TextAnchor.MiddleCenter);
        InfoLabel.setFontColor(9.0f, 9.0f, 9.0f, 1.0f);
        InfoLabel.setFontSize(12);
        InfoLabel.setSize(500, 40, false);
        InfoLabel.setPosition(0, 0, false);
        petsHUD.addChild(InfoLabel);
        player.setAttribute("InfoLabel", InfoLabel);
        
    }

  
    public void BeastFeedMenu(PlayerConnectEvent event) throws SQLException {   
        Player player = event.getPlayer();
        
        UIElement FeedMenu = new UIElement();
        player.addUIElement(FeedMenu);
        FeedMenu.setSize(270, 100, false); 
        FeedMenu.setPosition(45, 45, true); 
        FeedMenu.setBorderEdgeRadius(5.0f, false);
        FeedMenu.setBorder(1);
        FeedMenu.setBorderColor(988);
        FeedMenu.setBackgroundColor(0.1f, 0.1f, 0.1f, 0.7f); // Semi-transparent
        FeedMenu.setVisible(false);
        player.setAttribute("FeedMenu", FeedMenu);
        
        
        UILabel FeedButton = new UILabel();
        FeedButton.setText("Feed");
        FeedButton.setFont(Font.Medieval);
        FeedButton.style.textAlign.set(TextAnchor.MiddleCenter);
        FeedButton.setFontColor(9.0f, 9.0f, 9.0f, 1.0f);
        FeedButton.setFontSize(12);
        FeedButton.setSize(250, 40, false);
        FeedButton.setPosition(10, 5, false);
        FeedButton.setBorder(3);
        FeedButton.setBackgroundColor(988);
        FeedButton.setClickable(true);
        FeedMenu.addChild(FeedButton);
        player.setAttribute("FeedButton", FeedButton);
        
        UILabel CancelFeedButton = new UILabel();
        CancelFeedButton.setText("Cancel");
        CancelFeedButton.setFont(Font.Medieval);
        CancelFeedButton.style.textAlign.set(TextAnchor.MiddleCenter);
        CancelFeedButton.setFontColor(9.0f, 9.0f, 9.0f, 1.0f);
        CancelFeedButton.setFontSize(12);
        CancelFeedButton.setSize(250, 40, false);
        CancelFeedButton.setPosition(10, 50, false);
        CancelFeedButton.setBorder(3);
        CancelFeedButton.setBackgroundColor(988);
        CancelFeedButton.setClickable(true);
        FeedMenu.addChild(CancelFeedButton);
        player.setAttribute("CancelFeedButton", CancelFeedButton);
        
        
        
    }
    
    
    public void BeastControlMenu(PlayerConnectEvent event) throws SQLException {   
        Player player = event.getPlayer();
        
        UIElement ControlMenu = new UIElement();
        player.addUIElement(ControlMenu);
        ControlMenu.setSize(270, 300, false); 
        ControlMenu.setPosition(45, 45, true); 
        ControlMenu.setBorderEdgeRadius(5.0f, false);
        ControlMenu.setBorder(1);
        ControlMenu.setBorderColor(988);
        ControlMenu.setBackgroundColor(0.1f, 0.1f, 0.1f, 0.7f); // Semi-transparent
        ControlMenu.setVisible(false);
        player.setAttribute("ControlMenu", ControlMenu);
        
        UILabel ControlCallButton = new UILabel();
        ControlCallButton.setText("Call");
        ControlCallButton.setFont(Font.Medieval);
        ControlCallButton.style.textAlign.set(TextAnchor.MiddleCenter);
        ControlCallButton.setFontColor(9.0f, 9.0f, 9.0f, 1.0f);
        ControlCallButton.setFontSize(12);
        ControlCallButton.setSize(250, 40, false);
        ControlCallButton.setPosition(10, 5, false);
        ControlCallButton.setBorder(3);
        ControlCallButton.setBackgroundColor(988);
        ControlCallButton.setClickable(true);
        ControlMenu.addChild(ControlCallButton);
        player.setAttribute("ControlCallButton", ControlCallButton);
        
        UILabel ControlAttackButton = new UILabel();
        ControlAttackButton.setText("Attack/guard");
        ControlAttackButton.setFont(Font.Medieval);
        ControlAttackButton.style.textAlign.set(TextAnchor.MiddleCenter);
        ControlAttackButton.setFontColor(9.0f, 9.0f, 9.0f, 1.0f);
        ControlAttackButton.setFontSize(12);
        ControlAttackButton.setSize(250, 40, false);
        ControlAttackButton.setPosition(10, 50, false);
        ControlAttackButton.setBorder(3);
        ControlAttackButton.setBackgroundColor(988);
        ControlAttackButton.setClickable(true);
        ControlMenu.addChild(ControlAttackButton);
        player.setAttribute("ControlAttackButton", ControlAttackButton);
        
      //  ControlGuardButton
        
        UILabel ControlGuardButton = new UILabel();
        ControlGuardButton.setText("Stand/guard");
        ControlGuardButton.setFont(Font.Medieval);
        ControlGuardButton.style.textAlign.set(TextAnchor.MiddleCenter);
        ControlGuardButton.setFontColor(9.0f, 9.0f, 9.0f, 1.0f);
        ControlGuardButton.setFontSize(12);
        ControlGuardButton.setSize(250, 40, false);
        ControlGuardButton.setPosition(10, 95, false);
        ControlGuardButton.setBorder(3);
        ControlGuardButton.setBackgroundColor(988);
        ControlGuardButton.setClickable(true);
        ControlMenu.addChild(ControlGuardButton);
        player.setAttribute("ControlGuardButton", ControlGuardButton);
        
        
        UILabel ControlbreedButton = new UILabel();
        ControlbreedButton.setText("breed");
        ControlbreedButton.setFont(Font.Medieval);
        ControlbreedButton.style.textAlign.set(TextAnchor.MiddleCenter);
        ControlbreedButton.setFontColor(9.0f, 9.0f, 9.0f, 1.0f);
        ControlbreedButton.setFontSize(12);
        ControlbreedButton.setSize(250, 40, false);
        ControlbreedButton.setPosition(10, 140, false);
        ControlbreedButton.setBorder(3);
        ControlbreedButton.setBackgroundColor(988);
        ControlbreedButton.setClickable(true);
        ControlMenu.addChild(ControlbreedButton);
        player.setAttribute("ControlbreedButton", ControlbreedButton);
        
        
        
         UILabel ControlReNameButton = new UILabel();
        ControlReNameButton.setText("ReName");
        ControlReNameButton.setFont(Font.Medieval);
        ControlReNameButton.style.textAlign.set(TextAnchor.MiddleCenter);
        ControlReNameButton.setFontColor(9.0f, 9.0f, 9.0f, 1.0f);
        ControlReNameButton.setFontSize(12);
        ControlReNameButton.setSize(250, 40, false);
        ControlReNameButton.setPosition(10, 185, false);
        ControlReNameButton.setBorder(3);
        ControlReNameButton.setBackgroundColor(988);
        ControlReNameButton.setClickable(true);
        ControlMenu.addChild(ControlReNameButton);
        player.setAttribute("ControlReNameButton", ControlReNameButton);
        
        
        UILabel CancelControlButton = new UILabel();
        CancelControlButton.setText("Cancel");
        CancelControlButton.setFont(Font.Medieval);
        CancelControlButton.style.textAlign.set(TextAnchor.MiddleCenter);
        CancelControlButton.setFontColor(9.0f, 9.0f, 9.0f, 1.0f);
        CancelControlButton.setFontSize(12);
        CancelControlButton.setSize(250, 40, false);
        CancelControlButton.setPosition(10, 230, false);
        CancelControlButton.setBorder(3);
        CancelControlButton.setBackgroundColor(988);
        CancelControlButton.setClickable(true);
        ControlMenu.addChild(CancelControlButton);
        player.setAttribute("CancelControlButton", CancelControlButton);
        
        
        
        
        
        
        
        // rename popup
        UIElement ControlReNameMenu = new UIElement();
        player.addUIElement(ControlReNameMenu);
        ControlReNameMenu.setSize(270, 140, false); 
        ControlReNameMenu.setPosition(45, 45, true); 
        ControlReNameMenu.setBorderEdgeRadius(5.0f, false);
        ControlReNameMenu.setBorder(1);
        ControlReNameMenu.setBorderColor(988);
        ControlReNameMenu.setBackgroundColor(0.1f, 0.1f, 0.1f, 0.7f); // Semi-transparent
        ControlReNameMenu.setVisible(false);
        player.setAttribute("ControlReNameMenu", ControlReNameMenu);
        
        
          //ControlReNameTextField
          UITextField ControlReNameTextField = new UITextField();
          ControlReNameTextField.setClickable(true);
          ControlReNameTextField.setMaxCharacters(36);
          ControlReNameTextField.setText("");
          ControlReNameTextField.setBorderColor(143);
          ControlReNameTextField.setSize(250, 30, false);
          ControlReNameTextField.setPosition(10, 5, false); 
          ControlReNameMenu.addChild((UIElement)ControlReNameTextField);
          ControlReNameTextField.getCurrentText(player, (String ControlReNameText) -> {
          player.setAttribute("ExitPortalNameText", ControlReNameText); 
          });      
          player.setAttribute("ControlReNameTextField", ControlReNameTextField);
          
        //  player.addUIElement((UIElement)ExitPortalNameTextField);
       
        UILabel ReNameButton = new UILabel();
        ReNameButton.setText("Set Name");
        ReNameButton.setFont(Font.Medieval);
        ReNameButton.style.textAlign.set(TextAnchor.MiddleCenter);
        ReNameButton.setFontColor(9.0f, 9.0f, 9.0f, 1.0f);
        ReNameButton.setFontSize(12);
        ReNameButton.setSize(250, 40, false);
        ReNameButton.setPosition(10, 40, false);
        ReNameButton.setBorder(3);
        ReNameButton.setBackgroundColor(988);
        ReNameButton.setClickable(true);
        ControlReNameMenu.addChild(ReNameButton);
        player.setAttribute("ReNameButton", ReNameButton);
        
        UILabel CancelReNameButton = new UILabel();
        CancelReNameButton.setText("Cancel");
        CancelReNameButton.setFont(Font.Medieval);
        CancelReNameButton.style.textAlign.set(TextAnchor.MiddleCenter);
        CancelReNameButton.setFontColor(9.0f, 9.0f, 9.0f, 1.0f);
        CancelReNameButton.setFontSize(12);
        CancelReNameButton.setSize(250, 40, false);
        CancelReNameButton.setPosition(10, 85, false);
        CancelReNameButton.setBorder(3);
        CancelReNameButton.setBackgroundColor(988);
        CancelReNameButton.setClickable(true);
        ControlReNameMenu.addChild(CancelReNameButton);
        player.setAttribute("CancelReNameButton", CancelReNameButton);
        
        
        
        
        
        
        
        
        
        
        
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
     
    
} 

