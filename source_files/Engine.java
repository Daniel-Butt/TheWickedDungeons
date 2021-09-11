/*
Game Engine class, where the magic happends :3

This is where most backend and non-game specific methods are located,
along with a few game specific methods, mostly graphics related

side note:
if your IDE allows you to collapse code blocks, I would highly recommend using it
*/

//importing all required libraries
import java.util.*;
import java.awt.*;
import java.io.*;
import javax.sound.sampled.*;
import java.awt.image.BufferedImage;
import java.awt.image.AffineTransformOp;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.WindowConstants;

//extends JPanel so any time you see this.some_method() its referring to the JPanel
public class Engine extends JPanel
{
  //creating a JFrame for the game
  private JFrame frame;

  //creating a instance of UserInput class which packages all required methods
  //for player input for the game
  public UserInput ui = new UserInput();

  //the following is to get the monitor refreshRate
  private GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
  private GraphicsDevice[] gs = ge.getScreenDevices();
  //index 0 assumes you either only have one monitor or are playing on your main monitor
  //this could be a problem if you have multiple montiors with various refresh rates
  //and switch which monitor your playing on after the game starts up
  private int refreshRate = gs[0].getDisplayMode().getRefreshRate();

  //getting the screen dimensions, again could be a problem if you switch monitors
  private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
  public int screenWidth = screenSize.width;
  public int screenHeight = screenSize.height;

  // Transparent cursor image.
  BufferedImage blankCursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

