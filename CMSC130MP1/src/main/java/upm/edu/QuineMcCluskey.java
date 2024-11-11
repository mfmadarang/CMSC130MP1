package upm.edu;
import java.util.*;

public class QuineMcCluskey {

    // Step 1: Convert minterms to binary strings and group by number of 1s
    public static Map<Integer, List<String>> groupMinterms(int[] minterms, int bitLength) {
        Map<Integer, List<String>> groups = new TreeMap<>();
        for (int minterm : minterms) {
            String binary = String.format("%" + bitLength + "s",
                    Integer.toBinaryString(minterm)).replace(' ', '0');
            int onesCount = countOnes(binary);
            groups.computeIfAbsent(onesCount, k -> new ArrayList<>()).add(binary);
        }
        return groups;
    }

    private static int countOnes(String binary) {
        return (int) binary.chars().filter(ch -> ch == '1').count();
    }

    // Step 2: Find prime implicants through iterative combining
    public static List<String> findPrimeImplicants(Map<Integer, List<String>> groups, int bitLength) {
        Set<String> primeImplicants = new HashSet<>();
        Set<String> combinedTerms = new HashSet<>();

        boolean foundNew;
        do {
            Map<Integer, List<String>> newGroups = new TreeMap<>();
            foundNew = false;
            Set<String> newCombinedTerms = new HashSet<>();

            // Compare adjacent groups
            List<Integer> groupNumbers = new ArrayList<>(groups.keySet());
            for (int i = 0; i < groupNumbers.size() - 1; i++) {
                int currentGroup = groupNumbers.get(i);
                int nextGroup = groupNumbers.get(i + 1);

                if (nextGroup - currentGroup == 1) {
                    for (String term1 : groups.get(currentGroup)) {
                        for (String term2 : groups.get(nextGroup)) {
                            String combined = combineTerms(term1, term2);
                            if (combined != null) {
                                foundNew = true;
                                newCombinedTerms.add(term1);
                                newCombinedTerms.add(term2);
                                int onesCount = countOnes(combined.replace('-', '1'));
                                newGroups.computeIfAbsent(onesCount, k -> new ArrayList<>())
                                        .add(combined);
                            }
                        }
                    }
                }
            }

            // Add uncombined terms as prime implicants
            for (List<String> groupTerms : groups.values()) {
                for (String term : groupTerms) {
                    if (!newCombinedTerms.contains(term) && !combinedTerms.contains(term)) {
                        primeImplicants.add(term);
                    }
                }
            }

            combinedTerms.addAll(newCombinedTerms);
            groups = newGroups;
        } while (foundNew);

        // Add any remaining terms from the final iteration
        for (List<String> groupTerms : groups.values()) {
            primeImplicants.addAll(groupTerms);
        }

        return new ArrayList<>(primeImplicants);
    }

    private static String combineTerms(String term1, String term2) {
        if (term1.length() != term2.length()) return null;

        StringBuilder combined = new StringBuilder();
        int differences = 0;

        for (int i = 0; i < term1.length(); i++) {
            if (term1.charAt(i) == term2.charAt(i)) {
                combined.append(term1.charAt(i));
            } else if (term1.charAt(i) != '-' && term2.charAt(i) != '-') {
                differences++;
                if (differences > 1) return null;
                combined.append('-');
            } else if (term1.charAt(i) != term2.charAt(i)) {
                return null;
            }
        }

        return differences == 1 ? combined.toString() : null;
    }

    // Step 3: Create prime implicant chart
    public static Map<String, Set<Integer>> createPrimeImplicantTable(
            List<String> primeImplicants, int[] minterms) {
        Map<String, Set<Integer>> table = new LinkedHashMap<>();

        for (String implicant : primeImplicants) {
            Set<Integer> coveredMinterms = new HashSet<>();
            List<String> possibleMinterms = generateMinterms(implicant);

            for (String possibleMinterm : possibleMinterms) {
                int mintermValue = Integer.parseInt(possibleMinterm, 2);
                if (Arrays.stream(minterms).anyMatch(x -> x == mintermValue)) {
                    coveredMinterms.add(mintermValue);
                }
            }

            table.put(implicant, coveredMinterms);
        }

        return table;
    }

