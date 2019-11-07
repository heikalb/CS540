import processing.core.*;
import processing.data.*;

public class HEIKAL_BADRULHISHAM_DecisionTree extends DrawableTree
{
	public HEIKAL_BADRULHISHAM_DecisionTree(PApplet p) { super(p); }
		
	// This method loads the examples from the provided filename, and
	// then builds a decision tree (stored in the inherited field: tree).
	// Each of the nodes in this resulting tree will be named after
	// either an attribute to split on (vote01, vote02, etc), or a party
	// classification (DEMOCRAT, REPUBLICAN, or possibly TIE).
	public void learnFromTrainingData(String filename)
	{
		// NOTE: Set the inherited field dirtyTree to true after building the
		// decision tree and storing it in the inherited field tree.  This will
		// trigger the DrawableTree's graphical rendering of the tree.
		if(filename == null)
			return;
		
		//Load dataset tree
		 XML dataset = this.p.loadXML(filename);
		
		if(dataset == null)
			return;
		
		//Delete whitespace nodes in the dataset tree
		delWhitespace(dataset);
		
		this.tree = new XML("decisionTree");
		
		//Build the decision tree
		this.recursiveBuildTree(dataset, this.tree);
		
		this.dirtyTree = true;
	}
			
	// This method recursively builds a decision tree based on
	// the set of examples that are children of dataset.
	public void recursiveBuildTree(XML dataset, XML tree)
	{
		// NOTE: You MUST add YEA branches to your decision nodes before
		// adding NAY branches.  This will result in YEA branches being
		// child[0], which will be drawn to the left of any NAY branches.
		// The grading tests assume that you are following this convention.
		if(dataset == null || tree == null)
			return;
		
		//If there is only one party in the bucket, label it by the party
		if(isHomogenous(dataset, "party"))
		{
			tree.setName(dataset.getChild(0).getString("party"));
			return;
		}
		
		//If the bucket is not homogeneous find best attribute to split on.
		//Initial entropy of the dataset:
		double initEnt = calculateEntropy(dataset);		
		//Best attribute to split on:
		String splitAtr = chooseSplitAttribute(dataset);	
		//Entropy after splitting:
		double entAfter = calculatePostSplitEntropy(splitAtr, dataset);
		
		//If the entropy after splitting is not less than before, 
		//don't branch, but label node
		if(initEnt <= entAfter)
		{
			tree.setName(plurality(dataset));
			return;
		}
		
		//If a split is possible name the node by the best attribute
		tree.setName(splitAtr);
		
		//Add YEA and NAY branches to the tree
		XML yeaBranch = tree.addChild("YEA"),
		nayBranch = tree.addChild("NAY");
		
		//Datasets for splitting the YAY and NAY branches further
		XML yeaDataset = new XML("YeaDataset"),
			nayDataset = new XML("NayDataset");
		
		//Load YAY and NAY branches in the dataset
		for(XML ch : dataset.getChildren())
		{
			if(ch.getString(splitAtr).equals("YEA"))
				yeaDataset.addChild(ch);
			if(ch.getString(splitAtr).equals("NAY"))
				nayDataset.addChild(ch);
		}
		
		//Build yay and nay branches
		recursiveBuildTree(yeaDataset, yeaBranch);
		recursiveBuildTree(nayDataset, nayBranch);
	}

	// This method calculates and returns the mode (most common value) among
	// the party attributes of the children examples under dataset.  If there
	// happens to be an exact tie, this method returns "TIE".
	public String plurality(XML dataset)
	{
		if(dataset == null)
			return "";
		
		int numRep = 0, numDem = 0;		//Number of Republicans and Democrats
		
		//Count party affiliation
		for(XML ch : dataset.getChildren())
		{
			if(ch.getString("party").equals("REPUBLICAN"))
				numRep++;
			if(ch.getString("party").equals("DEMOCRAT"))
				numDem++;
		}
		
		//Return party plurality 
		if(numRep > numDem)
			return "REPUBLICAN";
		if(numDem > numRep)
			return "DEMOCRAT";
		
		return "TIE";
	}

	// This method calculates and returns the name of the attribute that results
	// in the lowest entropy, after splitting all children examples according
	// to their value for this attribute into two separate groups: YEA vs. NAY.	
	public String chooseSplitAttribute(XML dataset)
	{
		if(dataset == null)
			return "";

		//Minimum entropy (initialized to entropy before split), and the 
		//corresponding attribute name
		double minEntropy = calculateEntropy(dataset);
		String minAtr = "vote01";	
		
		//Vote attribute name
		String atr;
		
		//Test splitting on each attribute and find the least-entropy attribute
		for(int i = 1; i <= 16; i++)
		{
			if(i < 10)
				atr = "vote0" + i;
			else
				atr = "vote" + i;
			
			//Post-split entropy
			double ent = calculatePostSplitEntropy(atr, dataset);
			
			if(ent < minEntropy)
			{
				minEntropy = ent;
				minAtr = atr;
			}
		}
		return minAtr;
	}
		
