package job.caurix.com.selfservicewithdrawal;

/**
 * Created by lawrene on 6/28/18.
 */

public class Response {
    String phoneNumber, amount, confirmation_id;

    public Response(String phoneNumber, String amount, String confirmation_id) {
        this.phoneNumber = phoneNumber;
        this.amount = amount;
        this.confirmation_id = confirmation_id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAmount() {
        return amount;
    }

    public String getConfirmation_id() {
        return confirmation_id;
    }
}

