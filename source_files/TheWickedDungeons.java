/*
Title: The Wicked Dungeons
Creator: Daniel Butt
Date: May - June 2020
Purpose: To create an 2D game using Java in the Adventure/RPG style

- as a side note, I did not and do not take any credit for any of the artwork as
I did not create them.
asset pack: https://0x72.itch.io/dungeontileset-ii
title screen animation: https://coub.com/view/1mk7t2
*/

//importing all require libraries
import java.util.*;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.imageio.*;
import java.io.File;
import java.io.IOException;

//main class
class TheWickedDungeons
{
  //main method
  public static void main(String[] args)
  {
    //creating an engine
    Engine engine = new Engine("The Wicked Dungeons");

    //setting various directories
    engine.setAssetsDirectory("../assets/frames/");
    engine.setTitleDirectory("../titlescreen/");
    engine.setLevelsDirectory("../levels/");
    engine.setSoundsDirectory("../sounds/");

    //setting up the engine
    engine.setup();

    //running the engine
    engine.run();

    //see engine class for more details
  }

}
