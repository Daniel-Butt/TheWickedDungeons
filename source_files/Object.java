/*
  All Game Objects start here
  see included docmentation for subclass flow chart
*/

//importing required libraries
import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class Object
{
  //position
  public Vector pos;
  //width / height
  public int w, h;
  //collision grids that the object is in
  public ArrayList<Vector> collisionArea = new ArrayList<Vector>();
  //if the object is solid or not
  public boolean solid = true;

  //main sprite
  public Image sprite;
  //idle animation spirites
  public Image[] idle;
  //same as above but flips horizontally
  public Image[] idleFlipped;
  //current animation frame for the above
  public int animationFrame = 0;
  //counter for when to update the animation frame
  public int frameCounter = 0;

  //constructor
  Object(int x, int y, int width, int height)
  {
    pos = new Vector(x,y);
    w = width;
    h = height;
  }

  //setting the idle sprites
  public void setIdleSprites(Image[] images)
  {
    //set idle and idleflipped to be the passed Image[]
    idle = images;
    idleFlipped = idle.clone();

    //flip each image in idleFlipped
    for(int i = 0; i < idleFlipped.length; i++)
    {
      //set up transform to flip horizontally
      AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
      tx.translate(-idleFlipped[i].getWidth(null), 0);
      AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);

      //create new buffered image version of orginal image
      BufferedImage image = new BufferedImage(
        idleFlipped[i].getWidth(null), idleFlipped[i].getHeight(null),
        BufferedImage.TYPE_INT_ARGB);
      Graphics g = image.createGraphics();
      g.drawImage(idleFlipped[i], 0, 0, null);
      g.dispose();

      //perform flip transformation
      idleFlipped[i] = op.filter(image, null);
    }
  }

  //set the objects sprite
  public void setSprite(Image image)
  {
    sprite = image;
  }

  //update the object
  public void update(Engine engine)
  {
    //update what frame to show for the idle animation, if the idle animation exists
    if(idle != null)
    {
      //every 9 update cycles change the animation frame
      if(frameCounter == 9)
      {
        if(animationFrame == idle.length - 1)
        {
          animationFrame = 0;
        }
        else
        {
          animationFrame++;
        }
        frameCounter = 0;
      }
      else
      {
        frameCounter++;
      }
    }
  }

  //render the object
  public void render(Graphics g)
  {
    //if idle animation exists, render the animation
    if(idle != null)
    {
      g.drawImage(idle[animationFrame], pos.x, pos.y, null);
    }
    //otherwise just render the mian sprite
    else
    {
      g.drawImage(sprite, pos.x, pos.y, null);
    }
  }

  //debug mode
  public void debug(Graphics g)
  {
    //if the object is solid
    if(solid)
    {
      //draw hit box
      g.setColor(new Color(247, 22, 154));
      g.drawRect(pos.x, pos.y, w, h);
    }
  }

}
