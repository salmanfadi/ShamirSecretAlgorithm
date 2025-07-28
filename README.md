# Shamir's Secret Sharing Implementation

This Java application implements Shamir's Secret Sharing (SSS) scheme, a cryptographic algorithm that divides a secret into multiple parts (shares) that must be combined to reconstruct the original secret.

## Features

- **Secret Reconstruction**: Reconstructs a secret from a subset of shares using Lagrange interpolation
- **Multiple Base Support**: Handles secrets and shares in different numerical bases (2-36)
- **JSON Input**: Processes test cases defined in JSON format
- **Arbitrary Precision**: Uses `BigInteger` for handling very large numbers
- **Error Handling**: Validates inputs and provides meaningful error messages

## How It Works

### Key Concepts

- **Secret**: The original data to be protected
- **Shares**: Parts of the secret that can be distributed
- **Threshold (k)**: Minimum number of shares required to reconstruct the secret
- **Total Shares (n)**: Total number of shares created
- **Polynomial**: A mathematical function of degree (k-1) used to generate shares

### Algorithm

1. **Share Generation** (not implemented in this file):
   - A random polynomial of degree (k-1) is created
   - The constant term is the secret
   - n points on the polynomial are generated as shares

2. **Secret Reconstruction**:
   - Takes at least k shares
   - Uses Lagrange interpolation to find the polynomial
   - The constant term of the polynomial is the reconstructed secret

## Input Format

The program accepts JSON files with the following structure:

```json
{
  "keys": {
    "n": 5,
    "k": 3
  },
  "1": {
    "base": "10",
    "value": "1234"
  },
  "2": {
    "base": "16",
    "value": "4d2"
  },
  "3": {
    "base": "2",
    "value": "10011010010"
  }
  // ... more shares
}
```

### Fields:
- `keys.n`: Total number of shares (n)
- `keys.k`: Threshold (minimum shares needed, k)
- Each share has:
  - Key: x-coordinate (as a string)
  - Value: Object with `base` and `value`
    - `base`: Numerical base of the value (2-36)
    - `value`: The y-coordinate in the specified base

## Usage

1. Compile the Java file:
   ```bash
   javac -cp "path/to/json-simple-1.1.1.jar" ShamirSecretSharing.java
   ```

2. Run the program with one or more test case files:
   ```bash
   java -cp ".;path/to/json-simple-1.1.1.jar" ShamirSecretSharing testcase1.json testcase2.json
   ```
   (Use `:` instead of `;` on Unix-based systems)

## Dependencies

- `json-simple-1.1.1.jar` (or later) for JSON parsing

## Implementation Details

### Main Classes and Methods

- **ShamirSecretSharing**: Main class containing the implementation
- **Share**: Inner class representing a single share (x, y coordinate)
- **Fraction**: Inner class for exact arithmetic in Lagrange interpolation

### Key Methods

- `convertFromBase(String value, int base)`: Converts a number from any base to decimal
- `lagrangeInterpolation(List<Share> shares)`: Reconstructs the secret using Lagrange interpolation
- `processTestCase(String filename)`: Processes a single test case file
- `main(String[] args)`: Entry point that handles multiple test case files

## Example

Given a test case with k=3 and shares:
- (1, 1234) in base 10
- (2, 1238) in base 10
- (3, 1246) in base 10

The program will output the reconstructed secret (1234 in this case).

## Notes

- The implementation uses exact arithmetic with fractions to avoid floating-point inaccuracies
- Only the first k shares are used for reconstruction (as per standard SSS)
- The code includes input validation and error handling for common issues

## License

This code is provided as-is for educational purposes. You're free to use and modify it according to your needs.
