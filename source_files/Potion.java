/*
potion object, player can use for effects

Object -> Interactable -> potions
*/

public class Potion extends Interactable
{
  //type of potion
  private int type;

  //constuctor
  Potion(int x, int y, int width, int height, int t)
  {
    //calls Interactable constuctor
    super(x, y, width, height);
    //set type
    type = t;
  }

  //if the potion is used by the player
  public void use(Player player)
  {
    //it can no longer be used and is removed
    usable = false;
    //give the player the specific potion based on the type
    switch(type)
    {
      case 0:
        player.healthPotions++;
        break;
      case 1:
        player.strengthPotions++;
        break;
      case 2:
        player.speedPotions++;
        break;
      case 3:
        player.defencePotions++;
        break;
      case 4:
        player.bigHealthPotions++;
        break;
      case 5:
        player.bigStrengthPotions++;
        break;
      case 6:
        player.bigSpeedPotions++;
        break;
      case 7:
        player.bigDefencePotions++;
        break;
    }
  }

}
