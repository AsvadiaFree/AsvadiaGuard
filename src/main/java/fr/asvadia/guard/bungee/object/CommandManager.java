package fr.asvadia.guard.bungee.object;

import fr.asvadia.api.bungee.reflection.Reflector;
import fr.asvadia.guard.bungee.ProxyMain;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.*;
import java.util.stream.Collectors;

public class CommandManager {

    ProxyMain main;

    Set<Command> hideCommands;
    Set<String> hideLabels;

    public CommandManager(ProxyMain main) {
        hideCommands = new HashSet<>();
        hideLabels = new HashSet<>();
        this.main = main;
    }

    public void load() {
        int config = main.getConf().get("hide.blacklist.config");

        List<Plugin> whitelistPlugin = new ArrayList<>();
        for(String pl : main.getConf().getResource().getStringList("hide.whitelist.plugin")) whitelistPlugin.add(main.getProxy().getPluginManager().getPlugin(pl));
        List<String> whitelistCommand = new ArrayList<>(main.getConf().get("hide.whitelist.command"));

        List<Plugin> blacklistPlugin = new ArrayList<>();
        for(String pl : main.getConf().getResource().getStringList("hide.blacklist.plugin")) blacklistPlugin.add(main.getProxy().getPluginManager().getPlugin(pl));
        List<String> blacklistCommand = new ArrayList<>(main.getConf().get("hide.blacklist.command"));

        Map<Plugin, Collection<Command>> commandMap = Reflector.getCommands().asMap();


        for (Plugin plugin : commandMap.keySet()) {

            if(whitelistPlugin.contains(plugin)) break;
            for(Command command : commandMap.get(plugin)) {

                boolean whitelist = false;

                if (whitelistCommand.contains(command.getName().toLowerCase())) whitelist = true;
                else for (String alias : command.getAliases())
                    if (whitelistCommand.contains(alias.toLowerCase())) {
                        whitelist = true;
                        break;
                    }
                if (!whitelist) {
                    if (config == 2) hideCommands.add(command);
                    else {
                        if (blacklistPlugin.contains(plugin)) hideCommands.add(command);
                        else if (blacklistCommand.contains(command.getName().toLowerCase())) hideCommands.add(command);
                        else for (String alias : command.getAliases())
                                if (blacklistCommand.contains(alias.toLowerCase())) {
                                    hideCommands.add(command);
                                    break;
                                }
                    }
                }
                blacklistCommand.remove(command.getName().toLowerCase());
                blacklistCommand.removeAll(Arrays.asList(command.getAliases()).stream().map(String::toLowerCase).collect(Collectors.toList()));
            }
        }
        hideLabels.addAll(blacklistCommand);
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
        for (Map.Entry<String, Command> commandMap : main.getProxy().getPluginManager().getCommands()) {
            Command command = commandMap.getValue();
            if (command.getName().equalsIgnoreCase(enter) || Arrays.stream(command.getAliases()).map(String::toLowerCase).collect(Collectors.toList()).contains(enter))
                return command;
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
