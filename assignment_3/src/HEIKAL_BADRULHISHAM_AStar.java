import java.util.ArrayList;
import java.util.Collections;

public class HEIKAL_BADRULHISHAM_AStar 
{		
	public ArrayList<SearchPoint> frontier;
	public ArrayList<SearchPoint> explored;
	public ArrayList<Map.Point> frontierMP;	//Map.Points in the frontier
	public ArrayList<Map.Point> exploredMP;	//Map.Points in explored
	public Map map;					//Map
	public Map.Point start, end;	//Start and ending points
	public int heuristic;			//Heuristic: 0,1 or 2
	public SearchPoint curr;		//Current search point
	
	public class SearchPoint implements Comparable<SearchPoint>
	{
		public Map.Point mapPoint;
		//The previous search point before this one in a path:
		public SearchPoint prev = null; 
		public boolean sPexplored = false;	//Whether the searchpoint has been explored
		// TODO - implement this method to return the minimum cost
		// necessary to travel from the start point to here
		public float g() 
		{
			if(this.prev == null)
				return 0;
			
			//Distances from the previous point
			float dx = this.mapPoint.x - this.prev.mapPoint.x,
				  dy = this.mapPoint.y - this.prev.mapPoint.y;
			
			return prev.g() + (float) Math.sqrt(dx*dx + dy*dy);
		}	
		
		// TODO - implement this method to return the heuristic estimate
		// of the remaining cost, based on the H parameter passed from main:
		// 0: always estimate zero, 1: manhattan distance, 2: euclidean l2 distance
		public float h()
		{
			//Distances from this point to the goal
			float dx = map.end.x - this.mapPoint.x,	
				  dy = map.end.y - this.mapPoint.y;
			
			//Return Manhattan distance heuristic
			if(heuristic == 1)	
				return Math.abs(dx) + Math.abs(dy);
			
			//Return Euclidean distance heuristic
			if(heuristic == 2)
				return (float) Math.sqrt(dx*dx + dy*dy);
			
			//Return zero heuristic
			return 0;	
			
		}
		
		// TODO - implement this method to return to final priority for this
		// point, which include the cost spent to reach it and the heuristic 
		// estimate of the remaining cost
		public float f()
		{
			return this.g() + this.h();
		}
		
		// TODO - override this compareTo method to help sort the points in 
		// your frontier from highest priority = lowest f(), and break ties
		// using whichever point has the lowest g()
		@Override
		public int compareTo(SearchPoint other)
		{
			if(other == null)
				return 0;
			
			if(this.f() > other.f())
				return -1;
			if(this.f() < other.f())
				return 1;
			if(this.g() > other.g())
				return -1;
			if(this.g() < other.g())
				return 1;
			return 0;
		}
		
		// TODO - override this equals to help you check whether your ArrayLists
		// already contain a SearchPoint referencing a given Map.Point
		@Override
		public boolean equals(Object other)
		{
			if(other == null)
				return false;
			if(!other.getClass().isInstance(curr))
				return false;
			
			//The map point of the search point passed
			Map.Point comp = ((SearchPoint) other).mapPoint;
			
			//Search through explored
			for(SearchPoint sP : explored)
			{
				if(sP.mapPoint.x == comp.x && sP.mapPoint.y == comp.y)
					return true;
			}
			//Search through frontier
			for(SearchPoint sP : frontier)
			{
				if(sP.mapPoint.x == comp.x && sP.mapPoint.y == comp.y)
					return true;
			}
			
			return false;
		}		
	}
	
