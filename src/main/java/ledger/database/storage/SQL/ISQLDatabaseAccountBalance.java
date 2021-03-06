package ledger.database.storage.SQL;

import ledger.database.entity.Account;
import ledger.database.entity.AccountBalance;
import ledger.database.storage.SQL.SQLite.ISQLiteDatabase;
import ledger.database.storage.table.AccountBalanceTable;
import ledger.exception.StorageException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public interface ISQLDatabaseAccountBalance extends ISQLiteDatabase {

    @Override
    default void addBalanceForAccount(AccountBalance balance) throws StorageException {
        try {
            PreparedStatement stmt = getDatabase().prepareStatement("INSERT INTO " + AccountBalanceTable.TABLE_NAME +
                    " (" + AccountBalanceTable.ABAL_ACCOUNT_ID +
                    ", " + AccountBalanceTable.ABAL_AMOUNT +
                    ", " + AccountBalanceTable.ABAL_DATETIME +
                    ") VALUES (?, ?, ?)");

            stmt.setInt(1, balance.getAccount().getId());
            stmt.setLong(2, balance.getAmount());
            stmt.setLong(3, balance.getDate().getTime());

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                balance.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new StorageException("Error while adding account balance", e);
        }
    }

    @Override
    default AccountBalance getBalanceForAccount(Account account) throws StorageException {
        try {
            PreparedStatement stmt = getDatabase().prepareStatement("SELECT " +
                    AccountBalanceTable.ABAL_ID + ", " +
                    AccountBalanceTable.ABAL_ACCOUNT_ID + ", " +
                    AccountBalanceTable.ABAL_AMOUNT + ", " +
                    AccountBalanceTable.ABAL_DATETIME +
                    " FROM " + AccountBalanceTable.TABLE_NAME +
                    " WHERE " + AccountBalanceTable.ABAL_ACCOUNT_ID + " =?");
            stmt.setInt(1, account.getId());
            ResultSet rs = stmt.executeQuery();

            //Store info so we can query for accounts only once
            int balanceID = -1;
            long amount = -1;
            long mostRecentDate = -1L;
            while (rs.next()) {
                long currentDate = rs.getLong(AccountBalanceTable.ABAL_DATETIME);
                if (currentDate > mostRecentDate) {
                    mostRecentDate = currentDate;

                    balanceID = rs.getInt(AccountBalanceTable.ABAL_ID);
                    amount = rs.getLong(AccountBalanceTable.ABAL_AMOUNT);
                }
            }

            return new AccountBalance(account, new Date(mostRecentDate), amount, balanceID);

        } catch (SQLException e) {
            throw new StorageException("Error while getting account balance", e);
        }
    }

}
