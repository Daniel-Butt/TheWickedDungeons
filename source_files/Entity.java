/*
entity object, objects that can move / be killed

Object -> Entity
*/

//importing all required libraries
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class Entity extends Object
{
  //current health
  public int health = 6;
  //total health
  public int totalHealth = 6;
  //velocity
  public Vector vel;
  //max movement speed
  public int maxSpeed = 1;
  //run animation sprites
  public Image[] run;
  public Image[] runFlipped;
  //hit sprites
  private Image hit;
  private Image hitFlipped;
  //render animation state
  public String state = "idle";
  //direction the entity is facing
  private int direction = 0;
  //counter for how long to display hit sprite after
  //entity is damaged
  public int damagedCounter = 0;

  //constuctor
  public Entity(int x, int y, int width, int height)
  {
    //object constructor
    super(x ,y, width, height);
    //initializing velocity
    vel = new Vector(0,0);

  }

  //set run animation sprites
  //see Object class setIdleSprites() for more info
  public void setRunSprites(Image[] images)
  {
    run = images;
    runFlipped = run.clone();
    for(int i = 0; i < runFlipped.length; i++)
    {
      AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
      tx.translate(-runFlipped[i].getWidth(null), 0);
      AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
      BufferedImage image = new BufferedImage(
        runFlipped[i].getWidth(null), runFlipped[i].getHeight(null),
        BufferedImage.TYPE_INT_ARGB);
      Graphics g = image.createGraphics();
      g.drawImage(runFlipped[i], 0, 0, null);
      g.dispose();
      runFlipped[i] = op.filter(image, null);
    }
  }

  //set hit sprites
  //see Object class setIdleSprites() for more info
  public void setHitSprite(Image img)
  {
    BufferedImage image = new BufferedImage(
      img.getWidth(null), img.getHeight(null),
      BufferedImage.TYPE_INT_ARGB);
    Graphics g = image.createGraphics();
    g.drawImage(img, 0, 0, null);
    g.dispose();

    for(int i = 0; i < image.getWidth(); i++)
    {
      for(int j = 0; j < image.getHeight(); j++)
      {
        Color c = new Color(image.getRGB(i,j), true);
        c = new Color(255, c.getGreen(), c.getBlue(), c.getAlpha());
        image.setRGB(i, j, c.getRGB());
      }
    }

    hit = (Image) image;

    hitFlipped = (Image) image;

    AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
    tx.translate(-hitFlipped.getWidth(null), 0);
    AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    hitFlipped = op.filter(image, null);
  }

  //render entity
  public void render(Graphics g)
  {
    //renders based on state
    switch(state)
    {
      case("idle"):
        if(direction == 0)
        {
          g.drawImage(idle[animationFrame], pos.x, pos.y, null);
        }
        else
        {
          g.drawImage(idleFlipped[animationFrame], pos.x, pos.y, null);
        }
        break;
      case("running"):
        if(direction == 0)
        {
          g.drawImage(run[animationFrame], pos.x, pos.y, null);
        }
        else
        {
          g.drawImage(runFlipped[animationFrame], pos.x, pos.y, null);
        }
        break;
      case("hit"):
        if(hit != null)
        {
          if(direction == 0)
          {
            g.drawImage(hit, pos.x, pos.y, null);
          }
          else
          {
            g.drawImage(hitFlipped, pos.x, pos.y, null);
          }
        }
        break;
    }

  }

  //update entity movement / animation frame / state
  public void update(Engine engine)
  {
    //ensure velocity < max speed
    if(Math.abs(vel.x) > maxSpeed)
    {
      vel.x = maxSpeed * (int)Math.signum(vel.x);
    }
    if(Math.abs(vel.y) > maxSpeed)
    {
      vel.y = maxSpeed * (int)Math.signum(vel.y);
    }

    //movement, the following are very similar just different depending
    //on which direction the entity is travelling

    if(vel.x > 0)//moving right
    {

      //increase the entity's position one at
      //checking collision after each movement
      for(int i = 0; i < vel.x; i++)
      {
        //increase postion by one
        pos.x += Math.signum(vel.x);

        //dynamically update the entity's positon
        //in the collision grid since it has moved
        engine.collisionGrid.update((Object)this);

        //check if the entity is colliding
        if(engine.isColliding(this))
        {
          //if so move back
          pos.x -= Math.signum(vel.x);
          //veloctiy becomes zero
          vel.x = 0;
        }
      }
      //dynamically update the entity's positon
      //in the collision grid since its finsihed moving
      engine.collisionGrid.update((Object)this);
    }
    else if (vel.x < 0) //left
    {

      for(int i = 0; i < Math.abs(vel.x); i++)
      {
        pos.x += Math.signum(vel.x);

        engine.collisionGrid.update((Object)this);

        if(engine.isColliding(this))
        {
          pos.x -= Math.signum(vel.x);
          vel.x = 0;

        }
      }
      engine.collisionGrid.update((Object)this);
    }
    if(vel.y > 0) //down
    {

      for(int i = 0; i < vel.y; i++)
      {

        pos.y += Math.signum(vel.y);

        engine.collisionGrid.update((Object)this);

        if(engine.isColliding(this))
        {
          pos.y -= Math.signum(vel.y);
          vel.y = 0;
        }
      }

      engine.collisionGrid.update((Object)this);
    }
    else if (vel.y < 0) //up
    {

      for(int i = 0; i < Math.abs(vel.y); i++)
      {
        pos.y += Math.signum(vel.y);

        engine.collisionGrid.update((Object)this);

        if(engine.isColliding(this))
        {
          pos.y -= Math.signum(vel.y);
          vel.y = 0;
        }
      }

      engine.collisionGrid.update((Object)this);
    }

    //update animation frame
    if(idle != null && run != null)
    {
      switch(state)
      {
        case("idle"):
          //update the animations frame every 9 game cycles
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
          break;
        case("running"):
          if(frameCounter == 9)
          {
            if(animationFrame == run.length - 1)
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
          break;
      }
    }

    //switch direction based on movement
    if(vel.x > 0)
    {
      direction = 0;
    }
    else if(vel.x < 0)
    {
      direction = 1;
    }

    //switch state between idle and run if the entity's
    //velocity > 0
    if(!state.equals("hit"))
    {
      if(vel.y == 0 && vel.x == 0)
      {
        if(!state.equals("idle"))
        {
          state = "idle";
          animationFrame = 0;
          frameCounter = 0;
        }
      }
      else
      {
        if(!state.equals("running"))
        {
          state = "running";
          animationFrame = 0;
          frameCounter = 0;
        }
      }
    }
  }

}
