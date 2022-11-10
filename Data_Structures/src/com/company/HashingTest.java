package com.company;

import java.util.Scanner;

public class HashingTest {

	// Print Collision Menu
	public static void printCollisionMenu() {
		System.out.print("Select a Collision Resolution Technique.\n"
					   + "1 - Linear Probing.\n"
					   + "2 - Quadratic Probing.\n"
					   + ">> ");
	}

	// Print Option Menu
	public static void printOptionMenu() {
		System.out.print("\nHash Table Options\n"
					   + "1. Insert value\n"
					   + "2. Remove value\n"
					   + "3. Search value\n"
					   + "4. Auto-fill table\n"
					   + "5. Print Hash Table\n"
					   + "6. Clear Hash Table\n"
					   + "0. Exit\n"
					   + ">> ");
	}

	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);
		int selection; //CollisionMenu selection
		int value=0; //user input value
		int size=0; //hash table size

	/** Section 1 - Collision Choice **/
		do {
            printCollisionMenu();
            selection = input.nextInt();

            if (selection == 1) {
                System.out.print("\nEnter number of elements you wish to have (1-100): ");
                size = input.nextInt();
                while (Hashing.checkValid(size) == false) {
                    System.out.print("Number input is in invalid range\n"
                    			   + "Please re-enter the number of elements you wish to have: ");
                    size = input.nextInt();
                }

                Hashing.createLinearHashtable(size);
                System.out.println("Linear hash table created, with " + size + " buckets.\n");
                break;
            }
            else if (selection == 2) {
                System.out.print("\nEnter number of elements you wish to have (1-100): ");
                size = input.nextInt();
                while (Hashing.checkValid(size) == false) {
                    System.out.print("Number input is in invalid range\n"
             			   		   + "Please re-enter the number of elements you wish to have: ");
                    size = input.nextInt();
                }

                Hashing.createQuadraticHashtable(size);
                System.out.println("Quadratic hash table created, with "+ Hashing.capacity +" buckets.\n");
                break;
            }
            else {
                System.out.println("Error! Selection entered invalid. Please try again!\n");
            }
        }//error checking - if user enters selection out of range of 1 & 2
           while (selection != 1 || selection != 2);

	/** Section 2 - Hashing Options Menu **/
		Hashing.printTable();

		printOptionMenu();
		int option = input.nextInt();
		System.out.println();

		while (option != 0) {
			switch (option) {

			case 1: //Insert value.
				System.out.print("Enter value to insert: ");
				value = input.nextInt();
				if (selection == 1) {Hashing.linearInsertion(value);}
				else {Hashing.quadraticInsertion(value);}
				break;

			case 2: //Remove value.
				System.out.print("Enter value to delete: ");
				value = input.nextInt();
				if (selection == 1) {Hashing.deleteValue(value);}
				else {Hashing.deleteValue(value);}
				break;

			case 3: //Search value.
				System.out.print("Enter value to search: ");
				value = input.nextInt();
				Hashing.searchTable(value);
				break;

			case 4: //Auto-fill hash table.
				if (selection == 1) {
					for (int i=Hashing.getCounter(); i>0; i--) {
						Hashing.linearInsertion(Hashing.randomGenerator());
					}
				}
				else {
					for (int i=Hashing.getCounter(); i>0; i--) {
						Hashing.quadraticInsertion(Hashing.randomGenerator());
					}
				}
				break;

			case 5: //Print hash table.
				Hashing.printTable();
				break;

			case 6: //Clear hash table.
				Hashing.clearTable();
				//Hash table will be recreated after clearing as to prevent errors.
				if (selection == 1) {
					Hashing.createLinearHashtable(size);
				}
				else {
					Hashing.createQuadraticHashtable(size);
				}
				Hashing.printTable();
				break;

			default:
				System.out.println("Error! Option entered invalid. Please try again!\n");
				break;
			}

			printOptionMenu();
			option = input.nextInt();
			System.out.println();
		}

		if (option == 0) {
			System.out.println("End.");
		}
	}
}
