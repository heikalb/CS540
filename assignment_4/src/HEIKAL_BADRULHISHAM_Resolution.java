import processing.core.PApplet;
import processing.data.XML;

public class HEIKAL_BADRULHISHAM_Resolution extends DrawableTree
{
	public HEIKAL_BADRULHISHAM_Resolution(PApplet p, XML tree) 
	{ 
		super(p); 
		this.tree = tree; 
		dirtyTree = true;
	}
		
	public void eliminateBiconditions()
	{
		// TODO - Implement the first step in converting logic in tree to CNF:
		// Replace all biconditions with truth preserving conjunctions of conditions.
		if(this.tree == null)
			return;
		eliminateBiconditionsHelper(this.tree);
		this.dirtyTree = true;
	}	

	private void eliminateBiconditionsHelper(XML t)
	{
		if(t == null)
			return;
		
		//Recurse down the tree
		for(XML c : t.getChildren())
			eliminateBiconditionsHelper(c);

		//Replace biconditions
		if(t.getName().equals("bicondition"))
		{
			XML cond1 = new XML("condition");	//The first condition
			XML cond2 = new XML("condition");	//The second condition

			//Add propositions to the conditions
			cond1.addChild(t.getChild(0));
			cond1.addChild(t.getChild(1));
			cond2.addChild(t.getChild(1));
			cond2.addChild(t.getChild(0));

			//Remove children of the bicondition
			removeAllChildren(t);

			//Change the node to an and of conditions
			t.setName("and");
			t.addChild(cond1);
			t.addChild(cond2);
		}
	}
	
	public void eliminateConditions()
	{
		if(this.tree == null)
			return;
		eliminateConditionsHelper(this.tree);	
		this.dirtyTree = true;
	}

	private void eliminateConditionsHelper(XML t)
	{
		if(t == null)
			return;
		
		//Recurse down the tree
		for(XML c : t.getChildren())
			eliminateConditionsHelper(c);

		//Replace conditions
		if(t.getName().equals("condition"))
		{
			XML notN = new XML("not");			//The not child of the or 
			XML propN = t.getChild(1);			//The right child of the or

			//Add the left side of the condition to the not node
			notN.addChild(t.getChild(0));

			//Remove propositions of the condition
			removeAllChildren(t);

			//Change the node to an or node, and add the not node
			t.setName("or");
			t.addChild(notN);
			t.addChild(propN);
		}
	}
	
	public void moveNegationInwards()
	{
		// TODO - Implement the third step in converting logic in tree to CNF:
		// Move negations in a truth preserving way to apply only to literals.
		if(this.tree == null)
			return;
		moveNegationInwardsHelper(this.tree);
		this.dirtyTree = true;
	}
	
	private void moveNegationInwardsHelper(XML t)
	{
		if(t == null)
			return;
		
		//Recurse down the tree
		for(XML c : t.getChildren())
			moveNegationInwardsHelper(c);

		//Process not nodes governing non-literals
		if(t.getName().equals("not"))
		{
			XML notC = t.getChild(0);		//Child of the not node
			String cName = notC.getName();	//Name of the not's child

			//Negation of negation
			if(cName.equals("not"))
			{
				t.getParent().addChild(notC.getChild(0));
				t.getParent().removeChild(t);
			}
			//Negation of and
			else if(cName.equals("and"))
			{
				//Negatives of the children of the and node
				XML not1 = new XML("not");
				XML not2 = new XML("not");

				not1.addChild(notC.getChild(0));
				not2.addChild(notC.getChild(1));

				//Remove current children of the node
				removeAllChildren(t);

				//Turn the node into an or of negative propositions
				t.setName("or");
				t.addChild(not1);
				t.addChild(not2);
			}
			//Negation of or 
			else if(cName.equals("or"))
			{
				//Negatives of the children of the or node
				XML not1 = new XML("not");
				XML not2 = new XML("not");

				not1.addChild(notC.getChild(0));
				not2.addChild(notC.getChild(1));

				//Remove current children of the node
				removeAllChildren(t);

				//Turn the node into an or of negative propositions
				t.setName("and");
				t.addChild(not1);
				t.addChild(not2);
			}
		}
	}
	
