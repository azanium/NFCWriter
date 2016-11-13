package com.azacode.NFCWriter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;

/**
 * Activity to write NFC tags with own mimetype and ID
 * Based on the excellent tutorial by Jesse Chen
 * http://www.jessechen.net/blog/how-to-nfc-on-the-android-platform/
 */
public class MainActivity extends Activity {

	boolean mWriteMode = false;
	private NfcAdapter mNfcAdapter;
	private PendingIntent mNfcPendingIntent;
	private AlertDialog alertDlg;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


		((Button) findViewById(R.id.button)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mNfcAdapter = NfcAdapter.getDefaultAdapter(MainActivity.this);
				mNfcPendingIntent = PendingIntent.getActivity(MainActivity.this, 0,
				    new Intent(MainActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

				enableTagWriteMode();

				alertDlg = new AlertDialog.Builder(MainActivity.this).setTitle("Sentuh Tag dengan handphone untuk menulis")
				    .setOnCancelListener(new DialogInterface.OnCancelListener() {
				        @Override
				        public void onCancel(DialogInterface dialog) {
				            disableTagWriteMode();
				        }

				    }).create();
				alertDlg.show();
			}
		});
	}

	private void enableTagWriteMode() {
	    mWriteMode = true;
	    IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
	    IntentFilter[] mWriteTagFilters = new IntentFilter[] { tagDetected };
	    mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mWriteTagFilters, null);
	}

	private void disableTagWriteMode() {
	    mWriteMode = false;
		mNfcAdapter.disableForegroundDispatch(this);
	}

    private String getDataText(int resourceId) {
        TextView text = (TextView)findViewById(resourceId);
        return text.getText().toString();
    }

	@Override
	protected void onNewIntent(Intent intent) {
	    // Tag writing mode
	    if (mWriteMode && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
	        Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            String securityOnDuty = getDataText(R.id.securityOnDutyText);
            String client = getDataText(R.id.clientNameText);
            String location = getDataText(R.id.locationText);
            String building = getDataText(R.id.buildingText);
            String floor = getDataText(R.id.floorText);

			JSONObject json = new JSONObject();
			String output = "";
			try {
                json.put("security", securityOnDuty);
                json.put("client", client);
                json.put("location", location);
                json.put("building", building);
                json.put("floor", floor);

				DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
				String time = (String)df.format(new Date());
				json.put("time", time);

				output = json.toString();
                Log.i("NFC", output);
			}
			catch (JSONException ex) {
				ex.printStackTrace();
			}

	        //NdefRecord record = NdefRecord.createMime( ((TextView)findViewById(R.id.mime)).getText().toString(), ((TextView)findViewById(R.id.value)).getText().toString().getBytes());
	        try {
				NdefMessage message = new NdefMessage(new NdefRecord[]{createRecord(output)});
				if (writeTag(message, detectedTag)) {
					Toast.makeText(this, "Success: Wrote placeid to nfc tag", Toast.LENGTH_LONG)
							.show();
					alertDlg.dismiss();
				}
			}
			catch (UnsupportedEncodingException ex) {
				ex.printStackTrace();
			}
	    }
	}

	private NdefRecord createRecord(String text) throws UnsupportedEncodingException {

		//create the message in according with the standard
		String lang = "en";
		byte[] textBytes = text.getBytes();
		byte[] langBytes = lang.getBytes("US-ASCII");
		int langLength = langBytes.length;
		int textLength = textBytes.length;

		byte[] payload = new byte[1 + langLength + textLength];
		payload[0] = (byte) langLength;

		// copy langbytes and textbytes into payload
		System.arraycopy(langBytes, 0, payload, 1, langLength);
		System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

		NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);
		return recordNFC;
	}

	/*
	* Writes an NdefMessage to a NFC tag
	*/
	public boolean writeTag(NdefMessage message, Tag tag) {
	    int size = message.toByteArray().length;
	    try {
	        Ndef ndef = Ndef.get(tag);
	        if (ndef != null) {
	            ndef.connect();
	            if (!ndef.isWritable()) {
					Toast.makeText(getApplicationContext(),
					"Error: tag not writable",
					Toast.LENGTH_SHORT).show();
	                return false;
	            }
	            if (ndef.getMaxSize() < size) {
					Toast.makeText(getApplicationContext(),
					"Error: tag too small",
					Toast.LENGTH_SHORT).show();
	                return false;
	            }
	            ndef.writeNdefMessage(message);
	            return true;
	        } else {
	            NdefFormatable format = NdefFormatable.get(tag);
	            if (format != null) {
	                try {
	                    format.connect();
	                    format.format(message);
	                    return true;
	                } catch (IOException e) {
	                    return false;
	                }
	            } else {
	                return false;
	            }
	        }
	    } catch (Exception e) {
	        return false;
	    }
	}
}
