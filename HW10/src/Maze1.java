//import java.awt.Color;
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Random;
//
//import javalib.impworld.World;
//import javalib.impworld.WorldScene;
//import javalib.worldimages.RectangleImage;
//import javalib.worldimages.WorldImage;
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
//  void mapName(HashMap<String, String> hash, Vertex v) {
//    hash.put(this.name, v.name);
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
//    this.from.outEdges.add(this);
//    this.to.outEdges.add(this);
//  }
//}
//
//// Represents a graph
//class Graph {
//  ArrayList<Vertex> vertices;
//  ArrayList<Edge> allEdges;
//  int width; 
//  int height;
//
//  Graph(ArrayList<Vertex> verts) {
//    this.vertices = verts;
//  }
//  
//  Graph(Random rand, int width, int height) {
//    this.width = width;
//    this.height = height;
//    this.vertices = new ArrayUtils().makeStartingVertices(this.width, this.height);
//    this.allEdges = new ArrayUtils().makeStartingEdges(this.vertices, this.width, this.height, rand);
//  }
// 
//
//  // Create the world Image of this graph
//  WorldImage makeGraphScene(int scale) {
//    // **************************************
//    return null;
//  }
//  
//  // Uses Kruskal's algorithm to form the list of edges that are 
//  // connected to form a proper maze
//  List<Edge> makeMaze() {
//    Comparator<Edge> edgeWeight = (Edge x, Edge y) -> x.weight - y.weight;
//    int verticesSize = this.vertices.size();
//    HashMap<String, String> representatives = new HashMap<String, String>();
//    List<Edge> edgesInTree = new ArrayList<Edge>();
//    new ArrayUtils().mergesort(this.allEdges, edgeWeight); 
//    List<Edge> worklist = this.allEdges; //= all edges in graph, sorted by edge weights;
//    
//    
//    for (int i = 0; i < verticesSize; i++) {
//      this.vertices.get(i).mapName(representatives, this.vertices.get(i));
//    }
//    
//
//    while (edgesInTree.size() < verticesSize) {
//      Edge edgeTemp = worklist.remove(0);
//      
//      if (find(representatives, edgeTemp.from.name).equals(find(representatives, edgeTemp.to.name))) {
//        
//      }
//
//      
//      //      Pick the next cheapest edge of the graph: suppose it connects X and Y.
//      
////    If find(representatives, X) equals find(representatives, Y):
////      discard this edge  // they're already connected
////    Else:
////      Record this edge in edgesInTree
////      union(representatives,
////            find(representatives, X),
////            find(representatives, Y))
//    }
//    
//    return edgesInTree;
//  }
//}
//
//
//class ArrayUtils {
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
//     int i = low;
//     int j = mid + 1;
//     int k = low;
//     
//     while((i <= mid) && (j<= high)) {
//       int r = comp.compare(list.get(i), list.get(j));
//       if (r <= 0) { // i<=j
//         temp.set(k, list.get(i));
//         i += 1;
//       } else {
//         temp.set(k, list.get(j));
//         j += 1;
//       }
//       k += 1;
//     }
//     
//     while(i <= mid) {
//       temp.set(k, list.get(i));
//       i += 1;
//       k += 1; 
//     }
//     
//     while(j <= mid) {
//       temp.set(k, list.get(j));
//       j += 1;
//       k += 1; 
//     }
//     
//     for (k = low; k <= high; k++) {
//       list.set(k, temp.get(k));
//     }
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
//  //creates an ArrayList<Vertex> of the board with no edges
//  ArrayList<Edge> makeStartingEdges(ArrayList<Vertex> allVertices, int width, int height, Random rand) {
//    ArrayList<Edge> edgeList = new ArrayList<Edge>();
//
//    for (int i = 0; i < width; i++) {
//      for (int j = 0; j < height; j++) {
//        if (j < height-1) {
//          edgeList.add(new Edge(allVertices.get(i + (j * width)), 
//              allVertices.get(i + (j * width) + width), rand));
//        } 
//        if (i < width - 1) {
//          edgeList.add(new Edge(allVertices.get(i + (j * width)), 
//              allVertices.get(i + (j * width) + 1), rand));
//        }
//      }
//    }
//    return edgeList;
//  }
//
//}
//
//class HashUtils {
//  
//  
//}
//
//
//
//
////represents the world class, MazeWorld
//class MazeWorld extends World {
//  int width;
//  int height;
//  int scale;
//  Graph graph;
//  
//  MazeWorld(int width, int height) {
//    this.width = width;
//    this.height = height;
//    this.graph = new Graph(new Random());
//    this.scale = 20; // must be even
//  }
//  
//  @Override
//  // produces the world scene
//  public WorldScene makeScene() {
//    WorldScene image =  this.getEmptyScene();
//    image.placeImageXY(new RectangleImage(this.width * this.scale, this.height * this.scale,
//        "solid", Color.LIGHT_GRAY), this.width * this.scale, this.height * this.scale);
//    image.placeImageXY(this.graph.makeGraphScene(this.scale), 
//        this.width * this.scale / 2, this.height * this.scale / 2);
//    
//    return image;
//  }
//  
//  
//}
//
//
//
//
//class ExamplesMaze {
//  
//  Comparator<Edge> edgeWeight = (Edge x, Edge y) -> x.weight - y.weight;
//  
//  Random rand1;
//  ArrayUtils util = new ArrayUtils();
//  
//  Vertex v1;
//  Vertex v2;
//  Vertex v3;
//  Vertex v4;
//  
//  Edge e1;
//  Edge e2;
//  Edge e3;
//  Edge e4;
//  
//  ArrayList<Edge> edgeList1;
//  ArrayList<Edge> edgeList2;
//  ArrayList<Edge> edgeList3;
//  ArrayList<Edge> edgeList4;
//  
//  
//  
//  void initializeObjects() {    
//    this.rand1 = new Random(1);
//    
//    this.v1 = new Vertex(0,0);
//    this.v2 = new Vertex(1,0);
//    this.v3 = new Vertex(0,1);
//    this.v4 = new Vertex(1,1);
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
//  }
//  
//
//  //tests for the constructors of Vertex
//  boolean testVertex(Tester t) {
//    return t.checkConstructorException(new IllegalArgumentException("Given X is less than 0"),
//            "Vertex", -1, 0)
//        && t.checkConstructorException(new IllegalArgumentException("Given Y is less than 0"),
//            "Vertex", 0, -1)
//        && t.checkConstructorException(new IllegalArgumentException("Given X is less than 0"),
//            "Vertex", -1, -1);
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
//    
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
//}
//
