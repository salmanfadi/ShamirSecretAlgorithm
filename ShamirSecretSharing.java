import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ShamirSecretSharing {
    
    static class Share {
        BigInteger x, y;
        
        Share(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }
    
    // Helper method to repeat a string (for Java 8 compatibility)
    public static String repeatString(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
    
    // Convert value from given base to decimal (BigInteger)
    public static BigInteger convertFromBase(String value, int base) {
        BigInteger result = BigInteger.ZERO;
        BigInteger baseBI = BigInteger.valueOf(base);
        
        for (int i = 0; i < value.length(); i++) {
            char digit = value.charAt(i);
            int digitValue;
            
            if (digit >= '0' && digit <= '9') {
                digitValue = digit - '0';
            } else if (digit >= 'a' && digit <= 'z') {
                digitValue = digit - 'a' + 10;
            } else if (digit >= 'A' && digit <= 'Z') {
                digitValue = digit - 'A' + 10;
            } else {
                throw new IllegalArgumentException("Invalid character in value: " + digit);
            }
            
            if (digitValue >= base) {
                throw new IllegalArgumentException("Digit " + digit + " is invalid for base " + base);
            }
            
            result = result.multiply(baseBI).add(BigInteger.valueOf(digitValue));
        }
        
        return result;
    }
    
    public static void main(String[] args) {
        // Handle both single file and multiple files
        if (args.length == 0) {
            System.err.println("Usage: java ShamirSecretSharing <input1.json> [input2.json] ...");
            return;
        }
        
        // Process each test case file
        for (int fileIndex = 0; fileIndex < args.length; fileIndex++) {
            String filename = args[fileIndex];
            System.out.println(repeatString("=", 60));
            System.out.println("Processing Test Case " + (fileIndex + 1) + ": " + filename);
            System.out.println(repeatString("=", 60));
            
            try {
                BigInteger secret = processTestCase(filename);
                System.out.println();
                System.out.println("SECRET FOR TEST CASE " + (fileIndex + 1) + ": " + secret);
                System.out.println();
                
            } catch (Exception e) {
                System.err.println("Error processing " + filename + ": " + e.getMessage());
            }
        }
    }
    
    private static BigInteger processTestCase(String filename) throws Exception {
        // Read and parse JSON file
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(filename));
        
        // Extract n and k from keys object
        JSONObject keys = (JSONObject) jsonObject.get("keys");
        long n = (Long) keys.get("n");
        long k = (Long) keys.get("k");
        
        System.out.println("Total shares (n): " + n);
        System.out.println("Threshold (k): " + k);
        System.out.println("Polynomial degree (m): " + (k - 1));
        System.out.println();
        
        // Parse shares
        List<Share> shares = new ArrayList<>();
        System.out.println("Decoding shares:");
        
        // Iterate through all keys in JSON to find share objects
        for (Object keyObj : jsonObject.keySet()) {
            String key = (String) keyObj;
            
            // Skip the "keys" object
            if (key.equals("keys")) {
                continue;
            }
            
            try {
                // Parse x coordinate from key
                BigInteger x = new BigInteger(key);
                
                // Get share object
                JSONObject shareObj = (JSONObject) jsonObject.get(key);
                String base = (String) shareObj.get("base");
                String value = (String) shareObj.get("value");
                
                // Convert y value from given base to decimal
                int baseInt = Integer.parseInt(base);
                BigInteger y = convertFromBase(value, baseInt);
                
                System.out.println("Point (" + x + ", " + y + ") -> Base " + base + " value: \"" + value + "\"");
                
                shares.add(new Share(x, y));
            } catch (NumberFormatException e) {
                // Skip non-numeric keys
                continue;
            }
        }
        
        System.out.println();
        System.out.println("Using first " + k + " shares for secret reconstruction:");
        
        if (shares.size() < k) {
            throw new RuntimeException("Not enough shares to reconstruct secret. Need at least " + k + " shares.");
        }
        
        // Use the first k shares for reconstruction (as per standard Shamir's Secret Sharing)
        List<Share> selectedShares = shares.subList(0, (int) k);
        System.out.println("Selected shares: " + selectedShares);
        
        // Calculate the secret using Lagrange interpolation
        BigInteger secret = lagrangeInterpolation(selectedShares);
        
        System.out.println("Reconstructed polynomial constant term (secret): " + secret);
        
        return secret;
    }
    
    private static List<List<Share>> generateCombinations(List<Share> shares, int k) {
        List<List<Share>> result = new ArrayList<>();
        generateCombinationsHelper(shares, k, 0, new ArrayList<>(), result);
        return result;
    }
    
    private static void generateCombinationsHelper(List<Share> shares, int k, int start, 
                                                  List<Share> current, List<List<Share>> result) {
        if (current.size() == k) {
            result.add(new ArrayList<>(current));
            return;
        }
        
        for (int i = start; i < shares.size(); i++) {
            current.add(shares.get(i));
            generateCombinationsHelper(shares, k, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }
    
    // Fraction class for exact arithmetic in Lagrange interpolation
    static class Fraction {
        BigInteger numerator;
        BigInteger denominator;
        
        Fraction(BigInteger numerator, BigInteger denominator) {
            if (denominator.equals(BigInteger.ZERO)) {
                throw new ArithmeticException("Division by zero");
            }
            
            // Normalize: ensure denominator is positive
            if (denominator.compareTo(BigInteger.ZERO) < 0) {
                numerator = numerator.negate();
                denominator = denominator.negate();
            }
            
            // Reduce to lowest terms
            BigInteger gcd = numerator.gcd(denominator);
            this.numerator = numerator.divide(gcd);
            this.denominator = denominator.divide(gcd);
        }
        
        Fraction add(Fraction other) {
            BigInteger newNum = this.numerator.multiply(other.denominator)
                               .add(other.numerator.multiply(this.denominator));
            BigInteger newDen = this.denominator.multiply(other.denominator);
            return new Fraction(newNum, newDen);
        }
        
        Fraction multiply(Fraction other) {
            return new Fraction(
                this.numerator.multiply(other.numerator),
                this.denominator.multiply(other.denominator)
            );
        }
        
        BigInteger toBigInteger() {
            if (!numerator.remainder(denominator).equals(BigInteger.ZERO)) {
                throw new ArithmeticException("Cannot convert fraction to integer: " + numerator + "/" + denominator);
            }
            return numerator.divide(denominator);
        }
        
        @Override
        public String toString() {
            return numerator + "/" + denominator;
        }
    }
    
    private static BigInteger lagrangeInterpolation(List<Share> shares) {
        Fraction result = new Fraction(BigInteger.ZERO, BigInteger.ONE);
        
        for (int i = 0; i < shares.size(); i++) {
            Share shareI = shares.get(i);
            Fraction basis = new Fraction(BigInteger.ONE, BigInteger.ONE);
            
            // Calculate Lagrange basis polynomial at x = 0
            for (int j = 0; j < shares.size(); j++) {
                if (i != j) {
                    Share shareJ = shares.get(j);
                    // For x = 0: (0 - x_j) / (x_i - x_j) = -x_j / (x_i - x_j)
                    Fraction factor = new Fraction(
                        shareJ.x.negate(),
                        shareI.x.subtract(shareJ.x)
                    );
                    basis = basis.multiply(factor);
                }
            }
            
            // Multiply by y_i and add to result
            Fraction term = basis.multiply(new Fraction(shareI.y, BigInteger.ONE));
            result = result.add(term);
        }
        
        return result.toBigInteger();
    }
}