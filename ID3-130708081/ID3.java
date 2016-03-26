// ECS629/759 Assignment 2 - ID3 Skeleton Code
// Author: Simon Dixon

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.*;
import java.text.*;
import java.math.*;
class ID3 {

	/** Each node of the tree contains either the attribute number (for non-leaf
	 *  nodes) or class number (for leaf nodes) in <b>value</b>, and an array of
	 *  tree nodes in <b>children</b> containing each of the children of the
	 *  node (for non-leaf nodes).
	 *  The attribute number corresponds to the column number in the training
	 *  and test files. The children are ordered in the same order as the
	 *  Strings in strings[][]. E.g., if value == 3, then the array of
	 *  children correspond to the branches for attribute 3 (named data[0][3]):
	 *      children[0] is the branch for attribute 3 == strings[3][0]
	 *      children[1] is the branch for attribute 3 == strings[3][1]
	 *      children[2] is the branch for attribute 3 == strings[3][2]
	 *      etc.
	 *  The class number (leaf nodes) also corresponds to the order of classes
	 *  in strings[][]. For example, a leaf with value == 3 corresponds
	 *  to the class label strings[attributes-1][3].
	 **/
	class Tree {

		Tree[] children;
		int value;

		public Tree(Tree[] ch, int val) {
			value = val;
			children = ch;
		} // constructor

		public String toString() {
			return toString("");
		} // toString()

		String toString(String indent) {
			if (children != null) {
				String s = "";
				for (int i = 0; i < children.length; i++)
					s += indent + data[0][value] + "=" +
							strings[value][i] + "\n" +
							children[i].toString(indent + '\t');
				return s;
			} else
				return indent + "Class: " + strings[attributes-1][value] + "\n";
		} // toString(String)

	} // inner class Tree

	private int attributes; 	// Number of attributes (including the class)
	private int examples;		// Number of training examples
	private Tree decisionTree;	// Tree learnt in training, used for classifying
	private String[][] data;	// Training data indexed by example, attribute
	private String[][] strings; // Unique strings for each attribute
	private int[] stringCount;  // Number of unique strings for each attribute


	public ID3() {
		attributes = 0;
		examples = 0;
		decisionTree = null;
		data = null;
		strings = null;
		stringCount = null;
	} // constructor

	public void printTree() {
		if (decisionTree == null)
			error("Attempted to print null Tree");
		else
			System.out.println(decisionTree);
	} // printTree()

	/** Print error message and exit. **/
	static void error(String msg) {
		System.err.println("Error: " + msg);
		System.exit(1);
	} // error()

	static final double LOG2 = Math.log(2.0);

	static double xlogx(double x) {
		return x == 0? 0: x * Math.log(x) / LOG2;
	} // xlogx()

	/** Execute the decision tree on the given examples in testData, and print
	 *  the resulting class names, one to a line, for each example in testData.
	 **/


	 // CLASSIFY


	public void classify(String[][] testData) {
		if (decisionTree == null)
			error("Please run training phase before classification");
		// PUT  YOUR CODE HERE FOR CLASSIFICATION
		for(int i = 0; i < testData.length; i++)
			classify(testData[i], decisionTree);
	} // classify()

	public void classify(String[] example, Tree currentNode){
		if(currentNode.children != null){
			int attV = currentNode.value;
			for(int i = 0; i < currentNode.children.length; i++)
				if(example[attV].equals(strings[attV][i]))
					classify(example, currentNode.children[i]);
		 }
		else
			System.out.println(""+strings[attributes-1][currentNode.value]);
	}


	// TRAIN MAIN METHOD

	public void train(String[][] trainingData) {

		// PUT YOUR CODE HERE FOR TRAINING
		if(decisionTree == null){
			Tree root = new Tree(null, 0);
			decisionTree = train(trainingData, root, 0, 0);
		}
	} // train()

	// check if the the trainingData contains repeating sequences of target attribute
	private int isHomogeneous(String[][] trainingData){
		int[] occurencesCount = new int[stringCount[attributes-1]]; // occurences for each value of target

		for (int i = 0; i < stringCount[attributes-1]; i ++){
			int targetAtt = trainingData[0].length - 1;
			for(int j = 1; j < trainingData.length; j++) // exclude header
				if(trainingData[j][targetAtt].equals(strings[attributes-1][i]))
					occurencesCount[i]++;
		}
		for (int i = 0; i < occurencesCount.length; i ++)
			if(occurencesCount[i] == trainingData.length - 1) // extra - 1 for excluding the header
				return i;
		return -1;
	}

