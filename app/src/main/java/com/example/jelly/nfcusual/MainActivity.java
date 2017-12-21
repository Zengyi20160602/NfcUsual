package com.example.jelly.nfcusual;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.jelly.nfcusual.common.NumeralParse;
import com.example.jelly.nfcusual.common.ResultAdapter;
import com.example.jelly.nfcusual.nfcTech.IsoDepParse;

import java.util.ArrayList;
import java.util.HashMap;

import darks.log.Logger;

public class MainActivity extends AppCompatActivity{
    private static Logger log = Logger.getLogger(MainActivity.class);
    private static final int ISODEP_ONMESSAGERECEIVE = 0;
    private PendingIntent pendingIntent;
    private NfcAdapter mAdapter;
    private LinearLayout scanHint;
    private NumeralParse numeralParse;
    private ArrayList<HashMap<String, String>> resultData;
    private ResultAdapter resultAdapter;
    private IsoDepParse isoDepParse;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message message) {
            switch (message.what) {
                case ISODEP_ONMESSAGERECEIVE:
                    resultAdapter.setData(resultData);
                    resultAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
            super.handleMessage(message);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        resolveIntent(getIntent());

        mAdapter = NfcAdapter.getDefaultAdapter(this);
        if(mAdapter == null) {
            //提示错误
            toast(getString(R.string.open_nfc_error));
            return;
        }
        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (resultData.size() == 0){
            toast(getString(R.string.no_record));
            return true;
        }
        switch (item.getItemId()){
            case R.id.clear_scan:
                resultData.clear();
                resultAdapter.setData(resultData);
                resultAdapter.notifyDataSetChanged();
                scanHint.setVisibility(View.VISIBLE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void toast(String value) {
        Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
    }

    private void resolveIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            IsoDep isoDep = IsoDep.get(tag);
            if (isoDep != null) {
                log.info("isoDep != null");
                isoDepParse = new IsoDepParse(isoDep, onMessageReceived);
                Thread thread = new Thread(isoDepParse);
                thread.start();
            }
            resultData = dumpTagData(tag);
            log.info(resultData.toString());
            resultAdapter.setData(resultData);
            resultAdapter.notifyDataSetChanged();
            scanHint.setVisibility(View.GONE);
        }else {
            scanHint.setVisibility(View.VISIBLE);
        }
    }

    private void init(){
        numeralParse = new NumeralParse();
        RecyclerView resultContainer = (RecyclerView) findViewById(R.id.result_container);
        scanHint = (LinearLayout) findViewById(R.id.scan_hint);
        resultData = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        resultContainer.setLayoutManager(layoutManager);
        resultAdapter = new ResultAdapter(this);
        resultAdapter.setData(resultData);
        resultContainer.setAdapter(resultAdapter);
    }

    private HashMap<String, String> itemData(String title, String text) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("title", title);
        hashMap.put("text", text);
        return hashMap;
    }


    private ArrayList<HashMap<String, String>> dumpTagData(Tag tag){
        ArrayList<HashMap<String, String>> data = new ArrayList<>();
        byte[] id = tag.getId();
        data.add(itemData(getString(R.string.id_hex), numeralParse.toHex(id)));
        data.add(itemData(getString(R.string.id_r_hex), numeralParse.toReversedHex(id)));
        data.add(itemData(getString(R.string.id_dec), String.valueOf(numeralParse.toDec(id))));
        data.add(itemData(getString(R.string.id_r_dec), String.valueOf(numeralParse.toReversedDec(id))));
        String s = "";
        String prefix = "android.nfc.tech.";
        for (String tech : tag.getTechList()) {
            s = s + tech.substring(prefix.length()) + ", ";

        }
        s = s.substring(0, s.length() - 2);
        data.add(itemData(getString(R.string.technologies), s));

        for (String tech : tag.getTechList()) {
            if (tech.equals(MifareClassic.class.getName())) {
                String type = "Unknown";
                try {
                    MifareClassic mifareTag;
                    try {
                        mifareTag = MifareClassic.get(tag);
                    } catch (Exception e) {
                        // Fix for Sony Xperia Z3/Z5 phones
                        tag = cleanupTag(tag);
                        mifareTag = MifareClassic.get(tag);
                    }
                    switch (mifareTag.getType()) {
                        case MifareClassic.TYPE_CLASSIC:
                            type = "Classic";
                            break;
                        case MifareClassic.TYPE_PLUS:
                            type = "Plus";
                            break;
                        case MifareClassic.TYPE_PRO:
                            type = "Pro";
                            break;
                    }
                    data.add(itemData(getString(R.string.mifare_classic_type), type));
                    data.add(itemData(getString(R.string.mifare_size), mifareTag.getSize() + " bytes"));
                    data.add(itemData(getString(R.string.mifare_sectors), String.valueOf(mifareTag.getSectorCount())));
                    data.add(itemData(getString(R.string.mifare_blocks), String.valueOf(mifareTag.getBlockCount())));
                } catch (Exception e) {
                    data.add(itemData(getString(R.string.mifare_classic_error), e.getMessage()));
                }
            }

            if (tech.equals(MifareUltralight.class.getName())) {
                MifareUltralight mifareUlTag = MifareUltralight.get(tag);
                String type = "Unknown";
                switch (mifareUlTag.getType()) {
                    case MifareUltralight.TYPE_ULTRALIGHT:
                        type = "Ultralight";
                        break;
                    case MifareUltralight.TYPE_ULTRALIGHT_C:
                        type = "Ultralight C";
                        break;
                }
                data.add(itemData(getString(R.string.mifare_ultralight_type), type));
            }
        }

        return data;
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    @Override
    public void onPause() {
        super.onPause();
        mAdapter.disableForegroundDispatch(this);
    }

    private Tag cleanupTag(Tag oTag) {
        if (oTag == null)
            return null;

        String[] sTechList = oTag.getTechList();

        Parcel oParcel = Parcel.obtain();
        oTag.writeToParcel(oParcel, 0);
        oParcel.setDataPosition(0);

        int len = oParcel.readInt();
        byte[] id = null;
        if (len >= 0) {
            id = new byte[len];
            oParcel.readByteArray(id);
        }
        int[] oTechList = new int[oParcel.readInt()];
        oParcel.readIntArray(oTechList);
        Bundle[] oTechExtras = oParcel.createTypedArray(Bundle.CREATOR);
        int serviceHandle = oParcel.readInt();
        int isMock = oParcel.readInt();
        IBinder tagService;
        if (isMock == 0) {
            tagService = oParcel.readStrongBinder();
        } else {
            tagService = null;
        }
        oParcel.recycle();

        int nfca_idx = -1;
        int mc_idx = -1;
        short oSak = 0;
        short nSak = 0;

        for (int idx = 0; idx < sTechList.length; idx++) {
            if (sTechList[idx].equals(NfcA.class.getName())) {
                if (nfca_idx == -1) {
                    nfca_idx = idx;
                    if (oTechExtras[idx] != null && oTechExtras[idx].containsKey("sak")) {
                        oSak = oTechExtras[idx].getShort("sak");
                        nSak = oSak;
                    }
                } else {
                    if (oTechExtras[idx] != null && oTechExtras[idx].containsKey("sak")) {
                        nSak = (short) (nSak | oTechExtras[idx].getShort("sak"));
                    }
                }
            } else if (sTechList[idx].equals(MifareClassic.class.getName())) {
                mc_idx = idx;
            }
        }

        boolean modified = false;

        if (oSak != nSak) {
            oTechExtras[nfca_idx].putShort("sak", nSak);
            modified = true;
        }

        if (nfca_idx != -1 && mc_idx != -1 && oTechExtras[mc_idx] == null) {
            oTechExtras[mc_idx] = oTechExtras[nfca_idx];
            modified = true;
        }

        if (!modified) {
            return oTag;
        }

        Parcel nParcel = Parcel.obtain();
        nParcel.writeInt(id.length);
        nParcel.writeByteArray(id);
        nParcel.writeInt(oTechList.length);
        nParcel.writeIntArray(oTechList);
        nParcel.writeTypedArray(oTechExtras, 0);
        nParcel.writeInt(serviceHandle);
        nParcel.writeInt(isMock);
        if (isMock == 0) {
            nParcel.writeStrongBinder(tagService);
        }
        nParcel.setDataPosition(0);

        Tag nTag = Tag.CREATOR.createFromParcel(nParcel);

        nParcel.recycle();

        return nTag;
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        resolveIntent(intent);
    }


    private void receiveData(){
        resultData.add(itemData(getString(R.string.receive_data), ""));
    }

    private void toMainHandler(int value){
        Message message = new Message();
        message.what = value;
        mHandler.sendMessage(message);
    }

    private IsoDepParse.OnMessageReceived onMessageReceived = new IsoDepParse.OnMessageReceived() {
        @Override
        public void onMessage(byte[] message) {
            log.info("onMessage, result=" + numeralParse.toReversedDec(message));
            log.info("onMessage, result2=" + numeralParse.toDec(message));
            log.info("onMessage, result3=" + numeralParse.toHex(message));
            log.info("onMessage, result4=" + numeralParse.toReversedHex(message));
            receiveData();
            resultData.add(itemData(getString(R.string.data_length), String.valueOf(message.length)));
            resultData.add(itemData(getString(R.string.data_hex), numeralParse.toHex(message)));
            resultData.add(itemData(getString(R.string.data_r_hex), numeralParse.toReversedHex(message)));
            resultData.add(itemData(getString(R.string.data_dec), String.valueOf(numeralParse.toDec(message))));
            resultData.add(itemData(getString(R.string.data_r_dec), String.valueOf(numeralParse.toReversedDec(message))));
            resultData.add(itemData(getString(R.string.data_to_string), new String(message)));
            toMainHandler(ISODEP_ONMESSAGERECEIVE);
        }

        @Override
        public void onError(Exception exception) {
            isoDepParse = null;
            receiveData();
            resultData.add(itemData(getString(R.string.error), exception.getMessage()));
            toMainHandler(ISODEP_ONMESSAGERECEIVE);
        }
    };
}
