# ChestLogger
Minecraft plugin that keeps track of chests. The goal is to keep and display a history or ledger of a chest. Some advanced features that I may want to include are alerts when someone opens a chest you are keeping track of, a personal list of chests to keep track of, and a website that displays the same information as in game. 

## Versions
Tested and works on 1.18.2. Other versions should work.

## Contributing 
This is my first minecraft plugin and naturally, I have no knowledge of advanced methods or good practices to use when making a plugin. If you see anything in the source that results in undefined behavior or is bad practice, please open up a pr! 

## Commands
`/chest add` - adds a chest that the player is looking at to their watchlist 

`/chest del [ID]` - removes a chest that the player's watchlist. if no id is given it will remove the chest the player is looking at.

`/chest list` - lists all chests a player is watching with their status

`/chest alert [ID]` - toggle real time alerts from the given chest id. if no id is given then the chest the player is looking at is toggled.

`/chest ledger [ID]` - display the ledger of the chest of the id given. if no id is given then the chest the player is looking at is displayed.

`/chest clear  [ID]` - clear the ledger for the chest

## Screenshots
coming soon

## Building
coming soon
