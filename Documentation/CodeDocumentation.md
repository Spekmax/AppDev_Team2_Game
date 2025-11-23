************************************************************
* Code Documentation                                       *
************************************************************

Overview
--------
This project is a turn-based grid game inspired by Battleship.
Players place geckos on a board and take turns attacking to locate
and eliminate the opponent's geckos.

The backend is written in Kotlin using an object-oriented design.

************************************************************
* Class Documentation                                      *
************************************************************

Cell
----
Attributes:
- row : Int
- column : Int
- hasGecko : Boolean
- isHit : Boolean

Responsibility:
Represents a single position on the board. Eg. A1 or E4

------------------------------------------------------------

Board
-----
Attributes:
- size : Int
- grid : 2D Array<Cell>
- geckos : List<Gecko>

Functions:
- placeGecko(...)
- attack(row, col)
- allGeckosEliminated()

Responsibility:
Manages the game grid and interactions with cells.

------------------------------------------------------------

Gecko
-----
Attributes:
- name : String
- size : Int
- positions : List<Cell>
- orientation : Orientation (HORIZONTAL or VERTICAL)

Computed Properties:
- isEliminated : Boolean

Responsibility:
Represents a gecko that is placed on the board and can be eliminated.

For the eventual game we would likely make a preset list of Gecko's like how the set of ships is mostly the same in each version of battleships. 
Also only allows straight lines of a certain lenght for now, no 2d options like an L shape.

------------------------------------------------------------

Player
------
Attributes:
- name : String
- board : Board

Responsibility:
Holds the player's own board and interacts with the game.

------------------------------------------------------------

Game
----
Attributes:
- player1 : Player
- player2 : Player
- currentPlayer : Player

Functions:
- takeTurn(row, col)
- checkWinner()
- switchTurn()

Responsibility:
Controls gameplay flow, turns, and victory conditions.

************************************************************
* How to use the gameplay functions                        *
************************************************************
Main activity will create a game and 2 players in onCreate. When the players are created they will automatically create a personal board. 
The board will have a base size of 10 by 10 but can easily be altered inside the board class.

In order to place a gecko you can call player1.board.placeGecko(<nameOfGecko>)
Then once the game starts a cell can be shot with game.takeTurn(row,col) "shooting" that cell and returning a boolean for hit (true for hit ofcourse).
After each turn the game.checkWinner() will check if either of the players board is wiped out and then return the winning player. If no players have won it will return null instead.

************************************************************
* Future Improvements                                      *
************************************************************
- AI opponent
- Save/load game state
- Multiplayer support
- Additional gecko types and special abilities
