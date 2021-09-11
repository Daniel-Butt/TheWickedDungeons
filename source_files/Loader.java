/*
class for loading various game files
*/

//importing required libraries
import java.io.*;
import javax.imageio.*;
import java.awt.Image;
import java.util.ArrayList;
import java.awt.Dimension;

public class Loader
{
  //all below methods are quite similar

  //load game sprites
  public static Image[] loadAssets(File folder)
  {
    //create a temporary arraylist for storing loaded images
    ArrayList<Image> assets = new ArrayList<Image>();
    //counter for how many images were loaded
    int counter = 0;

    //for all the files in the assets folder
    for(File asset : folder.listFiles())
    {
      try
      {
        //add image to arraylist
        assets.add(ImageIO.read(asset));
        //increase counter
        counter++;
      }
      catch(IOException e)
      {
        System.out.println("error loading assets");

      }
    }

    //create an array from the arraylist and return it
    return assets.toArray(new Image[counter]);
  }

  //load title screen animation files
  public static Image[] loadTitle(File folder, Dimension screenSize)
  {
    ArrayList<Image> titles = new ArrayList<Image>();
    int counter = 0;

    for(File title : folder.listFiles())
    {
      try
      {
        titles.add(ImageIO.read(title).getScaledInstance(screenSize.width,
                                                         screenSize.height,
                                                         Image.SCALE_SMOOTH));
        counter++;
      }
      catch(IOException e)
      {
        System.out.println("error loading assets");
        //System.out.println(e.getCause());
      }
    }
    //System.out.println(counter);
    return titles.toArray(new Image[counter]);
  }

  //load game levels
  public static File[] loadLevels(File folder)
  {
    ArrayList<File> files = new ArrayList<File>();
    int counter = 0;

    for(File file : folder.listFiles())
    {
      files.add(file);
      counter++;
    }

    return files.toArray(new File[counter]);
  }

  //load game sounds
  public static File[] loadSounds(File folder)
  {
    ArrayList<File> sounds = new ArrayList<File>();
    int counter = 0;

    for(File sound : folder.listFiles())
    {
      sounds.add(sound);
      counter++;
    }
    //System.out.println(counter);
    return sounds.toArray(new File[counter]);
  }


}
