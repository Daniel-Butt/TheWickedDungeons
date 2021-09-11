/*
This class gets all require user input for the engine

includes
 - Keyboard input
 - mouse position
 - if the mouse chnages position
 - if the mouse wheel is moved
*/

//importing all require libraries
import java.awt.event.*;

//implements all required interfaces
public class UserInput implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener
{
  //total number of keys supported on standard ASCII keyboard
  private final int NUMBER_OF_KEYS = 256;
  //array of all keys, true or false if they are being pressed or not
  public boolean[] keysDown = new boolean[NUMBER_OF_KEYS];
  //current key being held down (last key if pressing multiple)
  public int currentKey = -1;
  //current mouse button being pressed (last if pressing multiple)
  public int mouseButton = -1;
  //amount of rotation since that update
  public int wheelRotation = 0;
  //mouse position
  public int mouseX, mouseY;

  //constructor
  UserInput()
  {
    //initializing keysDown[]
    for(boolean key : keysDown)
    {
      key = false;
    }
  }

  //not used but has to be initialized
  @Override
  public void keyTyped(KeyEvent e){}

  @Override
  public void keyPressed(KeyEvent e)
  {
    //get key pressed
    int keyCode = e.getKeyCode();
    //set currentkey to keycode (required because intefaces run
    //on separte threads so keypressed and keyReleased could be
    // running at the same time)
    currentKey = keyCode;
    //if keycode is standard ASCII
    if(keyCode > -1 && keyCode < NUMBER_OF_KEYS)
    {
      //set the key in keysDown to true
      keysDown[keyCode] = true;
    }

  }

  //same as keypressed but opposite
  @Override
  public void keyReleased(KeyEvent e)
  {
    int keyCode = e.getKeyCode();
    if(currentKey == keyCode)
    {
      currentKey = -1;
    }
    if(keyCode > -1 && keyCode < NUMBER_OF_KEYS)
    {
      keysDown[keyCode] = false;
    }
  }

  //get wheel rotation
  public void mouseWheelMoved(MouseWheelEvent e)
  {
    wheelRotation = e.getWheelRotation();
  }

  //get mouse location
  public void mouseMoved(MouseEvent e)
  {
    mouseX = e.getX();
    mouseY = e.getY();
  }

  //not used but has to be initialized
  public void mouseDragged(MouseEvent e){}

  //set mouseButton to the pressed button
  public void mousePressed(MouseEvent e)
  {
    mouseButton = e.getButton();
  }

  //unset mouseButton 
  public void mouseReleased(MouseEvent e)
  {
    if(mouseButton == e.getButton())
    {
      mouseButton = -1;
    }
  }

  //not used but has to be initialized
  public void mouseEntered(MouseEvent e){}

  //not used but has to be initialized
  public void mouseExited(MouseEvent e){}

  //not used but has to be initialized
  public void mouseClicked(MouseEvent e){}

}
