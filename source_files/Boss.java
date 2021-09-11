/*
boss object

Object -> Entity -> Enemy -> Boss
*/

//importing all required libraries
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class Boss extends Enemy
{
  //timer between shooting special attack
  int shootTimer = 600;
  //counter for how many times it has spawned minions
  int spawnMinionsCounter = 0;
  //if the hue of the boss has been changed nor not\
  //for a special attack
  boolean changedHue = false;

  //constuctor
  Boss(int x, int y, int width, int height)
  {
    super(x, y, width, height, 0);
  }

  public void update(Engine engine)
  {
    //if aggressive, fight player, otherwise wait till player
    //moves into aggro box;
    if(aggroed)
    {
      //decrease the shoot timer for when to shoot
      shootTimer--;

      //if there is ~1 second before it shoots, the boss turns
      // blue as a warning
      if(shootTimer < 60)
      {
        //to ensure hue is only changed once for every shoot
        if(!changedHue)
        {
          //ensure its state is idle
          state = "idle";
          vel = new Vector(0,0);

          //change hue to blueish
          changedHue = true;
          for(int n = 0; n < idle.length; n++)
          {
            BufferedImage image = new BufferedImage(
              idle[n].getWidth(null), idle[n].getHeight(null),
              BufferedImage.TYPE_INT_ARGB);
            Graphics g = image.createGraphics();
            g.drawImage(idle[n], 0, 0, null);
            g.dispose();

            for(int i = 0; i < image.getWidth(); i++)
            {
              for(int j = 0; j < image.getHeight(); j++)
              {
                Color c = new Color(image.getRGB(i,j), true);
                c = new Color(c.getRed(), c.getGreen(), 255, c.getAlpha());
                image.setRGB(i, j, c.getRGB());
              }
            }
            idle[n] = (Image) image;
          }
          for(int n = 0; n < idleFlipped.length; n++)
          {
            BufferedImage image = new BufferedImage(
              idleFlipped[n].getWidth(null), idleFlipped[n].getHeight(null),
              BufferedImage.TYPE_INT_ARGB);
            Graphics g = image.createGraphics();
            g.drawImage(idleFlipped[n], 0, 0, null);
            g.dispose();

            for(int i = 0; i < image.getWidth(); i++)
            {
              for(int j = 0; j < image.getHeight(); j++)
              {
                Color c = new Color(image.getRGB(i,j), true);
                c = new Color(c.getRed(), c.getGreen(), 255, c.getAlpha());
                image.setRGB(i, j, c.getRGB());
              }
            }
            idleFlipped[n] = (Image) image;
          }
        }
      }
      //if the bosses health drop below certain points
      //it spawns two minions to attack the player
      //see Engine.startLevel() for more info
      else if((health < 75 && spawnMinionsCounter == 0) ||
              (health < 50 && spawnMinionsCounter == 1) ||
              (health < 25 && spawnMinionsCounter == 2))
      {
        spawnMinionsCounter++;

        Enemy enem;
        Image[] sprites;

        enem = new Enemy(960, 1380, 60, 90, 0);
        enem.maxSpeed = 5;
        enem.health = 3;
        enem.totalHealth = 3;
        enem.attackDamage = 2;
        enem.aggroed = true;
        sprites = new Image[4];
        sprites[0] = engine.assets[25].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
        sprites[1] = engine.assets[26].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
        sprites[2] = engine.assets[27].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
        sprites[3] = engine.assets[28].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
        enem.setIdleSprites(sprites);
        sprites = new Image[4];
        sprites[0] = engine.assets[29].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
        sprites[1] = engine.assets[30].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
        sprites[2] = engine.assets[31].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
        sprites[3] = engine.assets[32].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
        enem.setRunSprites(sprites);
        enem.setHitSprite(engine.assets[28].getScaledInstance(60, 90, Image.SCALE_DEFAULT));
        engine.objects.add(engine.playerIndex - 1, enem);

        enem = new Enemy(1440, 300, 60, 90, 0);
        enem.maxSpeed = 5;
        enem.health = 3;
        enem.totalHealth = 3;
        enem.attackDamage = 2;
        enem.aggroed = true;
        enem.setRunSprites(sprites);
        sprites = new Image[4];
        sprites[0] = engine.assets[25].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
        sprites[1] = engine.assets[26].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
        sprites[2] = engine.assets[27].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
        sprites[3] = engine.assets[28].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
        enem.setIdleSprites(sprites);
        enem.setHitSprite(engine.assets[28].getScaledInstance(60, 90, Image.SCALE_DEFAULT));
        engine.objects.add(engine.playerIndex - 1, enem);

        engine.playerIndex += 2;
      }
      else
      {
        //update to chase the player
        //calls Enemy.update();
        super.update(engine);
      }
      //time to shoot
      if(shootTimer == 0)
      {
        shootTimer = 600;
        //change hue back to red
        Image[] sprites;
        sprites = new Image[4];
        sprites[0] = engine.assets[0].getScaledInstance(240, 270, Image.SCALE_DEFAULT);
        sprites[1] = engine.assets[1].getScaledInstance(240, 270, Image.SCALE_DEFAULT);
        sprites[2] = engine.assets[2].getScaledInstance(240, 270, Image.SCALE_DEFAULT);
        sprites[3] = engine.assets[3].getScaledInstance(240, 270, Image.SCALE_DEFAULT);
        setIdleSprites(sprites);
        changedHue = false;

        //shoot a bunch of projectiles in a circle around the boss
        //right
        shoot(new Vector(5, 0), engine);
        //top right corner
        shoot(new Vector(3, 4), engine);
        //up
        shoot(new Vector(0, 5), engine);
        //top left corner
        shoot(new Vector(-3, 4), engine);
        //left
        shoot(new Vector(-5, 0), engine);
        //bottom left corner
        shoot(new Vector(-3, -4), engine);
        //down
        shoot(new Vector(0, -5), engine);
        //bottom right corner
        shoot(new Vector(3, -4), engine);
      }
    }
    else
    {
      //if not aggressive, check if it should be aggressive
      //calls Enemy.update();
      super.update(engine);
    }
  }

  public void render(Graphics g)
  {
    //drawing the health bar

    //red background
    g.setColor(Color.RED);
    g.fillRect(pos.x + 60, pos.y - 20, 120, 20);

    //green section
    g.setColor(Color.GREEN);
    double healthBarLength = (double)health / (double)totalHealth * 120.0;
    g.fillRect(pos.x + 60, pos.y - 20, (int)healthBarLength, 20);

    //call Entity.render();
    super.render(g);
  }

}
