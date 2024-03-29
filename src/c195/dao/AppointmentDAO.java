package c195.dao;

import c195.model.Appointment;
import c195.model.Contact;
import c195.model.Customer;
import c195.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;

/**
 * @author Jonathan Dowdell
 */
public class AppointmentDAO {

    /**
     * Removes Appointment using the Appointment Object.
     * @param appointment Appointment
     * @return boolean is Appointment was removed.
     */
    public static boolean removeAppointment(Appointment appointment) {
        String deletionQuery = "DELETE FROM appointments WHERE Appointment_ID = ?";
        try(PreparedStatement statement = SQLDBService.getConnection().prepareStatement(deletionQuery)) {
            statement.setLong(1, appointment.getAppointmentID());
            statement.executeUpdate();
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    /**
     * Loads Appointments from Database.
     * @return Observable List of Appointments.
     */
    public static ObservableList<Appointment> getAllAppointments() {
        ObservableList<Appointment> appointments = FXCollections.observableArrayList();
        String getAllAppointmentsQuery = "SELECT * FROM appointments AS a " +
                "JOIN customers as c " +
                "ON a.Customer_ID = c.Customer_ID " +
                "JOIN users as u " +
                "ON a.User_ID = u.User_ID " +
                "JOIN contacts as cont " +
                "ON a.Contact_ID = cont.Contact_ID";
        try (Statement statement = SQLDBService.getConnection().createStatement()) {
            ResultSet resultSet = statement.executeQuery(getAllAppointmentsQuery);
            while (resultSet.next()) {
                Appointment appointment = pullAppointmentResultSet(resultSet);
                appointments.add(appointment);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return appointments;
    }


    /**
     * Finds Overlapping Appointments.
     * @param as LocalDateTime
     * @param ae LocalDateTime
     * @param customerId Long
     * @return Returns Appointment ID and -1 is no overlapping appointment
     */
    public static long hasOverlappingAppointments(LocalDateTime as, LocalDateTime ae, long customerId, long appointmentId) {
        ObservableList<Appointment> allAppointments = getAllAppointments();
        long overlappingAppointmentId = -1;

        for (Appointment appointment : allAppointments) {
            if ((appointment.getCustomer().getCustomerID() == customerId) && (appointment.getAppointmentID() != appointmentId)) {
                LocalDateTime nas = appointment.getStart();
                LocalDateTime nae = appointment.getEnd();
                if (nas.isEqual(as)  || nae.isEqual(ae)) {
                    overlappingAppointmentId = appointment.getAppointmentID();
                    break;
                } else if (nas.isBefore(as) && nae.isAfter(ae)) {
                    overlappingAppointmentId = appointment.getAppointmentID();
                    break;
                } else if (nas.isAfter(as) && nas.isBefore(ae)) {
                    overlappingAppointmentId = appointment.getAppointmentID();
                    break;
                } else if (nae.isAfter(as) && nae.isBefore(ae)) {
                    overlappingAppointmentId = appointment.getAppointmentID();
                    break;
                } else if (nas.isAfter(as) && nae.isBefore(ae)) {
                    overlappingAppointmentId = appointment.getAppointmentID();
                    break;
                }
            }
        }

        return overlappingAppointmentId;
    }

    /**
     * Add Appointment to Database.
     * @param appointment Appointment
     */
    public static void addAppointment(Appointment appointment) {
        String createAppointmentSQLQuery = "INSERT INTO appointments( Title, Description, Location, Type,  Start, End, Create_Date, Created_By,  Last_Update, Last_Updated_By,  Customer_ID, User_ID, Contact_ID)  VALUES (?,?,?,?,?,?,now(),?,now(),?,?,?,?);";
        try (PreparedStatement statement = SQLDBService.getConnection().prepareStatement(createAppointmentSQLQuery)) {
            User currentUser = UserDAO.getCurrentUser();
            statement.setString(1, appointment.getTitle());
            statement.setString(2, appointment.getDescription());
            statement.setString(3, appointment.getLocation());
            statement.setString(4, appointment.getType());
            statement.setTimestamp(5, Timestamp.valueOf(appointment.getStart()));
            statement.setTimestamp(6, Timestamp.valueOf(appointment.getEnd()));
            statement.setString(7, currentUser.getUsername());
            statement.setString(8, currentUser.getUsername());
            statement.setLong(9, appointment.getCustomer().getCustomerID());
            statement.setLong(10, currentUser.getUserID());
            statement.setLong(11, appointment.getContact().getContactID());
            statement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * Update Appointment in Database.
     * @param appointment Appointment
     */
    public static void updateAppointment(Appointment appointment) {
        final String updateAppointmentSQLQuery = "UPDATE appointments\n" +
                "SET \n" +
                "Title = ?, \n" +
                "Description = ?, \n" +
                "Location = ?, \n" +
                "Type = ?, \n" +
                "Start = ?, End = ?, \n" +
                "Last_Update = now(), Last_Updated_By = ?, \n" +
                "Customer_ID = ?, User_ID = ?, Contact_ID = ?\n" +
                "WHERE Appointment_ID = ?";
        try (PreparedStatement statement = SQLDBService.getConnection().prepareStatement(updateAppointmentSQLQuery)) {
            User currentUser = UserDAO.getCurrentUser();
            statement.setString(1, appointment.getTitle());
            statement.setString(2, appointment.getDescription());
            statement.setString(3, appointment.getLocation());
            statement.setString(4, appointment.getType());
            statement.setTimestamp(5, Timestamp.valueOf(appointment.getStart()));
            statement.setTimestamp(6, Timestamp.valueOf(appointment.getEnd()));
            statement.setString(7, currentUser.getUsername());
            statement.setLong(8, appointment.getCustomer().getCustomerID());
            statement.setLong(9, appointment.getUser().getUserID());
            statement.setLong(10, appointment.getContact().getContactID());
            statement.setLong(11, appointment.getAppointmentID());
            statement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private static Appointment pullAppointmentResultSet(ResultSet resultSet) throws SQLException {
        Appointment appointment = new Appointment();
        long appointmentID = resultSet.getLong("Appointment_ID");
        String title = resultSet.getString("Title");
        String description = resultSet.getString("Description");
        String location = resultSet.getString("Location");
        String type = resultSet.getString("Type");
        LocalDateTime startDate = resultSet.getTimestamp("Start").toLocalDateTime();
        LocalDateTime endDate = resultSet.getTimestamp("End").toLocalDateTime();
        LocalDateTime createDate = resultSet.getTimestamp("Create_Date").toLocalDateTime();
        String createdBy = resultSet.getString("Created_By");
        LocalDateTime lastUpdateDate = resultSet.getTimestamp("Last_Update").toLocalDateTime();
        String lastUpdatedBy = resultSet.getString("Last_Updated_By");

        appointment.setAppointmentID(appointmentID);
        appointment.setTitle(title);
        appointment.setDescription(description);
        appointment.setLocation(location);
        appointment.setType(type);
        appointment.setStart(startDate);
        appointment.setEnd(endDate);
        appointment.setCreateDate(createDate);
        appointment.setCreatedBy(createdBy);
        appointment.setLastUpdate(lastUpdateDate);
        appointment.setLastUpdatedBy(lastUpdatedBy);

        Customer customer = CustomerDAO.pullCustomerFromResultSet(resultSet);
        appointment.setCustomer(customer);


        User user = UserDAO.pullUserFromResultSet(resultSet);
        appointment.setUser(user);


        Contact contact = ContactDAO.pullContactFromResultSet(resultSet);
        appointment.setContact(contact);
        return appointment;
    }

    /**
     * Loads Appointments By Customer.
     * @param customer Customer
     * @return Observable List of Appointments.
     */
    public static ObservableList<Appointment> getAppointmentByCustomer(Customer customer) {
        ObservableList<Appointment> appointments = FXCollections.observableArrayList();
        String getAllAppointmentsQuery = "SELECT * FROM appointments AS a JOIN customers as c ON a.Customer_ID = c.Customer_ID JOIN users as u ON a.User_ID = u.User_ID JOIN contacts as cont ON a.Contact_ID = cont.Contact_ID WHERE a.Customer_ID = ?";

        try {
            PreparedStatement statement = SQLDBService.getConnection().prepareStatement(getAllAppointmentsQuery);
            statement.setLong(1,customer.getCustomerID());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Appointment appointment = pullAppointmentResultSet(resultSet);
                appointments.add(appointment);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return appointments;
    }
}