  // Create a new blank cursor.
  Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
      blankCursorImg, new Point(0, 0), "blank cursor");

  //ticks per second, how many times the game should update per second
  public final int TPS = 60;

  //various values for in game setting
  //shows fps
  public boolean SHOWFPS;
  //turns on/off VSYNC
  public boolean VSYNC;
  //turns on debug/level building mode
  public boolean DEBUG;
  //turns on/off music
  public boolean MUSIC;

  //engine constuctor
  // name is the name of the game
  Engine(String name)
  {
    //create the game frame with the inputed name
    frame = new JFrame(name);
    //add all required user input
    addKeyListener(ui);
    addMouseListener(ui);
    addMouseMotionListener(ui);
    addMouseWheelListener(ui);

    //add the JPanel to the frame
    frame.add(this);
    //ensuring the game starts in fullscreen mode
    frame.setPreferredSize(screenSize);
    frame.setMinimumSize(screenSize);
    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    //setting the frame to exit by pressing the close button
    //kinda pointless since the frame in undecorated (see below)
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    //making the frame truly fullscreen
    frame.setUndecorated(true);
    //packing the above into the frame
    frame.pack();

    //getting focus for the JPanel
    this.setFocusable(true);
    this.requestFocusInWindow();
  }

  //gameState differentiates what the game loop does
  public String gameState = "";

  //the below is for calculating the fps and how often the game should update
  //the total time passed since startup
  private long timePassed = 0;
  //the current time IRL
  private long currentTime;
  //the delta between currentTime and tempTime
  private long deltaTime;
  //current time at last game loop
  private long tempTime;
  //fps (onscreen) only updates every so often, so you can actually read it
  //as well as performance reasons
  private int fpsTempCounter = 0;
  //fps on screen
  private String fps = "0";

  //frame counter for the title screen animation
  private int titleFrame = 0;
  //counter for when to update the title screen animation frame
  private int titleTempCounter = 0;
  //counter for when to update the new game screen animations
  private int newGameTempCounter = 0;
  private int newGameFrameCounter = 0;
  //counter for how long to display the death screen
  private int deadCounter = 0;
  //death screen pro tips message
  private int deathMsg = 0;

  //where the magic happends
  //game loop
  public void paintComponent(Graphics g)
  {
    //side note: any time you see * screenWidth / 1920 or
    //something similar, its to scale the picture/ string
    //according to your monitor dimensions

    //game state dictates what the game loop does
    switch(gameState)
    {
      //main game
      case "game":
        if(!DEBUG)
        {
          // Set the blank cursor to the JFrame.
          this.setCursor(blankCursor);
        }

        // turn on background music / turn off campFire music
        if(!clips[0].isRunning() && MUSIC)
        {
          clips[0].loop(Clip.LOOP_CONTINUOUSLY);
        }
        if(clips[1].isRunning())
        {
          clips[1].stop();
        }

        //reload sounds
        if(MUSIC)
        {
          for(int i = 2; i < clips.length; i++)
          {
            //if the sound clip has finished, reset the clip to play from the begining
            if(clips[i].getFrameLength() == clips[i].getFramePosition())
            {
              clips[i].setFramePosition(0);
            }
          }
        }

        //calculate how many times to update
        currentTime = System.nanoTime();

        //change in time since last game loop
        deltaTime = currentTime - tempTime;

        //add that to time passed
        timePassed += deltaTime;

        //VSYNC
        if(VSYNC)
        {
          //if the time since the last game loop is less than the refresh rate
          //sleep until it is
          if(deltaTime < 1e9 / refreshRate)
          {
            try
            {
              Thread.sleep((int)((1e9 / refreshRate - deltaTime) / 1e6));
              deltaTime += 1e9 / refreshRate - deltaTime;
            }
            catch(InterruptedException e){}
          }
        }

        //set temp time to currentTime for the next game loop
        tempTime = currentTime;

        //update loop
        //if the timePassed is greater then 1 / TPS seconds
        //then update, subtract timePassed by 1 / TPS seconds
        //and repeat untill its not
        while(timePassed > 1e9 / TPS)
        {

          timePassed -= (long) Math.floor(1e9 / TPS);
          update();
        }

        //render, game updates 60 times per seconds, but renders
        //as often as possible (factoring in VSYNC if enabled)
        render(g);//passing the graphics of the JPanel

        //fps counter
        if(SHOWFPS) showFPS(g);

        //if ESC key is pressed
        if(ui.keysDown[27])
        {
          //got to setting menu
          gameState = "settings";
        }

        break;
      //title screen
      case "title":
        //reset cursor to default
        this.setCursor(Cursor.getDefaultCursor());

        //turn on campfire sound if is off and music is enabled
        if(!clips[1].isRunning() && MUSIC)
        {
          clips[1].loop(Clip.LOOP_CONTINUOUSLY);
        }

        //draw title screen animation
        g.drawImage(titleScreen[titleFrame], 0, 0, null);

        //if its been 4 game cycles, update the title screen aniamtion frame
        //aniamtion was designed to run at ~15 fps
        titleTempCounter++;
        if(titleTempCounter == 4)
        {
          titleTempCounter = 0;
          if(titleFrame < titleScreen.length - 1)
          {
            titleFrame++;
          }
          else
          {
            titleFrame = 0;
          }
        }

        //draw title
        g.setColor(new Color(240, 109, 22));
        g.setFont(new Font("TimesRoman", Font.BOLD, 80 * screenWidth / 1920));
        g.drawString("The Wicked Dungeons", 50 * screenWidth / 1920, 300 * screenHeight / 1080);

        //draw menu
        g.setColor(new Color(240, 139, 22));
        g.setFont(new Font("TimesRoman", Font.BOLD, 50 * screenWidth / 1920));

        //The following are buttons,
        //any time you see (button) the comments are the same
        //minus the if pressed section

        //Continue (button)
        //if the saves folder and file exists
        if(saves.exists())
        {
          // check and if if the mouse if between the buttons hit box
          if(ui.mouseX > 318 * screenWidth / 1920 &&
             ui.mouseX < 318 * screenWidth / 1920 + 220 * screenWidth / 1920 &&
             ui.mouseY > 410 * screenHeight / 1080 &&
             ui.mouseY < 410 * screenHeight / 1080 + 50 * screenHeight / 1080)
          {
            //display a different size and color font
            g.setFont(new Font("TimesRoman", Font.BOLD, 60 * screenWidth / 1920));
            g.setColor(new Color(240, 69, 22));
            g.drawString("Continue", 310 * screenWidth / 1920, 450 * screenHeight / 1080);
            //reset font
            g.setColor(new Color(240, 139, 22));
            g.setFont(new Font("TimesRoman", Font.BOLD, 50 * screenWidth / 1920));

            //**if pressed setion**
            //if the left mouse button is also pressed
            if(ui.mouseButton == 1)
            {
              //reset the mouse button
              ui.mouseButton = -1;
              //load game save
              save = new File("../saves/save.txt");
              loadSave(save);
              //set game state to game
              gameState = "game";
            }
          }
          //if mouse is not in button hitbox
          else
          {
            //just display normal text
            g.drawString("Continue", 330 * screenWidth / 1920, 450 * screenHeight / 1080);
          }
        }
        else
        {
          g.setColor(new Color(120, 120, 120));
          g.drawString("Continue", 330 * screenWidth / 1920, 450 * screenHeight / 1080);
          g.setColor(new Color(240, 139, 22));
        }
        //New Game (button)
        if(ui.mouseX > 308 * screenWidth / 1920 &&
           ui.mouseX < 308 * screenWidth / 1920 + 240 * screenWidth / 1920 &&
           ui.mouseY > 510 * screenHeight / 1080 &&
           ui.mouseY < 510 * screenHeight / 1080 + 50 * screenHeight / 1080)
        {
          g.setFont(new Font("TimesRoman", Font.BOLD, 60 * screenWidth / 1920));
          g.setColor(new Color(240, 69, 22));
          g.drawString("New Game", 292 * screenWidth / 1920, 550 * screenHeight / 1080);
          g.setColor(new Color(240, 139, 22));
          g.setFont(new Font("TimesRoman", Font.BOLD, 50 * screenWidth / 1920));

          if(ui.mouseButton == 1)
          {
            ui.mouseButton = -1;
            //set game state to new game menu
            gameState = "newGame";
          }
        }
        else
        {
          g.drawString("New Game", 312 * screenWidth / 1920, 550 * screenHeight / 1080);
        }
        //Options (button)
        if(ui.mouseX > 340 * screenWidth / 1920 &&
           ui.mouseX < 340 * screenWidth / 1920 + 180 * screenWidth / 1920 &&
           ui.mouseY > 610 * screenHeight / 1080 &&
           ui.mouseY < 610 * screenHeight / 1080 + 55 * screenHeight / 1080)
        {
          g.setFont(new Font("TimesRoman", Font.BOLD, 60 * screenWidth / 1920));
          g.setColor(new Color(240, 69, 22));
          g.drawString("Options", 325 * screenWidth / 1920, 650 * screenHeight / 1080);
          g.setColor(new Color(240, 139, 22));
          g.setFont(new Font("TimesRoman", Font.BOLD, 50 * screenWidth / 1920));

          if(ui.mouseButton == 1)
          {
            ui.mouseButton = -1;
            //set game state to settings menu
            gameState = "settings";
          }
        }
        else
        {
          g.drawString("Options", 345 * screenWidth / 1920, 650 * screenHeight / 1080);
        }
        //Score (button)
        if(saves.exists()) // need a save to have score
        {
          if(ui.mouseX > 355 * screenWidth / 1920 &&
             ui.mouseX < 355 * screenWidth / 1920 + 140 * screenWidth / 1920 &&
             ui.mouseY > 710 * screenHeight / 1080 &&
             ui.mouseY < 710 * screenHeight / 1080 + 55 * screenHeight / 1080)
          {
            g.setFont(new Font("TimesRoman", Font.BOLD, 60 * screenWidth / 1920));
            g.setColor(new Color(240, 69, 22));
            g.drawString("Score", 355 * screenWidth / 1920, 750 * screenHeight / 1080);
            g.setColor(new Color(240, 139, 22));
            g.setFont(new Font("TimesRoman", Font.BOLD, 50 * screenWidth / 1920));

            if(ui.mouseButton == 1)
            {
              ui.mouseButton = -1;
              //set game state to score board
              gameState = "score";
            }
          }
          else
          {
            g.drawString("Score", 365 * screenWidth / 1920, 750 * screenHeight / 1080);
          }
        }
        else
        {
          g.setColor(new Color(120, 120, 120));
          g.drawString("Score", 365 * screenWidth / 1920, 750 * screenHeight / 1080);
          g.setColor(new Color(240, 139, 22));
        }
        //Quit (button)
        if(ui.mouseX > 370 * screenWidth / 1920 &&
           ui.mouseX < 370 * screenWidth / 1920 + 110 * screenWidth / 1920 &&
           ui.mouseY > 810 * screenHeight / 1080 &&
           ui.mouseY < 810 * screenHeight / 1080 + 55 * screenHeight / 1080)
        {
          g.setFont(new Font("TimesRoman", Font.BOLD, 60 * screenWidth / 1920));
          g.setColor(new Color(240, 69, 22));
          g.drawString("Quit", 365 * screenWidth / 1920, 850 * screenHeight / 1080);
          g.setColor(new Color(240, 139, 22));
          g.setFont(new Font("TimesRoman", Font.BOLD, 50 * screenWidth / 1920));

          if(ui.mouseButton == 1)
          {
            ui.mouseButton = -1;
            //kill the game JFrame
            frame.dispose();
            //quit the application
            System.exit(0);
          }
        }
        else
        {
          g.drawString("Quit", 375 * screenWidth / 1920, 850 * screenHeight / 1080);
        }

        //delay so it runs at 60 fps
        try
        {
          Thread.sleep(1000 / 60);

        }
        catch(InterruptedException e){}
        break;
      //setting menu
      case "settings":
        //reset cursor to default
        this.setCursor(Cursor.getDefaultCursor());

        //checking to see if the game is running
        //ie if the setting menu was accessed from in game or
        //the title screen
        //if in the game, objects arraylist wont be empty

        //not in game
        if(objects.size() < 1)
        {
          //delay so it runs at 60 fps
          try
          {
            Thread.sleep(1000 / 60);

          }
          catch(InterruptedException e){}

          //draw title screen animation
          g.drawImage(titleScreen[titleFrame], 0, 0, null);

          //if its been 4 game cycles, update the title screen aniamtion frame
          //aniamtion was designed to run at ~15 fps
          titleTempCounter++;
          if(titleTempCounter == 4)
          {
            titleTempCounter = 0;
            if(titleFrame < titleScreen.length - 1)
            {
              titleFrame++;
            }
            else
            {
              titleFrame = 0;
            }
          }

          //turn on/off campfire music
          if (clips[1].isRunning() && !MUSIC)
          {
            clips[1].stop();
          }
          else if(!clips[1].isRunning() && MUSIC)
          {
            clips[1].loop(Clip.LOOP_CONTINUOUSLY);
          }
        }
        //in game
        else
        {
          //render game in background (game is paused though)
          render(g);
          //draw a slightly opaque filter over top of the game
          g.setColor(new Color(100, 100, 100, 100));
          g.fillRect(0, 0, screenWidth, screenHeight);

          //turn on/off game music
          if(!clips[0].isRunning() && MUSIC)
          {
            clips[0].loop(Clip.LOOP_CONTINUOUSLY);
          }
          else if(clips[0].isRunning() && !MUSIC)
          {
            clips[0].stop();
          }
        }

        //Back (button)
        if(ui.mouseX > 370 * screenWidth / 1920 &&
           ui.mouseX < 370 * screenWidth / 1920 + 140 * screenWidth / 1920 &&
           ui.mouseY > (screenHeight - 150) &&
           ui.mouseY < (screenHeight - 150) + 55 * screenHeight / 1080)
        {
          g.setFont(new Font("TimesRoman", Font.BOLD, 60 * screenWidth / 1920));
          g.setColor(new Color(240, 69, 22));
          g.drawString("Back", 365 * screenWidth / 1920, (screenHeight - 110));

          if(ui.mouseButton == 1)
          {
            ui.mouseButton = -1;
            //if in game
            if(objects.size() > 0)
            {
              //go back to game
              gameState = "game";
              //update tempTime so the game doesn't update
              //for the time you were in settings
              tempTime = System.nanoTime();
            }
            else
            {
              //go back to title screen
              gameState = "title";
            }
          }
        }
        else
        {
          g.setFont(new Font("TimesRoman", Font.BOLD, 50 * screenWidth / 1920));
          g.setColor(new Color(240, 139, 22));
          g.drawString("Back", 375 * screenWidth / 1920, (screenHeight - 110));
        }

        //turns FrameRate on/off (button)
        {
          g.setFont(new Font("TimesRoman", Font.BOLD, 60 * screenWidth / 1920));
          g.setColor(new Color(240, 139, 22));

          g.drawString("Frame Rate Counter",
                       370 * screenWidth / 1920,
                       150 * screenHeight / 1080);

          if(SHOWFPS)
          {
            g.setColor(new Color(54, 183, 235));
            g.fillRect(300 * screenWidth / 1920,
                       150 * screenHeight / 1080 - 50 * screenWidth / 1920,
                       50 * screenWidth / 1920,
                       50 * screenHeight / 1080);
          }
          else
          {
            g.setColor(new Color(50, 50, 50));
            g.fillRect(300 * screenWidth / 1920,
                       150 * screenHeight / 1080 - 50 * screenWidth / 1920,
                       50 * screenWidth / 1920,
                       50 * screenHeight / 1080);
          }

          if(ui.mouseX > 300 * screenWidth / 1920 &&
             ui.mouseX < 300 * screenWidth / 1920 + 50 * screenWidth / 1920 &&
             ui.mouseY > 150 * screenHeight / 1080 - 50 * screenWidth / 1920 &&
             ui.mouseY < 150 * screenHeight / 1080)
          {
            if(ui.mouseButton == 1)
            {
              ui.mouseButton = -1;
              SHOWFPS = !SHOWFPS;
              saveConfig(config);
            }
            else
            {
              g.setColor(new Color(240, 31, 87));
              g.fillRect(300 * screenWidth / 1920,
                         150 * screenHeight / 1080 - 50 * screenWidth / 1920,
                         50 * screenWidth / 1920,
                         50 * screenHeight / 1080);
            }
          }

          g.setColor(new Color(120, 120, 120));
          g.drawRect(300 * screenWidth / 1920,
                     150 * screenHeight / 1080 - 50 * screenWidth / 1920,
                     50 * screenWidth / 1920,
                     50 * screenHeight / 1080);
        }
        //turns VSYNC on/off (button)
        {
          g.setFont(new Font("TimesRoman", Font.BOLD, 60 * screenWidth / 1920));
          g.setColor(new Color(240, 139, 22));

          g.drawString("VSYNC", 370 * screenWidth / 1920, 250 * screenHeight / 1080);

          if(VSYNC)
          {
            g.setColor(new Color(54, 183, 235));
            g.fillRect(300 * screenWidth / 1920,
                       250 * screenHeight / 1080 - 50 * screenWidth / 1920,
                       50 * screenWidth / 1920,
                       50 * screenHeight / 1080);
          }
          else
          {
            g.setColor(new Color(50, 50, 50));
            g.fillRect(300 * screenWidth / 1920,
                       250 * screenHeight / 1080 - 50 * screenWidth / 1920,
                       50 * screenWidth / 1920,
                       50 * screenHeight / 1080);
          }

          if(ui.mouseX > 300 * screenWidth / 1920 &&
             ui.mouseX < 300 * screenWidth / 1920 + 50 * screenWidth / 1920 &&
             ui.mouseY > 250 * screenHeight / 1080 - 50 * screenWidth / 1920 &&
             ui.mouseY < 250 * screenHeight / 1080)
          {
            if(ui.mouseButton == 1)
            {
              ui.mouseButton = -1;
              VSYNC = !VSYNC;
              saveConfig(config);
            }
            else
            {
              g.setColor(new Color(240, 31, 87));
              g.fillRect(300 * screenWidth / 1920,
                         250 * screenHeight / 1080 - 50 * screenWidth / 1920,
                         50 * screenWidth / 1920,
                         50 * screenHeight / 1080);
            }
          }

          g.setColor(new Color(120, 120, 120));
          g.drawRect(300 * screenWidth / 1920,
                     250 * screenHeight / 1080 - 50 * screenWidth / 1920,
                     50 * screenWidth / 1920,
                     50 * screenHeight / 1080);
        }
        //turns Debug mode on/off (button)
        {
          g.setFont(new Font("TimesRoman", Font.BOLD, 60 * screenWidth / 1920));
          g.setColor(new Color(240, 139, 22));

          g.drawString("Debug Mode", 370 * screenWidth / 1920, 350 * screenHeight / 1080);

          if(DEBUG)
          {
            g.setColor(new Color(54, 183, 235));
            g.fillRect(300 * screenWidth / 1920,
                       350 * screenHeight / 1080 - 50 * screenWidth / 1920,
                       50 * screenWidth / 1920,
                       50 * screenHeight / 1080);
          }
          else
          {
            g.setColor(new Color(50, 50, 50));
            g.fillRect(300 * screenWidth / 1920,
                       350 * screenHeight / 1080 - 50 * screenWidth / 1920,
                       50 * screenWidth / 1920,
                       50 * screenHeight / 1080);
          }

          if(ui.mouseX > 300 * screenWidth / 1920 &&
             ui.mouseX < 300 * screenWidth / 1920 + 50 * screenWidth / 1920 &&
             ui.mouseY > 350 * screenHeight / 1080 - 50 * screenWidth / 1920 &&
             ui.mouseY < 350 * screenHeight / 1080)
          {
            if(ui.mouseButton == 1)
            {
              ui.mouseButton = -1;
              DEBUG = !DEBUG;
              saveConfig(config);
            }
            else
            {
              g.setColor(new Color(240, 31, 87));
              g.fillRect(300 * screenWidth / 1920,
                         350 * screenHeight / 1080 - 50 * screenWidth / 1920,
                         50 * screenWidth / 1920,
                         50 * screenHeight / 1080);
            }
          }

          g.setColor(new Color(120, 120, 120));
          g.drawRect(300 * screenWidth / 1920,
                     350 * screenHeight / 1080 - 50 * screenWidth / 1920,
                     50 * screenWidth / 1920,
                     50 * screenHeight / 1080);
        }
        //turns Music on/off (button)
        {
          g.setFont(new Font("TimesRoman", Font.BOLD, 60 * screenWidth / 1920));
          g.setColor(new Color(240, 139, 22));

          g.drawString("Music", 370 * screenWidth / 1920, 450 * screenHeight / 1080);

          if(MUSIC)
          {
            g.setColor(new Color(54, 183, 235));
            g.fillRect(300 * screenWidth / 1920,
                       450 * screenHeight / 1080 - 50 * screenWidth / 1920,
                       50 * screenWidth / 1920,
                       50 * screenHeight / 1080);
          }
          else
          {
            g.setColor(new Color(50, 50, 50));
            g.fillRect(300 * screenWidth / 1920,
                       450 * screenHeight / 1080 - 50 * screenWidth / 1920,
                       50 * screenWidth / 1920,
                       50 * screenHeight / 1080);
          }

          if(ui.mouseX > 300 * screenWidth / 1920 &&
             ui.mouseX < 300 * screenWidth / 1920 + 50 * screenWidth / 1920 &&
             ui.mouseY > 450 * screenHeight / 1080 - 50 * screenWidth / 1920 &&
             ui.mouseY < 450 * screenHeight / 1080)
          {
            if(ui.mouseButton == 1)
            {
              ui.mouseButton = -1;
              MUSIC = !MUSIC;
              saveConfig(config);
            }
            else
            {
              g.setColor(new Color(240, 31, 87));
              g.fillRect(300 * screenWidth / 1920,
                         450 * screenHeight / 1080 - 50 * screenWidth / 1920,
                         50 * screenWidth / 1920,
                         50 * screenHeight / 1080);
            }
          }

          g.setColor(new Color(120, 120, 120));
          g.drawRect(300 * screenWidth / 1920,
                     450 * screenHeight / 1080 - 50 * screenWidth / 1920,
                     50 * screenWidth / 1920,
                     50 * screenHeight / 1080);
        }
        //if in game settings
        if(objects.size() > 0)
        {
          //Restart level (button)
          if(ui.mouseX > screenWidth - 370 * screenWidth / 1920 &&
             ui.mouseX < screenWidth - 370 * screenWidth / 1920 + 180 * screenWidth / 1920 &&
             ui.mouseY > 210 * screenHeight / 1080 &&
             ui.mouseY < 210 * screenHeight / 1080 + 55 * screenHeight / 1080)
          {
            g.setFont(new Font("TimesRoman", Font.BOLD, 60 * screenWidth / 1920));
            g.setColor(new Color(240, 69, 22));
            g.drawString("Restart", screenWidth - 375 * screenWidth / 1920, 250 * screenHeight / 1080);

            if(ui.mouseButton == 1)
            {
              ui.mouseButton = -1;
              //clear all objects from the game
              objects.clear();
              //reload save
              loadSave(save);
              //go to game
              gameState = "game";
            }
          }
          else
          {
            g.setFont(new Font("TimesRoman", Font.BOLD, 50 * screenWidth / 1920));
            g.setColor(new Color(240, 139, 22));
            g.drawString("Restart", screenWidth - 365 * screenWidth / 1920, 250 * screenHeight / 1080);
          }
          //Quit to title (button)
          if(ui.mouseX > screenWidth - 420 * screenWidth / 1920 &&
             ui.mouseX < screenWidth - 420 * screenWidth / 1920 + 280 * screenWidth / 1920 &&
             ui.mouseY > 360 * screenHeight / 1080 &&
             ui.mouseY < 360 * screenHeight / 1080 + 55 * screenHeight / 1080)
          {
            g.setFont(new Font("TimesRoman", Font.BOLD, 60 * screenWidth / 1920));
            g.setColor(new Color(240, 69, 22));
            g.drawString("Quit to Title", screenWidth - 425 * screenWidth / 1920, 400 * screenHeight / 1080);

            if(ui.mouseButton == 1)
            {
              ui.mouseButton = -1;
              //clear all objects from the game
              objects.clear();
              //go to title screen
              gameState = "title";
              //turn off background music
              if(clips[0].isRunning())
              {
                clips[0].stop();
              }
            }
          }
          else
          {
            g.setFont(new Font("TimesRoman", Font.BOLD, 50 * screenWidth / 1920));
            g.setColor(new Color(240, 139, 22));
            g.drawString("Quit to Title", screenWidth - 415 * screenWidth / 1920, 400 * screenHeight / 1080);
          }
          //Quit to desktop (button)
          if(ui.mouseX > screenWidth - 455 * screenWidth / 1920 &&
             ui.mouseX < screenWidth - 455 * screenWidth / 1920 + 350 * screenWidth / 1920 &&
             ui.mouseY > 510 * screenHeight / 1080 &&
             ui.mouseY < 510 * screenHeight / 1080 + 55 * screenHeight / 1080)
          {
            g.setFont(new Font("TimesRoman", Font.BOLD, 60 * screenWidth / 1920));
            g.setColor(new Color(240, 69, 22));
            g.drawString("Quit to Desktop", screenWidth - 465 * screenWidth / 1920, 550 * screenHeight / 1080);

            if(ui.mouseButton == 1)
            {
              ui.mouseButton = -1;
              //same a quit in title screen
              objects.clear();
              frame.dispose();
              System.exit(0);
            }
          }
          else
          {
            g.setFont(new Font("TimesRoman", Font.BOLD, 50 * screenWidth / 1920));
            g.setColor(new Color(240, 139, 22));
            g.drawString("Quit to Desktop", screenWidth - 450 * screenWidth / 1920, 550 * screenHeight / 1080);
          }
        }
        break;
      //score board
      case "score":
        //delay so it runs at 60 fps
        try
        {
          Thread.sleep(1000 / 60);

        }
        catch(InterruptedException e){}

        //draw title screen animation
        g.drawImage(titleScreen[titleFrame], 0, 0, null);

        //see title screen section
        titleTempCounter++;
        if(titleTempCounter == 4)
        {
          titleTempCounter = 0;
          if(titleFrame < titleScreen.length - 1)
          {
            titleFrame++;
          }
          else
          {
            titleFrame = 0;
          }
        }

        //Back (button)
        if(ui.mouseX > 370 * screenWidth / 1920 &&
           ui.mouseX < 370 * screenWidth / 1920 + 140 * screenWidth / 1920 &&
           ui.mouseY > (screenHeight - 150) &&
           ui.mouseY < (screenHeight - 150) + 55 * screenHeight / 1080)
        {
          g.setFont(new Font("TimesRoman", Font.BOLD, 60 * screenWidth / 1920));
          g.setColor(new Color(240, 69, 22));
          g.drawString("Back", 365 * screenWidth / 1920, (screenHeight - 110));

          if(ui.mouseButton == 1)
          {
            ui.mouseButton = -1;
            gameState = "title";
          }
        }
        else
        {
          g.setFont(new Font("TimesRoman", Font.BOLD, 50 * screenWidth / 1920));
          g.setColor(new Color(240, 139, 22));
          g.drawString("Back", 375 * screenWidth / 1920, (screenHeight - 110));
        }

        //getting score info from game save
        save = new File("../saves/save.txt");
        loadSave(save);

        //getting player info from game save
        Player p = (Player) objects.get(playerIndex);

        //displaying score board
        g.setFont(new Font("TimesRoman", Font.BOLD, 60 * screenWidth / 1920));

        g.setColor(new Color(240, 139, 22));
        g.drawString("Current Lv:", 219 * screenWidth / 1920, 200 * screenHeight / 1080);
        g.drawString("Score:", 370 * screenWidth / 1920, 300 * screenHeight / 1080);
        g.drawString("Coins:", 368 * screenWidth / 1920, 400 * screenHeight / 1080);
        g.drawString("Enemies Slain:", 153 * screenWidth / 1920, 500 * screenHeight / 1080);

        g.setColor(new Color(240, 69, 22));
        g.drawString("" + currentLevel, 670 * screenWidth / 1920, 200 * screenHeight / 1080);
        g.drawString("" + p.score, 670 * screenWidth / 1920, 300 * screenHeight / 1080);
        g.drawString("" + p.coins, 670 * screenWidth / 1920, 400 * screenHeight / 1080);
        g.drawString("" + p.slain, 670 * screenWidth / 1920, 500 * screenHeight / 1080);

        //clear game save as to not cause a memory leak
        objects.clear();
        break;
      //new game menu
      case "newGame":
        //delay so it runs at 60 fps
        try
        {
          Thread.sleep(1000 / 60);

        }
        catch(InterruptedException e){}

          //draw title screen animation
          g.drawImage(titleScreen[titleFrame], 0, 0, null);

          titleTempCounter++;
          if(titleTempCounter == 4)
          {
            titleTempCounter = 0;
            if(titleFrame < titleScreen.length - 1)
            {
              titleFrame++;
            }
            else
            {
              titleFrame = 0;
            }
          }

        //pick character screen
        g.setColor(new Color(240, 109, 22));
        g.setFont(new Font("TimesRoman", Font.BOLD, 80 * screenWidth / 1920));
        g.drawString("Pick a Character", screenWidth/2 - 285 * screenWidth / 1920, 150);

        //drawing boxes for characters
        g.drawRect(screenWidth/2 - 260 * screenWidth / 1920, 300 * screenHeight / 1080, 120 * screenWidth / 1920, 120 * screenHeight / 1080);
        g.drawRect(screenWidth/2 - 60 * screenWidth / 1920, 300 * screenHeight / 1080, 120 * screenWidth / 1920, 120 * screenHeight / 1080);
        g.drawRect(screenWidth/2 + 140 * screenWidth / 1920, 300 * screenHeight / 1080, 120 * screenWidth / 1920, 120 * screenHeight / 1080);
        g.drawRect(screenWidth/2 - 260 * screenWidth / 1920, 600 * screenHeight / 1080, 120 * screenWidth / 1920, 120 * screenHeight / 1080);
        g.drawRect(screenWidth/2 - 60 * screenWidth / 1920, 600 * screenHeight / 1080, 120 * screenWidth / 1920, 120 * screenHeight / 1080);
        g.drawRect(screenWidth/2 + 140 * screenWidth / 1920, 600 * screenHeight / 1080, 120 * screenWidth / 1920, 120 * screenHeight / 1080);

        g.setColor(new Color(120, 120, 120));

        g.fillRect(screenWidth/2 - 258 * screenWidth / 1920, 302 * screenHeight / 1080, 117 * screenWidth / 1920, 117 * screenHeight / 1080);
        g.fillRect(screenWidth/2 - 58 * screenWidth / 1920, 302 * screenHeight / 1080, 117 * screenWidth / 1920, 117 * screenHeight / 1080);
        g.fillRect(screenWidth/2 + 142 * screenWidth / 1920, 302 * screenHeight / 1080, 117 * screenWidth / 1920, 117 * screenHeight / 1080);
        g.fillRect(screenWidth/2 - 258 * screenWidth / 1920, 602 * screenHeight / 1080, 117 * screenWidth / 1920, 117 * screenHeight / 1080);
        g.fillRect(screenWidth/2 - 58 * screenWidth / 1920, 602 * screenHeight / 1080, 117 * screenWidth / 1920, 117 * screenHeight / 1080);
        g.fillRect(screenWidth/2 + 142 * screenWidth / 1920, 602 * screenHeight / 1080, 117 * screenWidth / 1920, 117 * screenHeight / 1080);

        g.setFont(new Font("TimesRoman", Font.BOLD, 50 * screenWidth / 1920));
        g.setColor(new Color(240, 139, 22));

        //Back (button)
        if(ui.mouseX > 370 * screenWidth / 1920 &&
           ui.mouseX < 370 * screenWidth / 1920 + 140 * screenWidth / 1920 &&
           ui.mouseY > (screenHeight - 150) &&
           ui.mouseY < (screenHeight - 150) + 55 * screenHeight / 1080)
        {
          g.setFont(new Font("TimesRoman", Font.BOLD, 60 * screenWidth / 1920));
          g.setColor(new Color(240, 69, 22));
          g.drawString("Back", 365 * screenWidth / 1920, (screenHeight - 110));

          if(ui.mouseButton == 1)
          {
            ui.mouseButton = -1;
            gameState = "title";
          }
        }
        else
        {
          g.setFont(new Font("TimesRoman", Font.BOLD, 50 * screenWidth / 1920));
          g.setColor(new Color(240, 139, 22));
          g.drawString("Back", 375 * screenWidth / 1920, (screenHeight - 110));
        }

        //the following are buttons with the exception of also
        //being animated
        //top left, female elf (button)
        if(ui.mouseX > screenWidth/2 - 260 * screenWidth / 1920 &&
           ui.mouseX < screenWidth/2 - 260 * screenWidth / 1920 + 120 * screenWidth / 1920 &&
           ui.mouseY > 300 * screenHeight / 1080 &&
           ui.mouseY < 300 * screenHeight / 1080 + 120 * screenHeight / 1080)
        {
          //show run animation of character
          newGameTempCounter++;
          if(newGameTempCounter == 9)
          {
            newGameTempCounter = 0;
            if(newGameFrameCounter < 3)
            {
              newGameFrameCounter++;
            }
            else
            {
              newGameFrameCounter = 0;
            }
          }
          g.drawImage(assets[53 + newGameFrameCounter].getScaledInstance(120 * screenWidth / 1920,
                                                                         120 * screenHeight / 1080,
                                                                         Image.SCALE_DEFAULT),
                      screenWidth/2 - 258 * screenWidth / 1920,
                      300 * screenHeight / 1080,
                      null);

          if(ui.mouseButton == 1)
          {
            ui.mouseButton = -1;

            //the following is all the same regardless of which
            //character is selected (other than the sprites)

            //create saves directory and save file
            if(!saves.exists())
            {
              saves.mkdir();
            }
            save = new File("../saves/save.txt");
            try
            {
              save.delete();
              save.createNewFile();
            }
            catch(IOException e){}

            //set player Type
            playerType = 0;

            //load selected character sprites
            playerIdle = new Image[4];
            playerIdle[0] = assets[49].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerIdle[1] = assets[50].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerIdle[2] = assets[51].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerIdle[3] = assets[52].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerRun = new Image[4];
            playerRun[0] = assets[53].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerRun[1] = assets[54].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerRun[2] = assets[55].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerRun[3] = assets[56].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerHit = assets[48].getScaledInstance(60, 90, Image.SCALE_DEFAULT);

            //start first level
            startLevel(0);
            //go to game
            gameState = "game";

            //ensure the player in loaded
            if(objects.get(playerIndex) instanceof Player)
            {
              //set player sprites
              Player player = (Player) objects.get(playerIndex);
              player.setIdleSprites(playerIdle);
              player.setRunSprites(playerRun);
              player.setHitSprite(playerHit);
            }

            //make default game save
            save(save);
          }
        }
        else
        {
          //draw basic character
          g.drawImage(assets[49].getScaledInstance(120 * screenWidth / 1920,
                                                   120 * screenHeight / 1080,
                                                   Image.SCALE_DEFAULT),
                      screenWidth/2 - 258 * screenWidth / 1920,
                      300 * screenHeight / 1080,
                      null);
        }

        //bottom left, male elf (button)
        if(ui.mouseX > screenWidth/2 - 260 * screenWidth / 1920 &&
           ui.mouseX < screenWidth/2 - 260 * screenWidth / 1920 + 120 * screenWidth / 1920 &&
           ui.mouseY > 600 * screenHeight / 1080 &&
           ui.mouseY < 600 * screenHeight / 1080 + 120 * screenHeight / 1080)
        {
          //show run animation of character
          newGameTempCounter++;
          if(newGameTempCounter == 9)
          {
            newGameTempCounter = 0;
            if(newGameFrameCounter < 3)
            {
              newGameFrameCounter++;
            }
            else
            {
              newGameFrameCounter = 0;
            }
          }
          g.drawImage(assets[62 + newGameFrameCounter].getScaledInstance(120 * screenWidth / 1920,
                                                                         120 * screenHeight / 1080,
                                                                         Image.SCALE_DEFAULT),
                      screenWidth/2 - 260 * screenWidth / 1920,
                      600 * screenHeight / 1080,
                      null);

          if(ui.mouseButton == 1)
          {
            ui.mouseButton = -1;

            //create saves directory and save file
            if(!saves.exists())
            {
              saves.mkdir();
            }
            save = new File("../saves/save.txt");
            try
            {
              save.delete();
              save.createNewFile();
            }
            catch(IOException e){}

            //set player Type
            playerType = 1;

            //load selected character sprites
            playerIdle = new Image[4];
            playerIdle[0] = assets[58].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerIdle[1] = assets[59].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerIdle[2] = assets[60].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerIdle[3] = assets[61].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerRun = new Image[4];
            playerRun[0] = assets[62].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerRun[1] = assets[63].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerRun[2] = assets[64].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerRun[3] = assets[65].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerHit = assets[57].getScaledInstance(60, 90, Image.SCALE_DEFAULT);

            //start first level
            startLevel(0);
            gameState = "game";

            if(objects.get(playerIndex) instanceof Player)
            {
              Player player = (Player) objects.get(playerIndex);
              player.setIdleSprites(playerIdle);
              player.setRunSprites(playerRun);
              player.setHitSprite(playerHit);
            }

            //make default game save
            save(save);
          }
        }
        else
        {
          //draw basic character
          g.drawImage(assets[58].getScaledInstance(120 * screenWidth / 1920,
                                                                         120 * screenHeight / 1080,
                                                                         Image.SCALE_DEFAULT),
                      screenWidth/2 - 260 * screenWidth / 1920,
                      600 * screenHeight / 1080,
                      null);
        }

        //top middle, female lizard (button)
        if(ui.mouseX > screenWidth/2 - 60 * screenWidth / 1920 &&
           ui.mouseX < screenWidth/2 - 60 * screenWidth / 1920 + 120 * screenWidth / 1920 &&
           ui.mouseY > 300 * screenHeight / 1080 &&
           ui.mouseY < 300 * screenHeight / 1080 + 120 * screenHeight / 1080)
        {
          //show run animation of character
          newGameTempCounter++;
          if(newGameTempCounter == 9)
          {
            newGameTempCounter = 0;
            if(newGameFrameCounter < 3)
            {
              newGameFrameCounter++;
            }
            else
            {
              newGameFrameCounter = 0;
            }
          }
          g.drawImage(assets[135 + newGameFrameCounter].getScaledInstance(120 * screenWidth / 1920,
                                                                          120 * screenHeight / 1080,
                                                                          Image.SCALE_DEFAULT),
                      screenWidth/2 - 61 * screenWidth / 1920,
                      300 * screenHeight / 1080,
                      null);

          if(ui.mouseButton == 1)
          {
            ui.mouseButton = -1;

            //create saves directory and save file
            if(!saves.exists())
            {
              saves.mkdir();
            }
            save = new File("../saves/save.txt");
            try
            {
              save.delete();
              save.createNewFile();
            }
            catch(IOException e){}

            //set player Type
            playerType = 2;

            //load selected character sprites
            playerIdle = new Image[4];
            playerIdle[0] = assets[131].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerIdle[1] = assets[132].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerIdle[2] = assets[133].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerIdle[3] = assets[134].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerRun = new Image[4];
            playerRun[0] = assets[135].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerRun[1] = assets[136].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerRun[2] = assets[137].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerRun[3] = assets[138].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerHit = assets[130].getScaledInstance(60, 90, Image.SCALE_DEFAULT);

            //start first level
            startLevel(0);
            gameState = "game";

            if(objects.get(playerIndex) instanceof Player)
            {
              Player player = (Player) objects.get(playerIndex);
              player.setIdleSprites(playerIdle);
              player.setRunSprites(playerRun);
              player.setHitSprite(playerHit);
            }

            //make default game save
            save(save);
          }
        }
        else
        {
          //draw basic character
          g.drawImage(assets[131].getScaledInstance(120 * screenWidth / 1920,
                                                                          120 * screenHeight / 1080,
                                                                          Image.SCALE_DEFAULT),
                      screenWidth/2 - 61 * screenWidth / 1920,
                      300 * screenHeight / 1080,
                      null);
        }

        //bottom middle, male lizard (button)
        if(ui.mouseX > screenWidth/2 - 60 * screenWidth / 1920 &&
           ui.mouseX < screenWidth/2 - 60 * screenWidth / 1920 + 120 * screenWidth / 1920 &&
           ui.mouseY > 600 * screenHeight / 1080 &&
           ui.mouseY < 600 * screenHeight / 1080 + 120 * screenHeight / 1080)
        {
          //show run animation of character
          newGameTempCounter++;
          if(newGameTempCounter == 9)
          {
            newGameTempCounter = 0;
            if(newGameFrameCounter < 3)
            {
              newGameFrameCounter++;
            }
            else
            {
              newGameFrameCounter = 0;
            }
          }
          g.drawImage(assets[144 + newGameFrameCounter].getScaledInstance(120 * screenWidth / 1920,
                                                                          120 * screenHeight / 1080,
                                                                          Image.SCALE_DEFAULT),
                      screenWidth/2 - 61 * screenWidth / 1920,
                      600 * screenHeight / 1080,
                      null);

          if(ui.mouseButton == 1)
          {
            ui.mouseButton = -1;

            //create saves directory and save file
            if(!saves.exists())
            {
              saves.mkdir();
            }
            save = new File("../saves/save.txt");
            try
            {
              save.delete();
              save.createNewFile();
            }
            catch(IOException e){}

            //set player Type
            playerType = 3;

            //load selected character sprites
            playerIdle = new Image[4];
            playerIdle[0] = assets[140].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerIdle[1] = assets[141].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerIdle[2] = assets[142].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerIdle[3] = assets[143].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerRun = new Image[4];
            playerRun[0] = assets[144].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerRun[1] = assets[145].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerRun[2] = assets[146].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerRun[3] = assets[147].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerHit = assets[139].getScaledInstance(60, 90, Image.SCALE_DEFAULT);

            //start first level
            startLevel(0);
            gameState = "game";

            if(objects.get(playerIndex) instanceof Player)
            {
              Player player = (Player) objects.get(playerIndex);
              player.setIdleSprites(playerIdle);
              player.setRunSprites(playerRun);
              player.setHitSprite(playerHit);
            }

            //make default game save
            save(save);
          }
        }
        else
        {
          //draw basic character
          g.drawImage(assets[140].getScaledInstance(120 * screenWidth / 1920,
                                                                          120 * screenHeight / 1080,
                                                                          Image.SCALE_DEFAULT),
                      screenWidth/2 - 61 * screenWidth / 1920,
                      600 * screenHeight / 1080,
                      null);
        }

        //top right, female wizard (button)
        if(ui.mouseX > screenWidth/2 + 140 * screenWidth / 1920 &&
           ui.mouseX < screenWidth/2 + 140 * screenWidth / 1920 + 120 * screenWidth / 1920 &&
           ui.mouseY > 300 * screenHeight / 1080 &&
           ui.mouseY < 300 * screenHeight / 1080 + 120 * screenHeight / 1080)
        {
          //show run animation of character
          newGameTempCounter++;
          if(newGameTempCounter == 9)
          {
            newGameTempCounter = 0;
            if(newGameFrameCounter < 3)
            {
              newGameFrameCounter++;
            }
            else
            {
              newGameFrameCounter = 0;
            }
          }
          g.drawImage(assets[300 + newGameFrameCounter].getScaledInstance(120 * screenWidth / 1920,
                                                                          120 * screenHeight / 1080,
                                                                          Image.SCALE_DEFAULT),
                      screenWidth/2 + 143 * screenWidth / 1920,
                      300 * screenHeight / 1080,
                      null);

          if(ui.mouseButton == 1)
          {
            ui.mouseButton = -1;

            //create saves directory and save file
            if(!saves.exists())
            {
              saves.mkdir();
            }
            save = new File("../saves/save.txt");
            try
            {
              save.delete();
              save.createNewFile();
            }
            catch(IOException e){}

            //set player Type
            playerType = 4;

            //load selected character sprites
            playerIdle = new Image[4];
            playerIdle[0] = assets[296].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerIdle[1] = assets[297].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerIdle[2] = assets[298].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerIdle[3] = assets[299].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerRun = new Image[4];
            playerRun[0] = assets[300].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerRun[1] = assets[301].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerRun[2] = assets[302].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerRun[3] = assets[303].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerHit = assets[295].getScaledInstance(60, 90, Image.SCALE_DEFAULT);

            //start first level
            startLevel(0);
            gameState = "game";

            if(objects.get(playerIndex) instanceof Player)
            {
              Player player = (Player) objects.get(playerIndex);
              player.setIdleSprites(playerIdle);
              player.setRunSprites(playerRun);
              player.setHitSprite(playerHit);
            }

            //make default game save
            save(save);
          }
        }
        else
        {
          //draw basic character
          g.drawImage(assets[296].getScaledInstance(120 * screenWidth / 1920,
                                                                          120 * screenHeight / 1080,
                                                                          Image.SCALE_DEFAULT),
                      screenWidth/2 + 143 * screenWidth / 1920,
                      300 * screenHeight / 1080,
                      null);
        }

        //bottom right, male wizard (button)
        if(ui.mouseX > screenWidth/2 + 140 * screenWidth / 1920 &&
           ui.mouseX < screenWidth/2 + 140 * screenWidth / 1920 + 120 * screenWidth / 1920 &&
           ui.mouseY > 600 * screenHeight / 1080 &&
           ui.mouseY < 600 * screenHeight / 1080 + 120 * screenHeight / 1080)
        {
          //show run animation of character
          newGameTempCounter++;
          if(newGameTempCounter == 9)
          {
            newGameTempCounter = 0;
            if(newGameFrameCounter < 3)
            {
              newGameFrameCounter++;
            }
            else
            {
              newGameFrameCounter = 0;
            }
          }
          g.drawImage(assets[309 + newGameFrameCounter].getScaledInstance(120 * screenWidth / 1920,
                                                                          120 * screenHeight / 1080,
                                                                          Image.SCALE_DEFAULT),
                      screenWidth/2 + 140 * screenWidth / 1920,
                      598 * screenHeight / 1080,
                      null);

          if(ui.mouseButton == 1)
          {
            ui.mouseButton = -1;

            //create saves directory and save file
            if(!saves.exists())
            {
              saves.mkdir();
            }
            save = new File("saves/save.txt");
            try
            {
              save.delete();
              save.createNewFile();
            }
            catch(IOException e){}

            //set player Type
            playerType = 5;

            //load selected character sprites
            playerIdle = new Image[4];
            playerIdle[0] = assets[305].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerIdle[1] = assets[306].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerIdle[2] = assets[307].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerIdle[3] = assets[308].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerRun = new Image[4];
            playerRun[0] = assets[309].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerRun[1] = assets[310].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerRun[2] = assets[311].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerRun[3] = assets[312].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            playerHit = assets[304].getScaledInstance(60, 90, Image.SCALE_DEFAULT);

            //start first level
            startLevel(0);
            gameState = "game";

            if(objects.get(playerIndex) instanceof Player)
            {
              Player player = (Player) objects.get(playerIndex);
              player.setIdleSprites(playerIdle);
              player.setRunSprites(playerRun);
              player.setHitSprite(playerHit);
            }

            //make default game save
            save(save);
          }
        }
        else
        {
          //draw basic character
          g.drawImage(assets[305].getScaledInstance(120 * screenWidth / 1920,
                                                                          120 * screenHeight / 1080,
                                                                          Image.SCALE_DEFAULT),
                      screenWidth/2 + 140 * screenWidth / 1920,
                      598 * screenHeight / 1080,
                      null);
        }
        break;
      //death screen
      case "dead":
        //checking if dead game screen should end
        if(deadCounter == 0)
        {
          //reset game to last save
          objects.clear();
          loadSave(save);
          gameState = "game";
        }
        else
        {
          deadCounter--;
          try
          {
            Thread.sleep(5);
          }
          catch(InterruptedException e){}
        }
        //redraw background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, screenWidth, screenHeight);

        //death message
        g.setFont(new Font("TimesRoman", Font.BOLD, 80 * screenWidth / 1920));
        g.setColor(Color.RED);

        g.drawString("YOU DIED =(",
                     screenWidth / 2 - 250 * screenWidth / 1920,
                     300 * screenHeight / 1080);

        g.setFont(new Font("TimesRoman", Font.BOLD, 50 * screenWidth / 1920));
        g.setColor(Color.ORANGE);
        g.drawString("Pro Tip:",
                     100 * screenWidth / 1920,
                     500 * screenHeight / 1080);

        g.setFont(new Font("TimesRoman", Font.PLAIN, 40 * screenWidth / 1920));

        //various death messages, one gets randomly selected when you die
        switch(deathMsg)
        {
          case 0:
            g.drawString("Don't get stuck in corners!",
                         100 * screenWidth / 1920,
                         570 * screenHeight / 1080);
            break;
          case 1:
            g.drawString("Health potions are red =)",
                         100 * screenWidth / 1920,
                         570 * screenHeight / 1080);
            break;
          case 2:
            g.drawString("Swing that sword baby!",
                         100 * screenWidth / 1920,
                         570 * screenHeight / 1080);
            break;
          case 3:
            g.drawString("Just don't die",
                         100 * screenWidth / 1920,
                         570 * screenHeight / 1080);
            break;
          case 4:
            g.drawString("You will find better gear in chests!",
                         100 * screenWidth / 1920,
                         570 * screenHeight / 1080);
            break;

        }

        break;
    }
    //repaint in a build in method to java.swing which
    //effectley just prepares the JFrame and then recalls paintComponent;
    repaint();
  }

  //displays fps counter in top left
  private void showFPS(Graphics g)
  {
    //only displays every ~ second to ensure you can actually read it
    if(fpsTempCounter == TPS)
    {
      fps = "fps: " + Math.round((double)1 / (deltaTime / 1e9));
      fpsTempCounter = 0;
    }
    fpsTempCounter++;
    g.setColor(Color.YELLOW);
    g.setFont(new Font("TimesRoman", Font.PLAIN, 48 * screenWidth / 1920));
    g.drawString(fps, 50 * screenWidth / 1920, 50 * screenHeight / 1080);
  }

  //renders everything required in game
  private void render(Graphics g)
  {
    //redraw background
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, screenWidth, screenHeight);

    //translate by camera
    g.translate(-camera.x, -camera.y);

    //debug
    if(DEBUG)
    {
      //draw collision grid
      collisionGrid.debug(g);
    }

    //render objects
    renderObjects(g);

    //debug
    if(DEBUG)
    {
      //mostly for building levels, if you click the mouse
      //is tells you the x and y grid position of your mouse
      //as well as copies it to your clipboard
      if(ui.mouseButton != -1)
      {
        g.setColor(Color.BLACK);
        g.fillRect(camera.x + 97, camera.y + 187, 60, 20);
        g.setColor(Color.YELLOW);
        String str = "" + ((int)Math.floor((ui.mouseX + camera.x) / GRID_SCALE) * GRID_SCALE) + " "
                     + ((int)Math.floor((ui.mouseY + camera.y) / GRID_SCALE) * GRID_SCALE);
        g.drawString(str, 100 + camera.x, 200 + camera.y);

        try
        {
          StringSelection selection = new StringSelection(" " + str);
          Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
          clipboard.setContents(selection, selection);
        }
        //if you click outside the screen java doesn't have access to the
        //clipboard, so you have to throw this exception
        catch(IllegalStateException e){}
      }
    }

    //untranslate by camera
    g.translate(camera.x, camera.y);

    //get the player to give to the GUI
    Player player = (Player) objects.get(playerIndex);

    //render GUI
    renderGUI(g, player);
  }

  //updates everything required in the game
  private void update()
  {
    //player controller
    playerController();

    //update all objects (movement / animation frame)
    updateObjects();

    //update camera
    setCamera(CameraOffset);
  }

  //starts the game loop
  public void run()
  {
    //since the frame has now been setup successfully,
    //it can be displayed on screen
    frame.setVisible(true);
    //set tempTime to current time
    tempTime = System.nanoTime();
    //call paintComponent / start game loop
    repaint();
  }

  //the following are game files

  //game assets / sprites (you can see indexs using the
  //included AssetsIndexs.jar tool)
  public Image[] assets;
  //title screen animation frames
  private Image[] titleScreen;
  //game levels
  private File[] levels;
  //game sounds
  private File[] sounds;
  //playable version of game sounds
  public Clip[] clips;
  //game save
  private File save;
  //files directories
  private File saves = new File("../saves/");
  private File config = new File("../settings/config.txt");
  //the following have to be set manually as they are not
  //created by the engine
  private File assetsDirectory;
  private File levelsDirectory;
  private File titleDirectory;
  private File soundsDirectory;

  //the following set the various directories for the game
  public void setAssetsDirectory(String directory)
  {
    assetsDirectory = new File(directory);
  }

  public void setLevelsDirectory(String directory)
  {
    levelsDirectory = new File(directory);
  }

  public void setTitleDirectory(String directory)
  {
    titleDirectory = new File(directory);
  }

  public void setSoundsDirectory(String directory)
  {
    soundsDirectory = new File(directory);
  }

  //rotates an image (usually a sprite) by a given angle
  public BufferedImage rotateImg(Image img, double radians)
  {
    //create bufferedImage from img
    BufferedImage image = new BufferedImage(img.getWidth(null),
                                            img.getHeight(null),
                                            BufferedImage.TYPE_INT_ARGB);
    Graphics g = image.createGraphics();
    //drawing the img on the new bufferedImage inorder to copy it
    g.drawImage(img, 0, 0, null);
    g.dispose();

    //get sin and cos of the radian angle (mostly just to simplify the next part)
    double sin = Math.abs(Math.sin(radians));
    double cos = Math.abs(Math.cos(radians));

    //calculating the new width and height of the going to be rotated image
    int newWidth = (int) Math.floor(image.getWidth() * cos + image.getHeight() * sin);
    int newHeight = (int) Math.floor(image.getHeight() * cos + image.getWidth() * sin);

    //creating a new bufferedImage with the above width and heigth
    BufferedImage rotatedImage = new BufferedImage(newWidth, newHeight, image.getType());

    //getting the image ready to rotate
    AffineTransform at = new AffineTransform();
    //translating the rotation point to the center of the new image
    at.translate(newWidth / 2, newHeight / 2);
    //specifing the amount to rotate
    at.rotate(radians, 0, 0);
    //translating back by the amount of the old image
    at.translate(-image.getWidth() / 2, -image.getHeight() / 2);

    //applying the above step to create the new image
    AffineTransformOp rotateOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);

    //returning the rotated image
    return rotateOp.filter(image,rotatedImage);
  }

  //initializes the game engine
  public void setup()
  {
    //load all required game files
    assets = Loader.loadAssets(assetsDirectory);
    titleScreen = Loader.loadTitle(titleDirectory, screenSize);
    levels = Loader.loadLevels(levelsDirectory);
    sounds = Loader.loadSounds(soundsDirectory);
    //initialize game sounds
    initializeClips();
    //load game settings
    loadConfig(config);
    //go to title (once the run() in called)
    gameState = "title";
  }

  //turns the game sound files into playable clips
  private void initializeClips()
  {
    AudioInputStream inputStream;
    clips = new Clip[sounds.length];

    try
    {
      for(int i = 0; i < clips.length; i++)
      {
        inputStream = AudioSystem.getAudioInputStream(sounds[i]);
        clips[i] = AudioSystem.getClip();
        clips[i].open(inputStream);
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
      System.out.println("error initializing audio");
    }

  }

  //plays clip (if MUSIC is enabled)
  public void playClip(Clip clip)
  {
    if(MUSIC)
    {
      if(clip.getFramePosition() == 0)
      {
        clip.start();
      }
    }
  }

  //loads a given object into the game objects arraylist
  //not really used since I ended up needing to making
  //the objects araylist public
  public void loadObj(Object obj)
  {
    objects.add(obj);
  }

  //game key indexs
  public int UP_KEY = 87; //w
  public int DOWN_KEY = 83; //s
  public int RIGHT_KEY = 68; //d
  public int LEFT_KEY = 65; //a
  public int UTIL1_KEY = 69; //e
  public int UTIL2_KEY = 81; //q

  // idles sprites
  private Image[] playerIdle;
  //running sprites
  private Image[] playerRun;
  //hit sprite
  private Image playerHit;
  //player type (used for reloading player sprites)
  private int playerType = 0;
  //player index so the player can be found in objects arraylist
  public int playerIndex;
  //counter so you can't spam use potions
  public int useCounter = 0;

  //player controller, movement / use potions and other interactables
  private void playerController()
  {
    Player player;
    //assuming the player object is a index 0 of objects ArrayList
    if(objects.size() > 0 && objects.get(playerIndex) instanceof Player)
    {
      //getting player
      player = (Player) objects.get(playerIndex);

      //if dead
      if(player.health < 1)
      {
        gameState = "dead";
        deadCounter = 1000;
        deathMsg = (int)Math.floor(Math.random()*5);
      }
      //if player is not recovering from being hit
      if(player.damagedCounter == 0)
      {
        //movement
        if(ui.keysDown[UP_KEY])
        {
          player.vel.y += -1;
        }
        else if(ui.keysDown[DOWN_KEY])
        {
          player.vel.y += 1;
        }
        else
        {
          if(player.vel.y != 0)
          {
            player.vel.y -= Math.signum(player.vel.y);
          }
        }
        if(ui.keysDown[RIGHT_KEY])
        {
          player.vel.x += 1;
        }
        else if(ui.keysDown[LEFT_KEY])
        {
          player.vel.x += -1;
        }
        else
        {
          if(player.vel.x != 0)
          {
            player.vel.x -= Math.signum(player.vel.x);
          }
        }
      }
      //if you press a key between 1-8 (for navigating potions menu)
      if(ui.currentKey > 48 && ui.currentKey < 57)
      {
        guiIndex = ui.currentKey - 48;
      }
      //same as above but using mouse wheel instead
      int wheelRotation = ui.wheelRotation;
      if(wheelRotation != 0)
      {
        guiIndex += wheelRotation;
        ui.wheelRotation = 0;
        while(guiIndex > 8)
        {
          guiIndex -= 8;
        }
        while(guiIndex < 1)
        {
          guiIndex += 8;
        }
      }
      //checking if a player can wants to and can use a potion
      if(ui.keysDown[UTIL2_KEY] && useCounter == 0 && player.guiValues[guiIndex - 1] != 0)
      {
        switch(guiIndex)
        {
          //big speed
          case 1:
            useCounter = 120;
            player.maxSpeed += 4;
            player.bigSpeedPotions--;
            break;
          //big strength
          case 2:
            useCounter = 120;
            player.damageModifer = 3;
            player.bigStrengthPotions--;
            break;
          //big health
          case 3:
            if(player.health != player.totalHealth)
            {
              useCounter = 120;
              player.bigHealthPotions--;
              player.health += 4;
            }
            if(player.health > player.totalHealth)
            {
              player.health = player.totalHealth;
            }
            break;
          //big defence
          case 4:
            useCounter = 120;
            player.bigDefencePotions--;
            player.defenceModifer = 3;
            break;
          //speed
          case 5:
            useCounter = 120;
            player.speedPotions--;
            player.maxSpeed += 2;
            break;
          //strength
          case 6:
            if(player.damageModifer < 2)
            {
              useCounter = 120;
              player.strengthPotions--;
              player.damageModifer += 1;
            }
            break;
          //health
          case 7:
            if(player.health != player.totalHealth)
            {
              useCounter = 120;
              player.healthPotions--;
              player.health += 2;
            }
            if(player.health > player.totalHealth)
            {
              player.health = player.totalHealth;
            }
            break;
          //defence
          case 8:
            if(player.defenceModifer < 3)
            {
              useCounter = 120;
              player.defencePotions--;
              player.defenceModifer = 2;
            }
            break;
        }
        if(useCounter != 0)
        {
          playClip(clips[3]);
        }
      }
      //if the player has recently used a potion there is a delay
      //so they can't spam potions
      if(useCounter > 0)
      {
        useCounter--;
      }
    }

  }

  //camera
  private Vector camera = new Vector(0, 0);
  //camera offset so that bjects are rendered via the center
  public Vector CameraOffset = new Vector(-screenWidth/2 + 30, -screenHeight/2 + 30);

  //changing the camera positons based on player movement
  //ie the camera follows the player
  private void setCamera(Vector pos)
  {
    //ensuring a level has been initialized
    if(objects.size() > 0)
    {
      Object player = objects.get(playerIndex);
      camera.x = player.pos.x + pos.x;
      camera.y = player.pos.y + pos.y;
    }
  }

  //update objects
  private void updateObjects()
  {
    int level = currentLevel;
    try
    {
      //looping through backwards so if an objects gets deleted
      //the object after it (index wise) doesn't get skipped
      for(int i = objects.size() - 1; i >= 0; i--)
      {
        //call the objects specific update function
        //see individual object classes for more info
        objects.get(i).update(this);

        //check if player walked through a door
        //if so stop updating the current level
        if(level != currentLevel)
        {
          return;
        }

        //removeing enemies if they're dead
        if(objects.get(i) instanceof Enemy)
        {
          Enemy enemy = (Enemy) objects.get(i);
          if(enemy.health < 1)
          {
            Player player = (Player) objects.get(playerIndex);
            if(enemy instanceof Boss)
            {
              player.score += 900;
              for(Object obj : objects)
              {
                if(obj instanceof Door)
                {
                  Door door = (Door) obj;
                  if(door.locked)
                  {
                    door.locked = false;
                  }
                }
              }
            }
            player.score += 100;
            player.slain++;
            collisionGrid.remove((Object)enemy);
            objects.remove(enemy);
            playerIndex--;
          }
        }
      }
    }
    //since I'm removing objects from a list which im iterating through on another thread
    //I could technically remove them all and then would be trying
    //to iterate through a blank list
    //confuesed? so am I...
    catch(ConcurrentModificationException e){}

  }

  //renders all objects
  private void renderObjects(Graphics g)
  {
    //if in debug mode, call objects debug methods
    //as well as render
    if(DEBUG)
    {
      for(int i = objects.size() - 1; i >= 0; i--)
      {
        objects.get(i).render(g);
        objects.get(i).debug(g);
      }
    }
    //otherwise just render all objects
    else
    {
      for(int i = objects.size() - 1; i >= 0; i--)
      {
        //calling objects individual render methods
        //see individual object classes for more info
        objects.get(i).render(g);
      }
    }
  }

  //which potion the player is currently selecting
  private int guiIndex = 1;

  //renders the potion menu, scoreboard and player health
  private void renderGUI(Graphics g, Player player)
  {
    //counter for which potion to render / get inventory value for
    int spriteCounter = 0;

    //loop to draw gui boxes / potion sprites
    for(int i = 0; i < 8; i++)
    {
      g.setColor(new Color(40, 40, 40, 200));
      //draw box behind potion sprite so you can see it better
      g.fillRect(screenWidth / 2 - 280 * screenWidth / 1920 + i * 70 * screenWidth / 1920,
                 screenHeight - 120 * screenHeight / 1080,
                 60 * screenWidth / 1920,
                 60 * screenWidth / 1920);
      //draw potion sprite
      g.drawImage(assets[66 + spriteCounter].getScaledInstance(60 * screenWidth / 1920,
                                                               60 * screenWidth / 1920,
                                                               Image.SCALE_DEFAULT),
                  screenWidth / 2 - 280 * screenWidth / 1920 + i * 70 * screenWidth / 1920,
                  screenHeight - 120 * screenHeight / 1080,
                  null);
      if(player.guiValues[spriteCounter] == 0)
      {
        //if the player doesn't have any of the potion
        //draw a box over it to make the potion sprite darker
        g.fillRect(screenWidth / 2 - 280 * screenWidth / 1920 + i * 70 * screenWidth / 1920,
                   screenHeight - 120 * screenHeight / 1080,
                   60 * screenWidth / 1920,
                   60 * screenWidth / 1920);
      }
      else
      {
        //other wise draw the amount of said potion they have in the bottom right
        //of the box
        g.setColor(new Color(240, 139, 22));
        g.setFont(new Font("TimesRoman", Font.BOLD, 22 * screenWidth / 1920));
        g.drawString("" + player.guiValues[spriteCounter],
                     screenWidth / 2 - 235 * screenWidth / 1920 + i * 70 * screenWidth / 1920,
                     screenHeight - 65 * screenHeight / 1080);
      }
      //draw what number the box is, so you can select which potion you are using
      g.setFont(new Font("TimesRoman", Font.PLAIN, 14 * screenWidth / 1920));
      g.setColor(new Color(180, 180, 180));
      g.drawString("" + (spriteCounter + 1),
                   screenWidth / 2 - 275 * screenWidth / 1920 + i * 70 * screenWidth / 1920,
                   screenHeight - 105 * screenHeight / 1080);
      //draw box around the edge previous boxes and make it thicker if
      // its the box of the selected potion
      Graphics2D g2 = (Graphics2D) g;
      if(spriteCounter + 1 == guiIndex)
      {
        g2.setStroke(new BasicStroke(5));
      }
      g.drawRect(screenWidth / 2 - 280 * screenWidth / 1920 + i * 70 * screenWidth / 1920,
                 screenHeight - 120 * screenHeight / 1080,
                 60 * screenWidth / 1920,
                 60 * screenWidth / 1920);
      g2.setStroke(new BasicStroke(1));
      //increase spriteCounter to next potion sprite
      spriteCounter++;
    }

    //display bar if useCounter > 0 ie delay before you can use another potion
    if(useCounter > 0)
    {
      g.setColor(new Color(150, 150, 150));
      g.fillRect(screenWidth / 2 + 200 * screenWidth / 1920,
                 screenHeight - 145 * screenHeight / 1080,
                 60 * screenWidth / 1920, 20 * screenHeight / 1080);
      g.setColor(Color.RED);
      g.fillRect(screenWidth / 2 + 200 * screenWidth / 1920,
                 screenHeight - 145 * screenHeight / 1080,
                 useCounter * screenWidth / 1920, 20 * screenHeight / 1080);
    }

    //draw empty health hearts
    for(int i = 0; i < player.totalHealth; i+= 2)
    {
      g.drawImage(assets[221].getScaledInstance(40 * screenWidth / 1920,
                                                40 * screenWidth / 1920,
                                                Image.SCALE_DEFAULT),
                  screenWidth / 2 - 280 * screenWidth / 1920 + i * 30 * screenWidth / 1920,
                  screenHeight - 165 * screenHeight / 1080,
                  null);
    }

    //draw health hearts over top of empty health hearts
    for(int i = 0; i < player.health; i+= 2)
    {
      if(i == player.health - 1)
      {
        g.drawImage(assets[223].getScaledInstance(40 * screenWidth / 1920,
                                                  40 * screenWidth / 1920,
                                                  Image.SCALE_DEFAULT),
                    screenWidth / 2 - 280 * screenWidth / 1920 + i * 30 * screenWidth / 1920,
                    screenHeight - 165 * screenHeight / 1080,
                    null);
      }
      else
      {
        g.drawImage(assets[222].getScaledInstance(40 * screenWidth / 1920,
                                                  40 * screenWidth / 1920,
                                                  Image.SCALE_DEFAULT),
                    screenWidth / 2 - 280 * screenWidth / 1920 + i * 30 * screenWidth / 1920,
                    screenHeight - 165 * screenHeight / 1080,
                    null);
      }
    }

    //draw score
    g.setColor(new Color(240, 139, 22));
    g.setFont(new Font("TimesRoman", Font.BOLD, 40 * screenWidth / 1920));
    g.drawString("Score: ",
                 screenWidth / 2 - 400 * screenWidth / 1920,
                 screenHeight - 120 * screenHeight / 1080);
    g.drawString("" + player.score,
                 screenWidth / 2 - 400 * screenWidth / 1920,
                 screenHeight - 70 * screenHeight / 1080);

  }

  //collision grid class used for checking if objects are colliding
  //for more info see CollisionGrid class
  public CollisionGrid collisionGrid;
  //grid scale size (the smallest hit box you can have is 30x30)
  public final int GRID_SCALE = 30;
  //arraylist of all game objects
  public ArrayList<Object> objects = new ArrayList<Object>();

  //returns all objects that are colliding with the passed object
  //see isColliding() for more info
  public Object[] collidingWith(Object obj1)
  {
    ArrayList<Object> collided = new ArrayList<Object>();
    for (Vector gridBox : obj1.collisionArea)
    {
      for(Object obj2 : collisionGrid.grid[gridBox.x][gridBox.y])
      {
        if(obj1 != obj2 && !(obj2 instanceof Interactable))
        {
          if(obj1.pos.x + obj1.w >= obj2.pos.x &&
             obj1.pos.x <= obj2.pos.x + obj2.w &&
             obj1.pos.y + obj1.h >= obj2.pos.y &&
             obj1.pos.y <= obj2.pos.y + obj2.h)
          {
            collided.add(obj2);
          }
        }
      }
    }
    if(collided.size() < 1)
    {
      return null;
    }
    return collided.toArray(new Object[collided.size()]);
  }

  //checks if the passed object if colliding with another object
  public boolean isColliding(Object obj1)
  {
    //checks all grid boxes the player is in
    //as the play can only possibly be colliding with objects that are
    //in the same grid boxes
    //see CollisionGrid class for more details
    for (Vector gridBox : obj1.collisionArea)
    {
      //checks all objects in those grid boxes against the passed object
      //to see if they are colliding
      for(Object obj2 : collisionGrid.grid[gridBox.x][gridBox.y])
      {
        //no point in checking itself against itself
        //dont want to collided with interactables here
        //dont want projectiles to collid with themselfs either
        if(obj1 != obj2 && !(obj2 instanceof Interactable) &&
           !(obj1 instanceof Projectile && obj2 instanceof Projectile))
        {
          //if they are collding they're hit boxes will overlap
          if(obj1.pos.x + obj1.w >= obj2.pos.x &&
             obj1.pos.x <= obj2.pos.x + obj2.w &&
             obj1.pos.y + obj1.h >= obj2.pos.y &&
             obj1.pos.y <= obj2.pos.y + obj2.h)
          {
            //checking to see if the first object is a projectile
            if(obj1 instanceof Projectile)
            {
              Projectile proj = (Projectile) obj1;

              //if the second object is not a player or enemy
              //make the projectile inert (ie stuck in a wall)
              if(!(obj2 instanceof Player) && !(obj2 instanceof Enemy))
              {
                proj.inert = true;
                proj.solid = false;
                collisionGrid.remove(proj);
              }
              else
              {
                return false;
              }
            }
            if (obj2 instanceof Projectile && obj1 instanceof Enemy)
            {
              continue;
            }
            //if colliding
            return true;
          }
        }
      }
    }
    //if not colliding
    return false;
  }

  //checks if the player is colliding with any enemies
  //see isColliding() for more info
  public void isCollidingWithEnemy(Player player)
  {
    //increase hit box to see if the enemy is touching
    //rather then colliding with the enemy
    player.pos.x--;
    player.pos.y--;
    player.h += 2;
    player.w += 2;
    collisionGrid.update(player);
    grid: for (Vector gridBox : player.collisionArea)
    {
      for(Object obj : collisionGrid.grid[gridBox.x][gridBox.y])
      {
        if(player != obj && (obj instanceof Enemy || obj instanceof Projectile))
        {
          if(player.pos.x + player.w >= obj.pos.x &&
             player.pos.x <= obj.pos.x + obj.w &&
             player.pos.y + player.h >= obj.pos.y &&
             player.pos.y <= obj.pos.y + obj.h)
          {
            if(obj instanceof Enemy)
            {
              Enemy enemy = (Enemy) obj;
              if(enemy.attackCounter == 0)
              {
                enemy.attackCounter = 20;
                enemy.moveToIntercept(player);
                player.health -= Math.ceil((double)enemy.attackDamage /
                                           (double)player.defenceModifer);
                player.vel = enemy.vel;
                player.vel.setMagnitude(player.maxSpeed);
                player.damagedCounter = 10;
                // if(clips[4].getFramePosition() == 0)
                // {
                //   clips[4].start();
                // }
                playClip(clips[4]);
              }
            }
            else
            {
              Projectile proj = (Projectile) obj;
              if(!proj.inert)
              {
                player.health -= Math.ceil((double)proj.damage /
                                           (double)player.defenceModifer);
                player.damagedCounter = 10;
                collisionGrid.remove(proj);
                objects.remove(proj);
                playClip(clips[4]);
                break grid;
              }
            }
          }
        }
      }
    }
    player.pos.x++;
    player.pos.y++;
    player.h -= 2;
    player.w -= 2;
    collisionGrid.update(player);

  }

  //checks if the players weapon is colliding with an enemy
  //see isColliding() for more info
  public Enemy isCollidingWithWeapon(Object weapon)
  {
    collisionGrid.insert(weapon);
    for (Vector gridBox : weapon.collisionArea)
    {
      for(Object obj : collisionGrid.grid[gridBox.x][gridBox.y])
      {
        if(weapon != obj && obj instanceof Enemy)
        {
          if(weapon.pos.x + weapon.w >= obj.pos.x &&
             weapon.pos.x <= obj.pos.x + obj.w &&
             weapon.pos.y + weapon.h >= obj.pos.y &&
             weapon.pos.y <= obj.pos.y + obj.h)
          {
            Enemy enemy = (Enemy) obj;
            playClip(clips[5]);
            collisionGrid.remove(weapon);
            return enemy;
          }
        }
      }
    }
    collisionGrid.remove(weapon);
    return null;
  }

  //checks if the player in interacting with any interactables
  //interactables include chests, doors, potions, coins... etc
  //see isColliding() for more info
  public boolean isInteracting(Player player)
  {
    for (Vector gridBox : player.collisionArea)
    {
      for(Object obj : collisionGrid.grid[gridBox.x][gridBox.y])
      {
        if(obj instanceof Interactable && ui.keysDown[UTIL1_KEY])
        {
          Interactable object = (Interactable) obj;
          if(object.usable)
          {
            if(player.pos.x + player.w >= object.pos.x &&
               player.pos.x <= object.pos.x + object.w &&
               player.pos.y + player.h >= object.pos.y &&
               player.pos.y <= object.pos.y + object.h)
            {
              //if the player uses the interactable

              //play interacting sound
              playClip(clips[2]);

              //use the interactable
              object.use(player);

              //if the interactable should be removed, remove it
              if(object.remove)
              {
                collisionGrid.remove(object);
                objects.remove(object);
              }
              //only returns true for it the play went through a door
              return false;
            }
          }
        }
        //if player is going through the door
        else if(obj instanceof Door)
        {
          Door door = (Door) obj;
          if(!door.usable && player.pos.y == door.pos.y + 19 && ui.keysDown[UP_KEY])
          {
            //update to next level
            currentLevel++;

            //save game
            save = new File("../saves/save.txt");
            save(save);

            //load next level
            objects.clear();
            loadSave(save);
            return true;
          }
        }
      }
    }
    //only returns true for it the play went through a door
    return false;
  }

  //checks what side of the hit box object1 is colliding with objects2 on
  //see isColliding() for more info
  public int collidingWhere(Object obj1, Object obj2)
  {
    if(obj1.pos.x + obj1.w >= obj2.pos.x) //right
    {
      return 1;
    }
    if(obj1.pos.x <= obj2.pos.x + obj2.w) //left
    {
      return 2;
    }
    if(obj1.pos.y + obj1.h >= obj2.pos.y) //bottom
    {
      return 3;
    }
    if(obj1.pos.y <= obj2.pos.y + obj2.h) //top
    {
      return 4;
    }
    return 0;
  }

  //loads ingame settings on start up
  //see startLevel() for more info
  private void loadConfig(File config)
  {
    String str;
    try
    {
      BufferedReader br = new BufferedReader(new FileReader(config));

      while((str = br.readLine()) != null)
      {
        String[] stringTokens = str.split("\\s+");
        String type = stringTokens[0];
        boolean token = Boolean.parseBoolean(stringTokens[1]);

        //load the various values for settings
        switch(type)
        {
          case "SHOWFPS":
            SHOWFPS = token;
          break;
          case "VSYNC":
            VSYNC = token;
          break;
          case "DEBUG":
            DEBUG = token;
          break;
          case "MUSIC":
            MUSIC = token;
          break;
          default:
            System.out.println("Possible corrupted congig File: " + config.getAbsolutePath());
            break;
        }
      }
    }
    catch(IOException e)
    {
      System.out.println("error loading config");
    }
  }

  //saves the config file, called after every change to settings
  private void saveConfig(File config)
  {
    try
    {
      //write to the config file the various game settings
      FileWriter fw = new FileWriter(config);
      fw.write("SHOWFPS " + SHOWFPS + "\n" +
               "VSYNC " + VSYNC + "\n" +
               "DEBUG " + DEBUG + "\n" +
               "MUSIC " + MUSIC);
      fw.close();
     }
     catch(IOException e){}
  }

  //load game save (player data)
  //see startLevel() for more info
  private void loadSave(File save)
  {
    String str;
    Player player = null;
    try
    {
      BufferedReader br = new BufferedReader(new FileReader(save));

      while((str = br.readLine()) != null)
      {
        String[] stringTokens = str.split("\\s+");
        String type = stringTokens[0];
        int token = Integer.parseInt(stringTokens[1]);

        //load each value for the player
        switch(type)
        {
          case "currentLevel":
            currentLevel = token;
            startLevel(token);
            if(objects.get(playerIndex) instanceof Player)
            {
              player = (Player) objects.get(playerIndex);
            }
            break;
          case "type":
            if(player != null)
            {
              player.type = token;
              switch(token)
              {
                case 0:
                  //load selected character sprites
                  playerIdle = new Image[4];
                  playerIdle[0] = assets[49].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerIdle[1] = assets[50].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerIdle[2] = assets[51].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerIdle[3] = assets[52].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerRun = new Image[4];
                  playerRun[0] = assets[53].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerRun[1] = assets[54].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerRun[2] = assets[55].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerRun[3] = assets[56].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerHit = assets[48].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  break;
                case 1:
                  //load selected character sprites
                  playerIdle = new Image[4];
                  playerIdle[0] = assets[58].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerIdle[1] = assets[59].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerIdle[2] = assets[60].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerIdle[3] = assets[61].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerRun = new Image[4];
                  playerRun[0] = assets[62].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerRun[1] = assets[63].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerRun[2] = assets[64].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerRun[3] = assets[65].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerHit = assets[57].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  break;
                case 2:
                  //load selected character sprites
                  playerIdle = new Image[4];
                  playerIdle[0] = assets[131].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerIdle[1] = assets[132].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerIdle[2] = assets[133].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerIdle[3] = assets[134].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerRun = new Image[4];
                  playerRun[0] = assets[135].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerRun[1] = assets[136].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerRun[2] = assets[137].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerRun[3] = assets[138].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerHit = assets[130].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  break;
                case 3:
                  //load selected character sprites
                  playerIdle = new Image[4];
                  playerIdle[0] = assets[140].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerIdle[1] = assets[141].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerIdle[2] = assets[142].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerIdle[3] = assets[143].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerRun = new Image[4];
                  playerRun[0] = assets[144].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerRun[1] = assets[145].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerRun[2] = assets[146].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerRun[3] = assets[147].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerHit = assets[139].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  break;
                case 4:
                  //load selected character sprites
                  playerIdle = new Image[4];
                  playerIdle[0] = assets[296].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerIdle[1] = assets[297].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerIdle[2] = assets[298].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerIdle[3] = assets[299].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerRun = new Image[4];
                  playerRun[0] = assets[300].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerRun[1] = assets[301].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerRun[2] = assets[302].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerRun[3] = assets[303].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerHit = assets[295].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  break;
                case 5:
                  //load selected character sprites
                  playerIdle = new Image[4];
                  playerIdle[0] = assets[305].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerIdle[1] = assets[306].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerIdle[2] = assets[307].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerIdle[3] = assets[308].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerRun = new Image[4];
                  playerRun[0] = assets[309].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerRun[1] = assets[310].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerRun[2] = assets[311].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerRun[3] = assets[312].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerHit = assets[304].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  break;
                case 6:
                  //load selected character sprites
                  playerIdle = new Image[4];
                  playerIdle[0] = assets[113].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerIdle[1] = assets[114].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerIdle[2] = assets[115].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerIdle[3] = assets[116].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerRun = new Image[4];
                  playerRun[0] = assets[117].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerRun[1] = assets[118].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerRun[2] = assets[119].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerRun[3] = assets[120].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  playerHit = assets[112].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
                  break;
                default:
                  System.out.println("Corrupted save File: " + levels[currentLevel].getAbsolutePath());
                  break;
              }
              player.setIdleSprites(playerIdle);
              player.setRunSprites(playerRun);
              player.setHitSprite(playerHit);
            }
            else
            {
              System.out.println("Corrupted save File: " + levels[currentLevel].getAbsolutePath());
            }
            break;
          case "weaponType":
            if(player != null)
            {
              player.weaponType = token;
            }
            else
            {
              System.out.println("Corrupted save File: " + levels[currentLevel].getAbsolutePath());
            }
            break;
          case "healthPotions":
            if(player != null)
            {
              player.healthPotions = token;
            }
            else
            {
              System.out.println("Corrupted save File: " + levels[currentLevel].getAbsolutePath());
            }
            break;
          case "bigHealthPotions":
            if(player != null)
            {
              player.bigHealthPotions = token;
            }
            else
            {
              System.out.println("Corrupted save File: " + levels[currentLevel].getAbsolutePath());
            }
            break;
          case "strengthPotions":
            if(player != null)
            {
              player.strengthPotions = token;
            }
            else
            {
              System.out.println("Corrupted save File: " + levels[currentLevel].getAbsolutePath());
            }
            break;
          case "bigStrengthPotions":
            if(player != null)
            {
              player.bigStrengthPotions = token;
            }
            else
            {
              System.out.println("Corrupted save File: " + levels[currentLevel].getAbsolutePath());
            }
            break;
          case "defencePotions":
            if(player != null)
            {
              player.defencePotions = token;
            }
            else
            {
              System.out.println("Corrupted save File: " + levels[currentLevel].getAbsolutePath());
            }
            break;
          case "bigDefencePotions":
            if(player != null)
            {
              player.bigDefencePotions = token;
            }
            else
            {
              System.out.println("Corrupted save File: " + levels[currentLevel].getAbsolutePath());
            }
            break;
          case "speedPotions":
            if(player != null)
            {
              player.speedPotions = token;
            }
            else
            {
              System.out.println("Corrupted save File: " + levels[currentLevel].getAbsolutePath());
            }
            break;
          case "bigSpeedPotions":
            if(player != null)
            {
              player.bigSpeedPotions = token;
            }
            else
            {
              System.out.println("Corrupted save File: " + levels[currentLevel].getAbsolutePath());
            }
            break;
          case "coins":
            if(player != null)
            {
              player.coins = token;
            }
            else
            {
              System.out.println("Corrupted save File: " + levels[currentLevel].getAbsolutePath());
            }
            break;
          case "score":
            if(player != null)
            {
              player.score = token;
            }
            else
            {
              System.out.println("Corrupted save File: " + levels[currentLevel].getAbsolutePath());
            }
            break;
          case "slain":
            if(player != null)
            {
              player.slain = token;
            }
            else
            {
              System.out.println("Corrupted save File: " + levels[currentLevel].getAbsolutePath());
            }
            break;
          default:
            System.out.println("Possible corrupted save File: " + levels[currentLevel].getAbsolutePath());
            break;
        }
      }
    }
    catch(IOException e)
    {
      System.out.println("error loading save");
    }
  }

  //save the game, only gets called on completion of a level
  private void save(File save)
  {
    Player player;
    if(objects.get(playerIndex) instanceof Player)
    {
      player = (Player) objects.get(playerIndex);

      try
      {
        //write the various player data to the save file
        FileWriter fw = new FileWriter(save);
        fw.write("currentLevel " + currentLevel + "\n" +
                 "type " + player.type + "\n" +
                 "weaponType " + player.weaponType + "\n" +
                 "healthPotions " + player.healthPotions + "\n" +
                 "bigHealthPotions " + player.bigHealthPotions + "\n" +
                 "strengthPotions " + player.strengthPotions + "\n" +
                 "bigStrengthPotions " + player.bigStrengthPotions + "\n" +
                 "defencePotions " + player.defencePotions + "\n" +
                 "bigDefencePotions " + player.bigDefencePotions + "\n" +
                 "speedPotions " + player.speedPotions + "\n" +
                 "bigSpeedPotions " + player.bigSpeedPotions + "\n" +
                 "coins " + player.coins + "\n" +
                 "score " + player.score + "\n" +
                 "slain " + player.slain + "");
        fw.close();
       }
       catch(IOException e){}
    }
    else
    {
      System.out.println("error finding player");
    }

  }

  //the index of the current level for levels[]
  //used for the index of startLevel()
  private int currentLevel = 0;

  //starts the level at the given index of Level[]
  //loads all game objects from level file
  private void startLevel(int index)
  {
    //setting currentlevel
    currentLevel = index;
    //various blank slate variables for loading data
    String str;
    Object obj;
    Entity ent;
    Enemy enem;
    Boss boss;
    Potion potion;
    Image[] sprites;

    try
    {
      //creating a reader for the level file
      BufferedReader br = new BufferedReader(new FileReader(levels[index]));

      //read the next line and if its not null (blank)
      //load the objects given by the keyword in the read line
      while((str = br.readLine()) != null)
      {
        //spltting the read line into tokens
        String[] stringTokens = str.split("\\s+");
        //the first token is the keyword
        String type = stringTokens[0];
        //the rest are number parameters for the keyword
        int[] tokens = new int[stringTokens.length - 1];
        for(int i = 1; i < stringTokens.length; i++)
        {
          tokens[i - 1] = Integer.parseInt(stringTokens[i]);
        }

        //create an object based on the keyword
        //set its position based on extra paramters
        //set the objects sprite(s) and if its solid (collidiable)
        //finally add the object to the objects arraylist
        //above each case lists the parameters require, if you dont
        //see any parameters, then it reqwuires the same ones as the above
        switch(type)
        {
          //required setup items
          //width, height
          case "level_size":
            collisionGrid = new CollisionGrid(GRID_SCALE, tokens[0], tokens[1]);
            break;
          //x, y
          case "player":
            Player player = new Player(tokens[0], tokens[1], 60, 90);
            player.type = playerType;
            player.solid = true;
            player.weaponSprites = Arrays.copyOfRange(assets, 274, 294);
            loadObj(player);
            break;

          //background objects

          //builds a section of flooring
          //top left corner x, y; bottom right corner x, y
          case "floor*":
            for(int i = tokens[0]; i <= tokens[2]; i+= 60)
            {
              for(int j = tokens[1]; j <= tokens[3]; j+= 60)
              {
                if(Math.floor(Math.random()*40) == 0)
                {
                  obj = new Object(i, j, 60, 60);
                  obj.setSprite(assets[204].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
                  obj.solid = false;
                  objects.add(obj);
                }
                if(Math.floor(Math.random()*10) == 0)
                {
                  obj = new Object(i, j, 60, 60);
                  obj.setSprite(assets[74 + (int)Math.ceil(Math.random()*7)].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
                  obj.solid = false;
                  objects.add(obj);
                }
                else
                {
                  obj = new Object(i, j, 60, 60);
                  obj.setSprite(assets[74].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
                  obj.solid = false;
                  objects.add(obj);
                }
              }
            }
            break;
          //x, y
          case "floor_1":
            obj = new Object(tokens[0], tokens[1], 60, 60);
            obj.setSprite(assets[74].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            obj.solid = false;
            objects.add(obj);
            break;
          case "floor_2":
            obj = new Object(tokens[0], tokens[1], 60, 60);
            obj.setSprite(assets[75].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            obj.solid = false;
            objects.add(obj);
            break;
          case "floor_3":
            obj = new Object(tokens[0], tokens[1], 60, 60);
            obj.setSprite(assets[76].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            obj.solid = false;
            objects.add(obj);
            break;
          case "floor_4":
            obj = new Object(tokens[0], tokens[1], 60, 60);
            obj.setSprite(assets[77].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            obj.solid = false;
            objects.add(obj);
            break;
          case "floor_5":
            obj = new Object(tokens[0], tokens[1], 60, 60);
            obj.setSprite(assets[78].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            obj.solid = false;
            objects.add(obj);
            break;
          case "floor_6":
            obj = new Object(tokens[0], tokens[1], 60, 60);
            obj.setSprite(assets[79].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            obj.solid = false;
            objects.add(obj);
            break;
          case "floor_7":
            obj = new Object(tokens[0], tokens[1], 60, 60);
            obj.setSprite(assets[80].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            obj.solid = false;
            objects.add(obj);
            break;
          case "floor_8":
            obj = new Object(tokens[0], tokens[1], 60, 60);
            obj.setSprite(assets[81].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            obj.solid = false;
            objects.add(obj);
            break;
          case "skull":
            obj = new Object(tokens[0], tokens[1], 60, 60);
            obj.setSprite(assets[204].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            obj.solid = false;
            objects.add(obj);
            break;

          //walls and other standard collidable objects
          //x, y build the full column, top, mid and bottom
          case "column":
            obj = new Object(tokens[0], tokens[1], 60, 60);
            obj.setSprite(assets[38].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            objects.add(obj);
            obj.solid = false;
            obj = new Object(tokens[0], tokens[1] + 60, 60, 60);
            obj.setSprite(assets[37].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            objects.add(obj);
            obj.solid = false;
            obj = new Object(tokens[0], tokens[1] + 120, 60, 30);
            obj.setSprite(assets[39].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            obj.solid = false;
            objects.add(obj);
            break;
          //x, y
          case "column_mid":
            obj = new Object(tokens[0], tokens[1], 60, 60);
            obj.setSprite(assets[37].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            obj.solid = false;
            objects.add(obj);
            break;
          case "column_top":
            obj = new Object(tokens[0], tokens[1], 60, 60);
            obj.setSprite(assets[38].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            obj.solid = false;
            objects.add(obj);
            break;
          case "column_base":
            obj = new Object(tokens[0], tokens[1], 60, 30);
            obj.setSprite(assets[39].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            obj.solid = false;
            objects.add(obj);
            break;
          case "crate":
            obj = new Object(tokens[0], tokens[1], 60, 60);
            obj.setSprite(assets[40].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            objects.add(obj);
            break;
          case "doors_frame_left":
            obj = new Object(tokens[0], tokens[1], 60, 120);
            obj.setSprite(assets[42].getScaledInstance(60, 120, Image.SCALE_DEFAULT));
            objects.add(obj);
            break;
          case "doors_frame_right":
            obj = new Object(tokens[0], tokens[1], 60, 120);
            obj.setSprite(assets[43].getScaledInstance(60, 120, Image.SCALE_DEFAULT));
            objects.add(obj);
            break;
          case "doors_frame_top":
            obj = new Object(tokens[0], tokens[1], 120, 30);
            obj.setSprite(assets[44].getScaledInstance(120, 12, Image.SCALE_DEFAULT));
            objects.add(obj);
            break;
          //builds a section of wall, left to right not up to down
          //left x, y, right x
          case "wall*":
            obj = new Object(tokens[0], tokens[1], 60, 60);
            obj.setSprite(assets[262].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            objects.add(obj);
            obj = new Object(tokens[2], tokens[1], 60, 60);
            obj.setSprite(assets[264].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            objects.add(obj);
            for(int i = tokens[0] + 60; i < tokens[2]; i+= 60)
            {
              if(Math.floor(Math.random()*10) == 0)
              {
                if(Math.floor(Math.random()*4) == 0)
                {
                  obj = new Object(i, tokens[1], 60, 60);
                  obj.setSprite(assets[255].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
                  objects.add(obj);
                }
                else
                {
                  obj = new Object(i, tokens[1], 60, 60);
                  obj.setSprite(assets[254].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
                  objects.add(obj);
                }
              }
              else
              {
                obj = new Object(i, tokens[1], 60, 60);
                obj.setSprite(assets[263].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
                objects.add(obj);
              }
            }
            break;
          //x, y
          case "wall_banner_blue":
            obj = new Object(tokens[0], tokens[1], 60, 60);
            obj.setSprite(assets[224].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            objects.add(obj);
            break;
          case "wall_banner_green":
            obj = new Object(tokens[0], tokens[1], 60, 60);
            obj.setSprite(assets[225].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            objects.add(obj);
            break;
          case "wall_banner_red":
            obj = new Object(tokens[0], tokens[1], 60, 60);
            obj.setSprite(assets[226].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            objects.add(obj);
            break;
          case "wall_banner_yellow":
            obj = new Object(tokens[0], tokens[1], 60, 60);
            obj.setSprite(assets[227].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            objects.add(obj);
            break;
          //same as regular column but made to blend into wall
          //x, y
          case "wall_column":
            obj = new Object(tokens[0], tokens[1], 60, 60);
            obj.setSprite(assets[229].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            obj.solid = false;
            objects.add(obj);
            obj = new Object(tokens[0], tokens[1] + 60, 60, 60);
            obj.setSprite(assets[228].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            objects.add(obj);
            obj = new Object(tokens[0], tokens[1] + 120, 60, 30);
            obj.setSprite(assets[39].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            obj.solid = false;
            objects.add(obj);
            break;
          case "wall_column_mid":
            obj = new Object(tokens[0], tokens[1], 60, 60);
            obj.setSprite(assets[228].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            objects.add(obj);
            break;
          case "wall_column_top":
            obj = new Object(tokens[0], tokens[1], 60, 60);
            obj.setSprite(assets[229].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            obj.solid = false;
            objects.add(obj);
            break;
          case "wall_column_base":
            obj = new Object(tokens[0], tokens[1], 60, 30);
            obj.setSprite(assets[238].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            obj.solid = false;
            objects.add(obj);
            break;
          case "wall_corner_front_left":
            obj = new Object(tokens[0], tokens[1], 60, 60);
            obj.setSprite(assets[232].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            objects.add(obj);
            break;
          case "wall_corner_front_right":
            obj = new Object(tokens[0], tokens[1], 60, 60);
            obj.setSprite(assets[233].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            objects.add(obj);
            break;
          case "wall_corner_left":
            obj = new Object(tokens[0], tokens[1], 60, 30);
            obj.setSprite(assets[234].getScaledInstance(60, 30, Image.SCALE_DEFAULT));
            objects.add(obj);
            break;
          case "wall_corner_right":
            obj = new Object(tokens[0], tokens[1], 60, 30);
            obj.setSprite(assets[235].getScaledInstance(60, 30, Image.SCALE_DEFAULT));
            objects.add(obj);
            break;
          case "wall_corner_top_left":
            obj = new Object(tokens[0], tokens[1], 60, 60);
            obj.setSprite(assets[236].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            objects.add(obj);
            break;
          case "wall_corner_top_right":
            obj = new Object(tokens[0], tokens[1], 60, 60);
            obj.setSprite(assets[236].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            objects.add(obj);
            break;
          //simialr to columns, builds the whole fountain
          //x, y
          case "wall_fountain_blue":
            obj = new Object(tokens[0], tokens[1], 60, 30);
            obj.setSprite(assets[251].getScaledInstance(60, 30, Image.SCALE_DEFAULT));
            objects.add(obj);
            obj = new Object(tokens[0], tokens[1] + 30, 60, 60);
            obj.setSprite(assets[245].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            sprites = new Image[3];
            sprites[0] = assets[245].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            sprites[1] = assets[246].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            sprites[2] = assets[247].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            obj.setIdleSprites(sprites);
            objects.add(obj);
            obj = new Object(tokens[0], tokens[1] + 90, 60, 30);
            obj.setSprite(assets[239].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            sprites = new Image[3];
            sprites[0] = assets[239].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            sprites[1] = assets[240].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            sprites[2] = assets[241].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            obj.setIdleSprites(sprites);
            obj.solid = false;
            objects.add(obj);
            break;
          case "wall_fountain_red":
            obj = new Object(tokens[0], tokens[1], 60, 30);
            obj.setSprite(assets[251].getScaledInstance(60, 30, Image.SCALE_DEFAULT));
            objects.add(obj);
            obj = new Object(tokens[0], tokens[1] + 30, 60, 60);
            obj.setSprite(assets[248].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            sprites = new Image[3];
            sprites[0] = assets[248].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            sprites[1] = assets[249].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            sprites[2] = assets[250].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            obj.setIdleSprites(sprites);
            objects.add(obj);
            obj = new Object(tokens[0], tokens[1] + 90, 60, 30);
            obj.setSprite(assets[242].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            sprites = new Image[3];
            sprites[0] = assets[242].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            sprites[1] = assets[243].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            sprites[2] = assets[243].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            obj.setIdleSprites(sprites);
            obj.solid = false;
            objects.add(obj);
            break;
          case "wall_fountain_basin_blue":
            obj = new Object(tokens[0], tokens[1], 60, 30);
            obj.setSprite(assets[239].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            sprites = new Image[3];
            sprites[0] = assets[239].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            sprites[1] = assets[240].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            sprites[2] = assets[241].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            obj.setIdleSprites(sprites);
            obj.solid = false;
            objects.add(obj);
            break;
          case "wall_fountain_basin_red":
            obj = new Object(tokens[0], tokens[1], 60, 30);
            obj.setSprite(assets[242].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            sprites = new Image[3];
            sprites[0] = assets[242].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            sprites[1] = assets[243].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            sprites[2] = assets[244].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            obj.setIdleSprites(sprites);
            obj.solid = false;
            objects.add(obj);
            break;
          case "wall_fountain_mid_blue":
            obj = new Object(tokens[0], tokens[1], 60, 60);
            obj.setSprite(assets[245].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            sprites = new Image[3];
            sprites[0] = assets[245].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            sprites[1] = assets[246].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            sprites[2] = assets[247].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            obj.setIdleSprites(sprites);
            objects.add(obj);
            break;
          case "wall_fountain_mid_red":
            obj = new Object(tokens[0], tokens[1], 60, 60);
            obj.setSprite(assets[248].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            sprites = new Image[3];
            sprites[0] = assets[248].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            sprites[1] = assets[249].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            sprites[2] = assets[250].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            obj.setIdleSprites(sprites);
            objects.add(obj);
            break;
          case "wall_fountain_top":
            obj = new Object(tokens[0], tokens[1], 60, 30);
            obj.setSprite(assets[251].getScaledInstance(60, 30, Image.SCALE_DEFAULT));
            objects.add(obj);
            break;
          case "wall_goo":
            obj = new Object(tokens[0], tokens[1], 60, 60);
            obj.setSprite(assets[252].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            objects.add(obj);
            break;
          case "wall_goo_base":
            obj = new Object(tokens[0], tokens[1], 60, 60);
            obj.setSprite(assets[253].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            obj.solid = false;
            objects.add(obj);
            break;
          case "wall_hole_1":
            obj = new Object(tokens[0], tokens[1], 60, 60);
            obj.setSprite(assets[254].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            objects.add(obj);
            break;
          case "wall_hole_2":
            obj = new Object(tokens[0], tokens[1], 60, 60);
            obj.setSprite(assets[255].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            objects.add(obj);
            break;
          case "wall_inner_corner_top_left":
            obj = new Object(tokens[0], tokens[1], 60, 30);
            obj.setSprite(assets[236].getScaledInstance(60, 30, Image.SCALE_DEFAULT));
            objects.add(obj);
            break;
          case "wall_inner_corner_top_right":
            obj = new Object(tokens[0], tokens[1], 60, 30);
            obj.setSprite(assets[237].getScaledInstance(60, 30, Image.SCALE_DEFAULT));
            objects.add(obj);
            break;
          case "wall_inner_corner_mid_left":
            obj = new Object(tokens[0], tokens[1], 60, 60);
            obj.setSprite(assets[234].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            objects.add(obj);
            break;
          case "wall_inner_corner_mid_right":
            obj = new Object(tokens[0], tokens[1], 60, 60);
            obj.setSprite(assets[235].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            objects.add(obj);
            break;
          case "wall_left":
            obj = new Object(tokens[0], tokens[1], 60, 60);
            obj.setSprite(assets[262].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            objects.add(obj);
            break;
          case "wall_mid":
            obj = new Object(tokens[0], tokens[1], 60, 60);
            obj.setSprite(assets[263].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            objects.add(obj);
            break;
          case "wall_right":
            obj = new Object(tokens[0], tokens[1], 60, 60);
            obj.setSprite(assets[264].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            objects.add(obj);
            break;
          //similar to wall*, builds a section of wall, up to down
          //left and right (the one below) determin which side of the
          //screen they're used on
          //x, y1, y2
          case "wall_side_left*":
            obj = new Object(tokens[0], tokens[1], 30, 30);
            obj.setSprite(assets[269].getScaledInstance(30, 30, Image.SCALE_DEFAULT));
            obj.solid = false;
            objects.add(obj);
            for(int i = tokens[1] + 30; i <= tokens[2]; i+= 60)
            {
              obj = new Object(tokens[0], i, 30, 60);
              obj.setSprite(assets[267].getScaledInstance(30, 60, Image.SCALE_DEFAULT));
              objects.add(obj);
            }
            break;
          case "wall_side_right*":
            obj = new Object(tokens[0], tokens[1], 30, 30);
            obj.setSprite(assets[270].getScaledInstance(30, 30, Image.SCALE_DEFAULT));
            obj.solid = false;
            objects.add(obj);
            for(int i = tokens[1] + 30; i <= tokens[2]; i+= 60)
            {
              obj = new Object(tokens[0], i, 30, 60);
              obj.setSprite(assets[268].getScaledInstance(30, 60, Image.SCALE_DEFAULT));
              objects.add(obj);
            }
            break;
          //x, y
          case "wall_side_front_left":
            obj = new Object(tokens[0], tokens[1], 30, 60);
            obj.setSprite(assets[265].getScaledInstance(30, 60, Image.SCALE_DEFAULT));
            objects.add(obj);
            break;
          case "wall_side_front_right":
            obj = new Object(tokens[0], tokens[1], 30, 60);
            obj.setSprite(assets[266].getScaledInstance(30, 60, Image.SCALE_DEFAULT));
            objects.add(obj);
            break;
          case "wall_side_mid_left":
            obj = new Object(tokens[0], tokens[1], 30, 60);
            obj.setSprite(assets[267].getScaledInstance(30, 60, Image.SCALE_DEFAULT));
            objects.add(obj);
            break;
          case "wall_side_mid_right":
            obj = new Object(tokens[0], tokens[1], 30, 60);
            obj.setSprite(assets[268].getScaledInstance(30, 60, Image.SCALE_DEFAULT));
            objects.add(obj);
            break;
          case "wall_side_top_left":
            obj = new Object(tokens[0], tokens[1], 30, 30);
            obj.setSprite(assets[269].getScaledInstance(30, 30, Image.SCALE_DEFAULT));
            objects.add(obj);
            break;
          case "wall_side_top_right":
            obj = new Object(tokens[0], tokens[1], 30, 30);
            obj.setSprite(assets[270].getScaledInstance(30, 30, Image.SCALE_DEFAULT));
            objects.add(obj);
            break;
          //like wall* but for the top edging
          //x1, y, x2
          case "wall_top*":
            obj = new Object(tokens[0], tokens[1], 60, 30);
            obj.setSprite(assets[271].getScaledInstance(60, 30, Image.SCALE_DEFAULT));
            obj.solid = false;
            objects.add(obj);
            obj = new Object(tokens[2], tokens[1], 60, 30);
            obj.setSprite(assets[273].getScaledInstance(60, 30, Image.SCALE_DEFAULT));
            obj.solid = false;
            objects.add(obj);
            for(int i = tokens[0] + 60; i < tokens[2]; i+= 60)
            {
              obj = new Object(i, tokens[1], 60, 30);
              obj.setSprite(assets[272].getScaledInstance(60, 30, Image.SCALE_DEFAULT));
              obj.solid = false;
              objects.add(obj);
            }
            break;
          //x, y
          case "wall_top_left":
            obj = new Object(tokens[0], tokens[1], 60, 30);
            obj.setSprite(assets[271].getScaledInstance(60, 30, Image.SCALE_DEFAULT));
            obj.solid = false;
            objects.add(obj);
            break;
          case "wall_top_mid":
            obj = new Object(tokens[0], tokens[1], 60, 30);
            obj.setSprite(assets[272].getScaledInstance(60, 30, Image.SCALE_DEFAULT));
            obj.solid = false;
            objects.add(obj);
            break;
          case "wall_top_right":
            obj = new Object(tokens[0], tokens[1], 60, 30);
            obj.setSprite(assets[273].getScaledInstance(60, 30, Image.SCALE_DEFAULT));
            obj.solid = false;
            objects.add(obj);
            break;

          //Interactable Objects
          //x, y, type
          case "chest":
            Chest chest = new Chest(tokens[0], tokens[1], 60, 60, tokens[2]);
            chest.setSprite(assets[16].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            sprites = new Image[3];
            sprites[0] = assets[17].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            sprites[1] = assets[18].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            sprites[2] = assets[18].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            chest.setIdleSprites(sprites);
            objects.add(chest);
            break;
          //x, y
          case "coin":
            Coin coin = new Coin(tokens[0], tokens[1], 30, 30);
            coin.setSprite(assets[33].getScaledInstance(30, 30, Image.SCALE_DEFAULT));
            sprites = new Image[4];
            sprites[0] = assets[33].getScaledInstance(30, 30, Image.SCALE_DEFAULT);
            sprites[1] = assets[34].getScaledInstance(30, 30, Image.SCALE_DEFAULT);
            sprites[2] = assets[35].getScaledInstance(30, 30, Image.SCALE_DEFAULT);
            sprites[3] = assets[36].getScaledInstance(30, 30, Image.SCALE_DEFAULT);
            coin.setIdleSprites(sprites);
            objects.add(coin);
            break;
          case "door":
            Door door = new Door(tokens[0], tokens[1], 120, 120);
            door.setSprite(assets[45].getScaledInstance(120, 120, Image.SCALE_DEFAULT));
            if(tokens.length > 2)
            {
              if(tokens[2] == 1)
              {
                door.locked = true;
              }
            }
            sprites = new Image[1];
            sprites[0] = assets[46].getScaledInstance(120, 120, Image.SCALE_DEFAULT);
            door.setIdleSprites(sprites);
            objects.add(door);
            break;
          case "health_potion":
            potion = new Potion(tokens[0], tokens[1], 60, 60, 0);
            potion.setSprite(assets[72].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            objects.add(potion);
            break;
          case "strength_potion":
            potion = new Potion(tokens[0], tokens[1], 60, 60, 1);
            potion.setSprite(assets[71].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            objects.add(potion);
            break;
          case "speed_potion":
            potion = new Potion(tokens[0], tokens[1], 60, 60, 2);
            potion.setSprite(assets[70].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            objects.add(potion);
            break;
          case "defence_potion":
            potion = new Potion(tokens[0], tokens[1], 60, 60, 3);
            potion.setSprite(assets[73].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            objects.add(potion);
            break;
          case "big_health_potion":
            potion = new Potion(tokens[0], tokens[1], 60, 60, 4);
            potion.setSprite(assets[68].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            objects.add(potion);
            break;
          case "big_strength_potion":
            potion = new Potion(tokens[0], tokens[1], 60, 60, 5);
            potion.setSprite(assets[67].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            objects.add(potion);
            break;
          case "big_speed_potion":
            potion = new Potion(tokens[0], tokens[1], 60, 60, 6);
            potion.setSprite(assets[66].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            objects.add(potion);
            break;
          case "big_defence_potion":
            potion = new Potion(tokens[0], tokens[1], 60, 60, 7);
            potion.setSprite(assets[69].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            objects.add(potion);
            break;

          //Enemies
          //x, y, aggrobox: top left x,y; bottom right x,y
          //x, y, aggrobox top left x, y; bottom rigth x, y
          case "orc":
           enem = new Enemy(tokens[0], tokens[1], 60, 75, 0);
           enem.maxSpeed = 4;
           enem.health = 3;
           enem.totalHealth = 3;
           enem.bounds[0] = new Vector(tokens[2], tokens[3]);
           enem.bounds[1] = new Vector(tokens[4], tokens[5]);
           sprites = new Image[4];
           sprites[0] = assets[188].getScaledInstance(60, 75, Image.SCALE_DEFAULT);
           sprites[1] = assets[189].getScaledInstance(60, 75, Image.SCALE_DEFAULT);
           sprites[2] = assets[190].getScaledInstance(60, 75, Image.SCALE_DEFAULT);
           sprites[3] = assets[191].getScaledInstance(60, 75, Image.SCALE_DEFAULT);
           enem.setIdleSprites(sprites);
           sprites = new Image[4];
           sprites[0] = assets[192].getScaledInstance(60, 75, Image.SCALE_DEFAULT);
           sprites[1] = assets[193].getScaledInstance(60, 75, Image.SCALE_DEFAULT);
           sprites[2] = assets[194].getScaledInstance(60, 75, Image.SCALE_DEFAULT);
           sprites[3] = assets[195].getScaledInstance(60, 75, Image.SCALE_DEFAULT);
           enem.setRunSprites(sprites);
           enem.setHitSprite(assets[193].getScaledInstance(60, 75, Image.SCALE_DEFAULT));
           objects.add(enem);
           break;
          case "orc_shaman":
            enem = new Enemy(tokens[0], tokens[1], 60, 75, 1);
            enem.maxSpeed = 3;
            enem.health = 3;
            enem.totalHealth = 3;
            enem.bounds[0] = new Vector(tokens[2], tokens[3]);
            enem.bounds[1] = new Vector(tokens[4], tokens[5]);
            sprites = new Image[4];
            sprites[0] = assets[180].getScaledInstance(60, 75, Image.SCALE_DEFAULT);
            sprites[1] = assets[181].getScaledInstance(60, 75, Image.SCALE_DEFAULT);
            sprites[2] = assets[182].getScaledInstance(60, 75, Image.SCALE_DEFAULT);
            sprites[3] = assets[183].getScaledInstance(60, 75, Image.SCALE_DEFAULT);
            enem.setIdleSprites(sprites);
            sprites = new Image[4];
            sprites[0] = assets[184].getScaledInstance(60, 75, Image.SCALE_DEFAULT);
            sprites[1] = assets[185].getScaledInstance(60, 75, Image.SCALE_DEFAULT);
            sprites[2] = assets[186].getScaledInstance(60, 75, Image.SCALE_DEFAULT);
            sprites[3] = assets[187].getScaledInstance(60, 75, Image.SCALE_DEFAULT);
            enem.setRunSprites(sprites);
            enem.setHitSprite(assets[185].getScaledInstance(60, 75, Image.SCALE_DEFAULT));
            objects.add(enem);
            break;
          case "big_zombie":
            enem = new Enemy(tokens[0], tokens[1], 120, 120, 0);
            enem.maxSpeed = 3;
            enem.health = 8;
            enem.totalHealth = 8;
            enem.attackDamage = 3;
            enem.bounds[0] = new Vector(tokens[2], tokens[3]);
            enem.bounds[1] = new Vector(tokens[4], tokens[5]);
            sprites = new Image[4];
            sprites[0] = assets[8].getScaledInstance(120, 130, Image.SCALE_DEFAULT);
            sprites[1] = assets[9].getScaledInstance(120, 130, Image.SCALE_DEFAULT);
            sprites[2] = assets[10].getScaledInstance(120, 130, Image.SCALE_DEFAULT);
            sprites[3] = assets[11].getScaledInstance(120, 130, Image.SCALE_DEFAULT);
            enem.setIdleSprites(sprites);
            sprites = new Image[4];
            sprites[0] = assets[12].getScaledInstance(120, 130, Image.SCALE_DEFAULT);
            sprites[1] = assets[13].getScaledInstance(120, 130, Image.SCALE_DEFAULT);
            sprites[2] = assets[14].getScaledInstance(120, 130, Image.SCALE_DEFAULT);
            sprites[3] = assets[15].getScaledInstance(120, 130, Image.SCALE_DEFAULT);
            enem.setRunSprites(sprites);
            enem.setHitSprite(assets[13].getScaledInstance(120, 130, Image.SCALE_DEFAULT));
            objects.add(enem);
            break;
          case "chort":
            enem = new Enemy(tokens[0], tokens[1], 60, 90, 0);
            enem.maxSpeed = 5;
            enem.health = 3;
            enem.totalHealth = 3;
            enem.attackDamage = 2;
            enem.bounds[0] = new Vector(tokens[2], tokens[3]);
            enem.bounds[1] = new Vector(tokens[4], tokens[5]);
            sprites = new Image[4];
            sprites[0] = assets[25].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            sprites[1] = assets[26].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            sprites[2] = assets[27].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            sprites[3] = assets[28].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            enem.setIdleSprites(sprites);
            sprites = new Image[4];
            sprites[0] = assets[29].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            sprites[1] = assets[30].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            sprites[2] = assets[31].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            sprites[3] = assets[32].getScaledInstance(60, 90, Image.SCALE_DEFAULT);
            enem.setRunSprites(sprites);
            enem.setHitSprite(assets[28].getScaledInstance(60, 90, Image.SCALE_DEFAULT));
            objects.add(enem);
            break;
          case "necromancer":
            enem = new Enemy(tokens[0], tokens[1], 60, 75, 1);
            enem.maxSpeed = 4;
            enem.health = 4;
            enem.totalHealth = 4;
            enem.bounds[0] = new Vector(tokens[2], tokens[3]);
            enem.bounds[1] = new Vector(tokens[4], tokens[5]);
            sprites = new Image[4];
            sprites[0] = assets[164].getScaledInstance(60, 75, Image.SCALE_DEFAULT);
            sprites[1] = assets[165].getScaledInstance(60, 75, Image.SCALE_DEFAULT);
            sprites[2] = assets[166].getScaledInstance(60, 75, Image.SCALE_DEFAULT);
            sprites[3] = assets[167].getScaledInstance(60, 75, Image.SCALE_DEFAULT);
            enem.setIdleSprites(sprites);
            sprites = new Image[4];
            sprites[0] = assets[168].getScaledInstance(60, 75, Image.SCALE_DEFAULT);
            sprites[1] = assets[169].getScaledInstance(60, 75, Image.SCALE_DEFAULT);
            sprites[2] = assets[170].getScaledInstance(60, 75, Image.SCALE_DEFAULT);
            sprites[3] = assets[171].getScaledInstance(60, 75, Image.SCALE_DEFAULT);
            enem.setRunSprites(sprites);
            enem.setHitSprite(assets[167].getScaledInstance(60, 75, Image.SCALE_DEFAULT));
            objects.add(enem);
            break;
          case "swampy":
            enem = new Enemy(tokens[0], tokens[1], 60, 60, 0);
            enem.maxSpeed = 2;
            enem.health = 5;
            enem.totalHealth = 5;
            enem.bounds[0] = new Vector(tokens[2], tokens[3]);
            enem.bounds[1] = new Vector(tokens[4], tokens[5]);
            sprites = new Image[4];
            sprites[0] = assets[205].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            sprites[1] = assets[206].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            sprites[2] = assets[207].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            sprites[3] = assets[208].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            enem.setIdleSprites(sprites);
            sprites = new Image[4];
            sprites[0] = assets[209].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            sprites[1] = assets[210].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            sprites[2] = assets[211].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            sprites[3] = assets[212].getScaledInstance(60, 60, Image.SCALE_DEFAULT);
            enem.setRunSprites(sprites);
            enem.setHitSprite(assets[209].getScaledInstance(60, 60, Image.SCALE_DEFAULT));
            objects.add(enem);
            break;
          case "boss_demon":
            boss = new Boss(tokens[0], tokens[1], 240, 270);
            boss.maxSpeed = 4;
            boss.health = 100;
            boss.totalHealth = 100;
            boss.attackDamage = 4;
            boss.bounds[0] = new Vector(tokens[2], tokens[3]);
            boss.bounds[1] = new Vector(tokens[4], tokens[5]);
            sprites = new Image[4];
            sprites[0] = assets[0].getScaledInstance(240, 270, Image.SCALE_DEFAULT);
            sprites[1] = assets[1].getScaledInstance(240, 270, Image.SCALE_DEFAULT);
            sprites[2] = assets[2].getScaledInstance(240, 270, Image.SCALE_DEFAULT);
            sprites[3] = assets[3].getScaledInstance(240, 270, Image.SCALE_DEFAULT);
            boss.setIdleSprites(sprites);
            sprites = new Image[4];
            sprites[0] = assets[4].getScaledInstance(240, 270, Image.SCALE_DEFAULT);
            sprites[1] = assets[5].getScaledInstance(240, 270, Image.SCALE_DEFAULT);
            sprites[2] = assets[6].getScaledInstance(240, 270, Image.SCALE_DEFAULT);
            sprites[3] = assets[7].getScaledInstance(240, 270, Image.SCALE_DEFAULT);
            boss.setRunSprites(sprites);
            boss.setHitSprite(assets[2].getScaledInstance(240, 270, Image.SCALE_DEFAULT));
            objects.add(boss);
            break;

          //object not found / corrupted level
          default:
            System.out.println("Possible corrupted level File: " + levels[index].getAbsolutePath());
            break;
        }

      }
    }
    catch(IOException e)
    {
      System.out.println("error loading level");
    }

    //finding the player in the level
    //setting the playerindex to the index the player is
    //at in the objects arraylist
    for(int i = 0; i < objects.size(); i++)
    {
      if(objects.get(i) instanceof Player)
      {
        playerIndex = i;
      }
    }

    //add all objects to the grid
    for(Object object : objects)
    {
      if(object.solid == true || object instanceof Interactable)
      {
        object.collisionArea.clear();
        collisionGrid.insert(object);
      }
    }
  }

}
