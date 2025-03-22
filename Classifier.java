import java.io.*;
import java.util.*;

// This class is a model that predicts a label when given text-based data. In this case, the thing
// being classified is emails. 
public class Classifier {
    private ClassifierNode overallRoot;

    // Behavior: 
    //   - This method constructs a classifier object. The input has to be formatted in a specific
    //   - manner, otherwise problems will arise. Expect to use a pre-order traversal,
    //   - with features being on a line followed by thresholds on the next line, both having their
    //   - respective words placed before. For example, before every feature the word 'feature:'
    //   - will be placed before, and same for threshold. For labels, there won't be the word
    //   - 'label:' before every label.
    // Parameters:
    //   - input: the input being taken in to make the classifier object
    // Exceptions:
    //   - if the given input is null, an IllegalArgumentException is thrown.
    public Classifier(Scanner input){
        if(input == null){
            throw new IllegalArgumentException();
        }
        overallRoot = classifierHelper(input);
    }

    // Behavior: 
    //   - This method recursively constructs a classification tree from the input Scanner.
    //   - If the input describes a decision node it creates a node with a feature and
    //     threshold, then recursively builds the left and right subtrees.
    //   - If the input describes a leaf node (classification label), it creates a leaf node 
    //     with the given label.
    // Parameters:
    //   - input: A Scanner object that provides the classification tree data in pre-order format.
    // Returns:
    //   - ClassifierNode: The root of the tree, or null if empty
    private ClassifierNode classifierHelper(Scanner input){
        if(!input.hasNextLine()){
            return null;
        }

        String line = input.nextLine().trim();
        if(line.startsWith("Feature: ")){
            String feature = line.substring("Feature: ".length());
            String thresholdLine = input.nextLine().trim();
            double threshold = Double.parseDouble(thresholdLine.substring("Threshold: ".length()));
            ClassifierNode currentNode = new ClassifierNode(threshold, feature, 
                    classifierHelper(input), classifierHelper(input));
            return currentNode;
        }
        else{
            return new ClassifierNode(line, null);
        }
    }

    // Behavior: 
    //   - This method creates a classifier object using sizable amounts of data
    // Parameters:
    //   - data: a list of different data samples
    //   - labels: the class labels corresponding to each data sample
    // Exceptions:
    //   - if data or labels is null, if size of data and labels dont match, or data is empty,
    //   - an IllegalArgument Exception is thrown
    public Classifier(List<TextBlock> data, List<String> labels){
        if(data == null || labels == null || data.size() != labels.size() || data.isEmpty()){
            throw new IllegalArgumentException();
        }
        overallRoot = new ClassifierNode(labels.get(0), data.get(0));
        for(int i = 1; i < data.size(); i++){
            overallRoot = classifierHelper(overallRoot, labels.get(i), data.get(i));
        }
    }

    // Behavior:  
    //   - This method inserts a new labeled data point into the classification tree.  
    //   - If the current node is a leaf:  
    //       - If the label matches, returns the node as is.  
    //       - Otherwise, creates a decision node based on the biggest difference feature.  
    //   - If the current node is not a leaf, recursively traverses left or right based on the  
    //     feature threshold.  
    // Parameters:  
    //   - node: The current ClassifierNode being evaluated.  
    //   - label: The class label for the new data.  
    //   - data: A TextBlock containing the feature values of the new data sample.  
    // Returns:  
    //   - A ClassifierNode representing the updated tree structure after insertion.  
    private ClassifierNode classifierHelper(ClassifierNode node, String label, TextBlock data){
        if(node.isLeaf()){
            if(node.label.equals(label)){
                return node;
            }
            else{
                String feature = node.block.findBiggestDifference(data);
                double threshold = midpoint(node.block.get(feature), data.get(feature));
                ClassifierNode decisionNode = new ClassifierNode(threshold, feature);
                if(data.get(feature) < threshold){
                    decisionNode.left = new ClassifierNode(label, data);
                    decisionNode.right = node;
                }
                else{
                    decisionNode.left = node;
                    decisionNode.right = new ClassifierNode(label, data);
                }
                return decisionNode;
            }
        }
        else{
            if(data.get(node.feature) < node.threshold){
                node.left = classifierHelper(node.left, label, data);
            }
            else{
                node.right = classifierHelper(node.right, label, data);
            }
            return node;
        }
    }

    // Behavior: 
    //   - This method calculates the appropriate label for a given piece of data, whether it's
    //   - spam or not
    // Parameters:
    //   - input: the data being inputted
    // Returns:
    //   - String: either spam or ham(not spam)
    // Exceptions:
    //   - if the given input is null, an IllegalArgumentException is thrown.
    public String classify(TextBlock input){
        if(input == null){
            throw new IllegalArgumentException();
        }
        return classify(input, overallRoot);
    }

    // Behavior: 
    //   - This method classifies a given data point based on the decision tree.
    //   - If the current node is a leaf, return its label. Otherwise, it goes left or right based
    //   - on the feature threshold.
    // Parameters:
    //   - input: a TextBlock containing feature values to be classified
    //   - curr: the current ClassifierNode being evaluated in decision tree
    // Returns:
    //   - String: spam if the input data is classified as such, or ham otherwise
    private String classify(TextBlock input, ClassifierNode curr){
        if(curr.isLeaf()){
            return curr.label;
        }
        else{
            if(input.get(curr.feature) < curr.threshold){
                return classify(input, curr.left);
            }
            else{
                return classify(input, curr.right);
            }
        }
    }

