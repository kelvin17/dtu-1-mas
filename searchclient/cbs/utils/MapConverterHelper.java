package searchclient.cbs.utils;

import searchclient.cbs.model.Move;

import java.util.*;

public class MapConverterHelper {
    public static List<Map<Character, Move>> convertMapToListOfMaps(Map<Character, List<Move>> inputMap) {
        List<Map<Character, Move>> result = new ArrayList<>();
        List<Character> keys = new ArrayList<>(inputMap.keySet());
        List<List<Move>> values = new ArrayList<>(inputMap.values());

        int[] indices = new int[keys.size()];
        Arrays.fill(indices, 0);

        while (true) {
            Map<Character, Move> innerMap = new HashMap<>();
            for (int i = 0; i < keys.size(); i++) {
                innerMap.put(keys.get(i), values.get(i).get(indices[i]));
            }
            result.add(innerMap);

            int k = keys.size() - 1;
            while (k >= 0 && indices[k] == values.get(k).size() - 1) {
                indices[k] = 0;
                k--;
            }
            if (k < 0) break;
            indices[k]++;
        }

        return result;
    }

//    public static void main(String[] args) {
//        Map<String, List<String>> inputMap = new HashMap<>();
//        inputMap.put("a", Arrays.asList("a1", "a2", "a3", "a4"));
//        inputMap.put("b", Arrays.asList("b1", "b2"));
//        inputMap.put("c", Arrays.asList("c1", "c2", "c3", "c4"));
//
//        List<Map<String, String>> listOfMaps = convertMapToListOfMaps(inputMap);
//
//        for (Map<String, String> map : listOfMaps) {
//            System.out.println(map);
//        }
//    }
}

