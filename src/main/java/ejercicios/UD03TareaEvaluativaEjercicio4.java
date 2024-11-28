/**************************************
 * Autor: Axel Álvarez Santos
 * Fecha: 28/11/2024
 * Tarea: AD03 Tarea Evaluativa 01
 **************************************/

package ejercicios;

import java.sql.*;

public class UD03TareaEvaluativaEjercicio4 {
    
    // Configuración para la conexión a la base de datos
    private static final String CONNECTION_URL = "jdbc:mysql://localhost:3306/dbeventos";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "secret";
    private static final String FUNCTION_CALL = "{? = CALL obtener_numero_asistentes(?)}"; // Llamada a la función
    
    // Punto de entrada principal
    public static void main(String[] args) {
        
        // Conexión a la base de datos y flujo general del programa
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL, USERNAME, PASSWORD)) {
            
            // Crea la función almacenada obtener_numero_asistentes si no existe
            createNumberAttendeesFunction(conn);
            
            // Imprime por consola la lista de eventos
            printListEvents(conn);
            
            // Permite seleccionar un evento e imprime el total de asistentes
            selectEventAndPrintAttendees(conn);
            
        } catch (SQLException es) {
            muestraErrorSQL(es);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
    
    /**
     * Muestra una lista de eventos disponibles en la base de datos
     * Realiza una consulta para obtener los eventos y sus IDs
     * @param conn Conexión a la base de datos
     */
    private static void printListEvents(Connection conn) {
        String query = "SELECT id_evento, nombre_evento FROM eventos";
        
        System.out.println("Lista de eventos:");
        try (
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()
        ) {
            
            // Itera sobre los resultados e imprime cada evento
            while (rs.next()) {
                int idEvent = rs.getInt("id_evento");
                String nameEvent = rs.getString("nombre_evento");
                
                // Imprime el ID y el nombre del evento
                System.out.println(idEvent + ". " + nameEvent);
            }
            
        } catch (SQLException es) {
            muestraErrorSQL(es);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
    
    /**
     * Permite al usuario seleccionar un evento por su ID e imprime el número de asistentes
     * Ejecuta la función almacenada obtener_numero_asistentes y muestra el resultado
     * @param conn Conexión a la base de datos
     */
    private static void selectEventAndPrintAttendees(Connection conn) {
        System.out.println("\nIntroduce el ID del evento para consultar la cantidad de asistentes");
        int selectedEvent = Console.readInt();
        
        // Registra el parámetro de salida y el de entrada para recibir el número de asistentes
        try (CallableStatement stmt = conn.prepareCall(FUNCTION_CALL)) {
                stmt.registerOutParameter(1, Types.INTEGER);
                stmt.setInt(2, selectedEvent);
                stmt.execute();
                
                // Obtiene el número de asistentes desde el parámetro de salida
                int numAttendes = stmt.getInt(1);
                System.out.println("El número de asistentes para el evento seleccionado es: " + numAttendes);
            
        } catch (SQLException es) {
            muestraErrorSQL(es);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
    
    /**
     * Verifica si la función obtener_numero_asistentes existe en la base de datos
     * Si no existe, la crea
     * @param conn Conexión a la base de datos
     */
    private static void createNumberAttendeesFunction(Connection conn) {
        
        // Elimina la función obtener_numero_asistentes si ya existe (para depuración)
        //dropFunctionIfExists(conn);
        
        // Si la función no existe, la crea
        if (!doesFunctionExist(conn, "obtener_numero_asistentes")) {
            System.out.println("La función no existe. Creándola...");
            createFunction(conn);
            System.out.println("La función ha sido creada.");
        } else {
            System.out.println("La función ya existe en la base de datos.");
        }
    }
    
    /**
     * Verifica si la función existe en la base de datos
     * @param conn Conexión a la base de datos
     * @param functionName Nombre de la función que se desea verificar
     * @return true si la función existe, false en caso contrario
     */
    private static boolean doesFunctionExist(Connection conn, String functionName) {
        String query = """
                SELECT COUNT(*) FROM information_schema.ROUTINES
                WHERE ROUTINE_SCHEMA = DATABASE() AND ROUTINE_NAME = ? AND ROUTINE_TYPE = 'FUNCTION'
                """;
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, functionName);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException es) {
            muestraErrorSQL(es);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        
        return false;
    }
    
    /**
     * Crea la función almacenada obtener_numero_asistentes en la base de datos
     * La función cuenta los asistentes de un evento específico
     * @param conn Conexión a la base de datos
     */
    private static void createFunction(Connection conn) {
        String createFunctionSQL = """
            CREATE DEFINER=`root`@`localhost` FUNCTION `obtener_numero_asistentes`(id_evento_param INT)
                   RETURNS INT
                   DETERMINISTIC
                   BEGIN
                   DECLARE total_asistentes INT DEFAULT 0;
                           SELECT COUNT(*) INTO total_asistentes
                                           FROM asistentes_eventos
                                           WHERE id_evento = id_evento_param;
            RETURN total_asistentes;
            END
            """;
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createFunctionSQL);
            
        } catch (SQLException es) {
            muestraErrorSQL(es);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
    
    /**
     * Elimina la función obtener_numero_asistentes si ya existe
     * Esto es útil para asegurarse de que la función sea recreada si hay algún cambio
     * @param conn Conexión a la base de datos
     */
    private static void dropFunctionIfExists(Connection conn) {
        String dropFunctionSQL = "DROP FUNCTION IF EXISTS obtener_numero_asistentes";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(dropFunctionSQL);
            System.out.println("La función obtener_numero_asistentes ha sido eliminada si existía.");
        } catch (SQLException e) {
            System.err.println("Error al eliminar la función: " + e.getMessage());
        }
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