	public void distributeOrsOverAnds()
	{
		// TODO - Implement the fourth step in converting logic in tree to CNF:
		// Move negations in a truth preserving way to apply only to literals.
		if(this.tree == null)
			return;
		distributeOrsOverAndsHelper(this.tree);
		this.dirtyTree = true;
	}
	
	private void distributeOrsOverAndsHelper(XML t)
	{
		if(t == null)
			return;
		
		if(t.getName().equals("or") && t.getChild("and") != null)
		{
			//The child who is an and node, and the other child
			XML andChild, otherChild;
			
			//Assign and child and the other child
			if(t.getChild(0).getName().equals("and"))
			{
				andChild = t.getChild(0);
				otherChild = t.getChild(1);
			}
			else
			{
				andChild = t.getChild(1);
				otherChild = t.getChild(0);
			}
			
			//The two resulting two or nodes
			XML orNode1 = new XML("or");
			XML orNode2 = new XML("or");
			
			//Fill up the or nodes
			orNode1.addChild(otherChild);
			orNode1.addChild(andChild.getChild(0));
			
			orNode2.addChild(otherChild);
			orNode2.addChild(andChild.getChild(1));
			
			//Turn the tree into an and of ors
			removeAllChildren(t);
			t.setName("and");
			t.addChild(orNode1);
			t.addChild(orNode2);
		}
		
		//Recurse down the tree
		for(XML c : t.getChildren())
			distributeOrsOverAndsHelper(c);
	}
		
	public void collapse()
	{
		// TODO - Clean up logic in tree in preparation for Resolution:
		// 1) Convert nested binary ands and ors into n-ary operators so
		// there is a single and-node child of the root logic-node, all of
		// the children of this and-node are or-nodes, and all of the
		// children of these or-nodes are literals: either atomic or negated	
		// 2) Remove redundant literals from every clause, and then remove
		// redundant clauses from the tree.
		// 3) Also remove any clauses that are always true (tautologies)
		// from your tree to help speed up resolution.
		if(this.tree == null)
			return;
		
		//Put a dummy and node if there is only the logic node
		if(this.tree.getChildCount() == 0)
			this.tree.addChild("and");
		
		//Add an and node after logic if there is none
		if(!this.tree.getChild(0).equals("and"))
		{
			XML and = new XML("and");
			and.addChild(this.tree.getChild(0));
			removeAllChildren(this.tree);
			this.tree.addChild(and);
		}
		
		removeNesting(this.tree);
		removeRedundantLiterals(this.tree);
		removeRedundantClauses(this.tree);
		removeTautologies(this.tree);
		
		this.dirtyTree = true;
	}
	
	private void removeNesting(XML t)
	{
		if(t == null)
			return;
		
		//Recurse down the tree
		for(XML c : t.getChildren())
			removeNesting(c);
		
		//Stop if the tree has no parent
		if(t.getParent() == null)
			return;
		
		//If an and node governs lone literals, create an or node between the literal and the and node
		if(t.getName().equals("and"))
		{
			for(XML c : t.getChildren())
			{
				if(isLiteral(c))
				{
					XML or = new XML("or");
					or.addChild(c);
					t.removeChild(c);
					t.addChild(or);
				}
			}
		}
		
		//Handle nested nodes
		if((t.getName().equals("or") && t.getParent().getName().equals("or")) ||
			(t.getName().equals("and") && t.getParent().getName().equals("and")) )
		{
			t.getParent().removeChild(t);
			
			for(XML c : t.getChildren())
				t.getParent().addChild(c);
		}
	}
	
