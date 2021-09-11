/*
just a simply vector class for easy use of vectors
*/

public class Vector
{
  //cartesian vector values
  public int x, y, z;

  //constructor 2D
  Vector(int x, int y)
  {
    this.x = x;
    this.y = y;
    z = 0;
  }

  //constructor 3D
  Vector(int x, int y, int z)
  {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  //add a vector to this vector
  public void add(Vector v)
  {
    x += v.x;
    y += v.y;
    z += v.z;
  }

  //multiply this vecotr by a factor
  public void mult(int factor)
  {
    x *= factor;
    y *= factor;
    z *= factor;
  }

  //get the magnitude of the vector
  public int magnitude()
  {
    return (int)(Math.sqrt(x*x + y*y + z*z));
  }

  //set the magnitude of the vector to a specifice value
  public void setMagnitude(int newMag)
  {
    Double magnitude = Math.sqrt(x*x + y*y + z*z);

    x = (int)(x * newMag / magnitude);
    y = (int)(y * newMag / magnitude);
    z = (int)(z * newMag / magnitude);
  }

}
