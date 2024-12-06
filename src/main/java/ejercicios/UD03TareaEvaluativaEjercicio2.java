/**************************************************
 * Autor: Axel Álvarez Santos
 * Fecha: 26/11/2024
 * Tarea: AD03 Tarea Evaluativa 01 Ejercicio 2
 **************************************************/

package ejercicios;

import java.sql.*;

/**
 * Gestiona la conexión a la base de datos y permite modificar la capacidad máxima de una ubicación
 * Interactúa con el usuario para obtener la ubicación y la nueva capacidad, y actualiza la base de datos
 */
public class UD03TareaEvaluativaEjercicio2 {
    
    // Configuración para la conexión a la base de datos
    private static final String CONNECTION_URL = "jdbc:mysql://localhost:3306/dbeventos";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "secret";
    
    // Punto de entrada principal
    public static void main(String[] args) {
        String location = "";
        
        // Establece la conexión con la base de datos
        try(Connection conn = DriverManager.getConnection(CONNECTION_URL, USERNAME, PASSWORD)) {
            
            // Obtiene la ubicación
            location = fetchLocation(conn);
            
            // Muestra la capacidad actual de la ubicación
            printTotalCapacity(conn, location);
            
            // Modifica la capacidad de la ubicación
            modifyCapacity(conn, location);
            
        } catch (SQLException es) {
            muestraErrorSQL(es);
        } catch (Exception e) {
            e.printStackTrace(System.err); // Imprime detalles de error genérico
        }
    }
    
    /**
     * Obtiene el nombre de una ubicación en la base de datos e imprime su capacidad
     * Solicita al usuario que introduzca el nombre de la ubicación y valida si existe
     * @param conn Conexión a la base de datos
     * @return Nombre de la ubicación encontrada
     */
    private static String fetchLocation(Connection conn) {
        boolean locationFound = false;
        String inputLocation = "";
        
        // Bucle para seguir pidiendo una ubicación si no se encuentra
        do {
            System.out.print("Introduce el nombre de la ubicación: ");
            inputLocation = Console.readString().trim();
            
            // Verifica si la ubicación existe en la base de datos
            locationFound = checkLocationExists(conn, inputLocation);
            
            if (!locationFound) {
                System.out.println("No se encontraron ubicaciones con el nombre: '" + inputLocation + "'");
            }
            
        } while (!locationFound);
        
        // Retorna la ubicación si se encuentra
        return inputLocation;
    }
    
    /**
     * Verifica si una ubicación existe en la base de datos
     * Realiza una consulta SQL para comprobar si hay una ubicación con el nombre dado
     * @param conn Conexión a la base de datos
     * @param inputLocation Nombre de la ubicación para verificar
     * @return true si la ubicación existe, false si no existe
     */
    private static boolean checkLocationExists(Connection conn, String inputLocation) {
        // Consulta SQL para verificar la existencia de la ubicación dada
        String query = "SELECT 1 FROM ubicaciones WHERE nombre = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            // Establece el parámetro de la consulta
            stmt.setString(1, inputLocation);
            
            // Ejecuta la consulta y verifica si se encontró algún resultado
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
            
        } catch (SQLException es) {
            muestraErrorSQL(es);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        
        return false;
    }
    
    /**
     * Obtiene y muestra la capacidad actual de una ubicación desde la base de datos
     * @param conn Conexión a la base de datos
     * @param inputLocation Nombre de la ubicación
     */
    private static void printTotalCapacity(Connection conn, String inputLocation) {
        // Consulta SQL para obtener la capacidad de la ubicación
        String query = "SELECT capacidad FROM ubicaciones WHERE nombre = ?";
        int totalCapacity = 0;
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            // Establece el parámetro de la consulta
            stmt.setString(1, inputLocation);
            
            // Ejecuta la consulta y obtiene la capacidad de la ubicación
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    totalCapacity = rs.getInt("capacidad");
                }
            }
            
        } catch (SQLException es) {
            muestraErrorSQL(es);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        
        // Muestra la capacidad actual al usuario
        System.out.println("La capacidad actual de la ubicación '" + inputLocation + "' es: " + totalCapacity);
    }
    
    /**
     * Gestiona la interacción con el usuario, verifica si la ubicación existe
     * en la base de datos y permite modificar su capacidad
     * @param conn Conexión a la base de datos
     */
    private static void modifyCapacity(Connection conn, String location) {
        // Variables para verificar la actualización
        boolean rowsUpdated = false;
        
        // Solicita al usuario la nueva capacidad máxima
        System.out.print("Introduce la nueva capacidad máxima: ");
        int inputTotalCapacity = Console.readInt();
        
        // Actualiza la capacidad máxima de la ubicación
        rowsUpdated = setTotalCapacity(conn, location, inputTotalCapacity);
        
        // Muestra un mensaje con el resultado de la actualización
        if (rowsUpdated) {
            System.out.println("Capacidad actualizada correctamente.");
            System.out.println();
        } else {
            System.out.println("No se encontraron coincidencias para actualizar");
        }
    }
    
    /**
     * Actualiza la capacidad máxima de una ubicación de la base de datos
     * @param conn Conexión a la base de datos
     * @param inputLocation Nombre de la ubicación
     * @param inputTotalCapacity Nueva capacidad total
     */
    private static boolean setTotalCapacity(Connection conn, String inputLocation, int inputTotalCapacity) {
        // Consulta SQL para actualizar la capacidad de una ubicación
        String query = "UPDATE ubicaciones SET capacidad = ? WHERE nombre = ?";
        int rowsUpdated = 0;
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            // Establece los parámetros de la consulta
            stmt.setInt(1, inputTotalCapacity);
            stmt.setString(2, inputLocation);
            
            // Ejecuta la actualización y obtiene el número de filas afectadas
            rowsUpdated = stmt.executeUpdate();
            
        } catch (SQLException es) {
            muestraErrorSQL(es);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        
        // Retorna true si se actualizó al menos una fila
        return rowsUpdated > 0;
    }
    
    /**
     * Muestra los detalles de un error SQL en la consola
     * Imprime información detallada del error para ayudar a depurar
     * @param es Objeto SQLException con los detalles del error
     */
    public static void muestraErrorSQL(SQLException es) {
        System.err.println("SQL ERROR mensaje: " + es.getMessage());
        System.err.println("SQL Estado: " + es.getSQLState());
        System.err.println("SQL código específico: " + es.getErrorCode());
    }
}
