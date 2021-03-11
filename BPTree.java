import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * Implementation of a B+ tree to allow efficient access to many different indexes of a large data
 * set. BPTree objects are created for each type of index needed by the program. BPTrees provide an
 * efficient range search as compared to other types of data structures due to the ability to
 * perform log_m N lookups and linear in-order traversals of the data items.
 * 
 * @author sapan (sapan@cs.wisc.edu)
 *
 * @param <K> key - expect a string that is the type of id for each item
 * @param <V> value - expect a user-defined type that stores all data for a food item
 */
public class BPTree<K extends Comparable<K>, V> implements BPTreeADT<K, V> {

  // Root of the tree
  private Node root;

  // Branching factor is the number of children nodes
  // for internal nodes of the tree
  private int branchingFactor;

  // keeps count of the size of tree
  private int size = 0;

  // stores all the leaf keys
  private Object[][] kV = new Object[99999][2];

  /**
   * Public constructor
   * 
   * @param branchingFactor
   */
  public BPTree(int branchingFactor) {
    if (branchingFactor <= 2) {
      throw new IllegalArgumentException("Illegal branching factor: " + branchingFactor);
    }
    this.branchingFactor = branchingFactor;
    this.root = new LeafNode();
  }


  /*
   * (non-Javadoc)
   * 
   * @see BPTreeADT#insert(java.lang.Object, java.lang.Object)
   */
  @Override
  public void insert(K key, V value) {
    // if the key is null, throw an illegal argument exception
    if (key == null) {
      throw new IllegalArgumentException();
    } else {

      try {
        root.insert(key, value);
        kV[size][0] = key;
        kV[size++][1] = value;
      } catch (IndexOutOfBoundsException e) {
        System.out.println("out of bounds exception caught");
      }

    }

  }


  /*
   * (non-Javadoc)
   * 
   * @see BPTreeADT#rangeSearch(java.lang.Object, java.lang.String)
   */
  @Override
  public List<V> rangeSearch(K key, String comparator) {
    if (!comparator.contentEquals(">=") && !comparator.contentEquals("==")
        && !comparator.contentEquals("<="))
      return new ArrayList<V>();
    List<V> rangeVals = root.rangeSearch(key, comparator);
    return rangeVals;
  }

