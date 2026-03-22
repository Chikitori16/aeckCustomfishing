# aeckCustomFishing - Minecraft Paper Plugin

## Project Overview

A custom fishing plugin for Minecraft Paper server (API 1.21.1). Developed by AECK.ONLINE. Built with Java 21 and Maven.

This is a **Minecraft server plugin** — there is no web frontend or backend server. The project compiles to a JAR file that gets installed on a Paper/Spigot Minecraft server.

## Build

Run the **Build Plugin** workflow, or manually:

```bash
JAVA_HOME=/nix/store/3ilfkn8kxd9f6g5hgr0wpbnhghs4mq2m-openjdk-21.0.7+6 \
PATH=/nix/store/3ilfkn8kxd9f6g5hgr0wpbnhghs4mq2m-openjdk-21.0.7+6/bin:$PATH \
mvn -f aeckCustomfishing/aeckCustomfishing/aeckfishing1.0/pom.xml clean package
```

**Output:** `aeckCustomfishing/aeckCustomfishing/aeckfishing1.0/target/customfish-1.1-RELEASE.jar`

## Project Structure

```
aeckCustomfishing/aeckCustomfishing/aeckfishing1.0/
├── pom.xml                         # Maven build config (Java 21, Paper API 1.21.1)
├── src/main/java/online/aeck/customfish/
│   ├── CustomFish.java             # Main plugin class
│   ├── FishCatchListener.java      # Fishing event handler
│   ├── FishCombatManager.java      # Combat system (BossBar HP)
│   ├── FishCombatListener.java     # Combat input listener
│   ├── EconomyBridge.java          # Vault economy integration
│   ├── MySQLManager.java           # MySQL logging & leaderboard
│   ├── FishInventoryGUI.java       # Admin item GUI
│   ├── RodUpgradeGUI.java          # /nangcapcan equipment GUI
│   ├── SkillTreeGUI.java           # /kynangcauca skill tree GUI
│   ├── FishingQuestGUI.java        # Quest GUI
│   ├── FishSellGUI.java            # /banca sell GUI
│   ├── TopFishGUI.java             # /topcauca leaderboard GUI
│   └── ...                         # Other managers and commands
└── src/main/resources/
    ├── plugin.yml                  # Plugin metadata & commands
    ├── config.yml                  # Main config (EXP, MySQL, multipliers)
    ├── fishingrod.yml              # Rod/line/hook/bait equipment stats
    ├── fishspecial.yml             # Special fish definitions
    └── quests.yml                  # Quest definitions
```

## Key Dependencies

- **Paper API 1.21.1** (provided by server)
- **Vault API** (economy integration, provided by server)
- **PlaceholderAPI** (placeholders, provided by server)

## Java Version

The project requires **Java 21**. The Nix store path is:
`/nix/store/3ilfkn8kxd9f6g5hgr0wpbnhghs4mq2m-openjdk-21.0.7+6`

## Features

- Custom fishing mechanics with quality tiers (Common → Godly)
- Fish combat system with BossBar HP display
- Equipment system: rods, lines, hooks, bait
- Skill tree with Mastery and Power upgrades
- Fishing quests with quality requirements
- Economy integration via Vault (/banca sell command)
- MySQL leaderboard (/topcauca)
- Seasonal fishing bonuses (4 seasons)
- Automatic fishing tournaments every 2 hours
- EXP and leveling system
