/**************************************
 * Autor: Axel Álvarez Santos
 * Fecha: 25/11/2024
 * Tarea: AD03 Tarea Evaluativa 01
 **************************************/

package ejercicios;

import java.sql.*;

public class UD03TareaEvaluativaEjercicio1 {
    
    // Configuración para la conexión a la base de datos
    private static final String CONNECTION_URL = "jdbc:mysql://localhost:3306/dbeventos";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "secret";
    
    public static void main(String[] args) {
        // Establece la conexión con la base de datos y ejecuta el flujo principal
        try(Connection conn = DriverManager.getConnection(CONNECTION_URL, USERNAME, PASSWORD)) {
            System.out.println("Conexión establecida con éxito");
            printHeader();
            fetchAndPrintEventsData(conn);
            
        } catch (Exception e) {
            System.out.println("Error al conectar el servidor");
        }
    }
    
    /**
     * Imprime la cabecera de la tabla con una separación
     */
    private static void printHeader() {
        // Definimos el ancho total de la tabla
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
        String eventsQuery = "SELECT * FROM eventos ORDER BY nombre_evento DESC";
        
        try(PreparedStatement stmt = conn.prepareStatement(eventsQuery)) {
            ResultSet rs = stmt.executeQuery(eventsQuery);
            
            // Recorre los resultados de la consulta
            while(rs.next()) {
                int idEvent = rs.getInt("id_evento");
                String eventName = rs.getString("nombre_evento");
                int idLocation = rs.getInt("id_ubicacion");
                
                // Imprime la información del evento
                printEventLine(conn, idEvent, eventName, idLocation);
            }
            
        } catch (Exception e) {
            System.out.println("Error al obtener los eventos: " + e.getMessage());
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
        String asistentesQuery = "SELECT COUNT(*) FROM asistentes_eventos WHERE id_evento = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(asistentesQuery)) {
            // Establece el parámetro id_evento
            stmt.setInt(1, idEvent);
            
            // Ejecuta la consulta
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            System.out.println("Error al obtener asistentes: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Obtiene los detalles de la ubicación de un evento
     * @param conn Conexión a la base de datos
     * @param idLocation ID de la ubicación
     * @return Array de String con el nombre y la dirección de la ubicación
     */
    private static String[] getEventLocation(Connection conn, int idLocation) {
        String[] location = new String[2];
        String asistentesQuery = "SELECT * FROM ubicaciones WHERE id_ubicacion = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement((asistentesQuery))) {
            // Establece el parámetro de la consulta
            stmt.setInt(1, idLocation);
            
            // Ejecuta la consulta
            ResultSet rs = stmt.executeQuery();
            
            // Almacena el nombre y la dirección del evento
            if (rs.next()) {
                location[0] = rs.getString("nombre");
                location[1] = rs.getString("direccion");
            }
        } catch (Exception e) {
            System.out.println("Error al obtener la ubicación: " + e.getMessage());
        }
        
        // Devuelve el array con el nombre y la dirección del evento
        return  location;
    }
}

