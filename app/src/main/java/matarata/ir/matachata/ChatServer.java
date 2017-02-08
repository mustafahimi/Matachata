package matarata.ir.matachata;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import static matarata.ir.matachata.SocketConnectionThread.socket;

public class ChatServer extends AsyncTask<String, Void, String> {

    private Context context;
    public PrintWriter output;
    public OutputStream out;
    public InputStream input;
    private String requestType="",username="",opponentUsername="",msgText="",msgDate="";

    public ChatServer(Context context) {
        this.context = context;
    }

    protected void onPreExecute() {

    }

    @Override
    protected String doInBackground(String... arg0) {
        requestType = arg0[0];
        username = arg0[1];
        opponentUsername = arg0[2];
        msgText = arg0[3];
        msgDate = arg0[4];
        String result = "";

        try {
            ///Socket Connection
            JSONObject myJsonObject = new JSONObject();
            myJsonObject.put("requestType", requestType);
            myJsonObject.put("username", username);
            myJsonObject.put("opponentUsername", opponentUsername);
            myJsonObject.put("msgText", msgText);
            myJsonObject.put("msgDate", msgDate);
            out = socket.getOutputStream();
            output = new PrintWriter(out);
            output.println(myJsonObject);
            output.flush();
            input = socket.getInputStream();
            int lockSeconds = 10*1000;
            long lockThreadCheckpoint = System.currentTimeMillis();
            int availableBytes = input.available();
            while(availableBytes <= 0 && (System.currentTimeMillis() < lockThreadCheckpoint + lockSeconds)){
                try{
                    Thread.sleep(10);
                }catch(InterruptedException ie){}
                availableBytes = input.available();
            }
            byte[] buffer = new byte[availableBytes];
            input.read(buffer, 0, availableBytes);
            result = new String(buffer);

            return result;
        } catch (Exception e) {
            return new String("Exception: " + e.getMessage());
        }
    }

    @Override
    protected void onPostExecute(String result) {
        String jsonStr = result;
        if (jsonStr != "") {
            try {
                JSONObject jsonObj = new JSONObject(jsonStr);
                String jsonTempResult = jsonObj.getString("serverJsonResult");
                ChatActivity.socketResultChat = jsonTempResult;
                JSONArray arrJson= jsonObj.getJSONArray("client1ChatsData");
                String[] arr=new String[arrJson.length()];
                for(int i=0;i<arrJson.length();i++)
                    arr[i]=arrJson.getString(i);
                ChatActivity.socketClient1ChatsData = arr;
                JSONArray arrJson2= jsonObj.getJSONArray("client2ChatsData");
                String[] arr2=new String[arrJson2.length()];
                for(int i=0;i<arrJson2.length();i++)
                    arr2[i]=arrJson2.getString(i);
                ChatActivity.socketClient2ChatsData = arr2;
            } catch (JSONException e) {
                Toast.makeText(context, jsonStr + "\nJson Error: " + e.toString(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(context, "Couldn't get any JSON data.", Toast.LENGTH_LONG).show();
        }
    }
}