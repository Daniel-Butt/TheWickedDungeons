/*
chest object

Object -> interactable -> Chest
*/

//importing all required libraries
import java.awt.*;

public class Chest extends Interactable
{
  //counter for when to change the open animation frame
  private int openCounter = 0;
  //open animation frame
  private int openFrame = 0;
  //type of reward
  private int type;
  //how long to display reward once open
  private int showRewardCounter = 0;
  //image of reward
  private Image rewardSprite;

  //constructor
  Chest(int x, int y, int width, int height, int type)
  {
    //calls interactable constuctor
    super(x, y, width, height);
    //dont remove once the player opens the chest
    remove = false;
    //get the reward type
    this.type = type;
  }

  //if the player open the chest
  public void use(Player player)
  {
    //increase score
    player.score += 200;
    //no longer usable
    usable = false;
  }

  //update chest
  public void update(Engine engine)
  {
    //if opened and still displaying open animation
    if((!usable) && openFrame < idle.length)
    {
      //if counter is at 30, change to the next open animation frame
      if(openCounter == 30)
      {
        sprite = idle[openFrame];
        openFrame++;

        //once opened give and display reward
        if (openFrame == idle.length)
        {
          Player player = (Player) engine.objects.get(engine.playerIndex);
          switch(type)
          {
            case 0:
              rewardSprite = engine.assets[33].getScaledInstance(30, 30, Image.SCALE_DEFAULT);
              player.coins += 6;
              player.score += 300;
              break;
            case 1:
              rewardSprite = engine.assets[68].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
              player.bigHealthPotions++;
              break;
            case 2:
              rewardSprite = engine.assets[279].getScaledInstance(18, 60, Image.SCALE_DEFAULT);
              player.weaponType = 1;
              break;
            case 3:
              rewardSprite = engine.assets[274].getScaledInstance(24, 60, Image.SCALE_DEFAULT);
              player.weaponType = 2;
              break;
            case 4:
              rewardSprite = engine.assets[113].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
              player.type = 6;
              //load selected character sprites
              Image[] playerIdle = new Image[4];
              playerIdle[0] = engine.assets[113].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
              playerIdle[1] = engine.assets[114].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
              playerIdle[2] = engine.assets[115].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
              playerIdle[3] = engine.assets[116].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
              Image[] playerRun = new Image[4];
              playerRun[0] = engine.assets[117].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
              playerRun[1] = engine.assets[118].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
              playerRun[2] = engine.assets[119].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
              playerRun[3] = engine.assets[120].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
              Image playerHit = engine.assets[112].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
              player.setIdleSprites(playerIdle);
              player.setRunSprites(playerRun);
              player.setHitSprite(playerHit);
              player.totalHealth = 10;
              player.health += 4;
              break;
          }
          showRewardCounter = 60;
        }

        openCounter = 0;
      }
      openCounter++;
    }

    //decrease show reward counter if active
    if(showRewardCounter > 0)
    {
      showRewardCounter--;
    }
  }

  //render chest / reward
  public void render(Graphics g)
  {
    //draw sprite
    g.drawImage(sprite, pos.x, pos.y, null);

    //if just opened, show reward
    if(showRewardCounter > 0)
    {
      g.drawImage(rewardSprite,
                  pos.x + w / 2 - rewardSprite.getWidth(null) / 2,
                  pos.y + h / 4 - rewardSprite.getHeight(null),
                  null);
    }
  }

}
