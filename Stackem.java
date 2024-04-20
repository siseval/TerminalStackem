import java.time.*;
import raster.*;
import java.util.Scanner;
import java.util.ArrayList;

class Stackem
{
  int height, width;
  Scanner s = new Scanner(System.in);

  Raster raster;

  ArrayList<Piece> pieces = new ArrayList<Piece>();

  int inpMs = 20;
  int ms = 220;
  int startMs = 220;
  Instant last;

  boolean over = false;
  int winLose = 0;

  Pixel.Color pCol = Pixel.Color.RED;
  Pixel.Color deadCol = Pixel.Color.WHITE;

  ArrayList<Block> falling = new ArrayList<Block>();

  Piece curPiece;
  int curLength = 3;
  
  int X, Y;
  boolean right = true;

  Input inp;

  public Stackem(int x, int y)
  {
    raster = new Raster(x, y);

    width = x;
    height = y;

    inp = new Input(this);
    inp.start();
  }
  
  void run() 
  {
    last = Instant.now();

    reset();
    while (true)
    {
      checkFired(); 
      if (Duration.between(last, Instant.now()).toMillis() > ms) 
      {
        updateFalling();
        last = Instant.now();
        if (!over)
        {
          updateX();
        }
        updateRaster();
        raster.draw();
      }
    }
  }
  void updateRaster()
  {
    raster.clear();

    for (Piece p : pieces)
    {
      for (Block b : p.blocks) 
      {
        raster.addPixel(b.X, height - 2 - b.Y, b.alive ? pCol : deadCol);
      }
    }
    raster.addBorder(Pixel.Color.BLACK);

    if (winLose != 0)
    {
      String text = winLose > 0 ? "-= YOU  WIN =-" : "-= YOU LOSE =-";
      raster.addSyms(2, height - 1, text, winLose > 0 ? Pixel.Color.GREEN : Pixel.Color.RED);
    }
  }
  void win()
  {
    over = true;
    winLose = 1;
  }
  void lose()
  {
    over = true;
    winLose = -1;
  }
  void reset()
  {
    over = false;
    winLose = 0;
    pieces.clear();
    falling.clear();
    Y = 0;
    curLength = 3;
    ms = startMs;
    getPiece();
  }
  void checkFired()
  {
    if (inp.fired)
    {
      if (over)
      {
        reset();
        inp.fired = false;
        return;
      }
      doFired(); 
      inp.fired = false;
    }
  }
  void doFired()
  {
    ms -= (int)((ms / height) * 1.78f);
    Y += 1;
    X = 0;
    right = true;
    for (Block b : curPiece.blocks)
    {
      boolean live = false;
      if (Y == 1)
      {
        live = true;
        break;
      }
      for (Piece p : pieces)
      {
        for (Block b2 : p.blocks)
        {
          if (b2.X == b.X && b2.Y == b.Y - 1 && b != b2 && b2.alive)
          {
            live = true;
          }
        }
      }
      if (!live)
      {
        falling.add(b);
      }
      b.alive = live;
    }
    int dead = 0;
    for (Block b : curPiece.blocks)
    {
      if (!b.alive)
      {
        dead += 1;
      }
    }
    if (dead == curPiece.len)
    {
      lose();
      return;
    }
    if (Y == height - 2)
    {
      win();
      return;
    }
    curLength -= dead;
    getPiece();
  }
  void updateFalling()
  {
    ArrayList<Block> toRemove = new ArrayList<Block>();
    for (Block b : falling)
    {
      boolean found = false;
      if (b.Y == 0)
      {
        found = true;
      }
      for (Piece p : pieces)
      {
        if (found)
        {
          break;
        }
        for (Block b2 : p.blocks)
        {
          if (found)
          {
            break;
          }
          if (b2. X == b.X && b2.Y == b.Y - 1 && b != b2)
          {
            toRemove.add(b);
            found = true;
            break;
          }
        }
      }
      if (!found)
      {
        b.Y -= 1;
      }
    }

    for (Block b : toRemove)
    {
      falling.remove(b);
    }
  }
  void updateX()
  {
    X += right ? 1 : -1;
    if (X + curPiece.len >= width - 1)
    {
      right = false;
    }
    if (X <= 1)
    {
      right = true;
    }
    curPiece.setX(X);
  }

  void getPiece()
  {
    curPiece = new Piece(curLength);
    curPiece.setX(X);
    curPiece.setY(Y);
    pieces.add(curPiece);
  }

  class Piece
  {
    int len;
    int X, Y;

    ArrayList<Block> blocks = new ArrayList<Block>();

    void setX(int x)
    {
      for (Block b : blocks)
      {
        b.X = x + b.ind; 
      }
      X = x;
    }
    void setY(int y)
    {
      for (Block b : blocks)
      {
        b.Y = y;
      }
      Y = y;
    }

    Piece(int l)
    {
      len = l;
      
      for (int i = 0; i < l; i++)
      {
        blocks.add(new Block(i, Y));
      }
    }
  }
  class Block
  {
    int ind = 0;
    int X, Y;
    boolean alive = true;

    Block(int i, int y)
    {
      ind = i;
      Y = y;
    }
  }

  class Input extends Thread
  {
    Stackem game;
    boolean fired;

    Scanner scn = new Scanner(System.in);

    Input(Stackem s)
    {
      game = s;
    }

    void check()
    {
      scn.nextLine();
      fired = true;
    }

    public void run()
    {
      try 
      {
        while (true)  
        {
          check();
        }
      } catch (Exception e){}
    }
  }
}
