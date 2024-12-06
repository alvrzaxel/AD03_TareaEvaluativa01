/**************************************************
 * Autor: Axel Álvarez Santos
 * Fecha: 27/11/2024
 * Tarea: AD03 Tarea Evaluativa 01 Ejercicio 3
 **************************************************/

package ejercicios;

import java.sql.*;

/**
 * Gestiona el proceso de registro de un asistente en un evento
 * Permite al usuario ingresar su DNI, valida si ya está registrado en la base de datos y, si no lo está,
 * registra su información. Luego muestra una lista de eventos disponibles y permite al usuario registrarse
 */
public class UD03TareaEvaluativaEjercicio3 {
    
    // Configuración para la conexión a la base de datos
    private static final String CONNECTION_URL = "jdbc:mysql://localhost:3306/dbeventos";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "secret";
    
    // Punto de entrada principal
    public static void main(String[] args) {
        
        // Conexión a la base de datos y flujo general del programa
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL, USERNAME, PASSWORD)) {
            
            // Obtiene o registra el asistente si no existe
            String attendeeDni = getOrRegisterAttendee(conn);
            
            // Muestra la lista de eventos
            printEventList(conn);
            
            // Solicita y valida el evento seleccionado por el usuario
            int selectedEventId = selectAndValidateEvent(conn);
            
            // Registra al asistente en el evento seleccionado
            registerAttendeeToEvent(conn, attendeeDni, selectedEventId);
            
        } catch (SQLException es) {
            showSQLError(es);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
    
    /**
     * Solicita el DNI del asistente y verifica si ya está registrado
     * Si no está registrado, solicita el nombre y lo añade a la base de datos
     * @param conn Conexión a la base de datos
     * @return DNI del asistente
     */
    private static String getOrRegisterAttendee(Connection conn) {
        String attendeeDni;
        boolean isDniValid = false;
        
        // Bucle para solicitar el DNI hasta que sea válido
        do {
            System.out.println("Introduce el DNI del asistente:");
            attendeeDni = Console.readString().toUpperCase();
            isDniValid = validateDniFormat(attendeeDni);
            
            if (!isDniValid) {
                System.out.println("El DNI introducido no es válido. Inténtalo de nuevo.");
            }
        } while (!isDniValid);
        
        // Intenta buscar al asistente en la base de datos
        String attendeeName = findAttendeeNameByDni(conn, attendeeDni);
        
        // Si no lo encuentra, solicita los datos y lo registra
        if (attendeeName == null) {
            System.out.println("No se encontró un asistente con el DNI proporcionado.");
            System.out.println("Introduce el nombre del asistente:");
            attendeeName = Console.readString();
            registerNewAttendee(conn, attendeeDni, attendeeName);
        }
        
        System.out.println("Estás realizando la reserva para: " + attendeeName);
        return attendeeDni;
    }
    
    /**
     * Validación del formato del DNI introducido
     * El formato válido es 8 dígitos seguidos de una letra mayúscula
     * @param dni DNI a validar
     * @return true si el formato es correcto, false en caso contrario
     */
    private static boolean validateDniFormat(String dni) {
        return dni != null && dni.matches("\\d{8}[A-Z]");
    }
    
    /**
     * Busca el nombre de un asistente en la base de datos por su DNI
     * @param conn Conexión a la base de datos
     * @param dni DNI del asistente
     * @return El nombre del asistente si existe, null si no está registrado
     */
    private static String findAttendeeNameByDni(Connection conn, String dni) {
        String query = "SELECT nombre FROM asistentes WHERE dni = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, dni);
            
            // Si encuentra al asistente, devuelve el nombre
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nombre");
                }
            }
            
        } catch (SQLException es) {
            showSQLError(es);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        
        return null; // Si no lo encuentra
    }
    
    /**
     * Inserta un nuevo asistente en la base de datos
     * @param conn Conexión a la base de datos
     * @param dni DNI del asistente
     * @param name Nombre del asistente
     */
    private static void registerNewAttendee(Connection conn, String dni, String name) {
        String query = "INSERT INTO asistentes (dni, nombre) VALUES (?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            // Establece los parámetros de la consulta y la ejecuta
            stmt.setString(1, dni);
            stmt.setString(2, name);
            stmt.executeUpdate();
            
        } catch (SQLException es) {
            showSQLError(es);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
    
    /**
     * Muestra la lista de eventos junto con los espacios disponibles
     * @param conn Conexión a la base de datos
     */
    private static void printEventList(Connection conn) {
        String query = "SELECT id_evento, nombre_evento, id_ubicacion FROM eventos";
        
        try (
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()
        ) {
            // Itera en cada evento para mostrar los detalles
            System.out.println("Lista de eventos:");
            while (rs.next()) {
                int eventId = rs.getInt("id_evento");
                String eventName = rs.getString("nombre_evento");
                int locationId = rs.getInt("id_ubicacion");
                int availableSpaces = calculateAvailableSpaces(conn, eventId, locationId);
                
                // Muestra los detalles del evento, incluyendo los espacios disponible
                System.out.println(eventId + ". " + eventName + " - Espacios disponibles: " + availableSpaces);
            }
            
        } catch (SQLException es) {
            showSQLError(es);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
    
    /**
     * Solicita al usuario seleccionar un evento y comprueba si tiene espacios disponibles
     * @param conn Conexión a la base de datos
     * @return El ID del evento seleccionado
     */
    private static int selectAndValidateEvent(Connection conn) {
        int selectedEventId = -1;
        boolean isSelectionValid = false;
        
        // Bucle que pide el número del evento mientras no tenga espacios disponibles
        do {
            System.out.println("Elige el número del evento al que quiere asistir:");
            selectedEventId = Console.readInt();
            
            // Obtiene el ID de la ubicación
            int locationId = getEventLocationId(conn, selectedEventId);
            
            // Comprueba si existen espacios disponibles en la ubicación del evento seleccionado
            int availableSpaces = calculateAvailableSpaces(conn, locationId, selectedEventId);
            
            if (availableSpaces > 0) {
                isSelectionValid = true;
            } else {
                System.out.println("El evento seleccionado está lleno. Por favor, elige otro evento.");
            }
            
        } while (!isSelectionValid);
        
        return selectedEventId;
    }
    
    /**
     * Obtiene el ID de la ubicación de un evento por su ID
     * @param conn Conexión a la base de datos
     * @param idEvent ID del evento
     * @return El ID de la ubicación
     */
    private static int getEventLocationId(Connection conn, int idEvent) {
        String query = "SELECT id_ubicacion FROM eventos WHERE id_evento = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            // Establece el parámetro de la consulta
            stmt.setInt(1, idEvent);
            
            // Si obtiene resultados, retorna el id de la ubicación
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_ubicacion");
                }
            }
            
        } catch (SQLException es) {
            showSQLError(es);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        
        return -1; // Retorna -1 si no se encuentra la ubicación
    }
    
    /**
     * Calcula el número de espacios disponibles para un evento
     * @param conn Conexión a la base de datos
     * @param eventId ID del evento
     * @param locationId ID de la ubicación
     * @return número de espacios disponibles
     */
    private static int calculateAvailableSpaces(Connection conn, int eventId, int locationId) {
        String getEventAttendees = "SELECT COUNT(*) FROM asistentes_eventos WHERE id_evento = ?";
        int registeredAttendeesCount = 0;
        int locationCapacity = 0;
        int availableSpaces = 0;
        
        try (PreparedStatement stmt = conn.prepareStatement(getEventAttendees)) {
            // Establece el parámetro de la consulta
            stmt.setInt(1, eventId);
            
            // Obtiene los sitios disponibles con la diferencia de la capacidad total y los asistentes registrados
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    registeredAttendeesCount = rs.getInt(1);
                    locationCapacity = getEventCapacity(conn, locationId);
                    availableSpaces = locationCapacity - registeredAttendeesCount;
                }
            }
            
        } catch (SQLException es) {
            showSQLError(es);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        
        return availableSpaces;
    }
    
    /**
     * Obtiene la capacidad máxima de una ubicación de un evento
     * @param conn Conexión a la base de datos
     * @param idLocation ID de la ubicación
     * @return La capacidad máxima
     */
    private static int getEventCapacity(Connection conn, int idLocation) {
        String getEventCapacityQuery = "SELECT capacidad FROM ubicaciones WHERE id_ubicacion = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(getEventCapacityQuery)) {
            // Establece el parámetro de la consulta
            stmt.setInt(1, idLocation);
            
            // Obtiene y devuelve la capacidad máxima
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("capacidad");
                }
            }
            
        } catch (SQLException es) {
            showSQLError(es);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        
        return 0; // Si no encuentra la ubicación del evento
    }
    
    /**
     * Registra un asistente en un evento si no está ya registrado
     * @param conn Conexión a la base de datos
     * @param attendeeDni DNI del asistente
     * @param eventId ID del evento
     */
    private static void registerAttendeeToEvent(Connection conn, String attendeeDni, int eventId) {
        String attendeeName = findAttendeeNameByDni(conn, attendeeDni);
        if (isAttendeeAlReadyRegistered(conn, attendeeDni, eventId)) {
            System.out.println("El asistente " + attendeeName + " ya estaba registrado.");
            return;
        }
        
        // Inserta un asistente si no estaba ya registrado
        int rowsAffected = 0;
        String query = "INSERT INTO asistentes_eventos (dni, id_evento) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            // Establece los parámetros de la consulta y la ejecuta
            stmt.setString(1, attendeeDni);
            stmt.setInt(2, eventId);
            rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println(attendeeName + " ha sido registrado para el evento seleccionado.");
            }
            
        } catch (SQLException es) {
            showSQLError(es);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
    
    /**
     * Verifica si un asistente ya está registrado en un evento
     * @param conn Conexión a la base de datos
     * @param dni DNI del asistente
     * @param idEvent ID del evento
     * @return true si ya está registrado, false en caso contrario
     */
    private static boolean isAttendeeAlReadyRegistered(Connection conn, String dni, int idEvent) {
        String query = "SELECT 1 FROM asistentes_eventos WHERE dni = ? AND id_evento = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            // Establece los parámetros de la consulta
            stmt.setString(1, dni);
            stmt.setInt(2, idEvent);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Devuelve true si hay resultados
            }
            
        } catch (SQLException es) {
            showSQLError(es);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        
        return false;
    }
    
    /**
     * Muestra los detalles de un error SQL en la consola
     * Imprime información detallada del error para ayudar a depurar
     * @param es Objeto SQLException con los detalles del error
     */
    public static void showSQLError(SQLException es) {
        System.err.println("SQL ERROR mensaje: " + es.getMessage());
        System.err.println("SQL Estado: " + es.getSQLState());
        System.err.println("SQL código específico: " + es.getErrorCode());
    }

}