	private void removeRedundantLiterals(XML t)
	{
		if(t == null)
			return;
		
		//Recurse down the tree
		for(XML c : t.getChildren())
			removeRedundantLiterals(c);
		
		//Stop if redundancy is not possible or if the tree is not a clause
		if(t.getChildCount() < 2 || !t.getName().equals("or"))
			return;
		
		XML toKeep = new XML("toKeep");	//To store unique children
		
		//Record unique literals to save
		for(XML c : t.getChildren())
		{
			if(isLiteral(c) && !clauseContainsLiteral(toKeep, getAtomFromLiteral(c), isLiteralNegated(c)))
				toKeep.addChild(c);
		}
		
		//Remove all literals from the clause
		for(XML c : t.getChildren())
		{
			if(isLiteral(c))
				t.removeChild(c);
		}

		//Fill the clause with unique literals
		for(XML tkp : toKeep.getChildren())
			t.addChild(tkp);
	}

	private void removeRedundantClauses(XML t)
	{
		if(t == null)
			return;
		
		//Recurse down the tree
		for(XML c : t.getChildren())
			removeRedundantClauses(c);
		
		//Stop if a redundancy is impossible or the tree is not a set
		if(t.getChildCount() < 2 || !t.getName().equals("and"))
			return;
		
		XML toKeep = new XML("toKeep");	//To store unique clauses
		
		//Filter out redundant clauses
		for(XML c : t.getChildren())
		{
			if(isLiteral(c))
				toKeep.addChild(c);
			
			if(c.getName().equals("or") && !setContainsClause(toKeep, c))
				toKeep.addChild(c);
		}

		//Remove all clause from the set
		removeAllChildren(t);

		//Fill the set with unique clause
		for(XML tkp : toKeep.getChildren())
			t.addChild(tkp);
		
	}
	
	private void removeTautologies(XML t)
	{
		if(t == null)
			return;
		
		for(XML c : t.getChildren())
			removeTautologies(c);
		
		if(clauseIsTautology(t))
			t.getParent().removeChild(t);
	}
	
	public boolean applyResolution()
	{
		// TODO - Implement resolution on the logic in tree.  New resolvents
		// should be added as children to the only and-node in tree.  This
		// method should return true when a conflict is found, otherwise it
		// should only return false after exploring all possible resolvents.
		// Note: you are welcome to leave out resolvents that are always
		// true (tautologies) to help speed up your search.
		if(this.tree == null || this.tree.getChildCount() == 0)
			return false;
		
		XML andTop = tree.getChild(0);	//The node after the logic node
		
		//Apply resolution with every pair of clause in the CNF
		for(int i = 0; i < andTop.getChildCount(); i++)
		{
			for(int j = 0; j < andTop.getChildCount(); j++)
			{
				if(andTop.getChild(i).equals(andTop.getChild(j)))
					continue;
				
				XML resolvent = resolve(andTop.getChild(i), andTop.getChild(j));	//New resolvent

				if(resolvent != null)
				{
					//If there is conflict return true
					if(resolvent.getChildCount() == 0)
						return true;
					//Add a new clause
					if(!clauseIsTautology(resolvent) && !setContainsClause(andTop, resolvent))
						andTop.addChild(resolvent);
					
					this.dirtyTree = true;
				}
			}
		}
		
		return false;
	}

	public XML resolve(XML clause1, XML clause2)
	{
		// TODO - Attempt to resolve these two clauses and return the resulting
		// resolvent.  You should remove any redundant literals from this 
		// resulting resolvent.  If there is a conflict, you will simply be
		// returning an XML node with zero children.  If the two clauses cannot
		// be resolved, then return null instead.
		if(clause1 == null || clause2 == null)
			return null;
		
		if(!clause1.getName().equals("or") || !clause2.getName().equals("or"))
			return null;
		
		XML resolvent = new XML("or");	//The resolvent
		XML toExclude = null,			//The literals to exclude from the resolvent
		toExclude2 = null;
		
		//Find the literal to exclude in the first clause
		for(XML lit1 : clause1.getChildren())
		{
			if(clauseContainsLiteral(clause2, getAtomFromLiteral(lit1), !isLiteralNegated(lit1) ))
			{
				toExclude = lit1;
				break;
			}
		}
		
		//Return null if there is nothing to resolve
		if(toExclude == null)
			return null;
		
		//Find the literal to exclude from the second clause
		for(XML lit2 : clause2.getChildren())
		{
			if(isLiteralNegated(lit2) != isLiteralNegated(toExclude) && 
				getAtomFromLiteral(lit2).equals(getAtomFromLiteral(toExclude))   )
			{
				toExclude2 = lit2;
				break;
			}
		}
		
		//Load up the resolvent with literals to keep
		for(XML lit1 : clause1.getChildren())
		{
			if(!lit1.equals(toExclude))
				resolvent.addChild(lit1);
		}
		
		for(XML lit2 : clause2.getChildren())
		{
			if(!lit2.equals(toExclude2) && 
				!clauseContainsLiteral(resolvent, getAtomFromLiteral(lit2), isLiteralNegated(lit2) ))
				resolvent.addChild(lit2);
		}
		
		return resolvent;
	}	
	
