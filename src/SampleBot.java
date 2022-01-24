/**
 * Group Members:
 * Vincent Mark, 000803494
 * Allan Le, 000804364
 * Mark Kawucha, 000816619
 *
 * Statement of Authorship:
 * Our group certifies that all work submitted is our own; we have not copied it from any other source. We also certify that we have not allowed our work to be copied by others.
 */

import battleship.BattleShip;
import battleship.CellState;
import java.awt.Point;
import java.util.Random;

/**
 * A Sample random shooter - Takes no precaution on double shooting and has no strategy once
 * a ship is hit.
 *
 * @author mark.yendt
 */
public class SampleBot {
    private int gameSize;
    private BattleShip battleShip;
    private Random random;
    private CellState[][] map; // Store the coordinates of every shot in a 2D array.
    private int[][] probabilityDistribution; // A 2D histogram which stores the probability scores of the entire game board.
    Point temp; // Used to store coordinates of a Point for special-case situations.

    /**
     * Constructor keeps a copy of the BattleShip instance
     *
     * @param b previously created battleship instance - should be a new game
     */
    public SampleBot(BattleShip b) {
        battleShip = b;
        gameSize = b.boardSize;
        random = new Random();   // Needed for random shooter - not required for more systematic approaches
        probabilityDistribution = new int[gameSize][gameSize];
        map = new CellState[gameSize][gameSize];

        for (int y = 0; y < gameSize; y++) {
            for (int x = 0; x < gameSize; x++) {
                map[y][x] = CellState.Empty;
            }
        }
    }

    /**
     * Shoots at a space on the board and calls the battleship shoot method.
     * HUNT mode - uses probability distribution to determine where to place the shot.
     * Shoots until it discovers a battleship, in which case it will enter SINK mode.
     * SINK mode fires at the adjacent coordinates of the discovered battleship by calling an overloaded method to fire at specific coordinates.
     * The two while loops represent different directions of fire - horizontal and vertical.
     * If the algorithm decides it needs to change direction, it breaks out of the first loop to begin the second loop.
     * It first tries to shoot horizontally and then vertically.
     *
     * @return True if a battleship is hit, false otherwise.
     */
    // HUNT mode - randomly hunt for a ship.
    public boolean fireShot() {
        boolean hit = false;

        // Main loop. Continues until all five battleships are sunk.
        // The algorithm defaults to HUNT mode.
        // Enters SINK mode when a battleship is discovered.
        while (true) {
            boolean reset = false; // Used when a battleship has been sunk.
            boolean changeDirection = false; // Used when the algorithm needs to shoot in a different direction during SINK mode.

            // Probability distribution algorithm runs during HUNT mode.
            // It re-calculates the probability distribution after every shot.
            calculateProbabilityDistribution();

            // Use probability distribution to retrieve the coordinates of the shot.
            Point shot = getCoordinates();
            int x = (int)shot.getX();
            int y = (int)shot.getY();

            // HUNT mode starts here.
            // Only shoots at empty spots on the map.
            if (map[y][x] == CellState.Empty) {
                int numberOfShipsSunk = battleShip.numberOfShipsSunk(); // Keep track of the sunken ships for later logic checks.

                hit = battleShip.shoot(shot); // Calls battleship method to count the shots.

                // Once a battleship is found it will switch to SINK mode - continuously fires at adjacent spaces until either the battleship sinks or the available spaces are exhausted.
                // Each while loop represents a direction of fire (horizontal and vertical).
                // As such, breaking out of one loop will cause the algorithm to start firing in a different direction via the next loop.
                if (hit) {
                    // Record the hit.
                    map[y][x] = CellState.Hit;

                    // Record the initial hit of SINK mode to later use for special-case situations.
                    temp = new Point(x, y);

                    // Fire horizontally (East by default).
                    while ((hit && x != gameSize - 1)) {
                        x++;
                        numberOfShipsSunk = battleShip.numberOfShipsSunk();

                        // Only shoot at empty spots on the map.
                        // If the space is not empty, switch directions.
                        if (map[y][x] == CellState.Empty) {
                            hit = fireShot(x, y);

                            // A battleship has been sunk.
                            if (numberOfShipsSunk < battleShip.numberOfShipsSunk()) {

                                // Go back to HUNT mode.
                                reset = true;
                                break;

                                // The detected battleship has not been sunk - start firing in the opposite direction.
                            } else if (map[y][x] == CellState.Miss || x == gameSize - 1 || map[y][x + 1] == CellState.Miss) {
                                // Re-adjust direction.
                                x--;
                                if (x != 0) {
                                    while (x != 0 && (map[y][x] == CellState.Hit || map[y][x] == CellState.Empty)) {
                                        x--;
                                        // Skip to where the next empty spot is.
                                        if (map[y][x] == CellState.Empty) {
                                            hit = fireShot(x, y);

                                            // A battleship has been sunk.
                                            if (numberOfShipsSunk < battleShip.numberOfShipsSunk()) {

                                                // Go back to HUNT mode.
                                                reset = true;
                                                break;

                                                // The detected battleship has not been sunk - need to switch directions.
                                            } else if (map[y][x] == CellState.Miss) {
                                                // Escape out of the loop to switch direction.
                                                changeDirection = true;
                                                x++;
                                                break;
                                            }
                                        } else {
                                            continue;
                                        }
                                    }

                                    if (changeDirection) {
                                        break;
                                    }
                                }
                            }
                            // Break out of the current loop and start shooting vertically.
                        } else {
                            break;
                        }
                    }

                    // Hard reset when a battleship is sunk.
                    if (reset) {
                        return hit;
                    }

                    // Fire vertically (South by default).
                    while ((hit && y != gameSize - 1) || (changeDirection && y != gameSize - 1)) {
                        y++;

                        numberOfShipsSunk = battleShip.numberOfShipsSunk();

                        // Only shoot at empty spots on the map.
                        // If it cannot fire vertically, the loop cycle ends and HUNT mode is restarted.
                        if (map[y][x] == CellState.Empty) {
                            hit = fireShot(x, y);

                            // A battleship has been sunk.
                            if (numberOfShipsSunk < battleShip.numberOfShipsSunk()) {

                                // Go back to HUNT mode.
                                reset = true;
                                break;

                                // The detected battleship has not been sunk - start firing in the opposite direction.
                            } else if (map[y][x] == CellState.Miss || y == gameSize - 1 || map[y + 1][x] == CellState.Miss) {
                                // Re-adjust direction.
                                y--;
                                if (y != 0) {
                                    while (y != 0 && (map[y][x] == CellState.Hit || map[y][x] == CellState.Empty)) {
                                        y--;
                                        // Skip to where the next empty spot is.
                                        if (map[y][x] == CellState.Empty) {
                                            hit = fireShot(x, y);

                                        } else {
                                            continue;
                                        }
                                    }
                                }
                            }
                        } else {
                            break;
                        }
                        // Once the available empty spaces are exhausted in the vertical direction, SINK mode ends and the algorithm switches back to HUNT mode.
                    }
                    // The shot was a miss.
                } else {
                    // Record the miss.
                    map[y][x] = CellState.Miss;
                }
                return hit;
            }
        }
    }