    private static List<String> generateMinterms(String implicant) {
        List<String> minterms = new ArrayList<>();
        generateMintermsRecursive(implicant, 0, "", minterms);
        return minterms;
    }

    private static void generateMintermsRecursive(String implicant, int index,
                                                  String current, List<String> minterms) {
        if (index == implicant.length()) {
            minterms.add(current);
            return;
        }

        if (implicant.charAt(index) == '-') {
            generateMintermsRecursive(implicant, index + 1, current + "0", minterms);
            generateMintermsRecursive(implicant, index + 1, current + "1", minterms);
        } else {
            generateMintermsRecursive(implicant, index + 1,
                    current + implicant.charAt(index), minterms);
        }
    }

    // Step 4: Find essential prime implicants
    public static Set<String> findEssentialPrimeImplicants(
            Map<String, Set<Integer>> primeImplicantTable, int[] minterms) {
        Set<String> essentialImplicants = new LinkedHashSet<>();
        Set<Integer> coveredMinterms = new HashSet<>();

        // Find essential prime implicants (those that uniquely cover some minterm)
        boolean found;
        do {
            found = false;
            for (int minterm : minterms) {
                if (coveredMinterms.contains(minterm)) continue;

                String essentialImplicant = null;
                int coverage = 0;

                for (Map.Entry<String, Set<Integer>> entry : primeImplicantTable.entrySet()) {
                    if (entry.getValue().contains(minterm)) {
                        coverage++;
                        essentialImplicant = entry.getKey();
                        if (coverage > 1) break;
                    }
                }

                if (coverage == 1 && essentialImplicant != null) {
                    essentialImplicants.add(essentialImplicant);
                    coveredMinterms.addAll(primeImplicantTable.get(essentialImplicant));
                    found = true;
                }
            }
        } while (found);

        // Add necessary non-essential prime implicants
        while (coveredMinterms.size() < minterms.length) {
            String bestImplicant = null;
            int maxNewCovered = 0;

            for (Map.Entry<String, Set<Integer>> entry : primeImplicantTable.entrySet()) {
                if (!essentialImplicants.contains(entry.getKey())) {
                    Set<Integer> newCovered = new HashSet<>(entry.getValue());
                    newCovered.removeAll(coveredMinterms);

                    if (newCovered.size() > maxNewCovered) {
                        maxNewCovered = newCovered.size();
                        bestImplicant = entry.getKey();
                    }
                }
            }

            if (bestImplicant != null) {
                essentialImplicants.add(bestImplicant);
                coveredMinterms.addAll(primeImplicantTable.get(bestImplicant));
            } else {
                break;
            }
        }

        return essentialImplicants;
    }

    // Step 5: Convert to Boolean expression
    public static String constructBooleanExpression(Set<String> essentialImplicants,
                                                    String[] variables) {
        if (essentialImplicants.isEmpty()) {
            return "0";
        }

        List<String> terms = new ArrayList<>();
        for (String implicant : essentialImplicants) {
            StringBuilder term = new StringBuilder();
            for (int i = 0; i < implicant.length(); i++) {
                if (implicant.charAt(i) != '-') {
                    if (implicant.charAt(i) == '0') {
                        term.append(variables[i]).append("'");
                    } else {
                        term.append(variables[i]);
                    }
                }
            }
            terms.add(term.toString());
        }

        return String.join(" + ", terms);
    }

    // Main minimization method
    public static String minimize(int[] minterms, String[] variables) {
        if (minterms.length == 0) {
            return "0";
        }

        int bitLength = variables.length;

        // Validate minterms against bit length
        int maxPossibleValue = (1 << bitLength) - 1;
        for (int minterm : minterms) {
            if (minterm > maxPossibleValue) {
                throw new IllegalArgumentException(
                        "Minterm " + minterm + " exceeds maximum value for " +
                                bitLength + " variables");
            }
        }

        Map<Integer, List<String>> groups = groupMinterms(minterms, bitLength);
        List<String> primeImplicants = findPrimeImplicants(groups, bitLength);
        Map<String, Set<Integer>> primeImplicantTable =
                createPrimeImplicantTable(primeImplicants, minterms);
        Set<String> essentialImplicants =
                findEssentialPrimeImplicants(primeImplicantTable, minterms);
        return constructBooleanExpression(essentialImplicants, variables);
    }
}