	// REQUIRED HELPERS: may be helpful to implement these before collapse(), applyResolution(), and resolve()
	// Some terminology reminders regarding the following methods:
	// atom: a single named proposition with no children independent of whether it is negated
	// literal: either an atom-node containing a name, or a not-node with that atom as a child
	// clause: an or-node, all the children of which are literals
	// set: an and-node, all the children of which are clauses (disjunctions)
		
	public boolean isLiteralNegated(XML literal) 
	{ 
		// TODO - Implement to return true when this literal is negated and false otherwise.
		if(literal == null)
			return false;
		
		return literal.getName().equals("not"); 
	}

	public String getAtomFromLiteral(XML literal) 
	{ 
		// TODO - Implement to return the name of the atom in this literal as a string.
		if(literal == null)
			return "";
		
		//If the literal is negated, return the atom child
		if(isLiteralNegated(literal))
			return literal.getChild(0).getName();
		
		//Else return the literal's name
		return literal.getName();
	}	
	
	public boolean clauseContainsLiteral(XML clause, String atom, boolean isNegated)
	{
		// TODO - Implement to return true when the provided clause contains a literal
		// with the atomic name and negation (isNegated).  Otherwise, return false.	
		if(clause == null || atom == null)
			return false;
		
		for(XML lit : clause.getChildren())
		{
			if(!isNegated)
			{
				if(lit.getName().equals(atom))
					return true;
			}
			else
			{
				if(isLiteralNegated(lit) && getAtomFromLiteral(lit).equals(atom))
					return true;
			}
		}
		return false;
	}
	
	public boolean setContainsClause(XML set, XML clause)
	{
		// TODO - Implement to return true when the set contains a clause with the
		// same set of literals as the clause parameter.  Otherwise, return false.
		if(set == null || clause == null)
			return false;
		
		//Iterate through the clauses contained in the passed set
		for(XML cl : set.getChildren())
		{
			int comLits = 0;	//Number of common literals
			//For each clause go through the literals of the passed clause
			for(XML lit : clause.getChildren())
			{
				if(clauseContainsLiteral(cl, getAtomFromLiteral(lit), isLiteralNegated(lit)))
					comLits++;
			}
			
			if(cl.getChildCount() == clause.getChildCount() && clause.getChildCount() == comLits)
				return true;
		}
		
		return false;
	}
	
	public boolean clauseIsTautology(XML clause)
	{
		// TODO - Implement to return true when this clause contains a literal
		// along with the negated form of that same literal.  Otherwise, return false.
		if(clause == null || !clause.getName().equals("or"))
			return false;
		
		for(XML lit : clause.getChildren())
		{
			if(isLiteral(lit) && 
					clauseContainsLiteral(clause, getAtomFromLiteral(lit), !isLiteralNegated(lit) )  )
				return true;
		}
		
		return false;
	}	
	
	private void removeAllChildren(XML t)
	{
		if(t == null)
			return;
		
		for(XML c : t.getChildren())
			t.removeChild(c);
	}

	private boolean isLiteral(XML t)
	{
		return t.getChildCount() == 0 || 
				(t.getName().equals("not") && t.getChildCount() == 1 && t.getChild(0).getChildCount() == 0);
	}
}
