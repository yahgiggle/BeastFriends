package beastfriends;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.risingworld.api.Plugin;
import net.risingworld.api.Server;
import net.risingworld.api.Timer;
import net.risingworld.api.World;
import net.risingworld.api.database.Database;
import net.risingworld.api.definitions.Npcs;
import net.risingworld.api.events.EventMethod;
import net.risingworld.api.events.Listener;
import net.risingworld.api.events.player.PlayerConnectEvent;
import net.risingworld.api.events.player.PlayerDisconnectEvent;
import net.risingworld.api.events.player.PlayerMouseButtonEvent;
import net.risingworld.api.events.player.ui.PlayerUIElementClickEvent;
import net.risingworld.api.objects.Npc;
import net.risingworld.api.objects.Player;
import net.risingworld.api.objects.Time;
import net.risingworld.api.ui.UIElement;
import net.risingworld.api.ui.UILabel;
import net.risingworld.api.utils.MouseButton;
import net.risingworld.api.utils.Vector3f;






public class BeastFriends extends Plugin implements Listener {

    protected static Database beastfriendsDataBaseAccess1;
    protected static Database beastfriendsDataBaseAccess2;
    protected static Database beastfriendsDataBaseAccess3;
    private static String WorldName;
    protected static Plugin plugin;
   
     
    
    @Override
    public void onEnable() {
        System.out.println("BeastFriends plugin starting...");
        try {
            // Initialize database structure
            new DatabaseManager(this).createDatabaseStructure();
            
            registerEventListener(this);
            this.plugin = this;
            System.out.println("BeastFriends plugin successfully enabled.");
        } catch (Exception e) {
            System.out.println("Failed to enable BeastFriends plugin: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Set All breeding false
        Npc[] npcs = World.getAllNpcs();
        for (Npc npc : npcs) {
 	npc.setAttribute("Breed", false);
        }
        
        
            // clean out database  
            try(ResultSet Result = beastfriendsDataBaseAccess1.executeQuery("SELECT * FROM `beastfriends`")){
            while(Result.next()) {
            long SavedNPCs =  Result.getLong("GlobalID");
            Npc KillNpc = World.getNpc(SavedNPCs);
            if(KillNpc != null){
            System.out.println("NPC Found");
            }else{System.out.println("NPC Not Found");
            beastfriendsDataBaseAccess2.executeUpdate("DELETE FROM beastfriends WHERE GlobalID = '"+SavedNPCs+"'");
            }
            }
            }catch (SQLException ex) {Logger.getLogger(BeastFriends.class.getName()).log(Level.SEVERE, null, ex);}
        
        
            
            
            
            //set guards
            try(ResultSet Result = beastfriendsDataBaseAccess1.executeQuery("SELECT * FROM `beastfriends`")){
            while(Result.next()) {
                
            long SavedNPCs =  Result.getLong("GlobalID");
            Npc GuardNpc = World.getNpc(SavedNPCs);
            if(GuardNpc != null){
            
            if (Boolean.TRUE.equals(Result.getBoolean("Guard"))) {
            String NPCOwner = Result.getString("OwnerUID");  
            String NpcName = Result.getString("NpcName"); 
            System.out.println("Gaurd Set");
            GuardNpc.setName("Guard "+NpcName);
            Vector3f MakeVector3f = new Vector3f(Result.getFloat("posx"),Result.getFloat("posy"),Result.getFloat("posz"));   
            GuardNpc.setAttribute("GuardSpot",MakeVector3f);    
             
            Timer guardTimer = new Timer(1.0f, 0.0f, -1, () -> {
            for(Player OnLineplayers : Server.getAllPlayers()){
            if(OnLineplayers != null){ 
            Npc[] GetNPCs = World.getAllNpcsInRange(OnLineplayers.getPosition(), 25);
            for (Npc GotNPCs : GetNPCs) {
            if(GotNPCs.getGlobalID() == GuardNpc.getGlobalID()){   
            // within range   
            GuardNpc.setAttribute("GuardAttack", true);
            
                        if(OnLineplayers.getUID().matches(NPCOwner)){
                        GuardNpc.setAlerted(false);
                        GuardNpc.setBehaviour(Npcs.Behaviour.Default);
                        GuardNpc.setAttackReaction(Npcs.AttackReaction.Ignore);
                        GuardNpc.moveTo((Vector3f)GuardNpc.getAttribute("GuardSpot"));    
                    }else if(!OnLineplayers.getUID().matches(NPCOwner)){
                        GuardNpc.setAlerted(true);
                        GuardNpc.setBehaviour(Npcs.Behaviour.Aggressive);
                        GuardNpc.setAttackReaction(Npcs.AttackReaction.Attack);
                        GuardNpc.moveTo(OnLineplayers.getPosition());
                    }
              
            }}   
            }}   
            });
            guardTimer.start();   
            GuardNpc.setAttribute("GuardTimer", guardTimer);
            }
            }
            }
            }catch (SQLException ex) {Logger.getLogger(BeastFriends.class.getName()).log(Level.SEVERE, null, ex);}
        
        
            
            
            
            
            
            
            
            
            
            
            
            
            
         
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
       
        
        
        
        
        
    }
    
    
    
    
           @EventMethod
  public void onPlayerConnect(PlayerConnectEvent event) throws SQLException  {
  Player player = event.getPlayer();
    
    int Clicked = 0;
    player.setAttribute("Clicked", Clicked);
    long NpcGlobalID = 0;      
    player.setAttribute("NpcGlobalID", NpcGlobalID);
    int FeedCount = 0;
    player.setAttribute("FeedCount", FeedCount);
    int MenuCount = 0;
    player.setAttribute("MenuCount", MenuCount);
    player.setAttribute("SetNpctoBreed", false);
    
    
     new BeastFriendsUI(this).BeastHUD(event);
     new BeastFriendsUI(this).BeastFeedMenu(event);
     new BeastFriendsUI(this).BeastControlMenu(event);
     
     
    
  }
    
  
    
    @EventMethod
    public void onPlayerMouseButtonEvent(PlayerMouseButtonEvent event ) throws SQLException {
    Player player = event.getPlayer();
    
    if (event.isPressed() && event.getButton() == MouseButton.Right) {
      new TamingManager(this).TamingNPC(player);
    }
                    
    }
    
    
    
      @EventMethod
    public void onPlayerUIEClick(PlayerUIElementClickEvent event) throws SQLException {
    
    
    new TamingManager(this).TamingOnUiClickEvent(event);
    new PetManager(this).ControllingNPCOnClickEvent(event);
    
    }

    
               @EventMethod
  public void onPlayerDisconnectEvent(PlayerDisconnectEvent event) throws SQLException  {
  Player player = event.getPlayer();
  
  }
    
    
    
    
    
    @Override
    public void onDisable() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

  
}

