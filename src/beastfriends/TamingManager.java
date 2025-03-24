package beastfriends;

import java.util.Random;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.risingworld.api.Plugin;
import net.risingworld.api.Timer;
import net.risingworld.api.events.player.ui.PlayerUIElementClickEvent;
import net.risingworld.api.objects.Npc;
import net.risingworld.api.objects.Player;
import net.risingworld.api.ui.UIElement;
import net.risingworld.api.ui.UILabel;
import net.risingworld.api.utils.Vector3f;
import net.risingworld.api.objects.Inventory;
import net.risingworld.api.objects.Item;
import net.risingworld.api.definitions.Npcs;

    




public class TamingManager extends BeastFriends{
    
    
     
     

   public TamingManager(Plugin plugin) {
        this.plugin = plugin;
        
    }
   
    
    public void TamingNPC(Player player) {
    UILabel InfoLabel = (UILabel)player.getAttribute("InfoLabel");
    UIElement FeedMenu = (UIElement)player.getAttribute("FeedMenu");
    UIElement ControlMenu = (UIElement)player.getAttribute("ControlMenu");
    
    
    player.getNpcInLineOfSight(50, (npc) -> {
     
    // if breeding just rin this code 
    if (Boolean.TRUE.equals(player.getAttribute("SetNpctoBreed"))) {    
    if (npc != null) {
    player.setAttribute("GetTheOtherBreedingNPC", npc);
    ControlMenu.setVisible(true); // show the notes menu
    player.setMouseCursorVisible(true); // show the mouse cursor
    int Clicked = 0;
    player.setAttribute("Clicked", Clicked);
    return; 
    }     
    }
     
    
    //Npc could be null, so check that
    if (npc != null) {
        
    if((int)player.getAttribute("Clicked") == 2){
    InfoLabel.setText(npc.getDefinition().name+" is already been called over");
    int Clicked = 0;
    player.setAttribute("Clicked", Clicked);
    return;
    }    
     
     try (ResultSet Result = beastfriendsDataBaseAccess1.executeQuery("SELECT * FROM `beastfriends` WHERE GlobalID = '"+npc.getGlobalID()+"'")) {
                    if (Result.next()) {
                    String OwnerName = Result.getString("OwnerName");
                    String OwnerUID = Result.getString("OwnerUID");
                    if(OwnerUID.equals(player.getUID())){
                    if((int)player.getAttribute("MenuCount")==1){
                    ControlMenu.setVisible(true);
                    player.setMouseCursorVisible(true);
                    npc.setAttribute("Breed", false);
                    player.setAttribute("GetTheNPC", npc);
                    int MenuCount = 0;
                    player.setAttribute("MenuCount", MenuCount);
                    return;
                    }     
                       
                    InfoLabel.setText("You Already own this "+npc.getDefinition().name);
                    int Clicked = 0;
                    player.setAttribute("Clicked", Clicked);
                    // Opens user control panel here
                    try {
                    Thread.sleep(1000); // Pause for 3 seconds
                    } catch (InterruptedException e) {
                    e.printStackTrace();
                    }
                    InfoLabel.setText(OwnerName+" Right Click the "+npc.getDefinition().name+" Again to open the control paniel"); 
                    int MenuCount = (int)player.getAttribute("MenuCount")+1;
                    player.setAttribute("MenuCount", MenuCount);
                    return;
                    }else{
                    InfoLabel.setText(OwnerName+" already owns this "+npc.getDefinition().name+" you cannot call it"); 
                    int Clicked = 0;
                    player.setAttribute("Clicked", Clicked);
                    int MenuCount = 0;
                    player.setAttribute("MenuCount", MenuCount);
                    return;
                    }
                    }
                    }catch (SQLException ex) {
                    Logger.getLogger(TamingManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
    
    
    
    
    if((int)player.getAttribute("Clicked") == 0){
    
    npc.setAttribute("old Position", npc.getPosition());
    InfoLabel.setText("Click the "+npc.getDefinition().name+" Again to call it over");
    
    long NpcGlobalID = npc.getGlobalID();
    player.setAttribute("NpcGlobalID", NpcGlobalID);
    
    
    int Clicked = 1;
    player.setAttribute("Clicked", Clicked);
    }else if((int)player.getAttribute("Clicked") == 1 && (long)player.getAttribute("NpcGlobalID") == npc.getGlobalID()){
    
    InfoLabel.setText(npc.getDefinition().name+" called over");
    npc.setAlerted(false);
    npc.setLocked(false);
                    
       Timer moveTimer = new Timer(1.0f, 0.0f, -1, () -> {
       npc.moveTo(player.getPosition());

        player.getNpcInLineOfSight(3, (CalledNpc) -> {
        if(CalledNpc != null){    
        if(CalledNpc.getGlobalID() == npc.getGlobalID()){
        npc.setAlerted(false);
        npc.setLocked(true);
        InfoLabel.setText(npc.getDefinition().name+" has reached you, Click Feed to feed or Cancel");
        // reset Clicked to 0 will move the process to UI for feeding
        int Clicked = 0;
        player.setAttribute("Clicked", Clicked);
        // UI popup
        player.setMouseCursorVisible(true);
        FeedMenu.setVisible(true);
        Timer KillmoveTimer = (Timer)npc.getAttribute("NpcComeToPlayerTimer");
        KillmoveTimer.kill();
        player.setAttribute("GetTheNPC", npc);
        }}   
        });
       
       });
       moveTimer.start();                  
       npc.setAttribute("NpcComeToPlayerTimer", moveTimer);
    
    
    int Clicked = 2;
    player.setAttribute("Clicked", Clicked);
    }else{InfoLabel.setText("This "+npc.getDefinition().name+" is not the same "+npc.getDefinition().name+" Click the Same "+npc.getDefinition().name+" Again!!!"); 
    int Clicked = 0;
    player.setAttribute("Clicked", Clicked);
    return;}
    }
    });
    
    }
    
    public void TamingOnUiClickEvent(PlayerUIElementClickEvent event) {
    Player player = event.getPlayer();
     if (event.getUIElement() != null) {
         
         
    
    UILabel CancelFeedButton = (UILabel)player.getAttribute("CancelFeedButton");
    UIElement FeedMenu = (UIElement)player.getAttribute("FeedMenu");
    UILabel FeedButton = (UILabel)player.getAttribute("FeedButton");
    
    // Cancel feed return NPC
    if (event.getUIElement().getID() == CancelFeedButton.getID()) {
        UILabel InfoLabel = (UILabel)player.getAttribute("InfoLabel");
            FeedMenu.setVisible(false); // Hide the notes menu
            player.setMouseCursorVisible(false); // Hide the mouse cursor
        Npc npc = (Npc)player.getAttribute("GetTheNPC");
        npc.setLocked(false);
        npc.moveTo((Vector3f)npc.getAttribute("old Position"));
        InfoLabel.setText(npc.getDefinition().name + " is returning hungry ");
        int FeedCount = 0;
        player.setAttribute("FeedCount", FeedCount);
        }
    
    
    
   // feed NPC
if (event.getUIElement().getID() == FeedButton.getID()) {
    UILabel InfoLabel = (UILabel)player.getAttribute("InfoLabel"); 
    // Get the right NPC 
    Npc npc = (Npc)player.getAttribute("GetTheNPC");
    
    String NPCNAME = npc.getDefinition().name;
    String foodType; // Declare a variable to hold the food type
    // Assign the food type based on NPCNAME
    switch (NPCNAME) {
        case "fox": case "lion": case "lioness": case "wolf": case "shewolf": case "arcticwolf": case "arcticshewolf": case "firewolf":
            foodType = "steak";
            break;
        case "pig": case "piglet": case "goat": case "billygoat": case "goatling":
            foodType = "apple";
            break;
        case "chicken": case "chick":
            foodType = "earthworm";
            break;
        case "cow": case "bull": case "calf":
            foodType = "grass";
            break;
        case "sheep": case "ram": case "lamb": case "sheepshorn": case "ramshorn":
            foodType = "lettuceleaves";
            break;
        case "bear": case "bearmale": case "bearcub": case "polarbear":
            foodType = "corncob";
            break;
        case "hare": case "foxcub": case "arcticfox": case "arcticfoxcub":
            foodType = "carrot";
            break;
        case "deer": case "deerstag": case "deercalf": case "deerred":
            foodType = "berries";
            break;
        case "moose": case "moosebull": case "moosecalf":
            foodType = "twigs";
            break;
        case "wildsow": case "wildboar": case "wildpiglet":
            foodType = "mushroom";
            break;
        case "penguin":
            foodType = "fish";
            break;
        case "horse": case "foal": case "zebra":
            foodType = "hay";
            break;
        case "elephant": case "rhinoceros":
            foodType = "leaves";
            break;
        case "snake": case "scorpion": case "spider":
            foodType = "insect";
            break;
        case "earthworm":
            foodType = "dirt";
            break;
        case "bandit": case "barbarian": case "dummy":
            foodType = "bread";
            break;
        case "skeleton": case "ghoul":
            foodType = "bone";
            break;
        default:
            foodType = "unknown"; // Use a default value instead of substring
            break;
    }
    // Set the label text using the food type
    InfoLabel.setText(npc.getDefinition().name + " eats " + foodType);
    
       
        int slot = player.getInventory().getQuickslotFocus();
        // now see if player food in hand = right food type
        Item slotItem = player.getInventory().getItem(slot, Inventory.SlotType.Quickslot);
        
       
        
        if(slotItem != null){
        
        
        if (slotItem.getName().equals(foodType)) {
       
        
        
        
        
        if(slotItem.getStack() >= 1){        
        player.getInventory().removeItem(slot, Inventory.SlotType.Quickslot, 1);
        int FeedCount = (int)player.getAttribute("FeedCount")+1;
        player.setAttribute("FeedCount", FeedCount);
        }else{player.getInventory().removeItem(slot, Inventory.SlotType.Quickslot);
        int FeedCount = (int)player.getAttribute("FeedCount")+1;
        player.setAttribute("FeedCount", FeedCount);
        }
        
        
        
        
        if((int)player.getAttribute("FeedCount") <= 4){
        Random random = new Random();
        int roll = random.nextInt(10) + 1; // 1 to 10
        
        if(roll == 1){
        FeedMenu.setVisible(false); // Hide the notes menu
        player.setMouseCursorVisible(false); // Hide the mouse cursor
        npc.setAlerted(false); 
        npc.setLocked(true);
        InfoLabel.setText(npc.getDefinition().name + "is happy and wants more food");  
        }
        else if(roll == 2){
         FeedMenu.setVisible(false); // Hide the notes menu
        player.setMouseCursorVisible(false); // Hide the mouse cursor
        npc.setAlerted(false); 
        npc.setLocked(true);
        InfoLabel.setText(npc.getDefinition().name + "is happy and wants more food");      
        }
        else if(roll == 3){ 
         FeedMenu.setVisible(false); // Hide the notes menu
        player.setMouseCursorVisible(false); // Hide the mouse cursor
        npc.setAlerted(false); 
        npc.setLocked(true);
        InfoLabel.setText(npc.getDefinition().name + "is happy and wants more food");      
        }
        else if(roll == 4){ 
         FeedMenu.setVisible(false); // Hide the notes menu
        player.setMouseCursorVisible(false); // Hide the mouse cursor
        npc.setAlerted(false); 
        npc.setLocked(true);
        InfoLabel.setText(npc.getDefinition().name + "is happy and wants more food");      
        }
        else if(roll == 5){
         FeedMenu.setVisible(false); // Hide the notes menu
        player.setMouseCursorVisible(false); // Hide the mouse cursor
        npc.setAlerted(false); 
        npc.setLocked(true);
        InfoLabel.setText(npc.getDefinition().name + "is happy and wants more food");      
        }
        else if(roll == 6){
        FeedMenu.setVisible(false); // Hide the notes menu
        player.setMouseCursorVisible(false); // Hide the mouse cursor
        npc.setAlerted(false); 
        npc.setLocked(true);
        InfoLabel.setText(npc.getDefinition().name + "is happy and wants more food");     
        }
        else if(roll == 7){
        FeedMenu.setVisible(false); // Hide the notes menu
        player.setMouseCursorVisible(false); // Hide the mouse cursor
        npc.setAlerted(false);
        npc.setLocked(true);
        InfoLabel.setText(npc.getDefinition().name + "is happy and wants more food");    
        }
        else if(roll == 8){
        FeedMenu.setVisible(false); // Hide the notes menu
        player.setMouseCursorVisible(false); // Hide the mouse cursor
        npc.setLocked(false);
        npc.setAlerted(true);
        npc.moveTo((Vector3f)npc.getAttribute("old Position"));    
        InfoLabel.setText(npc.getDefinition().name + "lost the food and is returning more hungry and frightened");  
        
        int FeedCount = (int)player.getAttribute("FeedCount")-1;
        player.setAttribute("FeedCount", FeedCount);
        }
        else if(roll == 9){
        FeedMenu.setVisible(false); // Hide the notes menu
        player.setMouseCursorVisible(false); // Hide the mouse cursor
        npc.setLocked(false);
        npc.setAlerted(true);
        npc.moveTo((Vector3f)npc.getAttribute("old Position"));    
        InfoLabel.setText(npc.getDefinition().name + " is returning a little Less hungry but frightened");     
        }
        else if(roll == 10){
        FeedMenu.setVisible(false); // Hide the notes menu
        player.setMouseCursorVisible(false); // Hide the mouse cursor
        npc.setLocked(false);
        npc.moveTo((Vector3f)npc.getAttribute("old Position"));    
        InfoLabel.setText(npc.getDefinition().name + " is returning a little Less hungry ");    
        }   
        
        }
        
        
        InfoLabel.setText(InfoLabel.getText()+" "+(int)player.getAttribute("FeedCount")+"/5");  
        
        if((int)player.getAttribute("FeedCount") >= 5){
        // Npc is now taimed lets add it to the players Database  and close the taiming paniel 
      
        String npcName = npc.getDefinition().name;
        String petName; // Variable to store the result
        switch (npcName.toLowerCase()) {
        case "fox":
            petName = "Lassie";
            break;
        case "pig":
            petName = "Casber";
            break;
        case "chicken":
            petName = "Peek";
            break;
        case "cow":
            petName = "Chops";
            break;
        case "sheep":
            petName = "LambChops";
            break;
        case "bear":
            petName = "Poo";
            break;
        case "lion":
            petName = "Leo";
            break;
        case "goat":
            petName = "Billy";
            break;
        case "dummy":
            petName = "John";
            break;
        case "piglet":
            petName = "Piggy";
            break;
        case "bull":
            petName = "Bully";
            break;
        case "calf":
            petName = "Calfy";
            break;
        case "ram":
            petName = "Rammy";
            break;
        case "lamb":
            petName = "Lamby";
            break;
        case "sheepshorn":
            petName = "Shornie";
            break;
        case "ramshorn":
            petName = "Ramshornie";
            break;
        case "billygoat":
            petName = "Billygoat";
            break;
        case "goatling":
            petName = "Goatling";
            break;
        case "chick":
            petName = "Chicky";
            break;
        case "hare":
            petName = "Harey";
            break;
        case "earthworm":
            petName = "Wormy";
            break;
        case "snake":
            petName = "Snakey";
            break;
        case "scorpion":
            petName = "Scorp";
            break;
        case "spider":
            petName = "Spidey";
            break;
        case "deer":
            petName = "Deery";
            break;
        case "deerstag":
            petName = "Staggy";
            break;
        case "deercalf":
            petName = "Calfy";
            break;
        case "deerred":
            petName = "Reddy";
            break;
        case "foxcub":
            petName = "Cubby";
            break;
        case "arcticfox":
            petName = "Arctic";
            break;
        case "arcticfoxcub":
            petName = "ArcticCub";
            break;
        case "moose":
            petName = "Moosey";
            break;
        case "moosebull":
            petName = "BullMoose";
            break;
        case "moosecalf":
            petName = "MooseCalf";
            break;
        case "wildsow":
            petName = "Sowwy";
            break;
        case "wildboar":
            petName = "Boary";
            break;
        case "wildpiglet":
            petName = "Piglet";
            break;
        case "bearmale":
            petName = "BearMan";
            break;
        case "bearcub":
            petName = "BearCub";
            break;
        case "polarbear":
            petName = "Polar";
            break;
        case "penguin":
            petName = "Pengy";
            break;
        case "horse":
            petName = "Horsey";
            break;
        case "foal":
            petName = "Foaly";
            break;
        case "zebra":
            petName = "Zebra";
            break;
        case "elephant":
            petName = "Ellie";
            break;
        case "rhinoceros":
            petName = "Rhino";
            break;
        case "lioness":
            petName = "Leona";
            break;
        case "wolf":
            petName = "Wolfy";
            break;
        case "shewolf":
            petName = "SheWolf";
            break;
        case "wolfcub":
            petName = "WolfCub";
            break;
        case "arcticwolf":
            petName = "ArcticWolf";
            break;
        case "arcticshewolf":
            petName = "ArcticShe";
            break;
        case "firewolf":
            petName = "FireWolf";
            break;
        case "bandit":
            petName = "Bandit";
            break;
        case "barbarian":
            petName = "Barb";
            break;
        case "skeleton":
            petName = "Skelly";
            break;
        case "ghoul":
            petName = "Ghoulie";
            break;
        default:
            petName = npcName.substring(0, 1).toUpperCase() + npcName.substring(1);
            break;
    }
    
        
      
        
        
        npc.setName(petName);
        npc.setAttackReaction(Npcs.AttackReaction.Ignore);
        npc.setBehaviour(Npcs.Behaviour.Default);
        npc.setInvincible(true);
        
        
        
        beastfriendsDataBaseAccess1.execute("INSERT INTO `beastfriends` (`NpcName`,`TypeName`, `NpcVariation`, `GroupId`, `GlobalID`, `OwnerUID`, `OwnerName`, `health`,`posx`, `posy`, `posz`, `rotx`, `roty`, `rotz`, `rotw`, `Age`, `Guard`) VALUES ('"+ petName +"','"+npc.getDefinition().name +"','"+ npc.getVariant() +"','"+ npc.getGroupID() +"','"+ npc.getGlobalID() +"','"+ player.getUID() +"','"+ player.getName() +"','"+ npc.getHealth() +"','"+ npc.getPosition().x +"','"+ npc.getPosition().y +"','"+ npc.getPosition().z +"','"+ npc.getRotation().x +"','"+ npc.getRotation().y  +"','"+ npc.getRotation().z  +"','"+ npc.getRotation().w  +"','"+ npc.getAge() +"','0')");          
        
        
        FeedMenu.setVisible(false); // Hide the notes menu
        player.setMouseCursorVisible(false); // Hide the mouse cursor 
        int FeedCount = 0;
        player.setAttribute("FeedCount", FeedCount);
        return;
        }
        
        return;
        }else{
        // NPC not Happy so returns feeding failed    
        FeedMenu.setVisible(false); // Hide the notes menu
        player.setMouseCursorVisible(false); // Hide the mouse cursor 
        InfoLabel.setText(npc.getDefinition().name + "'s eats " + foodType + " Not "+slotItem.getName()+"!!!");
        try {
        Thread.sleep(3000); // Pause for 3 seconds
        } catch (InterruptedException e) {
        e.printStackTrace();
        }
        npc.setLocked(false);
        npc.moveTo((Vector3f)npc.getAttribute("old Position"));
        InfoLabel.setText(npc.getDefinition().name + " is returning hungry ");
        int FeedCount = 0;
        player.setAttribute("FeedCount", FeedCount);
        }
        
        }
        if (slotItem == null) {
            InfoLabel.setText(npc.getDefinition().name + " hold some " + foodType + " to feed them");
            return;
        }
    
    
    
}
    
     }
    }
    
    
    
    
    
}


