package job.caurix.com.selfservicewithdrawal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by lawrene on 6/27/18.
 */

public class SMSReceiver extends BroadcastReceiver {
    String ussdCode = "retrait*300*OtherText";

    String ussd = "*XXX*X" + Uri.encode("#");
//    startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + ussd)));


    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle extras = intent.getExtras();
        if (extras == null)
            return;

        // To display a Toast whenever there is an SMS.
        //Toast.makeText(context,"Recieved",Toast.LENGTH_LONG).show();

        Object[] pdus = (Object[]) extras.get("pdus");
        for (int i = 0; i < pdus.length; i++) {
            SmsMessage SMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);
            String sender = SMessage.getOriginatingAddress();
            String body = SMessage.getMessageBody().toString();

            // A custom Intent that will used as another Broadcast
            Intent in = new Intent(context,MainActivity.class).
                    setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).
                    putExtra("sender", sender).
                    putExtra("body", body);

            //You can place your check conditions here(on the SMS or the sender)
            //and then send another broadcast
            context.startActivity(in);

//            Log.i("TAG", sender+ body);
            // This is used to abort the broadcast and can be used to silently
            // process incoming message and prevent it from further being
            // broadcasted. Avoid this, as this is not the way to program an app.
            // this.abortBroadcast();
        }

    }

}
