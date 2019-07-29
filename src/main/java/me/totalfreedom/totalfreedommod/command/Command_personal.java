package me.totalfreedom.totalfreedommod.command;

import me.totalfreedom.totalfreedommod.rank.Rank;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermissions(level = Rank.OP, source = SourceType.ONLY_IN_GAME)
@CommandParameters(description = "Personal commands for specific people.", usage = "/<command>", aliases = "psl")
public class Command_personal extends FreedomCommand {

    @Override
    public boolean run (CommandSender sender, Player player, Command cmd, String label, String[] args, boolean senderIsConsole)
    {
        if (isConsole())
        {
            sender.sendMessage("This command works only in-game, mot from console.");
            return true;
        }

        switch (player.getName())
        {
            case "CrafterSmith12":
            {
                sender.sendMessage("stuff here blah blah");
                return true;
            }
            case "taahanis":
            {
                sender.sendMessage("stuff here blah blah blah");
                return true;
            }
        }
        return true;
    }

}
