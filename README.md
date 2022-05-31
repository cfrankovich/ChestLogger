# ChestLogger
Minecraft plugin that keeps track of chests. The goal is to keep and display a ledger of a chest. Some advanced features that I may want to include are alerts when someone opens a chest you are keeping track of, a personal list of chests to keep track of, and a website that displays the same information as in game. For more information on commands please visit [commands.md](https://github.com/cfrankovich/ChestLogger/blob/main/commanddocs.md)

## Versions
Tested and works on 1.18.2. Other versions should work.

## Commands
`/chest add` - adds a chest that the player is looking at to their watchlist 

`/chest list` - lists all chests a player is watching with their status

`/chest del [ID]` - removes a chest that the player's watchlist. if no id is given it will remove the chest the player is looking at.

`/chest ledger [ID]` - display the ledger of the chest of the id given. if no id is given then the chest the player is looking at is displayed.

`/chest clear  [ID]` - clear the ledger for the chest

## Screenshots
_Output of /chest list_

![LocationSampleOutput](https://i.imgur.com/LyLKl8f.png)

_Output of /chest ledger [ID]_

![LedgerExample](https://i.imgur.com/okky5ob.png)

## Releases 
- [v0.1.0](https://github.com/cfrankovich/ChestLogger/releases/tag/v0.1.0) - Initial Release
- [v0.1.1](https://github.com/cfrankovich/ChestLogger/releases/tag/v0.1.1) - Double Chest Fix
