package com.company;

import java.util.Scanner;
import java.util.ArrayList;

public class BinarySearchTreeTest {

    public static void main(String[] args) {
        BinarySearchTree bst = new BinarySearchTree();
        Scanner s = new Scanner(System.in);
        ArrayList<Integer> nodeValues = new ArrayList<>();

        while (true) {
            System.out.println("\nWhat would you like to do? Type");
            System.out.println("'I' - Insert node");
            System.out.println("'D' - Delete node");
            System.out.println("'S' - Search node");
            System.out.println("'P' - Print BST");
            System.out.println("'C' - Clear BST");
            System.out.println("'E' - Exit program");

            String userAction = s.nextLine();
            if (userAction == ""){
                userAction = s.nextLine();
            }
            int inputVal;

            switch (userAction.toUpperCase()) {
                case "I" :
                    System.out.println("Enter the number of nodes you wish to insert (defaults to 1)");
                    int numOfValuesToInsert = GetIntegerInput(s, "Invalid input, input must be an integer!");
                    if (numOfValuesToInsert <= 0) {
                        numOfValuesToInsert = 1;
                        System.out.println("You must input at least 1 input!");
                    }

                    for (int i = 0; i < numOfValuesToInsert; i++){
                        System.out.println("Enter node value to insert");
                        inputVal = GetIntegerInput(s,  "Invalid input, input must be an integer!");

                        bst.insertKey(inputVal);
                        nodeValues.add(inputVal);
                        System.out.println(nodeValues);
                    }
                    break;
                case "D" :
                    System.out.println("Enter node value to delete");
                    inputVal = GetIntegerInput(s,   "Invalid input, input must be an integer!");

                    bst.deleteKey(inputVal);
                    nodeValues.remove(Integer.valueOf(inputVal));
                    System.out.println(nodeValues);
                    break;
                case "S" :
                    System.out.println("Enter node value to search");
                    inputVal = GetIntegerInput(s,   "Invalid input, input must be an integer!");

                    BinarySearchTree.Node searchedNode = bst.find(inputVal);
                    if (searchedNode == null){
                        System.out.println("Node with value '" + inputVal + "' does not exist");
                    } else {
                        System.out.println("Node with value '" + inputVal + "' exists");
                    }
                    break;
                case "C" :
                    bst = new BinarySearchTree();
                    nodeValues.clear();
                    break;
                case "P" :
                    System.out.println("Type");
                    System.out.println("'In' - inorder traversal");
                    System.out.println("'Pre' - preorder traversal");
                    System.out.println("'Post' - postorder traversal");
                    System.out.println("'Tree' - binary tree structure");

                    String traversalType = s.nextLine();
                    switch (traversalType.toUpperCase()) {
                        case "IN" -> bst.inorder();
                        case "PRE" -> bst.preorder();
                        case "POST" -> bst.postorder();
                        case "TREE" -> bst.printTree();
                        default -> System.out.println("Command unrecognised");
                    }
                    System.out.println();
                    break;
                case "E" :
                    return;
                default :
                    System.out.println("Command unrecognised");
                    break;
            }
        }
    }

    private static int GetIntegerInput(Scanner scanner,  String errorTryAgainPrompt) {
        while (true){
            if (scanner.hasNextInt()){
                return scanner.nextInt();
            }else{
                System.out.println(errorTryAgainPrompt);
                scanner.next();
            }
        }
    }
}
