package job.caurix.com.selfservicewithdrawal;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    //    Makes the network request
    private static final int PERMISSION_REQUEST = 100;
    String finalResult;
    HttpParse httpParse = new HttpParse();
    HashMap<String, String> hashmap = new HashMap<>();
    String HttpURL = "http://www.caurix.net/insertNew.php";

    String smsBody;
    String smsSender;
    String amount;

    String ussdCode = "blank";
    List<Response> responseList;
    DatabaseAccess databaseAccess;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkpermission();

        databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();

        responseList = databaseAccess.getResponses();

        smsBody = getIntent().getStringExtra("body");
        smsSender = getIntent().getStringExtra("sender");


//        -------------------------------------THis handles a case where there is internet connection----------------------
        if (isConnectingToInternet(MainActivity.this)) {
//            Check for the local ones to upload them

            for(int i = 0; i < responseList.size(); i++){
                getRequest(responseList.get(i).getPhoneNumber(), responseList.get(i).getAmount(), responseList.get(i).getConfirmation_id());
            }

//            Normal procedure
            if (smsBody != null) {
                if (smsBody.contains("reussi")) {

                    getRequest(databaseAccess.getPhoneNumber(), databaseAccess.getAmount(), parseSMS(smsBody));

                } else if (smsBody.contains("FAIL")) {
                    Log.i("TAG", "SMS FAILED");
                }

                else if (smsBody.contains("retrait*")) {

                    StringTokenizer stringTokenizer = new StringTokenizer(smsBody, "*");
                    String firstToken = stringTokenizer.nextToken();
                    amount = stringTokenizer.nextToken();

                    databaseAccess.storeAmount(amount);
                    databaseAccess.storePhoneNumber(smsSender);

                    ussdCode = "*143*1*" + smsSender + "*" + amount + "*" + "7234" + Uri.encode("#");

                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }

//                    Toast.makeText(this, "" + ussdCode, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + ussdCode)));
                } else {
                    Toast.makeText(this, "This message is useless", Toast.LENGTH_SHORT).show();
                }
            }
        }


//        ----------------------------------This handles a case where there is no internet-----------------------------
        else {

            if (smsBody != null) {

                if (smsBody.contains("reussi")) {
                    databaseAccess.addOfflineRecord(smsSender, amount, parseSMS(smsBody));
                }

                else if (smsBody.contains("FAIL")) {
                    Log.i("TAG", "SMS FAILED");
                }

                else if (smsBody.contains("retrait*")) {

                    StringTokenizer stringTokenizer = new StringTokenizer(smsBody, "*");
                    String firstToken = stringTokenizer.nextToken();
                    amount = stringTokenizer.nextToken();

                    ussdCode = "*143*1*" + smsSender + "*" + amount + "*" + "7234" + Uri.encode("#");

                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + ussdCode)));
                }
            }

        }

    }

    public void getRequest(final String phonenumber, final String amount, final String confirmationid) {
        String url = "http://www.caurix.net/selfpayment.php";
        final AsyncHttpClient client = new AsyncHttpClient();


        client.get(this, url, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                try {
                    JSONArray arr = new JSONArray(responseString);
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject eachObject = arr.getJSONObject(i);

                        String phone_number = eachObject.getString("phone_number");
                        String bank_id = eachObject.getString("bank_id");
                        String bank_name = eachObject.getString("bank_name");
                        String balance = eachObject.getString("balance");

                        if (phonenumber.equals(phone_number)) {
                            if (Integer.parseInt(balance) > Integer.parseInt(amount)) {
                                Toast.makeText(MainActivity.this, "Records have been Updated", Toast.LENGTH_SHORT).show();
                                updateRecords(phone_number, bank_id, bank_name, amount,confirmationid);
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void updateRecords(String number, String bank_id, String bank_name, String balance, String confirmation_id) {
        class UpdateRecords extends AsyncTask<String, Void, String> {

            @Override
            protected void onCancelled() {
                Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                Toast.makeText(MainActivity.this, "" + s.toString(), Toast.LENGTH_SHORT).show();
                Log.i("TAG", "" + s.toString());
            }

            @Override
            protected String doInBackground(String... params) {
                hashmap.put("phone_number", params[0]);
                hashmap.put("bank_id", params[1]);
                hashmap.put("bank_name", params[2]);
                hashmap.put("balance", params[3]);
                hashmap.put("confirmation_id", params[4]);

                finalResult = httpParse.postRequest(hashmap, HttpURL);
                return finalResult;
            }
        }

        UpdateRecords updateRecords = new UpdateRecords();
        updateRecords.execute(number, bank_id, bank_name, balance, confirmation_id);
    }

    public void checkpermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    PERMISSION_REQUEST);
        }
    }

    public static boolean isConnectingToInternet(Context context) {
        ConnectivityManager connectivity =
                (ConnectivityManager) context.getSystemService(
                        Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
        }
        return false;
    }

    public String parseSMS(String original){
        String test = "R" + original.substring(original.lastIndexOf("Ref") + 1);
        String test2 = test.substring(test.lastIndexOf(".") -7);
        String finalNumber = test.replace(test2, "");

        return finalNumber;

    }
}
