/**************************************
 * Autor: Axel Álvarez Santos
 * Fecha: 27/11/2024
 * Tarea: AD03 Tarea Evaluativa 01
 **************************************/

package ejercicios;

import java.sql.*;

public class UD03TareaEvaluativaEjercicio3 {
    
    // Configuración para la conexión a la base de datos
    private static final String CONNECTION_URL = "jdbc:mysql://localhost:3306/dbeventos";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "secret";
    
    // Punto de entrada principal
    public static void main(String[] args) {
        
        // Conexión a la base de datos y flujo general del programa
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL, USERNAME, PASSWORD)) {
            
            // Obtiene y registra el asistente si no existe
            String dniAttendee = getOrRegisterAtendee(conn);
            
            // Muestra los eventos disponibles y selecciona uno
            int selectedEvent = selectEvent(conn);
            
            // Registra al asistente en el evento seleccionado
            registerAttendeeToEvent(conn, dniAttendee, selectedEvent);
            
        } catch (SQLException es) {
            muestraErrorSQL(es);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
    
    /**
     * Obtiene el DNI del asistente y verifica si ya está registrado
     * Si no está registrado, solicita el nombre y lo añade a la base de datos
     * @param conn Conexión a la base de datos
     * @return DNI del asistente
     */
    private static String getOrRegisterAtendee(Connection conn) {
        String dniAttendee;
        boolean dniValid = false;
        
        do {
            System.out.println("Introduce el DNI del asistente:");
            dniAttendee = Console.readString().toUpperCase();
            dniValid = isDniValid(dniAttendee);
            
            if (!dniValid) {
                System.out.println("El DNI introducido no es válido. Inténtalo de nuevo.");
            }
        } while (!dniValid);
        
        // Intenta buscar al asistente en la base de datos
        String name = getAttendeeNameByDni(conn, dniAttendee);
        
        // Si no lo encuentra, solicita los datos y lo registra
        if (name == null) {
            System.out.println("No se encontró un asistente con el DNI proporcionado.");
            System.out.println("Introduce el nombre del asistente:");
            name = Console.readString();
            insertNewAteendee(conn, dniAttendee, name);
        }
        
        System.out.println("Estás realizando la reserva para: " + name);
        return dniAttendee;
    }
    
    /**
     * Valida que el DNI introducido tenga el formato correcto
     * El formato válido es 8 dígitos seguidos de una letra mayúscula
     * @param dni DNI a validar
     * @return true si el formato es correcto, false en caso contrario
     */
    private static boolean isDniValid(String dni) {
        return dni != null && dni.matches("\\d{8}[A-Z]");
    }
    
    /**
     * Busca el nombre de un asistente en la base de datos por su DNI
     * @param conn Conexión a la base de datos
     * @param dni DNI del asistente
     * @return Nombre del asistente o null si no existe
     */
    private static String getAttendeeNameByDni(Connection conn, String dni) {
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
            muestraErrorSQL(es);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        
        return null; // Si no lo encuentra
    }
    
    /**
     * Registra un nuevo asistente en la base de datos
     * @param conn Conexión a la base de datos
     * @param dni DNI del asistente
     * @param name Nombre del asistente
     */
    private static void insertNewAteendee(Connection conn, String dni, String name) {
        String query = "INSERT INTO asistentes (dni, nombre) VALUES (?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, dni);
            stmt.setString(2, name);
            stmt.executeUpdate();
            //debug
            System.out.println("El asistente " + name + " ha sido insertado.");
            
        } catch (SQLException es) {
            muestraErrorSQL(es);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
    
    /**
     * Muestra todos los eventos disponibles y permite seleccionar uno
     * @param conn Conexión a la base de datos
     * @return ID del evento seleccionado
     */
    private static int selectEvent(Connection conn) {
        String query = "SELECT id_evento, nombre_evento, id_ubicacion FROM eventos";
        
        System.out.println("Lista de eventos:");
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int idEvent = rs.getInt("id_evento");
                    String nameEvent = rs.getString("nombre_evento");
                    int idLocation = rs.getInt("id_ubicacion");
                    int availableSeats = getAvailableSeats(conn, idEvent, idLocation);
                    
                    // Muestra los detalles del evento, incluyendo el espacio disponible
                    System.out.println(idEvent + ". " + nameEvent + " - Espacios disponibles: " + availableSeats);
                }
                
                // Solicita al usuario seleccionar un evento
                System.out.println("Elige el número del evento al que quiere asistir:");
                return Console.readInt();
            }
            
        } catch (SQLException es) {
            muestraErrorSQL(es);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        
        return -1;
    }
    
    /**
     * Calcula el número de espacios disponibles para un evento
     * @param conn Conexión a la base de datos
     * @param idEvent ID del evento
     * @param idLocation ID de la ubicación
     * @return número de espacios disponibles
     */
    private static int getAvailableSeats(Connection conn, int idEvent, int idLocation) {
        String getEventAttendees = "SELECT COUNT(*) FROM asistentes_eventos WHERE id_evento = ?";
        int eventAttendees = 0;
        int eventCapacity = 0;
        
        try (PreparedStatement stmt = conn.prepareStatement(getEventAttendees)) {
            stmt.setInt(1, idEvent);
            
            // Obtiene los sitios disponibles con la diferencia de la capacidad total y los asistentes registrados
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    eventAttendees = rs.getInt(1);
                    eventCapacity = getEventCapacity(conn, idLocation);
                    return eventCapacity - eventAttendees;
                }
            }
            
        } catch (SQLException es) {
            muestraErrorSQL(es);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        
        return 0;
    }
    
    /**
     * Obtiene la capacidad máxima de una ubicación de un evento
     * @param conn Conexión a la base de datos
     * @param idLocation ID de la ubicación
     * @return Capacidad máxima
     */
    private static int getEventCapacity(Connection conn, int idLocation) {
        String getEventCapacityQuery = "SELECT capacidad FROM ubicaciones WHERE id_ubicacion = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(getEventCapacityQuery)) {
            stmt.setInt(1, idLocation);
            
            // obtiene y devuelve la capacidad máxima
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("capacidad");
                }
            }
            
        } catch (SQLException es) {
            muestraErrorSQL(es);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        
        return 0;
    }
    
    /**
     * Registra un asistente en un evento si no está ya registrado
     * @param conn Conexión a la base de datos
     * @param dni DNI del asistente
     * @param idEvent ID del evento
     */
    private static void registerAttendeeToEvent(Connection conn, String dni, int idEvent) {
        String nameAttendee = getAttendeeNameByDni(conn, dni);
        if (isAttendeeAlReadyRegistered(conn, dni, idEvent)) {
            System.out.println("El asistente " + nameAttendee + " ya estaba registrado.");
            return;
        }
        
        // Inserta un asistente si no estaba ya registrado
        int rowsAffected = 0;
        String query = "INSERT INTO asistentes_eventos (dni, id_evento) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, dni);
            stmt.setInt(2, idEvent);
            rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println(nameAttendee + " ha sido registrado para el evento seleccionado.");
            }
            
        } catch (SQLException es) {
            muestraErrorSQL(es);
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
            stmt.setString(1, dni);
            stmt.setInt(2, idEvent);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Devuelve true si hay resultado
            }
            
        } catch (SQLException es) {
            muestraErrorSQL(es);
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
    public static void muestraErrorSQL(SQLException es) {
        System.err.println("SQL ERROR mensaje: " + es.getMessage());
        System.err.println("SQL Estado: " + es.getSQLState());
        System.err.println("SQL código específico: " + es.getErrorCode());
    }

}
