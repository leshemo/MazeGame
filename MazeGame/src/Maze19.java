import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Random;
import javalib.impworld.World;
import javalib.impworld.WorldScene;
import javalib.worldimages.*;
import tester.Tester;

// Represents a vertex of a graph
class Vertex {
  String name;   // x1y1, x21y3
  int x;
  int y;
  ArrayList<Edge> outEdges;

  Vertex(int x, int y) {
    if (x < 0) {
      throw new IllegalArgumentException("Given X is less than 0");
    }
    if (y < 0) {
      throw new IllegalArgumentException("Given Y is less than 0");
    }

    this.x = x;
    this.y = y;
    this.name = "x" + String.valueOf(x) + "y" + String.valueOf(y);
    this.outEdges = new ArrayList<Edge>();
  }

  // add the given edge to this vertex's outEdges 
  void connect(Edge e) {
    if (!this.outEdges.contains(e)) {
      this.outEdges.add(e);
    }
  }

  @Override
  // Is this Vertex equal to the given object
  public boolean equals(Object other) {
    if (!(other instanceof Vertex)) { 
      return false; 
    }
    Vertex that = (Vertex)other;
    return this.name.equals(that.name);
  }

  @Override
  // defines the hashCode for a Vertex
  public int hashCode() {
    return this.name.hashCode();
  }

  WorldImage makeEdgeSceneVertex(int scale, WorldImage currentImage, Vertex to, Color color) {
    if (this.x == to.x) {
      //horizontal wall
      return new OverlayOffsetAlign(AlignModeX.LEFT, AlignModeY.TOP, 
          currentImage, 
          this.x * scale, to.y * scale,  new LineImage(new Posn(scale, 0), color));
    }
    if (this.y == to.y) {
      //vertical wall
      return new OverlayOffsetAlign(AlignModeX.LEFT, AlignModeY.TOP, 
          currentImage, 
          to.x * scale, this.y * scale,  new LineImage(new Posn(0, scale), color));
    }
    return currentImage;
  }

  // is this vertex the given position?
  boolean isPosition(int x1, int y1) {
    return this.x == x1 && this.y == y1;
  }

  // add this vertex's position to the given list
  void addPosition(ArrayList<Posn> l) {
    l.add(new Posn(this.x, this.y));
  }
}

// Represents edges between graphs
class Edge {
  Vertex from;
  Vertex to;
  int weight;
  boolean wall; // is this edge a wall?

  Edge(Vertex from, Vertex to, Random rand) {
    this.weight = rand.nextInt(50); // Randomize weight to form random tree
    this.wall = true;
    this.from = from;
    this.to = to;
    this.from.connect(this);
    this.to.connect(this);
  }

  // makes this edge into a path, not a wall
  public void makePath() {
    this.wall = false;
  }

  //creates a WorldImage for this edge, given the scale and the current WorldImage
  WorldImage makeEdgeScene(int scale, WorldImage currentImage) {
    if (wall) {
      return this.from.makeEdgeSceneVertex(scale, currentImage, this.to, Color.black);
    } else {
      return this.from.makeEdgeSceneVertex(scale, currentImage, this.to, Color.LIGHT_GRAY);
    }
  }
}

// Represents a graph
class Graph {
  ArrayList<Vertex> vertices;
  ArrayList<Edge> allEdges;
  ArrayList<Edge> edgesInTree;
  int width; 
  int height;
  ArrayList<Vertex> solutionPath;
  ICollection<Vertex> worklist;  
  Vertex end;
  HashMap<Vertex, Boolean> alreadySeen;
  HashMap<Vertex, Vertex> cameFromEdge; 
  boolean searchOver;

  Graph(Random rand, int width, int height) {
    if (width < 1) {
      throw new IllegalArgumentException("Given width is less than 1");
    }
    if (height < 1) {
      throw new IllegalArgumentException("Given height is less than 1");
    }
    this.width = width;
    this.height = height;
    this.vertices = new ArrayUtils().makeStartingVertices(this.width, this.height);
    this.allEdges = new ArrayUtils().makeStartingEdges(
        this.vertices, this.width, this.height, rand);
    this.edgesInTree = new ArrayList<Edge>();
    this.solutionPath = new ArrayList<Vertex>();
    this.end = new Vertex(width - 1, height - 1);
    this.alreadySeen = new HashMap<Vertex, Boolean>();
    for (Vertex v: this.vertices) {
      this.alreadySeen.put(v, false);
    }
    this.cameFromEdge = new HashMap<Vertex, Vertex>();
    this.searchOver = false;

  }


  // Create the worldImage of this graph
  WorldImage makeWallsScene(int scale, int width, int height) {
    WorldImage image = 
        new RectangleImage(width * scale, height * scale, OutlineMode.OUTLINE, Color.black);
    for (Edge e: this.allEdges) {
      image = e.makeEdgeScene(scale, image);
    }
    return image;
  }

  // Uses Kruskal's algorithm to form the list of edges that are walls in a proper maze
  void makeMaze() {
    Comparator<Edge> edgeWeight = (Edge x, Edge y) -> x.weight - y.weight;
    int verticesSize = this.vertices.size();
    HashMap<Vertex, Vertex> representatives = new HashMap<Vertex, Vertex>();
    ArrayList<Edge> worklist = new ArrayList<Edge>(this.allEdges); 
    this.edgesInTree.clear();
    new ArrayUtils().mergesort(worklist, edgeWeight);
    int numEdges = 0;

    for (int i = 0; i < verticesSize; i++) {
      representatives.put(this.vertices.get(i), this.vertices.get(i));
    }

    while (numEdges < verticesSize - 1) {
      Edge edgeTemp = worklist.remove(0);
      Vertex fromRep = new HashUtils().find(representatives, edgeTemp.from);
      Vertex toRep = new HashUtils().find(representatives, edgeTemp.to);

      if (!fromRep.equals(toRep)) {
        this.edgesInTree.add(edgeTemp);
        representatives.replace(fromRep, toRep);
        numEdges += 1;
      }

    }
  }

  // Changes the edges in the created maze to not be walls
  public void createPaths() {
    for (Edge e: this.edgesInTree) {
      e.makePath();
    }
  }

  // is there an edge with no wall between the given positions
  public boolean moveValid(int x1, int y1, int x2, int y2) {
    return new ArrayUtils().edgeExists(this.edgesInTree, x1, y1, x2, y2);
  }

  //queue is for Breadth First Search
  void bfs() {
    this.worklist = new Queue<Vertex>();
    this.solutionPath = new ArrayList<Vertex>();
    this.worklist.add(this.vertices.get(0));
    for (Vertex v: this.vertices) {
      this.alreadySeen.put(v, false);
    }
    this.cameFromEdge = new HashMap<Vertex, Vertex>();
    this.searchOver = false;
  }

  //stack is for depth-first search
  void dfs() {
    this.worklist = new Stack<Vertex>();
    this.solutionPath = new ArrayList<Vertex>();
    this.worklist.add(this.vertices.get(0));
    for (Vertex v: this.vertices) {
      this.alreadySeen.put(v, false);
    }
    this.cameFromEdge = new HashMap<Vertex, Vertex>();
    this.searchOver = false;
  }

  //Breadth and depth-first searchHelp, worklist is a Queue or a Stack, depending on the algorithm
  HashMap<Vertex, Boolean> search() {

    if (this.worklist.isEmpty()) {
      throw new RuntimeException("Worklist is Empty");
    } else {
      Vertex next = this.worklist.remove();
      if (this.alreadySeen.get(next)) {
        // do nothing: we've already seen this one
      }
      else if (next.equals(this.end)) {
        this.reconstruct(); // Success
        this.searchOver = true;
      }
      else {
        // add all the neighbors of next to the worklist for further processing
        for (Edge e : next.outEdges) {
          if (!e.wall) {
            if (next.equals(e.from) && !this.alreadySeen.get(e.to)) {
              this.worklist.add(e.to);
              //Record the edge (next->n) in the cameFromEdge map
              this.cameFromEdge.put(e.to, next); 
            }
            if (next.equals(e.to) && !this.alreadySeen.get(e.from)) {
              this.worklist.add(e.from);
              //Record the edge (next->n) in the cameFromEdge map
              this.cameFromEdge.put(e.from, next);
            }
          }
        }
        this.alreadySeen.put(next, true);
      }

      return new HashMap<Vertex, Boolean>(this.alreadySeen);
    }
  }

  // Change the solutionPath to the solution to the end node using the cameFromEdge hashMap
  // that produces the path in reverse by traversing from the end
  void reconstruct() {
    Vertex current = end;
    while (!current.isPosition(0, 0)) {
      this.solutionPath.add(current);
      current = this.cameFromEdge.get(current);
    }
  }

  // is the search for the end over?
  boolean searchOver() {
    return this.searchOver;
  }

  // returns the solution path as a list of Posn
  ArrayList<Posn> solutionPosns() {
    ArrayList<Posn> temp = new ArrayList<Posn>();
    for (Vertex v: this.solutionPath) {
      v.addPosition(temp);
    }
    return temp;
  }
}

//Represents a mutable collection of items
abstract class ICollection<T> {
  Deque<T> contents;

  ICollection() {
    this.contents = new ArrayDeque<T>();
  }

  // Is this collection empty?
  abstract boolean isEmpty();

  // EFFECT: adds the item to the collection
  abstract void add(T item);

  // Returns the first item of the collection
  // EFFECT: removes that first item
  abstract T remove();
}

//representation of the worklist used in breadth-first search
class Stack<T> extends ICollection<T> {

  //Is this collection empty?
  public boolean isEmpty() {
    return this.contents.isEmpty();
  }

  //EFFECT: adds the item to the collection
  public T remove() {
    if (this.isEmpty()) {
      throw new RuntimeException("Tried removing from empty Stack");
    }
    return this.contents.removeFirst();
  }

  //Returns the first item of the collection
  // EFFECT: removes that first item
  public void add(T item) {
    this.contents.addFirst(item);
  }
}

//representation of the worklist used in depth-first search
class Queue<T> extends ICollection<T> {

  //Is this collection empty?
  public boolean isEmpty() {
    return this.contents.isEmpty();
  }

  //EFFECT: adds the item to the collection
  public T remove() {
    if (this.isEmpty()) {
      throw new RuntimeException("Tried removing from empty Queue");
    }
    return this.contents.removeFirst();
  }

  //Returns the first item of the collection
  // EFFECT: removes that first item
  public void add(T item) {
    this.contents.addLast(item); // NOTE: Different from Stack!
  }
}

//Class used for methods to act upon ArrayLists
class ArrayUtils {

