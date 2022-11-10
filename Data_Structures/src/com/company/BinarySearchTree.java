package com.company;

public class BinarySearchTree {
	class Node {
		int key;
		Node leftChild, rightChild;

		public Node(int item) {
			key = item;
			leftChild = rightChild = null;
		}
	}

	Node root;

	BinarySearchTree() {
		root = null;
	}

	public void printTree() {
		printTree_rec(root, 0);
	}

	private void printTree_rec(Node root, int level) {
		if (root == null) {
			return;
		}

		if (level == 0) {
			System.out.println(root.key);
		} else {
			String printMsg = "   ".repeat(Math.max(0, level - 1));
			printMsg += "|__" + root.key;
			System.out.println(printMsg);
		}

		printTree_rec(root.leftChild, level + 1);
		printTree_rec(root.rightChild, level + 1);
	}


	public void inorder() {
		inorder_rec(root);
	}

	// Inorder Traversal
	private void inorder_rec(Node root) {
		if (root != null) {
			inorder_rec(root.leftChild);
			System.out.print(root.key + " -> ");
			inorder_rec(root.rightChild);
		}
	}

	public void preorder() {
		preorder_rec(root);
	}

	// Preorder Traversal
	private void preorder_rec(Node root) {
		if (root != null) {
			System.out.print(root.key + " -> ");
			preorder_rec(root.leftChild);
			preorder_rec(root.rightChild);
		}
	}

	public void postorder() {
		postorder_rec(root);
	}

	// Postorder Traversal
	private void postorder_rec(Node root) {
		if (root != null) {
			postorder_rec(root.leftChild);
			postorder_rec(root.rightChild);
			System.out.print(root.key + " -> ");
		}
	}


	public Node find(int key) {
		return find_rec(root, key);
	}

	private Node find_rec(Node root, int key) {
		if (root == null)
			return null;

		// Find the node
		if (key < root.key)
			return find_rec(root.leftChild, key);
		else if (key > root.key)
			return find_rec(root.rightChild, key);

		return root;
	}


	public void insertKey(int key) {
		root = insertKey_rec(root, key);
	}

	// Insert key in the tree
	private Node insertKey_rec(Node root, int key) {
		// Return a new node if the tree is empty
		if (root == null) {
			root = new Node(key);
			return root;
		}

		// Traverse to the right place and insert the node
		if (key < root.key)
			root.leftChild = insertKey_rec(root.leftChild, key);
		else if (key > root.key)
			root.rightChild = insertKey_rec(root.rightChild, key);

		return root;
	}

	public void deleteKey(int key) {
		root = deleteKey_rec(root, key);
	}

	private Node deleteKey_rec(Node root, int key) {
		// Return if the tree is empty
		if (root == null)
			return null;

		// Find the node to be deleted
		if (key < root.key)
			root.leftChild = deleteKey_rec(root.leftChild, key);
		else if (key > root.key)
			root.rightChild = deleteKey_rec(root.rightChild, key);
		else {
			// If the node is with only one child or no child
			if (root.leftChild == null)
				return root.rightChild;
			else if (root.rightChild == null)
				return root.leftChild;

			// If the node has two children
			// Place the inorder successor in position of the node to be deleted
			int inorderSuccessorVal = minValue(root.rightChild);
			root.key = inorderSuccessorVal;

			// Delete the inorder successor
			root.rightChild = deleteKey_rec(root.rightChild, inorderSuccessorVal);
		}

		return root;
	}

	// Find the inorder successor
	private int minValue(Node root) {
		int minVal = root.key;
		while (root.leftChild != null) {
			minVal = root.leftChild.key;
			root = root.leftChild;
		}
		return minVal;
	}
}
