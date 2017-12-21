package com.example.jelly.nfcusual.common;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.jelly.nfcusual.R;

import java.util.ArrayList;
import java.util.HashMap;

import darks.log.Logger;

/**
 * Created by jelly on 2017/12/1.
 * 读取NFC结果适配器
 */

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ResultViewHolder>{
    private static Logger log = Logger.getLogger(ResultAdapter.class);
    private ArrayList<HashMap<String, String>> resultData = new ArrayList<>();
    private Context context;

   class ResultViewHolder extends RecyclerView.ViewHolder{
       private TextView title;
       private TextView text;
       ResultViewHolder(View itemView) {
           super(itemView);
           title = (TextView) itemView.findViewById(R.id.list_title);
           text = (TextView) itemView.findViewById(R.id.list_text);
       }

       void bindData(HashMap<String, String> itemData) {
           title.setText(itemData.get("title"));
           text.setText(itemData.get("text"));
       }
   }

   public ResultAdapter(Context context){
       this.context = context.getApplicationContext();
   }

   public void setData(ArrayList<HashMap<String, String>> resultData) {
       this.resultData = resultData;
   }

    @Override
    public ResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.recycleview_list, parent, false);

        return new ResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ResultViewHolder holder, int position) {
        HashMap<String, String> itemData = resultData.get(position);
        log.info(resultData.get(position).toString());
        holder.bindData(itemData);
    }

    @Override
    public int getItemCount() {
        return resultData.size();
    }
}