    /**
     * Overloaded method.
     * SINK mode - fire at specific coordinates once a ship has been found.
     * Calls the battleship shoot method.
     *
     * @param x The x-coordinate of a space on the board.
     * @param y The y-coordinate of a space on the board.
     * @return True if a battleship is hit, false otherwise.
     */
    public boolean fireShot(int x, int y) {
        boolean hit = false;

        Point shot = new Point(x, y);

        hit = battleShip.shoot(shot); // Call the battleship method to count the shots.

        // Record the hit.
        if (hit) {
            map[y][x] = CellState.Hit;
        } else {
            map[y][x] = CellState.Miss;
        }

        return hit;
    }

    /**
     * Calculates the probability distribution for the battleship map.
     * Determines all possible battleship locations and assigns probability scores to each space.
     * Accounts for adjacent hits and special cases by providing appropriate probability scores to those specific situations.
     * Tallies up the probability scores in the 'probabilityDistribution' histogram.
     * The main idea is that the more times a space can be occupied by a possible ship, the higher its probability score is.
     */
    public void calculateProbabilityDistribution() {
        // The probability distribution must be re-calculated after each shot.
        probabilityDistribution = new int[gameSize][gameSize];

        // Scans through the battleship map and calculates all of the possible coordinates a battleship may be located.
        // Check for the different types of battleship.
        for (int shipNumber = 0; shipNumber < battleShip.shipSizes().length; shipNumber++) {
            // Start with the smallest battleship of length = 2.
            int shipLength = battleShip.shipSizes()[shipNumber];

            // Traverse the y-axis.
            for (int y = 0; y < gameSize; y++) {
                // Traverse the x-axis.
                for (int x = 0; x < gameSize; x++) {
                    // Check if each battleship can fit into the available space, horizontally and vertically.
                    // At each point, it scans 'i' spaces ahead and checks if either the battleship goes off the map or is obstructed by a non-empty space.
                    // If there is an obstruction, then the location is deemed to not contain a battleship and it moves onto the next location.
                    // If there is no obstruction, then the location can potentially contain a battleship and a probability score is assigned to the adjacent spaces.

                    // Horizontal orientation.
                    for (int i = 0; i < shipLength; i++) {
                        // Check if the ship goes off the map and isn't obstructed by non-empty spaces.
                        if ((i + x) >= gameSize || map[y][i + x] != CellState.Empty) {
                            break;
                        }

                        // Increment the probability score of the space if a ship can be placed.
                        if (i == shipLength - 1) {
                            for (int j = 0; j < shipLength; j++) {
                                probabilityDistribution[y][j + x]++;
                            }
                        }
                    }

                    // Vertical orientation.
                    for (int i = 0; i < shipLength; i++) {
                        // Check if the ship goes off the map and isn't obstructed by non-empty spaces.
                        if ((i + y) >= gameSize || map[i + y][x] != CellState.Empty) {
                            break;
                        }

                        // Increment the probability score of the space if a ship can be placed.
                        if (i == shipLength - 1) {
                            for (int j = 0; j < shipLength; j++) {
                                probabilityDistribution[j + y][x]++;
                            }
                        }
                    }
                }
            }
        }

        // Check for spaces adjacent to hits and assign probability scores to them.
        for (int y = 0; y < gameSize; y++) {
            for (int x = 0; x < gameSize; x++) {
                if (x < gameSize - 1 && map[y][x] == CellState.Empty && map[y][x + 1] == CellState.Hit) {
                    probabilityDistribution[y][x] += 10;
                }
                if (x > 0 && map[y][x] == CellState.Empty && map[y][x - 1] == CellState.Hit) {
                    probabilityDistribution[y][x] += 10;
                }
                if (y < gameSize - 1 && map[y][x] == CellState.Empty && map[y + 1][x] == CellState.Hit) {
                    probabilityDistribution[y][x] += 10;
                }
                if (y > 0 && map[y][x] == CellState.Empty && map[y - 1][x] == CellState.Hit) {
                    probabilityDistribution[y][x] += 10;
                }
            }
        }

        // Special Case: check for a specific location where an adjacent battleship might be located one space above the initial hit of a discovered battleship.
        if (temp != null && (int)temp.getY() - 1 > 0 && map[(int)temp.getY() - 1][(int)temp.getX()] == CellState.Empty) {
            probabilityDistribution[(int)temp.getY() - 1][(int)temp.getX()] += 35;
        }

        // Special Case: check for a specific location where an adjacent battleship might be located one space below the initial hit of a discovered battleship.
        if (temp != null && (int)temp.getY() + 1 < 10 && map[(int)temp.getY() + 1][(int)temp.getX()] == CellState.Empty) {
            probabilityDistribution[(int)temp.getY() + 1][(int)temp.getX()] += 35;
        }
    }