	// create the subset filtering by attribute with a ceratain value
	private String[][] subset(String[][] trainingData, int attribute, int value){

		ArrayList<String[]> newTrainExamplesArray = new ArrayList<String[]>();
		int stringsAttId = getAttributeIdByName(trainingData[0][attribute]);
		// add header
		newTrainExamplesArray.add(trainingData[0]);
		for(int i = 1; i < trainingData.length; i++){	// now exclude header
			if(trainingData[i][attribute].equals(strings[stringsAttId][value]))
				newTrainExamplesArray.add(trainingData[i]);
		}

		int newExamplesSize = newTrainExamplesArray.size();
		int newExamplesAttributes = trainingData[0].length - 1;
		String[][] newTrainExamples = new String[newExamplesSize][newExamplesAttributes];

		for(int i = 0; i <= newExamplesAttributes; i++){
			// int attCounter = 0;
			if(i != attribute){
				int newAttId = (i > attribute) ? i - 1 : i;
				for(int j = 0; j < newExamplesSize; j++)
					newTrainExamples[j][newAttId] = newTrainExamplesArray.get(j)[i];

			}
		}
		return newTrainExamples;
	}

	public Tree train(String[][] trainingData, Tree node, int attribute, int value){

		int stringsAttId = getAttributeIdByName(trainingData[0][attribute]);
		Tree rootNode = new Tree(null, 0);

		if(isHomogeneous(trainingData) >=0 || trainingData.length <= 1){
			rootNode.value = isHomogeneous(trainingData);
			return rootNode;
		}

		// calculate best attribute to split the list on
		int bestAttribute = findBestAttribute(trainingData, rootNode);
		// assign the current node this best attribute
		int stringsBestAttId = getAttributeIdByName(trainingData[0][bestAttribute]);
		rootNode.value = stringsBestAttId;

		Tree[] rootChildren = new Tree[stringCount[stringsBestAttId]];
		for(int i = 0; i < stringCount[stringsBestAttId]; i++){
			String[][] train_examples = subset(trainingData, bestAttribute, i);
			// create new value node with future child in it
			Tree nextChild = train(train_examples, rootNode, bestAttribute, i);
			// add childrens
			rootChildren[i] = nextChild;
		}
		rootNode.children = rootChildren;
		return rootNode;
	}



	// TRAIN UTILS

	// Find best attribute
	private int findBestAttribute(String[][] trainingData, Tree node){

		int bestAttribute = 0;
		int totalOccurences = trainingData.length - 1; // - 1 (remove the header)
		// calculate rootEntropy
		float rootEntropy = calculateRootEntropy(trainingData, trainingData[0].length - 1, totalOccurences);
		// loop through all attributes in the data set
		float bestGain = 0;
		for(int i = 0; i < trainingData[0].length - 1; i++){
			// calculate gain of every attribute in the dataset
			float attGain = calculateAttributeGain(trainingData, rootEntropy, totalOccurences, i);
			if(bestGain < attGain){
				bestAttribute = i;
				bestGain = attGain;
			}
		}
		return bestAttribute;
	}

	// 	calculate root entropy of the training data
	private float calculateRootEntropy(String[][] data, int attribute, int totalOccurences){
		float entropy = 0;
		// run through attributes
		for(int i = 0; i < stringCount[attribute]; i++){
			int occurences = 0;
			// run through data set to calculate probabilty
			for(int j = 1; j < data.length ; j++){ // exclude header
				if(data[j][attribute].equals(strings[attributes-1][i])){
					occurences++;
				}
			}
			float probability = occurences * 1.0f /totalOccurences ;
			// System.out.println(strings[attributes-1][i]+" " + occurences + " " + totalOccurences + " " + probability);
			entropy += -xlogx(probability);
		}
		return entropy;
	}

	//  get real attribute of the trimmed training data
	private int getAttributeIdByName(String name){
		for(int i = 0; i < this.data[0].length; i++)
			if(this.data[0][i].equals(name))
				return i;
		return -1;
	}

