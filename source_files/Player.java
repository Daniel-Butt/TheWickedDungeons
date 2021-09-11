/*
player object, the player... you :3

Object -> Entity -> Player
*/

//importing all required libraries
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class Player extends Entity
{
  //sprite type
  public int type = 0;
  //which weapon the player has
  public int weaponType = 0;
  //how much damage said weapon does
  public int weaponDamage = 1;
  //how long before you can attack again
  public int weaponCoolDownCounter = 0;
  //modifer from strength potions
  public int damageModifer = 0;
  //modifer from defence potions
  public int defenceModifer = 1;
  //all weapon sprites
  public Image[] weaponSprites;
  //current weapon sprite
  public Image weapon;
  //weapon position
  public Vector weaponPos = new Vector(0,0);
  //last amount the weapon was rotated by
  public double lastWeaponRotation = 0;
  //how many coins the player has
  public int coins = 0;
  //player's score
  public int score = 0;
  //how many enemies the player has slain
  public int slain = 0;
  //values passed to the engine class for displaying
  //how many potions of each type the player has
  //see Engine.playerController() for more info
  public int[] guiValues = new int[8];
  //how many of each type of potion the player has
  public int healthPotions = 0;
  public int bigHealthPotions = 0;
  public int strengthPotions = 0;
  public int bigStrengthPotions = 0;
  public int defencePotions = 0;
  public int bigDefencePotions = 0;
  public int speedPotions = 0;
  public int bigSpeedPotions = 0;

  //constuctor
  public Player(int x, int y, int width, int height)
  {
    //calls object constuctor
    super(x, y, width, height);
    //set max movement speed
    maxSpeed = 6;
  }

  public void update(Engine engine)
  {
    //check if the player is interacting with interactables
    //only returns true if the player walked through a door
    if(engine.isInteracting(this))
    {
      //return to Engine game loop
      return;
    }

    //update GUI values
    guiValues[0] = bigSpeedPotions;
    guiValues[1] = bigStrengthPotions;
    guiValues[2] = bigHealthPotions;
    guiValues[3] = bigDefencePotions;
    guiValues[4] = speedPotions;
    guiValues[5] = strengthPotions;
    guiValues[6] = healthPotions;
    guiValues[7] = defencePotions;

    //check if player is colliding with enemy
    engine.isCollidingWithEnemy(this);

    //if player has been hit
    if(damagedCounter > 0)
    {
      state = "hit";
      damagedCounter--;
      if(damagedCounter == 0)
      {
        state = "temp";
      }
    }


    //call entity update method
    super.update(engine);

    //update with weapon the player has
    switch(weaponType)
    {
      case 0:
        weapon = weaponSprites[14].getScaledInstance(15, 66, Image.SCALE_DEFAULT);
        weaponDamage = 1 + damageModifer;
        break;
      case 1:
        weapon = weaponSprites[5].getScaledInstance(27, 90, Image.SCALE_DEFAULT);
        weaponDamage = 2 + damageModifer;
        break;
      case 2:
        weapon = weaponSprites[0].getScaledInstance(36, 90, Image.SCALE_DEFAULT);
        weaponDamage = 3 + damageModifer;
        break;
    }

    //rotate the weapon around the player based on the mouses loaction
    //middle of the screen position
    int startX = engine.screenWidth / 2;
    int startY = engine.screenHeight / 2;
    //current mosue position
    int mouseX = engine.ui.mouseX;
    int mouseY = engine.ui.mouseY;
    //rotation amount
    double rotationAngle;

    //if the mouse's x postion is same as the centre of the screen
    //rotate only based on the mouse's y position
    if(mouseX == startX)
    {
      if(mouseY > startY)
      {
        rotationAngle = Math.PI;
      }
      else
      {
        rotationAngle = 0;
      }
    }
    //otherwise calculate rotation normally
    else
    {
      //angle is equal to tan^-1(the distance from mouseX to startX / the distance from mouseY to startY)
      // - Math.PI/2 (90 degrees) to account for the standard rotation
      rotationAngle = Math.atan(((double)(mouseY - startY)/(double)(mouseX - startX))) - Math.PI / 2;
      if(mouseX > startX)
      {
        rotationAngle += Math.PI;
      }
    }

    //if the weapon is moved more than Math.PI / 8 (22.5 degress),
    //play a sword swing sound effect
    if(Math.abs(rotationAngle - lastWeaponRotation) > Math.PI / 8)
    {
      engine.playClip(engine.clips[6]);
    }

    //update las rotation angle
    lastWeaponRotation = rotationAngle;

    //create a bufferedImage version of the weapon sprite
    //and rotate it based on the angle calculated above
    BufferedImage weaponCopy = engine.rotateImg(weapon, rotationAngle);

    //calculate the weapons positon around the player
    weaponPos.x = (int) (pos.x + 70 * Math.sin(rotationAngle));
    weaponPos.y = (int) (pos.y - 70 * Math.cos(rotationAngle) + 20);

    //chech if the player can attack
    if(weaponCoolDownCounter == 0)
    {
      //check if the weapon is colliding with an enemy
      //see isCollidingWithWeapon() in the Engine class
      Enemy enemy = engine.isCollidingWithWeapon(new Object(weaponPos.x - 1,
                                                            weaponPos.y - 1,
                                                            weaponCopy.getWidth() + 1,
                                                            weaponCopy.getHeight() + 1));
      //if the weapon is colliding with an enemy
      if(enemy != null)
      {
        //deal damage to the enemy
        enemy.health -= weaponDamage;
        enemy.damagedCounter = 30;
        weaponCoolDownCounter = 30;
      }
    }
    else
    {
      //otherwise the weapon is cooling down
      weaponCoolDownCounter--;
      //change the hue of the weapon to red
      for(int i = 0; i < weaponCopy.getWidth(); i++)
      {
        for(int j = 0; j < weaponCopy.getHeight(); j++)
        {
          Color c = new Color(weaponCopy.getRGB(i,j), true);
          c = new Color(255, c.getGreen(), c.getBlue(), c.getAlpha());
          weaponCopy.setRGB(i, j, c.getRGB());
        }
      }
    }

    //update the weapon sprite based on the above
    weapon = (Image) weaponCopy;

  }

  //render the player
  public void render(Graphics g)
  {
    //call Entity render()
    super.render(g);
    //draw weapon
    g.drawImage(weapon, weaponPos.x, weaponPos.y, null);
  }

  //if debug is enabled
  public void debug(Graphics g)
  {
    //call Object debug
    super.debug(g);
    //draw hit box around weapon
    if(weaponPos != null && weapon != null)
    {
      g.setColor(Color.ORANGE);
      g.drawRect(weaponPos.x, weaponPos.y, weapon.getWidth(null), weapon.getHeight(null));
    }
  }
}
