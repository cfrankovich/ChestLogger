# Commands

## Contents
1) [/chest add](#chest-add=)
2) [/chest list](#chest-list=)
3) [/chest ledger [ID]](#chest-ledger-id=)
4) [/chest del [ID]](#chest-del-id=)
5) [/chest clear [ID]](#chest-clear-id=)

## /chest add
Chest add is a command to add a chest to a player's watchlist. 
To successfully run this command, a player must be aiming at a chest that is 4 blocks away from them.

Once a chest in range has been targeted and identified, it must not be a chest that is already being watched by the player.
To check if a chest is being watched already, simply run `/chest add` and see if an error message appears like so...

![Error2](https://i.imgur.com/KE1UGcx.png)

If this message does not appear then this message will appear meaning you have successfully added a chest to your watchlist.

![AddSuccess](https://i.imgur.com/zdoMoAJ.png)

## /chest list
Once a player has chests added to their watchlsit, they can access all of the chests along with their IDs and coordinates using this simple command.
A sample output looks like this

![LocationSampleOutput](https://i.imgur.com/LyLKl8f.png)

Please keep in mind that IDs will always not begin with one or increment by one.
Each chest in the server has its own unique id.

## /chest ledger [ID]
A player can use this command on any chest they have access to.
This means that if player A tries to access the ledger of a chest that player B has watchlisted, it will not work.
However, multiple players can watchlist the same chest but they will be given different IDs for security purposes.

The once this command is ran with a valid ID, the following might appear.

![LedgerExample](https://i.imgur.com/okky5ob.png)

White entries are neutral events such as a player opening a chest.
Red entries are events in which a player has took out items or destroyed a chest.
Green entires are when a player places in items to the chest.

Entries that begin with `*` signify that a player other than the owner has interacted with the chest.

## /chest del [ID]
A player can use this command on any chest they have access to.
This means that if player A tries to delete (un-watchlist) a chest that player B has watchlisted, it will not work.
However, multiple players can watchlist the same chest but they will be given different IDs for security purposes.
When a chest is removed from one player's watchlist, it will NOT remove it from another's.

This command removes a chest from the watchlist. 
No backups of the ledger or items are kept.
Use this command with caution.

## /chest clear [ID]
A valid ID that a player has access to must be given.
This means that if player A tries to clear a ledger that player B has watchlisted, it will not work.
However, multiple players can watchlist the same chest but they will be given different IDs for security purposes.
Every single player has a different ledger for a chest meaning that no one can clear someone else's ledger.

This command clears the ledger for readability. No backups are kept so use this command with caution.
