/*
coin object

Object -> interactable -> Coin
*/

public class Coin extends Interactable
{
  //constructor
  Coin(int x, int y, int width, int height)
  {
    //call Interacable constructor
    super(x, y, width, height);
  }

  //if the player picks it up
  public void use(Player player)
  {
    //increase the number of coins the player has
    player.coins++;
    //give player score
    player.score += 50;
    //set usable false and subsequently remove the coin
    usable = false;
  }
}
