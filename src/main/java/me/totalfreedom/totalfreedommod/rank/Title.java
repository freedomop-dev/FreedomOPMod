package me.totalfreedom.totalfreedommod.rank;

import lombok.Getter;
import org.bukkit.ChatColor;

public enum Title implements Displayable
{

    MASTER_BUILDER("a", "Master Builder", ChatColor.DARK_AQUA, "MB"),
    EXECUTIVE("an", "Executive", ChatColor.RED, "Exec"),
    DEVELOPER("a", "Developer", ChatColor.DARK_PURPLE, "TFM-Dev"),
    FOP_DEVELOPER("an", "FOP Developer", ChatColor.DARK_PURPLE, "Dev"),
    SPECIAL_EXECUTIVE("a", "Special Executive", ChatColor.YELLOW, "Spec-Exec"),
    SYSTEM_ADMIN("a", "System-Admin", ChatColor.DARK_RED, "System-Admin"),
    OWNER("the", "Owner", ChatColor.DARK_RED, "Owner");

    private final String determiner;
    @Getter
    private final String name;
    @Getter
    private final String abbr;
    @Getter
    private final String tag;
    @Getter
    private final String coloredTag;
    @Getter
    private final ChatColor color;

    private Title(String determiner, String name, ChatColor color, String tag)
    {
        this.determiner = determiner;
        this.name = name;
        this.coloredTag = ChatColor.DARK_GRAY + "[" + color + tag + ChatColor.DARK_GRAY + "]" + color;
        this.abbr = tag;
        this.tag = "[" + tag + "]";
        this.color = color;
    }

    @Override
    public String getColoredName()
    {
        return color + name;
    }

    @Override
    public String getColoredLoginMessage()
    {
        return determiner + " " + color + ChatColor.ITALIC + name;
    }

}
