package beastfriends;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.risingworld.api.Plugin;
import net.risingworld.api.Server;
import net.risingworld.api.Timer;
import net.risingworld.api.World;
import net.risingworld.api.definitions.Npcs;
import net.risingworld.api.events.player.ui.PlayerUIElementClickEvent;
import net.risingworld.api.objects.Npc;
import net.risingworld.api.objects.Player;
import net.risingworld.api.ui.UIElement;
import net.risingworld.api.ui.UILabel;
import net.risingworld.api.ui.UITextField;
import net.risingworld.api.utils.Quaternion;
import static net.risingworld.api.utils.Utils.MathUtils.distance;
import net.risingworld.api.utils.Vector3f;


public class PetManager extends BeastFriends{
    
    
    public PetManager(Plugin plugin) {
        this.plugin = plugin; 
    }
    
    
     public void ControllingNPCOnClickEvent(PlayerUIElementClickEvent event) throws SQLException {
      Player player = event.getPlayer(); 
      
      UILabel InfoLabel = (UILabel)player.getAttribute("InfoLabel");
      
      UILabel CancelControlButton = (UILabel)player.getAttribute("CancelControlButton");
      UIElement ControlMenu = (UIElement)player.getAttribute("ControlMenu");
      UIElement ControlReNameMenu = (UIElement)player.getAttribute("ControlReNameMenu");
      
      UITextField ControlReNameTextField = (UITextField)player.getAttribute("ControlReNameTextField");
      
      
      UILabel ControlCallButton = (UILabel)player.getAttribute("ControlCallButton");  
      UILabel ControlAttackButton = (UILabel)player.getAttribute("ControlAttackButton");  
      UILabel ControlbreedButton = (UILabel)player.getAttribute("ControlbreedButton");
      UILabel ControlGuardButton = (UILabel)player.getAttribute("ControlGuardButton");
      UILabel ControlReNameButton = (UILabel)player.getAttribute("ControlReNameButton");
      UILabel CancelReNameButton = (UILabel)player.getAttribute("CancelReNameButton");
      UILabel ReNameButton = (UILabel)player.getAttribute("ReNameButton");   
      
      
      
      if (event.getUIElement() != null) {
         
       if (event.getUIElement().getID() == CancelControlButton.getID()) {
            ControlMenu.setVisible(false); // Hide the notes menu
            player.setMouseCursorVisible(false); // Hide the mouse cursor
       }   
          
          
      
       
       
        if (event.getUIElement().getID() == ControlCallButton.getID()) {
        ControlMenu.setVisible(false); // Hide the notes menu
        player.setMouseCursorVisible(false); // Hide the mouse cursor    
        Npc npc = (Npc)player.getAttribute("GetTheNPC");
        InfoLabel.setText(npc.getDefinition().name+" has Been Called");
        
        try(ResultSet Result = beastfriendsDataBaseAccess1.executeQuery("SELECT * FROM `beastfriends` WHERE GlobalID = '"+ npc.getGlobalID() +"'")){
        if (Result.next()) {
        npc.setName(Result.getString("NpcName"));    
        }
        }
        
        Timer KillGuardTimer = (Timer)npc.getAttribute("GuardTimer");
        if(KillGuardTimer != null){KillGuardTimer.kill();}
        
        beastfriendsDataBaseAccess2.executeUpdate("UPDATE beastfriends SET posx = '" + npc.getPosition().x + "', posy = '" + npc.getPosition().y + "', posz = '" + npc.getPosition().z + "', Guard = '0'  WHERE GlobalID = '" + npc.getGlobalID() + "'"); // Update DB
    
        
        npc.setAlerted(false);
        npc.setLocked(false);
        
        Timer moveTimer = new Timer(1.0f, 0.0f, -1, () -> {
        npc.moveTo(player.getPosition());
        // npc.setAttackReaction(Npcs.AttackReaction.Ignore);
        //  npc.setBehaviour(Npcs.Behaviour.Default);
        
        player.getNpcInLineOfSight(2, (CalledNpc) -> {
        if(CalledNpc != null){    
        if(CalledNpc.getGlobalID() == npc.getGlobalID()){
        npc.setAlerted(false);
        npc.setLocked(true);
        //  npc.setAttackReaction(Npcs.AttackReaction.Ignore);
        InfoLabel.setText(npc.getDefinition().name+" has reached you");
        Timer KillmoveTimer = (Timer)npc.getAttribute("NpcComeToPlayerTimer");
        KillmoveTimer.kill();
        }}   
        });
       
        });
        moveTimer.start();                  
        npc.setAttribute("NpcComeToPlayerTimer", moveTimer);  
        }    
          
          
          
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        if (event.getUIElement().getID() == ControlAttackButton.getID()) {
        ControlMenu.setVisible(false); // Hide the notes menu
        player.setMouseCursorVisible(false); // Hide the mouse cursor    
        Npc npc = (Npc)player.getAttribute("GetTheNPC");
        InfoLabel.setText(npc.getDefinition().name+" has Been set to attack for 60sec");
        
        
        
        try(ResultSet Result = beastfriendsDataBaseAccess1.executeQuery("SELECT * FROM `beastfriends` WHERE GlobalID = '"+ npc.getGlobalID() +"'")){
        if (Result.next()) {
        npc.setName(Result.getString("NpcName"));    
        }
        }
        Timer KillGuardTimer = (Timer)npc.getAttribute("GuardTimer");
        if(KillGuardTimer != null){KillGuardTimer.kill();}
        beastfriendsDataBaseAccess2.executeUpdate("UPDATE beastfriends SET posx = '" + npc.getPosition().x + "', posy = '" + npc.getPosition().y + "', posz = '" + npc.getPosition().z + "', Guard = '0'  WHERE GlobalID = '" + npc.getGlobalID() + "'"); // Update DB
    
        
        npc.setAlerted(false);
        npc.setLocked(false);
        
        Timer moveTimer = new Timer(1.0f, 0.0f, 60, () -> {
       // npc.moveTo(player.getPosition());
        if(npc != null){
        Player NearestPlayer = npc.getNearestPlayer();
        if(NearestPlayer != null){
        if(NearestPlayer.getUID().equals(player.getUID())){
        npc.setAlerted(false);
        npc.setLocked(true);
        npc.setBehaviour(Npcs.Behaviour.Default);
        npc.setAttackReaction(Npcs.AttackReaction.Ignore);
        }else{
        npc.setAlerted(true);
        npc.setLocked(false);    
        npc.moveTo(NearestPlayer.getPosition());    
        npc.setBehaviour(Npcs.Behaviour.Aggressive);
        npc.setAttackReaction(Npcs.AttackReaction.Attack);
        }
        }
        
        player.getNpcInLineOfSight(2, (CalledNpc) -> {
        if(CalledNpc != null){    
        if(CalledNpc.getGlobalID() == npc.getGlobalID()){
        npc.setAlerted(false);
        npc.setLocked(true);
        npc.setBehaviour(Npcs.Behaviour.Default);
        npc.setAttackReaction(Npcs.AttackReaction.Ignore);
        InfoLabel.setText(npc.getDefinition().name+" has reached you and is submissive");
        Timer KillmoveTimer = (Timer)npc.getAttribute("NpcComeToPlayerTimer");
        KillmoveTimer.kill();
        }}
        
        });
       
        
        }
        
        });
        moveTimer.start();                  
        npc.setAttribute("NpcComeToPlayerTimer", moveTimer);  
        }    
          
          
        
        
        
        
        
        
        
        
        
        
        
          
        
        if (event.getUIElement().getID() == ControlbreedButton.getID()) {
        Npc npc = (Npc)player.getAttribute("GetTheNPC"); 
        
        try(ResultSet Result = beastfriendsDataBaseAccess1.executeQuery("SELECT * FROM `beastfriends` WHERE GlobalID = '"+ npc.getGlobalID() +"'")){
        if (Result.next()) {
        npc.setName(Result.getString("NpcName"));    
        }
        }
        Timer KillGuardTimer = (Timer)npc.getAttribute("GuardTimer");
        if(KillGuardTimer != null){KillGuardTimer.kill();}
        beastfriendsDataBaseAccess2.executeUpdate("UPDATE beastfriends SET posx = '" + npc.getPosition().x + "', posy = '" + npc.getPosition().y + "', posz = '" + npc.getPosition().z + "', Guard = '0'  WHERE GlobalID = '" + npc.getGlobalID() + "'"); // Update DB
    
        
        if (Boolean.FALSE.equals(player.getAttribute("SetNpctoBreed"))) {  
        InfoLabel.setText(npc.getDefinition().name+" has Been set to breed, select anther "+npc.getDefinition().name+" to breed with.");
        ControlMenu.setVisible(false); // Hide the notes menu
        player.setMouseCursorVisible(false); // Hide the mouse cursor
        npc.setAttribute("Breed", true);
        player.setAttribute("SetNpctoBreed", true);
        }else if (Boolean.TRUE.equals(player.getAttribute("SetNpctoBreed"))) {  
        Npc BreedingNpc = (Npc)player.getAttribute("GetTheOtherBreedingNPC");  
        BreedingNpc.setAttribute("SetHome", BreedingNpc.getPosition());
        npc.setAttribute("SetHome", npc.getPosition());
        
        InfoLabel.setText(npc.getDefinition().name+" has Been set to breed, with "+BreedingNpc.getDefinition().name);
        npc.setAttribute("Breed", false);
        player.setAttribute("SetNpctoBreed", false);
        // stop here for now
        ControlMenu.setVisible(false); // Hide the notes menu
        player.setMouseCursorVisible(false); // Hide the mouse cursor
        
        npc.setAlerted(false);
        npc.setLocked(false);
        npc.setColliderEnabled(false);
        
        BreedingNpc.setAlerted(false);
        BreedingNpc.setLocked(false);
        BreedingNpc.setColliderEnabled(false);
        
        if(BreedingNpc.getName() == null){
        BreedingNpc.setName("Wild Breeder");
        }
        
        
        
        
        Timer moveTimer = new Timer(1.0f, 0.0f, -1, () -> {
         
        if(npc.getPosition() == null){
        Timer KillmoveTimer = (Timer)npc.getAttribute("NpcComeToPlayerTimer");
        KillmoveTimer.kill(); 
        InfoLabel.setText("Something went wrong!!!");
        return;
        }    
         
        if(BreedingNpc.getPosition() == null){
        Timer KillmoveTimer = (Timer)npc.getAttribute("NpcComeToPlayerTimer");
        KillmoveTimer.kill();
        InfoLabel.setText("Something went wrong!!!");
        return;
        } 
        
        npc.setLocked(true);
        float distance = npc.getPosition().distance(BreedingNpc.getPosition());
        npc.setLocked(false);
        BreedingNpc.setColliderEnabled(false);
        npc.moveTo(BreedingNpc.getPosition());
      
        String NPCNAME = BreedingNpc.getDefinition().name;
        float NPCSize; 
       
        switch (NPCNAME) {
        case "fox": case "lion": case "lioness": case "wolf": case "shewolf": case "arcticwolf": case "arcticshewolf": case "firewolf":
            NPCSize = 3.0f;
            break;
        case "pig": case "piglet": case "goat": case "billygoat": case "goatling":
            NPCSize = 3.0f;
            break;
        case "chicken": case "chick":
            NPCSize = 1.3f;
            break;
        case "cow": case "bull": case "calf":
            NPCSize = 5.0f;
            break;
        case "sheep": case "ram": case "lamb": case "sheepshorn": case "ramshorn":
            NPCSize = 3.0f;
            break;
        case "bear": case "bearmale": case "bearcub": case "polarbear":
            NPCSize = 6.0f;
            break;
        case "hare": case "foxcub": case "arcticfox": case "arcticfoxcub":
            NPCSize = 3.0f;
            break;
        case "deer": case "deerstag": case "deercalf": case "deerred":
            NPCSize = 3.0f;
            break;
        case "moose": case "moosebull": case "moosecalf":
            NPCSize = 6.0f;
            break;
        case "wildsow": case "wildboar": case "wildpiglet":
            NPCSize = 3.0f;
            break;
        case "penguin":
            NPCSize = 2.0f;
            break;
        case "horse": case "foal": case "zebra":
            NPCSize = 6.0f;
            break;
        case "elephant": case "rhinoceros":
            NPCSize = 6.0f;
            break;
        case "snake": case "scorpion": case "spider":
            NPCSize = 2.0f;
            break;
        case "earthworm":
            NPCSize = 0.2f;
            break;
        case "bandit": case "barbarian": case "dummy":
            NPCSize = 2.0f;
            break;
        case "skeleton": case "ghoul":
            NPCSize = 2.0f;
            break;
        default:
            NPCSize = 10.0f; 
            break;
    }
    
        
        if(distance <= NPCSize){
        if(npc.getTypeID() == BreedingNpc.getTypeID()){  
            
        World.spawnNpc(npc.getTypeID(), 0, npc.getPosition(), Quaternion.IDENTITY).setName("Baby "+npc.getName());
        
        Timer KillBreedTimer = (Timer)npc.getAttribute("NpcComeToPlayerTimer");
        if(KillBreedTimer != null){KillBreedTimer.kill();}
        
        InfoLabel.setText(npc.getName()+" breeded, with "+BreedingNpc.getName());    
           
        npc.setLocked(false);
        npc.setColliderEnabled(true);
        npc.moveTo((Vector3f)npc.getAttribute("SetHome"));
        
        BreedingNpc.setLocked(false);
        BreedingNpc.setColliderEnabled(true);
        BreedingNpc.moveTo((Vector3f)BreedingNpc.getAttribute("SetHome"));
        
        }else{InfoLabel.setText(npc.getDefinition().name+"s cannot breed, with "+BreedingNpc.getDefinition().name);
        
        npc.setLocked(false);
        npc.setColliderEnabled(true);
        npc.moveTo((Vector3f)npc.getAttribute("SetHome"));
        
        BreedingNpc.setLocked(false);
        BreedingNpc.setColliderEnabled(true);
        BreedingNpc.moveTo((Vector3f)BreedingNpc.getAttribute("SetHome"));
        
        }
        Timer KillmoveTimer = (Timer)npc.getAttribute("NpcComeToPlayerTimer");
        KillmoveTimer.kill();        
        }
        
        });
        moveTimer.start();                  
        npc.setAttribute("NpcComeToPlayerTimer", moveTimer);  
        
        
        
        
        
        
        
        
        
        
        
        }
        
        }    
        
        
        
        
        
        
        
        
    if (event.getUIElement().getID() == ControlGuardButton.getID()) {
    ControlMenu.setVisible(false);
    player.setMouseCursorVisible(false);
    Npc npc = (Npc)player.getAttribute("GetTheNPC");
    npc.setAttribute("GuardSpot", npc.getPosition());
    npc.setAttribute("GuardOwnerUID", player.getDbID());
    npc.setAttribute("GuardAttack", false);
    
    
    try(ResultSet Result = beastfriendsDataBaseAccess1.executeQuery("SELECT * FROM `beastfriends` WHERE GlobalID = '"+ npc.getGlobalID() +"'")){
    if (Result.next()) {
       
    if (Boolean.FALSE.equals(Result.getBoolean("Guard"))) {     
    player.sendYellMessage("Pet set to guard this spot", 3, true);
    npc.setName("Guard "+npc.getName());
    npc.moveTo((Vector3f)npc.getAttribute("GuardSpot"));
    beastfriendsDataBaseAccess2.executeUpdate("UPDATE beastfriends SET posx = '" + npc.getPosition().x + "', posy = '" + npc.getPosition().y + "', posz = '" + npc.getPosition().z + "', Guard = '1'  WHERE GlobalID = '" + npc.getGlobalID() + "'"); // Update DB
    InfoLabel.setText(npc.getDefinition().name + " is now in guard mode");
    }else if (Boolean.TRUE.equals(Result.getBoolean("Guard"))) {
    player.sendYellMessage("Guard turned off ", 3, true);
    npc.setName(Result.getString("NpcName"));
    npc.setAlerted(false);
    npc.setLocked(false);
    beastfriendsDataBaseAccess2.executeUpdate("UPDATE beastfriends SET posx = '" + npc.getPosition().x + "', posy = '" + npc.getPosition().y + "', posz = '" + npc.getPosition().z + "', Guard = '0'  WHERE GlobalID = '" + npc.getGlobalID() + "'"); // Update DB
    InfoLabel.setText(npc.getDefinition().name + " is now Not in guard mode");
    
    Timer KillGuardTimer = (Timer)npc.getAttribute("GuardTimer");
    if(KillGuardTimer != null){KillGuardTimer.kill();}
    return;
    }
    
    }else{
    InfoLabel.setText(npc.getDefinition().name + " is not your pet");
    return;
    }
    }
    
    Timer guardTimer = new Timer(1.0f, 0.0f, -1, () -> {
        // if not in range return to gard area
        if (Boolean.FALSE.equals(npc.getAttribute("GuardAttack"))) {  
        npc.moveTo((Vector3f)npc.getAttribute("GuardSpot"));
        }
        
        for(Player OnLineplayers : Server.getAllPlayers()){
        if(OnLineplayers != null){    
        Npc[] GetNPCs = World.getAllNpcsInRange(OnLineplayers.getPosition(), 25);
        
        for (Npc GotNPCs : GetNPCs) {
        if(GotNPCs.getGlobalID() == npc.getGlobalID()){
        // within range   
        npc.setAttribute("GuardAttack", true);    
            
        if(OnLineplayers.getUID().matches(player.getUID())){
        npc.setAlerted(false);
        npc.setBehaviour(Npcs.Behaviour.Default);
        npc.setAttackReaction(Npcs.AttackReaction.Ignore);
        npc.moveTo((Vector3f)npc.getAttribute("GuardSpot"));
        }else if(!OnLineplayers.getUID().matches(player.getUID())){
        npc.setAlerted(true);
        npc.setBehaviour(Npcs.Behaviour.Aggressive);
        npc.setAttackReaction(Npcs.AttackReaction.Attack);
        npc.moveTo(OnLineplayers.getPosition());
        }else{
        npc.moveTo((Vector3f)npc.getAttribute("GuardSpot"));
        }   
            
      //  OnLineplayers.sendTextMessage("test0 ids="+GotNPCs.getGlobalID()+" match="+npc.getGlobalID()); 
        
        }else{npc.setAttribute("GuardAttack", false); }  
        }
        }
        }   
            
       
    });
    guardTimer.start();
    npc.setAttribute("GuardTimer", guardTimer);
}
        
        
        
        
        
        
    
    
    
        // ControlReNameButton
        if (event.getUIElement().getID() == ControlReNameButton.getID()) {
        ControlMenu.setVisible(false); 
        ControlReNameMenu.setVisible(true);
        Npc npc = (Npc)player.getAttribute("GetTheNPC");
        InfoLabel.setText("ReName "+npc.getDefinition().name+" "+npc.getName());
        ControlReNameTextField.setText(npc.getName());
        }    
              
        
        // ReNameButton
        if (event.getUIElement().getID() == ReNameButton.getID()) {
        ControlMenu.setVisible(true); 
        ControlReNameMenu.setVisible(false);
        Npc npc = (Npc)player.getAttribute("GetTheNPC");
        
        ControlReNameTextField.getCurrentText(player, (String txt) -> {
        beastfriendsDataBaseAccess2.executeUpdate("UPDATE beastfriends SET NpcName = '"+ txt +"'  WHERE GlobalID = '" + npc.getGlobalID() + "'"); // Update DB
        npc.setName(txt);
        InfoLabel.setText("ReNamed "+npc.getDefinition().name+" to "+txt);    
        });        
        
        }
        
        // CancelReNameButton
        if (event.getUIElement().getID() == CancelReNameButton.getID()) {
        ControlMenu.setVisible(true); 
        ControlReNameMenu.setVisible(false);
        } 
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
           
        
         
     
      }
      }
    
    
    
    
    
    
}