  /*
   * (non-Javadoc)
   * 
   * @see BPTreeADT#get(java.lang.Object)
   */
  @Override
  public V get(K key) {
    // return null if key is null
    if (key == null) {
      return null;
    } else {
      for (int i = 0; i < size(); i++) {
        if (key.equals(kV[i][0])) {
          return (V) kV[i][1];
        }
      }
    }
    // if key not found, return null
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see BPTreeADT#size()
   */
  @Override
  public int size() {
    return this.size;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @SuppressWarnings("unchecked")
  @Override
  public String toString() {
    Queue<List<Node>> queue = new LinkedList<List<Node>>();
    queue.add(Arrays.asList(root));
    StringBuilder sb = new StringBuilder();
    while (!queue.isEmpty()) {
      Queue<List<Node>> nextQueue = new LinkedList<List<Node>>();
      while (!queue.isEmpty()) {
        List<Node> nodes = queue.remove();
        sb.append('{');
        Iterator<Node> it = nodes.iterator();
        while (it.hasNext()) {
          Node node = it.next();
          sb.append(node.toString());
          if (it.hasNext())
            sb.append(", ");
          if (node instanceof BPTree.InternalNode)
            nextQueue.add(((InternalNode) node).children);
        }
        sb.append('}');
        if (!queue.isEmpty())
          sb.append(", ");
        else {
          sb.append('\n');
        }
      }
      queue = nextQueue;
    }
    return sb.toString();
  }


  /**
   * This abstract class represents any type of node in the tree This class is a super class of the
   * LeafNode and InternalNode types.
   * 
   * @author sapan
   */
  private abstract class Node {

    // List of keys
    List<K> keys;

    /**
     * Package constructor
     */
    Node() {
      this.keys = new ArrayList<K>();
    }

    /**
     * Inserts key and value in the appropriate leaf node and balances the tree if required by
     * splitting
     * 
     * @param key
     * @param value
     */
    abstract void insert(K key, V value);

    /**
     * Gets the first leaf key of the tree
     * 
     * @return key
     */
    abstract K getFirstLeafKey();

    /**
     * Gets the new sibling created after splitting the node
     * 
     * @return Node
     */
    abstract Node split();

    /*
     * (non-Javadoc)
     * 
     * @see BPTree#rangeSearch(java.lang.Object, java.lang.String)
     */
    abstract List<V> rangeSearch(K key, String comparator);

    /**
     * 
     * @return boolean
     */
    abstract boolean isOverflow();

    public String toString() {
      return keys.toString();
    }

  } // End of abstract class Node

  /**
   * This class represents an internal node of the tree. This class is a concrete sub class of the
   * abstract Node class and provides implementation of the operations required for internal
   * (non-leaf) nodes.
   * 
   * @author sapan
   */
  private class InternalNode extends Node {

    // List of children nodes
    List<Node> children;


    /**
     * Package constructor
     */
    InternalNode() {
      super();
      children = new ArrayList<Node>();
    }

    /**
     * (non-Javadoc)
     * 
     * @see BPTree.Node#getFirstLeafKey()
     */
    K getFirstLeafKey() {
      K childFirstKey = children.get(0).getFirstLeafKey();
      return childFirstKey;
    }

    /**
     * (non-Javadoc)
     * 
     * @see BPTree.Node#isOverflow()
     */
    boolean isOverflow() {
      return (this.children.size() > branchingFactor);
    }

    /**
     * (non-Javadoc)
     * 
     * @see BPTree.Node#insert(java.lang.Comparable, java.lang.Object)
     */
    void insert(K key, V value) {
      InternalNode currentRoot;
      Node child1 = child(key);
      child1.insert(key, value);
      if (child1.isOverflow()) {
        Node sibling = child1.split();
        childInsert(sibling.getFirstLeafKey(), sibling);
      }

      if (root.isOverflow()) {
        Node sibling = split();
        currentRoot = new InternalNode();
        currentRoot.keys.add(sibling.getFirstLeafKey());
        currentRoot.children.add(this);
        currentRoot.children.add(sibling);
        root = currentRoot;
      }


    }

    /**
     * (non-Javadoc)
     * 
     * @see BPTree.Node#split()
     */
    Node split() {

      /*
       * InternalNode sibling = new InternalNode(); Iterator it = keys.iterator(); for(int i =
       * origin; i < destination; i++) { while(it.hasNext()) { sibling.keys.add(keys.get(i));
       * sibling.children.add(children.get(i)); keys.get(i); } }
       * 
       */
      // return sibling;

      int origin = (keys.size() / 2) + 1;
      int destination = keys.size();
      InternalNode sibling = new InternalNode();
      sibling.keys.addAll(keys.subList(origin, destination));
      int incDestination = (destination + 1);
      int decOrigin = (origin - 1);
      sibling.children.addAll(children.subList(origin, incDestination));
      keys.subList(decOrigin, destination).clear();
      children.subList(origin, incDestination).clear();

      return sibling;
    }

    /**
     * (non-Javadoc)
     * 
     * @see BPTree.Node#rangeSearch(java.lang.Comparable, java.lang.String)
     */
    List<V> rangeSearch(K key, String comparator) {
      return child(key).rangeSearch(key, comparator);
    }


    private Node child(K key) {
      int pos = Collections.binarySearch(keys, key);
      int ind = 0;
      if(pos >= 0) {
        ind = pos+1;
      }else {
        ind = -pos -1;
      }
      
      Node nodes = children.get(ind);
      return nodes;
    }

    private void childInsert(K key, Node child) {
      int pos = Collections.binarySearch(keys, key);
      int childIndex = 0;
      if(pos >= 0) {
        childIndex = pos +1;
      }else {
        childIndex = -pos -1;
      }
      int incChildIndex = childIndex + 1;
      if (pos >= 0) {
        children.set(childIndex, child);
      } else {
        keys.add(childIndex, key);
        children.add(incChildIndex, child);
      }
    }

  } // End of class InternalNode


  /**
   * This class represents a leaf node of the tree. This class is a concrete sub class of the
   * abstract Node class and provides implementation of the operations that required for leaf nodes.
   * 
   * @author sapan
   */
  private class LeafNode extends Node {

    // List of values
    List<V> values;

    // Reference to the next leaf node
    LeafNode next;

    // Reference to the previous leaf node
    // LeafNode previous;

    /**
     * Package constructor
     */
    LeafNode() {
      super();
      this.keys = new ArrayList<K>();
      this.values = new ArrayList<V>();
    }


    /**
     * (non-Javadoc)
     * 
     * @see BPTree.Node#getFirstLeafKey()
     */
    K getFirstLeafKey() {
      return keys.get(0);
    }

    /**
     * (non-Javadoc)
     * 
     * @see BPTree.Node#isOverflow()
     */
    boolean isOverflow() {
      int dec = branchingFactor - 1;
      return values.size() > dec;
    }

    /**
     * (non-Javadoc)
     * 
     * @see BPTree.Node#insert(Comparable, Object)
     */
    void insert(K key, V value) {
      int pos = Collections.binarySearch(keys, key);
      int index = 0;
      if(pos >= 0) {
        index = pos;
      }else {
        index = -pos -1;
      }
      if (pos >= 0) {
        values.set(index, value);
      } else {
        keys.add(index, key);
        values.add(index, value);
      }
      if (root.isOverflow()) {
        Node sibling = split();
        InternalNode currentRoot = new InternalNode();
        currentRoot.keys.add(sibling.getFirstLeafKey());
        currentRoot.children.add(this);
        currentRoot.children.add(sibling);
        root = currentRoot;
      }

    }

    /**
     * (non-Javadoc)
     * 
     * @see BPTree.Node#split()
     */
    Node split() {
      LeafNode sibling = new LeafNode();
      int origin = keys.size() / 2;
      int destination = keys.size();
      sibling.keys.addAll(keys.subList(origin, destination));
      sibling.values.addAll(values.subList(origin, destination));
      keys.subList(origin, destination).clear();
      values.subList(origin, destination).clear();
      sibling.next = next;
      next = sibling;
      return sibling;
    }

    /**
     * (non-Javadoc)
     * 
     * @see BPTree.Node#rangeSearch(Comparable, String)
     */
    @SuppressWarnings("unchecked")
    List<V> rangeSearch(K key, String comparator) {
      List<V> list = new LinkedList<V>();

      switch (comparator) {
        case "==":
          for (int i = 0; i < size(); i++) {
            int compare = key.compareTo((K) kV[i][0]);
            if (compare == 0) {
              list.add((V) kV[i][1]);
            }
          }
          break;
        case ">=":
          for (int i = 0; i < size(); i++) {
            int compare = key.compareTo((K) kV[i][0]);
            if (compare <= 0) {
              list.add((V) kV[i][1]);
            }
          }
          break;

        case "<=":
          for (int i = 0; i < size(); i++) {
            int compare = key.compareTo((K) kV[i][0]);
            if (compare >= 0) {
              list.add((V) kV[i][1]);
            }
          }
          break;
        default:
          for (int i = 0; i < size(); i++) {
            int compare = key.compareTo((K) kV[i][0]);
            if (compare == 0) {
              list.add((V) kV[i][1]);
            }
          }
      }
      return list;
    }

  } // End of class LeafNode


  /**
   * Contains a basic test scenario for a BPTree instance. It shows a simple example of the use of
   * this class and its related types.
   * 
   * @param args
   */
  public static void main(String[] args) {
    // create empty BPTree with branching factor of 3
    BPTree<Double, Double> bpTree = new BPTree<>(3);

    // create a pseudo random number generator
    Random rnd1 = new Random();

    // some value to add to the BPTree
    Double[] dd = {1.0d, 0.0d, 0.3d, 4.0d};

    // build an ArrayList of those value and add to BPTree also
    // allows for comparing the contents of the ArrayList
    // against the contents and functionality of the BPTree
    // does not ensure BPTree is implemented correctly
    // just that it functions as a data structure with
    // insert, rangeSearch, and toString() working.
    List<Double> list = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      Double j = dd[rnd1.nextInt(4)];
      list.add(j);
      bpTree.insert(j, j);
      System.out.println("\n\nTree structure:\n" + bpTree.toString());
    }
    List<Double> filteredValues = bpTree.rangeSearch(1.0d, ">=");
    System.out.println("Filtered values: " + filteredValues.toString());
    System.out.println("size is " + bpTree.size());
  }

} // End of class BPTree

