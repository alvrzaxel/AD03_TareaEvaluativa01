/**************************************
 * Autor: Axel Álvarez Santos
 * Fecha: 28/11/2024
 * Tarea: AD03 Tarea Evaluativa
 **************************************/

package ejercicios;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

/*
 * Clase para gestionar las entradas del usuario por teclado
 */
public class Console {
    
    // Lectura de un número entero válido
    public static int readInt() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int x = 0;
        boolean valid = false;
        
        while (!valid) {
            try {
                x = Integer.parseInt(in.readLine());
                valid = true;
            } catch (NumberFormatException e) {
                System.out.println("Introduzca un numero");
            } catch (IOException e) {
                System.out.println("Error de entrada/salida: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Error inesperado: " + e.getMessage());
            }
        }
        return x;
    }
    
    // Lectura de una cadena válida
    public static String readString() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String str = null;
        boolean valid = false;
        
        while (!valid) {
            try {
                str = in.readLine();
                valid = true;
                
            } catch (IOException e) {
                System.out.println("Error de entrada/salida: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Error inesperado: " + e.getMessage());
            }
        }
        return str;
    }
}