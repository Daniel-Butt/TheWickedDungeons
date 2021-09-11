/*
door object

Object -> interactable -> Door
*/

//importing all required libraries
import java.awt.Graphics;

public class Door extends Interactable
{
  //if the door can be opened or not
  //false for all doors other than the boss door
  public boolean locked = false;

  //constuctor
  Door(int x, int y, int width, int height)
  {
    //call Interactable constuctor
    super(x, y, width, height);
    //dont remove when used
    remove = false;
  }

  //if play opens the door
  public void use(Player player)
  {
    if(!locked)
    {
      //give player score
      player.score += 200;
      //can't open / close door
      usable = false;
    }
  }

  //render door
  public void render(Graphics g)
  {
    //if unopened
    if(usable)
    {
      g.drawImage(sprite, pos.x, pos.y, null);
    }
    //if opened
    else
    {
      g.drawImage(idle[0], pos.x, pos.y, null);
    }
  }
}
