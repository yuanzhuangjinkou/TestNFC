package com.aofeng.testnfc;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.fragment.app.Fragment;

import com.aofeng.testnfc.databinding.FragmentFirstBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class FirstFragment extends Fragment implements NfcAdapter.ReaderCallback {

    private FragmentFirstBinding binding;

    private IsoDep isoDep;

    private byte[] random1;
    private byte[] random2;

    private byte[] ak;

    private Tag tag;

    private NfcAdapter mNfcAdapter;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);

        IntentFilter filter = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        Intent intent = new Intent(this.getContext(), getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this.getContext());

        if (mNfcAdapter == null) {
            Log.d("FirstFragment", "onCreateView: 设备不支持nfc");
        }

        if (!mNfcAdapter.isEnabled()) {
            startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));
        }

        mNfcAdapter.enableReaderMode(getActivity(), this, NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonRead4442.setOnClickListener(view1 -> new Thread(() -> {
            byte[] result = this.writeWithTag("00A4040007D2760000850102");
            Log.d("TAG", "onViewCreated333333: " + HexDump.decBytesToHex(result));


            result = this.writeWithTag("00B0000000");
            Log.d("TAG", "onViewCreated444444: " + Arrays.toString(result));

            String cardinfo = HexDump.decBytesToHex(result);

            log("读取卡内数据返回： " + cardinfo);

            // 发送到诚然服务器
            String url = "http://117.34.91.57:8003/ReadCard";

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("CardType", "1");
                jsonObject.put("cardinfo", cardinfo);
                jsonObject.put("operatetype", 1);
                jsonObject.put("AREA", "BaiShui");
                jsonObject.put("readParam", "{}");

                String returnData = HexDump.post(url, jsonObject);
                if ("".equalsIgnoreCase(returnData)) {
                    log("读取卡上数据信息失败");
                } else {
                    JSONObject resultJSON = new JSONObject(returnData);
                    log("读取成功，返回");
                    log(returnData);
                    // 设置卡号
                    setTextView(binding.textviewCardid2, resultJSON.optString("CardID"));
                    setTextView(binding.edittextGas2, resultJSON.optString("Gas"));
                    setTextView(binding.edittextTimes2, resultJSON.optString("Times"));

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start()
        );


        binding.buttonWrite4442.setOnClickListener(view1 -> new Thread(() -> {
            byte[] result = this.writeWithTag("00A4040007D2760000850102");
            Log.d("TAG", "onViewCreated333333: " + HexDump.decBytesToHex(result));
            int length = result.length;
            if(!(result[length-2] == 0x90 && result[length-1] == 0x00)){
                Log.d("TAG", "onViewCreated444444: " + Arrays.toString(result));
                return;
            }

            result = this.writeWithTag("00B0000000");
            Log.d("TAG", "onViewCreated444444: " + Arrays.toString(result));

            String CardData = HexDump.decBytesToHex(result);

            //
            String url = "http://117.34.91.57:8003/WriteGasCard";

            try {
                JSONObject jsonObject = new JSONObject("{\"csql\":\"7606\",\"lastmoney\":\"24111.02\",\"ccsql\":\"3155\",\"lastlastmoney\":\"10001.35\",\"ljgql\":\"243364\",\"ljyql\":\"0\",\"yhh\":\"100100\",\"sxrq\":\"20210924\",\"totalmoney\":\"774439.39\",\"stairgas1\":\"77777777\",\"stairgas2\":\"88888888\",\"stairgas3\":\"99999999\",\"stairprice1\":\"3.170\",\"stairprice2\":\"3.170\",\"stairprice3\":\"3.170\",\"factory\":\"XinDong\",\"kh\":\"00000009\",\"kmm\":\"null\",\"ql\":\"6426\",\"cs\":\"42\",\"money\":\"20370.42\",\"dqdm\":\"0\",\"klx\":\"null\",\"ljgqje\":\"774439.39\",\"AREA\":\"BaiShui\",\"opid\":1,\"operatetype\":1}");
                jsonObject.put("ql", binding.edittextBuygas.getText().toString());
                jsonObject.put("CardData", CardData);
                jsonObject.put("CardType", 1);
                String returnData = HexDump.post(url, jsonObject);


                if ("".equalsIgnoreCase(returnData)) {
                    log("写卡失败");
                } else {
                    JSONObject resultJSON = new JSONObject(returnData);

                    String Kdata = resultJSON.optString("Kdata");
                    String cscKmm = resultJSON.optString("CscKmm");

                    String writeData = "00D60020E3" + cscKmm.substring(0, 6) + Kdata.substring(0x20 * 2);

                    result = this.writeWithTag(writeData);
                    Log.d("TAG", "onViewCreated555555: " + HexDump.decBytesToHex(result));

                    log("写卡成功");
                }
                log(returnData);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }).start()
        );

        binding.buttonClear.setOnClickListener(v -> binding.textview.setText(""));

        binding.buttonRead102.setOnClickListener(v->new Thread(()->{

            try {

                byte[] result = this.writeWithTag("00A4040007D2760000850101");
                Log.d("TAG", "onViewCreated111111111: " + HexDump.decBytesToHex(result));

                result = this.writeWithTag("00A4000002DF01");
                Log.d("TAG", "onViewCreated2222222222: " + HexDump.decBytesToHex(result));

                boolean ret = getTongdao();
                if (!ret) {
                    return;
                }
                // 读所有数据
                result = this.writeWithTag("00B00D0001B2");
                Log.d("TAG", "onViewCreated3333333333: " + HexDump.decBytesToHex(result));

                byte[] data_ec = new byte[result.length - 10];
                System.arraycopy(result, 0, data_ec, 0, data_ec.length);

                byte[] data = Encrypt.decrypt_ECB(ak, data_ec);

                Log.d("TAG", "读卡返回: " + HexDump.toHexString(data, 1, data[0] & 0xFF));




            }catch (Exception e){
                e.printStackTrace();
            }

        }).start());


        binding.buttonWrite102.setOnClickListener((v)->new Thread(()->{

            try {

                byte[] result = this.writeWithTag("00A4040007D2760000850101");
                Log.d("TAG", "onViewCreated111111111: " + HexDump.decBytesToHex(result));

                result = this.writeWithTag("00A4000002DF01");
                Log.d("TAG", "onViewCreated2222222222: " + HexDump.decBytesToHex(result));

                boolean ret = getTongdao();
                if (!ret) {
                    return;
                }

                // 校验密码
                byte[] verifyData = new byte[256];

                verifyData[0] = 0x04;
                verifyData[1] = 0x33;
                verifyData[2] = 0x00;
                verifyData[3] = 0x00;

                byte[] key = Encrypt.encrypt_ECB(ak, HexDump.hexStringToByteArray("02F0F0"));
                verifyData[4] = (byte) (8 + key.length);
                System.arraycopy(key, 0, verifyData, 5, key.length);

                byte[] macold = Encrypt.encrypt_CBC(ak, new byte[16], verifyData, 5 + key.length);
                Log.d("TAG", "macold: " + HexDump.toHexString(macold));
                System.arraycopy(macold, macold.length - 16, verifyData, 5 + key.length, 8);
                result = this.writeWithTag(HexDump.toHexString(verifyData, 0, 13 + key.length));
                Log.d("TAG", "核对密码返回: " + HexDump.toHexString(result));

                if( !(result[0] == (byte) 0x90 && result[1] == 0x00) ){
                    return;
                }
                // 写入数据 0x20写入数据，A2131091
                byte[] writeData = new byte[256];
                writeData[0] = 0x04;
                writeData[1] = (byte) 0xD6;
                writeData[2] = 0x0D;
                writeData[3] = 0x20;

                byte[] write = Encrypt.encrypt_ECB(ak, HexDump.hexStringToByteArray("04A2131091"));
                writeData[4] = (byte) (8 + write.length);

                System.arraycopy(write, 0, writeData, 5, write.length);

                macold = Encrypt.encrypt_CBC(ak, new byte[16], writeData, 5 + write.length);
                Log.d("TAG", "macold: " + HexDump.toHexString(macold));
                System.arraycopy(macold, macold.length - 16, writeData, 5 + write.length, 8);
                result = this.writeWithTag(HexDump.toHexString(writeData, 0, 13 + write.length));
                Log.d("TAG", "写卡返回: " + HexDump.toHexString(result));


            }catch (Exception e){
                e.printStackTrace();
            }

        }).start());

    }


    private boolean getTongdao() throws Exception{
        // 建立安全通道
        byte[] result = this.writeWithTag("008A000021");
        Log.d("TAG", "onViewCreated2222222222: " + HexDump.decBytesToHex(result));

        if (result == null) {
            log("建立安全通道失败");
            return false;
        }

        byte keyType = result[0];

        byte[] uuid = new byte[16];

        System.arraycopy(result, 1, uuid, 0, 16);

        byte[] randomEncrypt = new byte[16];

        System.arraycopy(result, 17, randomEncrypt, 0, 16);
        //
        byte[] key = Encrypt.getKey(keyType, uuid);
        Log.d("TAG", "key: " + HexDump.decBytesToHex(key));
        random1 = Encrypt.decrypt_ECB(key, randomEncrypt);

        Log.d("TAG", "random1: " + HexDump.decBytesToHex(random1));

        byte[] data1 = Encrypt.xor(random1,uuid);
        random2 = Encrypt.getRandomBytes(16);
        Log.d("TAG", "random2: " + HexDump.decBytesToHex(random2));
        byte[] data2 = new byte[32];


        System.arraycopy(data1, 0, data2, 0, 16);

        System.arraycopy(random2, 0, data2, 16, 16);


        byte[] sendSecond = Encrypt.encrypt_ECB(key, data2);
        Log.d("TAG", "sendSecond: " + HexDump.decBytesToHex(sendSecond));
        result = this.writeWithTag("008B000020" + HexDump.decBytesToHex(sendSecond));
        Log.d("TAG", "008B000020: " + HexDump.decBytesToHex(result));



        ak = Encrypt.getRandomBytes(16);

        byte[] sendThird = Encrypt.encrypt_ECB(key, Encrypt.xor(ak,random1));

        result = this.writeWithTag("008c000010" + HexDump.decBytesToHex(sendThird));
        Log.d("TAG", "008c000010: " + HexDump.decBytesToHex(result));

        if(result[0] == (byte) 0x90 && result[1] == 0x00){
            return true;
        }
        return false;
    }


    private String readTextFromTag(Tag tag) {
        Log.d("", "readTextFromTag: " + tag);
        IsoDep isoDep = IsoDep.get(tag);

        NfcA nfcA = NfcA.get(tag);

        if(nfcA == null && isoDep == null){
            return null;
        }
//        this.nfcA = nfcA;
//        try {
//            nfcA.connect();
//            byte[] data = nfcA.transceive(new byte[]{0x30, 0x04}); // 读取数据的命令
//            Log.d("", "readTextFromTag: " + HexDump.decBytesToHex(data));
//        }catch (Exception e){
//
//        }
        this.isoDep = isoDep;
        return HexDump.decBytesToHex(isoDep.getHistoricalBytes());
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        mNfcAdapter.disableReaderMode(getActivity());
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        // Read the data from the tag
        String text = readTextFromTag(tag);

        this.tag = tag;

        Log.d("FirstFragment", "onTagDiscovered: nfc返回数据 : " + tag);

        Log.d("FirstFragment", "onTagDiscovered: nfc返回数据 : " + text);

        log(text);

    }

    @SuppressLint("SetTextI18n")
    @UiThread
    private void log(final String text){
        Log.d("TAG", text);
        final TextView textView = this.binding.textview;
        this.getActivity().runOnUiThread(() -> {
            textView.append(text + "\n");
        });
    }

    @UiThread
    private void setTextView(final TextView textView,final String text){
        Log.d("TAG", text);
        this.getActivity().runOnUiThread(() -> textView.setText(text));
    }


    private byte[] writeWithTag(String data) {
        try {
            if(!isoDep.isConnected())
                isoDep.connect();
            Log.d("TAG", "writeWithTag: "+ data);
            byte[] command = HexDump.hexStringToByteArray(data);

            return isoDep.transceive(command);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}