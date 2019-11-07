import processing.core.*;
import processing.data.*;

/**
 * Loads an XML file representing an XML tree of
 * move and box nodes and gives the user options
 * to draw the image based on the tree, expand the image and
 * adding boxes to it
 * 
 * @author Heikal Badrulhisham
 *
 */
public class heikal_Assmt01 extends PApplet
{		
	private XML data;	//XML tree
	private boolean keyAlreadyPressed;	//Whether a key is being pressed

	
	/**
	 * Load an XML tree and delete whitespace nodes
	 * @param filename Name of XML the file
	 * @return	XML tree loaded from the file
	 * @throws IllegalArgumentExcpetion for null values passed
	 */
	public void loadBoxes(String filename)
	{
		if(filename == null)
			throw new IllegalArgumentException();
		
		data = loadXML(filename);	//Load XML file
		delWhitespace(data);		//Delete whitespace nodes in the XML tree
	}
	
	/**
	 * Traverses an XML tree and increment passed x, y values
	 * by x, y values of move nodes encountered. Draw a 20x20
	 * box centered at the current x, y values whenever a box node
	 * is encountered
	 * @param xml	XML tree
	 * @param x	x attribute 
	 * @param y y attribute
	 * @throws IllegalArgumentExcpetion for null values passed
	 */
	public void drawBoxes(XML xml, int x, int y)
	{
		if(xml == null)
			throw new IllegalArgumentException();
		
		//Increment x, y values by the values on a move node
		if(xml.getName().equals("move"))
		{
			x += xml.getInt("x");
			y += xml.getInt("y");
		}
		
		//Draw a box centered at the current x, y coordinates
		if(xml.getName().equals("box"))
			rect(x - 10, y + 10, 20, 20 );
		
		//Traverse child nodes
		for(XML c : xml.getChildren())
			drawBoxes(c, x, y);
			
	}
	
	/**
	 * Double the x and y attributes of every move node in 
	 * an XML tree
	 * @param xml XML tree
	 * @throws IllegalArgumentExcpetion for null values passed
	 */
	public void doubleMoves(XML xml)
	{
		if(xml == null)
			throw new IllegalArgumentException();
		
		//Multiply x and y attributes if the node has them
		if(xml.getName().equals("move"))
		{
			xml.setInt("x", xml.getInt("x")*2);
			xml.setInt("y", xml.getInt("y")*2);
		}
		
		//Do the same for child nodes
		for(XML c : xml.getChildren())
			doubleMoves(c);
	}
	
	/**
	 * Replaces every box node in an XML tree with four move nodes 
	 * with a box node child each
	 * @param xml XML tree
	 * @throws IllegalArgumentExcpetion for null values passed
	 */
	public void doubleBoxes(XML xml)
	{
		if(xml == null)
			throw new IllegalArgumentException();
		
		//Start from leaves
		for(XML c : xml.getChildren())
			doubleBoxes(c);
		
		//Replace box nodes with four move nodes
		if(xml.getName().equals("box"))
		{
			XML parent = xml.getParent();	//Parent of the box node to be replaced
			
			//Replace the box node with four move nodes with 
			//+/- the original box's values
			for(int i = 0; i < 4; i++)
			{
				//Multiples of the 10 to be added to the original box's position
				int mult = 1, mult2 = 1;	
				
				if(i == 0 || i == 1)
					mult = -1;
				if(i == 0 || i ==2)
					mult2 = -1;
				
				XML newMove = parent.addChild("move");	//New move node
				//Set positions
				newMove.setInt("x", xml.getInt("x") + mult*10);		
				newMove.setInt("y", xml.getInt("y") + mult2*10);
				newMove.addChild("box");	//Add a box child to the new move node
			}
			
			parent.removeChild(xml);	//Remove current box node
		}
		
	}
	
	/**
	 * Helper method for deleting whitespace nodes
	 * @param x XML tree
	 * @throws IllegalArgumentExcpetion for null values passed
	 */
	private void delWhitespace(XML x)
	{
		if(x == null)
			throw new IllegalArgumentException();;
		
		//Traverse children
		for(XML c : x.getChildren())
			delWhitespace(c);
		//Remove #text nodes
		if(x.getName().equals("#text"))
			x.getParent().removeChild(x);
	}
	
	
	// tie key press events to calling the functions above:
	// 1 - loadBoxes
	// 2 - drawBoxes
	// 3 - doubleMoves
	// 4 - doubleBoxes3
	public void draw()
	{
		if(keyPressed)
		{
			if(keyAlreadyPressed == false)
			{
				switch(key)
				{
				case '1':
					loadBoxes("boxData.xml");
					break;
				case '2':
					background( 255 );
					drawBoxes(data, width/2, height/2);	
					save("output.png");
					break;
				case '3':
					doubleMoves(data);
					break;
				case '4':
					doubleBoxes(data);
					break;
				}
			}
			keyAlreadyPressed = true;
		}
		else
			keyAlreadyPressed = false;
	}

	// basic processing setup: window size and background color
	public void setup()
	{
		size( 800, 600 );
		background( 255 );
		data = null;
		keyAlreadyPressed = true;
	}
		
	// run as an Application instead of as an Applet
	public static void main(String[] args) 
	{
		String thisClassName = new Object(){}.getClass().getEnclosingClass().getName();
		PApplet.main( new String[] { thisClassName } );
	}
}
