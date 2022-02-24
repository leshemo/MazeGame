//import java.awt.Color;
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Random;
//import javalib.impworld.World;
//import javalib.impworld.WorldScene;
//import javalib.worldimages.*;
//import tester.Tester;
//
//// Represents a vertex of a graph
//class Vertex {
//  String name;   // x1y1, x21y3
//  int x;
//  int y;
//  ArrayList<Edge> outEdges;
//
//  Vertex(int x, int y) {
//    if (x < 0) {
//      throw new IllegalArgumentException("Given X is less than 0");
//    }
//    if (y < 0) {
//      throw new IllegalArgumentException("Given Y is less than 0");
//    }
//
//    this.x = x;
//    this.y = y;
//    this.name = "x" + String.valueOf(x) + "y" + String.valueOf(y);
//    this.outEdges = new ArrayList<Edge>();
//  }
//
//  // add the given edge to this vertex's outEdges 
//  void connect(Edge e) {
//    if (!this.outEdges.contains(e)) {
//      this.outEdges.add(e);
//    }
//  }
//
//  @Override
//  // Is this Vertex equal to the given object
//  public boolean equals(Object other) {
//    if (!(other instanceof Vertex)) { 
//      return false; 
//    }
//    Vertex that = (Vertex)other;
//    return this.name.equals(that.name);
//  }
//
//  @Override
//  // defines the hashCode for a Vertex
//  public int hashCode() {
//    return this.name.hashCode();
//  }
//
//  WorldImage makeEdgeSceneVertex(int scale, WorldImage currentImage, Vertex to, Color color) {
//    if (this.x == to.x) {
//      //horizontal wall
//      return new OverlayOffsetAlign(AlignModeX.LEFT, AlignModeY.TOP, 
//          currentImage, 
//          this.x * scale, to.y * scale,  new LineImage(new Posn(scale, 0), color));
//    }
//    if (this.y == to.y) {
//      //vertical wall
//      return new OverlayOffsetAlign(AlignModeX.LEFT, AlignModeY.TOP, 
//          currentImage, 
//          to.x * scale, this.y * scale,  new LineImage(new Posn(0, scale), color));
//    }
//    return currentImage;
//  }
//}
//
//// Represents edges between graphs
//class Edge {
//  Vertex from;
//  Vertex to;
//  int weight;
//  boolean wall; // is this edge a wall?
//
//  Edge(Vertex from, Vertex to, Random rand) {
//    this.weight = rand.nextInt(50); // Randomize weight to form random tree
//    this.wall = true;
//    this.from = from;
//    this.to = to;
//    this.from.connect(this);
//    this.to.connect(this);
//  }
//
//  // makes this edge into a path, not a wall
//  public void makePath() {
//    this.wall = false;
//  }
//
//  //creates a WorldImage for this edge, given the scale and the current WorldImage
//  WorldImage makeEdgeScene(int scale, WorldImage currentImage) {
//    if (wall) {
//      return this.from.makeEdgeSceneVertex(scale, currentImage, this.to, Color.DARK_GRAY);
//    } else {
//      return this.from.makeEdgeSceneVertex(scale, currentImage, this.to, Color.LIGHT_GRAY);
//    }
//  }
//}
//
//// Represents a graph
//class Graph {
//  ArrayList<Vertex> vertices;
//  ArrayList<Edge> allEdges;
//  List<Edge> edgesInTree;
//  int width; 
//  int height;
//
//  Graph(Random rand, int width, int height) {
//    if (width < 0) {
//      throw new IllegalArgumentException("Given width is less than 0");
//    }
//    if (height < 0) {
//      throw new IllegalArgumentException("Given height is less than 0");
//    }
//    this.width = width;
//    this.height = height;
//    this.vertices = new ArrayUtils().makeStartingVertices(this.width, this.height);
//    this.allEdges = new ArrayUtils().
//        makeStartingEdges(this.vertices, this.width, this.height, rand);
//    this.edgesInTree = new ArrayList<Edge>();
//  }
//
//
//  // Create the worldImage of this graph
//  WorldImage makeGraphScene(int scale, int width, int height) {
//    WorldImage image = 
//        new RectangleImage(width * scale, height * scale, OutlineMode.OUTLINE, Color.black);
//    for (Edge e: this.allEdges) {
//      //image.placeImage();
//      image = e.makeEdgeScene(scale, image);
//    }
//    return image;
//  }
//
//  // Uses Kruskal's algorithm to form the list of edges that are walls in a proper maze
//  void makeMaze() {
//    Comparator<Edge> edgeWeight = (Edge x, Edge y) -> x.weight - y.weight;
//    int verticesSize = this.vertices.size();
//    HashMap<Vertex, Vertex> representatives = new HashMap<Vertex, Vertex>();
//    ArrayList<Edge> worklist = new ArrayList<Edge>(this.allEdges); 
//    this.edgesInTree.clear();
//    new ArrayUtils().mergesort(worklist, edgeWeight);
//    int numEdges = 0;
//
//    for (int i = 0; i < verticesSize; i++) {
//      representatives.put(this.vertices.get(i), this.vertices.get(i));
//    }
//
//    while (numEdges < verticesSize - 1) {
//      Edge edgeTemp = worklist.remove(0);
//      Vertex fromRep = new HashUtils().find(representatives, edgeTemp.from);
//      Vertex toRep = new HashUtils().find(representatives, edgeTemp.to);
//
//      if (!fromRep.equals(toRep)) {
//        this.edgesInTree.add(edgeTemp);
//        representatives.replace(fromRep, toRep);
//        numEdges += 1;
//      }
//
//    }
//  }
//
//  // Changes the edges in the created maze to not be walls
//  public void createPaths() {
//    for (Edge e: this.edgesInTree) {
//      e.makePath();
//    }
//  }
//}
//
////Class used for methods to act upon ArrayLists
//class ArrayUtils {
//
//  // Sorts the provided list according to the given comparator
//  <T> void mergesort(ArrayList<T> list, Comparator<T> comp) {
//    ArrayList<T> temp = new ArrayList<T>(list);
//    mergesortHelp(list, temp, comp, 0, list.size()-1);
//  }
//
//  // Helps sort the provided list according to the given comparator and bounds given
//  <T> void mergesortHelp(ArrayList<T> list, ArrayList<T> temp, 
//      Comparator<T> comp, int low, int high) {
//    if (low < high) {
//      int mid = (low + high) / 2;
//
//      mergesortHelp(list, temp, comp, low, mid);
//      mergesortHelp(list, temp, comp, mid + 1, high);
//      merge(list, temp, comp, low, mid, high);
//    }
//  }
//
//  // merges the sorted sublists in the given list defined by the bounds based on the comparator
//  <T> void merge(ArrayList<T> list, ArrayList<T> temp, 
//      Comparator<T> comp, int low, int mid, int high) {
//    int i = low;
//    int j = mid + 1;
//    int k = low;
//
//    while((i <= mid) && (j<= high)) {
//      int r = comp.compare(list.get(i), list.get(j));
//      if (r <= 0) { // i<=j
//        temp.set(k, list.get(i));
//        i += 1;
//      } else {
//        temp.set(k, list.get(j));
//        j += 1;
//      }
//      k += 1;
//    }
//
//    while(i <= mid) {
//      temp.set(k, list.get(i));
//      i += 1;
//      k += 1; 
//    }
//
//    while(j <= mid) {
//      temp.set(k, list.get(j));
//      j += 1;
//      k += 1; 
//    }
//
//    for (k = low; k <= high; k++) {
//      list.set(k, temp.get(k));
//    }
//  }
//
//  //creates an ArrayList<Vertex> of the board with no edges
//  ArrayList<Vertex> makeStartingVertices(int width, int height) {
//    ArrayList<Vertex> vertexList = new ArrayList<Vertex>();
//    for (int i = 0; i < width; i++) {
//      for (int j = 0; j < height; j++) {
//        vertexList.add(new Vertex(i, j));
//      }
//    }
//    return vertexList;
//  }
//
//  //creates an ArrayList<Edge> of all the board's edges
//  ArrayList<Edge> makeStartingEdges(ArrayList<Vertex> allVertices, 
//      int width, int height, Random rand) {
//    ArrayList<Edge> edgeList = new ArrayList<Edge>();
//
//    for (int x = 0; x < width; x++) {
//      for (int y = 0; y < height; y++) {
//        if (y < height - 1) {
//          edgeList.add(new Edge(allVertices.get(y + (x * height)), 
//              allVertices.get(y + (x * height) + 1), rand));
//        } 
//        if (x < width - 1) {
//          edgeList.add(new Edge(allVertices.get(y + (x * height)), 
//              allVertices.get(y + (x * height) + height), rand));
//        }
//      }
//    }
//    return edgeList;
//  }
//}
//
////class used for methods that act upon hashMaps
//class HashUtils {
//
//  // Finds the top level representative of the given Vertex
//  Vertex find(HashMap<Vertex, Vertex> reps, Vertex name) {
//    Vertex vert = name;
//    Vertex mappedTo = reps.get(vert);
//    while (!mappedTo.equals(vert)) {
//      vert = mappedTo;
//      mappedTo = reps.get(vert);
//    }
//    return vert;
//  }
//}
//
//
////represents the world class, MazeWorld
//class MazeWorld extends World {
//  int width;
//  int height;
//  int scale;
//  Graph graph;
//
//  // Random Game Constructor
//  MazeWorld(int width, int height, int scale) {
//    if (width < 1) {
//      throw new IllegalArgumentException("Given width is less than 1");
//    }
//    if (height < 1) {
//      throw new IllegalArgumentException("Given height is less than 1");
//    }
//    if (scale < 1) {
//      throw new IllegalArgumentException("Given scale is less than 1");
//    }    
//    if (scale % 2 != 0) {
//      throw new IllegalArgumentException("Given scale is odd");
//    }
//    this.width = width;
//    this.height = height;
//    this.graph = new Graph(new Random(), this.width, this.height);
//    this.graph.makeMaze();
//    this.graph.createPaths();
//    this.scale = scale; 
//  }
//
//  // Constructor for testing
//  MazeWorld(Random rand, int width, int height) {
//    if (width < 1) {
//      throw new IllegalArgumentException("Given width is less than 1");
//    }
//    if (height < 1) {
//      throw new IllegalArgumentException("Given height is less than 1");
//    }
//    this.width = width;
//    this.height = height;
//    this.graph = new Graph(rand, this.width, this.height);
//    this.graph.makeMaze();
//    this.graph.createPaths();
//    this.scale = 20; 
//  }
//
//  @Override
//  // produces the world scene
//  public WorldScene makeScene() {
//    WorldScene image =  this.getEmptyScene();
//    // Background
//    image.placeImageXY(new RectangleImage(this.width * this.scale, this.height * this.scale,
//        "solid", Color.LIGHT_GRAY), this.width * this.scale / 2, this.height * this.scale / 2);
//    // Start
//    image.placeImageXY(new RectangleImage(this.scale, this.scale,"solid", Color.green), 
//        this.scale / 2, this.scale / 2);
//    // End
//    image.placeImageXY(new RectangleImage(this.scale, this.scale,"solid", Color.magenta), 
//        this.width * this.scale - this.scale / 2, this.height * this.scale - this.scale / 2);
//    // Walls
//    image.placeImageXY(this.graph.makeGraphScene(this.scale, this.width, this.height), 
//        this.width * this.scale / 2, this.height * this.scale / 2);
//
//    return image;
//  }
//}
//
//
//class ExamplesMaze {
//
//  Comparator<Edge> edgeWeight = (Edge x, Edge y) -> x.weight - y.weight;
//
//  Object int1;
//  Object objectv1;
//  Object objectv2;
//
//  Random rand1 = new Random(1);
//  ArrayUtils util = new ArrayUtils();
//  HashUtils hashUtil = new HashUtils();
//
//  Vertex v1;
//  Vertex v2;
//  Vertex v3;
//  Vertex v4;
//
//  Vertex v5;
//  Vertex v6;
//  Vertex v7;
//  Vertex v8;
//
//  Vertex v9;
//  Vertex v10;
//  Vertex v11;
//  Vertex v12;
//
//  Vertex v13;
//  Vertex v14;
//  Vertex v15;
//  Vertex v16;
//
//  ArrayList<Vertex> vertList0;
//  ArrayList<Vertex> vertList1;
//  ArrayList<Vertex> vertList2;
//
//  Edge e1;
//  Edge e2;
//  Edge e3;
//  Edge e4;
//
//  Edge e5;
//  Edge e6;
//  Edge e7;
//  Edge e8;
//
//  ArrayList<Edge> edgeList1;
//  ArrayList<Edge> edgeList2;
//  ArrayList<Edge> edgeList3;
//  ArrayList<Edge> edgeList4;
//  ArrayList<Edge> edgeList5;
//  ArrayList<Edge> edgeList6;
//
//  Graph g0 = new Graph(rand1, 2, 2);
//  Graph g1 = new Graph(rand1, 2, 2);
//  Graph g2;
//
//  HashMap<Vertex, Vertex> map1;
//
//  MazeWorld w1;
//  MazeWorld w2;
//
//  void initializeObjects() {    
//    this.rand1 = new Random(1);
//
//    this.int1 = 1;
//    this.objectv1 = new Vertex(0,0);
//    this.objectv2 = new Vertex(1,0);
//
//    this.v1 = new Vertex(0,0);
//    this.v2 = new Vertex(1,0);
//    this.v3 = new Vertex(0,1);
//    this.v4 = new Vertex(1,1);
//
//    this.v5 = new Vertex(0,0);
//    this.v6 = new Vertex(0,1);
//    this.v7 = new Vertex(1,0);
//    this.v8 = new Vertex(1,1);
//
//    this.v9 = new Vertex(0,0);
//    this.v10 = new Vertex(0,1);
//    this.v11 = new Vertex(1,0);
//    this.v12 = new Vertex(1,1);
//
//    this.v13 = new Vertex(0,0);
//    this.v14 = new Vertex(0,1);
//    this.v15 = new Vertex(1,0);
//    this.v16 = new Vertex(1,1);
//
//    this.vertList0 = new ArrayList<Vertex>();
//    this.vertList1 = new ArrayList<Vertex>();
//    this.vertList2 = new ArrayList<Vertex>();
//
//    this.vertList1.add(this.v1);
//    this.vertList1.add(this.v3);
//    this.vertList1.add(this.v2);
//    this.vertList1.add(this.v4);
//
//    this.vertList2.add(this.v9);
//    this.vertList2.add(this.v10);
//    this.vertList2.add(this.v11);
//    this.vertList2.add(this.v12);
//
//    this.e1 = new Edge(this.v1, this.v2, rand1);
//    this.e2 = new Edge(this.v1, this.v3, rand1);
//    this.e3 = new Edge(this.v2, this.v4, rand1);
//    this.e4 = new Edge(this.v3, this.v4, rand1);
//
//    this.edgeList1 = new ArrayList<Edge>();
//    this.edgeList2 = new ArrayList<Edge>();
//    this.edgeList3 = new ArrayList<Edge>();
//    this.edgeList4 = new ArrayList<Edge>();
//    this.edgeList5 = new ArrayList<Edge>();
//    this.edgeList6 = new ArrayList<Edge>();
//
//    this.edgeList2.add(this.e1);
//    this.edgeList2.add(this.e2);
//    this.edgeList2.add(this.e3);
//    this.edgeList2.add(this.e4);
//
//    this.edgeList4.add(this.e1);
//    this.edgeList4.add(this.e2);
//    this.edgeList4.add(this.e3);
//    this.edgeList4.add(this.e4);
//
//    this.edgeList6.add(this.e2);
//    this.edgeList6.add(this.e3);
//
//    this.g0 = new Graph(rand1, 0, 0);
//    this.g1 = new Graph(rand1, 2, 2);
//
//    this.map1 = new HashMap<Vertex, Vertex>();
//    this.map1.put(this.v1, this.v1);
//    this.map1.put(this.v2, this.v1);
//    this.map1.put(this.v3, this.v2);
//    this.map1.put(this.v4, this.v3);
//  }
//
//  void initializeObjects2() { 
//    initializeObjects();
//    this.g2 = new Graph(rand1, 8, 3);
//
//    this.w1 = new MazeWorld(rand1, 1, 1);
//    this.w2 = new MazeWorld(rand1, 2, 2);
//  }
//
//  //runs the maze game
//  void testGame(Tester t) {
//    int width = 20;
//    int height = 25;
//    //int scale = (150 / ((width+height) / 4))*2 ;
//    int scale = (150 / ((width+height) / 5)) *2;
//    MazeWorld m = new MazeWorld(width, height, scale);
//    m.bigBang(width * scale, height * scale, .000001);
//  }
//
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
//        1 * 20, 0,  new LineImage(new Posn(0, 20), Color.DARK_GRAY));
//    WorldImage out2 = new OverlayOffsetAlign(AlignModeX.LEFT, AlignModeY.TOP, 
//        rec, 
//        0, 20,  new LineImage(new Posn(20, 0), Color.DARK_GRAY));
//
//    return t.checkExpect(this.g1.allEdges.get(1).from.makeEdgeSceneVertex(20, rec, 
//        this.g1.allEdges.get(1).to, Color.DARK_GRAY), out1)
//        && t.checkExpect(this.g1.allEdges.get(0).from.makeEdgeSceneVertex(20, rec, 
//            this.g1.allEdges.get(0).to, Color.DARK_GRAY), out2);
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
//            20, rec, this.g1.allEdges.get(0).to, Color.DARK_GRAY))
//        && t.checkExpect(
//            this.g1.allEdges.get(1).makeEdgeScene(20, rec), 
//            this.g1.allEdges.get(1).from.makeEdgeSceneVertex(
//                20, rec, this.g1.allEdges.get(1).to, Color.DARK_GRAY));
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
//    return t.checkConstructorException(new IllegalArgumentException("Given width is less than 0"),
//        "Graph", new Random(), -1, -1)
//        && t.checkConstructorException(new IllegalArgumentException("Given height is less than 0"),
//            "Graph", new Random(), 0, -1)
//        && t.checkConstructorException(new IllegalArgumentException("Given width is less than 0"),
//            "Graph", new Random(), -1, 0);
//  }
//
//  //tests for the method makeGraphScene for a Graph
//  boolean testmakeGraphScene(Tester t) {
//    initializeObjects();
//    WorldImage image1 = new RectangleImage(0 * 10, 0 * 10, OutlineMode.OUTLINE, Color.black);
//
//    this.g1.allEdges.get(2).wall = false;
//    WorldImage image2 = new RectangleImage(2 * 20, 2 * 20, OutlineMode.OUTLINE, Color.black);
//    image2 = this.g1.allEdges.get(0).makeEdgeScene(20, image2);
//    image2 = this.g1.allEdges.get(1).makeEdgeScene(20, image2);
//    image2 = this.g1.allEdges.get(2).makeEdgeScene(20, image2);
//    image2 = this.g1.allEdges.get(3).makeEdgeScene(20, image2);
//
//    return t.checkExpect(this.g0.makeGraphScene(10, 0, 0), image1)
//        && t.checkExpect(this.g1.makeGraphScene(20, 2, 2), image2);
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
//  //tests for the method makeScene for a MazeWorld
//  boolean testMakeScene(Tester t) {
//    initializeObjects2();
//    WorldScene image1 = new WorldScene(0, 0);
//    image1.placeImageXY(new RectangleImage(20, 20, "solid", Color.LIGHT_GRAY),  10, 10);
//    image1.placeImageXY(new RectangleImage(20, 20,"solid", Color.green), 10, 10);
//    image1.placeImageXY(new RectangleImage(20, 20,"solid", Color.magenta), 20 - 10, 20 - 10);
//    image1.placeImageXY(this.w1.graph.makeGraphScene(20, 1, 1), 10, 10);
//
//    WorldScene image2 = new WorldScene(0, 0);
//    image2.placeImageXY(new RectangleImage(40, 40,"solid", Color.LIGHT_GRAY), 20, 20);
//    image2.placeImageXY(new RectangleImage(20, 20,"solid", Color.green), 10, 10);
//    image2.placeImageXY(new RectangleImage(20, 20,"solid", Color.magenta), 30, 30);
//    image2.placeImageXY(this.w2.graph.makeGraphScene(20, 2, 2), 20, 20);
//
//    return t.checkExpect(this.w1.makeScene(), image1)
//        && t.checkExpect(this.w2.makeScene(), image2);
//  }
//
//
//}
//
