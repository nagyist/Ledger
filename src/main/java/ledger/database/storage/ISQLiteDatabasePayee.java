package ledger.database.storage;

import ledger.database.IDatabase;
import ledger.database.enity.Payee;
import ledger.exception.StorageException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by CJ on 10/11/2016.
 */
public interface ISQLiteDatabasePayee extends ISQLiteDatabase {

    @Override
    default void insertPayee(Payee payee) throws StorageException {
        try {
            PreparedStatement stmt =
                    getDatabase().prepareStatement("INSERT INTO PAYEE (PAYEE_NAME, PAYEE_DESC) VALUES (?, ?)");
            stmt.setString(1, payee.getName());
            stmt.setString(2, payee.getDescription());
            stmt.executeUpdate();

            ResultSet generatedIDs = stmt.getGeneratedKeys();
            if (generatedIDs.next()) {
                int insertedPayeeID = generatedIDs.getInt(1);
                payee.setId(insertedPayeeID);
            }

        } catch (java.sql.SQLException e) {
            throw new StorageException("Error while adding Payee", e);
        }
    }

    @Override
    default void deletePayee(Payee payee) throws StorageException {
        try {
            PreparedStatement stmt = getDatabase().prepareStatement("DELETE FROM PAYEE WHERE PAYEE_ID = ?");
            stmt.setInt(1, payee.getId());
            stmt.executeUpdate();
        } catch (java.sql.SQLException e) {
            throw new StorageException("Error while deleting Payee", e);
        }
    }

    @Override
    default void editPayee(Payee payee) throws StorageException {
        try {
            PreparedStatement stmt =
                    getDatabase().prepareStatement("UPDATE PAYEE SET PAYEE_NAME = ?, PAYEE_DESC = ? WHERE PAYEE_ID = ?");

            stmt.setString(1, payee.getName());
            stmt.setString(2, payee.getDescription());
            stmt.setInt(3, payee.getId());
            stmt.executeUpdate();
        } catch (java.sql.SQLException e) {
            throw new StorageException("Error while editing Payee", e);
        }
    }

    @Override
    default List<Payee> getAllPayees() throws StorageException {
        try {
            Statement stmt = getDatabase().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM PAYEE;");

            ArrayList<Payee> payeeList = new ArrayList<>();

            while (rs.next()) {

                int payeeID = rs.getInt("PAYEE_ID");
                String payeeName = rs.getString("PAYEE_NAME");
                String payeeDesc = rs.getString("PAYEE_DESC");

                Payee currentPayee = new Payee(payeeName, payeeDesc, payeeID);

                payeeList.add(currentPayee);
            }
            return payeeList;
        } catch (java.sql.SQLException e) {
            throw new StorageException("Error while getting all payees", e);
        }
    }

}