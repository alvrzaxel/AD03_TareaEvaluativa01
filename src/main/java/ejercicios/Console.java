package ejercicios;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class Console {
    
    private static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    
    public static byte readByte() {
        try {
            return Byte.parseByte(in.readLine());
        } catch (IOException e) {
            System.err.println("Error al leer byte: " + e.getMessage());
            return 0; // valor predeterminado
        }
    }
    
    public static short readShort() {
        try {
            return Short.parseShort(in.readLine());
        } catch (IOException e) {
            System.err.println("Error al leer short: " + e.getMessage());
            return 0; // valor predeterminado
        }
    }
    
    public static int readInt() {
        try {
            return Integer.parseInt(in.readLine());
        } catch (IOException e) {
            System.err.println("Error al leer int: " + e.getMessage());
            return 0; // valor predeterminado
        }
    }
    
    // Similar para readLong, readFloat y readDouble...
    
    public static char readChar() {
        try {
            return (char) in.read();
        } catch (IOException e) {
            System.err.println("Error al leer char: " + e.getMessage());
            return '\0'; // valor predeterminado
        }
    }
    
    public static String readString() {
        try {
            return in.readLine();
        } catch (IOException e) {
            System.err.println("Error al leer String: " + e.getMessage());
            return ""; // valor predeterminado
        }
    }
}