package buywell.hg_score;

import java.util.*;
import java.util.logging.Logger;

import dev.jcsoftware.jscoreboards.JGlobalMethodBasedScoreboard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;



public final class Hg_score extends JavaPlugin implements Listener {

    Logger log = getLogger();
    boolean temp = false;
    LinkedHashMap<String, Integer> score_map = new LinkedHashMap<>(); //score for one map
    List<String> stringList = new ArrayList<>();
    LinkedHashMap<String, Integer> GlobalScore = new LinkedHashMap<>(); //global score
    HashSet<String> alive = new HashSet<>();


    private JGlobalMethodBasedScoreboard scoreboard;

    @Override
    public void onEnable() {
        // Plugin startup logic
        log.info("Your plugin has been enabled!");
        scoreboard = new JGlobalMethodBasedScoreboard();
        scoreboard.setLines("hi");
        scoreboard.setTitle("HG Score");
        getServer().getPluginManager().registerEvents(this,this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(command.getName().equalsIgnoreCase("resettab")){
            if(sender instanceof ConsoleCommandSender || sender.isOp()){
                alive.clear();
                GlobalScore.clear();
                score_map.clear();
                log.info(alive.toString());
                log.info(GlobalScore.toString());
                log.info(score_map.toString());
                Bukkit.getOnlinePlayers().forEach(this::forReset);
                log.info(alive.toString());
                log.info(GlobalScore.toString());
                log.info(score_map.toString());
                update_board();
            }
        }

        if (command.getName().equalsIgnoreCase("ifsuicide")){
            if(sender instanceof ConsoleCommandSender || sender.isOp()){
                Bukkit.getPluginManager().callEvent(new PlayerDeathEvent(null, null, 0, null));
            }
        }
        return true;
    }

    private void update_string_list(){
        int count = 0;
        stringList.clear();
        List<Map.Entry<String, Integer>> temp_list = new ArrayList<>(score_map.entrySet());
        temp_list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        for(Map.Entry<String, Integer> entry: temp_list){
            stringList.add(ChatColor.WHITE + entry.getKey() + ' ' + entry.getValue());
            count++;
            if(count == 5){
                return;
            }
        }
    }


    private void addToScoreboard(Player player){
        scoreboard.addPlayer(player);
    }

    private void update_board(){
        update_string_list();
        stringList.add("Alive: " + alive.size() + "/" + Bukkit.getOnlinePlayers().size());
        scoreboard.setLines(stringList);
        Bukkit.getOnlinePlayers().forEach(this::addToScoreboard);
    }

    private void forReset(Player player){
        alive.add(player.getDisplayName());
        score_map.put(player.getDisplayName(), 0);
        GlobalScore.put(player.getDisplayName(), 0);
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event){
       Player player = event.getPlayer();
       if(score_map.get(player.getDisplayName()) != null){
           scoreboard.addPlayer(player);
           return;
       }
       alive.add(player.getDisplayName());
       score_map.put(player.getDisplayName(), 0);
       GlobalScore.put(player.getDisplayName(), 0);
       update_board();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onDead(PlayerDeathEvent event){
        if(!temp){
            if(alive.size() == 12 || alive.size() == 6 || alive.size() == 3 || alive.size() == 2 || alive.size() == 1){
                for(String it : alive){
                    score_map.put(it, score_map.get(it)+1);
                }
                if(alive.size() <= 2){
                    temp = true;
                    log.info(String.valueOf(temp));
                }
            }
        }
        else if (temp == true){
            update_board();
            temp = false;
            update_global_score();
            reset_local_score();
            Bukkit.getOnlinePlayers().forEach(this::make_all_alive_and_show_result);
            update_board();
        }
        if(alive.size() == 0 || alive.size() == 1){
            update_board();
            temp = false;
            update_global_score();
            reset_local_score();
            Bukkit.getOnlinePlayers().forEach(this::make_all_alive_and_show_result);
            update_board();
        }
        update_board();
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onKill(EntityDamageEvent event){
        Player player = (Player) event.getEntity();
        if(player.getHealth() <= event.getFinalDamage()) {
            alive.remove(player.getDisplayName());
            if (score_map.get(player.getKiller().getDisplayName()) != null) {
                score_map.put(player.getKiller().getDisplayName(), score_map.get(player.getKiller().getDisplayName()) + 1);
            }
            Bukkit.getPluginManager().callEvent(new PlayerDeathEvent(player, null, 0, null));
        }
        update_board();
    }

    private void update_global_score(){
        for(Map.Entry<String, Integer> entry: score_map.entrySet()){
            GlobalScore.put(entry.getKey(), GlobalScore.get(entry.getKey())+entry.getValue());
        }
    }

    private void reset_local_score(){
        score_map.replaceAll((k, v) -> v = 0);
    }

    private void make_all_alive_and_show_result(Player player){
        alive.add(player.getDisplayName());
        log.info("add to alive " + player.getDisplayName());
        for(Map.Entry<String, Integer> entry: GlobalScore.entrySet()){
            player.sendMessage(ChatColor.GREEN + entry.getKey() + ": " + entry.getValue());
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        log.info("Your plugin has been disabled.");

    }

}

