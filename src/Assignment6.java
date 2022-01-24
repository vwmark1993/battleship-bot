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

/**
 * Starting code for COMP10205 - Assignment#6
 * @author mark.yendt
 */

public class Assignment6 {

   static final int NUMBER_OF_GAMES = 10000;
   // static final int NUMBER_OF_GAMES = 1; // For debugging.

   public static void startingSolution() {
    int totalShots = 0;
    System.out.println(BattleShip.version());
    long startTime = System.nanoTime();
    for (int game = 0; game < NUMBER_OF_GAMES; game++) {

      BattleShip battleShip = new BattleShip();
      SampleBot sampleBot = new SampleBot(battleShip);

      // Call SampleBot Fire randomly - You need to make this better!
      while (!battleShip.allSunk()) {
        sampleBot.fireShot();
      }
      int gameShots = battleShip.totalShotsTaken();
      totalShots += gameShots;
    }
    // Must leave on screen
    long stopTime = System.nanoTime();
    System.out.printf("\nMy solution requires %d ms to solve.\n", (stopTime - startTime) / 1_000_000);

    System.out.printf("SampleBot - The Average # of Shots required in %d games to sink all Ships = %.2f\n", NUMBER_OF_GAMES, (double)totalShots / NUMBER_OF_GAMES);
    
  }
  public static void main(String[] args)
  {
    startingSolution();
  }
}