  // Sorts the provided list according to the given comparator
  <T> void mergesort(ArrayList<T> list, Comparator<T> comp) {
    ArrayList<T> temp = new ArrayList<T>(list);
    mergesortHelp(list, temp, comp, 0, list.size() - 1);
  }

  // Helps sort the provided list according to the given comparator and bounds given
  <T> void mergesortHelp(ArrayList<T> list, ArrayList<T> temp, 
      Comparator<T> comp, int low, int high) {
    if (low < high) {
      int mid = (low + high) / 2;

      mergesortHelp(list, temp, comp, low, mid);
      mergesortHelp(list, temp, comp, mid + 1, high);
      merge(list, temp, comp, low, mid, high);
    }
  }

  // merges the sorted sublists in the given list defined by the bounds based on the comparator
  <T> void merge(ArrayList<T> list, ArrayList<T> temp, 
      Comparator<T> comp, int low, int mid, int high) {
    int i = low;
    int j = mid + 1;
    int k = low;

    while ((i <= mid) && (j <= high)) {
      int r = comp.compare(list.get(i), list.get(j));
      if (r <= 0) { // i<=j
        temp.set(k, list.get(i));
        i += 1;
      } else {
        temp.set(k, list.get(j));
        j += 1;
      }
      k += 1;
    }

    while (i <= mid) {
      temp.set(k, list.get(i));
      i += 1;
      k += 1; 
    }

    while (j <= mid) {
      temp.set(k, list.get(j));
      j += 1;
      k += 1; 
    }

    for (k = low; k <= high; k++) {
      list.set(k, temp.get(k));
    }
  }

  //creates an ArrayList<Vertex> of the board with no edges
  ArrayList<Vertex> makeStartingVertices(int width, int height) {
    ArrayList<Vertex> vertexList = new ArrayList<Vertex>();
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        vertexList.add(new Vertex(i, j));
      }
    }
    return vertexList;
  }

  //creates an ArrayList<Edge> of all the board's edges
  ArrayList<Edge> makeStartingEdges(ArrayList<Vertex> allVertices, 
      int width, int height, Random rand) {
    ArrayList<Edge> edgeList = new ArrayList<Edge>();

    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        if (y < height - 1) {
          edgeList.add(new Edge(allVertices.get(y + (x * height)), 
              allVertices.get(y + (x * height) + 1), rand));
        } 
        if (x < width - 1) {
          edgeList.add(new Edge(allVertices.get(y + (x * height)), 
              allVertices.get(y + (x * height) + height), rand));
        }
      }
    }
    return edgeList;
  }

  // does an edge with the given vertex positions exist
  boolean edgeExists(ArrayList<Edge> l, int x1, int y1, int x2, int y2) {
    boolean temp = false;
    for (Edge e: l) {
      temp = temp 
          || (e.from.isPosition(x1, y1) && e.to.isPosition(x2, y2))
          || (e.to.isPosition(x1, y1) && e.from.isPosition(x2, y2));
    }
    return temp;
  }

}

//class used for methods that act upon hashMaps
class HashUtils {

  // Finds the top level representative of the given Vertex
  Vertex find(HashMap<Vertex, Vertex> reps, Vertex name) {
    Vertex vert = name;
    Vertex mappedTo = reps.get(vert);
    while (!mappedTo.equals(vert)) {
      vert = mappedTo;
      mappedTo = reps.get(vert);
    }
    return vert;
  }


}


//represents the world class, MazeWorld
class MazeWorld extends World {
  int width;
  int height;
  int scale;
  Graph graph;
  Posn playerPos;
  boolean endReached;
  WorldImage mazeWallsImage;
  ArrayList<Posn> placesBeen;
  int phase;

  // Random Game Constructor
  MazeWorld(int width, int height, int scale) {
    if (width < 1) {
      throw new IllegalArgumentException("Given width is less than 1");
    }
    if (height < 1) {
      throw new IllegalArgumentException("Given height is less than 1");
    }
    if (scale < 1) {
      throw new IllegalArgumentException("Given scale is less than 1");
    }    
    if (scale % 2 != 0) {
      throw new IllegalArgumentException("Given scale is odd");
    }
    this.width = width;
    this.height = height;
    this.graph = new Graph(new Random(), this.width, this.height);
    this.graph.makeMaze();
    this.graph.createPaths();
    this.scale = scale; 
    this.playerPos = new Posn(0,0);
    this.endReached = false;
    this.mazeWallsImage = this.graph.makeWallsScene(this.scale, this.width, this.height);
    this.placesBeen = new ArrayList<Posn>();
    this.phase = 0;
  }

  // Constructor for testing
  MazeWorld(Random rand, int width, int height) {
    if (width < 1) {
      throw new IllegalArgumentException("Given width is less than 1");
    }
    if (height < 1) {
      throw new IllegalArgumentException("Given height is less than 1");
    }
    this.width = width;
    this.height = height;
    this.graph = new Graph(rand, this.width, this.height);
    this.graph.makeMaze();
    this.graph.createPaths();
    this.scale = 20; 
    this.playerPos = new Posn(0,0);
    this.endReached = false;
    this.mazeWallsImage = this.graph.makeWallsScene(this.scale, this.width, this.height);
    this.placesBeen = new ArrayList<Posn>();
    this.phase = 0;
  }

  @Override
  // produces the world scene
  public WorldScene makeScene() {
    WorldScene image =  this.getEmptyScene();
    // Background
    image.placeImageXY(new RectangleImage(this.width * this.scale, this.height * this.scale,
        "solid", Color.LIGHT_GRAY), this.width * this.scale / 2, this.height * this.scale / 2);
    // Vertices been
    for (Posn p: this.placesBeen) {
      image.placeImageXY(new RectangleImage(this.scale, 
          this.scale,"solid", new Color(138,192,255)), 
          p.x * this.scale + this.scale / 2, p.y * this.scale + this.scale / 2);
    }
    // Solution Vertices
    for (Posn p: this.graph.solutionPosns()) {
      image.placeImageXY(new RectangleImage(this.scale, this.scale,"solid", Color.white), 
          p.x * this.scale + this.scale / 2, p.y * this.scale + this.scale / 2);
    }
    // Start
    image.placeImageXY(new RectangleImage(this.scale, this.scale,"solid", Color.green), 
        this.scale / 2, this.scale / 2);
    // End
    image.placeImageXY(new RectangleImage(this.scale, this.scale,"solid", Color.magenta), 
        this.width * this.scale - this.scale / 2, this.height * this.scale - this.scale / 2);
    // Walls
    image.placeImageXY(this.mazeWallsImage, 
        this.width * this.scale / 2, this.height * this.scale / 2); 
    // Player
    image.placeImageXY(new CircleImage(this.scale / 2,"solid", new Color(207,85,81)), 
        this.playerPos.x * this.scale + this.scale / 2, 
        this.playerPos.y * this.scale + this.scale / 2);


    return image;
  }

  @Override
  // handles onTick, updating the world after each tick
  public void onTick() {
    if (this.phase == 1 ) { 
      HashMap<Vertex, Boolean> alreadySeen = this.graph.search();
      if (this.graph.searchOver()) {
        this.phase = 0;
      } else {
        for (Vertex v: alreadySeen.keySet()) {
          if (alreadySeen.get(v)) {
            if (!this.placesBeen.contains(new Posn(v.x, v.y))) {
              v.addPosition(this.placesBeen);
            }
          }
        }
      }
    }

    if (this.phase == 2 ) { 
      this.graph.search();
      if (this.graph.searchOver()) {
        this.phase = 3;
      } 
    }

    if (this.playerPos.x == this.width - 1 && this.playerPos.y == this.height - 1 
        && phase != 2 && phase != 3) {
      this.endReached = true;
      this.phase = 2;

      this.graph.dfs();
    }
  }

  @Override
  //handles when there are keyEvents
  public void onKeyEvent(String key) {
    if (!endReached && phase == 0) {
      if (key.equals("right")) {
        if (this.graph.moveValid(this.playerPos.x, this.playerPos.y, 
            this.playerPos.x + 1, this.playerPos.y)) {
          this.playerPos.x += 1;
        }
        //        this.graph.bfs(new Vertex(width-1,height-1));
      } else if (key.equals("left")) {
        if (this.graph.moveValid(this.playerPos.x, this.playerPos.y, 
            this.playerPos.x - 1, this.playerPos.y)) {
          this.playerPos.x -= 1;
        }
      } else if (key.equals("up")) {
        if (this.graph.moveValid(this.playerPos.x, this.playerPos.y, 
            this.playerPos.x, this.playerPos.y - 1)) {
          this.playerPos.y -= 1;
        }
      } else if (key.equals("down")) {
        if (this.graph.moveValid(this.playerPos.x, this.playerPos.y, 
            this.playerPos.x, this.playerPos.y + 1)) {
          this.playerPos.y += 1;
        }
      }
      if (!this.placesBeen.contains(this.playerPos)) {
        this.placesBeen.add(new Posn(this.playerPos.x, this.playerPos.y));
      }


    } 

    if (key.equals("b")) {
      this.phase = 1;
      this.placesBeen = new ArrayList<Posn>();
      this.graph.bfs();

    } else if (key.equals("d")) {
      this.phase = 1;
      this.placesBeen = new ArrayList<Posn>();
      this.graph.dfs();
    }

    if (key.equals("r")) {
      this.graph = new Graph(new Random(), this.width, this.height);
      this.graph.makeMaze();
      this.graph.createPaths();
      this.playerPos = new Posn(0,0);
      this.endReached = false;
      this.mazeWallsImage = this.graph.makeWallsScene(this.scale, this.width, this.height);
      this.placesBeen = new ArrayList<Posn>();
      this.phase = 0;
    }
  }
}


class ExamplesMaze {

  Comparator<Edge> edgeWeight = (Edge x, Edge y) -> x.weight - y.weight;

  Object int1;
  Object objectv1;
  Object objectv2;

  Random rand1 = new Random(1);
  ArrayUtils util = new ArrayUtils();
  HashUtils hashUtil = new HashUtils();

  Vertex v1;
  Vertex v2;
  Vertex v3;
  Vertex v4;

  Vertex v5;
  Vertex v6;
  Vertex v7;
  Vertex v8;

  Vertex v9;
  Vertex v10;
  Vertex v11;
  Vertex v12;

  Vertex v13;
  Vertex v14;
  Vertex v15;
  Vertex v16;

  ArrayList<Vertex> vertList0;
  ArrayList<Vertex> vertList1;
  ArrayList<Vertex> vertList2;

  Edge e1;
  Edge e2;
  Edge e3;
  Edge e4;

  Edge e5;
  Edge e6;
  Edge e7;
  Edge e8;

