/*
Optimises collision by dividing the game screen into a grid,
placing all collidable objects into the grid boxes that their
hit boxes are in, an only checking if objects in the same grid
boxes are colliding. Furthurmore, it can be dynamically updated
if objects are moved.
*/

//importing required libraries
import java.util.ArrayList;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;

public class CollisionGrid
{
  //array of arrays of arraylists
  public ArrayList<Object> grid[][];

  private int scale;
  private int max_rows;
  private int max_cols;

  //constructor
  CollisionGrid(int scale, int screenWidth, int screenHeight)
  {
    //kinda mixed up rows and cols but like whatever.
    //# of rows and cols
    max_rows = screenWidth / scale;
    max_cols = screenHeight / scale;
    //creating grid
    grid = new ArrayList[max_rows][max_cols];
    //setting grid scale
    this.scale = scale;
  }

  //empties grid
  public void reset()
  {
    //for each arraylist in grid, clear it
    for(int i = 0; i < max_rows; i++)
    {
      for(int j = 0; j < max_cols; j++)
      {
        if(grid[i][j] != null)
        {
          grid[i][j].clear();
          grid[i][j] = null;
        }
      }
    }
  }

  //insert an object into the grid
  public void insert(Object hitBox)
  {
    //ensuring the object inside the grid
    if(hitBox.pos.x + hitBox.w <= 0 || hitBox.pos.x >= scale * max_rows || hitBox.pos.y + hitBox.h <= 0 || hitBox.pos.y >= scale * max_cols)
    {
      return;
    }

    //index for the right side of hitbox
    float xMaxIndex = (float) (hitBox.pos.x + hitBox.w) / scale;
    //index for the left side of hitbox
    float xIndex = (float) hitBox.pos.x / scale;
    //index for the bottom side of hitbox
    float yMaxIndex = (float) (hitBox.pos.y + hitBox.h) / scale;
    //index for the top side of hitbox
    float yIndex = (float) hitBox.pos.y / scale;

    //placing the object into the respective grids between the above indexs
    for(int i = (int)Math.floor(xIndex); i < (int)Math.ceil(xMaxIndex); i++)
    {
      if(i > -1 && i < max_rows)
      {
        for(int j = (int)Math.floor(yIndex); j < (int)Math.ceil(yMaxIndex); j++)
        {
          if(j > -1 && j < max_cols)
          {
            //place the object into the respective grid
            add(hitBox, i, j);
          }
        }
      }
    }
  }

  //if debug mode is enabled
  public void debug(Graphics g)
  {
    //draw lines to show the grid
    g.setColor(Color.GREEN.darker().darker());
    for(int i = 0; i <= max_rows; i++)
    {
      g.drawLine(i*scale, 0, i*scale, scale*max_cols);
    }
    for(int i = 0; i <= max_cols; i++)
    {
      g.drawLine(0, i*scale, max_rows*scale, i*scale);
    }

    //draw numbers for the x, y location of each grid box
    g.setColor(Color.ORANGE);
    g.setFont(new Font("TimesRoman", Font.PLAIN, 11));

    for(int i = 0; i < max_rows; i++)
    {
      for(int j = 0; j < max_cols; j++)
      {
        g.drawString("" + i*scale, i*scale + 5, j*scale + 15);
        g.drawString("" + j*scale, i*scale + 5, j*scale + 26);
      }
    }
  }

  //add object to specific grid box by index
  private void add(Object obj, int i, int j)
  {
    if(grid[i][j] == null)
    {
      //create an arraylist at specxific index if it doesn't already exist
      grid[i][j] = new ArrayList<Object>();
    }
    //add object to the grid at specific index
    grid[i][j].add(obj);
    //add the specific grid index to the objects collisionArea arraylist
    //for future reference
    obj.collisionArea.add(new Vector(i, j));
  }

  //remove a specific object from the grid
  public void remove(Object obj)
  {
    for(Vector gridBox : obj.collisionArea)
    {
      grid[gridBox.x][gridBox.y].remove(obj);
    }
    obj.collisionArea.clear();
  }

  //dynamically update the grid if an object moves
  public void update (Object obj)
  {
    remove(obj);
    insert(obj);
  }

}