    /**
     * Retrieve the coordinates for a shot using probability distribution.
     *
     * @return A coordinate in the form of a Point object.
     */
    public Point getCoordinates() {
        int xCoordinate = 0;
        int yCoordinate = 0;

        // Scan the probability distribution histogram and retrieve the space with the highest probability score.
        for (int y = 0; y < gameSize; y++) {
            for (int x = 0; x < gameSize; x++) {
                if (probabilityDistribution[y][x] > probabilityDistribution[yCoordinate][xCoordinate]) {
                    xCoordinate = x;
                    yCoordinate = y;
                }
            }
        }

        // Choose random coordinates if the probability distribution becomes too spread thin.
        if (xCoordinate < 2 && yCoordinate < 2) {
            xCoordinate = random.nextInt(gameSize);
            yCoordinate = random.nextInt(gameSize);
        }

        return new Point(xCoordinate, yCoordinate);
    }

    /**
     * Prints out the CellState map of the game board.
     * Used for debugging.
     */
    public void printMap() {
        System.out.println("-------------------");
        for (int y = 0; y < gameSize; y++) {
            for (int x = 0; x < gameSize; x++) {
                System.out.print(map[y][x] + " ");
            }
            System.out.println();
        }
    }

    /**
     * Prints out the probability distribution histogram map of the game board.
     * Used for debugging.
     */
    public void printProbabilityDistribution() {
        System.out.println("-------------------");
        for (int y = 0; y < gameSize; y++) {
            for (int x = 0; x < gameSize; x++) {
                System.out.printf("%2d ", probabilityDistribution[y][x]);
            }
            System.out.println();
        }
    }
}
