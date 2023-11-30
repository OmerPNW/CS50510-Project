# CS51510-Project

## Installation
Download and install the appropriate Java Development Kit (JDK) for your operating system, set the necessary environment variables (e.g., JAVA_HOME and PATH), and verify the installation using java -version in the terminal or command prompt.

## Description
This project delves into the world of graph algorithms, integral to Data Structures. Drawing from deep interdisciplinary research, these visually appealing algorithms, exemplified by Google Maps, find application in real-life scenarios. Focused on implementing graph algorithms and engineering heuristics, this project is considered to work on geographical map of US Central Time Zone cities. Interconnected by directed links reflecting distance as the primary cost, the project evaluates traversal costs considering topological, environmental, and human-centric factors. Using optimal data structures like priority queues, hash maps, and binary trees, algorithms such as Prim's, Kruskal's MST, BFS, Djikstra, and Bellman Ford address diverse use cases, reflecting adaptability to real-world scenarios

## Usage
#### 1. Data Validation:
- This step ensures that data meets specified crieteria, standard or rules to aiming to maintain accuracy and reliability.
- Run this code [LDP.java](LDP.java) to validate your input against the **city data** and **city connections data**.

#### 2. Data Verification:
- This step includes of confirming the accuracy, consistency and completeness of the data to ensure it aligns with predefined standards.
- It mainly includes a check where it verifies if every city is connected to other city by any means and this done using the breadth first search (**BFS**).
- Run the same [LDP.java](LDP.java) code to verify your data.
     
#### 3. Optimal path
- [OptimalPath.java](OptimalPath.java) is sort of the main file which uses [MST.java](MST.java) and [MapVisualize.java](MapVisualize.java) file classes as well. 

- To determine the optimal path using Kruskal's or Prim's algorithms, focus on constructing the Minimum Spanning Tree (MST) of the interconnected cities on the map. These algorithms systematically select edges with minimal associated costs, ensuring the creation of an efficient tree that spans all cities.
- Once the MST [MST.java](MST.java) is established, you can extend the pathfinding capabilities by incorporating additional graph algorithms like Djikstra's or Bellman Ford. These algorithms work synergistically, providing an optimized solution for traversing the geographical map, considering both the MST structure and individual edge costs. By seamlessly integrating Kruskal's or Prim's algorithms with pathfinding approaches, the code achieves a holistic strategy for finding the optimal path in the context of the constructed map.
- To find the optimal path using the implemented algorithms, initiate the pathfinding process by specifying the source and destination nodes in the [OptimalPath.java](OptimalPath.java). 
- This java file [OptimalPath.java](OptimalPath.java) not only identifies and outputs the most efficient route but also provides comprehensive information about the path, including details on the traversals made.
#### 4. Visualize 
- The [MapVisualize.java](MapVisualize.java) code, empowers the graphical depiction of our constructed map and the outcomes of the implemented algorithms. This visual representation enhances comprehension of the optimal path, Minimum Spanning Tree (MST), and other pertinent information derived from the project.


## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

## License

[MIT](https://choosealicense.com/licenses/mit/)