	// This method calculates and returns the entropy that results after 
	// splitting the children examples of dataset into two groups based
	// on their YEA vs. NAY value for the specified attribute.
	public double calculatePostSplitEntropy(String attribute, XML dataset)
	{		
		if(attribute == null || dataset == null)
			return 0.0;
		
		//Branches of yea and nay voters
		XML yeas = new XML("Yea"),
		nays = new XML("Nay");
		
		//Sort representatives into YEA and NAY voters
		for(XML ch : dataset.getChildren())
		{
			if(ch.getString(attribute).equals("YEA"))
				yeas.addChild(ch);
			else
				nays.addChild(ch);
		}
		
		//Probability of voting YAY or NAY(for calculating entropy)
		double pYEA = (double) (yeas.getChildCount()) / 
						(double) dataset.getChildCount();
		double pNAY = 1 - pYEA;
		
		//Return entropy across the two buckets
		return pYEA*calculateEntropy(yeas) + pNAY*calculateEntropy(nays);
	}
	
	// This method calculates and returns the entropy for the children examples
	// of a single dataset node with respect to which party they belong to.
	public double calculateEntropy(XML dataset)
	{
		if(dataset == null)
			return 0.0;
		
		//Number of Republicans
		double numRep = 0.0;		
		//Count party affiliation
		for(XML ch : dataset.getChildren())
		{
			if(ch.getString("party").equals("REPUBLICAN"))
				numRep += 1.0;
		}
		
		//Probability of being a Republican
		double pRep = (numRep) / (double) dataset.getChildCount();
		
		return B(pRep);
	}

	// This method calculates and returns the entropy of a Boolean random 
	// variable that is true with probability q (as on page 704 of the text).
	// Don't forget to use the limit, when q makes this formula unstable.
	public static double B(double q)
	{
		//Limit value of the entropy formula
		if(q == 0.0 || q == 1.0)
			return 0.0;
		
		return (q*Math.log10(1/q) + (1-q)*Math.log10(1/(1-q))) / Math.log10(2);
		
	}

	// This method loads and runs an entire file of examples against the 
	// decision tree, and returns the percentage of those examples that this
	// decision tree correctly predicts.
	public double runTests(String filename)
	{
		if(filename == null)
			return 0.0;
		
		//Load test dataset
		XML testData = p.loadXML(filename);
		
		//If file loading fails
		if(testData == null)
			return 0.0;
		
		//Clean up test dataset
		delWhitespace(testData);
		
		double numRight = 0.0;	//Number of correct predictions
		
		//Run each example though the decision tree and count correct
		//predictions
		for(XML ch : testData.getChildren())
		{
			//Actual and predicted party name
			String realParty = ch.getString("party"),
				   predParty = predict(ch, this.tree);
			
			if(realParty.equals(predParty))
				numRight += 1.0;
		}
		
		//Return correct percentage
		return (numRight / (double)testData.getChildCount()) * 100;
	}
	
	// This method runs a single example through the decision tree, and then 
	// returns the party that this tree predicts the example to belonging to.
	// If this example contains a party attribute, it should be ignored here.	
	public String predict(XML example, XML decisionTree)
	{
		if(example == null || decisionTree == null)
			return "";
		
		//The label on the tree node
		String label = decisionTree.getName();
		
		//If this is a leaf node return party name
		if(label.equals("REPUBLICAN"))
			return "REPUBLICAN";
		if(label.equals("DEMOCRAT"))
			return "DEMOCRAT";
		if(label.equals("TIE"))
			return "TIE";
		
		//If the label is an attribute name, branch down
		if(example.getString(label).equals("YEA"))
			return predict(example, decisionTree.getChild(0));
		else
			return predict(example, decisionTree.getChild(1));
	}

	/**
	 * Helper method for deleting whitespace nodes
	 * @param x XML tree
	 */
	private void delWhitespace(XML x)
	{
		if(x == null)
			return;
		
		//Traverse children
		for(XML c : x.getChildren())
			delWhitespace(c);
		
		//Remove #text nodes
		if(x.getName().equals("#text"))
			x.getParent().removeChild(x);
	}
	
	/**
	 * Helper method to see if a data set is homogeneous by an attribute
	 * @param x	XML tree
	 * @param atr	Attribute to test on
	 */
	private boolean isHomogenous(XML x, String atr)
	{
		if(x == null || atr == null)
			return true;
		
		for(int i = 0; i < x.getChildCount() - 1; i++)
		{
			if(!x.getChild(i).getString(atr).equals(
											x.getChild(i + 1).getString(atr)))
				return false;
		}
		
		return true;
	}
}