  ArrayList<Edge> edgeList1;
  ArrayList<Edge> edgeList2;
  ArrayList<Edge> edgeList3;
  ArrayList<Edge> edgeList4;
  ArrayList<Edge> edgeList5;
  ArrayList<Edge> edgeList6;

  Graph g0 = new Graph(rand1, 2, 2);
  Graph g1 = new Graph(rand1, 2, 2);
  Graph g2;

  HashMap<Vertex, Vertex> map1;

  MazeWorld w1;
  MazeWorld w2;
  MazeWorld w3;
  MazeWorld w4;

  ICollection<Vertex> ic1;
  ICollection<Vertex> ic2;
  ICollection<Vertex> ic3;
  ICollection<Vertex> ic4;

  ArrayList<Posn> lposn1;
  ArrayList<Posn> lposn2;

  void initializeObjects() {    
    this.rand1 = new Random(1);

    this.int1 = 1;
    this.objectv1 = new Vertex(0,0);
    this.objectv2 = new Vertex(1,0);

    this.v1 = new Vertex(0,0);
    this.v2 = new Vertex(1,0);
    this.v3 = new Vertex(0,1);
    this.v4 = new Vertex(1,1);

    this.v5 = new Vertex(0,0);
    this.v6 = new Vertex(0,1);
    this.v7 = new Vertex(1,0);
    this.v8 = new Vertex(1,1);

    this.v9 = new Vertex(0,0);
    this.v10 = new Vertex(0,1);
    this.v11 = new Vertex(1,0);
    this.v12 = new Vertex(1,1);

    this.v13 = new Vertex(0,0);
    this.v14 = new Vertex(0,1);
    this.v15 = new Vertex(1,0);
    this.v16 = new Vertex(1,1);

    this.vertList0 = new ArrayList<Vertex>();
    this.vertList1 = new ArrayList<Vertex>();
    this.vertList2 = new ArrayList<Vertex>();

    this.vertList1.add(this.v1);
    this.vertList1.add(this.v3);
    this.vertList1.add(this.v2);
    this.vertList1.add(this.v4);

    this.vertList2.add(this.v9);
    this.vertList2.add(this.v10);
    this.vertList2.add(this.v11);
    this.vertList2.add(this.v12);

    this.e1 = new Edge(this.v1, this.v2, rand1);
    this.e2 = new Edge(this.v1, this.v3, rand1);
    this.e3 = new Edge(this.v2, this.v4, rand1);
    this.e4 = new Edge(this.v3, this.v4, rand1);

    this.edgeList1 = new ArrayList<Edge>();
    this.edgeList2 = new ArrayList<Edge>();
    this.edgeList3 = new ArrayList<Edge>();
    this.edgeList4 = new ArrayList<Edge>();
    this.edgeList5 = new ArrayList<Edge>();
    this.edgeList6 = new ArrayList<Edge>();

    this.edgeList2.add(this.e1);
    this.edgeList2.add(this.e2);
    this.edgeList2.add(this.e3);
    this.edgeList2.add(this.e4);

    this.edgeList4.add(this.e1);
    this.edgeList4.add(this.e2);
    this.edgeList4.add(this.e3);
    this.edgeList4.add(this.e4);

    this.edgeList6.add(this.e2);
    this.edgeList6.add(this.e3);

    this.g0 = new Graph(rand1, 1, 1);
    this.g1 = new Graph(rand1, 2, 2);

    this.map1 = new HashMap<Vertex, Vertex>();
    this.map1.put(this.v1, this.v1);
    this.map1.put(this.v2, this.v1);
    this.map1.put(this.v3, this.v2);
    this.map1.put(this.v4, this.v3);
  }

  void initializeObjects2() { 
    initializeObjects();
    this.g2 = new Graph(rand1, 8, 3);

    this.w1 = new MazeWorld(rand1, 1, 1);
    this.w2 = new MazeWorld(rand1, 2, 2);
    this.w3 = new MazeWorld(rand1, 10, 10);

    this.ic1 = new Stack<Vertex>();
    this.ic2 = new Stack<Vertex>();
    this.ic3 = new Queue<Vertex>();
    this.ic4 = new Queue<Vertex>();

    this.ic2.add(new Vertex(0, 0));
    this.ic2.add(new Vertex(0, 1));
    this.ic2.add(new Vertex(1, 0));
    this.ic2.add(new Vertex(1, 1));

    this.ic4.add(new Vertex(0, 0));
    this.ic4.add(new Vertex(0, 1));
    this.ic4.add(new Vertex(1, 0));
    this.ic4.add(new Vertex(1, 1));

    this.lposn1 = new ArrayList<Posn>();
    this.lposn2 = new ArrayList<Posn>();

    this.lposn2.add(new Posn(0,0));
    this.lposn2.add(new Posn(1,0));

    this.g2 = new Graph(rand1, 3, 3);
    this.w4 = new MazeWorld(rand1, 2, 2);
  }

  //How to play the game:
  //Press "d" to use depth-first search
  //Press "b" to use breadth-first search
  //Press "r" to reset the maze and create a new random maze
  //Use arrow keys (up, down, left, right) to manually move around the maze

