package src;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

public class MST {
    
    public static HashMap<String, City> kruskalMST(HashMap<String, City> cityMap) {
        HashMap<String, City> mst = new HashMap<>();
        DisjointSet disjointSet = new DisjointSet(cityMap);


        List<CityConnectionStruct> allConnections = new ArrayList<>();

        for (City city : cityMap.values()) {
            for (CityConnectionStruct c : city.connections) c.meta = utils.standardizeCityKey(city.state, city.name);
            allConnections.addAll(city.connections);
            city.connections = new LinkedList<>();
        }
        
        for (City city : cityMap.values()) {
            String cityKey = utils.standardizeCityKey(city.state, city.name);
            // String cityKey = StringStandardize.standardizeString(city.state) + "__" + StringStandardize.standardizeString(city.name);
            mst.put(cityKey, city);
        }

        Collections.sort(allConnections, Comparator.comparingDouble(c -> c.distance));

        for (CityConnectionStruct connection : allConnections) {
            String city1 = utils.standardizeCityKey(connection.state, connection.name);
            // String city1 = StringStandardize.standardizeString(connection.state) + "__" + StringStandardize.standardizeString(connection.name);
            String city2 = connection.meta;
            if (cityMap.get(city1)!=null){
                if (!disjointSet.find(city1).equals(disjointSet.find(city2))) {
                    mst.get(city2).connections.add(connection);
                    disjointSet.union(city1, city2);
                }
            }
        }

        return mst;
    }

    public static HashMap<String, City> primMST(HashMap<String, City> cityMap) {
        HashMap<String, City> mst = new HashMap<>();
        Set<String> visited = new HashSet<>();

        PriorityQueue<CityConnectionStruct> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(c -> c.distance));
        int index = new Random().nextInt(cityMap.size());
        String startCityKey  = cityMap.keySet().toArray(new String[cityMap.size()])[index];
        // String startCityKey = cityMap.keySet().iterator().next();
        System.out.println("Starting from : " + startCityKey);
        visited.add(startCityKey);

        mst.put(startCityKey, cityMap.get(startCityKey));

        for (CityConnectionStruct c: cityMap.get(startCityKey).connections){
            c.meta = startCityKey;
        }

        priorityQueue.addAll(cityMap.get(startCityKey).connections);
        cityMap.get(startCityKey).connections = new LinkedList<CityConnectionStruct>();

        while (!priorityQueue.isEmpty()) {
            CityConnectionStruct connection = priorityQueue.poll();
            String connectedCityKey = utils.standardizeCityKey(connection.state, connection.name);
            // String connectedCityKey = StringStandardize.standardizeString(connection.state) + "__" + StringStandardize.standardizeString(connection.name) ;

            if (!visited.contains(connectedCityKey) && cityMap.get(connectedCityKey)!=null) {
                visited.add(connectedCityKey);
                mst.put(connectedCityKey, cityMap.get(connectedCityKey));
                mst.get(connection.meta).connections.add(connection);
                for (CityConnectionStruct c: cityMap.get(connectedCityKey).connections){
                    c.meta = connectedCityKey;
                }

                priorityQueue.addAll(cityMap.get(connectedCityKey).connections);
                cityMap.get(connectedCityKey).connections = new LinkedList<CityConnectionStruct>();
            }
        }

        return mst;
    }

    private static class DisjointSet {
        HashMap<String, String> parent;

        public DisjointSet(HashMap<String, City> cityMap) {
            parent = new HashMap<>();
            for (Map.Entry<String, City> me: cityMap.entrySet()) {

                parent.put(me.getKey(), me.getKey());
            }
        }

        public String find(String cityKey) {
            if (parent.get(cityKey).equals(cityKey)) {
                return cityKey;
            }
            return find(parent.get(cityKey));
        }

        public void union(String city1, String city2) {
            String root1 = find(city1);
            String root2 = find(city2);
            parent.put(root1, root2);
        }
    }

}