	//	Calculate entropies of each attribute
	private float calculateAttributeGain(String[][] trainingData, float rootEntropy, int totalOccurences, int attribute){
		// int probability = getProbability();
		float attEntropy = 0;
		// target attribute coloumd id
		int targetAttId = trainingData[0].length - 1;
		// get real strings array ID of the attribute for comparisons
		int stringsAttId = getAttributeIdByName(trainingData[0][attribute]);

		// run through every value of attribute
		for(int attId = 0; attId < stringCount[stringsAttId]; attId++){
			int occurences = 0;
			// count every value occurence with the target index
			int[] occurencesCount = new int[stringCount[attributes-1]]; // occurences for each value of target
			for(int j = 1; j < trainingData.length; j++)
				if(trainingData[j][attribute].equals(strings[stringsAttId][attId])){
					// count total occurences
					occurences++;
					// calculate single target values for each attribute valuesfor(int i = 0; i < stringCount[attributes-1]; i++)
					for(int i = 0; i < stringCount[attributes-1]; i++)
						if(trainingData[j][targetAttId].equals(strings[attributes-1][i]))
							occurencesCount[i]++;
				}
			float probability = (occurences == 0) ? 0 : occurences * 1.0f / totalOccurences;
			float entropy = calculateAttributeEntropy(occurencesCount, attribute);
			attEntropy += probability * entropy;
		}
		return rootEntropy - attEntropy;
	}

	// calculate single attribute's entropy
	private float calculateAttributeEntropy(int[] occurencesCount, int attribute){
		int totalOccurences = 0;
		float entropy = 0;

		// sum occurences
		for(int i = 0; i < occurencesCount.length; i++)
			totalOccurences += occurencesCount[i];
		// calculate entropy and probabilty
		for(int i = 0; i < stringCount[attributes-1]; i++){
			float probability = (occurencesCount[i] == 0) ? 0 : (occurencesCount[i] * 1.0f / totalOccurences);
			entropy += - xlogx(probability);
		}
		return entropy;
	}

	/** Given a 2-dimensional array containing the training data, numbers each
	 *  unique value that each attribute has, and stores these Strings in
	 *  instance variables; for example, for attribute 2, its first value
	 *  would be stored in strings[2][0], its second value in strings[2][1],
	 *  and so on; and the number of different values in stringCount[2].
	 **/
	void indexStrings(String[][] inputData) {
		data = inputData;
		examples = data.length;
		attributes = data[0].length;
		stringCount = new int[attributes];
		strings = new String[attributes][examples];// might not need all columns
		int index = 0;
		for (int attr = 0; attr < attributes; attr++) {
			stringCount[attr] = 0;
			for (int ex = 1; ex < examples; ex++) {
				for (index = 0; index < stringCount[attr]; index++)
					if (data[ex][attr].equals(strings[attr][index]))
						break;	// we've seen this String before
				if (index == stringCount[attr])		// if new String found
					strings[attr][stringCount[attr]++] = data[ex][attr];
			} // for each example
		} // for each attribute
	} // indexStrings()

	/** For debugging: prints the list of attribute values for each attribute
	 *  and their index values.
	 **/
	void printStrings() {
		for (int attr = 0; attr < attributes; attr++)
			for (int index = 0; index < stringCount[attr]; index++)
				System.out.println(data[0][attr] + " value " + index +
									" = " + strings[attr][index]);
	} // printStrings()

	/** Reads a text file containing a fixed number of comma-separated values
	 *  on each line, and returns a two dimensional array of these values,
	 *  indexed by line number and position in line.
	 **/
	static String[][] parseCSV(String fileName)
								throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String s = br.readLine();
		int fields = 1;
		int index = 0;
		while ((index = s.indexOf(',', index) + 1) > 0)
			fields++;
		int lines = 1;
		while (br.readLine() != null)
			lines++;
		br.close();
		String[][] data = new String[lines][fields];
		Scanner sc = new Scanner(new File(fileName));
		sc.useDelimiter("[,\n]");
		for (int l = 0; l < lines; l++)
			for (int f = 0; f < fields; f++)
				if (sc.hasNext())
					data[l][f] = sc.next();
				else
					error("Scan error in " + fileName + " at " + l + ":" + f);
		sc.close();
		return data;
	} // parseCSV()

	public static void main(String[] args) throws FileNotFoundException,
												  IOException {
		if (args.length != 2)
			error("Expected 2 arguments: file names of training and test data");
		String[][] trainingData = parseCSV(args[0]);
		String[][] testData = parseCSV(args[1]);
		ID3 classifier = new ID3();
		classifier.indexStrings(trainingData);

		classifier.train(trainingData);
		classifier.printTree();
		classifier.classify(testData);
	} // main()

} // class ID3
