/*
enemy object

Object -> Entity -> Enemy
*/

//importing all required libraries
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Image;

public class Enemy extends Entity
{
  //attack damage of enemy
  public int attackDamage = 1;
  //counter for delay between attacks
  public int attackCounter = 0;
  //type of attack
  public int type;
  //temperary speed since enemies slow down after attacking
  public int tempSpeed;
  //if agressive or not, ie going after the player
  public boolean aggroed = false;
  //boundaries that if the player crosses, makes the enemy aggressive
  public Vector[] bounds = new Vector[2];

  //constructor
  Enemy(int x, int y, int width, int height, int type)
  {
    //calls Entity constructor
    super(x ,y, width, height);
    //set type of attack
    this.type = type;

    /*manually set when level is loaded
      attackDamage
      maxSpeed
      bounds
      health
      sprites
    */
  }

  //update enemy
  public void update(Engine engine)
  {
    //get player
    Player player = (Player) engine.objects.get(engine.playerIndex);

    //if recently attacked, slow down
    if(attackCounter > 0)
    {
      tempSpeed = 2;
      attackCounter--;
    }
    //otherwise speed is normal
    else
    {
      tempSpeed = maxSpeed;
    }

    //if not agressive check if player is between boundaries
    if(!aggroed)
    {
      aggroed = aggressive(player);
    }
    //if aggressive
    else
    {
      //different attack types
      switch(type)
      {
        //melee
        case 0:
          moveToIntercept(player);
          break;
        //ranged
        case 1:
          moveToShoot(player, engine);
          break;
      }

    }

    //if recently hit by player
    if(damagedCounter > 0)
    {
      state = "hit";
      //get knocked back
      vel.mult(-1);
      damagedCounter--;
      if(damagedCounter == 0)
      {
        state = "temp";
      }
    }

    //call Entity update (to move)
    super.update(engine);
  }

  //check aggro
  private boolean aggressive(Player player)
  {
    //check if player enter aggro bounds
    if(bounds != null)
    {
      if(player.pos.x + player.w > bounds[0].x &&
         player.pos.x < bounds[1].x &&
         player.pos.y + player.h > bounds[0].y &&
         player.pos.y < bounds[1].y)
      {
        return true;
      }
      return false;
    }
    else
    {
      System.out.println("no bounds on an enemy");
      return false;
    }
  }

  //move to intercept player
  public void moveToIntercept(Player player)
  {
    //find the difference between the player and enemy
    Vector v = new Vector(player.pos.x - pos.x, player.pos.y - pos.y);
    //set its magnitude to be the enemies speed
    v.setMagnitude(tempSpeed);
    //set enemies velocity to be the above
    vel = v;
  }

  //move to shoot at player
  public void moveToShoot(Player player, Engine engine)
  {
    //if the distance from the enemy to the player is less than 200 pixels
    if(Math.hypot(player.pos.x - pos.x, player.pos.y - (pos.y + w / 2)) < 200)
    {
      //turn into a melee enemy
      moveToIntercept(player);
      return;
    }

    Vector v;

    //if the enemy is more left/right of the player than above/below
    if(Math.abs(player.pos.x - pos.x) > Math.abs(player.pos.y - pos.y))
    {
      //move up/down to be on the same height
      //then throw a projectile every 300 updates
      v = new Vector(0, player.pos.y - pos.y);
      if(v.magnitude() > 10)
      {
        v.setMagnitude(maxSpeed);
        vel = v;
      }
      else
      {
        vel = new Vector(0,0);
      }

      if(attackCounter == 0)
      {
        attackCounter = 300;
        v = new Vector(player.pos.x - pos.x, 0);
        v.setMagnitude(4);
        shoot(v, engine);
      }
    }
    //if the enemy is more above/below of the player than left/right
    else
    {
      //move left/right to be on the same width
      //then throw a projectile every 300 updates
      v = new Vector(player.pos.x - pos.x, 0);
      if(v.magnitude() > 10)
      {
        v.setMagnitude(maxSpeed);
        vel = v;
      }
      else
      {
        vel = new Vector(0,0);
      }

      if(attackCounter == 0)
      {
        attackCounter = 300;
        v = new Vector(0, player.pos.y - pos.y);
        v.setMagnitude(4);
        shoot(v, engine);
      }
    }

  }

  //shoot projectile
  public void shoot(Vector vel, Engine engine)
  {
    //create projectile
    Projectile proj = new Projectile(pos.x + w / 2, pos.y + h / 2, 30, 30);
    //set velocity to specified velocity
    proj.vel = vel;
    //set max speed to specified max speed
    proj.maxSpeed = vel.magnitude();

    //get projectile image
    Image sprite = engine.assets[284].getScaledInstance(18, 39, Image.SCALE_DEFAULT);

    //determines which direction the projectile is travelling and
    //offsets its position to that it doesn't start off hitting
    //the enemy that shot it.
    //it also rotates the projectile Image by its direction

    //right
    if(vel.x > 0)
    {
      //bottom right
      if(vel.y > 0)
      {
        sprite = engine.rotateImg(sprite, Math.PI / 2 + Math.PI / 4);
        proj.pos.x += w;
        proj.pos.y += h;
      }
      //top right
      else if(vel.y < 0)
      {
        sprite = engine.rotateImg(sprite, Math.PI / 4);
        proj.pos.x += w;
        proj.pos.y -= h;
      }
      //right
      else
      {
        sprite = engine.rotateImg(sprite, Math.PI / 2);
        proj.pos.x += w;
      }
    }
    //left
    else if(vel.x < 0)
    {
      //bottom left
      if(vel.y > 0)
      {
        sprite = engine.rotateImg(sprite, Math.PI + Math.PI / 4);
        proj.pos.x -= w;
        proj.pos.y += h;
      }
      //top left
      else if(vel.y < 0)
      {
        sprite = engine.rotateImg(sprite, -Math.PI / 4);
        proj.pos.x -= w;
        proj.pos.y -= h;
      }
      //left
      else
      {
        sprite = engine.rotateImg(sprite, Math.PI * 1.5);
        proj.pos.x -= w;
      }
    }
    //down
    else if(vel.y > 0)
    {
      sprite = engine.rotateImg(sprite, Math.PI);
      proj.pos.y += h;
    }
    //up
    else
    {
      proj.pos.y -= h;
    }

    //set now possibly rotated sprite
    proj.setSprite(sprite);
    //add projectile, above player so you see it
    engine.objects.add(engine.playerIndex + 1, proj);

  }

  //draw the aggro boundaries
  public void debug(Graphics g)
  {
    if(!aggroed)
    {
      g.setColor(new Color(36, 232, 242));
      g.drawRect(bounds[0].x, bounds[0].y,
               bounds[1].x - bounds[0].x,
               bounds[1].y - bounds[0].y);
    }
    //call Entity debug
    super.debug(g);
  }







}
