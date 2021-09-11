/*
projectile object, thrown by enemy

Object -> Entity -> Projectile
*/

//importing all required libraries
import java.awt.Graphics;

public class Projectile extends Entity
{
  //how much damage th projectile does
  int damage = 1;
  //if the projectile is inert, ie stuck in a wall
  boolean inert = false;

  //constuctor
  Projectile(int x, int y, int width, int height)
  {
    //calls Entity constuctor
    super(x, y, width, height);
  }

  //render the projectile
  public void render(Graphics g)
  {
    g.drawImage(sprite, pos.x, pos.y, null);
  }

}