	// TODO - implement this constructor to initialize your member variables
	// and search, by adding the start point to your frontier.  The parameter
	// H indicates which heuristic you should use while searching:
	// 0: always estimate zero, 1: manhattan distance, 2: euclidean l2 distance
	public HEIKAL_BADRULHISHAM_AStar(Map map, int H)
	{
		if(map == null || H > 2 || H < 0 || map.allPoints == null 
			|| map.allPoints.isEmpty() || map.allStreets == null || 
			map.allStreets.isEmpty()|| map.start == null || map.end == null)
			return;
		
		//Initialize frontier and explored
		this.frontier = new ArrayList<SearchPoint>();
		this.explored = new ArrayList<SearchPoint>();
		this.frontierMP = new ArrayList<Map.Point>();
		this.exploredMP = new ArrayList<Map.Point>();
		
		//Initialize other fields
		this.map = map;
		this.heuristic = H;
		this.start = map.start;
		this.end = map.end;
		
		SearchPoint startSP = new SearchPoint();	//Starting search point
		startSP.mapPoint = map.start;				//Store initial point
		
		this.frontier.add(startSP);			//Add starting point to frontier
		this.frontierMP.add(map.start);
		this.curr = startSP;				//Make starting point the current point
		this.curr.sPexplored = true;
		
	}
	
	// TODO - implement this method to explore the single highest priority
	// and lowest f() SearchPoint from your frontier. This method will be 
	// called multiple times from Main to help you visualize the search.
	// This method should not do anything, if your search is complete.
	public void exploreNextNode() 
	{
		//Return when the goal has been reached
		if(curr.mapPoint.x == end.x && curr.mapPoint.y == end.y)
			return;
		
		//Return if there is nothing in the frontier
		if(frontier.isEmpty())
			return;
			
		//Neighbor with the lowest f()
		SearchPoint bestNeighbor = frontier.get(0);
		
		//Find the best neighbor
		for(SearchPoint sP : this.frontier)
		{
			if(sP.mapPoint.isOnStreet && !sP.sPexplored)
			{
				if(bestNeighbor.compareTo(sP) == -1)
					bestNeighbor = sP;
			}
		}
			
		//Add the next point to explored, remove from frontier, record the
		//previous point of the new point
		this.frontier.remove(bestNeighbor);
		this.frontierMP.remove(bestNeighbor.mapPoint);
		this.explored.add(bestNeighbor);
		this.exploredMP.add(bestNeighbor.mapPoint);
		
		this.curr = bestNeighbor;			//Move to the best neighbor
		this.curr.sPexplored = true;
		
		if(isComplete())
			return;
		
		//Add the unexplored neighbors of the last explored node to the frontier
		for(Map.Point mP : bestNeighbor.mapPoint.neighbors)
		{
			if(mP.isOnStreet)
			{
				//Neighboring search point of the newly explored node
				SearchPoint newSP = new SearchPoint();
				newSP.mapPoint = mP;
				
				if(!this.curr.equals(newSP))
				{
					this.frontier.add(newSP);
					newSP.prev = bestNeighbor;
					this.frontierMP.add(mP);
				}
				
			}
		}
		
	}

	// TODO - implement this method to return an ArrayList of Map.Points
	// that represents the SearchPoints in your frontier.
	public ArrayList<Map.Point> getFrontier()
	{
		return this.frontierMP;
	}
	
	// TODO - implement this method to return an ArrayList of Map.Points
	// that represents the SearchPoints that you have explored.
	public ArrayList<Map.Point> getExplored()
	{
		return this.exploredMP;
	}

	// TODO - implement this method to return true only after a solution
	// has been found, or you have determined that no solution is possible.
	public boolean isComplete()
	{
		if(curr.mapPoint.x == end.x && curr.mapPoint.y == end.y)
			return true;
		if(this.frontier.isEmpty())
			return false;
		return false;
	}

	// TODO - implement this method to return an ArrayList of the Map.Points
	// that are along the path that you have found from the start to end.  
	// These points must be in the ArrayList in the order that they are 
	// traversed while moving along the path that you have found.
	public ArrayList<Map.Point> getSolution()
	{
		//Solution path
		ArrayList<Map.Point> retSol = new ArrayList<Map.Point>();
		
		if(!this.isComplete())
			return retSol;
		
		//The goal point
		SearchPoint sP = this.curr;
		
		//Store previous points from goal point back to starting point
		while(sP != null)
		{
			retSol.add(0, sP.mapPoint);;
			sP = sP.prev;
		}
		
		return retSol;
	}	
}