    // Behavior: 
    //   - This method prints the contents of the classification tree to the given output in a
    //   - preorder traversal
    // Parameters:
    //   - output: the place where the classification tree will be written
    // Exceptions:
    //   - if the given output is null, an IllegalArgumentException will be thrown
    public void save(PrintStream output){
        if(output == null){
            throw new IllegalArgumentException();
        }
        save(output, overallRoot);
    }

    // Behavior:  
    //   - This method recursively saves (prints) the classification tree structure to the given
    //   - output stream. If the current node is a leaf, it prints the label. If the current node
    //   - is a decision node, it prints the feature and threshold before recursively saving its 
    //   - children. The contents of the tree are printed using a pre-order traversal. 
    // Parameters:  
    //   - output: The PrintStream where the classification tree will be written.  
    //   - curr: The current node in the classification tree being processed.
    private void save(PrintStream output, ClassifierNode curr){
        if(curr != null){
            if(curr.isLeaf()){
                output.println(curr.label);
            }
            else{
                output.println("Feature: " + curr.feature);
                output.println("Threshold: " + curr.threshold);
            }
            save(output, curr.left);
            save(output, curr.right);   
        }
    }
    
    // This class represents a node in the classification tree. A node has a threshold value, a
    // a feature, a label designating whether something is spam or not, a block of data, and access
    // to it's left and right children.
    private static class ClassifierNode {
        public final double threshold;
        public final String feature;
        public final String label;
        public ClassifierNode left;
        public ClassifierNode right;
        public final TextBlock block;

        // Behavior: 
        //   - This method constructs a classifierNode with a threshold and feature
        // Parameters:
        //   - threshold: the probability of a feature occurring
        //   - feature: the word being looked at
        public ClassifierNode(double threshold, String feature){
            this.threshold = threshold;
            this.feature = feature;
            label = null;
            block = null;
        }

        // Behavior: 
        //   - This method constructs a classifierNode with a threshold, feature, and the given
        //   - left and right links to the children 
        // Parameters:
        //   - threshold: the probability of a feature occurring
        //   - feature: the word being looked at
        //   - left: the left link linking the currentnode to a child node
        //   - right: the right link linking the currentnode to a child node
        public ClassifierNode(double threshold, String feature, ClassifierNode left,
                ClassifierNode right){
                    this(threshold, feature);
                    this.left = left;
                    this.right = right;
        }

        // Behavior: 
        //   - This method constructs a classifierNode with a label and data. This is a leaf node.
        // Parameters:
        //   - label: either spam or ham
        //   - block: the data associated with the node
        public ClassifierNode(String label, TextBlock block){
            this.label = label;
            this.feature = null;
            this.threshold = 0.0;
            this.block = block;
        }

        // Behavior: 
        //   - This method determines whether a classifierNode is a leaf or not
        // Returns:
        //   - boolean: true if it has a spam or ham value
        public boolean isLeaf(){
            return label != null;
        }
    }
    ////////////////////////////////////////////////////////////////////
    // PROVIDED METHODS - **DO NOT MODIFY ANYTHING BELOW THIS LINE!** //
    ////////////////////////////////////////////////////////////////////

    // Helper method to calcualte the midpoint of two provided doubles.
    private static double midpoint(double one, double two) {
        return Math.min(one, two) + (Math.abs(one - two) / 2.0);
    }    

    // Behavior: Calculates the accuracy of this model on provided Lists of 
    //           testing 'data' and corresponding 'labels'. The label for a 
    //           datapoint at an index within 'data' should be found at the 
    //           same index within 'labels'.
    // Exceptions: IllegalArgumentException if the number of datapoints doesn't match the number 
    //             of provided labels
    // Returns: a map storing the classification accuracy for each of the encountered labels when
    //          classifying
    // Parameters: data - the list of TextBlock objects to classify. Should be non-null.
    //             labels - the list of expected labels for each TextBlock object. 
    //             Should be non-null.
    public Map<String, Double> calculateAccuracy(List<TextBlock> data, List<String> labels) {
        // Check to make sure the lists have the same size (each datapoint has an expected label)
        if (data.size() != labels.size()) {
            throw new IllegalArgumentException(
                    String.format("Length of provided data [%d] doesn't match provided labels [%d]",
                                  data.size(), labels.size()));
        }
        
        // Create our total and correct maps for average calculation
        Map<String, Integer> labelToTotal = new HashMap<>();
        Map<String, Double> labelToCorrect = new HashMap<>();
        labelToTotal.put("Overall", 0);
        labelToCorrect.put("Overall", 0.0);
        
        for (int i = 0; i < data.size(); i++) {
            String result = classify(data.get(i));
            String label = labels.get(i);

            // Increment totals depending on resultant label
            labelToTotal.put(label, labelToTotal.getOrDefault(label, 0) + 1);
            labelToTotal.put("Overall", labelToTotal.get("Overall") + 1);
            if (result.equals(label)) {
                labelToCorrect.put(result, labelToCorrect.getOrDefault(result, 0.0) + 1);
                labelToCorrect.put("Overall", labelToCorrect.get("Overall") + 1);
            }
        }

        // Turn totals into accuracy percentage
        for (String label : labelToCorrect.keySet()) {
            labelToCorrect.put(label, labelToCorrect.get(label) / labelToTotal.get(label));
        }
        return labelToCorrect;
    }
}