/*
interactable object, objects the player can interact with

Object -> Interactable
*/

public class Interactable extends Object
{
  //if the object is still able to be used by the player
  public boolean usable = true;
  //if the object should be removed after its used
  public boolean remove = true;

  //constructor
  Interactable(int x, int y, int width, int height)
  {
    //calls Objects constuctor
    super(x, y, width, height);
    solid = false;
  }

  //blank slate for what to do if the object is used
  public void use(Player player){}
}
