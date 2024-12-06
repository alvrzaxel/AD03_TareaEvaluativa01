/**************************************************
 * Autor: Axel Álvarez Santos
 * Fecha: 25/11/2024
 * Tarea: AD03 Tarea Evaluativa 01 Ejercicio 1
 **************************************************/

package ejercicios;

import java.sql.*;

/**
 * Conecta a una base de datos MySQL que contiene información sobre eventos y asistentes
 * Imprime una lista de eventos, mostrando su nombre, el número de asistentes, la ubicación y su dirección
 */
public class UD03TareaEvaluativaEjercicio1 {
    
    // Configuración para la conexión a la base de datos
    private static final String CONNECTION_URL = "jdbc:mysql://localhost:3306/dbeventos";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "secret";
    
    // Punto de entrada principal
    public static void main(String[] args) {
        
        // Establece la conexión con la base de datos y ejecuta el flujo principal
        try(Connection conn = DriverManager.getConnection(CONNECTION_URL, USERNAME, PASSWORD)) {
            
            // Imprime la cabecera de la tabla
            printHeader();
            
            // Busca e imprime la información de los eventos
            fetchAndPrintEventsData(conn);
            
        } catch (SQLException se) {
            muestraErrorSQL(se);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
    
    /**
     * Imprime la cabecera de la tabla con una separación
     */
    private static void printHeader() {
        // Ancho total de la tabla
        int totalWidth = 120;
        String separator = "-".repeat(totalWidth);
        
        System.out.printf("%-32s %-12s %-36s %-30s%n",
                "Eventos", "| Asistentes", "| Ubicación", "| Dirección");
        System.out.println(separator);
    }
    
    /**
     * Obtiene los eventos de la base de datos y los imprime en una tabla
     * @param conn Conexión a la base de datos
     */
    private static void fetchAndPrintEventsData(Connection conn) {
        String query = "SELECT * FROM eventos ORDER BY nombre_evento DESC";
        
        try (
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()
        ) {
            // Control para informar si no hay datos
            boolean hasData = false;
            
            // Recorre los resultados de la consulta
            while(rs.next()) {
                hasData = true;
                int idEvent = rs.getInt("id_evento");
                String eventName = rs.getString("nombre_evento");
                int idLocation = rs.getInt("id_ubicacion");
                
                // Imprime la información del evento
                printEventLine(conn, idEvent, eventName, idLocation);
            }
            
            // Si no se encuentran datos
            if(!hasData) {
                System.out.println("No se encontraron eventos en la base de datos");
            }
            
        } catch (SQLException se) {
            muestraErrorSQL(se);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
    
    /**
     * Imprime la información de un evento formateada en una línea
     * @param conn Conexión a la base de datos
     * @param idEvent ID del evento
     * @param eventName Nombre del evento
     * @param idLocation ID de la ubicación asociada al evento
     */
    private static void printEventLine(Connection conn, int idEvent, String eventName, int idLocation) {
        // Obtiene el número de asistentes al evento
        int asistentes = getEventAttendees(conn, idEvent);
        
        // Obtiene los detalles de la ubicación
        String[] location = getEventLocation(conn, idLocation);
        
        // Imprime una línea con los datos del evento
        System.out.printf("%-32s %-12s %-36s %-30s%n",
                eventName, "| " + asistentes, "| " + location[0], "| " + location[1]);
    }
    
    /**
     * Obtiene el número de asistentes a un evento
     * @param conn Conexión a la base de datos
     * @param idEvent ID del evento
     * @return Número de asistentes al evento
     */
    private static int getEventAttendees(Connection conn, int idEvent) {
        String query = "SELECT COUNT(*) FROM asistentes_eventos WHERE id_evento = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            // Establece el parámetro de la consulta
            stmt.setInt(1, idEvent);
            
            // Ejecuta la consulta
            try (ResultSet rs = stmt.executeQuery()) {
                if(rs.next()) {
                    return rs.getInt(1);
                } else {
                    System.err.println("No se encontraron datos para el ID proporcionado: " + idEvent);
                }
            }
        } catch (SQLException se) {
            muestraErrorSQL(se);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return 0;
    }
    
    /**
     * Obtiene el nombre y la dirección de la ubicación de un evento
     * @param conn Conexión a la base de datos
     * @param idLocation ID de la ubicación
     * @return Array de String con el nombre y la dirección de la ubicación
     */
    private static String[] getEventLocation(Connection conn, int idLocation) {
        String[] location = new String[2];
        String query = "SELECT * FROM ubicaciones WHERE id_ubicacion = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement((query))) {
            // Establece el parámetro de la consulta
            stmt.setInt(1, idLocation);
            
            // Ejecuta la consulta
            try (ResultSet rs = stmt.executeQuery()) {
                // Almacena el nombre y la dirección del evento
                if (rs.next()) {
                    location[0] = rs.getString("nombre");
                    location[1] = rs.getString("direccion");
                } else {
                    System.err.println("No se encontraron datos para el ID proporcionado: " + idLocation);
                }
            }
        } catch (SQLException se) {
            muestraErrorSQL(se);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        
        // Devuelve el array con el nombre y la dirección de la ubicación del evento
        return  location;
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

