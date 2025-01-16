import org.json.JSONObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ShamirSecretSharing {

    // Helper class to represent a point (x, y) on the polynomial
    static class Point {
        BigInteger x;
        BigInteger y;

        Point(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }

    public static void main(String[] args) throws IOException {
        // Load and solve for two test cases
        JSONObject testCase1 = loadJSONFile("testcase1.json");
        JSONObject testCase2 = loadJSONFile("testcase2.json");

        // Compute and print the secret (constant term) for each test case
        System.out.println("Secret for Test Case 1: " + computeSecret(testCase1));
        System.out.println("Secret for Test Case 2: " + computeSecret(testCase2));
    }

    // Reads a JSON file and converts it to a JSONObject
    private static JSONObject loadJSONFile(String filePath) throws IOException {
        String fileContent = new String(Files.readAllBytes(Paths.get(filePath)));
        return new JSONObject(fileContent);
    }

    // Decodes a string value based on the given base (e.g., binary, hexadecimal)
    private static BigInteger decodeValue(String value, String base) {
        return new BigInteger(value, Integer.parseInt(base));
    }

    // Extracts points and computes the constant term using Lagrange interpolation
    private static BigInteger computeSecret(JSONObject testCase) {
        JSONObject keys = testCase.getJSONObject("keys");
        int requiredPoints = keys.getInt("k");
        List<Point> points = new ArrayList<>();

        // Collect the first 'k' points from the test case
        for (int i = 1; i <= requiredPoints; i++) {
            String keyIndex = String.valueOf(i);
            if (!testCase.has(keyIndex)) continue;

            JSONObject pointData = testCase.getJSONObject(keyIndex);
            BigInteger x = BigInteger.valueOf(i);
            BigInteger y = decodeValue(pointData.getString("value"), pointData.getString("base"));

            points.add(new Point(x, y));
        }

        // Compute the secret using Lagrange interpolation
        return lagrangeInterpolationAtZero(points);
    }

    // Applies Lagrange interpolation to find the constant term (secret) at x = 0
    private static BigInteger lagrangeInterpolationAtZero(List<Point> points) {
        BigInteger secret = BigInteger.ZERO;
        int totalPoints = points.size();

        for (int i = 0; i < totalPoints; i++) {
            BigInteger numerator = points.get(i).y;
            BigInteger denominator = BigInteger.ONE;

            for (int j = 0; j < totalPoints; j++) {
                if (i != j) {
                    // Multiply by (-x_j) for the numerator and (x_i - x_j) for the denominator
                    numerator = numerator.multiply(points.get(j).x.negate());
                    denominator = denominator.multiply(points.get(i).x.subtract(points.get(j).x));
                }
            }

            // Adjust signs if the denominator is negative to keep the result positive
            if (denominator.signum() < 0) {
                numerator = numerator.negate();
                denominator = denominator.negate();
            }

            // Add the current term to the secret
            secret = secret.add(numerator.divide(denominator));
        }

        return secret;
    }
}
