package ledger.io.input;

import au.com.bytecode.opencsv.CSVReader;
import ledger.database.entity.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Takes a CSV file generated by Chase Bank and converts into Ledger Object Structure
 */
public class ChaseConverter implements IInAdapter<Transaction> {

    private File file;
    private Account account;

    public ChaseConverter(File file, Account account) {
        this.file = file;
        this.account = account;
    }

    @Override
    public List<Transaction> convert() throws FileNotFoundException {
        CSVReader reader = new CSVReader(new FileReader(file), ',', '"', 1);
        List<Transaction> transactions = new LinkedList<>();

        try {
            String[] nextLine;

            DateFormat df = new SimpleDateFormat("MM/dd/yyyy");

            while ((nextLine = reader.readNext()) != null) {
                String details = nextLine[0];
                String dateString = nextLine[1];
                String description = nextLine[2];
                String amountString = nextLine[3];
                String typeString = nextLine[4];
                String balanceString = nextLine[5];
                String checkNumberString = nextLine[6];

                //Date date, Type type, int amount, Account account, Payee payee, boolean pending, List<Tag> tagList, Note note

                Date date = df.parse(dateString);
                boolean pending = isPending(balanceString);
                int amount = (int) Math.round(Double.parseDouble(amountString) * 100);
                int balance = 0;
                if (!pending)
                    balance = (int) Math.round(Double.parseDouble(balanceString) * 100);

                Type type = TypeConversion.convert(typeString);

                // TODO: Should Payee be instantiated each time?
                details = details.substring(0, details.length() - 5).trim();
                Payee payee = new Payee(details, details);
                List<Tag> tags = null;
                Note note = null;

                Transaction transaction = new Transaction(date, type, amount, this.account, payee, pending, tags, note);

                transactions.add(transaction);
            }

        } catch (IOException e) {
            // TODO: Decide what to do
            e.printStackTrace();
        } catch (ParseException e) {
            // TODO: Throw Custom Exception
            e.printStackTrace();
        }
        return transactions;
    }

    private boolean isPending(String amount) {
        return amount == null || amount.trim().isEmpty();
    }
}
