/**************************************
 * Autor: Axel Álvarez Santos
 * Fecha: 28/11/2024
 * Tarea: AD03 Tarea Evaluativa 01 Ejercicio 4
 **************************************/

package ejercicios;

import java.sql.*;

/**
 * Interactúa con la base de datos para gestionar eventos
 * Permite listar eventos, consultar asistentes y verificar/crear funciones almacenadas
 */
public class UD03TareaEvaluativaEjercicio4 {
    
    // Configuración para la conexión a la base de datos
    private static final String CONNECTION_URL = "jdbc:mysql://localhost:3306/dbeventos";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "secret";
    private static final String FUNCTION_CALL = "{? = CALL obtener_numero_asistentes(?)}"; // Llamada a la función
    
    // Punto de entrada principal
    public static void main(String[] args) {
        String functionName = "obtener_numero_asistentes";
        
        // Conexión a la base de datos y flujo general del programa
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL, USERNAME, PASSWORD)) {
            
            // Verifica y crea la función almacenada obtener_numero_asistentes si no existe
            checkFunction(conn, functionName);
            
            // Imprime por consola la lista de eventos
            printListEvents(conn);
            
            // Permite seleccionar un evento y muestra el número de asistentes
            selectEventAndPrintAttendees(conn);
            
        } catch (SQLException es) {
            muestraErrorSQL(es);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
    
    /**
     * Verifica si la función con el nombre proporcionado existe en la base de datos
     * Si no existe, la crea
     * @param conn Conexión a la base de datos
     * @param functionName El nombre de la función a verificar
     */
    private static void checkFunction(Connection conn, String functionName) {
        // Elimina la función obtener_numero_asistentes si ya existe (solo para depuración)
        //dropFunctionIfExists(conn, functionName);
        String query = """
                SELECT COUNT(*) FROM information_schema.ROUTINES
                WHERE ROUTINE_SCHEMA = DATABASE() AND ROUTINE_NAME = ? AND ROUTINE_TYPE = 'FUNCTION'
                """;
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, functionName);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("La función " + functionName + " ya existe en la base de datos.");
                    
                } else {
                    createFunction(conn, functionName);
                }
            }
            
        } catch (SQLException es) {
            muestraErrorSQL(es);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
    
    /**
     * Crea la función almacenada obtener_numero_asistentes en la base de datos
     * @param conn Conexión a la base de datos
     */
    private static void createFunction(Connection conn, String functionName) {
        // La función cuenta los asistentes de un evento específico
        String createFunctionSQL = "CREATE DEFINER=`root`@`localhost` FUNCTION `"
                + functionName
                + "`(id_evento_param INT) "
                + "RETURNS INT "
                + "DETERMINISTIC "
                + "BEGIN "
                + "DECLARE total_asistentes INT DEFAULT 0; "
                + "SELECT COUNT(*) INTO total_asistentes "
                + "FROM asistentes_eventos "
                + "WHERE id_evento = id_evento_param; "
                + "RETURN total_asistentes; "
                + "END";
        
        try (Statement stmt = conn.createStatement()) {
            // Ejecuta la sentencia SQL
            stmt.execute(createFunctionSQL);
            System.out.println("La función " + functionName + " ha sido creada.");
        } catch (SQLException es) {
            muestraErrorSQL(es);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
    
    /**
     * Muestra una lista de eventos disponibles en la base de datos
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
     * Permite al usuario seleccionar un evento por su ID y muestra el número de asistentes
     * Ejecuta la función almacenada obtener_numero_asistentes para mostrar el resultado
     * @param conn Conexión a la base de datos
     */
    private static void selectEventAndPrintAttendees(Connection conn) {
        System.out.println("\nIntroduce el ID del evento para consultar la cantidad de asistentes:");
        int selectedEvent = Console.readInt();
        
        // Registra el parámetro de salida y asigna el de entrada para recibir el número de asistentes
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
     * Elimina la función obtener_numero_asistentes si ya existe
     * Esta función **solo debe usarse en un entorno de depuración**
     * Elimina la función existente para que pueda ser recreada si se requiere de una nueva definición
     * @param conn Conexión a la base de datos
     */
    private static void dropFunctionIfExists(Connection conn, String functionName) {
        // Concatenación del nombre de la función en la consulta SQL
        String dropFunctionSQL = "DROP FUNCTION IF EXISTS " + functionName;
        try (Statement stmt = conn.createStatement()) {
            //Ejecuta la sentencia
            stmt.execute(dropFunctionSQL);
            System.out.println("La función " + functionName + " ha sido eliminada si existía.");
            
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