  //runs the maze game
  @Test
  public void testGame(Tester t) {
    initializeObjects2();
    int width = 60;
    int height = 60;
    int scale = (150 / ((width + height) / 7)) * 2;
    MazeWorld m = new MazeWorld(width, height, scale);
    m.bigBang(width * scale, height * scale, .0001);
  }



//  //tests for the method compare for an edgeWeight
//  boolean testCompare(Tester t) {
//    initializeObjects();
//    return t.checkExpect(this.e1.weight, 35)
//        && t.checkExpect(this.e2.weight, 38)
//        && t.checkExpect(this.e3.weight, 47)
//        && t.checkExpect(this.e4.weight, 13)
//        && t.checkExpect(this.edgeWeight.compare(this.e1, this.e2), -3)
//        && t.checkExpect(this.edgeWeight.compare(this.e3, this.e4), 34)
//        && t.checkExpect(this.edgeWeight.compare(this.e4, this.e4), 0);
//  }
//
//  //tests for the constructors of Vertex
//  boolean testVertex(Tester t) {
//    return t.checkConstructorException(new IllegalArgumentException("Given X is less than 0"),
//        "Vertex", -1, 0)
//        && t.checkConstructorException(new IllegalArgumentException("Given Y is less than 0"),
//            "Vertex", 0, -1)
//        && t.checkConstructorException(new IllegalArgumentException("Given X is less than 0"),
//            "Vertex", -1, -1);
//  }
//
//  //tests for the method connect for a Vertex
//  boolean testConnect(Tester t) {
//    initializeObjects();
//    boolean tBefore = t.checkExpect(this.v5.outEdges, new ArrayList<Edge>())
//        && t.checkExpect(this.v8.outEdges, new ArrayList<Edge>());
//
//    this.v5.connect(this.e1);
//    this.v8.connect(this.e4);
//
//    ArrayList<Edge> out1 = new ArrayList<Edge>();
//    out1.add(this.e1);
//
//    ArrayList<Edge> out2 = new ArrayList<Edge>();
//    out2.add(this.e4);
//
//    boolean tAfter = t.checkExpect(this.v5.outEdges, out1)
//        && t.checkExpect(this.v8.outEdges, out2);
//
//    return tBefore && tAfter;
//  }
//
//  //tests for the method equals for a Vertex
//  boolean testEquals(Tester t) {
//    initializeObjects();
//    return t.checkExpect(this.v1.equals(this.int1), false)
//        && t.checkExpect(this.v1.equals(this.objectv1), true)
//        && t.checkExpect(this.v1.equals(this.objectv2), false);
//  }
//
//  //tests for the method hashCode for a Vertex
//  boolean testHashCode(Tester t) {
//    initializeObjects();
//    return t.checkExpect(this.v1.hashCode(), "x0y0".hashCode())
//        && t.checkExpect(new Vertex(1,52).hashCode(), "x1y52".hashCode());
//  }
//
//  //tests for the method makeEdgeSceneVertex for a Vertex
//  boolean testmakeEdgeSceneVertex(Tester t) {
//    initializeObjects();
//    WorldImage rec = new RectangleImage(4, 4, OutlineMode.OUTLINE, Color.black);
//    WorldImage out1 = new OverlayOffsetAlign(AlignModeX.LEFT, AlignModeY.TOP,
//        rec,
//        1 * 20, 0,  new LineImage(new Posn(0, 20), Color.BLACK));
//    WorldImage out2 = new OverlayOffsetAlign(AlignModeX.LEFT, AlignModeY.TOP,
//        rec,
//        0, 20,  new LineImage(new Posn(20, 0), Color.BLACK));
//
//    return t.checkExpect(this.g1.allEdges.get(1).from.makeEdgeSceneVertex(20, rec,
//        this.g1.allEdges.get(1).to, Color.BLACK), out1)
//        && t.checkExpect(this.g1.allEdges.get(0).from.makeEdgeSceneVertex(20, rec,
//            this.g1.allEdges.get(0).to, Color.BLACK), out2);
//  }
//
//  //tests for the method isPosition for a Vertex
//  boolean testIsPosition(Tester t) {
//    initializeObjects();
//    return t.checkExpect(this.v2.isPosition(1,0), true)
//        && t.checkExpect(this.v2.isPosition(1,1), false)
//        && t.checkExpect(this.v2.isPosition(0,1), false)
//        && t.checkExpect(this.v2.isPosition(-1,-1), false);
//  }
//
//  //tests for the method addPosition for a Vertex
//  boolean testAddPosition(Tester t) {
//    initializeObjects2();
//    ArrayList<Posn> out1 = new ArrayList<Posn>();
//    ArrayList<Posn> out2 = new ArrayList<Posn>();
//
//    out2.add(new Posn(0, 0));
//    out2.add(new Posn(1, 0));
//
//    boolean tBefore = t.checkExpect(this.lposn1, new ArrayList<Posn>())
//        && t.checkExpect(this.lposn2, out2);
//
//    out1.add(new Posn(1, 0));
//    out2.add(new Posn(1, 1));
//
//    this.v2.addPosition(this.lposn1);
//    this.v4.addPosition(this.lposn2);
//
//    boolean tAfter = t.checkExpect(this.lposn1, out1)
//        && t.checkExpect(this.lposn2, out2);
//
//    return tBefore && tAfter;
//  }
//
//  //tests for the method makePath for an Edge
//  boolean testMakePath(Tester t) {
//    initializeObjects();
//    this.e2.wall = false;
//    boolean tBefore = t.checkExpect(this.e1.wall, true)
//        && t.checkExpect(this.e2.wall, false);
//
//    this.e1.makePath();
//    this.e2.makePath();
//
//    boolean tAfter = t.checkExpect(this.e1.wall, false)
//        && t.checkExpect(this.e2.wall, false);
//    return tBefore && tAfter;
//  }
//
//  //tests for the method makeEdgeScene for an Edge
//  boolean testMakeEdgeScene(Tester t) {
//    initializeObjects();
//    WorldImage rec = new RectangleImage(4, 4, OutlineMode.OUTLINE, Color.black);
//    boolean testWall = t.checkExpect(
//        this.g1.allEdges.get(0).makeEdgeScene(20, rec),
//        this.g1.allEdges.get(0).from.makeEdgeSceneVertex(
//            20, rec, this.g1.allEdges.get(0).to, Color.BLACK))
//        && t.checkExpect(
//            this.g1.allEdges.get(1).makeEdgeScene(20, rec),
//            this.g1.allEdges.get(1).from.makeEdgeSceneVertex(
//                20, rec, this.g1.allEdges.get(1).to, Color.BLACK));
//
//    this.g1.allEdges.get(0).wall = false;
//    this.g1.allEdges.get(1).wall = false;
//
//    boolean testNotWall = t.checkExpect(
//        this.g1.allEdges.get(0).makeEdgeScene(20, rec),
//        this.g1.allEdges.get(0).from.makeEdgeSceneVertex(
//            20, rec, this.g1.allEdges.get(0).to, Color.LIGHT_GRAY))
//        && t.checkExpect(
//            this.g1.allEdges.get(1).makeEdgeScene(20, rec),
//            this.g1.allEdges.get(1).from.makeEdgeSceneVertex(
//                20, rec, this.g1.allEdges.get(1).to, Color.LIGHT_GRAY));
//
//    return testWall && testNotWall;
//  }
//
//  //tests for the constructors of Graph
//  boolean testGraph(Tester t) {
//    return t.checkConstructorException(new IllegalArgumentException("Given width is less than 1"),
//        "Graph", new Random(), 0, 0)
//        && t.checkConstructorException(new IllegalArgumentException("Given height is less than 1"),
//            "Graph", new Random(), 1, 0)
//        && t.checkConstructorException(new IllegalArgumentException("Given width is less than 1"),
//            "Graph", new Random(), 0, 1);
//  }
//
//  //tests for the method makeWallsScene for a Graph
//  boolean testMakeWallsScene(Tester t) {
//    initializeObjects();
//    WorldImage image1 = new RectangleImage(10, 10, OutlineMode.OUTLINE, Color.black);
//
//    this.g1.allEdges.get(2).wall = false;
//    WorldImage image2 = new RectangleImage(2 * 20, 2 * 20, OutlineMode.OUTLINE, Color.black);
//    image2 = this.g1.allEdges.get(0).makeEdgeScene(20, image2);
//    image2 = this.g1.allEdges.get(1).makeEdgeScene(20, image2);
//    image2 = this.g1.allEdges.get(2).makeEdgeScene(20, image2);
//    image2 = this.g1.allEdges.get(3).makeEdgeScene(20, image2);
//
//    return t.checkExpect(this.g0.makeWallsScene(10, 1, 1), image1)
//        && t.checkExpect(this.g1.makeWallsScene(20, 2, 2), image2);
//  }
//
//  // tests for the method makeMaze for a Graph
//  boolean testMakeMaze(Tester t) {
//    initializeObjects();
//    boolean tBefore = t.checkExpect(this.g0.edgesInTree, new ArrayList<Edge>())
//        && t.checkExpect(this.g1.edgesInTree, new ArrayList<Edge>());
//
//    ArrayList<Vertex> lv = new ArrayUtils().makeStartingVertices(2,2);
//
//    ArrayList<Edge> le = new ArrayUtils().makeStartingEdges(lv, 2, 2, rand1);
//    this.g1.makeMaze();
//
//    le.get(0).weight = 4;
//    Edge ed1 = le.get(0);
//
//    le.get(1).weight = 4;
//    Edge ed2 = le.get(1);
//
//    le.get(2).weight = 34;
//
//    le.get(3).weight = 6;
//    Edge ed3 = le.get(3);
//
//    ArrayList<Edge> out1 = new ArrayList<Edge>();
//    out1.add(ed1);
//    out1.add(ed2);
//    out1.add(ed3);
//
//    boolean tAfter = t.checkExpect(this.g0.edgesInTree, new ArrayList<Edge>())
//        && t.checkExpect(this.g1.edgesInTree, out1);
//
//    return tBefore && tAfter;
//  }
//
//  //tests for the method createPaths for a Graph
//  boolean testCreatePaths(Tester t) {
//    initializeObjects();
//    this.e3.wall = false;
//    this.g0.edgesInTree = this.edgeList1;
//    this.g1.edgesInTree = this.edgeList6;
//    boolean tBefore = t.checkExpect(this.g0.edgesInTree.size(), 0)
//        && t.checkExpect(this.g0.edgesInTree, new ArrayList<Edge>())
//        && t.checkExpect(this.g1.edgesInTree.size(), 2)
//        && t.checkExpect(this.g1.edgesInTree.get(0).wall, true)
//        && t.checkExpect(this.g1.edgesInTree.get(1).wall, false);
//
//    this.g0.createPaths();
//    this.g1.createPaths();
//
//    boolean tAfter = t.checkExpect(this.g0.edgesInTree.size(), 0)
//        && t.checkExpect(this.g0.edgesInTree, new ArrayList<Edge>())
//        && t.checkExpect(this.g1.edgesInTree.size(), 2)
//        && t.checkExpect(this.g1.edgesInTree.get(0).wall, false)
//        && t.checkExpect(this.g1.edgesInTree.get(1).wall, false);
//
//    return tBefore && tAfter;
//  }
//
//  //tests for the method moveValid for a graph
//  boolean testMoveValid(Tester t) {
//    initializeObjects2();
//    return t.checkExpect(this.g1.moveValid(0, 0, 1, 0), false)
//        && t.checkExpect(this.w2.graph.moveValid(0, 0, 1, 0), true)
//        && t.checkExpect(this.w2.graph.moveValid(0, 1, 1, 1), true)
//        && t.checkExpect(this.w2.graph.moveValid(1, 0, 1, 1), true)
//        && t.checkExpect(this.w2.graph.moveValid(0, 0, 0, 1), false)
//        && t.checkExpect(this.w2.graph.moveValid(0, 0, 1, 1), false)
//        && t.checkExpect(this.w2.graph.moveValid(1, 0, 0, 0), true)
//        && t.checkExpect(this.w2.graph.moveValid(1, 1, 0, 1), true)
//        && t.checkExpect(this.w2.graph.moveValid(1, 1, 1, 0), true)
//        && t.checkExpect(this.w2.graph.moveValid(0, 1, 0, 0), false)
//        && t.checkExpect(this.w2.graph.moveValid(1, 1, 0, 0), false);
//  }
//
//  //tests for the method bfs for a Graph
//  boolean testBfs(Tester t) {
//    initializeObjects2();
//    ICollection<Vertex> wl = new Queue<Vertex>();
//    wl.add(this.v1);
//    Graph temp = this.w2.graph;
//    temp.worklist = new Queue<Vertex>();
//    temp.worklist.add(this.v1);
//    temp.cameFromEdge.put(this.v3, this.v4);
//    ArrayList<Vertex> sP = new ArrayList<Vertex>();
//    sP.add(this.v2);
//    temp.solutionPath.add(this.v2);
//    temp.searchOver = true;
//    temp.alreadySeen = new HashMap<Vertex, Boolean>();
//    temp.alreadySeen.put(v1, false);
//    HashMap<Vertex, Boolean> alT = new HashMap<Vertex, Boolean>();
//    alT.put(this.v1, false);
//    HashMap<Vertex, Vertex> cFET = new HashMap<Vertex, Vertex>();
//    cFET.put(this.v3, this.v4);
//
//    boolean tBefore1 = t.checkExpect(temp.worklist, wl)
//        && t.checkExpect(temp.solutionPath, sP)
//        && t.checkExpect(temp.alreadySeen, alT)
//        && t.checkExpect(temp.cameFromEdge, cFET)
//        &&  t.checkExpect(temp.searchOver, true);
//
//    temp.bfs();
//
//    ICollection<Vertex> wl2 = new Queue<Vertex>();
//    wl2.add(temp.vertices.get(0));
//    HashMap<Vertex, Boolean> alreadySeenAfter = new HashMap<Vertex, Boolean>();
//    alreadySeenAfter.put(temp.vertices.get(0), false);
//    alreadySeenAfter.put(temp.vertices.get(1), false);
//    alreadySeenAfter.put(temp.vertices.get(2), false);
//    alreadySeenAfter.put(temp.vertices.get(3), false);
//
//    boolean tAfter1 = t.checkExpect(temp.worklist, wl2)
//        && t.checkExpect(temp.solutionPath, new ArrayList<Vertex>())
//        && t.checkExpect(temp.alreadySeen, alreadySeenAfter)
//        && t.checkExpect(temp.cameFromEdge, new HashMap<Vertex, Vertex>())
//        &&  t.checkExpect(temp.searchOver, false);
//
//    return tBefore1 && tAfter1;
//  }
//
//  //tests for the method dfs for a Graph
//  boolean testDfs(Tester t) {
//    initializeObjects2();
//    ICollection<Vertex> wl = new Queue<Vertex>();
//    wl.add(this.v1);
//    Graph temp = this.w2.graph;
//    temp.worklist = new Queue<Vertex>();
//    temp.worklist.add(this.v1);
//    temp.cameFromEdge.put(this.v3, this.v4);
//    ArrayList<Vertex> sP = new ArrayList<Vertex>();
//    sP.add(this.v2);
//    temp.solutionPath.add(this.v2);
//    temp.searchOver = true;
//    temp.alreadySeen = new HashMap<Vertex, Boolean>();
//    temp.alreadySeen.put(v1, false);
//    HashMap<Vertex, Boolean> alT = new HashMap<Vertex, Boolean>();
//    alT.put(this.v1, false);
//    HashMap<Vertex, Vertex> cFET = new HashMap<Vertex, Vertex>();
//    cFET.put(this.v3, this.v4);
//
//    boolean tBefore1 = t.checkExpect(temp.worklist, wl)
//        && t.checkExpect(temp.solutionPath, sP)
//        && t.checkExpect(temp.alreadySeen, alT)
//        && t.checkExpect(temp.cameFromEdge, cFET)
//        &&  t.checkExpect(temp.searchOver, true);
//
//    temp.dfs();
//
//    ICollection<Vertex> wl2 = new Stack<Vertex>();
//    wl2.add(temp.vertices.get(0));
//    HashMap<Vertex, Boolean> alreadySeenAfter = new HashMap<Vertex, Boolean>();
//    alreadySeenAfter.put(temp.vertices.get(0), false);
//    alreadySeenAfter.put(temp.vertices.get(1), false);
//    alreadySeenAfter.put(temp.vertices.get(2), false);
//    alreadySeenAfter.put(temp.vertices.get(3), false);
//
//    boolean tAfter1 = t.checkExpect(temp.worklist, wl2)
//        && t.checkExpect(temp.solutionPath, new ArrayList<Vertex>())
//        && t.checkExpect(temp.alreadySeen, alreadySeenAfter)
//        && t.checkExpect(temp.cameFromEdge, new HashMap<Vertex, Vertex>())
//        && t.checkExpect(temp.searchOver, false);
//
//    return tBefore1 && tAfter1;
//  }
//
//  //tests for the method search for a Graph
//  boolean testSearch(Tester t) {
//
//    initializeObjects2();
//    this.g0.dfs();
//    this.w4.graph.dfs();
//    ICollection<Vertex> wl0 = new Stack<Vertex>();
//    wl0.add(this.g0.vertices.get(0));
//    HashMap<Vertex, Vertex> cfe0 = new HashMap<Vertex, Vertex>();
//    HashMap<Vertex, Boolean> as0 = new HashMap<Vertex, Boolean>();
//    as0.put(this.g0.vertices.get(0), false);
//
//    ICollection<Vertex> wl1 = new Stack<Vertex>();
//    wl1.add(this.w4.graph.vertices.get(0));
//    HashMap<Vertex, Vertex> cfe1 = new HashMap<Vertex, Vertex>();
//    HashMap<Vertex, Boolean> as1 = new HashMap<Vertex, Boolean>();
//    for (Vertex v: this.w4.graph.vertices) {
//      as1.put(v, false);
//    }
//
//    boolean tBefore1 = t.checkExpect(this.g0.worklist, wl0)
//        && t.checkExpect(this.g0.searchOver, false)
//        && t.checkExpect(this.g0.cameFromEdge, cfe0)
//        && t.checkExpect(this.g0.alreadySeen, as0)
//        && t.checkExpect(this.w4.graph.worklist, wl1)
//        && t.checkExpect(this.w4.graph.searchOver, false)
//        && t.checkExpect(this.w4.graph.cameFromEdge, cfe1)
//        && t.checkExpect(this.w4.graph.alreadySeen, as1);
//
//    wl1 = new Stack<Vertex>();
//    wl1.add(this.w4.graph.vertices.get(1));
//    wl1.add(this.w4.graph.vertices.get(2));
//
//    cfe1.put(this.w4.graph.vertices.get(1), this.w4.graph.vertices.get(0));
//    cfe1.put(this.w4.graph.vertices.get(2), this.w4.graph.vertices.get(0));
//
//    as1.put(this.w4.graph.vertices.get(0), true);
//
//    boolean tReturn1 =  t.checkExpect(this.g0.search(), as0)
//        && t.checkExpect(this.w4.graph.search(), as1);
//
//    boolean tAfter1 = t.checkExpect(this.g0.worklist, new Stack<Vertex>())
//        && t.checkExpect(this.g0.searchOver, true)
//        && t.checkExpect(this.g0.cameFromEdge, cfe0)
//        && t.checkExpect(this.g0.alreadySeen, as0)
//        && t.checkExpect(this.w4.graph.worklist, wl1)
//        && t.checkExpect(this.w4.graph.searchOver, false)
//        && t.checkExpect(this.w4.graph.cameFromEdge, cfe1)
//        && t.checkExpect(this.w4.graph.alreadySeen, as1);
//
//    wl1.remove();
//
//    as1.put(this.w4.graph.vertices.get(2), true);
//
//    boolean tReturn2 =  t.checkException(
//        new RuntimeException("Worklist is Empty"), this.g0, "search")
//        && t.checkExpect(this.w4.graph.search(), as1);
//
//    boolean tAfter2 = t.checkExpect(this.w4.graph.worklist, wl1)
//        && t.checkExpect(this.w4.graph.searchOver, false)
//        && t.checkExpect(this.w4.graph.cameFromEdge, cfe1)
//        && t.checkExpect(this.w4.graph.alreadySeen, as1);
//
//    wl1.remove();
//    wl1.add(this.w4.graph.vertices.get(3));
//
//    cfe1.put(this.w4.graph.vertices.get(3), this.w4.graph.vertices.get(1));
//
//    as1.put(this.w4.graph.vertices.get(1), true);
//
//    boolean tReturn3 = t.checkExpect(this.w4.graph.search(), as1);
//
//    boolean tAfter3 = t.checkExpect(this.w4.graph.worklist, wl1)
//        && t.checkExpect(this.w4.graph.searchOver, false)
//        && t.checkExpect(this.w4.graph.cameFromEdge, cfe1)
//        && t.checkExpect(this.w4.graph.alreadySeen, as1);
//
//    wl1.remove();
//
//    boolean tReturn4 = t.checkExpect(this.w4.graph.search(), as1);
//
//    boolean tAfter4 = t.checkExpect(this.w4.graph.worklist, wl1)
//        && t.checkExpect(this.w4.graph.searchOver, true)
//        && t.checkExpect(this.w4.graph.cameFromEdge, cfe1)
//        && t.checkExpect(this.w4.graph.alreadySeen, as1);
//
//    return tBefore1 && tReturn1 && tAfter1 && tReturn2 && tAfter2
//        && tReturn3 && tAfter3 && tReturn4 && tAfter4;
//  }
//
//  //tests for the method reconstruct for a Graph
//  boolean testReconstruct(Tester t) {
//    initializeObjects2();
//    this.g2.cameFromEdge.put(new Vertex(2, 2), new Vertex(2, 1));
//    this.g2.cameFromEdge.put(new Vertex(2, 1), new Vertex(1, 1));
//    this.g2.cameFromEdge.put(new Vertex(1, 1), new Vertex(1, 0));
//    this.g2.cameFromEdge.put(new Vertex(1, 0), new Vertex(0, 0));
//
//    boolean tBefore = t.checkExpect(this.g0.solutionPath, new ArrayList<Vertex>())
//        && t.checkExpect(this.g1.solutionPath, new ArrayList<Vertex>());
//
//    this.g0.reconstruct();
//    this.g2.reconstruct();
//
//    ArrayList<Vertex> out = new ArrayList<Vertex>();
//    out.add(new Vertex(2, 2));
//    out.add(new Vertex(2, 1));
//    out.add(new Vertex(1, 1));
//    out.add(new Vertex(1, 0));
//
//    boolean tAfter = t.checkExpect(this.g0.solutionPath, new ArrayList<Vertex>())
//        && t.checkExpect(this.g2.solutionPath, out);
//
//    return tBefore && tAfter;
//  }
//
//  //tests for the method searchOver for a Graph
//  boolean testSearchOver(Tester t) {
//    initializeObjects2();
//    this.g0.searchOver = true;
//    this.g1.searchOver = false;
//
//    return t.checkExpect(this.g0.searchOver(), true)
//        && t.checkExpect(this.g1.searchOver(), false);
//  }
//
//  //tests for the method soluionPosns for a Graph
//  boolean testSolutionPosns(Tester t) {
//    initializeObjects2();
//    ArrayList<Posn> out = new ArrayList<Posn>();
//    out.add(new Posn(0, 0));
//    out.add(new Posn(1, 0));
//    out.add(new Posn(1, 1));
//
//    this.g1.solutionPath.add(new Vertex(0, 0));
//    this.g1.solutionPath.add(new Vertex(1, 0));
//    this.g1.solutionPath.add(new Vertex(1, 1));
//
//    return t.checkExpect(this.g0.solutionPosns(), new ArrayList<Vertex>())
//        && t.checkExpect(this.g1.solutionPosns(), out);
//  }
//
//  //tests for the method isEmpty for an ICollection
//  boolean testIsEmpty(Tester t) {
//    initializeObjects2();
//    return t.checkExpect(this.ic1.isEmpty(), true)
//        && t.checkExpect(this.ic2.isEmpty(), false)
//        && t.checkExpect(this.ic3.isEmpty(), true)
//        && t.checkExpect(this.ic4.isEmpty(), false);
//  }
//
//  //tests for the method remove for an ICollection
//  boolean testRemove(Tester t) {
//    initializeObjects2();
//    ICollection<Vertex> out1 = new Stack<Vertex>();
//    ICollection<Vertex> out2 = new Stack<Vertex>();
//    ICollection<Vertex> out3 = new Queue<Vertex>();
//    ICollection<Vertex> out4 = new Queue<Vertex>();
//
//    out2.add(new Vertex(0, 0));
//    out2.add(new Vertex(0, 1));
//    out2.add(new Vertex(1, 0));
//    out2.add(new Vertex(1, 1));
//
//    out4.add(new Vertex(0, 0));
//    out4.add(new Vertex(0, 1));
//    out4.add(new Vertex(1, 0));
//    out4.add(new Vertex(1, 1));
//
//    boolean tBefore1 = t.checkExpect(this.ic1, out1)
//        && t.checkExpect(this.ic2, out2)
//        && t.checkExpect(this.ic3, out3)
//        && t.checkExpect(this.ic4, out4);
//
//    out2.contents.removeFirst();
//    out4.contents.removeFirst();
//
//    this.ic2.remove();
//    this.ic4.remove();
//
//    boolean tAfter1 = t.checkException(
//        new RuntimeException("Tried removing from empty Stack"), this.ic1, "remove")
//        && t.checkExpect(this.ic2, out2)
//        && t.checkException(new RuntimeException(
//            "Tried removing from empty Queue"), this.ic3, "remove")
//        && t.checkExpect(this.ic4, out4);
//
//    return tBefore1 && tAfter1;
//  }
//
//  //tests for the method add for an ICollection
//  boolean testAdd(Tester t) {
//    initializeObjects2();
//    ICollection<Vertex> out1 = new Stack<Vertex>();
//    ICollection<Vertex> out2 = new Stack<Vertex>();
//    ICollection<Vertex> out3 = new Queue<Vertex>();
//    ICollection<Vertex> out4 = new Queue<Vertex>();
//
//    out2.add(new Vertex(0, 0));
//    out2.add(new Vertex(0, 1));
//    out2.add(new Vertex(1, 0));
//    out2.add(new Vertex(1, 1));
//
//    out4.add(new Vertex(0, 0));
//    out4.add(new Vertex(0, 1));
//    out4.add(new Vertex(1, 0));
//    out4.add(new Vertex(1, 1));
//
//    boolean tBefore1 = t.checkExpect(this.ic1, out1)
//        && t.checkExpect(this.ic2, out2)
//        && t.checkExpect(this.ic3, out3)
//        && t.checkExpect(this.ic4, out4);
//
//    out1.contents.addFirst(new Vertex(2, 2));
//    out2.contents.addFirst(new Vertex(2, 2));
//    out3.contents.addLast(new Vertex(2, 2));
//    out4.contents.addLast(new Vertex(2, 2));
//
//    this.ic1.add(new Vertex(2, 2));
//    this.ic2.add(new Vertex(2, 2));
//    this.ic3.add(new Vertex(2, 2));
//    this.ic4.add(new Vertex(2, 2));
//
//    boolean tAfter1 = t.checkExpect(this.ic1, out1)
//        && t.checkExpect(this.ic2, out2)
//        && t.checkExpect(this.ic3, out3)
//        && t.checkExpect(this.ic4, out4);
//
//    return tBefore1 && tAfter1;
//  }
//
//  //tests for the method mergeSort for an ArrayUtils
//  boolean testmergeSort(Tester t) {
//    initializeObjects();
//    ArrayList<Edge> test0 = new ArrayList<Edge>();
//    ArrayList<Edge> test1 = new ArrayList<Edge>();
//    test1.add(this.e1);
//    test1.add(this.e2);
//    test1.add(this.e3);
//    test1.add(this.e4);
//    boolean testBefore =
//        t.checkExpect(this.edgeList1, test0)
//        && t.checkExpect(this.edgeList2, test1);
//
//    this.util.mergesort(this.edgeList1, this.edgeWeight);
//    this.util.mergesort(this.edgeList2, this.edgeWeight);
//
//    ArrayList<Edge> test2 = new ArrayList<Edge>();
//    ArrayList<Edge> test3 = new ArrayList<Edge>();
//    test3.add(this.e4);
//    test3.add(this.e1);
//    test3.add(this.e2);
//    test3.add(this.e3);
//
//    boolean testAfter =
//        t.checkExpect(this.edgeList1, test2)
//        && t.checkExpect(this.edgeList2, test3);
//
//    return testBefore && testAfter;
//  }
//
//  //tests for the method mergeSortHelp for an ArrayUtils
//  boolean testmergeSortHelp(Tester t) {
//    initializeObjects();
//    ArrayList<Edge> test0 = new ArrayList<Edge>();
//    ArrayList<Edge> test1 = new ArrayList<Edge>();
//    test1.add(this.e1);
//    test1.add(this.e2);
//    test1.add(this.e3);
//    test1.add(this.e4);
//
//    boolean testBefore =
//        t.checkExpect(this.edgeList1, test0)
//        && t.checkExpect(this.edgeList2, test1);
//
//    this.util.mergesortHelp(this.edgeList1, this.edgeList3, this.edgeWeight, 0, 0);
//    this.util.mergesortHelp(this.edgeList2, this.edgeList4, this.edgeWeight, 0, 3);
//
//    ArrayList<Edge> test2 = new ArrayList<Edge>();
//    ArrayList<Edge> test3 = new ArrayList<Edge>();
//    test3.add(this.e4);
//    test3.add(this.e1);
//    test3.add(this.e2);
//    test3.add(this.e3);
//
//    boolean testAfter =
//        t.checkExpect(this.edgeList1, test2)
//        && t.checkExpect(this.edgeList2, test3);
//
//    initializeObjects();
//    this.util.mergesortHelp(this.edgeList2, this.edgeList4, this.edgeWeight, 2, 3);
//
//    ArrayList<Edge> test4 = new ArrayList<Edge>();
//    test4.add(this.e1);
//    test4.add(this.e2);
//    test4.add(this.e4);
//    test4.add(this.e3);
//
//    boolean testAfter2 = t.checkExpect(this.edgeList2, test4);
//
//    return testBefore && testAfter && testAfter2;
//  }
//
//  //tests for the method merge for an ArrayUtils
//  boolean testMerge(Tester t) {
//    initializeObjects();
//    this.util.mergesortHelp(this.edgeList2, this.edgeList4, this.edgeWeight, 0, 1);
//    this.util.mergesortHelp(this.edgeList2, this.edgeList4, this.edgeWeight, 2, 3);
//
//    ArrayList<Edge> test1 = new ArrayList<Edge>();
//    test1.add(this.e1);
//    test1.add(this.e2);
//    test1.add(this.e4);
//    test1.add(this.e3);
//
//    boolean testBefore = t.checkExpect(this.edgeList2, test1);
//
//    this.util.merge(this.edgeList2, this.edgeList4, this.edgeWeight, 0, 1, 3);
//
//    ArrayList<Edge> test2 = new ArrayList<Edge>();
//    test2.add(this.e4);
//    test2.add(this.e1);
//    test2.add(this.e2);
//    test2.add(this.e3);
//
//    boolean testAfter =
//        t.checkExpect(this.edgeList2, test2);
//
//    initializeObjects();
//    ArrayList<Edge> test3 = new ArrayList<Edge>();
//    test3.add(this.e1);
//    test3.add(this.e2);
//    test3.add(this.e3);
//    test3.add(this.e4);
//
//    boolean testBefore2 = t.checkExpect(this.edgeList2, test3);
//
//    this.util.merge(this.edgeList2, this.edgeList4, this.edgeWeight, 2, 2, 3);
//    boolean testAfter2 =
//        t.checkExpect(this.edgeList2, test1);
//
//    return testBefore && testAfter && testBefore2 && testAfter2;
//  }
//
//  //tests for the method makeStartingVertices for an ArrayUtils
//  boolean testMakeStartingVertices(Tester t) {
//    initializeObjects();
//    ArrayList<Vertex> out = new ArrayList<Vertex>();
//    out.add(new Vertex(0,0));
//    out.add(new Vertex(0,1));
//    out.add(new Vertex(1,0));
//    out.add(new Vertex(1,1));
//    return t.checkExpect(this.util.makeStartingVertices(0, 0), new ArrayList<Vertex>())
//        && t.checkExpect(this.util.makeStartingVertices(2, 2), out);
//  }
//
//  //tests for the method makeStartingEdges for an ArrayUtils
//  boolean testMakeStartingEdges(Tester t) {
//    initializeObjects();
//
//    this.e5 = new Edge(this.v13, this.v14, rand1);
//    this.e6 = new Edge(this.v13, this.v15, rand1);
//    this.e7 = new Edge(this.v14, this.v16, rand1);
//    this.e8 = new Edge(this.v15, this.v16, rand1);
//    this.e5.weight = 17;
//    this.e6.weight = 13;
//    this.e7.weight = 12;
//    this.e8.weight = 34;
//
//    this.edgeList5.add(this.e5);
//    this.edgeList5.add(this.e6);
//    this.edgeList5.add(this.e7);
//    this.edgeList5.add(this.e8);
//
//    boolean tBefore = t.checkExpect(this.vertList0.size(), 0)
//        && t.checkExpect(this.vertList2.size(), 4)
//        && t.checkExpect(this.vertList2.get(0).outEdges, new ArrayList<Edge>())
//        && t.checkExpect(this.vertList2.get(1).outEdges, new ArrayList<Edge>())
//        && t.checkExpect(this.vertList2.get(2).outEdges, new ArrayList<Edge>())
//        && t.checkExpect(this.vertList2.get(3).outEdges, new ArrayList<Edge>());
//
//    boolean tReturn =
//        t.checkExpect(this.util.makeStartingEdges(this.vertList0, 0, 0, rand1),
//            new ArrayList<Edge>())
//        && t.checkExpect(this.util.makeStartingEdges(this.vertList2, 2, 2, rand1), this.edgeList5);
//
//    ArrayList<Edge> out1 = new ArrayList<Edge>();
//    out1.add(this.e5);
//    out1.add(this.e6);
//
//    ArrayList<Edge> out2 = new ArrayList<Edge>();
//    out2.add(this.e5);
//    out2.add(this.e7);
//
//    ArrayList<Edge> out3 = new ArrayList<Edge>();
//    out3.add(this.e6);
//    out3.add(this.e8);
//
//    ArrayList<Edge> out4 = new ArrayList<Edge>();
//    out4.add(this.e7);
//    out4.add(this.e8);
//
//    boolean tAfter = t.checkExpect(this.vertList0.size(), 0)
//        && t.checkExpect(this.vertList0, new ArrayList<Vertex>())
//        && t.checkExpect(this.vertList2.size(), 4)
//        && t.checkExpect(this.vertList2.get(0).outEdges, out1)
//        && t.checkExpect(this.vertList2.get(1).outEdges, out2)
//        && t.checkExpect(this.vertList2.get(2).outEdges, out3)
//        && t.checkExpect(this.vertList2.get(3).outEdges, out4);
//    return tBefore && tReturn && tAfter;
//  }
//
//  //tests for the method edgeExists for a graph
//  boolean testEdgeExists(Tester t) {
//    initializeObjects2();
//    return t.checkExpect(this.util.edgeExists(new ArrayList<Edge>(), 0, 0, 1, 0), false)
//        && t.checkExpect(this.util.edgeExists(this.w2.graph.edgesInTree, 0, 0, 1, 0), true)
//        && t.checkExpect(this.util.edgeExists(this.w2.graph.edgesInTree, 0, 1, 1, 1), true)
//        && t.checkExpect(this.util.edgeExists(this.w2.graph.edgesInTree, 1, 0, 1, 1), true)
//        && t.checkExpect(this.util.edgeExists(this.w2.graph.edgesInTree, 0, 0, 0, 1), false)
//        && t.checkExpect(this.util.edgeExists(this.w2.graph.edgesInTree, 0, 0, 1, 1), false)
//        && t.checkExpect(this.util.edgeExists(this.w2.graph.edgesInTree, 1, 0, 0, 0), true)
//        && t.checkExpect(this.util.edgeExists(this.w2.graph.edgesInTree, 1, 1, 0, 1), true)
//        && t.checkExpect(this.util.edgeExists(this.w2.graph.edgesInTree, 1, 1, 1, 0), true)
//        && t.checkExpect(this.util.edgeExists(this.w2.graph.edgesInTree, 0, 1, 0, 0), false)
//        && t.checkExpect(this.util.edgeExists(this.w2.graph.edgesInTree, 1, 1, 0, 0), false);
//  }
//
//  //tests for the method find for a HashUtils
//  boolean testFind(Tester t) {
//    initializeObjects();
//    return t.checkExpect(this.hashUtil.find(this.map1, this.v1), this.v1)
//        && t.checkExpect(this.hashUtil.find(this.map1, this.v2), this.v1)
//        && t.checkExpect(this.hashUtil.find(this.map1, this.v4), this.v1);
//  }
//
//  //tests for the constructors of MazeWorld
//  boolean testMazeWorld(Tester t) {
//    initializeObjects2();
//    return t.checkConstructorException(new IllegalArgumentException("Given width is less than 1"),
//        "MazeWorld", new Random(), 0, 0)
//        && t.checkConstructorException(
//            new IllegalArgumentException("Given height is less than 1"),
//            "MazeWorld", new Random(), 1, 0)
//        && t.checkConstructorException(new IllegalArgumentException("Given width is less than 1"),
//            "MazeWorld", new Random(), 0, 1)
//        && t.checkConstructorException(new IllegalArgumentException("Given width is less than 1"),
//            "MazeWorld", 0, 0, 20)
//        && t.checkConstructorException(
//            new IllegalArgumentException("Given height is less than 1"),
//            "MazeWorld", 1, 0, 20)
//        && t.checkConstructorException(new IllegalArgumentException("Given width is less than 1"),
//            "MazeWorld", 0, 1, 20)
//        && t.checkConstructorException(new IllegalArgumentException("Given scale is less than 1"),
//            "MazeWorld", 1, 1, 0)
//        && t.checkConstructorException(new IllegalArgumentException("Given scale is odd"),
//            "MazeWorld", 1, 1, 9);
//  }
//
//  //tests for the method makeScene for a MazeWorld -- ADD TESTS HERE
//  boolean testMakeScene(Tester t) {
//    initializeObjects2();
//    WorldScene image1 = new WorldScene(0, 0);
//    image1.placeImageXY(new RectangleImage(20, 20, "solid", Color.LIGHT_GRAY),  10, 10);
//    image1.placeImageXY(new RectangleImage(20, 20, "solid", Color.green), 10, 10);
//    image1.placeImageXY(new RectangleImage(20, 20, "solid", Color.magenta), 20 - 10, 20 - 10);
//    image1.placeImageXY(this.w1.graph.makeWallsScene(20, 1, 1), 10, 10);
//    image1.placeImageXY(new CircleImage(10, "solid", new Color(207,85,81)), 10, 10);
//
//    WorldScene image2 = new WorldScene(0, 0);
//    image2.placeImageXY(new RectangleImage(40, 40, "solid", Color.LIGHT_GRAY), 20, 20);
//    image2.placeImageXY(new RectangleImage(20, 20, "solid", Color.green), 10, 10);
//    image2.placeImageXY(new RectangleImage(20, 20, "solid", Color.magenta), 30, 30);
//    image2.placeImageXY(this.w2.graph.makeWallsScene(20, 2, 2), 20, 20);
//    image2.placeImageXY(new CircleImage(10, "solid", new Color(207,85,81)), 10, 10);
//
//    this.w3.placesBeen.add(new Posn(0, 1));
//    this.w3.placesBeen.add(new Posn(1, 1));
//
//    this.w3.graph.solutionPath.add(new Vertex(1, 0));
//    this.w3.graph.solutionPath.add(new Vertex(1, 1));
//
//    this.w3.playerPos = new Posn(1, 1);
//
//    WorldScene image3 = new WorldScene(0, 0);
//    image3.placeImageXY(new RectangleImage(200, 200, "solid", Color.LIGHT_GRAY), 100, 100);
//
//    image3.placeImageXY(new RectangleImage(20, 20, "solid", new Color(138,192,255)), 10, 30);
//    image3.placeImageXY(new RectangleImage(20, 20, "solid", new Color(138,192,255)), 30, 30);
//
//    image3.placeImageXY(new RectangleImage(20, 20, "solid", Color.white), 30, 10);
//    image3.placeImageXY(new RectangleImage(20, 20, "solid", Color.white), 30, 30);
//
//    image3.placeImageXY(new RectangleImage(20, 20, "solid", Color.green), 10, 10);
//    image3.placeImageXY(new RectangleImage(20, 20, "solid", Color.magenta), 190, 190);
//
//    image3.placeImageXY(this.w2.graph.makeWallsScene(20, 2, 2), 20, 20);
//    image3.placeImageXY(new CircleImage(10, "solid", new Color(207,85,81)), 30, 30);
//
//    return t.checkExpect(this.w1.makeScene(), image1)
//        && t.checkExpect(this.w2.makeScene(), image2)
//        && t.checkExpect(this.w3.makeScene(), image3);
//  }
//
//  //tests for the method onTick for a MazeWorld
//  boolean testOnTick(Tester t) {
//    initializeObjects2();
//    this.w2.phase = 1;
//    this.w2.graph.searchOver = true;
//    this.w2.graph.dfs();
//
//    boolean tBefore1 = t.checkExpect(this.w2.phase, 1)
//        && t.checkExpect(this.w2.placesBeen, new ArrayList<Posn>())
//        && t.checkExpect(this.w2.playerPos, new Posn(0, 0))
//        && t.checkExpect(this.w2.endReached, false)
//        && t.checkExpect(this.w2.graph.searchOver, false);
//    this.w2.onTick();
//    this.lposn1.add(new Posn(0, 0));
//    boolean tAfter1 = t.checkExpect(this.w2.phase, 1)
//        && t.checkExpect(this.w2.placesBeen, this.lposn1)
//        && t.checkExpect(this.w2.playerPos, new Posn(0, 0))
//        && t.checkExpect(this.w2.endReached, false)
//        && t.checkExpect(this.w2.graph.searchOver, false);
//    this.w2.onTick();
//    this.lposn1.add(new Posn(1, 0));
//    boolean tAfter2 = t.checkExpect(this.w2.phase, 1)
//        && t.checkExpect(this.w2.placesBeen, this.lposn1)
//        && t.checkExpect(this.w2.playerPos, new Posn(0, 0))
//        && t.checkExpect(this.w2.endReached, false)
//        && t.checkExpect(this.w2.graph.searchOver, false);
//    this.w2.onTick();
//    boolean tAfter3 = t.checkExpect(this.w2.phase, 0)
//        && t.checkExpect(this.w2.placesBeen, this.lposn1)
//        && t.checkExpect(this.w2.playerPos, new Posn(0, 0))
//        && t.checkExpect(this.w2.endReached, false)
//        && t.checkExpect(this.w2.graph.searchOver, true);
//    this.w2.playerPos = new Posn(1, 1);
//    this.w2.onTick();
//    boolean tAfter4 = t.checkExpect(this.w2.phase, 2)
//        && t.checkExpect(this.w2.placesBeen, this.lposn1)
//        && t.checkExpect(this.w2.playerPos, new Posn(1, 1))
//        && t.checkExpect(this.w2.endReached, true)
//        && t.checkExpect(this.w2.graph.searchOver, false);
//
//    initializeObjects2();
//    this.w2.phase = 2;
//    this.w2.graph.searchOver = true;
//    this.w2.graph.dfs();
//    boolean tBefore2 = t.checkExpect(this.w2.phase, 2)
//        && t.checkExpect(this.w2.placesBeen, new ArrayList<Posn>())
//        && t.checkExpect(this.w2.playerPos, new Posn(0, 0))
//        && t.checkExpect(this.w2.endReached, false)
//        && t.checkExpect(this.w2.graph.searchOver, false);
//    this.w2.onTick();
//    this.lposn1.add(new Posn(0, 0));
//    boolean tAfter5 = t.checkExpect(this.w2.phase, 2)
//        && t.checkExpect(this.w2.placesBeen, new ArrayList<Posn>())
//        && t.checkExpect(this.w2.playerPos, new Posn(0, 0))
//        && t.checkExpect(this.w2.endReached, false)
//        && t.checkExpect(this.w2.graph.searchOver, false);
//    this.w2.onTick();
//    this.lposn1.add(new Posn(0, 0));
//    boolean tAfter6 = t.checkExpect(this.w2.phase, 2)
//        && t.checkExpect(this.w2.placesBeen, new ArrayList<Posn>())
//        && t.checkExpect(this.w2.playerPos, new Posn(0, 0))
//        && t.checkExpect(this.w2.endReached, false)
//        && t.checkExpect(this.w2.graph.searchOver, false);
//    this.w2.onTick();
//    this.lposn1.add(new Posn(0, 0));
//    boolean tAfter7 = t.checkExpect(this.w2.phase, 3)
//        && t.checkExpect(this.w2.placesBeen, new ArrayList<Posn>())
//        && t.checkExpect(this.w2.playerPos, new Posn(0, 0))
//        && t.checkExpect(this.w2.endReached, false)
//        && t.checkExpect(this.w2.graph.searchOver, true);
//
//    return tBefore1 && tBefore2 && tAfter1 && tAfter2 && tAfter3
//        && tAfter4 && tAfter5 && tAfter6 && tAfter7;
//  }
//
//  // tests for the method onKeyEvent for a MazeWorld
//  boolean testOnKeyEvent(Tester t) {
//    initializeObjects2();
//    boolean tBefore1 = t.checkExpect(this.w3.playerPos, new Posn(0, 0))
//        && t.checkExpect(this.w3.endReached, false)
//        && t.checkExpect(this.w3.placesBeen, this.lposn1)
//        && t.checkExpect(this.w3.phase, 0);
//    this.lposn1.add(new Posn(0, 0));
//    this.w3.onKeyEvent("o");
//    boolean tAfter1 = t.checkExpect(this.w3.playerPos, new Posn(0, 0))
//        && t.checkExpect(this.w3.endReached, false)
//        && t.checkExpect(this.w3.placesBeen, this.lposn1)
//        && t.checkExpect(this.w3.phase, 0);
//    this.w3.onKeyEvent("left");
//    boolean tAfter2 = t.checkExpect(this.w3.playerPos, new Posn(0, 0))
//        && t.checkExpect(this.w3.endReached, false)
//        && t.checkExpect(this.w3.placesBeen, this.lposn1)
//        && t.checkExpect(this.w3.phase, 0);
//    this.w3.onKeyEvent("up");
//    boolean tAfter3 = t.checkExpect(this.w3.playerPos, new Posn(0, 0))
//        && t.checkExpect(this.w3.endReached, false)
//        && t.checkExpect(this.w3.placesBeen, this.lposn1)
//        && t.checkExpect(this.w3.phase, 0);
//    this.w3.onKeyEvent("right");
//    this.lposn1.add(new Posn(1, 0));
//    boolean tAfter4 = t.checkExpect(this.w3.playerPos, new Posn(1, 0))
//        && t.checkExpect(this.w3.endReached, false)
//        && t.checkExpect(this.w3.placesBeen, this.lposn1)
//        && t.checkExpect(this.w3.phase, 0);
//
//    initializeObjects2();
//    this.lposn1.add(new Posn(0, 1));
//    this.w3.onKeyEvent("down");
//    boolean tAfter5 = t.checkExpect(this.w3.playerPos, new Posn(0, 1))
//        && t.checkExpect(this.w3.endReached, false)
//        && t.checkExpect(this.w3.placesBeen, this.lposn1)
//        && t.checkExpect(this.w3.phase, 0);
//    this.w3.onKeyEvent("down");
//    boolean tAfter6 = t.checkExpect(this.w3.playerPos, new Posn(0, 1))
//        && t.checkExpect(this.w3.endReached, false)
//        && t.checkExpect(this.w3.placesBeen, this.lposn1)
//        && t.checkExpect(this.w3.phase, 0);
//    this.w3.onKeyEvent("right");
//    boolean tAfter7 = t.checkExpect(this.w3.playerPos, new Posn(0, 1))
//        && t.checkExpect(this.w3.endReached, false)
//        && t.checkExpect(this.w3.placesBeen, this.lposn1)
//        && t.checkExpect(this.w3.phase, 0);
//
//    initializeObjects2();
//    this.w3.playerPos = new Posn(1, 1);
//    boolean tBefore2 = t.checkExpect(this.w3.playerPos, new Posn(1, 1))
//        && t.checkExpect(this.w3.endReached, false)
//        && t.checkExpect(this.w3.placesBeen, this.lposn1)
//        && t.checkExpect(this.w3.phase, 0);
//    this.w3.onKeyEvent("up");
//    this.lposn1.add(new Posn(1, 0));
//    boolean tAfter8 = t.checkExpect(this.w3.playerPos, new Posn(1, 0))
//        && t.checkExpect(this.w3.endReached, false)
//        && t.checkExpect(this.w3.placesBeen, this.lposn1)
//        && t.checkExpect(this.w3.phase, 0);
//    this.w3.onKeyEvent("left");
//    this.lposn1.add(new Posn(0, 0));
//    boolean tAfter9 = t.checkExpect(this.w3.playerPos, new Posn(0, 0))
//        && t.checkExpect(this.w3.endReached, false)
//        && t.checkExpect(this.w3.placesBeen, this.lposn1)
//        && t.checkExpect(this.w3.phase, 0);
//
//    initializeObjects2();
//    this.w3.endReached = true;
//    this.w3.onKeyEvent("o");
//    boolean tAfter10 = t.checkExpect(this.w3.playerPos, new Posn(0, 0))
//        && t.checkExpect(this.w3.endReached, true)
//        && t.checkExpect(this.w3.placesBeen, this.lposn1)
//        && t.checkExpect(this.w3.phase, 0);
//    this.w3.onKeyEvent("left");
//    boolean tAfter11 = t.checkExpect(this.w3.playerPos, new Posn(0, 0))
//        && t.checkExpect(this.w3.endReached, true)
//        && t.checkExpect(this.w3.placesBeen, this.lposn1)
//        && t.checkExpect(this.w3.phase, 0);
//    this.w3.onKeyEvent("up");
//    boolean tAfter12 = t.checkExpect(this.w3.playerPos, new Posn(0, 0))
//        && t.checkExpect(this.w3.endReached, true)
//        && t.checkExpect(this.w3.placesBeen, this.lposn1)
//        && t.checkExpect(this.w3.phase, 0);
//    this.w3.onKeyEvent("right");
//    boolean tAfter13 = t.checkExpect(this.w3.playerPos, new Posn(0, 0))
//        && t.checkExpect(this.w3.endReached, true)
//        && t.checkExpect(this.w3.placesBeen, this.lposn1)
//        && t.checkExpect(this.w3.phase, 0);
//    initializeObjects2();
//    this.w3.endReached = true;
//    this.w3.onKeyEvent("down");
//    boolean tAfter14 = t.checkExpect(this.w3.playerPos, new Posn(0, 0))
//        && t.checkExpect(this.w3.endReached, true)
//        && t.checkExpect(this.w3.placesBeen, this.lposn1)
//        && t.checkExpect(this.w3.phase, 0);
//    this.w3.onKeyEvent("down");
//    boolean tAfter15 = t.checkExpect(this.w3.playerPos, new Posn(0, 0))
//        && t.checkExpect(this.w3.endReached, true)
//        && t.checkExpect(this.w3.placesBeen, this.lposn1)
//        && t.checkExpect(this.w3.phase, 0);
//    this.w3.onKeyEvent("right");
//    boolean tAfter16 = t.checkExpect(this.w3.playerPos, new Posn(0, 0))
//        && t.checkExpect(this.w3.endReached, true)
//        && t.checkExpect(this.w3.placesBeen, this.lposn1)
//        && t.checkExpect(this.w3.phase, 0);
//    initializeObjects2();
//    this.w3.endReached = true;
//    this.w3.playerPos = new Posn(1, 1);
//    this.w3.onKeyEvent("up");
//    boolean tAfter17 = t.checkExpect(this.w3.playerPos, new Posn(1, 1))
//        && t.checkExpect(this.w3.endReached, true)
//        && t.checkExpect(this.w3.placesBeen, this.lposn1)
//        && t.checkExpect(this.w3.phase, 0);
//    this.w3.onKeyEvent("left");
//    boolean tAfter18 = t.checkExpect(this.w3.playerPos, new Posn(1, 1))
//        && t.checkExpect(this.w3.endReached, true)
//        && t.checkExpect(this.w3.placesBeen, this.lposn1)
//        && t.checkExpect(this.w3.phase, 0);
//
//    initializeObjects2();
//    this.w3.playerPos = new Posn(8, 9);
//    boolean tBefore3 = t.checkExpect(this.w3.playerPos, new Posn(8, 9))
//        && t.checkExpect(this.w3.endReached, false)
//        && t.checkExpect(this.w3.placesBeen, this.lposn1)
//        && t.checkExpect(this.w3.phase, 0);
//    this.w3.onKeyEvent("right");
//    this.lposn1.add(new Posn(9, 9));
//    boolean tAfter19 = t.checkExpect(this.w3.playerPos, new Posn(9, 9))
//        && t.checkExpect(this.w3.endReached, false)
//        && t.checkExpect(this.w3.placesBeen, this.lposn1)
//        && t.checkExpect(this.w3.phase, 0);
//    this.w3.endReached = true;
//    this.w3.onKeyEvent("left");
//    boolean tAfter20 = t.checkExpect(this.w3.playerPos, new Posn(9, 9))
//        && t.checkExpect(this.w3.endReached, true)
//        && t.checkExpect(this.w3.placesBeen, this.lposn1)
//        && t.checkExpect(this.w3.phase, 0);
//
//    initializeObjects2();
//    this.w3.playerPos = new Posn(1, 1);
//    this.w3.placesBeen.add(new Posn(1, 1));
//    this.w3.graph.searchOver = true;
//    this.lposn1.add(new Posn(1, 1));
//    boolean tBefore4 = t.checkExpect(this.w3.playerPos, new Posn(1, 1))
//        && t.checkExpect(this.w3.endReached, false)
//        && t.checkExpect(this.w3.placesBeen, this.lposn1)
//        && t.checkExpect(this.w3.graph.searchOver, true)
//        && t.checkExpect(this.w3.phase, 0);
//    this.w3.onKeyEvent("b");
//    ArrayList<Posn> out = new ArrayList<Posn>();
//    boolean tAfter21 = t.checkExpect(this.w3.playerPos, new Posn(1, 1))
//        && t.checkExpect(this.w3.endReached, false)
//        && t.checkExpect(this.w3.placesBeen, out)
//        && t.checkExpect(this.w3.graph.searchOver, false)
//        && t.checkExpect(this.w3.phase, 1);
//
//    initializeObjects2();
//    this.w3.playerPos = new Posn(1, 1);
//    this.w3.placesBeen.add(new Posn(1, 1));
//    this.w3.graph.searchOver = true;
//    this.lposn1.add(new Posn(1, 1));
//    boolean tBefore5 = t.checkExpect(this.w3.playerPos, new Posn(1, 1))
//        && t.checkExpect(this.w3.endReached, false)
//        && t.checkExpect(this.w3.placesBeen, this.lposn1)
//        && t.checkExpect(this.w3.graph.searchOver, true)
//        && t.checkExpect(this.w3.phase, 0);
//    this.w3.onKeyEvent("d");
//    boolean tAfter22 = t.checkExpect(this.w3.playerPos, new Posn(1, 1))
//        && t.checkExpect(this.w3.endReached, false)
//        && t.checkExpect(this.w3.placesBeen, out)
//        && t.checkExpect(this.w3.graph.searchOver, false)
//        && t.checkExpect(this.w3.phase, 1);
//
//    initializeObjects2();
//    this.w3.playerPos = new Posn(1, 1);
//    this.w3.placesBeen.add(new Posn(1, 1));
//    this.lposn1.add(new Posn(1, 1));
//    this.w3.endReached = true;
//    this.w3.mazeWallsImage = new EmptyImage();
//    this.w3.graph.searchOver = true;
//    this.w3.phase = 1;
//    boolean tBefore6 = t.checkExpect(this.w3.playerPos, new Posn(1, 1))
//        && t.checkExpect(this.w3.endReached, true)
//        && t.checkExpect(this.w3.placesBeen, this.lposn1)
//        && t.checkExpect(this.w3.graph.searchOver, true)
//        && t.checkExpect(this.w3.phase, 1)
//        && t.checkExpect(this.w3.mazeWallsImage, new EmptyImage());
//    this.w3.onKeyEvent("r");
//    boolean tAfter23 = t.checkExpect(this.w3.playerPos, new Posn(0, 0))
//        && t.checkExpect(this.w3.endReached, false)
//        && t.checkExpect(this.w3.placesBeen, new ArrayList<Posn>())
//        && t.checkExpect(this.w3.graph.searchOver, false)
//        && t.checkExpect(this.w3.phase, 0)
//        && t.checkExpect(this.w3.mazeWallsImage, this.w3.graph.makeWallsScene(20, 10, 10));
//
//
//    return tBefore1 && tBefore2 && tBefore3 && tBefore4 && tBefore5
//        && tBefore6 && tAfter1 && tAfter2 && tAfter3 && tAfter4
//        && tAfter5 && tAfter6 && tAfter7 && tAfter8 && tAfter9 && tAfter10
//        && tAfter11 && tAfter12 && tAfter13 && tAfter14 && tAfter15 && tAfter16
//        && tAfter17 && tAfter18 && tAfter19 && tAfter20 && tAfter21 && tAfter22 && tAfter23;
//  }

}

