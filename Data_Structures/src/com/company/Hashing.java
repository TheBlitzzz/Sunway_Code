package com.company;

import java.util.HashMap;
import java.util.Random;

public class Hashing {

	static int capacity;
	static int remainingCount=0;
	static Random r = new Random();
	static HashMap<Integer, Integer> hashtable = new HashMap<Integer, Integer>();

 // Creating Hash table for Linear Probing
	public static void createLinearHashtable(int c) {
		remainingCount = c;
		capacity = c;
		for (int i = 0; i < capacity; i++) {
			hashtable.put(i, null);
		}
	}

 // Checks if Hash table is full
	public static boolean checkFull() {
		for (int key : hashtable.keySet()) {
			if (hashtable.get(key) == null) {
				return false;
			}
		} return true;
	}

 // Collision Resolution Technique 1 - Linear Probing
	//If the insertion of a value was to be inserted into an index that is occupied with a value, a collision will occur.
	//This can be avoided by searching for an empty cell next to an occupied one by probing linearly.
	//A hash function is used to compute the key in which it is placed in the table (key = (hash(value)%capacity).
	//In order to prevent collision and perform linear probing, we add 1 to the hash(value) to look into the next key.
	/*
	 * Example: Table size of 5, index 1 is occupied with value 1.
	 *
	 * Value 11 is being inserted into the table but collides with value 1 as they share the same index key.
	 * Linear probing will then be applied ((11+1)%5) and check if the next cell is empty for insertion.
	 * Since index 2 has no occupants, value 11 is inserted into index 2.
	 */

	//If for instance, the value is trying to insert into a table with a cluster of values,
	//it will continue to add 1 to hash(value) until it finds an empty key.

	// https://www.upgrad.com/blog/hashing-in-data-structure/
	public static void linearInsertion(int value) {
		//error checking.
		if (checkFull()) {
			System.out.println("Error! Hash table is full, unable to insert value (" + value + ").");
			return;
		}
		else {
			//if key is occupied, will loop and move value to another key.
			for (int x = 0; x < capacity; x++) {
				//calculate key of input value.
				int key = (value + x) % capacity;
				//check if the key is empty, then insert.
				if (hashtable.get(key) == null) {
					hashtable.put(key, value);
					System.out.println("Value (" + value + ") INSERTED in Key (" + key + ")");
					remainingCount--;
					return;
				}
			}
		}
	}

 // Checks if number is a prime number
	public static boolean isPrimeNumber(int p) {
	    for (int i = 2; i <= p / 2; ++i) {
	    	if (p % i == 0) {
	    		return false;
	    	}
	    }
	    return true;
	}

 // Collision Resolution Technique 2 - Quadratic Probing
	//For quadratic probing, it is normal practice to ensure the size of the hash table to be x2 of
	//amount of elements we want to insert.
	//As if the size of the table is a prime, it is guaranteed that the 1st (0.5*table_size) elements
	//inserted will be hashed to distinct buckets. Hence we try to ensure the table is at least x2 of the
	//hashing key (in this case our table size). This ensures the elements will be hashed to distinct buckets

	/*
	 * Example: Table size is 16. Will simulate case where all 5 values will hash to value 2
	 *
	 * First piece goes to index 2.
	 * Second piece goes to 3 ((2 + 1)%16
	 * Third piece goes to 6 ((2+4)%16
	 * Fourth piece goes to 11((2+9)%16
	 * Fifth piece dosen't get inserted because (2+16)%16==2 which is full,
	 * so we end up back where we started and we haven't searched all empty spots.
	 */

	// https://cathyatseneca.gitbooks.io/data-structures-and-algorithms/content/tables/quadratic_probing_and_double_hashing.html

 // Creating Hash table for Quadratic Probing
	public static void createQuadraticHashtable(int c) {
		remainingCount = c;
		int min_size = c*2;

		//this loop will check for a number that is
		//1) 2x larger than the amount of items to insert
		//2) a prime number
		//after finding the number, we assign our capacity to that number
		mainLoop:
			for (int i = min_size; i<100000000; i++) {
				if (isPrimeNumber(i)) {
					capacity = i;
					break mainLoop;
				}
		}

		for (int x = 0; x < capacity; x++) {
			hashtable.put(x, null);
		}
	}

 // Collision Resolution Technique 2 - Quadratic Probing
    public static void quadraticInsertion(int value) {
        boolean sentinel = true;
        int i = 0;
        if (checkFull()) {
            System.out.println("Error! Hash table is full, unable to insert value (" + value + ").");
            return;
        }
        else {

        while (sentinel) {

            int key = (value+i*i) % capacity;

            if (hashtable.get(key) == null) {
                if (remainingCount <= 0) {
                    System.out.println("\nWarning! Hash table exceeding 50% capacity, hashing issues may arise (" + value + ").");
                    hashtable.put(key, value);
                    System.out.println("Value (" + value + ") INSERTED in Key (" + key + ")");
                    return;
                    } else {

                hashtable.put(key, value);
                System.out.println("Value (" + value + ") INSERTED in Key (" + key + ")");
                sentinel = false;
                    }
                }
            i++;
            }
        }
        remainingCount--;
    }

 // Delete a value in Hash table
	public static void deleteValue(int value) {
		for (int key : hashtable.keySet()) {
			try {
				if (hashtable.get(key) == value) {
					hashtable.remove(key);
					hashtable.put(key, null);
					System.out.println("Value (" + value + ") DELETED from Key (" + key + ")");
					remainingCount++;
					return;
				}
			} catch (Exception n) {
				//prevent "return null value" error.
				if (hashtable.get(key) == null) {
					continue;
				}
			}
		}
		System.out.println("Error! Value (" + value + ") does not exist, unable to remove.");
	}

 // Search for value in Hash table
	public static void searchTable(int value) {
		for (int key : hashtable.keySet()) {
            try {
                if (hashtable.get(key) == value) {
                    System.out.println("Value (" + value + ") is LOCATED in Key (" + key + ")");
                    return;

                }
            } catch (Exception n) {
                //prevent "return null value" error.
                if (hashtable.get(key) == null) {
                    continue;
                }
            }
        }
        System.out.println("Error! Value (" + value + ") is not found.");
	}

 // Print Hash table
	public static void printTable() {
		System.out.println("Hash Table");
		for (int key : hashtable.keySet()) {
			System.out.println(key + ": " + hashtable.get(key));
		}
	}

 // Clear Hash table
	public static void clearTable() {
		hashtable.clear();
		System.out.println("Successfully cleared Hash Table!\n");
	}

 // Generates number 1-100 to be randomly inserted later on
	public static int randomGenerator() {
		int random = r.nextInt(100)+1;
		return random;
	}

 // Used in main method to auto-fill
	public static int getCounter() {
		return remainingCount;
	}

 // Checks if user inputs a value between 1-100
    public static boolean checkValid(int x) {
        if (0<x && x<101) {
            return true;
        } else {
            return false;
        }
    }

}
