# Magic Maze AI Simulation Framework

## Overview
This project is a simulation framework for testing and analyzing AI player behavior for board game 
Magic Maze. It supports multiple AI player types and configurations, allowing for detailed analysis
of their performance and interactions.

## Project Structure
- **`src/main/java`**: Java source code for the game logic, AI players and UI.
- **`src/main/resources`**: Configuration files (e.g., `params.json`).
- **`analysis`**: Python scripts for analyzing game results.
- **`output`**: Directory for storing the AI game logs and results.
- **`output/human_gameplay`**: Transcriptions of real-life games played by human players.

## Requirements
### Java
- Java 17+
- Maven

### Python (for analysis)
- Python 3.8+
- Required libraries: `pandas`, `matplotlib`

## Setup
1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd <repository-folder>

2. Build the project using Maven:
    ```bash
    mvn clean install

## Configuration
Modify the `params.json` file in `src/main/resources` to set up different game parameters, such as:
    
```json
{
  "mode": "new",
  "gameName": "test",
  "replayRun": 1,
  "games": [
    {
    "numberOfPlayers": 4,
    "aiPlayers": [ "SHORT_FUSE", "SHORT_FUSE", "BASIC", "REACTIVE" ]
    }
  ]
}
```
- `mode`: Game mode ("new" or "replay").
- `gameName`: Name of the game session (creates/overwrites folder of that name).
- `replayRun`: Run number for replaying games.
- `games`: List of game configurations, including number of players and their AI types.
- `aiPlayers`: Types of AI players ("BASIC", "SHORT_FUSE", "REACTIVE" or "RANDOM").

For manually operated game, set `aiPlayers` to `[]`.

### Printing Game Logs
To enable printing game logs to the terminal, set the `PRINT_EVERYTHING` variable to `true` in `utils/Config.java`.

## Running the Simulation

To run the simulation, run `org.game.Main.java` in your IDE.
This will start the simulation based on the parameters defined in `params.json`.
The simulation opens a GUI window to visualize the game.
When the simulation completes, results will be stored in the `output/` directory in folder specified by `gameName` in `params.json`.

## Manually Operated Game
To play a manually operated game, set the `aiPlayers` array to empty in `params.json`:

```bash
"aiPlayers": []
```

This will allow you to control all players manually through the terminal. 
The GUI will still be displayed for visualization purposes.

In the terminal, use the following input format to control the pawns: `ca` or `ca1` 
where `c` is the color of the pawn, `a` is the action or direction 
and optional `1` indicates the number of vortex or discovery if applicable.

Values for `c` (color of pawn): 
- `y`: yellow
- `o`: orange
- `p`: purple
- `g`: green

Values for `a` (action or direction):
- `n`: north
- `s`: south
- `w`: west
- `e`: east
- `d`: discover
- `v`: vortex
- `x`: escalator

Examples:
- `yn`: Move yellow pawn north
- `os`: Move orange pawn south
- `px`: Use escalator with purple pawn
- `gd2`: Discover with green pawn card number 2
- `gd`: Discover with green pawn (default to next random card)
- `pv12`: Use vortex with purple pawn card number 12

