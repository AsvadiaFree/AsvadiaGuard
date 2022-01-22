package fr.asvadia.guard.bukkit.object;

import fr.asvadia.api.bukkit.reflection.Reflector;
import fr.asvadia.guard.bukkit.BukkitMain;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.stream.Collectors;

public class CommandManager {

    BukkitMain main;

    Set<Command> hideCommands;
    Set<String> hideLabels;

    public CommandManager(BukkitMain main) {
        hideCommands = new HashSet<>();
        hideLabels = new HashSet<>();
        this.main = main;
    }

    public void load() {
        int config = main.getConf().get("hide.blacklist.config");

        SimpleCommandMap map = (SimpleCommandMap) Reflector.getCommandMap();

        List<Plugin> pluginW = new ArrayList<>();
        for(String pl : main.getConf().getResource().getStringList("hide.whitelist.plugin")) pluginW.add(Bukkit.getPluginManager().getPlugin(pl));
        List<String> commandW = new ArrayList<>(main.getConf().get("hide.whitelist.command"));

        List<Plugin> pluginB = new ArrayList<>();
        for(String pl : main.getConf().getResource().getStringList("hide.blacklist.plugin")) pluginB.add(Bukkit.getPluginManager().getPlugin(pl));
        List<String> commandB = new ArrayList<>(main.getConf().get("hide.blacklist.command"));


        for (Command command : map.getCommands()) {
            if (isInListCommand(command, pluginW, commandW)) {
                if (config == 2) hideCommands.add(command);
                else {
                    if(isInListCommand(command, pluginB, commandB))hideCommands.add(command);
                }
            }
            commandB.removeAll(command.getAliases().stream().map(String::toLowerCase).collect(Collectors.toList()));
            commandB.remove(command.getName().toLowerCase());
        }
        hideLabels.addAll(commandB);
    }

    public boolean isInListCommand(Command command, List<Plugin> plugins, List<String> labels) {
        if (plugins != null && command instanceof PluginCommand && plugins.contains(((PluginCommand) command).getPlugin())) return true;
        else if (labels.contains(command.getName().toLowerCase())) return true;
        else for (String alias : command.getAliases()) if (labels.contains(alias.toLowerCase())) return true;
        return false;
    }


    public Set<Command> getHideCommands() {
        return hideCommands;
    }

    public Set<String> getHideLabels() {
        return hideLabels;
    }

    public boolean isHideCommand(String enter) {
        Command command = getCommandFromString(enter);
        if(command != null) return hideCommands.contains(command);
        else return hideLabels.contains(enter.toLowerCase());
    }

    public Command getCommandFromString(String enter) {
        enter = enter.toLowerCase();

        SimpleCommandMap commandMap = (SimpleCommandMap) Reflector.getCommandMap();

        Command enterCommand = commandMap.getCommand(enter);
        if(enterCommand != null) return enterCommand;

        else {
            for (Command command : commandMap.getCommands()) {
                if (command.getName().equalsIgnoreCase(enter) || command.getAliases().stream().map(String::toLowerCase).collect(Collectors.toList()).contains(enter))
                    return command;
            }
        }
        return null;
    }

    public String getEnterString(String enter) {
        enter = enter.replaceFirst("/", "").split(" ")[0];
        if (enter.contains(":")) {
            switch (enter.substring(enter.length() - 1)) {
                case "":
                case ":":
                    break;
                default: {
                    enter = enter.split(":")[1];
                    break;
                }
            }
        }
        return enter;
    }
}
